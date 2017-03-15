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
package edu.regis.jprobe.ui;


import static edu.regis.guitools.GraphStatusBar.SHOW_CPU;
import static edu.regis.guitools.GraphStatusBar.SHOW_HEAP;
import static edu.regis.guitools.GraphStatusBar.SHOW_NON_HEAP;
import static edu.regis.guitools.GraphStatusBar.SHOW_SCROLL;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import edu.regis.jprobe.model.DynamicLibraryManager;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.StackExcludeFilters;


/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UIOptions {

	//
	private transient String fileName;
	private Properties props;
	private Set<OptionChangeListener> changeListeners = new HashSet<OptionChangeListener>();
	//Managed Properties
	private boolean autoUpdate = true;
	private transient boolean showZeroCPU = false;
	private transient boolean cpuIsRelative = false;
	private transient boolean confirmExitIfActive = false;
	private transient boolean filterStack = true;
	private transient boolean showDeltas = true;
	private transient boolean showProbeThread = true;
	private boolean wasSelfElevated = false;
	private transient int updateInterval = 1000;
	private transient int lookAndFeel = 0;
	private transient int parentX = 0;
	private transient int parentY = 0;
	private int parentWidth = 640;
	private int parentHeight = 480;
	private int rowHeightAdjustment = 4;
	private int sampleRate = 100;
	private transient int sortCol = 0;
	private int maxTraceSize = 1000;
	private int socketConnectTimeout = 5000;
	private int socketReadTimeout = 60000;
	private int multicastReadTimeout = 50;
	private int multicastScanInterval = 1000;
	private double textCellHeightOffset = 1.7;
	private double clientRatio = 0.85;
	private double processSplitRatio = 0.55;
	private String lastRemoteHost = "";
	private String lastRemotePort = "32311";
	
	//Panel Display Options
	private transient boolean showOverviewTab = true;
	private transient boolean showCPUHistoryTab = true;
	private transient boolean showThreadCPUTab = true;
	private transient boolean showMemoryHistoryTab = true;
	private transient boolean showMemoryPoolHistoryTab = true;
	private transient boolean showIOCountHistoryTab = true;
	private transient boolean showIOBytesHistoryTab = true;
	private transient boolean showPagingHistoryTab = true;
	private transient boolean showMemoryStatsTab = true;
	private transient boolean showJVMPropertiesTab = true;
	private transient boolean showClassInfoTab = true;
	private transient boolean showJMXBeansTab = true;
	private transient boolean showLocksTab = true;
	private transient boolean showGCTab = true;
	private boolean showConnectWhenEmpty = true;
	private transient boolean sortAsc = true;
	private boolean useProbeForMBean = false;
	private boolean cacheExternalJars = true;
	private transient boolean obtainMonitorInfo = true;
	private transient boolean obtainLockInfo = true;
	private transient int statusBarType = STANDARD_STATUS_BAR;
	private int graphicStatusType = SHOW_HEAP + SHOW_NON_HEAP + SHOW_CPU + SHOW_SCROLL; 

	//Threshold levels and colors
	//CPU
	private transient double cpuThresholdOk = 50;
	private transient double cpuThresholdWarn = 70;
	private transient double cpuThresholdBad = 90;
	private Color cpuColorOk = Color.green;
	private Color cpuColorWarn = Color.yellow;
	private Color cpuColorBad = Color.red;
	//Heap
	private transient double heapThresholdOk = 50;
	private transient double heapThresholdWarn = 70;
	private transient double heapThresholdBad = 90;
	private Color heapColorOk = Color.green;
	private Color heapColorWarn = Color.yellow;
	private Color heapColorBad = Color.red;
	//Non-Heap
	private transient double nonHeapThresholdOk = 50;
	private transient double nonHeapThresholdWarn = 70;
	private transient double nonHeapThresholdBad = 90;
	private Color nonHeapColorOk = Color.green;
	private Color nonHeapColorWarn = Color.yellow;
	private Color nonHeapColorBad = Color.red;
	//GC
	private transient double gcThresholdOk = 50;
	private transient double gcThresholdWarn = 70;
	private transient double gcThresholdBad = 90;
	private Color gcColorOk = Color.green;
	private Color gcColorWarn = Color.yellow;
	private Color gcColorBad = Color.red;
    private Color blockingThreadColor = Color.red;
	private Color daemonThreadColor = Color.blue;
	private Color nonDaemonThreadColor = Color.black;
	
	//ForGraphs
	private Color graphForeground = Color.GREEN;
	private Color graphBackground = Color.BLACK;
	
	private float domainStrokeWidth = 1;
	private int domainStrokeCap = 0;
	private int domainStrokeJoin = 2;
	private float rangeStrokeWidth = 1;
    private int rangeStrokeCap = 0;
    private int rangeStrokeJoin = 2;
    private float baseStrokeWidth = 2;
    private int baseStrokeCap = 1;
    private int baseStrokeJoin = 2;
    private boolean showTickLines = true;
    private boolean showTickLabels = true;
    private boolean startLastLocation = false;
	
	//Constants for Display Options
	public static final int NO_STATUS_BAR = 0;
	public static final int STANDARD_STATUS_BAR = 1;
	public static final int GRAPHICAL_STATUS_BAR = 2;
	private StackExcludeFilters filters;
	private DynamicLibraryManager libManager;
    private File path;
    
    private static final String RECORDING_DIRECTORY = "Recordings";
    public static final String RECORDING_SUFFIX = ".probeRec";
	private static UIOptions options;
	//JVM Exit code
	private int exitCode = 666;
	
	private Frame parentComponent;
	
	public UIOptions() {
		this("JVMProbe.prop");
	}
	
	public UIOptions(String filename) {
		
		 this.fileName = filename;
		 this.path = new File(".");
		 filters = new StackExcludeFilters();
		 filters.restore(null);
		 props = new Properties();
		 File f = new File(fileName);
		 FileInputStream sf = null;
		 FileOutputStream fos = null;
		 
		  try {
			if (!f.exists()) {
			  	fos = new FileOutputStream(f);
				save();
				fos.close();
			  }
		} catch (Exception e) {
		    Logger.getLogger().logException(e,this);
		} 
		  
		 try {
			sf = new FileInputStream(f);
		} catch (FileNotFoundException e) {
		    Logger.getLogger().logException(e,this);
		}
		
		
		 try {
			props.load(sf);
		} catch (IOException e) {
		    Logger.getLogger().logException(e,this);
		}
		
		 String val = null; 
	     
	     try {
			val = props.getProperty("STARTING_X"); //$NON-NLS-1$
			 if (val != null) parentX = Integer.parseInt(val);
			    
			 val = props.getProperty("STARTING_Y"); //$NON-NLS-1$
			 if (val != null) parentY = Integer.parseInt(val);
			 
			 val = props.getProperty("STARTING_WIDTH"); //$NON-NLS-1$
			 if (val != null) parentWidth = Integer.parseInt(val);
			 
			 val = props.getProperty("STARTING_HEIGHT"); //$NON-NLS-1$
			 if (val != null) parentHeight = Integer.parseInt(val);
			 
			 val = props.getProperty("LOOK_AND_FEEL"); //$NON-NLS-1$
			 if (val != null) lookAndFeel = Integer.parseInt(val);
			 
			 val = props.getProperty("UPDATE_INTERVAL"); //$NON-NLS-1$
			 if (val != null) updateInterval = Integer.parseInt(val);
			 
			 val = props.getProperty("ROW_HEIGHT_ADJUSTMENT"); //$NON-NLS-1$
			 if (val != null) rowHeightAdjustment = Integer.parseInt(val);
				   	     
			 val = props.getProperty("AUTO_UPDATE"); //$NON-NLS-1$
			 if (val != null) autoUpdate = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_ZERO_CPU"); //$NON-NLS-1$
			 if (val != null) showZeroCPU = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("CPU_IS_RELATIVE"); //$NON-NLS-1$
			 if (val != null) cpuIsRelative = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("CONFIRM_EXIT"); //$NON-NLS-1$
			 if (val != null) confirmExitIfActive = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_OVERVIEW"); //$NON-NLS-1$
			 if (val != null) showOverviewTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_CPU_HISTORY"); //$NON-NLS-1$
			 if (val != null) showCPUHistoryTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_THREAD_CPU"); //$NON-NLS-1$
			 if (val != null) showThreadCPUTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_MEMORY_HISTORY"); //$NON-NLS-1$
			 if (val != null) showMemoryHistoryTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_MEMORY_POOL"); //$NON-NLS-1$
			 if (val != null) showMemoryPoolHistoryTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_IO_COUNT"); //$NON-NLS-1$
			 if (val != null) showIOCountHistoryTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_IO_BYTES"); //$NON-NLS-1$
			 if (val != null) showIOBytesHistoryTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_PAGING"); //$NON-NLS-1$
			 if (val != null) showPagingHistoryTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_MEMORY_STATS"); //$NON-NLS-1$
			 if (val != null) showMemoryStatsTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_JVM_PROPERTIES"); //$NON-NLS-1$
			 if (val != null) showJVMPropertiesTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_CLASS_INFO"); //$NON-NLS-1$
			 if (val != null) showClassInfoTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_JMX_BEANS"); //$NON-NLS-1$
			 if (val != null) showJMXBeansTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("SHOW_LOCKS"); //$NON-NLS-1$
             if (val != null) showLocksTab = Boolean.parseBoolean(val);
             
             val = props.getProperty("SHOW_GC"); //$NON-NLS-1$
             if (val != null) showGCTab = Boolean.parseBoolean(val);
			 
			 val = props.getProperty("STATUS_BAR_TYPE"); //$NON-NLS-1$
			 if (val != null) statusBarType = Integer.parseInt(val);

			 val = props.getProperty("GRAPHIC_STATUS_BAR_TYPE"); //$NON-NLS-1$
			 if (val != null) graphicStatusType = Integer.parseInt(val);

			 val = props.getProperty("CPU_THRESHOLD_OK"); //$NON-NLS-1$
			 if (val != null) cpuThresholdOk = Double.parseDouble(val);
			 
			 val = props.getProperty("CPU_THRESHOLD_WARN"); //$NON-NLS-1$
			 if (val != null) cpuThresholdWarn = Double.parseDouble(val);
			 
			 val = props.getProperty("CPU_THRESHOLD_BAD"); //$NON-NLS-1$
			 if (val != null) cpuThresholdBad = Double.parseDouble(val);
			 
			 val = props.getProperty("CPU_COLOR_OK"); //$NON-NLS-1$
			 if (val != null) cpuColorOk = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("CPU_COLOR_WARN"); //$NON-NLS-1$
			 if (val != null) cpuColorWarn = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("CPU_COLOR_BAD"); //$NON-NLS-1$
			 if (val != null) cpuColorBad = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("HEAP_THRESHOLD_OK"); //$NON-NLS-1$
			 if (val != null) heapThresholdOk = Double.parseDouble(val);
			 
			 val = props.getProperty("HEAP_THRESHOLD_WARN"); //$NON-NLS-1$
			 if (val != null) heapThresholdWarn = Double.parseDouble(val);
			 
			 val = props.getProperty("HEAP_THRESHOLD_BAD"); //$NON-NLS-1$
			 if (val != null) heapThresholdBad = Double.parseDouble(val);
			 
			 val = props.getProperty("HEAP_COLOR_OK"); //$NON-NLS-1$
			 if (val != null) heapColorOk = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("HEAP_COLOR_WARN"); //$NON-NLS-1$
			 if (val != null) heapColorWarn = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("HEAP_COLOR_BAD"); //$NON-NLS-1$
			 if (val != null) heapColorBad = new Color(Integer.parseInt(val));

			 val = props.getProperty("NONHEAP_THRESHOLD_OK"); //$NON-NLS-1$
			 if (val != null) nonHeapThresholdOk = Double.parseDouble(val);
			 
			 val = props.getProperty("NONHEAP_THRESHOLD_WARN"); //$NON-NLS-1$
			 if (val != null) nonHeapThresholdWarn = Double.parseDouble(val);
			 
			 val = props.getProperty("NONHEAP_THRESHOLD_BAD"); //$NON-NLS-1$
			 if (val != null) nonHeapThresholdBad = Double.parseDouble(val);
			 
			 val = props.getProperty("NONHEAP_COLOR_OK"); //$NON-NLS-1$
			 if (val != null) nonHeapColorOk = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("NONHEAP_COLOR_WARN"); //$NON-NLS-1$
			 if (val != null) nonHeapColorWarn = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("NONHEAP_COLOR_BAD"); //$NON-NLS-1$
			 if (val != null) nonHeapColorBad = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("GC_THRESHOLD_OK"); //$NON-NLS-1$
			 if (val != null) gcThresholdOk = Double.parseDouble(val);
			 
			 val = props.getProperty("GC_THRESHOLD_WARN"); //$NON-NLS-1$
			 if (val != null) gcThresholdWarn = Double.parseDouble(val);
			 
			 val = props.getProperty("GC_THRESHOLD_BAD"); //$NON-NLS-1$
			 if (val != null) gcThresholdBad = Double.parseDouble(val);
			 
			 val = props.getProperty("GC_COLOR_OK"); //$NON-NLS-1$
			 if (val != null) gcColorOk = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("GC_COLOR_WARN"); //$NON-NLS-1$
			 if (val != null) gcColorWarn = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("GC_COLOR_BAD"); //$NON-NLS-1$
			 if (val != null) gcColorBad = new Color(Integer.parseInt(val));
			 
             val = props.getProperty("BLOCKING_THREAD_COLOR"); //$NON-NLS-1$
             if (val != null) blockingThreadColor = new Color(Integer.parseInt(val));
             
             val = props.getProperty("DAEMON_THREAD_COLOR"); //$NON-NLS-1$
             if (val != null) daemonThreadColor = new Color(Integer.parseInt(val));
             
             val = props.getProperty("NON_DAEMON_THREAD_COLOR"); //$NON-NLS-1$
             if (val != null) nonDaemonThreadColor = new Color(Integer.parseInt(val));
			 
			 val = props.getProperty("SAMPLE_RATE"); //$NON-NLS-1$
			 if (val != null) sampleRate = Integer.parseInt(val);
			 
			 val = props.getProperty("EXIT_CODE"); //$NON-NLS-1$
			 if (val != null) exitCode = Integer.parseInt(val);
			 
			 val = props.getProperty("FILTER_STACK"); //$NON-NLS-1$
             if (val != null) filterStack = Boolean.parseBoolean(val);

             val = props.getProperty("SORT_COLUMN"); //$NON-NLS-1$
             if (val != null) sortCol = Integer.parseInt(val);
             
             val = props.getProperty("SORT_ASC"); //$NON-NLS-1$
             if (val != null) sortAsc = Boolean.parseBoolean(val);
             
             val = props.getProperty("SHOW_DELTAS"); //$NON-NLS-1$
             if (val != null) showDeltas = Boolean.parseBoolean(val);
             
             val = props.getProperty("TEXT_CELL_HEIGHT_RATIO"); //$NON-NLS-1$
             if (val != null) textCellHeightOffset = Double.parseDouble(val);
             
             val = props.getProperty("USE_PROBE_FOR_MBEANS"); //$NON-NLS-1$
             if (val != null) useProbeForMBean = Boolean.parseBoolean(val);
             
             val = props.getProperty("SHOW_CONNECT_WHEN_EMPTY"); //$NON-NLS-1$
             if (val != null) showConnectWhenEmpty = Boolean.parseBoolean(val);
             
             val = props.getProperty("MAX_TRACE_SIZE"); //$NON-NLS-1$
             if (val != null) maxTraceSize = Integer.parseInt(val);
             
             val = props.getProperty("SOCKET_CONNECT_TIMEOUT"); //$NON-NLS-1$
             if (val != null) socketConnectTimeout = Integer.parseInt(val);
             
             val = props.getProperty("SOCKET_READ_TIMEOUT"); //$NON-NLS-1$
             if (val != null) socketReadTimeout = Integer.parseInt(val);
             
             val = props.getProperty("OBTAIN_MONITOR_INFO"); //$NON-NLS-1$
             if (val != null) obtainMonitorInfo = Boolean.parseBoolean(val);
             
             val = props.getProperty("OBTAIN_LOCK_INFO"); //$NON-NLS-1$
             if (val != null) obtainLockInfo = Boolean.parseBoolean(val);
             
             val = props.getProperty("SHOW_PROBE_THREAD"); //$NON-NLS-1$
             if (val != null) showProbeThread = Boolean.parseBoolean(val);
             
             val = props.getProperty("CACHE_EXTERNAL_JARS"); //$NON-NLS-1$
             if (val != null) cacheExternalJars = Boolean.parseBoolean(val);
             
             val = props.getProperty("WAS_SELF_ELEVATED"); //$NON-NLS-1$
             if (val != null) wasSelfElevated = Boolean.parseBoolean(val);
             
             val = props.getProperty("CLIENT_RATIO"); //$NON-NLS-1$
             if (val != null) clientRatio = Double.parseDouble(val);
             
             val = props.getProperty("PROCESS_SPLIT_RATIO"); //$NON-NLS-1$
             if (val != null) processSplitRatio = Double.parseDouble(val);
             
             val = props.getProperty("LAST_REMOTE_HOST"); //$NON-NLS-1$
             if (val != null) lastRemoteHost = val;
             
             val = props.getProperty("LAST_REMOTE_PORT"); //$NON-NLS-1$
             if (val != null) lastRemotePort = val;
            
             val = props.getProperty("GRAPH_FOREGROUND"); //$NON-NLS-1$
             if (val != null) graphForeground = new Color(Integer.parseInt(val));
             
             val = props.getProperty("GRAPH_BACKGROUND"); //$NON-NLS-1$
             if (val != null) graphBackground = new Color(Integer.parseInt(val));
             
             val = props.getProperty("DOMAIN_STROKE_WIDTH"); //$NON-NLS-1$
             if (val != null) domainStrokeWidth = Float.parseFloat(val);
             
             val = props.getProperty("DOMAIN_STROKE_CAP"); //$NON-NLS-1$
             if (val != null) domainStrokeCap = Integer.parseInt(val);
             
             val = props.getProperty("DOMAIN_STROKE_JOIN"); //$NON-NLS-1$
             if (val != null) domainStrokeJoin = Integer.parseInt(val);
             
             val = props.getProperty("RANGE_STROKE_WIDTH"); //$NON-NLS-1$
             if (val != null) rangeStrokeWidth = Float.parseFloat(val);
             
             val = props.getProperty("RANGE_STROKE_CAP"); //$NON-NLS-1$
             if (val != null) rangeStrokeCap = Integer.parseInt(val);
             
             val = props.getProperty("RANGE_STROKE_JOIN"); //$NON-NLS-1$
             if (val != null) rangeStrokeJoin = Integer.parseInt(val);
             
             val = props.getProperty("BASE_STROKE_WIDTH"); //$NON-NLS-1$
             if (val != null) baseStrokeWidth = Float.parseFloat(val);
             
             val = props.getProperty("BASE_STROKE_CAP"); //$NON-NLS-1$
             if (val != null) baseStrokeCap = Integer.parseInt(val);
             
             val = props.getProperty("BASE_STROKE_JOIN"); //$NON-NLS-1$
             if (val != null) baseStrokeJoin = Integer.parseInt(val);
             
             val = props.getProperty("SHOW_TICK_LINES"); //$NON-NLS-1$
             if (val != null) showTickLines = Boolean.parseBoolean(val);
             
             val = props.getProperty("SHOW_TICK_LABELS"); //$NON-NLS-1$
             if (val != null) showTickLabels = Boolean.parseBoolean(val);
             
             val = props.getProperty("START_LAST_LOCATION"); //$NON-NLS-1$
             if (val != null) startLastLocation = Boolean.parseBoolean(val);
             
             val = props.getProperty("MULTICAST_READ_TIMEOUT"); //$NON-NLS-1$
             if (val != null) multicastReadTimeout = Integer.parseInt(val);
             
             val = props.getProperty("MULTICAST_SCAN_INTERVAL"); //$NON-NLS-1$
             if (val != null) multicastScanInterval = Integer.parseInt(val);
             
		} catch (NumberFormatException e) {
		    Logger.getLogger().logException(e,this);
		}
	     filters.setEnabled(filterStack);
	     options = this;
	     libManager = new DynamicLibraryManager();
         libManager.restore(null);
	     libManager.loadAllLibraries();
    }
    public void save() {
    	
       
       props.setProperty("STARTING_X", new Integer(parentX).toString());
       props.setProperty("STARTING_Y", new Integer(parentY).toString());
       props.setProperty("STARTING_HEIGHT", new Integer(parentHeight).toString());
       props.setProperty("STARTING_WIDTH", new Integer(parentWidth).toString());
       props.setProperty("LOOK_AND_FEEL", new Integer(lookAndFeel).toString());
       props.setProperty("UPDATE_INTERVAL", new Integer(updateInterval).toString());
       props.setProperty("ROW_HEIGHT_ADJUSTMENT", new Integer(rowHeightAdjustment).toString());
       props.setProperty("AUTO_UPDATE", new Boolean(autoUpdate).toString());
       props.setProperty("SHOW_ZERO_CPU", new Boolean(showZeroCPU).toString());
       props.setProperty("CPU_IS_RELATIVE", new Boolean(cpuIsRelative).toString());
       props.setProperty("CONFIRM_EXIT", new Boolean(confirmExitIfActive).toString());
       props.setProperty("SHOW_OVERVIEW", new Boolean(showOverviewTab).toString());
       props.setProperty("SHOW_CPU_HISTORY", new Boolean(showCPUHistoryTab).toString());
       props.setProperty("SHOW_THREAD_CPU", new Boolean(showThreadCPUTab).toString());
       props.setProperty("SHOW_MEMORY_HISTORY", new Boolean(showMemoryHistoryTab).toString());
       props.setProperty("SHOW_MEMORY_POOL", new Boolean(showMemoryPoolHistoryTab).toString());
       props.setProperty("SHOW_IO_COUNT", new Boolean(showIOCountHistoryTab).toString());
       props.setProperty("SHOW_IO_BYTES", new Boolean(showIOBytesHistoryTab).toString());
       props.setProperty("SHOW_PAGING", new Boolean(showPagingHistoryTab).toString());
       props.setProperty("SHOW_MEMORY_STATS", new Boolean(showMemoryStatsTab).toString());
       props.setProperty("SHOW_JVM_PROPERTIES", new Boolean(showJVMPropertiesTab).toString());
       props.setProperty("SHOW_CLASS_INFO", new Boolean(showClassInfoTab).toString());
       props.setProperty("SHOW_JMX_BEANS", new Boolean(showJMXBeansTab).toString());
       props.setProperty("SHOW_LOCKS", new Boolean(showLocksTab).toString());
       props.setProperty("SHOW_GC", new Boolean(showGCTab).toString());
       props.setProperty("STATUS_BAR_TYPE", new Integer(statusBarType).toString());
       props.setProperty("GRAPHIC_STATUS_BAR_TYPE", new Integer(graphicStatusType).toString());
       props.setProperty("CPU_THRESHOLD_OK", new Double(cpuThresholdOk).toString());
       props.setProperty("CPU_THRESHOLD_WARN", new Double(cpuThresholdWarn).toString());
       props.setProperty("CPU_THRESHOLD_BAD", new Double(cpuThresholdBad).toString());
       props.setProperty("CPU_COLOR_OK", new Integer(cpuColorOk.getRGB()).toString());
       props.setProperty("CPU_COLOR_WARN", new Integer(cpuColorWarn.getRGB()).toString());
       props.setProperty("CPU_COLOR_BAD", new Integer(cpuColorBad.getRGB()).toString());
       props.setProperty("HEAP_THRESHOLD_OK", new Double(heapThresholdOk).toString());
       props.setProperty("HEAP_THRESHOLD_WARN", new Double(heapThresholdWarn).toString());
       props.setProperty("HEAP_THRESHOLD_BAD", new Double(heapThresholdBad).toString());
       props.setProperty("HEAP_COLOR_OK", new Integer(heapColorOk.getRGB()).toString());
       props.setProperty("HEAP_COLOR_WARN", new Integer(heapColorWarn.getRGB()).toString());
       props.setProperty("HEAP_COLOR_BAD", new Integer(heapColorBad.getRGB()).toString());
       props.setProperty("NONHEAP_THRESHOLD_OK", new Double(nonHeapThresholdOk).toString());
       props.setProperty("NONHEAP_THRESHOLD_WARN", new Double(nonHeapThresholdWarn).toString());
       props.setProperty("NONHEAP_THRESHOLD_BAD", new Double(nonHeapThresholdBad).toString());
       props.setProperty("NONHEAP_COLOR_OK", new Integer(nonHeapColorOk.getRGB()).toString());
       props.setProperty("NONHEAP_COLOR_WARN", new Integer(nonHeapColorWarn.getRGB()).toString());
       props.setProperty("NONHEAP_COLOR_BAD", new Integer(nonHeapColorBad.getRGB()).toString());
       props.setProperty("GC_THRESHOLD_OK", new Double(gcThresholdOk).toString());
       props.setProperty("GC_THRESHOLD_WARN", new Double(gcThresholdWarn).toString());
       props.setProperty("GC_THRESHOLD_BAD", new Double(gcThresholdBad).toString());
       props.setProperty("GC_COLOR_OK", new Integer(gcColorOk.getRGB()).toString());
       props.setProperty("GC_COLOR_WARN", new Integer(gcColorWarn.getRGB()).toString());
       props.setProperty("GC_COLOR_BAD", new Integer(gcColorBad.getRGB()).toString());
       props.setProperty("BLOCKING_THREAD_COLOR", new Integer(blockingThreadColor.getRGB()).toString());
       props.setProperty("DAEMON_THREAD_COLOR", new Integer(daemonThreadColor.getRGB()).toString());
       props.setProperty("NON_DAEMON_THREAD_COLOR", new Integer(nonDaemonThreadColor.getRGB()).toString());
       props.setProperty("SAMPLE_RATE", new Integer(sampleRate).toString());
       props.setProperty("EXIT_CODE", new Integer(exitCode).toString());
       props.setProperty("FILTER_STACK", new Boolean(filterStack).toString());
       props.setProperty("SORT_COLUMN", new Integer(sortCol).toString());
       props.setProperty("SORT_ASC", new Boolean(sortAsc).toString());
       props.setProperty("SHOW_DELTAS", new Boolean(showDeltas).toString());
       props.setProperty("TEXT_CELL_HEIGHT_RATIO", new Double(textCellHeightOffset).toString());
       props.setProperty("USE_PROBE_FOR_MBEANS", new Boolean(useProbeForMBean).toString());
       props.setProperty("SHOW_CONNECT_WHEN_EMPTY", new Boolean(showConnectWhenEmpty).toString());
       props.setProperty("MAX_TRACE_SIZE", new Integer(maxTraceSize).toString());
       props.setProperty("SOCKET_CONNECT_TIMEOUT", new Integer(socketConnectTimeout).toString());
       props.setProperty("SOCKET_READ_TIMEOUT", new Integer(socketReadTimeout).toString());
       props.setProperty("OBTAIN_MONITOR_INFO", new Boolean(obtainMonitorInfo).toString());
       props.setProperty("OBTAIN_LOCK_INFO", new Boolean(obtainLockInfo).toString());
       props.setProperty("SHOW_PROBE_THREAD", new Boolean(showProbeThread).toString());
       props.setProperty("CACHE_EXTERNAL_JARS", new Boolean(cacheExternalJars).toString());
       props.setProperty("WAS_SELF_ELEVATED", new Boolean(wasSelfElevated).toString());
       props.setProperty("CLIENT_RATIO", new Double(clientRatio).toString());
       props.setProperty("PROCESS_SPLIT_RATIO", new Double(processSplitRatio).toString());
       props.setProperty("LAST_REMOTE_HOST", lastRemoteHost);
       props.setProperty("LAST_REMOTE_PORT", lastRemotePort);
       
       props.setProperty("GRAPH_FOREGROUND", new Integer(graphForeground.getRGB()).toString());
       props.setProperty("GRAPH_BACKGROUND", new Integer(graphBackground.getRGB()).toString());
      
       props.setProperty("DOMAIN_STROKE_WIDTH", Float.toString(domainStrokeWidth)); 
       props.setProperty("DOMAIN_STROKE_CAP", Integer.toString(domainStrokeCap)); 
       props.setProperty("DOMAIN_STROKE_JOIN", Integer.toString(domainStrokeJoin)); 
       
       props.setProperty("RANGE_STROKE_WIDTH", Float.toString(rangeStrokeWidth)); 
       props.setProperty("RANGE_STROKE_CAP", Integer.toString(rangeStrokeCap)); 
       props.setProperty("RANGE_STROKE_JOIN", Integer.toString(rangeStrokeJoin)); 
       
       props.setProperty("BASE_STROKE_WIDTH", Float.toString(baseStrokeWidth)); 
       props.setProperty("BASE_STROKE_CAP", Integer.toString(baseStrokeCap)); 
       props.setProperty("BASE_STROKE_JOIN", Integer.toString(baseStrokeJoin)); 
      
       props.setProperty("SHOW_TICK_LINES", Boolean.toString(showTickLines)); 
       props.setProperty("SHOW_TICK_LABELS", Boolean.toString(showTickLabels));
       props.setProperty("START_LAST_LOCATION", Boolean.toString(startLastLocation));
       
       props.setProperty("MULTICAST_READ_TIMEOUT", new Integer(multicastReadTimeout).toString());
       props.setProperty("MULTICAST_SCAN_INTERVAL", new Integer(multicastScanInterval).toString());
       
       try {
    		FileOutputStream sf = new FileOutputStream(fileName);
    		props.store(sf, "Java VM Probe Configuration");
    		sf.close();
    	} catch (FileNotFoundException e) {
    		 
    	    Logger.getLogger().logException(e,this);
    	} catch (IOException e) {
    		 
    	    Logger.getLogger().logException(e,this);
    	}
    }
	public void addChangeListener(OptionChangeListener listener) {
	    changeListeners.add(listener);
	}
   public void removeChangeListener(OptionChangeListener listener) {
        changeListeners.remove(listener);
    }
   public void notifyChangeListeners() {
       
       for (OptionChangeListener listener : changeListeners) {
           try {
               listener.onOptionsChange(this);
           } catch (Exception e) {
               Logger.getLogger().logException(e, this);
           }
       }
   }
	/**
	 * @return Returns the autoUpdate.
	 */
	public boolean isAutoUpdate() {
		return autoUpdate;
	}
	/**
	 * @param autoUpdate The autoUpdate to set.
	 */
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}
	/**
	 * @return Returns the cpuIsRelative.
	 */
	public boolean isCpuIsRelative() {
		return cpuIsRelative;
	}
	/**
	 * @param cpuIsRelative The cpuIsRelative to set.
	 */
	public void setCpuIsRelative(boolean cpuIsRelative) {
		this.cpuIsRelative = cpuIsRelative;
	}
	/**
	 * @return Returns the lookAndFeel.
	 */
	public int getLookAndFeel() {
		return lookAndFeel;
	}
	/**
	 * @param lookAndFeel The lookAndFeel to set.
	 */
	public void setLookAndFeel(int lookAndFeel) {
		this.lookAndFeel = lookAndFeel;
	}
	/**
	 * @return Returns the showZeroCPU.
	 */
	public boolean isShowZeroCPU() {
		return showZeroCPU;
	}
	/**
	 * @param showZeroCPU The showZeroCPU to set.
	 */
	public void setShowZeroCPU(boolean showZeroCPU) {
		this.showZeroCPU = showZeroCPU;
	}
	/**
	 * @return Returns the updateInterval.
	 */
	public int getUpdateInterval() {
		return updateInterval;
	}
	
	/**
	 * @param updateInterval The updateInterval to set.
	 */
	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}
	
	/**
	 * @return Returns the parentX.
	 */
	public int getParentX() {
		return parentX;
	}
	/**
	 * @param parentX The parentX to set.
	 */
	public void setParentX(int parentX) {
		this.parentX = parentX;
	}
	/**
	 * @return Returns the parentY.
	 */
	public int getParentY() {
		return parentY;
	}
	/**
	 * @param parentY The parentY to set.
	 */
	public void setParentY(int parentY) {
		this.parentY = parentY;
	}
	
	/**
	 * @return Returns the parentHeight.
	 */
	public int getParentHeight() {
		return parentHeight;
	}
	/**
	 * @param parentHeight The parentHeight to set.
	 */
	public void setParentHeight(int parentHeight) {
		this.parentHeight = parentHeight;
	}
	/**
	 * @return Returns the parentWidthX.
	 */
	public int getParentWidth() {
		return parentWidth;
	}
	/**
	 * @param parentWidth The parentWidthX to set.
	 */
	public void setParentWidth(int parentWidth) {
		this.parentWidth = parentWidth;
	}
	
	/**
	 * @return Returns the parentComponent.
	 */
	public Frame getParentComponent() {
		return parentComponent;
	}
	/**
	 * @param parentComponent The parentComponent to set.
	 */
	public void setParentComponent(Frame parentComponent) {
		this.parentComponent = parentComponent;
	}

	/**
	 * @return the rowHeightAdjustment
	 */
	public int getRowHeightAdjustment() {
		return rowHeightAdjustment;
	}

	/**
	 * @param rowHeightAdjustment the rowHeightAdjustment to set
	 */
	public void setRowHeightAdjustment(int rowHeightAdjustment) {
		this.rowHeightAdjustment = rowHeightAdjustment;
	}

	/**
	 * @return the confirmExitIfActive
	 */
	public boolean isConfirmExitIfActive() {
		return confirmExitIfActive;
	}

	/**
	 * @param confirmExitIfActive the confirmExitIfActive to set
	 */
	public void setConfirmExitIfActive(boolean confirmExitIfActive) {
		this.confirmExitIfActive = confirmExitIfActive;
	}

	/**
	 * @return the showClassInfoTab
	 */
	public boolean isShowClassInfoTab() {
		return showClassInfoTab;
	}

	/**
	 * @param showClassInfoTab the showClassInfoTab to set
	 */
	public void setShowClassInfoTab(boolean showClassInfoTab) {
		this.showClassInfoTab = showClassInfoTab;
	}

	/**
	 * @return the showCPUHistoryTab
	 */
	public boolean isShowCPUHistoryTab() {
		return showCPUHistoryTab;
	}

	/**
	 * @param showCPUHistoryTab the showCPUHistoryTab to set
	 */
	public void setShowCPUHistoryTab(boolean showCPUHistoryTab) {
		this.showCPUHistoryTab = showCPUHistoryTab;
	}

	/**
	 * @return the showIOBytesHistoryTab
	 */
	public boolean isShowIOBytesHistoryTab() {
		return showIOBytesHistoryTab;
	}

	/**
	 * @param showIOBytesHistoryTab the showIOBytesHistoryTab to set
	 */
	public void setShowIOBytesHistoryTab(boolean showIOBytesHistoryTab) {
		this.showIOBytesHistoryTab = showIOBytesHistoryTab;
	}

	/**
	 * @return the showIOCountHistoryTab
	 */
	public boolean isShowIOCountHistoryTab() {
		return showIOCountHistoryTab;
	}

	/**
	 * @param showIOCountHistoryTab the showIOCountHistoryTab to set
	 */
	public void setShowIOCountHistoryTab(boolean showIOCountHistoryTab) {
		this.showIOCountHistoryTab = showIOCountHistoryTab;
	}

	/**
	 * @return the showJMXBeansTab
	 */
	public boolean isShowJMXBeansTab() {
		return showJMXBeansTab;
	}

	/**
	 * @param showJMXBeansTab the showJMXBeansTab to set
	 */
	public void setShowJMXBeansTab(boolean showJMXBeansTab) {
		this.showJMXBeansTab = showJMXBeansTab;
	}

	/**
     * @return the showLocksTab
     */
    public boolean isShowLocksTab() {
        return showLocksTab;
    }

    /**
     * @param showLocksTab the showLocksTab to set
     */
    public void setShowLocksTab(boolean showLocksTab) {
        this.showLocksTab = showLocksTab;
    }

    public boolean isShowGCTab() {
        return showGCTab;
    }

    public void setShowGCTab(boolean showGCTab) {
        this.showGCTab = showGCTab;
    }

    /**
	 * @return the showJVMPropertiesTab
	 */
	public boolean isShowJVMPropertiesTab() {
		return showJVMPropertiesTab;
	}

	/**
	 * @param showJVMPropertiesTab the showJVMPropertiesTab to set
	 */
	public void setShowJVMPropertiesTab(boolean showJVMPropertiesTab) {
		this.showJVMPropertiesTab = showJVMPropertiesTab;
	}

	/**
	 * @return the showMemoryHistoryTab
	 */
	public boolean isShowMemoryHistoryTab() {
		return showMemoryHistoryTab;
	}

	/**
	 * @param showMemoryHistoryTab the showMemoryHistoryTab to set
	 */
	public void setShowMemoryHistoryTab(boolean showMemoryHistoryTab) {
		this.showMemoryHistoryTab = showMemoryHistoryTab;
	}

	/**
	 * @return the showMemoryPoolHistoryTab
	 */
	public boolean isShowMemoryPoolHistoryTab() {
		return showMemoryPoolHistoryTab;
	}

	/**
	 * @param showMemoryPoolHistoryTab the showMemoryPoolHistoryTab to set
	 */
	public void setShowMemoryPoolHistoryTab(boolean showMemoryPoolHistoryTab) {
		this.showMemoryPoolHistoryTab = showMemoryPoolHistoryTab;
	}

	/**
	 * @return the showMemoryStatsTab
	 */
	public boolean isShowMemoryStatsTab() {
		return showMemoryStatsTab;
	}

	/**
	 * @param showMemoryStatsTab the showMemoryStatsTab to set
	 */
	public void setShowMemoryStatsTab(boolean showMemoryStatsTab) {
		this.showMemoryStatsTab = showMemoryStatsTab;
	}

	/**
	 * @return the showOverviewTab
	 */
	public boolean isShowOverviewTab() {
		return showOverviewTab;
	}

	/**
	 * @param showOverviewTab the showOverviewTab to set
	 */
	public void setShowOverviewTab(boolean showOverviewTab) {
		this.showOverviewTab = showOverviewTab;
	}

	/**
	 * @return the showPagingHistoryTab
	 */
	public boolean isShowPagingHistoryTab() {
		return showPagingHistoryTab;
	}

	/**
	 * @param showPagingHistoryTab the showPagingHistoryTab to set
	 */
	public void setShowPagingHistoryTab(boolean showPagingHistoryTab) {
		this.showPagingHistoryTab = showPagingHistoryTab;
	}

	/**
	 * @return the showThreadCPUTab
	 */
	public boolean isShowThreadCPUTab() {
		return showThreadCPUTab;
	}

	/**
	 * @param showThreadCPUTab the showThreadCPUTab to set
	 */
	public void setShowThreadCPUTab(boolean showThreadCPUTab) {
		this.showThreadCPUTab = showThreadCPUTab;
	}

	/**
	 * @return the statusBarType
	 */
	public int getStatusBarType() {
		return statusBarType;
	}

	/**
	 * @param statusBarType the statusBarType to set
	 */
	public void setStatusBarType(int statusBarType) {
		this.statusBarType = statusBarType;
	}

	/**
	 * @return the graphicStatusType
	 */
	public int getGraphicStatusType() {
		return graphicStatusType;
	}

	/**
	 * @param graphicStatusType the graphicStatusType to set
	 */
	public void setGraphicStatusType(int graphicStatusType) {
		this.graphicStatusType = graphicStatusType;
	}

	/**
	 * @return the cpuColorBad
	 */
	public Color getCpuColorBad() {
		return cpuColorBad;
	}

	/**
	 * @param cpuColorBad the cpuColorBad to set
	 */
	public void setCpuColorBad(Color cpuColorBad) {
		this.cpuColorBad = cpuColorBad;
	}

	/**
	 * @return the cpuColorOk
	 */
	public Color getCpuColorOk() {
		return cpuColorOk;
	}

	/**
	 * @param cpuColorOk the cpuColorOk to set
	 */
	public void setCpuColorOk(Color cpuColorOk) {
		this.cpuColorOk = cpuColorOk;
	}

	/**
	 * @return the cpuColorWarn
	 */
	public Color getCpuColorWarn() {
		return cpuColorWarn;
	}

	/**
	 * @param cpuColorWarn the cpuColorWarn to set
	 */
	public void setCpuColorWarn(Color cpuColorWarn) {
		this.cpuColorWarn = cpuColorWarn;
	}

	/**
	 * @return the cpuThresholdBad
	 */
	public double getCpuThresholdBad() {
		return cpuThresholdBad;
	}

	/**
	 * @param cpuThresholdBad the cpuThresholdBad to set
	 */
	public void setCpuThresholdBad(double cpuThresholdBad) {
		this.cpuThresholdBad = cpuThresholdBad;
	}

	/**
	 * @return the cpuThresholdOk
	 */
	public double getCpuThresholdOk() {
		return cpuThresholdOk;
	}

	/**
	 * @param cpuThresholdOk the cpuThresholdOk to set
	 */
	public void setCpuThresholdOk(double cpuThresholdOk) {
		this.cpuThresholdOk = cpuThresholdOk;
	}

	/**
	 * @return the cpuThresholdWarn
	 */
	public double getCpuThresholdWarn() {
		return cpuThresholdWarn;
	}

	/**
	 * @param cpuThresholdWarn the cpuThresholdWarn to set
	 */
	public void setCpuThresholdWarn(double cpuThresholdWarn) {
		this.cpuThresholdWarn = cpuThresholdWarn;
	}

	/**
	 * @return the gcColorBad
	 */
	public Color getGcColorBad() {
		return gcColorBad;
	}

	/**
	 * @param gcColorBad the gcColorBad to set
	 */
	public void setGcColorBad(Color gcColorBad) {
		this.gcColorBad = gcColorBad;
	}

	/**
	 * @return the gcColorOk
	 */
	public Color getGcColorOk() {
		return gcColorOk;
	}

	/**
	 * @param gcColorOk the gcColorOk to set
	 */
	public void setGcColorOk(Color gcColorOk) {
		this.gcColorOk = gcColorOk;
	}

	/**
	 * @return the gcColorWarn
	 */
	public Color getGcColorWarn() {
		return gcColorWarn;
	}

	/**
	 * @param gcColorWarn the gcColorWarn to set
	 */
	public void setGcColorWarn(Color gcColorWarn) {
		this.gcColorWarn = gcColorWarn;
	}

	/**
	 * @return the gcThresholdBad
	 */
	public double getGcThresholdBad() {
		return gcThresholdBad;
	}

	/**
	 * @param gcThresholdBad the gcThresholdBad to set
	 */
	public void setGcThresholdBad(double gcThresholdBad) {
		this.gcThresholdBad = gcThresholdBad;
	}

	/**
	 * @return the gcThresholdOk
	 */
	public double getGcThresholdOk() {
		return gcThresholdOk;
	}

	/**
	 * @param gcThresholdOk the gcThresholdOk to set
	 */
	public void setGcThresholdOk(double gcThresholdOk) {
		this.gcThresholdOk = gcThresholdOk;
	}

	/**
	 * @return the gcThresholdWarn
	 */
	public double getGcThresholdWarn() {
		return gcThresholdWarn;
	}

	/**
	 * @param gcThresholdWarn the gcThresholdWarn to set
	 */
	public void setGcThresholdWarn(double gcThresholdWarn) {
		this.gcThresholdWarn = gcThresholdWarn;
	}

	/**
	 * @return the heapColorBad
	 */
	public Color getHeapColorBad() {
		return heapColorBad;
	}

	/**
	 * @param heapColorBad the heapColorBad to set
	 */
	public void setHeapColorBad(Color heapColorBad) {
		this.heapColorBad = heapColorBad;
	}

	/**
	 * @return the heapColorOk
	 */
	public Color getHeapColorOk() {
		return heapColorOk;
	}

	/**
	 * @param heapColorOk the heapColorOk to set
	 */
	public void setHeapColorOk(Color heapColorOk) {
		this.heapColorOk = heapColorOk;
	}

	/**
	 * @return the heapColorWarn
	 */
	public Color getHeapColorWarn() {
		return heapColorWarn;
	}

	/**
	 * @param heapColorWarn the heapColorWarn to set
	 */
	public void setHeapColorWarn(Color heapColorWarn) {
		this.heapColorWarn = heapColorWarn;
	}

	/**
	 * @return the heapThresholdBad
	 */
	public double getHeapThresholdBad() {
		return heapThresholdBad;
	}

	/**
	 * @param heapThresholdBad the heapThresholdBad to set
	 */
	public void setHeapThresholdBad(double heapThresholdBad) {
		this.heapThresholdBad = heapThresholdBad;
	}

	/**
	 * @return the heapThresholdOk
	 */
	public double getHeapThresholdOk() {
		return heapThresholdOk;
	}

	/**
	 * @param heapThresholdOk the heapThresholdOk to set
	 */
	public void setHeapThresholdOk(double heapThresholdOk) {
		this.heapThresholdOk = heapThresholdOk;
	}

	/**
	 * @return the heapThresholdWarn
	 */
	public double getHeapThresholdWarn() {
		return heapThresholdWarn;
	}

	/**
	 * @param heapThresholdWarn the heapThresholdWarn to set
	 */
	public void setHeapThresholdWarn(double heapThresholdWarn) {
		this.heapThresholdWarn = heapThresholdWarn;
	}

	/**
	 * @return the nonHeapColorBad
	 */
	public Color getNonHeapColorBad() {
		return nonHeapColorBad;
	}

	/**
	 * @param nonHeapColorBad the nonHeapColorBad to set
	 */
	public void setNonHeapColorBad(Color nonHeapColorBad) {
		this.nonHeapColorBad = nonHeapColorBad;
	}

	/**
	 * @return the nonHeapColorOk
	 */
	public Color getNonHeapColorOk() {
		return nonHeapColorOk;
	}

	/**
	 * @param nonHeapColorOk the nonHeapColorOk to set
	 */
	public void setNonHeapColorOk(Color nonHeapColorOk) {
		this.nonHeapColorOk = nonHeapColorOk;
	}

	/**
	 * @return the nonHeapColorWarn
	 */
	public Color getNonHeapColorWarn() {
		return nonHeapColorWarn;
	}

	/**
	 * @param nonHeapColorWarn the nonHeapColorWarn to set
	 */
	public void setNonHeapColorWarn(Color nonHeapColorWarn) {
		this.nonHeapColorWarn = nonHeapColorWarn;
	}

	/**
	 * @return the nonHeapThresholdBad
	 */
	public double getNonHeapThresholdBad() {
		return nonHeapThresholdBad;
	}

	/**
	 * @param nonHeapThresholdBad the nonHeapThresholdBad to set
	 */
	public void setNonHeapThresholdBad(double nonHeapThresholdBad) {
		this.nonHeapThresholdBad = nonHeapThresholdBad;
	}

	/**
	 * @return the nonHeapThresholdOk
	 */
	public double getNonHeapThresholdOk() {
		return nonHeapThresholdOk;
	}

	/**
	 * @param nonHeapThresholdOk the nonHeapThresholdOk to set
	 */
	public void setNonHeapThresholdOk(double nonHeapThresholdOk) {
		this.nonHeapThresholdOk = nonHeapThresholdOk;
	}

	/**
	 * @return the nonHeapThresholdWarn
	 */
	public double getNonHeapThresholdWarn() {
		return nonHeapThresholdWarn;
	}

	/**
	 * @param nonHeapThresholdWarn the nonHeapThresholdWarn to set
	 */
	public void setNonHeapThresholdWarn(double nonHeapThresholdWarn) {
		this.nonHeapThresholdWarn = nonHeapThresholdWarn;
	}

	/**
	 * @return the sampleRate
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * @param sampleRate the sampleRate to set
	 */
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
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

    public boolean isFilterStack() {
        return filterStack;
    }

    public void setFilterStack(boolean filterStack) {
        this.filterStack = filterStack;
    }

    public StackExcludeFilters getFilters() {
        return filters;
    }
    public DynamicLibraryManager getLibraryManager() {
        return libManager;
    }
    
    public String getRecordingDirectory() {
        
        return path.getAbsolutePath() + File.separator + RECORDING_DIRECTORY + File.separator;
    }

    public int getSortCol() {
        return sortCol;
    }

    public void setSortCol(int sortCol) {
        this.sortCol = sortCol;
    }

    public boolean isSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    public Color getBlockingThreadColor() {
        return blockingThreadColor;
    }

    public void setBlockingThreadColor(Color blockingThreadColor) {
        this.blockingThreadColor = blockingThreadColor;
    }

    public Color getDaemonThreadColor() {
        return daemonThreadColor;
    }

    public void setDaemonThreadColor(Color daemonThreadColor) {
        this.daemonThreadColor = daemonThreadColor;
    }

    public Color getNonDaemonThreadColor() {
        return nonDaemonThreadColor;
    }

    public void setNonDaemonThreadColor(Color nonDaemonThreadColor) {
        this.nonDaemonThreadColor = nonDaemonThreadColor;
    }

    /**
     * @return the showDeltas
     */
    public boolean isShowDeltas() {
        return showDeltas;
    }

    /**
     * @param showDeltas the showDeltas to set
     */
    public void setShowDeltas(boolean showDeltas) {
        this.showDeltas = showDeltas;
    }

    /**
     * @return the textCellHeightOffset
     */
    public double getTextCellHeightOffset() {
        return textCellHeightOffset;
    }

    /**
     * @param textCellHeightOffset the textCellHeightOffset to set
     */
    public void setTextCellHeightOffset(double textCellHeightOffset) {
        this.textCellHeightOffset = textCellHeightOffset;
    }
    
    /**
     * @return the useProbeForMBean
     */
    public final boolean isUseProbeForMBean() {
        return useProbeForMBean;
    }

    /**
     * @param useProbeForMBean the useProbeForMBean to set
     */
    public final void setUseProbeForMBean(boolean useProbeForMBean) {
        this.useProbeForMBean = useProbeForMBean;
    }

    /**
     * @return the showConnectWhenEmpty
     */
    public final boolean isShowConnectWhenEmpty() {
        return showConnectWhenEmpty;
    }

    /**
     * @return the maxTraceSize
     */
    public final int getMaxTraceSize() {
        return maxTraceSize;
    }

    /**
     * @param maxTraceSize the maxTraceSize to set
     */
    public final void setMaxTraceSize(int maxTraceSize) {
        this.maxTraceSize = maxTraceSize;
    }

    /**
     * @param showConnectWhenEmpty the showConnectWhenEmpty to set
     */
    public final void setShowConnectWhenEmpty(boolean showConnectWhenEmpty) {
        this.showConnectWhenEmpty = showConnectWhenEmpty;
    }

    /**
     * @return the socketConnectTimeout
     */
    public final int getSocketConnectTimeout() {
        return socketConnectTimeout;
    }

    /**
     * @param socketConnectTimeout the socketConnectTimeout to set
     */
    public final void setSocketConnectTimeout(int socketConnectTimeout) {
        this.socketConnectTimeout = socketConnectTimeout;
    }

    /**
     * @return the socketReadTimeout
     */
    public final int getSocketReadTimeout() {
        return socketReadTimeout;
    }

    /**
     * @param socketReadTimeout the socketReadTimeout to set
     */
    public final void setSocketReadTimeout(int socketReadTimeout) {
        this.socketReadTimeout = socketReadTimeout;
    }

    /**
     * @return the obtainMonitorInfo
     */
    public final boolean isObtainMonitorInfo() {
        return obtainMonitorInfo;
    }

    /**
     * @param obtainMonitorInfo the obtainMonitorInfo to set
     */
    public final void setObtainMonitorInfo(boolean obtainMonitorInfo) {
        this.obtainMonitorInfo = obtainMonitorInfo;
    }

    /**
     * @return the obtainLockInfo
     */
    public final boolean isObtainLockInfo() {
        return obtainLockInfo;
    }

    /**
     * @param obtainLockInfo the obtainLockInfo to set
     */
    public final void setObtainLockInfo(boolean obtainLockInfo) {
        this.obtainLockInfo = obtainLockInfo;
    }

    /**
     * @return the showProbeThread
     */
    public final boolean isShowProbeThread() {
        return showProbeThread;
    }

    /**
     * @param showProbeThread the showProbeThread to set
     */
    public final void setShowProbeThread(boolean showProbeThread) {
        this.showProbeThread = showProbeThread;
    }

    /**
     * @return the cacheExternalJars
     */
    public final boolean isCacheExternalJars() {
        return cacheExternalJars;
    }

    /**
     * @param cacheExternalJars the cacheExternalJars to set
     */
    public final void setCacheExternalJars(boolean cacheExternalJars) {
        this.cacheExternalJars = cacheExternalJars;
    }

    public static UIOptions getOptions() {
        return options;
    }

    /**
     * @return the wasSelfElevated
     */
    public final boolean wasSelfElevated() {
        return wasSelfElevated;
    }

    /**
     * @param wasSelfElevated the wasSelfElevated to set
     */
    public final void setWasSelfElevated(boolean wasSelfElevated) {
        this.wasSelfElevated = wasSelfElevated;
    }

    /**
     * @return the clientRatio
     */
    public final double getClientRatio() {
        return clientRatio;
    }

    /**
     * @param clientRatio the clientRatio to set
     */
    public final void setClientRatio(double clientRatio) {
        this.clientRatio = clientRatio;
    }

    /**
     * @return the processSplitRatio
     */
    public final double getProcessSplitRatio() {
        return processSplitRatio;
    }

    /**
     * @param processSplitRatio the processSplitRatio to set
     */
    public final void setProcessSplitRatio(double processSplitRatio) {
        this.processSplitRatio = processSplitRatio;
    }

    /**
     * @return the lastRemoteHost
     */
    public String getLastRemoteHost() {
        return lastRemoteHost;
    }

    /**
     * @param lastRemoteHost the lastRemoteHost to set
     */
    public void setLastRemoteHost(String lastRemoteHost) {
        this.lastRemoteHost = lastRemoteHost;
    }

    /**
     * @return the lastRemotePort
     */
    public String getLastRemotePort() {
        return lastRemotePort;
    }

    /**
     * @param lastRemotePort the lastRemotePort to set
     */
    public void setLastRemotePort(String lastRemotePort) {
        this.lastRemotePort = lastRemotePort;
    }

    /**
     * @return the graphForeground
     */
    public Color getGraphForeground() {
        return graphForeground;
    }

    /**
     * @param graphForeground the graphForeground to set
     */
    public void setGraphForeground(Color graphForeground) {
        this.graphForeground = graphForeground;
    }

    /**
     * @return the graphBackground
     */
    public Color getGraphBackground() {
        return graphBackground;
    }

    /**
     * @param graphBackground the graphBackground to set
     */
    public void setGraphBackground(Color graphBackground) {
        this.graphBackground = graphBackground;
    }

    /**
     * @return the domainStrokeWidth
     */
    public float getDomainStrokeWidth() {
        return domainStrokeWidth;
    }

    /**
     * @param domainStrokeWidth the domainStrokeWidth to set
     */
    public void setDomainStrokeWidth(float domainStrokeWidth) {
        if (domainStrokeWidth > 0) {
            this.domainStrokeWidth = domainStrokeWidth;
        }
    }

    /**
     * @return the domainStrokeCap
     */
    public int getDomainStrokeCap() {
        return domainStrokeCap;
    }

    /**
     * @param domainStrokeCap the domainStrokeCap to set
     */
    public void setDomainStrokeCap(int domainStrokeCap) {
        if (domainStrokeCap >= BasicStroke.CAP_BUTT && domainStrokeCap <= BasicStroke.CAP_SQUARE) {
            this.domainStrokeCap = domainStrokeCap;
        }
    }

    /**
     * @return the domainStrokeJoin
     */
    public int getDomainStrokeJoin() {
        return domainStrokeJoin;
    }

    /**
     * @param domainStrokeJoin the domainStrokeJoin to set
     */
    public void setDomainStrokeJoin(int domainStrokeJoin) {
        if (domainStrokeJoin >= BasicStroke.JOIN_MITER && domainStrokeJoin <= BasicStroke.JOIN_BEVEL) {
            this.domainStrokeJoin = domainStrokeJoin;
        }
    }

    /**
     * @return the rangeStrokeWidth
     */
    public float getRangeStrokeWidth() {
        return rangeStrokeWidth;
    }

    /**
     * @param rangeStrokeWidth the rangeStrokeWidth to set
     */
    public void setRangeStrokeWidth(float rangeStrokeWidth) {
        if (rangeStrokeWidth > 0) {
            this.rangeStrokeWidth = rangeStrokeWidth;
        }
    }

    /**
     * @return the rangeStrokeCap
     */
    public int getRangeStrokeCap() {
        return rangeStrokeCap;
    }

    /**
     * @param rangeStrokeCap the rangeStrokeCap to set
     */
    public void setRangeStrokeCap(int rangeStrokeCap) {
        
        if (rangeStrokeCap >= BasicStroke.CAP_BUTT && rangeStrokeCap <= BasicStroke.CAP_SQUARE) {
            this.rangeStrokeCap = rangeStrokeCap;
        }
    }

    /**
     * @return the rangeStrokeJoin
     */
    public int getRangeStrokeJoin() {
        return rangeStrokeJoin;
    }

    /**
     * @param rangeStrokeJoin the rangeStrokeJoin to set
     */
    public void setRangeStrokeJoin(int rangeStrokeJoin) {
        
        if (rangeStrokeJoin >= BasicStroke.JOIN_MITER && rangeStrokeJoin <= BasicStroke.JOIN_BEVEL) {
            this.rangeStrokeJoin = rangeStrokeJoin;
        }
    }

    /**
     * @return the baseStrokeWidth
     */
    public float getBaseStrokeWidth() {
        return baseStrokeWidth;
    }

    /**
     * @param baseStrokeWidth the baseStrokeWidth to set
     */
    public void setBaseStrokeWidth(float baseStrokeWidth) {
        if (baseStrokeWidth > 0) {
            this.baseStrokeWidth = baseStrokeWidth;
        }
    }

    /**
     * @return the baseStrokeCap
     */
    public int getBaseStrokeCap() {
        return baseStrokeCap;
    }

    /**
     * @param baseStrokeCap the baseStrokeCap to set
     */
    public void setBaseStrokeCap(int baseStrokeCap) {
        if (baseStrokeCap >= BasicStroke.CAP_BUTT && baseStrokeCap <= BasicStroke.CAP_SQUARE) {
            this.baseStrokeCap = baseStrokeCap;
        }
    }

    /**
     * @return the baseStrokeJoin
     */
    public int getBaseStrokeJoin() {
        return baseStrokeJoin;
    }

    /**
     * @param baseStrokeJoin the baseStrokeJoin to set
     */
    public void setBaseStrokeJoin(int baseStrokeJoin) {
        if (baseStrokeJoin >= BasicStroke.JOIN_MITER && baseStrokeJoin <= BasicStroke.JOIN_BEVEL) {
            this.baseStrokeJoin = baseStrokeJoin;
        }
    }

    /**
     * @return the showTickLines
     */
    public boolean isShowTickLines() {
        return showTickLines;
    }

    /**
     * @param showTickLines the showTickLines to set
     */
    public void setShowTickLines(boolean showTickLines) {
        this.showTickLines = showTickLines;
    }

    /**
     * @return the showTickLabels
     */
    public boolean isShowTickLabels() {
        return showTickLabels;
    }

    /**
     * @param showTickLabels the showTickLabels to set
     */
    public void setShowTickLabels(boolean showTickLabels) {
        this.showTickLabels = showTickLabels;
    }

    /**
     * @return the startLastLocation
     */
    public boolean isStartLastLocation() {
        return startLastLocation;
    }

    /**
     * @param startLastLocation the startLastLocation to set
     */
    public void setStartLastLocation(boolean startLastLocation) {
        this.startLastLocation = startLastLocation;
    }

    public int getMulticastReadTimeout() {
        return multicastReadTimeout;
    }

    public void setMulticastReadTimeout(int multicastReadTimeout) {
        this.multicastReadTimeout = multicastReadTimeout;
    }

    public int getMulticastScanInterval() {
        return multicastScanInterval;
    }

    public void setMulticastScanInterval(int multicastScanInterval) {
        this.multicastScanInterval = multicastScanInterval;
    }
	
}
