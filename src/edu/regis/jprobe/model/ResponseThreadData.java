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


/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResponseThreadData implements Externalizable { 

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String threadName;
    private String lockName;
    private String lockOwner;
    private long lockOwningThread;
	private long threadId;
	private long threadCPU;
	private long threadLastCPU;
	private long currentCPU;
	private long lastTime;
	private long allocatedBytes = -1;
	private double percentTotalCPU;
	private double percentCurrentCPU;
	private boolean threadBlocked = false;
	private boolean deamon = false;
	private boolean probeThread = false;
	private StackTraceElement[] currentStackFrame;
	private ResponseMonitorInfo[] monitorInfo;
	private ResponseMonitorInfo[] lockInfo;
	
	public ResponseThreadData() {
		
	}
	
	/**
	 * @return Returns the threadCPU.
	 */
	public long getThreadCPU() {
		return threadCPU;
	}
	/**
	 * @param threadCPU The threadCPU to set.
	 */
	public void setThreadCPU(long threadCPU) {
		this.threadCPU = threadCPU;
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
	 * @return Returns the threadName.
	 */
	public String getThreadName() {
		return threadName;
	}
	/**
	 * @param threadName The threadName to set.
	 */
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	
	/**
	 * @return Returns the currentCPU.
	 */
	public long getCurrentCPU() {
		return currentCPU;
	}
	/**
	 * @param currentCPU The currentCPU to set.
	 */
	public void setCurrentCPU(long currentCPU) {
		this.currentCPU = currentCPU;
	}
	/**
	 * @return Returns the percentCurrentCPU.
	 */
	public double getPercentCurrentCPU() {
		return percentCurrentCPU;
	}
	/**
	 * @param percentCurrentCPU The percentCurrentCPU to set.
	 */
	public void setPercentCurrentCPU(double percentCurrentCPU) {
		this.percentCurrentCPU = percentCurrentCPU;
	}
	/**
	 * @return Returns the percentTotalCPU.
	 */
	public double getPercentTotalCPU() {
		return percentTotalCPU;
	}
	/**
	 * @param percentTotalCPU The percentTotalCPU to set.
	 */
	public void setPercentTotalCPU(double percentTotalCPU) {
		this.percentTotalCPU = percentTotalCPU;
	}
	
	/**
	 * @return Returns the threadLastCPU.
	 */
	public long getThreadLastCPU() {
		return threadLastCPU;
	}
	/**
	 * @param threadLastCPU The threadLastCPU to set.
	 */
	public void setThreadLastCPU(long threadLastCPU) {
		this.threadLastCPU = threadLastCPU;
	}
	
	/**
	 * @return Returns the lastTime.
	 */
	public long getLastTime() {
		return lastTime;
	}
	/**
	 * @param lastTime The lastTime to set.
	 */
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	/**
	 * @return the threadBlocked
	 */
	public boolean isThreadBlocked() {
		return threadBlocked;
	}

	/**
	 * @param threadBlocked the threadBlocked to set
	 */
	public void setThreadBlocked(boolean threadBlocked) {
		this.threadBlocked = threadBlocked;
	}

    public StackTraceElement[] getCurrentStackFrame() {
        return currentStackFrame;
    }

    public void setCurrentStackFrame(StackTraceElement[] currentStackFrame) {
        this.currentStackFrame = currentStackFrame;
    }
    
    public long getAllocatedBytes() {
        return allocatedBytes;
    }

    public void setAllocatedBytes(long allocatedBytes) {
        this.allocatedBytes = allocatedBytes;
    }

    public boolean isDeamon() {
        return deamon;
    }

    public void setDeamon(boolean deamon) {
        this.deamon = deamon;
    }

    public String toString() {
        return Utilities.toStringFormatter(this);
    }

    public final String getLockName() {
        return lockName;
    }

     public final void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public final String getLockOwner() {
        return lockOwner;
    }

    public final void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    /**
     * @return the lockOwningThread
     */
    public final long getLockOwningThread() {
        return lockOwningThread;
    }

    /**
     * @return the monitorInfo
     */
    public final ResponseMonitorInfo[] getMonitorInfo() {
        return monitorInfo;
    }

    /**
     * @param monitorInfo the monitorInfo to set
     */
    public final void setMonitorInfo(ResponseMonitorInfo[] monitorInfo) {
        this.monitorInfo = monitorInfo;
    }

    /**
     * @return the lockInfo
     */
    public final ResponseMonitorInfo[] getLockInfo() {
        return lockInfo;
    }

    /**
     * @param lockInfo the lockInfo to set
     */
    public final void setLockInfo(ResponseMonitorInfo[] lockInfo) {
        this.lockInfo = lockInfo;
    }

    /**
     * @param lockOwningThread the lockOwningThread to set
     */
    public final void setLockOwningThread(long lockOwningThread) {
        this.lockOwningThread = lockOwningThread;
    }

    /**
     * @return the probeThread
     */
    public final boolean isProbeThread() {
        return probeThread;
    }

    /**
     * @param probeThread the probeThread to set
     */
    public final void setProbeThread(boolean probeThread) {
        this.probeThread = probeThread;
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
