///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2006  James Di Vincenzo
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
///////////////////////////////////////////////////////////////////////////////////
package edu.regis.jprobe.model;
import static edu.regis.jprobe.model.Logger.DEBUG;
import static edu.regis.jprobe.model.Utilities.debugMsg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author jdivinc
 *
 */
public class IdentityFileManager {
    
    private static final String FILE_NAME = "ProbeIdentity.dat";
    private File identityFile;
    private FileLock fileLock;
    private RandomAccessFile raf;
    private long timeout = 5000;
    private Exception lastException;
    private FileChannel channel;
    private List<String> errors = new ArrayList<String>();
    private String fileName;
    private Logger logger;
    
    private final static int BUFF_LEN = 1048576;
    
    public IdentityFileManager(Logger logger) {
        
        identityFile = new File(getProbeDirectory() + File.separator + FILE_NAME);
        fileName = identityFile.getAbsolutePath();
        this.logger = logger;
        
    }
    
    public List<BroadcastMessage> getProbes() {
        errors.clear();
        
        List<BroadcastMessage> probes = new ArrayList<BroadcastMessage>();
        
        if (identityFile.exists()) {
            try {
                log(DEBUG, "Getting Indentity File Entries");
                if (getLock(false)) { 
                    Map<String, BroadcastMessage> probeMap = read();
                    log(DEBUG, "ProbeMap has " + probeMap.size() + " entries");
                    probes.addAll(probeMap.values());
                } else {
                    log(DEBUG, "Can't get Identity File Lock"); 
                }
            } finally {
                freeLock();
            }
        } else {
            log(DEBUG, "Identity File " + identityFile.getAbsolutePath() + " Does not Exist");
        }
        
        return probes;
    }
    public boolean saveBroadcast(BroadcastMessage bm) {
        
        errors.clear();
        boolean ret = false;
        Map<String, BroadcastMessage> probeMap = null;
        log(DEBUG, "Adding Entry for " + bm.toString());
        try {
            if (getLock(true)) {
                if (identityFile.length() > 0) {
                    log(DEBUG, "Getting Existing Entries");
                    probeMap = read();
                } else {
                    log(DEBUG, "No Existing Entries");
                    probeMap = new HashMap<String, BroadcastMessage>();
                }
            } else {
                ret = false;
                errors.add("Unable to Obtain File Lock");
                return false;
            }
            probeMap.put(bm.getKey(), bm);
            write(probeMap);
            ret = true;
            
        } finally {
            freeLock();
        }
        
        return ret;
        
    }
    public boolean removeBroadcast(BroadcastMessage bm) {
        
        errors.clear();
        boolean ret = true;
        //boolean deleteFile = false;
        Map<String, BroadcastMessage> probeMap = null;
        log(DEBUG, "Removing Entry for " + bm.toString());
        try {
            if (identityFile.exists()) {
                if (getLock(true)) {
                    probeMap = read();
                    probeMap.remove(bm.getKey());
                    
                    if (!probeMap.isEmpty()) {
                        write(probeMap);
                    } else {
                        //deleteFile = true; 
                    }
                    ret = true;
                } else {
                    ret = false;
                    errors.add("Unable to Obtain File Lock");
                }
            }
        } finally {
            freeLock();
        }
        
        /*if (deleteFile) {
            if (!identityFile.delete()) {
                errors.add("Delete of file " + 
                        identityFile.getAbsolutePath() + 
                        " failed, set to delete on exit.");
                identityFile.deleteOnExit();
            }
        }*/
        return ret;
        
    }
    @SuppressWarnings("unchecked")
    private Map<String, BroadcastMessage> read() {
        
        log(DEBUG, "Reading Entries");
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        Map<String, BroadcastMessage> ret = new HashMap<String, BroadcastMessage>();
        
        try {
            long length = raf.length();
            log(DEBUG, "Reading Map, Size is " + length);
            byte[] buff = new byte[(int) length];
            raf.readFully(buff);
            bais = new ByteArrayInputStream(buff);
            ois = new ObjectInputStream(bais);
            ret = (Map<String, BroadcastMessage>) ois.readObject();
        } catch (Exception e) {
            lastException = e;
            errors.add("Error Opening Input Stream for file " + 
                    identityFile.getAbsolutePath() + ", Error is " + e.getMessage());
        } finally {
           try {
                if (ois != null) {
                    ois.close();
                }
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException e) {
                lastException = e;
                errors.add("Error Closing Input Stream for file " + 
                        identityFile.getAbsolutePath() + ", Error is " + e.getMessage());
            }
        }
        for (BroadcastMessage bm : ret.values()) {
            log(DEBUG, "Found Entry for " + bm.toString());
        }
        return ret;
    }
    private void write(Map<String, BroadcastMessage> map) {
        
        for (BroadcastMessage bm : map.values()) {
            log(DEBUG, "Writing Entry for " + bm.toString());
        }
        
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        
        
        try {
            
            baos = new ByteArrayOutputStream(BUFF_LEN);
            oos = new ObjectOutputStream(baos);
            oos.writeObject(map);
            log(DEBUG, "Writing Map, Size is " + baos.size());
            raf.write(baos.toByteArray());
        } catch (Exception e) {
            lastException = e;
            errors.add("Error Opening Output Stream for file " + 
                    identityFile.getAbsolutePath() + ", Error is " + e.getMessage());
        } finally {
           try {
            if (oos != null) {
                oos.close();
            }
            if (baos != null) {
                baos.close();
            }
            } catch (IOException e) {
                lastException = e;
                errors.add("Error Closing Output Stream for file " + 
                        identityFile.getAbsolutePath() + ", Error is " + e.getMessage());
            }
        }
        
        
    }
    private String getProbeDirectory() {
        
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            
        URL urls[] = sysloader.getURLs();
                
        for (int i = 0; i < urls.length; i++) {
            File f = new File(urls[i].getFile());
            if (f.getName().equalsIgnoreCase(Utilities.JAR_NAME)) {
               String path =  f.getPath();
               return path.substring(0, path.length() - Utilities.JAR_NAME.length());
            }
            
        }
        
        return new File(".").getPath();
    }
    
    private boolean getLock(boolean noWait)  {
        
        long start = System.currentTimeMillis();
        
        try {
            raf = new RandomAccessFile(identityFile, "rw");
            channel = raf.getChannel();
            
            while (System.currentTimeMillis() - start < timeout) {
                fileLock = channel.tryLock();
                if (fileLock != null) {
                    return true;
                }
                if (noWait) {
                    return false;
                }
                Utilities.sleep(50);
            }
        } catch (IOException e) {
            lastException = e;
            errors.add("Error Locking Broadcast file " + 
                    identityFile.getAbsolutePath() + ", Error is " + e.getMessage());
        } 
                
            
        return false;
    }
    
    private void freeLock()  {
        
        if (fileLock == null) {
            return;
        }
        
        try {
            fileLock.release();
            channel.close();
            raf.close();
        } catch (IOException e) {
            lastException = e;
            errors.add("Error Unlocking Broadcast file " + 
                    identityFile.getAbsolutePath() + ", Error is " + e.getMessage());
        }
        fileLock = null;
        raf = null;
    }

    /**
     * @return the lastException
     */
    public final Exception getLastException() {
        return lastException;
    }

    /**
     * @return the errors
     */
    public final List<String> getErrors() {
        return errors;
    }
    
    public final boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * @return the fileName
     */
    public final String getFileName() {
        return fileName;
    }
    
    private void log(int level, String msg) {
        if (logger != null) {
            logger.log(level, msg);
        } else {
            debugMsg(msg);
        }
    }

}
