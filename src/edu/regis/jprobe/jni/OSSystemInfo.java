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
package edu.regis.jprobe.jni;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import edu.regis.jprobe.model.Utilities;

public class OSSystemInfo {

	private static final String JAR_FILE_NAME = "JPROBE.JAR";
	private static String nativeLibraryName = "JavaOSInfo";
	private static String nativeLibraryPath = "lib";
	private static boolean isOperational = false;
	
	public static final int SYSTEM_CALL_OK = 0;
	public static final int INVALID_PARMS = -1;
	public static final int SYSTEM_CALL_FAILED = -2;
	public static final int SYSTEM_CALL_NOT_SUPPORTED = -3;
	public static final int SYSTEM_LIBRARY_LOAD_FAILED = -4;
	public static final int SYSTEM_LIBRARY_LOOKUP_FAILED = -5;
	public static final int SYSTEM_SELF_ELEVATE_FAILED = -6;
	public static final int SYSTEM_SELF_ELEVATE_CANCELLED = -7;
	
	/*
	 * Native Methods
	 */
	
	/**
	 * This method gets the CPU affinity mask
	 * @return cpu affinity mask
	 * where .... ...1 = CPU0
	 *       .... ..1. = CPU1
	 *       .... .1.. = CPU2
	 *       .... 1... = CPU3
	 *       etc...
	 * SO, a value of 3 would bind it to CPU0 & CPU1
	 */
	public static native long getCPUAffinity();
	
	/**
	 * This sets the CPU affinity mask
	 * @param mask
	 * @return SYSTEM_CALL_OK(0) if the call succeeded, any other value is the 
	 * 			 getLastError() for the error code.
	 * Set the mask to:
	 * where .... ...1 = CPU0
	 *       .... ..1. = CPU1
	 *       .... .1.. = CPU2
	 *       .... 1... = CPU3
	 *       etc...
	 * SO, a value of 3 would bind it to CPU0 & CPU1
	 */
	public static native long setCPUAffinity(long mask);
	
	/**
	 * This method sets the process CPU affinity mask to the system affinity
	 * such that it allows the process to use all CPU's
	 * @return SYSTEM_CALL_OK(0) if the call succeeded, any other value is the 
	 * 			 getLastError() for the error code.
	 */
	public static native long setCPUAffinityToSystem();
	
	/**
	 * This returns the number of open handles in the process.
	 * @return the number of open handles or SYSTEM_CALL_FAILED(-2) if the
	 * 			system call failed.
	 */
	public static native long getProcessHandleCount();
	
	/**
	 * This method get the number of CPU's available in the system.
	 * @return number of cpus
	 */
	public static native long getNumberOfCPUs();
	
	/**
	 * This method gets the process IO counters as an array of long values.
	 * @param counts - pre allocated array of counters
	 * @return SYSTEM_CALL_OK(0) if the call succeeded
	 * 		   INVALID_PARMS(-1) if the array passed in does not contain the
	 * 						     corret number of elements.
	 * 		   SYSTEM_CALL_FAILED(-2) if the system call failed. 
	 */
	private static native int getIOCounters(long[] counts);
	
	/**
	 * This method gets the process memory statistics as an array of long values.
	 * @param counts - pre allocated array of values
	 * @return SYSTEM_CALL_OK(0) if the call succeeded
	 * 		   INVALID_PARMS(-1) if the array passed in does not contain the
	 * 						     corret number of elements.
	 * 		   SYSTEM_CALL_FAILED(-2) if the system call failed. 
	 */
	private static native int getProcessMemoryInfo(long[] info);
	
	/**
	 * This method gets the process CPU times as an array of long values.
	 * @param counts - pre allocated array of values
	 * @return SYSTEM_CALL_OK(0) if the call succeeded
	 * 		   INVALID_PARMS(-1) if the array passed in does not contain the
	 * 						     corret number of elements.
	 * 		   SYSTEM_CALL_FAILED(-2) if the system call failed. 
	 */
	private static native int getProcessTimes(long[] times);
	
