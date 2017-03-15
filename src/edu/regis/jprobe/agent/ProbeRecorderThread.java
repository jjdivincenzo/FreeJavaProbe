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
package edu.regis.jprobe.agent;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


import edu.regis.jprobe.model.ProbeRequest;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.RecordingManager;
import edu.regis.jprobe.model.Utilities;

import static edu.regis.jprobe.model.Utilities.VERSION_HEADING;
import static edu.regis.jprobe.model.Utilities.debugMsg;
import edu.regis.jprobe.ui.UIOptions;

/**
 * This class is the probe recorder thread.
 * @author jdivince
 *
 */
public class ProbeRecorderThread extends Thread {
    
    private ProbeThread probe;
    private RecordingManager recMgr;
    private long recordingInterval = 1000;
    private String hostName;
    private String hostIP;
       
    public static final String ENABLE_RECORDING = "JPROBE_ENABLE_RECORDING";
    public static final String RECORDING_INTERVAL = "JPROBE_RECORDING_INTERVAL";
    public static final String RECORDING_DIRECTORY = "JPROBE_RECORDING_DIRECTORY";
    
    public ProbeRecorderThread(ProbeThread probe) {
        
        this.probe = probe;
        String val = System.getenv(RECORDING_INTERVAL);
        
        if (val != null) {
            try {
                recordingInterval = Integer.parseInt(val); 
                if (recordingInterval > 10000 || recordingInterval < 100) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                debugMsg("Value Specified for Recording Interval is invalid");
            }
            
        }
        
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            hostIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostName = "localhost";
            hostIP = "127.0.0.1";
        }
        
        
    }
        
    public void run() {
        
        if (!openRecording()) {
            return;
        }
        System.out.println(VERSION_HEADING + 
                " - Recording Thread Started, Writing to " + 
                recMgr.getRecordingFileName() + 
                ", Recording Interval is " + recordingInterval + 
                "ms");
        
        while(!probe.isShutdown()) {
            
            ProbeResponse resp = probe.processRequest(createRequest(ProbeRequest.REQ_STATS));
            
            try {
                recMgr.writeResponse(resp);
                Thread.sleep(recordingInterval);
            } catch (IOException e) {
                debugMsg("IOException writing probe response, error is " + 
                        e.getMessage());
                if (Utilities.debug) {
                    e.printStackTrace();
                }
                break;
            } catch (InterruptedException e) {
                debugMsg("Recorder Thread Interrupted, Recording Terminated");
                break;
            }
            //Refresh Class info every 500 writes
            if (recMgr.getWriteCount() % 500 == 0) {
                probe.processRequest(createRequest(ProbeRequest.REQ_CLASS_INFO)); 
            }
            
        }
        
        try {
            recMgr.close();
        } catch (IOException e) {
            debugMsg("IOException closing Recording, error is " + e.getMessage());
        }
        
    }
    
    private boolean openRecording() {
        String path = getRecordingDirectory();
        File recPath = new File(path);
        recPath.mkdir();
        String fileName = path + probe.getProbeName() + "_" + hostName + "_" +
                Utilities.formatTimeStamp(System.currentTimeMillis(), "MMddyyyyHHmmss")
                + UIOptions.RECORDING_SUFFIX;
  
        try {
            recMgr = new RecordingManager(fileName, RecordingManager.RECORDING_MODE);
       } catch (IOException e) {
            debugMsg("Error Opening Recording file, Error is " + e.getMessage() + 
                    "\nRecording Terminated");
            return false;
        }

        probe.processRequest(createRequest(ProbeRequest.REQ_CLASS_INFO));
        return true;
    
    }

    private String getRecordingDirectory() {
        
        String dir = System.getenv(RECORDING_DIRECTORY);
        
        if (dir != null) {
            File f = new File(dir);
            if (f.isDirectory() ) {
                if (dir.endsWith(File.separator)) {
                    return dir;
                }
                return dir + File.separator;
            }
        }
        
        File f = new File(".");
        return f.getAbsolutePath() + File.separator;
    }

    
    public static boolean isRecordingEnabled() {
        
        return ("true".equalsIgnoreCase(System.getenv(ENABLE_RECORDING)));
    }

    private ProbeRequest createRequest(int reqType) {
        
        ProbeRequest req = new ProbeRequest();
        req.setRequestType(reqType);
        req.setClientHostName(hostName);
        req.setClientIP(hostIP);
        
        
        return req;
        
    }
}
