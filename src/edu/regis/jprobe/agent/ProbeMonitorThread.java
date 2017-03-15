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
package edu.regis.jprobe.agent;

import static edu.regis.jprobe.model.Utilities.VERSION_HEADING;
import static edu.regis.jprobe.model.Utilities.debugMsg;
import static edu.regis.jprobe.model.Utilities.formatStackTrace;
import static edu.regis.jprobe.model.Utilities.getDateTime;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class ProbeMonitorThread extends Thread {

	private ProbeState status;
	private static final int WAIT_SECONDS = 15;
	private String fileName;
	private Set<Thread> keys;
	private ThreadMXBean threadBean;
	
	protected ProbeMonitorThread(ProbeState status) {
		fileName = "JProbe_StackDump_" +  
			getDateTime("MMddyyyyHHmmssS") + 
			".txt";
		this.status = status;
		setName("ProbeMonitor");
		setPriority(Thread.NORM_PRIORITY);
		setDaemon(true);
		keys = new HashSet<Thread>();
		threadBean = ManagementFactory.getThreadMXBean();
		
					
			
		
	}
	public void run() {
		int count = 0;
		debugMsg("ProbeMonitor Thread Started");
		while(!status.isInitialized()) {
			try {Thread.sleep(1000);} catch (InterruptedException e){}
			if (count++ > WAIT_SECONDS) {
				dump();
				return;
			}
		}
		debugMsg("ProbeMonitor Thread Detected Normal Startup");
		
		
	}
	private void dump() {
		
		
		System.err.println(VERSION_HEADING + 
				" - Probe Startup Hung, Dumping Stack Trace");
		
		dumpStackTrace();
		String trace = formatStackTrace();
				
		FileOutputStream fos = null;
		
		try {
			fos = new FileOutputStream(fileName);
			DataOutputStream out = new DataOutputStream(fos);
			out.write(trace.getBytes());
			out.close();
		} catch (Exception e) {
		
			System.err.println("Unable to Write Stack Dump, Error is " + 
					e.getMessage() );
			e.printStackTrace();
			return;
		}
		
		
	}
	public void dumpStackTrace() {
        
		System.out.print("Thread Stack Dump\n");
    	
		System.out.print("\tRequesting Thread: ");
		System.out.print(Thread.currentThread().getName());
		System.out.print(" - [");
		System.out.print(Thread.currentThread().getId());
		System.out.print("]\n");
    	
 		Map<Thread, StackTraceElement[]> stackMap = Thread.getAllStackTraces();
        
	    keys = stackMap.keySet();				
	    Iterator<Thread> iter = keys.iterator();		
	    
	    //Loop thru the map
	    while (iter.hasNext()) {
	        Thread thd = iter.next();
	        State st = thd.getState();
	        String state = st.toString();
	        String type = thd.isDaemon() ? "Deamon" : "Non-Deamon";
	        //long tid  = thd.getId();

	        System.out.print("\n\tStack Trace for ");
	        System.out.print(type);
	        System.out.print(" Thread [");
	        System.out.print(thd.getName());
	        System.out.print(":ID=");
	        System.out.print(thd.getId());
	        System.out.print("] State(");
	        System.out.print(state);
	        System.out.print(") Priority(");
	        System.out.print(thd.getPriority());
	        System.out.print(")\n");
	        
	        	        
	        StackTraceElement ste[] = stackMap.get(thd);
	        int offset = 0;
	        for (int i = 0; i < ste.length; i++ ) {
	        	
	        	System.out.print("\t\t(");
	        	System.out.print((offset==0?" ": "-"));
	        	System.out.print(offset++);
	        	System.out.print(") - ");
	        	System.out.print(ste[i].toString());	
		    	System.out.print( "\n");
	        }
	        
	        if (state.equals("BLOCKED")) {
	        	System.out.print("\n\t\t*** ");
	        	//System.out.print(getBlockedInfo(tid));
	        	System.out.print(" ***\n");
	        }
	        
	    }
	    	    
    }
	public String getBlockedInfo(long threadId) {
		
		ThreadInfo ti = threadBean.getThreadInfo(threadId, Integer.MAX_VALUE);
		long id = ti.getLockOwnerId();
		String lockName = ti.getLockName();
		String lockOwner = ti.getLockOwnerName();
		
		return "Thread is waiting on lock(" + lockName + ") Owned by " + 
				lockOwner + ":" + id;
	}
}
