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
//import static edu.regis.jprobe.model.Utilities.encrypt;
import static edu.regis.jprobe.model.Utilities.formatException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.MonitorInfo;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import edu.regis.jprobe.agent.mbean.ProbeStatus;
import edu.regis.jprobe.jni.OSIOCounters;
import edu.regis.jprobe.jni.OSLibInfo;
import edu.regis.jprobe.jni.OSProcessMemoryInfo;
import edu.regis.jprobe.jni.OSProcessTimes;
import edu.regis.jprobe.jni.OSSystemInfo;
import edu.regis.jprobe.model.ClassFormatter;
import edu.regis.jprobe.model.GarbageCollectorData;
import edu.regis.jprobe.model.MemoryPoolData;
import edu.regis.jprobe.model.ProbeRequest;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.ResponseMonitorInfo;
import edu.regis.jprobe.model.ResponseThreadData;
import edu.regis.jprobe.model.ServerGCProperties;
import edu.regis.jprobe.model.ServerOSProperties;
import edu.regis.jprobe.model.ThreadInfoData;
import edu.regis.jprobe.model.Utilities;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author jdivince
 *
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class ProbeThread extends Thread {

	private ProbeClassFileTransformer xform;
	private ProbeRecorderThread prt;
	private String probeName;
	private String hostName;
	private String hostIP;
	private String userid;
	private String password;
	private String lastJvmOpts = " ";
	private String lastClasspath = " ";
	private String lastJavaProperties = " ";
	private String lastEnvProperties = " ";
	private String lastLoadedClasses = " ";
	private String lastLoadedPackages = " ";
	private String lastClassLoaders = " ";
	private String jvmName = "?";
	private long numberOfLoadedClasses = 0;
	private long numberOfLoadedPackages = 0;
	private long numberOfClassLoaders = 0;
	private long processID = 0;
	private int objectsPendingFinalizationHWM = 0;
	private ThreadMXBean threadBean;
	private ClassLoadingMXBean classBean;
	private RuntimeMXBean rtBean;
	private MemoryMXBean memoryBean;
	private MBeanServerConnection mbsc;
	private MBeanServer mBeanServer;
	private OperatingSystemMXBean osmb;
	private CompilationMXBean compb;
	private java.lang.management.OperatingSystemMXBean osMXBean;
	protected ProbeServerManager server;
	private Method getAllocatedBytes;
	private boolean shutdown = false;
	private List<String> jvmArgs;
	private String jvmopts = "";
	private static final int STRING_BUFFER_SIZE = 4096;
	public static final long NANOS_PER_MILLI = 1000000;
	private String systemProperties = "";
	private String envProperties = "";
	private String classPath = "";
	private String loadedClasses = "Not Obtained";
	private String loadedPackages = "Not Obtained";
	private String classLoaders = "Not Obtained";
	private String dumpMessage = "";
	private OSLibInfo nativeLibs[];
	private boolean nativeLibsUpdated = false;

	private boolean noOSMbeans = false;
	private boolean useOSSystemInfo = false;
	private long startTime;
	private String interruptMsg;
	private ProbeStatus mbean;
	private ProbeState status;

	// JAVA JVM System Property Names
	private static final String JVMOPT_PASSWORD = "edu.regis.jprobe.password";
	private static final String JVMOPT_USER_NAME = "user.name";
	private static final String JVMOPT_USER_NAME_OVERRIDE = "edu.regis.jprobe.username";
	private static final String JVMOPT_NOOSBEAN = "edu.regis.jprobe.noosbean";

	public ProbeThread(Instrumentation inst, String name, ProbeClassFileTransformer xform, ProbeState status) {

		this.xform = xform;
		this.probeName = name;
		this.status = status;
		this.setName("Probe Thread <init>");
		this.setUncaughtExceptionHandler(new ProbeUncaughtExceptionHandler(inst, name));
		this.userid = System.getProperty(JVMOPT_USER_NAME_OVERRIDE);
		if (this.userid == null)
			this.userid = System.getProperty(JVMOPT_USER_NAME);
		this.password = System.getProperty(JVMOPT_PASSWORD);

		if (password != null) {
			debugMsg("This Probe Is Password Protected for Remote Access");
		}
		// Some J2EE app servers don't like having a MBean server running before
		// They start their own proxy, This turns off the SunOsBean server.
		String osMbean = System.getProperty(JVMOPT_NOOSBEAN);
		if ("true".equalsIgnoreCase(osMbean))
			noOSMbeans = true;

		debugMsg("Initializing Probe Thread");
		memoryBean = ManagementFactory.getMemoryMXBean();
		classBean = ManagementFactory.getClassLoadingMXBean();
		threadBean = ManagementFactory.getThreadMXBean();
		getAllocationMethod();
		osMXBean = ManagementFactory.getOperatingSystemMXBean();
		compb = ManagementFactory.getCompilationMXBean();

		mBeanServer = ManagementFactory.getPlatformMBeanServer();
		mbean = new ProbeStatus(this);
		mbean.setStatus("Initializing");
		mbean.setProbeVersion(VERSION_HEADING);

		try {

			String jmxDomainName = "JavaVMProbe";
			mBeanServer = ManagementFactory.getPlatformMBeanServer();
			Hashtable<String, String> mBeanProps = new Hashtable<String, String>();
			mBeanProps.put("type", "Perfomance");
			mBeanProps.put("server", InetAddress.getLocalHost().getCanonicalHostName());
			mBeanServer.registerMBean(mbean, new ObjectName(jmxDomainName, mBeanProps));
		} catch (Exception e) {
			debugMsg("Can't start MBean: " + e.getMessage());
			mbean.addException(e);
		}

		if (!threadBean.isThreadCpuTimeSupported()) {
			System.out.println(VERSION_HEADING + " - Thread CPU Timing Is Not Supported, Probe Ending...");
			shutdown = true;
			return;
		}
		if (!threadBean.isThreadCpuTimeEnabled()) {
			threadBean.setThreadCpuTimeEnabled(true);
			debugMsg("Thread CPU Timing Was Not Enabled, Enabling...");
		}

		if (!threadBean.isThreadContentionMonitoringEnabled()) {
			threadBean.setThreadContentionMonitoringEnabled(true);
			debugMsg("Thread Contention Monitoring Was Not Enabled, Enabling...");
		}

		if (!threadBean.isObjectMonitorUsageSupported()) {
			debugMsg("Thread Object Monitor Usage is not Supported");
			System.out.println("Thread Object Monitor Usage is not Supported");
		}
		if (!threadBean.isSynchronizerUsageSupported()) {
			debugMsg("Thread Synchronizer Usage is not Supported");
			System.out.println("Thread Object Monitor Usage is not Supported");
		}

		startTime = System.nanoTime();
		rtBean = ManagementFactory.getRuntimeMXBean();

		if (probeName.equals(""))
			probeName = rtBean.getName();

		try {
			hostName = InetAddress.getLocalHost().getHostName();
			hostIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			hostName = "Unknown";
			hostIP = "127.0.0.1";
			mbean.addException(e);
		}

		jvmArgs = rtBean.getInputArguments();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < jvmArgs.size(); i++) {
			String temp = jvmArgs.get(i).toString();

			if (temp.equals("-D" + JVMOPT_PASSWORD + "=" + password)) {
				sb.append("-D" + JVMOPT_PASSWORD + "=********");
				// jvmopts += "-D" + JVMOPT_PASSWORD + "=********\n";
			} else {
				// jvmopts +=temp + "\n";
				sb.append(temp);

			}
			sb.append("\n");

		}
		jvmopts = sb.toString();
		jvmName = System.getProperty("java.vm.name");

		useOSSystemInfo = OSSystemInfo.isOperational();

		if (useOSSystemInfo) {
			try {
				processID = OSSystemInfo.getCurrentProcessID();
			} catch (Exception e) {
				processID = -1;
			}
		}

	}

	public void run() {

		debugMsg("Probe Thread is Starting...");

		// Ugly! but if this thread pre-empts the main thread, it is
		// possible to execute the notify before main has entered a
		// wait!
		Utilities.sleep(50);
		if (!noOSMbeans) {
			mbsc = ManagementFactory.getPlatformMBeanServer();
			osmb = getSunOSBean();
		}

		debugMsg("Creating Server Manager");
		try {
			server = new ProbeServerManager(probeName, status);
		} catch (ProbeServerManagerException e1) {
			System.err.println("Unable to Initialize Server Communications, Error is " + e1.getMessage());
			System.err.println("Probe Disabled");
			mbean.addException(e1);
			return;
		}

		Runtime.getRuntime().addShutdownHook(new ProbeShutdownHook(server));
		mbean.setServiceURL(server.getServiceURL());

		if (ProbeRecorderThread.isRecordingEnabled()) {
			try {
				startProbeRecorder();
			} catch (IllegalStateException e) {
				debugMsg("Unable To Start Recorder Thread");
				mbean.addException(e);
			}

		}

		while (!shutdown) {

			ProbeRequest request = null;

			try {
				mbean.setStatus("Waiting for Connect");
				server.waitForConnect();

			} catch (ProbeServerManagerException e1) {
				debugMsg(formatException(e1, this));
				mbean.addException(e1);
				continue;
			}
			mbean.setStatus("Connected to " + server.getClientIP());
			mbean.setConnectTime();
			boolean disconnectRequested = false;

			if (!threadBean.isThreadCpuTimeEnabled()) {
				threadBean.setThreadCpuTimeEnabled(true);
				Utilities.debugMsg("Thread CPU Timing was Disabled, enabling");
			}
			if (!threadBean.isThreadContentionMonitoringEnabled()) {
				threadBean.setThreadContentionMonitoringEnabled(true);
				debugMsg("Thread Contention Monitoring Was Not Enabled, Enabling...");
			}

			getJVMProperties();
			getEnvProperties();
			getNativeLibs();
			resetSavedProperties(); // make sure we send this
			getClassInfo();

			try {
				while (!disconnectRequested) {

					request = server.getRequest();

					long start = System.currentTimeMillis();

					if (request.getRequestType() == ProbeRequest.REQ_DISCONNECT) {
						disconnectRequested = true;
						continue;
					}
					mbean.setLastRequest(request.getRequestName(request.getRequestType()));
					mbean.setSendCount(server.getSendCount());
					mbean.setReceiveCount(server.getReceiveCount());
					mbean.setBroadcastCount(server.getBroadcastCount());
					mbean.setSendBytes(server.getSendBytes());
					mbean.setSendUncompressedBytes(server.getSendUncompressedBytes());
					mbean.setReceiveBytes(server.getReceiveBytes());
					mbean.setReceiveUncompressedBytes(server.getReceiveUncompressedBytes());
					debugMsg("Receiving Request: " + request.getRequestName(request.getRequestType()));
					ProbeResponse response = null;
					try {
						response = processRequest(request);
					} catch (Exception e) {
						response = new ProbeResponse();
						response.setResponseCode(ProbeResponse.RES_FAILED);
						response.setResponseErrorMessage(e.getMessage());
						response.setProbeError(e);
						mbean.addException(e);
					}

					mbean.addToResponseTime(System.currentTimeMillis() - start);

					if (shutdown) {
						mbean.setStatus("Shutting Down");
						server.disconnect();
						System.out.println(VERSION_HEADING + " - Monitor Disabled");
						return;
					}

					server.sendResponse(response);

				}
				server.disconnect();
			} catch (ProbeServerManagerException e) {
				// e.printStackTrace();
				debugMsg(formatException(e, this));
				server.disconnect();
				mbean.addException(e);
				continue;
			} catch (Exception e) {
				debugMsg(formatException(e, this));
				server.disconnect();
				mbean.addException(e);
				continue;
			}

		}

		debugMsg("Closing Connection");

	}

	protected ProbeResponse processRequest(ProbeRequest request) {

		ProbeResponse response = new ProbeResponse();
		boolean isAuthorized = false;

		if (request.isProbeDebug() != Utilities.debug) {
			Utilities.debug = request.isProbeDebug();
		}
		// Check to make sure the ip address in the socket matches the ip
		// address
		// provided by the client...
		/*
		 * if (!request.getClientIP().equals(server.getClientIP())) { response =
		 * new ProbeResponse();
		 * response.setResponseCode(ProbeResponse.RES_INVALID_AUTHORIZATION);
		 * response.
		 * setResponseErrorMessage("Invalid Spoofed Client IP Address From Remote Client"
		 * ); return response; }
		 */

		// String pwString = "";
		// if (request.getPassword() != null)
		// pwString = new String(encrypt(request.getPassword()));
		// First check to see if the client is on the same system, if so, let
		// the
		// command proceed, otherwise, make sure the userid is the same, if not,
		// deny the request. If it is the same, check to see if a password was
		// specified
		// for the probe, if it is, it must match the one provided by the
		// client.
		// if (!request.getClientIP().equals(hostIP)) {
		// if (!request.getUserId().equals(userid)) {
		// response.setResponseCode(ProbeResponse.RES_INVALID_AUTHORIZATION);
		// response.setResponseErrorMessage("Invalid Userid From Remote
		// Client");
		// return response;
		// } else {
		// if (password != null && !password.equals(pwString)) {
		// response.setResponseCode(ProbeResponse.RES_INVALID_AUTHORIZATION);
		// response.setResponseErrorMessage("Invalid Password From Remote
		// Client");
		// return response;
		// }
		// }
		// } else {
		isAuthorized = true;
		// }

		if (request.isResetOptionData()) {
			resetSavedProperties();
		}

		switch (request.getRequestType()) {

		case ProbeRequest.REQ_STATS:
			response = getData(request.isCollectMonitorInfo(), request.isCollectLockInfo());
			response.setResponseCode(ProbeResponse.RES_OK);
			break;
		case ProbeRequest.REQ_THREAD_INFO:
			ThreadInfoData tid = getThreadData(request.getThreadId(), request.isCollectMonitorInfo(),
					request.isCollectLockInfo());
			response.setThreadInfoData(tid);
			response.setResponseCode(ProbeResponse.RES_OK);
			break;
		case ProbeRequest.REQ_STOP_PROBE:
			shutdown = true;
			break;
		case ProbeRequest.REQ_SEND_GC:
			long freeBefore = Runtime.getRuntime().freeMemory();
			System.runFinalization();
			System.gc();
			// lets trim the ws size
			if (OSSystemInfo.isOperational()) {
				int res = OSSystemInfo.emptyWorkingSet();
				if (res != OSSystemInfo.SYSTEM_CALL_OK) {
					debugMsg("EmptyWorkingSet Failed, GetLastError= " + res);
				}
			}
			long freeAfter = Runtime.getRuntime().freeMemory();
			debugMsg("GC issued, Free memory before " + Utilities.format(freeBefore) + ", Free memory after "
					+ Utilities.format(freeAfter) + ", memory Freed is " + Utilities.format(freeAfter - freeBefore));
			response.setResponseCode(ProbeResponse.RES_OK);
			break;
		case ProbeRequest.REQ_CLASS_INFO:
			this.lastClassLoaders = "n/a";
			this.lastClasspath = "n/a";
			this.lastEnvProperties = "n/a";
			this.lastJavaProperties = "n/a";
			this.lastJvmOpts = "n/a";
			this.lastLoadedClasses = "n/a";
			this.lastLoadedPackages = "n/a";

			getClassInfo();
			getJVMProperties();
			getNativeLibs();
			getEnvProperties();
			response.setResponseCode(ProbeResponse.RES_OK);
			break;
		case ProbeRequest.REQ_THREAD_INTERRUPT:
			if (isAuthorized) {
				if (interruptThread(request.getThreadId())) {
					response.setResponseCode(ProbeResponse.RES_OK);
				} else {
					response.setResponseCode(ProbeResponse.RES_FAILED);
					response.setResponseErrorMessage(interruptMsg);
				}
			} else {
				response.setResponseCode(ProbeResponse.RES_FAILED);
				response.setResponseErrorMessage("This Operation Is Prohibited " + "On A Remote System");
			}
			break;
		case ProbeRequest.REQ_KILL_THREAD:
			if (isAuthorized) {
				if (killThread(request.getThreadId())) {
					response.setResponseCode(ProbeResponse.RES_OK);
				} else {
					response.setResponseCode(ProbeResponse.RES_FAILED);
					response.setResponseErrorMessage(interruptMsg);
				}
			} else {
				response.setResponseCode(ProbeResponse.RES_FAILED);
				response.setResponseErrorMessage("This Operation Is Prohibited " + "On A Remote System");
			}
			break;
		case ProbeRequest.REQ_JVM_EXIT:
			System.out.println(VERSION_HEADING + " - JVM Termination Requested by " + request.getClientHostName() + "("
					+ request.getClientIP() + "), Exit Code(" + request.getExitCode() + ")");
			server.disconnect();
			Utilities.sleep(100);
			System.exit(request.getExitCode());
			break;
		case ProbeRequest.REQ_GET_MBEANS:
			try {
				ObjectName domainName = new ObjectName(request.getMbeanDomain() + ":*");

				response.setMbeans(mBeanServer.queryNames(domainName, null));

				response.setResponseCode(ProbeResponse.RES_OK);
			} catch (Exception e) {
				response.setResponseCode(ProbeResponse.RES_FAILED);
				response.setResponseErrorMessage(
						"Error Obtaining MBean " + request.getMbeanDomain() + ". Error is " + e.getMessage());
				debugMsg(formatException(e, this));
				mbean.addException(e);
			}
			break;
		case ProbeRequest.REQ_GET_MBEAN_INFO:
			try {
				ObjectName beanName = request.getObjectName();
				MBeanInfo info = mBeanServer.getMBeanInfo(beanName);
				response.setMbeanInfo(info);
				MBeanAttributeInfo attr[] = info.getAttributes();
				String attrs[] = new String[attr.length];

				for (int k = 0; k < attr.length; k++) {

					attrs[k] = attr[k].getName();

				}
				AttributeList attrList = mBeanServer.getAttributes(beanName, attrs);
				attrList = inspectAttributes(attrList);
				response.setAttributeList(attrList);

				response.setResponseCode(ProbeResponse.RES_OK);
			} catch (Exception e) {
				response.setResponseCode(ProbeResponse.RES_FAILED);
				response.setResponseErrorMessage("Exception " + e.getClass().getSimpleName()
						+ " Occurred Obtaining MBean " + request.getObjectName() + ". Error is " + e.getMessage());
				debugMsg(formatException(e, this));
			}
			break;
		case ProbeRequest.REQ_SET_PROPERTY:
			try {
				System.setProperty(request.getPropertyKey(), request.getPropertyValue());
			} catch (Exception e) {
				response.setResponseCode(ProbeResponse.RES_FAILED);
				response.setResponseErrorMessage(e.getMessage());
				mbean.addException(e);
				break;
			}
			resetSavedProperties();
			getJVMProperties();
			response.setResponseCode(ProbeResponse.RES_OK);
			break;
		case ProbeRequest.REQ_REMOVE_PROPERTY:
			try {
				System.clearProperty(request.getPropertyKey());
			} catch (Exception e) {
				response.setResponseCode(ProbeResponse.RES_FAILED);
				response.setResponseErrorMessage(e.getMessage());
				mbean.addException(e);
				break;
			}

			resetSavedProperties();
			getJVMProperties();
			response.setResponseCode(ProbeResponse.RES_OK);
			break;
		case ProbeRequest.REQ_SET_AFFINITY:
			long resp = OSSystemInfo.setCPUAffinity(request.getAffinity());
			if (resp != 0) {
				debugMsg("Set Affinity Failed, GetLastError=" + resp);
			}

			response.setResponseCode(ProbeResponse.RES_OK);
			break;
		case ProbeRequest.REQ_GET_CLASS:

			response.setClassInfo(getClassProperties(request.getClassName()));
			response.setResponseCode(ProbeResponse.RES_OK);
			break;

		case ProbeRequest.REQ_DUMP_STACKTRACE:

			response.setStackTraceInfo(Utilities.formatStackTrace());
			response.setResponseCode(ProbeResponse.RES_OK);
			break;
		case ProbeRequest.REQ_DUMP_HEAP:
			dumpHeap();
			response.setResponseCode(ProbeResponse.RES_OK);
			response.setResponseErrorMessage(dumpMessage);
			break;
		default:
			response.setResponseCode(ProbeResponse.RES_FAILED);
			response.setResponseErrorMessage("Unknown Command, command=" + request.getRequestType());
		}
		return response;
	}

	private ProbeResponse getData(boolean getMonitorInfo, boolean getLockInfo) {

		long currentTime = System.nanoTime() - startTime;
		ProbeResponse response = new ProbeResponse();
		response.setCurrentTime(System.currentTimeMillis());
		response.setProbeName(probeName);
		response.setHostName(hostName);
		response.setHostIP(hostIP);
		response.setExecutionStartTime(startTime);
		response.setJmxDomains(mBeanServer.getDomains());
		response.setJitCompilerName(compb.getName());
		response.setTotalCompilationTime(compb.getTotalCompilationTime());
		response.setJvmName(jvmName);
		response.setStartupTime(rtBean.getStartTime());

		if (nativeLibsUpdated) {
			response.setNativeLibs(nativeLibs);
			nativeLibsUpdated = false;
		}

		if (!lastJavaProperties.equals(systemProperties)) {
			response.setJavaProperties(systemProperties);
			response.setJavaProperties_updated(true);
			lastJavaProperties = systemProperties;
			response.setNativeLibs(nativeLibs);
		}
		if (!lastEnvProperties.equals(envProperties)) {
			response.setEnvProperties(envProperties);
			response.setEnvProperties_updated(true);
			lastEnvProperties = envProperties;
		}

		if (!lastClasspath.equals(classPath)) {
			response.setClasspath(classPath);
			response.setClasspath_updated(true);
			lastClasspath = classPath;
		}

		if (!lastLoadedClasses.equals(loadedClasses)) {
			response.setLoadedClasses(loadedClasses);
			response.setLoadedClasses_updated(true);
			lastLoadedClasses = loadedClasses;
		}

		if (!lastClassLoaders.equals(classLoaders)) {
			response.setClassLoaders(classLoaders);
			response.setClassLoaders_updated(true);
			lastClassLoaders = classLoaders;
		}

		if (!lastLoadedPackages.equals(loadedPackages)) {
			response.setLoadedPackages(loadedPackages);
			response.setLoadedPackages_updated(true);
			lastLoadedPackages = loadedPackages;
		}

		if (!lastJvmOpts.equals(jvmopts)) {
			response.setJvmOpts(jvmopts);
			response.setJvmOpts_updates(true);
			lastJvmOpts = jvmopts;
		}

		response.setNumberOfLoadedClasses(numberOfLoadedClasses);
		response.setNumberOfClassLoaders(numberOfClassLoaders);
		response.setNumberOfLoadedPackages(numberOfLoadedPackages);
		response.setTotalClassSize(xform.getTotalClassSize());

		MemoryUsage heap = memoryBean.getHeapMemoryUsage();
		MemoryUsage nonheap = memoryBean.getNonHeapMemoryUsage();

		response.setCurrentHeapSize(heap.getUsed());
		response.setCurrentNonHeapSize(nonheap.getUsed());
		response.setMaxHeapSize(heap.getMax());
		response.setMaxNonHeapSize(nonheap.getMax());

		response.setNumberOfCPUs(osMXBean.getAvailableProcessors());
		response.setAverageSystemLoad(osMXBean.getSystemLoadAverage());

		response.setNumberOfObjectsPendingFinalization(memoryBean.getObjectPendingFinalizationCount());

		if (response.getNumberOfObjectsPendingFinalization() > objectsPendingFinalizationHWM) {
			objectsPendingFinalizationHWM = response.getNumberOfObjectsPendingFinalization();
		}

		response.setNumberOfObjectsPendingFinalizationHWM(objectsPendingFinalizationHWM);

		List<GarbageCollectorMXBean> gcBeans;

		gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

		long gcCount = 0;
		long gcTime = 0;
		long totalCPU = 0;
		long classesLoaded = classBean.getTotalLoadedClassCount();
		// debugMsg("Iterating GC Beans");
		for (int g = 0; g < gcBeans.size(); g++) {
			GarbageCollectorMXBean gcBean = gcBeans.get(g);
			gcCount += gcBean.getCollectionCount();
			gcTime += gcBean.getCollectionTime();

			response.addGCValue(new GarbageCollectorData(gcBean));
		}
		try {
			ServerGCProperties gcProps = new ServerGCProperties(mbsc);
			response.setGarbageCollectors(gcProps.getCollectors());
		} catch (MalformedObjectNameException e1) {
			debugMsg("Exception getting GC properties");
			debugMsg(formatException(e1, this));
			mbean.addException(e1);
		} catch (IOException e1) {
			debugMsg("Exception getting GC properties");
			debugMsg(formatException(e1, this));
			mbean.addException(e1);
		}

		response.setTotalGCs(gcCount);
		response.setTotalGCTime(gcTime);
		long allThreads[] = threadBean.getAllThreadIds();
		response.setAllThreads(allThreads);
		response.setNumberOfActiveThreads(allThreads.length);
		response.setNumberOfDaemonThreads(threadBean.getDaemonThreadCount());
		// debugMsg("Get Thread Data");
		int blockedThreads = 0;

		ThreadInfo[] ti = threadBean.dumpAllThreads(getMonitorInfo, getLockInfo);
		for (int j = 0; j < ti.length; j++) {
			long threadId = ti[j].getThreadId();
			long threadCPU = threadBean.getThreadCpuTime(threadId);

			ResponseThreadData td = null;
			try {
				td = createThreadData(ti[j], currentTime);
			} catch (Exception e) {
				debugMsg("Exception in Probe Thread Formatting id " + threadId);
				debugMsg(formatException(e, this));
				mbean.addException(e);
			}

			if (td != null) {
				if (td.isThreadBlocked()) {
					blockedThreads++;
				}
				totalCPU += threadCPU;
				response.addThreadInfo(td);
			}

		}

		response.setNumberOfBlockedThreads(blockedThreads);

		if (osmb != null) {
			response.setTotalCPUTime(osmb.getProcessCpuTime());
			totalCPU = osmb.getProcessCpuTime();
			response.setOsCommittedVirtMemSize(osmb.getCommittedVirtualMemorySize());
			response.setOsFreePhysMemSize(osmb.getFreePhysicalMemorySize());
			response.setOsFreeSwapSpaceSize(osmb.getFreeSwapSpaceSize());
			response.setOsTotalPhysMemSize(osmb.getTotalPhysicalMemorySize());
			response.setOsTotoalSwapSpaceSize(osmb.getTotalSwapSpaceSize());
			response.setOsName(osmb.getName() + " " + osmb.getVersion() + "-" + osmb.getArch());

		} else {
			response.setTotalCPUTime(totalCPU);
			response.setOsName(osMXBean.getName() + " " + osMXBean.getVersion() + "-" + osMXBean.getArch());
		}
		ServerOSProperties osProps = null;
		try {
			osProps = new ServerOSProperties(mbsc);
			response.setSystemCpuLoad(osProps.getSystemCpuLoad());
			response.setProcessCpuLoad(osProps.getProcessCpuTime());

		} catch (MalformedObjectNameException e) {
			debugMsg("MalformedObjectNameException accessing OS MBean");
		} catch (IOException e) {
			debugMsg("IOException accessing OS MBean, Error is " + e.getMessage());
		}
		response.setTotalExecutionTime(currentTime);
		response.setTotalClassesLoaded(classesLoaded);

		if (useOSSystemInfo) {
			OSIOCounters io = OSSystemInfo.getIOCounters();
			if (io != null) {
				response.setIoReadBytes(io.ioReadBytes);
				response.setIoWriteBytes(io.ioWriteBytes);
				response.setIoOtherBytes(io.ioOtherBytes);
				response.setIoReadCount(io.ioReadCount);
				response.setIoWriteCount(io.ioWriteCount);
				response.setIoOtherCount(io.ioOtherCount);
			}

			OSProcessMemoryInfo mem = OSSystemInfo.getProcessMemoryInfo();
			if (mem != null) {
				response.setPageFaults(mem.pageFaults);
				response.setWorkingSetSize(mem.workingSetSize);
				response.setPeakWorkingSetSize(mem.peakworkingSetSize);
				response.setPagefileUsage(mem.pageFileUsage);
				response.setPeakPagefileUsage(mem.peakPageFileUsage);
				response.setPrivateBytes(mem.privateBytes);
			}

			OSProcessTimes times = OSSystemInfo.getProcessTimes();

			if (times != null) {
				response.setTotalKernelTime(times.kernelTime);
				response.setTotalUserTime(times.userTime);

			}
			response.setCpuAffinity(OSSystemInfo.getCPUAffinity());
			response.setIoCountersAvailable(true);

			response.setNumberOfPhysicalCPUs(OSSystemInfo.getNumberOfCPUs());
			response.setProcessID(processID);
		}

		List<MemoryPoolMXBean> poolBeans = ManagementFactory.getMemoryPoolMXBeans();

		for (int pools = 0; pools < poolBeans.size(); pools++) {

			response.addPoolValue(new MemoryPoolData(poolBeans.get(pools)));

		}
		mbean.setProbeOverhead(((double) mbean.getProbeCPUTime() / (double) totalCPU) * 100);
		mbean.setXformTime(xform.getTransformTime() / NANOS_PER_MILLI);
		return response;
	}

	protected ThreadInfoData getThreadData(long threadId, boolean getMonitorInfo, boolean getLockInfo) {

		ThreadInfoData tid = new ThreadInfoData();

		long totCPU = threadBean.getThreadCpuTime(threadId);
		long totUserCPU = threadBean.getThreadUserTime(threadId);
		long[] thds = new long[1];
		thds[0] = threadId;

		ThreadInfo[] allThreads = threadBean.getThreadInfo(thds, getMonitorInfo, getLockInfo);

		ThreadInfo ti = allThreads[0];

		if (ti == null)
			return null;

		long ownerId = ti.getLockOwnerId();

		StackTraceElement ste[] = ti.getStackTrace();

		Thread thd = getThread(ti.getThreadId());

		// If we have a handle to this thread that we saved in the ClassXformeer
		// and
		// it is not aready in an interrupted status, we set the canInterrupt
		// flag.
		if (thd != null) {
			if (thd.isInterrupted() != true) {
				tid.setCanInterrupt(true);
			}
			tid.setDaemon(thd.isDaemon());
			tid.setPriority(thd.getPriority());
		} else {
			return null;
		}

		tid.setTotalCPU(totCPU);
		tid.setTotalUserTime(totUserCPU);
		tid.setBlockCount(ti.getBlockedCount());
		tid.setBlockTime(ti.getBlockedTime());
		tid.setLockName(ti.getLockName());
		tid.setAllocatedBytes(getThreadAllocatedBytes(threadId));
		if (thd.getContextClassLoader() != null) {
			tid.setContextClassLoader(thd.getContextClassLoader().getClass().getName());
		}
		MonitorInfo mi[] = ti.getLockedMonitors();
		StringBuilder monitorLocks = new StringBuilder();
		for (int i = 0; i < mi.length; i++) {
			StackTraceElement sfe = mi[i].getLockedStackFrame();
			if (sfe != null) {
				monitorLocks.append("Monitor: ");
				monitorLocks.append(mi[i].getClassName()).append("@")
						.append(Integer.toHexString(mi[i].getIdentityHashCode())).append(" at ");
				monitorLocks.append(sfe.toString()).append("\n");
			}
		}

		LockInfo li[] = ti.getLockedSynchronizers();

		if (li.length == 0) {
			for (int i = 0; i < li.length; i++) {
				monitorLocks.append("Lock: ");
				monitorLocks.append(li[i].getClassName()).append("@")
						.append(Integer.toHexString(li[i].getIdentityHashCode())).append("\n");
			}
		}

		tid.setMonitorLockFrame(monitorLocks.toString());
		tid.setLockOwningThread(ownerId == -1 ? "None" : new Long(ownerId).toString());

		tid.setLockOwner(ti.getLockOwnerName());

		tid.setWaitCount(ti.getWaitedCount());
		tid.setWaitTime(ti.getWaitedTime());

		tid.setInNative(ti.isInNative() ? "Yes" : "No");
		tid.setSuspended(ti.isSuspended() ? "Yes" : "No");
		tid.setState(ti.getThreadState().toString());
		String trace = "";

		for (int i = 0; i < ste.length; i++) {
			String steStr = (i == 0 ? " " : "-");
			trace += "(" + steStr + i + ") - " + ste[i].toString() + "\n";
		}

		tid.setStackTrace(trace);

		return tid;

	}

	private void getJVMProperties() {

		debugMsg("Getting JVM Info");
		systemProperties = "";
		classPath = "";

		// Get an enumerated list of system property keys
		Properties prop = System.getProperties();
		StringBuilder sb = new StringBuilder(STRING_BUFFER_SIZE);
		// loop through each to display key=value
		List<String> propList = new ArrayList<String>();
		for (Enumeration<?> e = prop.propertyNames(); e.hasMoreElements();) {

			String key = e.nextElement().toString();
			String val = System.getProperty(key);
			// What would be the point of encrypting the password if we let them
			// see it
			// in the properties...
			String ent = key + "=" + (key.equals(JVMOPT_PASSWORD) ? "********" : val);
			propList.add(ent);
		}

		Collections.sort(propList);

		for (String pr : propList) {
			sb.append(pr);
			sb.append("\n");
		}

		systemProperties = sb.toString();
		// Get classpath
		// classPath = getURLClasspath();
		classPath = xform.getClassPathURLs();

		debugMsg("JVM Info Obtained");
	}

	private void getEnvProperties() {

		debugMsg("Getting OS Environment Info");
		envProperties = "";

		// Get an enumerated list of system property keys
		Map<String, String> prop = System.getenv();
		StringBuilder sb = new StringBuilder(STRING_BUFFER_SIZE);
		// loop through each to display key=value
		List<String> propList = new ArrayList<String>();
		Set<String> keySet = prop.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {

			String key = iter.next();
			String val = System.getenv(key);
			String ent = key + "=" + val;
			propList.add(ent);
		}

		Collections.sort(propList);

		for (String pr : propList) {
			sb.append(pr);
			sb.append("\n");
		}

		envProperties = sb.toString();

		debugMsg("OS Env Info Obtained");
	}

	public String getURLClasspath() {

		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();

		URL urls[] = sysloader.getURLs();

		StringBuilder sb = new StringBuilder(STRING_BUFFER_SIZE);
		for (int i = 0; i < urls.length; i++) {
			sb.append(urls[i].toString());
			sb.append("\n");
		}

		return sb.toString();
	}

	private OperatingSystemMXBean getSunOSBean() {
		OperatingSystemMXBean osmb = null;

		try {
			ObjectName objectname = new ObjectName("java.lang:type=OperatingSystem");
			if (osmb == null && mbsc.isInstanceOf(objectname, "com.sun.management.OperatingSystemMXBean"))
				osmb = ManagementFactory.newPlatformMXBeanProxy(mbsc, "java.lang:type=OperatingSystem",
						OperatingSystemMXBean.class);
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
			mbean.addException(e);
			return null;
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
			mbean.addException(e);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			mbean.addException(e);
			return null;
		}

		return osmb;
	}

	protected void getClassInfo() {

		try {
			debugMsg("Getting Class Info");
			setPriority(Thread.MIN_PRIORITY);
			loadedClasses = xform.getLoadedClasses();
			loadedPackages = xform.getPackages();
			classLoaders = xform.getClassLoaders();
			numberOfLoadedClasses = xform.getNumberOfLoadedClasses();
			numberOfClassLoaders = xform.getNumberOfClassLoaders();
			numberOfLoadedPackages = xform.getNumberOfPackages();
			debugMsg("JVM Class Obtained");
			setPriority(Thread.MAX_PRIORITY);
		} catch (ConcurrentModificationException e) {
			// TODO: ....
			debugMsg("ConcurrentModificationException Getting Class Info!");
			mbean.addException(e);
		}

	}

	protected void getNativeLibs() {
		if (useOSSystemInfo) {
			nativeLibs = OSSystemInfo.getNativeLibs();
			nativeLibsUpdated = true;
		}
	}

	private String getClassProperties(String className) {
		try {
			ClassLoader loader = xform.getClassLoaderForClass(className);
			if (loader == null) {
				return "Could not Find Classloader for " + className + "\n";
			}
			Class<?> clazz = loader.loadClass(className);
			return ClassFormatter.getClassDefinition(clazz, loader);
		} catch (ClassNotFoundException e) {
			mbean.addException(e);
			return "Class " + className + " was not found\n" + formatException(e, this);
		}

	}

	/**
	 * Get a thread by id
	 * 
	 * @param id
	 *            - Thread id
	 * @return - the thread or null if not found
	 */
	private Thread getThread(long id) {

		Map<Thread, StackTraceElement[]> stackMap = Thread.getAllStackTraces();

		Set<Thread> keys = stackMap.keySet();
		Iterator<Thread> iter = keys.iterator();

		// Loop thru the map
		while (iter.hasNext()) {
			Thread thd = iter.next();
			if (thd.getId() == id)
				return thd;
		}

		return null;

	}

	private ResponseThreadData createThreadData(ThreadInfo ti, long currentTime) {

		if (ti == null) {
			return null;
		}
		long threadCPU = threadBean.getThreadCpuTime(ti.getThreadId());

		ResponseThreadData td = new ResponseThreadData();
		if (ti.getThreadId() == Thread.currentThread().getId()) {
			mbean.setProbeCPUTime(threadCPU);
			td.setProbeThread(true);
		}
		if (ti.getThreadState().equals(State.TERMINATED)) {
			// Don't care about terminated threads
			return null;
		}

		td.setThreadName(ti.getThreadName());
		if (ti.getThreadState().compareTo(Thread.State.BLOCKED) == 0) {
			td.setThreadBlocked(true);
			Utilities.debugMsg("Thread " + ti.getThreadName() + " is Blocked");
		}
		td.setLockName(ti.getLockName());
		td.setLockOwner(ti.getLockOwnerName());
		td.setLockOwningThread(ti.getLockOwnerId());
		Thread thread = getThread(ti.getThreadId());

		td.setDeamon((thread == null ? false : thread.isDaemon()));

		td.setCurrentStackFrame(ti.getStackTrace());
		MonitorInfo[] mi = ti.getLockedMonitors();
		if (mi != null && mi.length > 0) {
			ResponseMonitorInfo[] rmi = new ResponseMonitorInfo[mi.length];
			for (int i = 0; i < mi.length; i++) {
				rmi[i] = new ResponseMonitorInfo(mi[i]);

			}
			td.setMonitorInfo(rmi);
		}
		LockInfo[] li = ti.getLockedSynchronizers();
		if (li != null && li.length > 0) {
			ResponseMonitorInfo[] rmi = new ResponseMonitorInfo[li.length];
			for (int i = 0; i < li.length; i++) {
				rmi[i] = new ResponseMonitorInfo(li[i]);

			}
			td.setLockInfo(rmi);
		}

		td.setThreadId(ti.getThreadId());

		td.setThreadCPU(threadBean.getThreadCpuTime(ti.getThreadId()));
		td.setPercentTotalCPU(((double) ((int) ((td.getThreadCPU() / (double) currentTime) * 10000)) / 100));
		td.setCurrentCPU(td.getThreadCPU() - td.getThreadLastCPU());

		td.setPercentCurrentCPU(
				((int) ((td.getCurrentCPU() / ((double) (currentTime - td.getLastTime())) * 10000)) / 100));

		td.setThreadLastCPU(td.getThreadCPU());
		td.setLastTime(currentTime);
		td.setAllocatedBytes(getThreadAllocatedBytes(ti.getThreadId()));

		return td;

	}

	private void resetSavedProperties() {

		lastJvmOpts = " ";
		lastClasspath = " ";
		lastJavaProperties = " ";
		lastEnvProperties = " ";
		lastLoadedClasses = " ";
		lastLoadedPackages = " ";
		lastClassLoaders = " ";
		debugMsg("Resetting Property Info");
	}

	private boolean interruptThread(long id) {

		debugMsg("Request to Interrupt Thread " + id);
		// Thread t = xform.getThread(id);
		Thread t = getThread(id);

		if (t == null) {
			interruptMsg = "Thread " + id + " is not found";
			debugMsg(interruptMsg);
			return false;
		}

		if (t.isInterrupted()) {
			interruptMsg = "Cannot Interrupt Thread " + id + " already Interrupted";
			debugMsg(interruptMsg);
			return false;
		}
		try {
			t.interrupt();
		} catch (SecurityException e) {
			interruptMsg = "Security Violation Interrupting Thread " + id + "";
			debugMsg(interruptMsg);
			return false;
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	private boolean killThread(long id) {

		debugMsg("Request to Kill Thread " + id);
		// Thread t = xform.getThread(id);
		Thread t = getThread(id);

		if (t == null) {
			interruptMsg = "Thread " + id + " is not found";
			debugMsg(interruptMsg);
			return false;
		}
		JProbeKilledThreadException exc = new JProbeKilledThreadException("Kill Requested by " + server.getClientIP());

		try {
			t.stop(exc);
		} catch (SecurityException e) {
			interruptMsg = "Security Violation Killing Thread " + id + "";
			debugMsg(interruptMsg);
			return false;
		}

		return true;
	}

	private AttributeList inspectAttributes(AttributeList attrl) {

		AttributeList ret = new AttributeList();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;

		try {
			oos = new ObjectOutputStream(baos);
		} catch (IOException e) {
			e.printStackTrace();
			return ret;
		}
		for (int k = 0; k < attrl.size(); k++) {

			Attribute val = (Attribute) attrl.get(k);

			Object o = val.getValue();

			try {
				oos.writeObject(o);
				ret.add(val);
			} catch (IOException e) {
				Attribute newAtt = new Attribute(val.getName(), "<Not Serializable>");
				ret.add(newAtt);
			}

		}
		return ret;

	}

	/**
	 * This method will create a heap dump of the entire JVM for debugging
	 * memory leaks and OutOfMemory Errors. The dump will reside in the working
	 * directory and be in binary format.
	 * 
	 * @return 0 if successful, see dumpHeap*String, boolean) for details
	 */
	public int dumpHeap() {

		String fileName = "java_pid" + getProcessID() + ".hprof";
		int tries = 0;
		File f = new File(fileName);

		while (tries < 100) {

			if (f.exists()) {
				fileName = "java_pid" + getProcessID() + "(" + ++tries + ").hprof";
				f = new File(fileName);
			} else {
				break;
			}
		}

		return dumpHeap(f.getAbsolutePath(), true);

	}

	/**
	 * This method will create a heap dump of the entire JVM for debugging
	 * memory leaks and OutOfMemory Errors.
	 * 
	 * @param fileName
	 *            - File to dump to
	 * @param binary
	 *            - if true, it will be in binary format
	 * @return - 0 if successful. -100 - If the JMX Hotspot diagnostic routine
	 *         is not present (InstanceNotFoundException); -200 - If the
	 *         dumpHeap method signature does not match (ReflectionException)
	 *         -300 - If There is an MBean exception (MBeanException) -400 - If
	 *         the Object bean is invalid (MalformedObjectNameException)
	 */
	public int dumpHeap(String fileName, boolean binary) {

		debugMsg("Request to Dump The Heap to File " + fileName);
		dumpMessage = "Request to Dump The Heap to File " + fileName + " Successful";
		int rc = 0;

		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName containerName = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
			// Build parms
			Object[] parms = new Object[2];
			parms[0] = fileName;
			parms[1] = Boolean.valueOf(binary);
			String[] signatures = new String[2];
			signatures[0] = "java.lang.String";
			signatures[1] = "boolean";
			// Execute the dump
			mbs.invoke(containerName, "dumpHeap", parms, signatures);
		} catch (InstanceNotFoundException e) {
			rc = -100;
			dumpMessage = "ERROR:InstanceNotFoundException: " + e.getMessage();
			mbean.addException(e);
		} catch (ReflectionException e) {
			rc = -200;
			dumpMessage = "ERROR:ReflectionException: " + e.getMessage();
			mbean.addException(e);
		} catch (MBeanException e) {
			rc = -300;
			dumpMessage = "ERROR:MBeanException: " + e.getMessage();
			mbean.addException(e);
		} catch (MalformedObjectNameException e) {
			rc = -400;
			dumpMessage = "ERROR:MalformedObjectException: " + e.getMessage();
			mbean.addException(e);
		} catch (Exception e) {
			dumpMessage = "ERROR:Exception: " + e.getMessage();
			rc = -500;
			mbean.addException(e);
		}

		return rc;
	}

	/**
	 * This method will obtain the process id of the JVM based on the JVM
	 * instance name
	 * 
	 * @return The process id.
	 */
	public int getProcessID() {

		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		String jvmName = runtimeBean.getName();

		int idx = jvmName.indexOf("@");

		return Integer.parseInt(jvmName.substring(0, idx));

	}

	public void finalize() {

		if (server != null) {
			server.disconnect();
		}
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public String getProbeName() {
		return probeName;
	}

	private void getAllocationMethod() {

		if (threadBean == null) {
			return;
		}

		Class<?> beanClass = threadBean.getClass();

		Method[] methods = beanClass.getDeclaredMethods();

		for (Method method : methods) {

			if (method.getName().equals("getThreadAllocatedBytes") && method.getReturnType().getName().equals("long")) {
				getAllocatedBytes = method;
				getAllocatedBytes.setAccessible(true);
			}
		}
		if (getAllocatedBytes == null) {
			debugMsg("Unable to Find getThreadAllocatedBytes method");
		}

	}

	private long getThreadAllocatedBytes(long threadID) {

		if (getAllocatedBytes == null) {
			return -1;
		}
		long ret = -1;
		Long[] inparm = new Long[1];
		inparm[0] = threadID;

		try {
			ret = (Long) getAllocatedBytes.invoke(threadBean, threadID);
		} catch (Exception e) {
			debugMsg("Exception invoking getThreadAllocatedBytes, Exception is " + e.getClass().getName()
					+ ", Error is " + e.getMessage());
			mbean.addException(e);
		}

		return ret;
	}

	public ProbeClassFileTransformer getClassTransformer() {
		return xform;
	}

	public void startProbeRecorder() {

		if (prt != null) {
			if (prt.isAlive()) {
				throw new IllegalStateException("Recorder Thread Already Started");
			}
		}
		prt = new ProbeRecorderThread(this);
		prt.setDaemon(true);
		prt.setName("ProbeRecorderThread");
		prt.start();

	}

	public void stopProbeRecorder() {

		if (prt != null) {
			if (!prt.isAlive()) {
				throw new IllegalStateException("Recorder Thread Is Not Active");
			}
		} else {
			throw new IllegalStateException("Recorder Thread Is Not Active");
		}
		prt.interrupt();

	}

	public void shutdown() {
		shutdown = true;
	}

}

class ProbeUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private Instrumentation inst;
	private String name;
	private static int failureCount = 0;
	private static final int MAX_RETRIES = 3;

	public ProbeUncaughtExceptionHandler(Instrumentation inst, String name) {

		this.inst = inst;
		this.name = name;

	}

	public void uncaughtException(Thread thd, Throwable exc) {

		debugMsg("Uncaught Exception in Probe Agent Thread");
		debugMsg(formatException(exc, thd));

		if (failureCount++ < MAX_RETRIES) {
			System.err.println(VERSION_HEADING + " - Monitor Restart Due To An Uncaught Exception");
			exc.printStackTrace();
			JProbeStub.premain(name, inst);
		} else {
			System.err.println(VERSION_HEADING + " - Monitor Terminated Due To An Uncaught Exception");
		}

	}

}