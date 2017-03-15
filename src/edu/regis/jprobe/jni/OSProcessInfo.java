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
package edu.regis.jprobe.jni;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import edu.regis.jprobe.model.Utilities;

/**
 * @author jdivinc
 *
 */
/**
 * @author jdivinc
 *
 */
public class OSProcessInfo {
    
    private String processName;
    private String imageType;
    private String user;
    private long processId;
    private long parentPID;
    private long threadCount;
    private long handleCount;
    private long totalCPU;
    private long userTime;
    private long kernelTime;
    private long ioReads;
    private long ioWrites;
    private long ioOther; 
    private long ioReadBytes; 
    private long ioWriteBytes;
    private long ioOtherBytes;
    private long pageFaults;
    private long workingSetSize;
    private long peakWorkingSetSize;
    private long pageFileUsage;
    private long peakPageFileUsage;
    private long privateUsage;
    
    private List<String> errors = new ArrayList<String>();
    
    private static final String DELIM_KV = "=";
    private static final String DELIM_ENTRY = ";";
    
    public OSProcessInfo() {
        this.processId = -1;
    }
    
    public OSProcessInfo(String parseString) {
        
        Map<String, String> dataMap = getData(parseString);
        
                
        Field[] fields = this.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            
            String name = field.getName().toUpperCase();
            String data = dataMap.get(name);
            Class<?> type = field.getType();
            String typeName = type.getSimpleName();
            if (data != null) {
                
                switch (typeName) {
                    case "String" :
                        setString(field, data);
                        break;
                    case "long" :
                        setLong(field, data);
                        break;
                    default:
                        errors.add("Unsupported Data Type of " + typeName + " for " + field.getName());    
                }
            }
        }
        
    }
    public OSProcessInfo(OSProcessInfo pi) {
        
        Field[] fields = this.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            try {
                Object data = field.get(pi);
                field.set(this, data);
            } catch (Exception e) {
                errors.add("Error " + e.getMessage() + " setting " +
                        field.getName());
            }
        }
        
    }
    private void setLong(Field field, String data) {
        long lval = -1;
        
        try {
            lval = Long.parseLong(data);
        } catch (Exception e) {
            errors.add("Error Parsing " + 
                    field.getName() + ", it is not numeric, value is " +
                    data);
        }
        try {
            field.setLong(this, lval);
        } catch (Exception e) {
            errors.add("Error " + e.getMessage() + " setting " +
                    field.getName());
        }
        
    }
    private void setString(Field field, String data) {
        
        try {
            field.set(this, data);
        } catch (Exception e) {
            errors.add("Error " + e.getMessage() + " setting " +
                    field.getName());
        }
        
    }
    private Map<String, String> getData(String str) {
        
        Map<String, String> map = new HashMap<String, String>();
        StringTokenizer stEntry = new StringTokenizer(str, DELIM_ENTRY);
        
        while (stEntry.hasMoreTokens()) {
            String element = stEntry.nextToken();
            StringTokenizer stKV = new StringTokenizer(element, DELIM_KV);
            while (stKV.hasMoreTokens()) {
                String key = stKV.nextToken();
                String value = stKV.nextToken();
                map.put(key.trim().toUpperCase(), value);
            }
        }
        
        return map;
        
    }
    /**
     * @return the processName
     */
    public final String getProcessName() {
        return processName;
    }
    /**
     * @return the processId
     */
    public final long getProcessId() {
        return processId;
    }
    /**
     * @return the parentPID
     */
    public final long getParentPID() {
        return parentPID;
    }
    /**
     * @return the threadCount
     */
    public final long getThreadCount() {
        return threadCount;
    }
    /**
     * @return the handleCount
     */
    public final long getHandleCount() {
        return handleCount;
    }
    /**
     * @return the totalCPU
     */
    public final long getTotalCPU() {
        return totalCPU;
    }
    /**
     * @return the userTime
     */
    public final long getUserTime() {
        return userTime;
    }
    /**
     * @return the kernelTime
     */
    public final long getKernelTime() {
        return kernelTime;
    }
    /**
     * @return the ioReads
     */
    public final long getIoReads() {
        return ioReads;
    }
    /**
     * @return the ioWrites
     */
    public final long getIoWrites() {
        return ioWrites;
    }
    /**
     * @return the ioOther
     */
    public final long getIoOther() {
        return ioOther;
    }
    /**
     * @return the ioReadBytes
     */
    public final long getIoReadBytes() {
        return ioReadBytes;
    }
    /**
     * @return the ioWriteBytes
     */
    public final long getIoWriteBytes() {
        return ioWriteBytes;
    }
    /**
     * @return the ioOtherBytes
     */
    public final long getIoOtherBytes() {
        return ioOtherBytes;
    }
    /**
     * @return the pageFaults
     */
    public final long getPageFaults() {
        return pageFaults;
    }
    /**
     * @return the workingSetSize
     */
    public final long getWorkingSetSize() {
        return workingSetSize;
    }
    /**
     * @return the peakWorkingSetSize
     */
    public final long getPeakWorkingSetSize() {
        return peakWorkingSetSize;
    }
    /**
     * @return the pageFileUsage
     */
    public final long getPageFileUsage() {
        return pageFileUsage;
    }
    /**
     * @return the peakPageFileUsage
     */
    public final long getPeakPageFileUsage() {
        return peakPageFileUsage;
    }
    /**
     * @return the privateUsage
     */
    public final long getPrivateUsage() {
        return privateUsage;
    }

    /**
     * @return the imageType
     */
    public final String getImageType() {
        return imageType;
    }

    public final List<String> getErrors() {
        return errors;
    }
    public final boolean hasErrors() {
        return !errors.isEmpty();
    }
    public final String getUser() {
        return user;
    }
    public String toString() {
        return Utilities.toStringFormatter(this);
    }
    
}