	/**
	 * This method instructs the OS to reclaim unused pages
	 * @return return from os call
	 */
	public static native int emptyWorkingSet();
	/**
	 * This methode instructs the OS to signal a CNTL-Break.
	 * @return error code
	 */
	public static native int sendCTLBreak();
	/**
	 * This methode instructs the OS to signal a CNTL-C.
	 * @return error code
	 */
	public static native int sendCTLC();
	/**
	 * This method get the number of current CPU this thread is running on
	 * @return number of the cpu
	 */
	public static native long getCurrentCPUId();
	
	/**
	 * This method retreives the loaded libraries for the process.
	 * @param libs
	 * @return error code
	 */
	public static native int getNativeLibList(OSNativeLibs libs, long pid);
	/**
	 * Turns on/off dll level debugging log
	 * @param debug
	 */
	private static native void setNativeDebug(boolean debug);
	/**
	 * Get the current process id
		 */
	public static native long getCurrentProcessID();
	/**
	 * Get the current thread id
	 * @return process id
	 */
	public static native long getCurrentThreadID();
	
   /**
     * Determines if the specified process (pid) exists.
     * @return true if a process exists with the specified pid
     */
    public static native boolean isProcessActive(long pid);
    
    /**
     *  
     * @return true if we are running with Admin Privileges
     */
    public static native boolean isAdmin();
    
    /**
     * Determines if the specified process (pid) exists.
     * @return true if a process exists with the specified pid
     */
    public static native long killProcess(long pid);
	
	/**
	 * Returns a List of process Strings as name/value pairs
	 * @return
	 */
	public static native long getProcessInfo(ProcessString processes, boolean allUsers);
	
	/**
	 * This method will prompt the user to elevate the privileges to admin
	 * @return true if successful.
	 */
	public static native long selfElevate();
	/**
	 * Get the percentage of cpu for the current process...
	 * @return
	 */
	//public static native int getCurrentCPU();
	/**
	 * Static initializer to load the JNI native library.
	 */
	static {
			
		
		//First look in the PATH area for our DLL...
		boolean found = false;
		String name = "UNKNOWN";
		
		try {
			
			System.loadLibrary(nativeLibraryName);
			isOperational = true;
			found = true;
		}	catch (UnsatisfiedLinkError e) {
			Utilities.debugMsg("JNI library " + nativeLibraryName + " not Found in the PATH");
			Utilities.debugMsg("Error is " + e.getMessage());
		}
		/* This is not a very elegent way of doing this....
		 * 
		 * What we do is look at the Java classpath for the name
		 * of the JProbe.jar file. Since this will always be present in 
		 * the launched agent, via the -javaagent: parm, we can then find the
		 * associated path and use that as the absolute path for the 
		 * library file.
		 */
			if (!found) {
				String cp = System.getProperty("java.class.path");
				
				
				StringTokenizer st = new StringTokenizer(cp, System.getProperty("path.separator"));
				while (st.hasMoreTokens()) {
					
					String ent = st.nextToken().toUpperCase();
					
					if (ent.contains(JAR_FILE_NAME)) {
							int idx = ent.indexOf(JAR_FILE_NAME);
							String newent = ent.substring(0, idx);
							
							try {
								name = getNativeLibraryName(newent);
								Utilities.debugMsg("Loading JNI DLL from path " + name);
								System.load(name);
								Utilities.debugMsg("Load of JNI library " + 
										name + " successful");
								isOperational = true;
								found = true;
								break;
							}catch (UnsatisfiedLinkError e) {
								Utilities.debugMsg("Error Loading JNI library, Error is " + e.getMessage() + 
								", IO statistics unavaiable");
								isOperational = false;
								found = true;
								if (Utilities.debug) e.printStackTrace();
								break;
							}
					}
					
				}
			}
			
			if (!found) {
				Utilities.debugMsg("Could not find JNI library " + name + 
					", IO statistics unavaiable");
				isOperational = false;
			}
			
			if (Utilities.debug) setDLLDebug(true);
		}
	private static String getNativeLibraryName(String absPath) {
		
		
		String filename =  System.mapLibraryName(absPath + 
			nativeLibraryPath + 
			System.getProperty("file.separator") + 
			System.getProperty("os.arch") +
			System.getProperty("file.separator") + 
			nativeLibraryName);
		
		File f = new File(filename);
		return f.getAbsolutePath();
		
	}
	
