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
package edu.regis.jprobe.ui;

import static edu.regis.jprobe.model.Utilities.toStringFormatter;

import edu.regis.jprobe.jni.OSProcessInfo;

public class ProcessData {
	
    public String processName;
    public String imageType;
    public String user;
    public long processId;
    public long totalCPU;
    public double percentCPU;
    public long ioReads;
    public long ioWrites;
    public long ioOther; 
    public long pageFaultsDelta;
    public long pageFaults;
    public long workingSetSize;
    public long privateUsage;
    public OSProcessInfo pi;
	
	public ProcessData() {
		
	}
	
	public ProcessData(OSProcessInfo pi) {
	    
	    this.pi = pi;
	    this.processName = pi.getProcessName();
	    this.imageType = pi.getImageType();
	    this.user = pi.getUser();
	    this.processId = pi.getProcessId();
	    this.totalCPU = pi.getTotalCPU();
	    this.ioReads = pi.getIoReads();
	    this.ioWrites = pi.getIoWrites();
	    this.ioOther = pi.getIoOther();
	    this.pageFaults = pi.getPageFaults();
	    this.workingSetSize = pi.getWorkingSetSize();
	    this.privateUsage = pi.getPageFileUsage();
        
    }
	public void update(OSProcessInfo pi) {
	    this.pi = pi;
        this.totalCPU = pi.getTotalCPU();
        this.ioReads = pi.getIoReads();
        this.ioWrites = pi.getIoWrites();
        this.ioOther = pi.getIoOther();
        this.pageFaults = pi.getPageFaults();
        this.workingSetSize = pi.getWorkingSetSize();
        this.privateUsage = pi.getPageFileUsage();
	        
	}
	public String toString() {
		
		return toStringFormatter(this);
	}
}