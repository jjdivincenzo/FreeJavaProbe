package edu.regis.jprobe.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.regis.jprobe.ui.UIOptions;

public class DynamicLibraryManager {
    
    public static final String DEFAULT_FILE_NAME = "DynamicLibraries.xml";
    private String fileName = DEFAULT_FILE_NAME;
    private Logger logger;
    private static final String CACHE_DIRECTORY = "cache";
    private String cacheDirectory;
    private UIOptions props;
    private List<String> libraries;
    public DynamicLibraryManager() {
        
        libraries = new ArrayList<String>();
        logger = Logger.getLogger();
        
        File curDir = new File(".");
        props = UIOptions.getOptions();
        File cacheDir = new File(curDir.getAbsolutePath() + File.separator + CACHE_DIRECTORY);
        cacheDir.mkdirs();
        cacheDirectory = cacheDir.getAbsolutePath() + File.separator;
    }
    
    public void restore(String fileName) {
        
        if (fileName == null) {
            this.fileName = DEFAULT_FILE_NAME;
        } else {
            this.fileName = fileName;
        }
        
        File libraryFile = new File(this.fileName);
        
        if (!libraryFile.exists()) {
            logger.info("Creating Dynamic Libraries");
            save();
            return;
        }
        
        FileInputStream fis = null;
        
        try {
            fis = new FileInputStream(libraryFile);
            Properties props = new Properties();
            props.loadFromXML(fis);
            loadFromProperties(props);
        } catch (IOException e) {
            logger.error("Error Loading Library File " + libraryFile.getAbsolutePath());
            logger.logException(e, this);
            
        }
        
        
        
    }
    
    public void save() {
        
        FileOutputStream fos = null;
        Properties props = new Properties();
        File excludeFile = new File(fileName);
        for (int i = 0; i < libraries.size(); i++) {
            String filter = libraries.get(i);
            props.put("CLASSFILE." + i, filter);
        }
        
        try {
            fos = new FileOutputStream(excludeFile);
            props.storeToXML(fos, "JProbe Dynamic Classpath Files");
        } catch (IOException e) {
            logger.error("Error Saving Library File " + excludeFile.getAbsolutePath());
            logger.logException(e, this);
        }
        
    }
    
    private void loadFromProperties(Properties props) {
        
        Set<Object> keys = props.keySet();
        Iterator<Object> iter = keys.iterator();
        libraries.clear();
        int count = 0;
        
        while (iter.hasNext()) {
            String key = iter.next().toString();
            String value = props.getProperty(key);
            libraries.add(value);
            logger.debug("Loading Library " + value);
            count++;
        }
        logger.info(count + " Libraries loaded from " + fileName);
    }

    
    public boolean addLibrary(String filter) {
        
        if (libraries.contains(filter.trim())) {
            return false;
        } 
        
        libraries.add(filter.trim());
        save();
        return true;

    }
    public File cacheLibrary(String fileName) {
        
        File src = new File(fileName);
        
        if (!src.exists()) {
            logger.error("Specified Library " + fileName + 
                    " Does Not Exist, Cannot Be Loaded");
            return null;
        }
        if (props.isCacheExternalJars()) {
            File target = new File(cacheDirectory + src.getName());
            try {
                copyFile(src, target);
            } catch (IOException e) {
                logger.error("Error Copying Specified Library " + fileName);
                logger.logException(e, this);
                return null;
            }
            return target;
        }
        
        return src;

    }

    public boolean removeLibrary(String filter) {
        
        if (!libraries.contains(filter.trim())) {
            return false;
        } 
            
        libraries.remove(filter.trim());
        return true;

    }
    public List<String> getAllLibraries() {
        
        List<String> ret = new ArrayList<String>();
        
        for (String filter : libraries) {
            ret.add(filter);
        }
        
        Collections.sort(ret);
        return ret;
    }
    public void setAllLibraries(List<String> libraries) {
        this.libraries = libraries;
    }
    public void loadAllLibraries() {
        
        
        int missingLibs = 0;
        logger.info("Loading Saved Dynamic Libraries");
        for (String lib : getAllLibraries()) {
            
            File f = new File(lib);
            
            if (f.exists() ) {
                
                    try {
                        if (props.isCacheExternalJars()) {
                            File cached = new File(cacheDirectory + f.getName());
                            if (!cached.exists()) {
                                logger.info("Loading Library " + lib + " into the cache");
                                copyFile(f, cached);
                            } else {
                                if (f.lastModified() != cached.lastModified() ||  
                                        f.length() != cached.length()) {
                                    logger.info("Library " + lib + " has changed, reloading into the cache");
                                    copyFile(f, cached); 
                                }
                            }
                            ClassPathUpdater.addFile(cached);
                        } else {
                            ClassPathUpdater.addFile(f);
                        }
                    } catch (IOException e) {
                        logger.error("Load of Library " + lib + " Failed");
                        logger.logException(e, this);
                    } 
                
            } else {
                logger.warning("Dynamic Library File " + lib + 
                        " Does Not Exist. Removing from Saved List");
                removeLibrary(lib);
                missingLibs++;
            }
        }
        
        if (missingLibs > 0) {
            save();
        }
    }

    private void copyFile(File src, File target) throws IOException {
        
        long lastTime = src.lastModified();
        
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(target);
        
        boolean eof = false;
        while (!eof) {
            byte[] buffer = new byte[4096];
            int len = fis.read(buffer);
            
            if (len > 0) {
                fos.write(buffer, 0, len);
            } else {
                eof = true;
            }
        }
        fos.close();
        fis.close();
        target.setLastModified(lastTime);
        
    }
    public int getSize() {
        return libraries.size();
    }
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("*** Libraries ***\n");
        
        for (String filter : libraries) {
            sb.append("\t").append(filter).append("\n");
        }
        
        return sb.toString();
    }




    public static void main(String[] args) throws Exception {
        
        DynamicLibraryManager sef = new DynamicLibraryManager();
        
        sef.restore(null);
        
        sef.addLibrary("com.sybase.");
        sef.addLibrary("com.oracle.");
        sef.addLibrary("com.ibm.");
        
        System.out.println(sef.toString());
        
        sef.removeLibrary("com.ibm.");
        
        System.out.println(sef.toString());
        sef.removeLibrary("com.sybase.");
        sef.removeLibrary("com.oracle.");
        
        sef.save();
        
    }

}
