///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2011  James Di Vincenzo
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

import static edu.regis.jprobe.model.Utilities.compress;
import static edu.regis.jprobe.model.Utilities.decompress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class RecordingManager {
    
    private String recordingFileName;
    private File recordingFile;
    private RandomAccessFile raf;
 
    private int mode = 0;
    private int writeCount = 0;
    private int readCount = 0;
    private long readBytes = 0;
    private long readUncompressedBytes = 0;
    private long writeBytes = 0;
    private long writeUncompressedBytes = 0;
    private long readPointer = 0;
    private boolean fileIsOpen = false;
    
    private List<Long> recordIndex;
    private volatile ProbeResponse lastResponse;
    
    public static final int RECORDING_MODE = 1;
    public static final int PLAYBACK_MODE = 2;
 
    public RecordingManager(String filename, int mode) throws IOException {
        
        
        if (mode == RECORDING_MODE) {
            this.mode = RECORDING_MODE;
        
            this.recordingFile = new File(filename);
            raf = new RandomAccessFile(recordingFile, "rw");
            this.recordingFileName = recordingFile.getAbsolutePath();
        } else if (mode == PLAYBACK_MODE){
            this.mode = PLAYBACK_MODE;
            this.recordingFile = new File(filename);
            raf = new RandomAccessFile(recordingFile, "r");
            this.recordingFileName = recordingFile.getAbsolutePath();
            recordIndex = new ArrayList<Long>();
            buildIndex();
            //printIndex();
            readPointer = 0;
        } else {
            throw new IllegalArgumentException("Invalid Mode(" + mode + ")");
        }
        fileIsOpen = true;
    }
    
    public void close() throws IOException {
        
        if (mode == PLAYBACK_MODE) {
            if (raf == null) {
                return;
            }
            raf.close();
            raf = null;
            
        } else {
            if (raf == null ) {
                return;
            }
            raf.close();
            raf = null;
       }
        fileIsOpen = false;
    }
    public void writeResponse(ProbeResponse pr) throws IOException {
        
        if (mode == PLAYBACK_MODE) {
            throw new IllegalStateException("Write is Invalid in Playback Mode");
        }
        
        if (raf == null ) {
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        writeUncompressedBytes += baos.size();
        oos.writeObject(pr);
        //byte[] data = baos.toByteArray();
        byte[] data = compress(baos.toByteArray());
        int len = data.length;
        
        raf.writeInt(len);
        raf.write(data);
        
        writeCount++;
        writeBytes += len;
        

       
    }
    protected boolean seek(int recNumber) throws IOException {
        
        Long recPtr = recordIndex.get(recNumber);
        
        if (recPtr == null) {
            return false;
        }
        
        readPointer = recPtr;
        raf.seek(readPointer);
        
        return true;
    }
    public ProbeResponse readResponse(int record) throws IOException,
    ClassNotFoundException {
        
        if (record > recordIndex.size() || record < 0) {
            throw new IllegalArgumentException("Record Number is Invalid, Specified " +
                    record + ", max is " + recordIndex.size());
        }
        //System.out.println("Reading Record " + record);
        if (record == recordIndex.size()) {
            throw new EOFException();
        }
        long pointer = recordIndex.get(record);
        return readResponse(pointer);
        
    }
    public ProbeResponse readResponse() throws IOException,
        ClassNotFoundException {
        return readResponse(readPointer);
        
    }
    public synchronized ProbeResponse readResponse(long pointer) throws IOException, 
        ClassNotFoundException {
        
        if (mode == RECORDING_MODE) {
            throw new IllegalStateException("Read is Invalid in Record Mode");
        }
        raf.seek(pointer);
        int len = raf.readInt();
        
        byte[] data = new byte[len];
        int read = raf.read(data);
        byte[] dataDecomp = decompress(data); 
        
        if (read != len) {
            throw new IOException("Data Length read doed not match stored value, " +
                    read + " bytes read, " + len + " expected");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(dataDecomp);
        ObjectInputStream ois = new ObjectInputStream(bais);
        ProbeResponse res =  (ProbeResponse) ois.readObject();
        
        readCount++;
        readUncompressedBytes += dataDecomp.length;
        readBytes += len;
        readPointer = raf.getFilePointer();
        lastResponse = res;
        return res;
        
    }
    private void buildIndex() throws IOException {

        long pointer = 0;
        boolean eof = false;
        recordIndex.clear();
        
        raf.seek(0);
        
        while (!eof) {
            try {
                int len = raf.readInt();
                recordIndex.add(pointer);
                //System.out.println("Record at " + pointer + " for a length of " + len);
                pointer = raf.getFilePointer() + len;
                raf.seek(pointer);
            } catch (EOFException e) {
                eof = true;
            }
        }
    }

    public String getRecordingFileName() {
        return recordingFileName;
    }
    public int getMode() {
        return mode;
    }
    public int getWriteCount() {
        return writeCount;
    }
    public int getReadCount() {
        return readCount;
    }

    public long getReadBytes() {
        return readBytes;
    }

    public long getWriteBytes() {
        return writeBytes;
    }
    
    public long getReadUncompressedBytes() {
        return readUncompressedBytes;
    }

    public long getWriteUncompressedBytes() {
        return writeUncompressedBytes;
    }

    public int getNumberOfRecords() {
        return recordIndex.size();
    }
    
    public ProbeResponse getLastResponse() {
        return lastResponse;
    }
    public boolean isOpen() {
        return fileIsOpen;
    }
    public void printIndex() {
        
        for (int i = 0; i < recordIndex.size(); i++) {
            
            System.out.println("Record " + i + " is at Offset " + recordIndex.get(i) );
        }
    }

}