	/**
	 * This method returns an IO Counter class that represents the IO counters.
	 * @return counters or null if the call failed.
	 */
	public static OSIOCounters getIOCounters() {
		
		long vals[] = new long[6];
		int res = getIOCounters(vals);
		
		if (res != 0) return null;
		return new OSIOCounters(vals);
	}
	/**
	 * This method returns a Memory Info class that represents memory stats.
	 * @return stats or null if the call failed.
	 */
	public static OSProcessMemoryInfo getProcessMemoryInfo() {
		
		long vals[] = new long[6];
		int res = getProcessMemoryInfo(vals);
		
		if (res != 0) return null;
		return new OSProcessMemoryInfo(vals);
	}
	/**
	 * This method returns a cpu times class that represents the cpu times.
	 * @return times or null if the call failed.
	 */
	public static OSProcessTimes getProcessTimes() {
		
		long vals[] = new long[2];
		int res = getProcessTimes(vals);
		
		if (res != 0) return null;
		return new OSProcessTimes(vals);
	}
	
	public static long getThreadCPUId() {
		if (isOperational) return getCurrentCPUId(); 
		return -1;
	}
	public static OSLibInfo[] getNativeLibs() {
		
		OSNativeLibs libList = new OSNativeLibs();
		int ret = SYSTEM_CALL_OK;
		
		try {
			ret = getNativeLibList(libList);
		} catch (UnsatisfiedLinkError e) {
			ret = SYSTEM_CALL_NOT_SUPPORTED;
		}
		
		if (ret == SYSTEM_CALL_OK ) {
			return libList.getLibs();
		}
		
		return null;
	}
	   public static List<OSLibInfo> getNativeLibs(long pid) {
	        
	        List<OSLibInfo> lst = new ArrayList<OSLibInfo>();
	        OSNativeLibs libList = new OSNativeLibs();
	        int ret = SYSTEM_CALL_OK;
	        
	        try {
	            ret = getNativeLibList(libList, pid);
	        } catch (UnsatisfiedLinkError e) {
	            ret = SYSTEM_CALL_NOT_SUPPORTED;
	        }
	        
	        if (ret == SYSTEM_CALL_OK ) {
	            for (OSLibInfo li : libList.getLibs()) {
	                lst.add(li);
	            }
	        }
	        
	        return lst;
	    }
	/**
	 * This method tells us that the JNI library has been loaded successfuly
	 * so that we may call the native methods.
	 * @return the isOperational
	 */
	public static boolean isOperational() {
		return isOperational;
	}

