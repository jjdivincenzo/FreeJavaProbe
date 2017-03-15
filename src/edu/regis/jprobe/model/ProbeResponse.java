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
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.ObjectName;

import edu.regis.jprobe.jni.OSLibInfo;



/**
 * @author jdivince
 *
 * This class is the data object that the broker maintains that contains the real time broker stats. It
 * can be passed to the UI for real-time reporting.
 * 
 */
public class ProbeResponse implements Externalizable { 
	/**
     * 
     */
    private static final long serialVersionUID = 1010L;
    //Command Resposes
    public static final int RES_OK = 1;
    public static final int RES_FAILED = 2;
    public static final int RES_INVALID_AUTHORIZATION = 3;
    public static final int RES_SHUTDOWN = 4;
    
    private int responseCode;
    private String responseErrorMessage;
    private String probeName;
    private String hostName;
    private String hostIP;
    
    private long executionStartTime = 0;
    private long currentTime = 0;
    private long totalCPUTime = 0;
    private long totalKernelTime = 0;
    private long totalUserTime = 0;
    private long totalExecutionTime = 0;
    private double currentCPUPercent = 0.0;
    private double averageCPUPercent = 0.0;
    private double averageSystemLoad = 0.0;
    private double systemCpuLoad = 0.0;
    private double processCpuLoad = 0.0;
    private long currentHeapSize = 0;
    private long maxHeapSize = 0;
    private long currentNonHeapSize = 0;
    private long maxNonHeapSize = 0;
    private long totalGCs = 0;
    private long totalGCTime = 0;
    private long totalClassesLoaded = 0;
    private int numberOfActiveThreads = 0;
    private int numberOfDaemonThreads = 0;
    private int numberOfBlockedThreads = 0;
    private int numberOfCPUs = 0;
    private int numberOfObjectsPendingFinalization = 0;
    private int numberOfObjectsPendingFinalizationHWM = 0;
    private long numberOfPhysicalCPUs = 0;
    private long cpuAffinity = 1;
    private long allThreads[];
    private long osCommittedVirtMemSize = -1;
    private long osFreePhysMemSize = -1;
    private long osFreeSwapSpaceSize = -1;
    private long osTotalPhysMemSize = -1;
    private long osTotoalSwapSpaceSize = -1;
    private long numberOfLoadedClasses = 0;
    private long numberOfLoadedPackages = 0;
    private long numberOfClassLoaders = 0;
    private long totalClassSize = 0;
    private long ioReadCount = 0;
    private long ioWriteCount = 0;
    private long ioOtherCount = 0;
    private long ioReadBytes = 0;
    private long ioWriteBytes = 0;
    private long ioOtherBytes = 0;
    private long pageFaults = 0;
    private long workingSetSize = 0;
    private long peakWorkingSetSize = 0;
    private long pagefileUsage = 0;
    private long peakPagefileUsage = 0;
    private long privateBytes = 0;
    private long totalCompilationTime = 0;
    private long processID = 0;
    private String osName = "n/a";
    private long startupTime;
    private String jvmOpts;
    private String classpath;
    private String javaProperties;
    private String envProperties;
    private String loadedClasses;
    private String loadedPackages;
    private String classLoaders;
    private String jmxDomains[];
    private String classInfo;
    private byte[] classBytes;
    private String jitCompilerName;
    private String stackTraceInfo;
    private String serviceURL;
    private String jvmName = "N/A";
    private String javaVersion = "1.7.0";
    private boolean classpath_updated = false;
    private boolean javaProperties_updated = false;
    private boolean envProperties_updated = false;
    private boolean loadedClasses_updated = false;
    private boolean classLoaders_updated = false;
    private boolean loadedPackages_updated = false;
    private boolean jvmOpts_updates = false;
    private boolean ioCountersAvailable = false;
    
    
    private Vector<ResponseThreadData> threadData;
    private Vector<MemoryPoolData> poolUsage;
    private Vector<GarbageCollectorData> gcData;
    private List<GarbageCollectorProperties> garbageCollectors;
    private OSLibInfo nativeLibs[];
    private Set<ObjectName> mbeans;
    private MBeanInfo mbeanInfo;
    private AttributeList attributeList;
    private ThreadInfoData tid;
    private Throwable probeError;
  
