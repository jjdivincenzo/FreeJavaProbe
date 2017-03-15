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
public class ThreadInfoData implements Externalizable { 
	
	
	private long totalCPU;
	private long totalUserTime;
	private long blockCount;
	private long blockTime;
	private long allocatedBytes;
	private String lockName;
	private String lockOwner;
	private String lockOwningThread;
    private String monitorLockFrame;
	private String state;
	private long waitCount;
	private long waitTime;
	private int priority = -1;
	private boolean daemon;
	private String inNative;
	private String suspended;
	private String stackTrace;
	private String contextClassLoader;
	private boolean canInterrupt = false;
	
	public ThreadInfoData() {
		
	}

	/**
	 * @return Returns the blockCount.
	 */
	public long getBlockCount() {
		return blockCount;
	}
	/**
	 * @param blockCount The blockCount to set.
	 */
	public void setBlockCount(long blockCount) {
		this.blockCount = blockCount;
	}
	/**
	 * @return Returns the blockTime.
	 */
	public long getBlockTime() {
		return blockTime;
	}
	/**
	 * @param blockTime The blockTime to set.
	 */
	public void setBlockTime(long blockTime) {
		this.blockTime = blockTime;
	}
	
	/**
	 * @return Returns the inNative.
	 */
	public String getInNative() {
		return inNative;
	}
	/**
	 * @param inNative The inNative to set.
	 */
	public void setInNative(String inNative) {
		this.inNative = inNative;
	}
	/**
	 * @return Returns the lockName.
	 */
	public String getLockName() {
		return lockName;
	}
	/**
	 * @param lockName The lockName to set.
	 */
	public void setLockName(String lockName) {
		this.lockName = lockName;
	}
	/**
	 * @return Returns the lockOwner.
	 */
	public String getLockOwner() {
		return lockOwner;
	}
	/**
	 * @param lockOwner The lockOwner to set.
	 */
	public void setLockOwner(String lockOwner) {
		this.lockOwner = lockOwner;
	}
	/**
	 * @return Returns the lockOwningThread.
	 */
	public String getLockOwningThread() {
		return lockOwningThread;
	}
	/**
	 * @param lockOwningThread The lockOwningThread to set.
	 */
	public void setLockOwningThread(String lockOwningThread) {
		this.lockOwningThread = lockOwningThread;
	}
	/**
	 * @return Returns the stackTrace.
	 */
	public String getStackTrace() {
		return stackTrace;
	}
	/**
	 * @param stackTrace The stackTrace to set.
	 */
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	/**
	 * @return Returns the state.
	 */
	public String getState() {
		return state;
	}
	/**
	 * @param state The state to set.
	 */
	public void setState(String state) {
		this.state = state;
	}
	/**
	 * @return Returns the suspended.
	 */
	public String getSuspended() {
		return suspended;
	}
	/**
	 * @param suspended The suspended to set.
	 */
	public void setSuspended(String suspended) {
		this.suspended = suspended;
	}
	/**
	 * @return Returns the totalCPU.
	 */
	public long getTotalCPU() {
		return totalCPU;
	}
	/**
	 * @param totalCPU The totalCPU to set.
	 */
	public void setTotalCPU(long totalCPU) {
		this.totalCPU = totalCPU;
	}
	/**
	 * @return Returns the totalUserTime.
	 */
	public long getTotalUserTime() {
		return totalUserTime;
	}
	/**
	 * @param totalUserTime The totalUserTime to set.
	 */
	public void setTotalUserTime(long totalUserTime) {
		this.totalUserTime = totalUserTime;
	}
	/**
	 * @return Returns the waitCount.
	 */
	public long getWaitCount() {
		return waitCount;
	}
	/**
	 * @param waitCount The waitCount to set.
	 */
	public void setWaitCount(long waitCount) {
		this.waitCount = waitCount;
	}
	/**
	 * @return Returns the waitTime.
	 */
	public long getWaitTime() {
		return waitTime;
	}
	/**
	 * @param waitTime The waitTime to set.
	 */
	public void setWaitTime(long waitTime) {
		this.waitTime = waitTime;
	}

	/**
	 * @return the canInterrupt
	 */
	public boolean isCanInterrupt() {
		return canInterrupt;
	}

	/**
	 * @param canInterrupt the canInterrupt to set
	 */
	public void setCanInterrupt(boolean canInterrupt) {
		this.canInterrupt = canInterrupt;
	}

	/**
	 * @return the daemon
	 */
	public boolean isDaemon() {
		return daemon;
	}

	/**
	 * @param daemon the daemon to set
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @return the monitorLockFrame
	 */
	public String getMonitorLockFrame() {
		return monitorLockFrame;
	}

	/**
	 * @param monitorLockFrame the monitorLockFrame to set
	 */
	public void setMonitorLockFrame(String monitorLockFrame) {
		this.monitorLockFrame = monitorLockFrame;
	}

	/**
	 * @return the contextClassLoader
	 */
	public String getContextClassLoader() {
		return contextClassLoader;
	}

	/**
	 * @param contextClassLoader the contextClassLoader to set
	 */
	public void setContextClassLoader(String contextClassLoader) {
		this.contextClassLoader = contextClassLoader;
	}
	
    public long getAllocatedBytes() {
        return allocatedBytes;
    }

    public void setAllocatedBytes(long allocatedBytes) {
        this.allocatedBytes = allocatedBytes;
    }

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