	public static void setDLLDebug(boolean val) {
		
		if (isOperational) {
			try {
				setNativeDebug(val);
			}catch (UnsatisfiedLinkError e){}
		}
	}
	public static int getNativeLibList(OSNativeLibs libs) {
	    return getNativeLibList(libs, 0);
	}
	public static List<OSProcessInfo> getOSProcesses(boolean allUsers) {
	    
	    ProcessString ps = new ProcessString();
	    
	    if (isOperational) {
	        long resp = getProcessInfo(ps, allUsers);
	        if (resp != SYSTEM_CALL_OK) {
	            Utilities.debugMsg("Unable to obtain process list, error is " + resp);
	        }
	    }
	    
	    return ps.getProcessList();
	}

/**
 * Main method for testing JNI calls
 * @param args
 */
	public static void main(String args[]) {
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		//Runtime.getRuntime().traceMethodCalls(true);
		//Runtime.getRuntime().traceInstructions(true);
		Thread thd = new Thread() {
			public void run() {
				long i = 0;
				while (!isOperational) {
					i++;
				}
				System.out.println("Thread id is " + getCurrentThreadID() +
				", CPUs.. " + i);
			}
		};
		thd.start();
		System.out.println("Lib Path=" + System.getProperty("java.library.path"));
		try {
			System.load("C:\\git\\MyStuff\\Z_JProbe\\jni\\debug\\x64\\JavaOSInfo.dll");
			isOperational = true;
			
			System.out.println("System has " + getNumberOfCPUs() +
					" CPUs");
			
			System.out.println("Getting CPU Affinity = " +
					getCPUAffinity());
			
			System.out.println("Setting CPU Affinity = " +
				setCPUAffinity(1));
			
			System.out.println("Setting CPU Affinity to System Affinity = " +
					setCPUAffinityToSystem());
			
			System.out.println("Our PID is " + Utilities.getProcessID() +  " extsts=" +
			        isProcessActive(Utilities.getProcessID()));
			System.out.println("test PID is 98765"  +  " extsts=" +
                    isProcessActive(98765));
			System.out.println("Total Process Handles  = " + getProcessHandleCount());
			System.out.println("OS Process ID = " +	getCurrentProcessID());
			System.out.println("OS Thread ID = " +	getCurrentThreadID());
			OSIOCounters cntr = getIOCounters();
			
			System.out.println("Counter-ReadCount   = " + cntr.ioReadCount);
			System.out.println("Counter-WriteCount  = " + cntr.ioWriteCount);
			System.out.println("Counter-OtherCount  = " + cntr.ioOtherCount);
			System.out.println("Counter-ReadBytes   = " + cntr.ioReadBytes);
			System.out.println("Counter-WriteBytes  = " + cntr.ioWriteBytes);
			System.out.println("Counter-OtherBytes  = " + cntr.ioOtherBytes);
			
			System.out.println("Shrinking Memory Working Set");
			emptyWorkingSet();
			OSProcessMemoryInfo inf = getProcessMemoryInfo();
			System.out.println("Memory-PageFaults  = " + inf.pageFaults);
			System.out.println("Memory-WSS         = " + inf.workingSetSize);
			System.out.println("Memory-PeakWSS     = " + inf.peakworkingSetSize);
			System.out.println("Memory-PageFileUse = " + inf.pageFileUsage);
			System.out.println("Memory-PeakPageUse = " + inf.peakPageFileUsage);
			System.out.println("Memory-PrivateBytes = " + inf.privateBytes);
			
			OSProcessTimes times = getProcessTimes();
			System.out.println("Times-Kernel  = " + times.kernelTime);
			System.out.println("Times-User    = " + times.userTime);
			System.out.println("Times-TotCPU  = " + times.cpuTime);
			
			
			ProcessString ps = new ProcessString();
			long resp = getProcessInfo(ps, true);
			if (resp == SYSTEM_CALL_OK) {
			    
			    for (OSProcessInfo pi : ps.getProcessList()) {
			        System.out.println(pi);
			        OSNativeLibs libList = new OSNativeLibs();
			        int ret = SYSTEM_CALL_OK;
			        
			        try {
			            ret = getNativeLibList(libList, pi.getProcessId());
			        } catch (UnsatisfiedLinkError e) {
			            ret = SYSTEM_CALL_NOT_SUPPORTED;
			        }
			        
			        if (ret == SYSTEM_CALL_OK ) {
			       
		                System.out.println("\n\tModules in process " + pi.getProcessId());
		                for (OSLibInfo li : libList.getLibList()) {
		                    System.out.println("\t\t" + li.toString());
		                }
		            }
			    }
			        
			    
			} else {
			    System.out.println("getProcessInfo call failed, rc=" + resp); 
			}
			    
			
			//System.out.println("\nSending CNTL-Break, RC=" + sendCTLBreak());
			//System.out.println("\nSending CNTL-C, RC=" + sendCTLC());
			
			System.out.println("Running as Admin: " + isAdmin());
			System.out.println("\nCPUID=" + getCurrentCPUId());
			
			System.out.println("Killing 11524: " + killProcess(10332));
			/*OSLibInfo libs[] = getNativeLibs();
			
			if (libs != null) {
				System.out.println("\nNative Libraries in process");
				for (int i=0; i < libs.length; i++) {
					System.out.println("\t" + libs[i]);
				}
			}*/
			setDLLDebug(true);
		} catch (UnsatisfiedLinkError e) {
			System.out.println("Error " + e.getMessage() + " Occurred");
		}
		
		
		
	}
}