    /**
     * This is the default CTOR for this class.
     *
     */
    public ProbeResponse() {
        
    	
    	threadData = new Vector<ResponseThreadData>();
    	poolUsage = new Vector<MemoryPoolData>();
    	gcData = new Vector<GarbageCollectorData>();
    	String version = System.getProperty("java.version");
    	if (version != null) {
    	    javaVersion = version;
    	}
    	
    }
    
    
    public void addThreadInfo(ResponseThreadData td) {
    	
    	threadData.add(td);
    }
    public void addPoolValue(MemoryPoolData mpd) {
    	
    	poolUsage.add(mpd);
    }
    public MemoryPoolData getPoolData(int idx) {
    	return poolUsage.get(idx);
    }
    public int getPoolDataSize() {
    	return poolUsage.size();
    }
    public void addGCValue(GarbageCollectorData gcd) {
    	
    	gcData.add(gcd);
    }
    
	/**
	 * @return Returns the responseCode.
	 */
	public int getResponseCode() {
		return responseCode;
	}
	/**
	 * @param responseCode The responseCode to set.
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	/**
	 * @return Returns the responseErrorMessage.
	 */
	public String getResponseErrorMessage() {
		return responseErrorMessage;
	}
	/**
	 * @param responseErrorMessage The responseErrorMessage to set.
	 */
	public void setResponseErrorMessage(String responseErrorMessage) {
		this.responseErrorMessage = responseErrorMessage;
	}
    public GarbageCollectorData getGCData(int idx) {
    	return gcData.get(idx);
    }
    public int getGCDataSize() {
    	return gcData.size();
    }
    public ResponseThreadData getThreadInfo(int idx) {
    	 
    	if (idx < threadData.size()) return threadData.get(idx);
    	
    	return null;
    }
    public int getNumberOfThreadInfo() {
    	return threadData.size(); 
    }
	/**
	 * @return Returns the currentHeapSize.
	 */
	public long getCurrentHeapSize() {
		return currentHeapSize;
	}
	/**
	 * @param currentHeapSize The currentHeapSize to set.
	 */
	public void setCurrentHeapSize(long currentHeapSize) {
		this.currentHeapSize = currentHeapSize;
	}
	/**
	 * @return Returns the currentNonHeapSize.
	 */
	public long getCurrentNonHeapSize() {
		return currentNonHeapSize;
	}
	/**
	 * @param currentNonHeapSize The currentNonHeapSize to set.
	 */
	public void setCurrentNonHeapSize(long currentNonHeapSize) {
		this.currentNonHeapSize = currentNonHeapSize;
	}
	/**
	 * @return Returns the currentTime.
	 */
	public long getCurrentTime() {
		return currentTime;
	}
	/**
	 * @param currentTime The currentTime to set.
	 */
	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}
	/**
	 * @return Returns the executionStartTime.
	 */
	public long getExecutionStartTime() {
		return executionStartTime;
	}
	/**
	 * @param executionStartTime The executionStartTime to set.
	 */
	public void setExecutionStartTime(long executionStartTime) {
		this.executionStartTime = executionStartTime;
	}
	/**
	 * @return Returns the maxHeapSize.
	 */
	public long getMaxHeapSize() {
		return maxHeapSize;
	}
	/**
	 * @param maxHeapSize The maxHeapSize to set.
	 */
	public void setMaxHeapSize(long maxHeapSize) {
		this.maxHeapSize = maxHeapSize;
	}
	/**
	 * @return Returns the maxNonHeapSize.
	 */
	public long getMaxNonHeapSize() {
		return maxNonHeapSize;
	}
	/**
	 * @param maxNonHeapSize The maxNonHeapSize to set.
	 */
	public void setMaxNonHeapSize(long maxNonHeapSize) {
		this.maxNonHeapSize = maxNonHeapSize;
	}
	/**
	 * @return Returns the numberOfActiveThreads.
	 */
	public int getNumberOfActiveThreads() {
		return numberOfActiveThreads;
	}
	/**
	 * @param numberOfActiveThreads The numberOfActiveThreads to set.
	 */
	public void setNumberOfActiveThreads(int numberOfActiveThreads) {
		this.numberOfActiveThreads = numberOfActiveThreads;
	}
	
	/**
	 * @return the numberOfBlockedThreads
	 */
	public int getNumberOfBlockedThreads() {
		return numberOfBlockedThreads;
	}


	/**
	 * @param numberOfBlockedThreads the numberOfBlockedThreads to set
	 */
	public void setNumberOfBlockedThreads(int numberOfBlockedThreads) {
		this.numberOfBlockedThreads = numberOfBlockedThreads;
	}


	/**
	 * @return Returns the startupTime.
	 */
	public long getStartupTime() {
		return startupTime;
	}
	/**
	 * @param startupTime The startupTime to set.
	 */
	public void setStartupTime(long startupTime) {
		this.startupTime = startupTime;
	}
	/**
	 * @return Returns the totalClassesLoaded.
	 */
	public long getTotalClassesLoaded() {
		return totalClassesLoaded;
	}
	/**
	 * @param totalClassesLoaded The totalClassesLoaded to set.
	 */
	public void setTotalClassesLoaded(long totalClassesLoaded) {
		this.totalClassesLoaded = totalClassesLoaded;
	}
	/**
	 * @return Returns the totalCPUTime.
	 */
	public long getTotalCPUTime() {
		return totalCPUTime;
	}
	/**
	 * @param totalCPUTime The totalCPUTime to set.
	 */
	public void setTotalCPUTime(long totalCPUTime) {
		this.totalCPUTime = totalCPUTime;
	}
	
	/**
	 * @return the totalKernelTime
	 */
	public long getTotalKernelTime() {
		return totalKernelTime;
	}


	/**
	 * @param totalKernelTime the totalKernelTime to set
	 */
	public void setTotalKernelTime(long totalKernelTime) {
		this.totalKernelTime = totalKernelTime;
	}


	/**
	 * @return the totalUserTime
	 */
	public long getTotalUserTime() {
		return totalUserTime;
	}


	/**
	 * @param totalUserTime the totalUserTime to set
	 */
	public void setTotalUserTime(long totalUserTime) {
		this.totalUserTime = totalUserTime;
	}


	/**
	 * @return Returns the totalGCs.
	 */
	public long getTotalGCs() {
		return totalGCs;
	}
	/**
	 * @param totalGCs The totalGCs to set.
	 */
	public void setTotalGCs(long totalGCs) {
		this.totalGCs = totalGCs;
	}
	/**
	 * @return Returns the totalGCTime.
	 */
	public long getTotalGCTime() {
		return totalGCTime;
	}
	/**
	 * @param totalGCTime The totalGCTime to set.
	 */
	public void setTotalGCTime(long totalGCTime) {
		this.totalGCTime = totalGCTime;
	}
	
	/**
	 * @return Returns the currentCPUPercent.
	 */
	public double getCurrentCPUPercent() {
		return currentCPUPercent;
	}
	/**
	 * @param currentCPUPercent The currentCPUPercent to set.
	 */
	public void setCurrentCPUPercent(double currentCPUPercent) {
		this.currentCPUPercent = currentCPUPercent;
	}
	
	/**
	 * @return Returns the averageCPUPercent.
	 */
	public double getAverageCPUPercent() {
		return averageCPUPercent;
	}
	/**
	 * @param averageCPUPercent The averageCPUPercent to set.
	 */
	public void setAverageCPUPercent(double averageCPUPercent) {
		this.averageCPUPercent = averageCPUPercent;
	}
	/**
	 * @return Returns the totalExecutionTime.
	 */
	public long getTotalExecutionTime() {
		return totalExecutionTime;
	}
	/**
	 * @param totalExecutionTime The totalExecutionTime to set.
	 */
	public void setTotalExecutionTime(long totalExecutionTime) {
		this.totalExecutionTime = totalExecutionTime;
	}
	
	/**
	 * @return Returns the tid.
	 */
	public ThreadInfoData getThreadInfoData() {
		return tid;
	}
	/**
	 * @param tid The tid to set.
	 */
	public void setThreadInfoData(ThreadInfoData tid) {
		this.tid = tid;
	}
	
	/**
	 * @return Returns the jvmOpts.
	 */
	public String getJvmOpts() {
		return jvmOpts;
	}
	/**
	 * @param jvmOpts The jvmOpts to set.
	 */
	public void setJvmOpts(String jvmOpts) {
		this.jvmOpts = jvmOpts;
	}
	
	/**
	 * @return Returns the allThreads.
	 */
	public long[] getAllThreads() {
		return allThreads;
	}
	/**
	 * @param allThreads The allThreads to set.
	 */
	public void setAllThreads(long[] allThreads) {
		this.allThreads = allThreads;
	}
	
	/**
	 * @return Returns the classpath.
	 */
	public String getClasspath() {
		return classpath;
	}
	/**
	 * @param classpath The classpath to set.
	 */
	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}
	/**
	 * @return Returns the javaProperties.
	 */
	public String getJavaProperties() {
		return javaProperties;
	}
	/**
	 * @param javaProperties The javaProperties to set.
	 */
	public void setJavaProperties(String javaProperties) {
		this.javaProperties = javaProperties;
	}
	
	public String getEnvProperties() {
        return envProperties;
    }


    public void setEnvProperties(String envProperties) {
        this.envProperties = envProperties;
    }


    /**
	 * @return Returns the loadedClasses.
	 */
	public String getLoadedClasses() {
		return loadedClasses;
	}
	/**
	 * @param loadedClasses The loadedClasses to set.
	 */
	public void setLoadedClasses(String loadedClasses) {
		this.loadedClasses = loadedClasses;
	}
	
	/**
	 * @return Returns the loadedPackages.
	 */
	public String getLoadedPackages() {
		return loadedPackages;
	}
	/**
	 * @param loadedPackages The loadedPackages to set.
	 */
	public void setLoadedPackages(String loadedPackages) {
		this.loadedPackages = loadedPackages;
	}
	
	/**
	 * @return Returns the numberOfDaemonThreads.
	 */
	public int getNumberOfDaemonThreads() {
		return numberOfDaemonThreads;
	}
	/**
	 * @param numberOfDaemonThreads The numberOfDaemonThreads to set.
	 */
	public void setNumberOfDaemonThreads(int numberOfDaemonThreads) {
		this.numberOfDaemonThreads = numberOfDaemonThreads;
	}
	
	/**
	 * @return Returns the osCommittedVirtMemSize.
	 */
	public long getOsCommittedVirtMemSize() {
		return osCommittedVirtMemSize;
	}
	/**
	 * @param osCommittedVirtMemSize The osCommittedVirtMemSize to set.
	 */
	public void setOsCommittedVirtMemSize(long osCommittedVirtMemSize) {
		this.osCommittedVirtMemSize = osCommittedVirtMemSize;
	}
	/**
	 * @return Returns the osFreePhysMemSize.
	 */
	public long getOsFreePhysMemSize() {
		return osFreePhysMemSize;
	}
	/**
	 * @param osFreePhysMemSize The osFreePhysMemSize to set.
	 */
	public void setOsFreePhysMemSize(long osFreePhysMemSize) {
		this.osFreePhysMemSize = osFreePhysMemSize;
	}
	/**
	 * @return Returns the osFreeSwapSpaceSize.
	 */
	public long getOsFreeSwapSpaceSize() {
		return osFreeSwapSpaceSize;
	}
	/**
	 * @param osFreeSwapSpaceSize The osFreeSwapSpaceSize to set.
	 */
	public void setOsFreeSwapSpaceSize(long osFreeSwapSpaceSize) {
		this.osFreeSwapSpaceSize = osFreeSwapSpaceSize;
	}
	/**
	 * @return Returns the osTotalPhysMemSize.
	 */
	public long getOsTotalPhysMemSize() {
		return osTotalPhysMemSize;
	}
	/**
	 * @param osTotalPhysMemSize The osTotalPhysMemSize to set.
	 */
	public void setOsTotalPhysMemSize(long osTotalPhysMemSize) {
		this.osTotalPhysMemSize = osTotalPhysMemSize;
	}
	/**
	 * @return Returns the osTotoalSwapSpaceSize.
	 */
	public long getOsTotoalSwapSpaceSize() {
		return osTotoalSwapSpaceSize;
	}
	/**
	 * @param osTotoalSwapSpaceSize The osTotoalSwapSpaceSize to set.
	 */
	public void setOsTotoalSwapSpaceSize(long osTotoalSwapSpaceSize) {
		this.osTotoalSwapSpaceSize = osTotoalSwapSpaceSize;
	}
	
	/**
	 * @return Returns the osName.
	 */
	public String getOsName() {
		return osName;
	}
	/**
	 * @param osName The osName to set.
	 */
	public void setOsName(String osName) {
		this.osName = osName;
	}
	
	/**
	 * @return Returns the numberOfCPUs.
	 */
	public int getNumberOfCPUs() {
		return numberOfCPUs;
	}
	/**
	 * @param numberOfCPUs The numberOfCPUs to set.
	 */
	public void setNumberOfCPUs(int numberOfCPUs) {
		this.numberOfCPUs = numberOfCPUs;
	}
	
	/**
	 * @return the numberOfPhysicalCPUs
	 */
	public long getNumberOfPhysicalCPUs() {
		return numberOfPhysicalCPUs;
	}


	/**
	 * @param numberOfPhysicalCPUs the numberOfPhysicalCPUs to set
	 */
	public void setNumberOfPhysicalCPUs(long numberOfPhysicalCPUs) {
		this.numberOfPhysicalCPUs = numberOfPhysicalCPUs;
	}


	/**
	 * @return the cpuAffinity
	 */
	public long getCpuAffinity() {
		return cpuAffinity;
	}


	/**
	 * @param cpuAffinity the cpuAffinity to set
	 */
	public void setCpuAffinity(long cpuAffinity) {
		this.cpuAffinity = cpuAffinity;
	}


	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}


	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}


	/**
	 * @return the probeName
	 */
	public String getProbeName() {
		return probeName;
	}


	/**
	 * @param probeName the probeName to set
	 */
	public void setProbeName(String probeName) {
		this.probeName = probeName;
	}

	
	/**
	 * @return the hostIP
	 */
	public String getHostIP() {
		return hostIP;
	}


	/**
	 * @param hostIP the hostIP to set
	 */
	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	/**
	 * @return the classpath_updated
	 */
	public boolean isClasspath_updated() {
		return classpath_updated;
	}


	/**
	 * @param classpath_updated the classpath_updated to set
	 */
	public void setClasspath_updated(boolean classpath_updated) {
		this.classpath_updated = classpath_updated;
	}


	/**
	 * @return the javaProperties_updated
	 */
	public boolean isJavaProperties_updated() {
		return javaProperties_updated;
	}


	/**
	 * @param javaProperties_updated the javaProperties_updated to set
	 */
	public void setJavaProperties_updated(boolean javaProperties_updated) {
		this.javaProperties_updated = javaProperties_updated;
	}


	/**
	 * @return the loadedClasses_updated
	 */
	public boolean isLoadedClasses_updated() {
		return loadedClasses_updated;
	}


	/**
	 * @param loadedClasses_updated the loadedClasses_updated to set
	 */
	public void setLoadedClasses_updated(boolean loadedClasses_updated) {
		this.loadedClasses_updated = loadedClasses_updated;
	}


	/**
	 * @return the loadedPackages_updated
	 */
	public boolean isLoadedPackages_updated() {
		return loadedPackages_updated;
	}


	/**
	 * @param loadedPackages_updated the loadedPackages_updated to set
	 */
	public void setLoadedPackages_updated(boolean loadedPackages_updated) {
		this.loadedPackages_updated = loadedPackages_updated;
	}


	/**
	 * @return the jvmOpts_updates
	 */
	public boolean isJvmOpts_updates() {
		return jvmOpts_updates;
	}


	/**
	 * @param jvmOpts_updates the jvmOpts_updates to set
	 */
	public void setJvmOpts_updates(boolean jvmOpts_updates) {
		this.jvmOpts_updates = jvmOpts_updates;
	}
	

	public boolean isEnvProperties_updated() {
        return envProperties_updated;
    }


    public void setEnvProperties_updated(boolean envProperties_updated) {
        this.envProperties_updated = envProperties_updated;
    }


    /**
	 * @return the classLoaders
	 */
	public String getClassLoaders() {
		return classLoaders;
	}


	/**
	 * @param classLoaders the classLoaders to set
	 */
	public void setClassLoaders(String classLoaders) {
		this.classLoaders = classLoaders;
	}


	/**
	 * @return the classLoaders_updated
	 */
	public boolean isClassLoaders_updated() {
		return classLoaders_updated;
	}


	/**
	 * @param classLoaders_updated the classLoaders_updated to set
	 */
	public void setClassLoaders_updated(boolean classLoaders_updated) {
		this.classLoaders_updated = classLoaders_updated;
	}

	
	/**
	 * @return the numberOfClassLoaders
	 */
	public long getNumberOfClassLoaders() {
		return numberOfClassLoaders;
	}


	/**
	 * @param numberOfClassLoaders the numberOfClassLoaders to set
	 */
	public void setNumberOfClassLoaders(long numberOfClassLoaders) {
		this.numberOfClassLoaders = numberOfClassLoaders;
	}


	/**
	 * @return the numberOfLoadedClasses
	 */
	public long getNumberOfLoadedClasses() {
		return numberOfLoadedClasses;
	}


	/**
	 * @param numberOfLoadedClasses the numberOfLoadedClasses to set
	 */
	public void setNumberOfLoadedClasses(long numberOfLoadedClasses) {
		this.numberOfLoadedClasses = numberOfLoadedClasses;
	}


	/**
	 * @return the numberOfLoadedPackages
	 */
	public long getNumberOfLoadedPackages() {
		return numberOfLoadedPackages;
	}


	/**
	 * @param numberOfLoadedPackages the numberOfLoadedPackages to set
	 */
	public void setNumberOfLoadedPackages(long numberOfLoadedPackages) {
		this.numberOfLoadedPackages = numberOfLoadedPackages;
	}


	/**
	 * @return the totalClassSize
	 */
	public long getTotalClassSize() {
		return totalClassSize;
	}


	/**
	 * @return the ioOtherBytes
	 */
	public long getIoOtherBytes() {
		return ioOtherBytes;
	}


	/**
	 * @param ioOtherBytes the ioOtherBytes to set
	 */
	public void setIoOtherBytes(long ioOtherBytes) {
		this.ioOtherBytes = ioOtherBytes;
	}


	/**
	 * @return the ioOtherCount
	 */
	public long getIoOtherCount() {
		return ioOtherCount;
	}


	/**
	 * @param ioOtherCount the ioOtherCount to set
	 */
	public void setIoOtherCount(long ioOtherCount) {
		this.ioOtherCount = ioOtherCount;
	}


	/**
	 * @return the ioReadBytes
	 */
	public long getIoReadBytes() {
		return ioReadBytes;
	}


	/**
	 * @param ioReadBytes the ioReadBytes to set
	 */
	public void setIoReadBytes(long ioReadBytes) {
		this.ioReadBytes = ioReadBytes;
	}


	/**
	 * @return the ioReadCount
	 */
	public long getIoReadCount() {
		return ioReadCount;
	}


	/**
	 * @param ioReadCount the ioReadCount to set
	 */
	public void setIoReadCount(long ioReadCount) {
		this.ioReadCount = ioReadCount;
	}


	/**
	 * @return the ioWriteBytes
	 */
	public long getIoWriteBytes() {
		return ioWriteBytes;
	}


	/**
	 * @param ioWriteBytes the ioWriteBytes to set
	 */
	public void setIoWriteBytes(long ioWriteBytes) {
		this.ioWriteBytes = ioWriteBytes;
	}


	/**
	 * @return the ioWriteCount
	 */
	public long getIoWriteCount() {
		return ioWriteCount;
	}


	/**
	 * @param ioWriteCount the ioWriteCount to set
	 */
	public void setIoWriteCount(long ioWriteCount) {
		this.ioWriteCount = ioWriteCount;
	}


	/**
	 * @param totalClassSize the totalClassSize to set
	 */
	public void setTotalClassSize(long totalClassSize) {
		this.totalClassSize = totalClassSize;
	}

	
	/**
	 * @return the ioCountersAvailable
	 */
	public boolean isIoCountersAvailable() {
		return ioCountersAvailable;
	}


	/**
	 * @param ioCountersAvailable the ioCountersAvailable to set
	 */
	public void setIoCountersAvailable(boolean ioCountersAvailable) {
		this.ioCountersAvailable = ioCountersAvailable;
	}


	/**
	 * @return the pageFaults
	 */
	public long getPageFaults() {
		return pageFaults;
	}


	/**
	 * @param pageFaults the pageFaults to set
	 */
	public void setPageFaults(long pageFaults) {
		this.pageFaults = pageFaults;
	}


	/**
	 * @return the pagefileUsage
	 */
	public long getPagefileUsage() {
		return pagefileUsage;
	}


	/**
	 * @param pagefileUsage the pagefileUsage to set
	 */
	public void setPagefileUsage(long pagefileUsage) {
		this.pagefileUsage = pagefileUsage;
	}


	/**
	 * @return the peakPagefileUsage
	 */
	public long getPeakPagefileUsage() {
		return peakPagefileUsage;
	}


	/**
	 * @param peakPagefileUsage the peakPagefileUsage to set
	 */
	public void setPeakPagefileUsage(long peakPagefileUsage) {
		this.peakPagefileUsage = peakPagefileUsage;
	}


	/**
	 * @return the peakWorkingSetSize
	 */
	public long getPeakWorkingSetSize() {
		return peakWorkingSetSize;
	}


	/**
	 * @param peakWorkingSetSize the peakWorkingSetSize to set
	 */
	public void setPeakWorkingSetSize(long peakWorkingSetSize) {
		this.peakWorkingSetSize = peakWorkingSetSize;
	}


	/**
	 * @return the privateBytes
	 */
	public long getPrivateBytes() {
		return privateBytes;
	}


	/**
	 * @param privateBytes the privateBytes to set
	 */
	public void setPrivateBytes(long privateBytes) {
		this.privateBytes = privateBytes;
	}


	/**
	 * @return the workingSetSize
	 */
	public long getWorkingSetSize() {
		return workingSetSize;
	}


	/**
	 * @param workingSetSize the workingSetSize to set
	 */
	public void setWorkingSetSize(long workingSetSize) {
		this.workingSetSize = workingSetSize;
	}


	/**
	 * @return the jmxDomains
	 */
	public String[] getJmxDomains() {
		return jmxDomains;
	}


	/**
	 * @param jmxDomains the jmxDomains to set
	 */
	public void setJmxDomains(String[] jmxDomains) {
		this.jmxDomains = jmxDomains;
	}


	/**
	 * @return the mbeans
	 */
	public Set<ObjectName> getMbeans() {
		return mbeans;
	}


	/**
	 * @param mbeans the mbeans to set
	 */
	public void setMbeans(Set<ObjectName> mbeans) {
		this.mbeans = mbeans;
	}


	/**
	 * @return the attributeList
	 */
	public AttributeList getAttributeList() {
		return attributeList;
	}


	/**
	 * @param attributeList the attributeList to set
	 */
	public void setAttributeList(AttributeList attributeList) {
		this.attributeList = attributeList;
	}


	/**
	 * @return the mbeanInfo
	 */
	public MBeanInfo getMbeanInfo() {
		return mbeanInfo;
	}


	/**
	 * @param mbeanInfo the mbeanInfo to set
	 */
	public void setMbeanInfo(MBeanInfo mbeanInfo) {
		this.mbeanInfo = mbeanInfo;
	}


	/**
	 * @return the classInfo
	 */
	public String getClassInfo() {
		return classInfo;
	}


	/**
	 * @param classInfo the classInfo to set
	 */
	public void setClassInfo(String classInfo) {
		this.classInfo = classInfo;
	}


	/**
	 * @return the jitCompilerName
	 */
	public String getJitCompilerName() {
		return jitCompilerName;
	}


	/**
	 * @param jitCompilerName the jitCompilerName to set
	 */
	public void setJitCompilerName(String jitCompilerName) {
		this.jitCompilerName = jitCompilerName;
	}


	/**
	 * @return the totalCompilationTime
	 */
	public long getTotalCompilationTime() {
		return totalCompilationTime;
	}


	/**
	 * @param totalCompilationTime the totalCompilationTime to set
	 */
	public void setTotalCompilationTime(long totalCompilationTime) {
		this.totalCompilationTime = totalCompilationTime;
	}


	/**
	 * @return the numberOfObjectsPendingFinalization
	 */
	public int getNumberOfObjectsPendingFinalization() {
		return numberOfObjectsPendingFinalization;
	}


	/**
	 * @param numberOfObjectsPendingFinalization the numberOfObjectsPendingFinalization to set
	 */
	public void setNumberOfObjectsPendingFinalization(
			int numberOfObjectsPendingFinalization) {
		this.numberOfObjectsPendingFinalization = numberOfObjectsPendingFinalization;
	}


	/**
	 * @return the numberOfObjectsPendingFinalizationHWM
	 */
	public int getNumberOfObjectsPendingFinalizationHWM() {
		return numberOfObjectsPendingFinalizationHWM;
	}


	/**
	 * @param numberOfObjectsPendingFinalizationHWM the numberOfObjectsPendingFinalizationHWM to set
	 */
	public void setNumberOfObjectsPendingFinalizationHWM(
			int numberOfObjectsPendingFinalizationHWM) {
		this.numberOfObjectsPendingFinalizationHWM = numberOfObjectsPendingFinalizationHWM;
	}


	/**
	 * @return the stackTraceInfo
	 */
	public String getStackTraceInfo() {
		return stackTraceInfo;
	}


	/**
	 * @param stackTraceInfo the stackTraceInfo to set
	 */
	public void setStackTraceInfo(String stackTraceInfo) {
		this.stackTraceInfo = stackTraceInfo;
	}

	/**
	 * @return the nativeLibs
	 */
	public OSLibInfo[] getNativeLibs() {
		return nativeLibs;
	}


	/**
	 * @param nativeLibs the nativeLibs to set
	 */
	public void setNativeLibs(OSLibInfo[] nativeLibs) {
		this.nativeLibs = nativeLibs;
	}


	/**
	 * @return the processID
	 */
	public long getProcessID() {
		return processID;
	}


	/**
	 * @param processID the processID to set
	 */
	public void setProcessID(long processID) {
		this.processID = processID;
	}


	/**
	 * @return the averageSystemLoad
	 */
	public double getAverageSystemLoad() {
		return averageSystemLoad;
	}


	/**
	 * @param averageSystemLoad the averageSystemLoad to set
	 */
	public void setAverageSystemLoad(double averageSystemLoad) {
		this.averageSystemLoad = averageSystemLoad;
	}


	/**
     * @return the systemCpuLoad
     */
    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }


    /**
     * @param systemCpuLoad the systemCpuLoad to set
     */
    public void setSystemCpuLoad(double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }


    /**
     * @return the processCpuLoad
     */
    public double getProcessCpuLoad() {
        return processCpuLoad;
    }


    /**
     * @param processCpuLoad the processCpuLoad to set
     */
    public void setProcessCpuLoad(double processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }


    public String getServiceURL() {
        return serviceURL;
    }


    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }


    public String getJvmName() {
        return jvmName;
    }


    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
    }


    public byte[] getClassBytes() {
        return classBytes;
    }


    public List<GarbageCollectorProperties> getGarbageCollectors() {
        return garbageCollectors;
    }


    public void setGarbageCollectors(List<GarbageCollectorProperties> garbageCollectors) {
        this.garbageCollectors = garbageCollectors;
    }


    public void setClassBytes(byte[] classBytes) {
        this.classBytes = classBytes;
    }

    public Vector<ResponseThreadData> getAllThreadData() {
        return threadData;
    }
    
    public String getJavaVersion() {
        return javaVersion;
    }


    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }


    /**
     * @return the probeError
     */
    public Throwable getProbeError() {
        return probeError;
    }


    /**
     * @param probeError the probeError to set
     */
    public void setProbeError(Throwable probeError) {
        this.probeError = probeError;
    }


    /**
	 * Remove all elements from our Vector lists
	 *
	 */
	public void clear() {
		
		threadData.clear();
		poolUsage.clear();
		gcData.clear();
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
 
