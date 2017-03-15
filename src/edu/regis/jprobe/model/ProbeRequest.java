///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2007  James Di Vincenzo
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
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.management.ObjectName;



/**
 * @author jdivince
 *
 * This class is a data container for the Broker Request
 */
public class ProbeRequest implements Externalizable, IRequest {
    

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int requestType;
    private long threadId;
    private String userId;
    private byte[] password;
    private String clientIP;
    private String clientHostName;
    private String mbeanDomain;
    private String propertyKey;
    private String propertyValue;
    private ObjectName objectName;
    private Object[] mbeanParms;
    private String className;
    private long affinity;
    private boolean resetOptionData = false;
    private boolean collectMonitorInfo = true;
    private boolean collectLockInfo = true;
    private boolean probeDebug = false;
    private int exitCode = 666;
       
    //Command Requests
    public static final int REQ_STATS = 0;
    public static final int REQ_DISCONNECT = 1;
    public static final int REQ_THREAD_INFO = 2;
    public static final int REQ_STOP_PROBE = 3;
    public static final int REQ_SEND_GC = 4;
    public static final int REQ_CLASS_INFO = 5;
    public static final int REQ_THREAD_INTERRUPT = 6;
    public static final int REQ_JVM_EXIT = 7;
    public static final int REQ_GET_MBEANS = 8;
    public static final int REQ_GET_MBEAN_INFO = 9;
    public static final int REQ_SET_PROPERTY = 10;
    public static final int REQ_REMOVE_PROPERTY = 11;
    public static final int REQ_SET_AFFINITY = 12;
    public static final int REQ_GET_CLASS = 13;
    public static final int REQ_EXEC_MBEAN = 14;
    public static final int REQ_DUMP_STACKTRACE = 15;
    public static final int REQ_DUMP_HEAP = 16;
    public static final int REQ_KILL_THREAD = 17;
    public String reqName[] = {
    		"Get Stats",
        	"Disconnect",
        	"Get Thread Info",
        	"Stop Probe",
        	"Do GC",
        	"Get Class Info",
        	"Interrupt Thread",
        	"Stop The JVM",
        	"Get JMXBeans",
        	"Get MBeans Info",
        	"Set Property",
        	"Remove Property",
        	"Set Affinity",
        	"Get Class Properties",
        	"Execute MBean Method",
        	"Dump Stack Trace",
        	"Dump Heap",
        	"Kill Thread"
    };
    
 
    /**
     * Default ctor ...
     *
     */
    public ProbeRequest() {
    	
    	    	
    }
    /**
     * @return Returns the requestType.
     */
    public int getRequestType() {
        return requestType;
    }
    /**
     * @param requestType The requestType to set.
     */
    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }
        
    public String getRequestName(int reqType) {
    	return reqName[reqType];
    }
    public String getRequestName() {
    	return reqName[requestType];
    }
	/**
	 * @return Returns the threadId.
	 */
	public long getThreadId() {
		return threadId;
	}
	/**
	 * @param threadId The threadId to set.
	 */
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}
	/**
	 * @return the clientIP
	 */
	public String getClientIP() {
		return clientIP;
	}
	/**
	 * @param clientIP the clientIP to set
	 */
	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}
	/**
	 * @return the password
	 */
	public byte[] getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(byte[] password) {
		this.password = password;
	}
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * @return the resetOptionData
	 */
	public boolean isResetOptionData() {
		return resetOptionData;
	}
	/**
	 * @param resetOptionData the resetOptionData to set
	 */
	public void setResetOptionData(boolean resetOptionData) {
		this.resetOptionData = resetOptionData;
	}
	/**
	 * @return the mbeanDomain
	 */
	public String getMbeanDomain() {
		return mbeanDomain;
	}
	/**
	 * @param mbeanDomain the mbeanDomain to set
	 */
	public void setMbeanDomain(String mbeanDomain) {
		this.mbeanDomain = mbeanDomain;
	}
	/**
	 * @return the objectName
	 */
	public ObjectName getObjectName() {
		return objectName;
	}
	/**
	 * @param objectName the objectName to set
	 */
	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}
	/**
	 * @return the propertyKey
	 */
	public String getPropertyKey() {
		return propertyKey;
	}
	/**
	 * @param propertyKey the propertyKey to set
	 */
	public void setPropertyKey(String propertyKey) {
		this.propertyKey = propertyKey;
	}
	/**
	 * @return the propertyValue
	 */
	public String getPropertyValue() {
		return propertyValue;
	}
	/**
	 * @param propertyValue the propertyValue to set
	 */
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	/**
	 * @return the affinity
	 */
	public long getAffinity() {
		return affinity;
	}
	/**
	 * @param affinity the affinity to set
	 */
	public void setAffinity(long affinity) {
		this.affinity = affinity;
	}
	/**
	 * @return the exitCode
	 */
	public int getExitCode() {
		return exitCode;
	}
	/**
	 * @param exitCode the exitCode to set
	 */
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	/**
	 * @return the mbeanParms
	 */
	public Object[] getMbeanParms() {
		return mbeanParms;
	}
	/**
	 * @param mbeanParms the mbeanParms to set
	 */
	public void setMbeanParms(Object[] mbeanParms) {
		this.mbeanParms = mbeanParms;
	}
	/**
	 * @return the clientHostName
	 */
	public String getClientHostName() {
		return clientHostName;
	}
	/**
	 * @param clientHostName the clientHostName to set
	 */
	public void setClientHostName(String clientHostName) {
		this.clientHostName = clientHostName;
	}
	
	/**
     * @return the collectMonitorInfo
     */
    public boolean isCollectMonitorInfo() {
        return collectMonitorInfo;
    }
    /**
     * @param collectMonitorInfo the collectMonitorInfo to set
     */
    public void setCollectMonitorInfo(boolean collectMonitorInfo) {
        this.collectMonitorInfo = collectMonitorInfo;
    }
    /**
     * @return the collectLockInfo
     */
    public boolean isCollectLockInfo() {
        return collectLockInfo;
    }
    /**
     * @param collectLockInfo the collectLockInfo to set
     */
    public void setCollectLockInfo(boolean collectLockInfo) {
        this.collectLockInfo = collectLockInfo;
    }
    
    /**
     * @return the probeDebug
     */
    public final boolean isProbeDebug() {
        return probeDebug;
    }
    /**
     * @param probeDebug the probeDebug to set
     */
    public final void setProbeDebug(boolean probeDebug) {
        this.probeDebug = probeDebug;
    }
    /**
	 * To String
	 * @return this object as a string
	 */
	public String toString() {
	    return Utilities.toStringFormatter(this);
	}
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Externalizer.getInstance().writeObject(this, out);
        
    }
 
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Externalizer.getInstance().readObject(this, in);
        
    }
}
