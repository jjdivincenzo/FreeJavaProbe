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

import static edu.regis.jprobe.model.Utilities.format;
import static edu.regis.jprobe.model.Utilities.formatElapsedTime;
import static edu.regis.jprobe.model.Utilities.getBitMask;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.regis.jprobe.model.JavaVersion;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.ProbeCommunicationsManager;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.Utilities;
import edu.regis.jprobe.ui.helpers.GridBagLayoutHelper;
import edu.regis.jprobe.ui.helpers.JLabelHelper;
import edu.regis.jprobe.ui.helpers.JProgressBarHelper;
import edu.regis.jprobe.ui.helpers.JTextFieldHelper;

/**
 * @author jdivince
 *
 * This is the Overview Panel.
 */
public class OverviewPanel extends PerformancePanel implements IPerformancePanel {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JTextField totalTime;
	private JTextField totalCPUTime;
	private JTextField currentHeapSize;
	private JTextField currentNonHeapSize;
	private JTextField maxHeapSize;
	private JTextField maxNonHeapSize;
	private JTextField totalGCs;
	private JTextField totalGCTime;
	private JTextField totalClassesLoaded;
	private JTextField averageCPU;
	private JTextField totalThreads;
	private JTextField totalDaemonThreads;
	private JTextField totalIOCount;
	private JTextField totalIOBytes;
	private JTextField totalCPUs;
	private JTextField cpuAffinity;
	private JTextField blockingThreads;
	private JTextField numObjPendingFinal;
	private JTextField hwmObjPendingFinal;
	private JLabel loadingPrompt;
	private JProgressBar gcCpuRatio;
	private JProgressBar currentHeapUsage;
	private JProgressBar currentNonHeapUsage;
	private JProgressBar currentCPUUsage;
	private JProgressBar systemCPUUsage;
	private JProgressBar currentPercentBlocking;
	private JProgressBar currentGcCpuRatio;
	private JTable threadTable;
	private ThreadDataModel tableModel;
	
	private long startTime = 0;
	private long endTime = 0;
	private long lastCPUTime = 0;
	private long lastGCTime = 0;
	private long lastWCTime = 0;
	private double cpuPercent = 0;
	private double avecpuPercent = 0;
	private double heapUsagePct =0;
	private double nonHeapUsagePct = 0;
	private long lastCPUCount = 0;
	private long lastAffinity = 0;
	private long lastHeapUsed = 0;
	private long lastNonHeapUsed = 0;
	private long lastCPUUsed = 0;
	private long lastGCCount = 0;
	private long lastIOCount = 0;
	private long lastIOBytes = 0;
	
	private String jvmName = "";
	private JProbeClientFrame ui;
	private UIOptions options;
	private ProbeCommunicationsManager pcm;
	private boolean panelBuilt = false;
	private boolean isJavaVersion8orHigher = false;
	private String javaVersion = "1.7.0";
	private ColumnListener colListener;
		
	protected Dimension buttonSize;
	protected Dimension headingSize;
	public Dimension largeTextSize;
	
	private static final double LABEL_WEIGHT = 0.1;
	private static final double VALUE_WEIGHT = 0.9;
	private static final double LABEL_WEIGHTP2 = 0.1;
	private static final double VALUE_WEIGHTP2 = 0.9;
	public static final long NANOS_PER_MILLI = 1000000;
	public static final long NANOS_PER_SECOND = NANOS_PER_MILLI * 1000;
	
	public OverviewPanel(JProbeClientFrame u, UIOptions options, ProbeCommunicationsManager pcm) {
		
		buttonSize = new Dimension(30,40);
		headingSize = new Dimension(50,30);
		largeTextSize = new Dimension(100,30);
		
		ui = u;
		this.pcm = pcm;
		this.options = options;
		startTime = System.nanoTime();
		JLabelHelper jlh = new JLabelHelper(JLabel.CENTER, null, Color.blue, null, 
				new Font("Courier New",Font.BOLD,24));
		
		loadingPrompt = jlh.newLabel("Loading, Please Wait...");
		add(loadingPrompt);
	}
	
	public void buildPanel(ProbeResponse res) {
		
		remove(loadingPrompt);
		jvmName = res.getJvmName();
		JavaVersion version = new JavaVersion(res.getJavaVersion());
        isJavaVersion8orHigher = (version.compareTo(JavaVersion.java1_8) >= 0);
		javaVersion = res.getJavaVersion();
		
		tableModel = new ThreadDataModel(startTime, options);
		
		// Resource Consumption panel
		JPanel p2 = createResourcePanel(res.isIoCountersAvailable());

 		// Thread Usage panel
		JPanel p3 = createThreadUsagePanel();
 		// Panel for our action buttons
		JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, p2, p3);
		sp.setOneTouchExpandable(true);
		this.add(sp);
		setLayout(new GridLayout(1,1,1,1));
		panelBuilt = true;
		
		validate();
		repaint();
	}
	public JPanel createResourcePanel(boolean showOSInfo) {
		
	    Color lblColor = Color.GRAY;
	    
		JPanel p2 = new JPanel();
 		p2.setLayout(new  GridBagLayout());
 		p2.setBorder(new TitledBorder( 
 		        new EtchedBorder(), "JVM Resource Consumption(" + 
 		                            jvmName + " " + javaVersion + ")"));
 		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(1,1,1,1);
		c2.fill = GridBagConstraints.BOTH;
		GridBagLayoutHelper helper = new GridBagLayoutHelper(p2, c2, 0.0, 0.0);
		JLabelHelper jlh = new JLabelHelper(JLabel.RIGHT, headingSize, lblColor, null, null);
		JProgressBarHelper jpbh = new JProgressBarHelper();
		JTextFieldHelper jtfh = new JTextFieldHelper(false, largeTextSize, null);
		jpbh.setDefaultPreferredSize(largeTextSize);
	
		JLabel l55 = jlh.newLabel("Current CPU Usage (%)");
		helper.addColumn(l55, 1, 1, LABEL_WEIGHTP2, 1.0);
		currentCPUUsage = jpbh.newProgressBar("This is the Percentage of CPU utilization");
		helper.addColumn(currentCPUUsage, 3, 1, VALUE_WEIGHTP2, 1.0);
		helper.newRow();
		
		JLabel l55s = jlh.newLabel("System CPU Usage (%)");
        helper.addColumn(l55s, 1, 1, LABEL_WEIGHTP2, 1.0);
        systemCPUUsage = jpbh.newProgressBar("This is the Percentage of Total System CPU utilization");
        helper.addColumn(systemCPUUsage, 3, 1, VALUE_WEIGHTP2, 1.0);
        helper.newRow();
		
		
		JLabel l33 = jlh.newLabel("Current Heap Usage (%)");
		helper.addColumn(l33, 1, 1, LABEL_WEIGHTP2, 1.0);
		currentHeapUsage = jpbh.newProgressBar("This is the Percentage of Heap utilization");
		helper.addColumn(currentHeapUsage, 3, 1, VALUE_WEIGHTP2, 1.0);
		helper.newRow();
		
		if (!isJavaVersion8orHigher) {
    		JLabel l44 = jlh.newLabel("Current Non Heap Usage (%)");
    		helper.addColumn(l44, 1, 1, LABEL_WEIGHTP2, 1.0);
    		currentNonHeapUsage = jpbh.newProgressBar("This is the Percentage of Non Heap utilization");
    		helper.addColumn(currentNonHeapUsage, 3, 1, VALUE_WEIGHTP2, 1.0);
    		helper.newRow();
		}
		
		
 		JLabel l14 = jlh.newLabel("Total Execution Time");
		helper.addColumn(l14, 1, 1, LABEL_WEIGHT, 1.0);
		totalTime = jtfh.newTextField("This is the Elapsed Execution Time");
		Font fontTF = totalTime.getFont();
		fontTF = fontTF.deriveFont(Font.BOLD);
		totalTime.setFont(fontTF);
		jtfh.setDefaultFont(fontTF);
		jpbh.setDefaultFont(fontTF);
		p2.add(totalTime,c2);
		helper.addColumn(totalTime, 1, 1, VALUE_WEIGHT, 1.0);
		
				
		JLabel l15 = jlh.newLabel("Total CPU Time");
		helper.addColumn(l15, 1, 1, LABEL_WEIGHT, 1.0);
		totalCPUTime = jtfh.newTextField("This is Total CPU Time");
		helper.addColumn(totalCPUTime, 1, 1, VALUE_WEIGHT, 1.0);
		helper.newRow();

		JLabel l16 = jlh.newLabel("Current Heap Size");
		helper.addColumn(l16, 1, 1, LABEL_WEIGHT, 1.0);
		currentHeapSize = jtfh.newTextField("This is the Current Size of the Heap");
		helper.addColumn(currentHeapSize, 1, 1, VALUE_WEIGHT, 1.0);
		
		JLabel l18 = jlh.newLabel("Current Non Heap Size");
		helper.addColumn(l18, 1, 1, LABEL_WEIGHT, 1.0);
		currentNonHeapSize = jtfh.newTextField("This is the Current Size of the Non Heap Area");
		helper.addColumn(currentNonHeapSize, 1, 1, VALUE_WEIGHT, 1.0);
		helper.newRow();
		
		JLabel l16n = jlh.newLabel("Max Heap Size");
		helper.addColumn(l16n, 1, 1, LABEL_WEIGHT, 1.0);
		maxHeapSize = jtfh.newTextField("This is the Maximum Size the Heap Can Attain");
		helper.addColumn(maxHeapSize, 1, 1, VALUE_WEIGHT, 1.0);
		
		if (!isJavaVersion8orHigher) { 
    		JLabel l18n = jlh.newLabel("Max Non Heap Size");
    		helper.addColumn(l18n, 1, 1, LABEL_WEIGHT, 1.0);
    		maxNonHeapSize = jtfh.newTextField("This is the Maximum Size the Non Heap Can Attain");
    		helper.addColumn(maxNonHeapSize, 1, 1, VALUE_WEIGHT, 1.0);
		}
		helper.newRow();
		
		JLabel l19 = jlh.newLabel("Total Garbage Collections");
		helper.addColumn(l19, 1, 1, LABEL_WEIGHT, 1.0);
		totalGCs = jtfh.newTextField("This is the Total Number of Garbage Collections");
		helper.addColumn(totalGCs, 1, 1, VALUE_WEIGHT, 1.0);
		
		JLabel l1A = jlh.newLabel("Total GC Time (ms)");
		helper.addColumn(l1A, 1, 1, LABEL_WEIGHT, 1.0);
		totalGCTime = jtfh.newTextField("This is the Total Time in Milliseconds Spent in Garbage Collection");
		helper.addColumn(totalGCTime, 1, 1, VALUE_WEIGHT, 1.0);
		helper.newRow();
		
 		JLabel lgcr = jlh.newLabel("Total GC CPU %");
 		helper.addColumn(lgcr, 1, 1, LABEL_WEIGHT, 1.0);
		gcCpuRatio = jpbh.newProgressBar("This is ratio of Total CPU to Time Spent in Garbage Collection");
		gcCpuRatio.setFont(fontTF);
		p2.add(gcCpuRatio,c2);
		helper.addColumn(gcCpuRatio, 1, 1, VALUE_WEIGHT, 1.0);
		
		JLabel lgcp = jlh.newLabel("Current GC CPU%");
		helper.addColumn(lgcp, 1, 1, LABEL_WEIGHTP2, 1.0);
		currentGcCpuRatio = jpbh.newProgressBar("This is ratio of Current CPU to Time Spent in Garbage Collection");
		helper.addColumn(currentGcCpuRatio, 1, 1, VALUE_WEIGHTP2, 1.0);
		helper.newRow();
		
		JLabel l1B = jlh.newLabel("Total Classes Loaded");
		helper.addColumn(l1B, 1, 1, LABEL_WEIGHT, 1.0);
		totalClassesLoaded = jtfh.newTextField("This is Total Number of Classes Loaded by All Classloaders");
		p2.add(totalClassesLoaded,c2);
		helper.addColumn(totalClassesLoaded, 1, 1, VALUE_WEIGHT, 1.0);

		JLabel lblCPU = jlh.newLabel("Average CPU%");
		helper.addColumn(lblCPU, 1, 1, LABEL_WEIGHT, 1.0);
		averageCPU = jtfh.newTextField("This is Average CPU Utilization of the JVM");
		helper.addColumn(averageCPU, 1, 1, VALUE_WEIGHT, 1.0);
		helper.newRow();
		
		JLabel lfc = jlh.newLabel("Objects Pending Finalization");
		helper.addColumn(lfc, 1, 1, LABEL_WEIGHT, 1.0);
		numObjPendingFinal = jtfh.newTextField("This is Total Number of Objects Pending Finalization");
		p2.add(numObjPendingFinal,c2);
		helper.addColumn(numObjPendingFinal, 1, 1, VALUE_WEIGHT, 1.0);

		JLabel lfch = jlh.newLabel("HWM Objects Pending Finalization");
		helper.addColumn(lfch, 1, 1, LABEL_WEIGHT, 1.0);
		hwmObjPendingFinal = jtfh.newTextField("This is High Water Mark of Objects Pending Finalization");
		helper.addColumn(hwmObjPendingFinal, 1, 1, VALUE_WEIGHT, 1.0);
		helper.newRow();
		
		JLabel lthd = jlh.newLabel("Total Threads");
		helper.addColumn(lthd, 1, 1, LABEL_WEIGHT, 1.0);
		totalThreads = jtfh.newTextField("This is the Total Number of Threads in the JVM");
		helper.addColumn(totalThreads, 1, 1, VALUE_WEIGHT, 1.0);
		
		JLabel ldem = jlh.newLabel("Total Daemon Threads");
		helper.addColumn(ldem, 1, 1, LABEL_WEIGHT, 1.0);
		totalDaemonThreads = jtfh.newTextField("This is the Total Number of Daemon Threads in the JVM");
		helper.addColumn(totalDaemonThreads, 1, 1, VALUE_WEIGHT, 1.0);
		helper.newRow();
		
 		JLabel lbt = jlh.newLabel("Current Blocked Threads");
 		helper.addColumn(lbt, 1, 1, LABEL_WEIGHT, 1.0);
		blockingThreads = jtfh.newTextField("This is the Total Number of Threads Currently Blocked on a Monitor Lock");
		helper.addColumn(blockingThreads, 1, 1, VALUE_WEIGHT, 1.0);
		
		JLabel lbtp = jlh.newLabel("Current% of Threads Blocking");
		helper.addColumn(lbtp, 1, 1, LABEL_WEIGHTP2, 1.0);
		currentPercentBlocking = jpbh.newProgressBar("This is the Ratio of Blocking Threads to All Threads");
		helper.addColumn(currentPercentBlocking, 1, 1, VALUE_WEIGHTP2, 1.0);
		
		
		
		
		if (showOSInfo) {
			helper.newRow();
			JLabel lioc = jlh.newLabel("Total IO Count");
			helper.addColumn(lioc, 1, 1, LABEL_WEIGHT, 1.0);
			totalIOCount = jtfh.newTextField("This is the Total Number of I/O Requests for the JVM");
			helper.addColumn(totalIOCount, 1, 1, VALUE_WEIGHT, 1.0);
			
			JLabel liob = jlh.newLabel("Total IO Bytes");
			helper.addColumn(liob, 1, 1, LABEL_WEIGHT, 1.0);
			totalIOBytes = jtfh.newTextField("This is the Total I/O Bytes for the JVM");
			helper.addColumn(totalIOBytes, 1, 1, VALUE_WEIGHT, 1.0);
			helper.newRow();
	
			JLabel ltcpu = jlh.newLabel("CPUs Available");
			helper.addColumn(ltcpu, 1, 1, LABEL_WEIGHT, 1.0);
			totalCPUs = jtfh.newTextField("This is the Total Number of CPU Available to the JVM");
			p2.add(totalCPUs,c2);
			helper.addColumn(totalCPUs, 1, 1, VALUE_WEIGHT, 1.0);
			
			JLabel lcpua = jlh.newLabel("Process CPU Affinity");
			helper.addColumn(lcpua, 1, 1, LABEL_WEIGHT, 1.0);
			cpuAffinity = jtfh.newTextField("This is the CPU Affinity Mask for the JVM, Right Click to Modify");
			p2.add(cpuAffinity,c2);
			helper.addColumn(cpuAffinity, 1, 1, VALUE_WEIGHT, 1.0);
			
			cpuAffinity.addMouseListener(
					new MouseAdapter()
					{
						public void mouseClicked(MouseEvent e)
						{
							if (e.getButton() == 3 && lastCPUCount > 1 && !ui.isPlaybackMode()) {
								AffinityDialog dlg = new AffinityDialog(ui.parent,lastCPUCount, lastAffinity);
								if (!dlg.isDialogCanceled() && lastAffinity != dlg.getNewMask()) {
									
									ui.setAffinity(dlg.getNewMask());
								}
							}
			                
						 }
					}
					);
		} else {
		    helper.newRow();
		    JLabel ltcpu = jlh.newLabel("CPUs Available");
            helper.addColumn(ltcpu, 1, 1, LABEL_WEIGHT, 1.0);
            totalCPUs = jtfh.newTextField("This is the Total Number of CPU Available to the JVM");
            p2.add(totalCPUs,c2);
            helper.addColumn(totalCPUs, 1, 1, VALUE_WEIGHT, 1.0);
		}

		return p2;
		
	}
	public JPanel createThreadUsagePanel() {
		
		JPanel p3 = new JPanel();
 		p3.setLayout(new  GridBagLayout());
 		p3.setBorder(new TitledBorder( new EtchedBorder(), "CPU Usage By Thread"));
 		GridBagConstraints cc = new GridBagConstraints();
		cc.insets= new Insets(1,1,1,1);
		cc.fill = GridBagConstraints.BOTH;
		
		//Thread Usage Table
		threadTable = new TTable( tableModel, options);
		tableModel.setOwner(threadTable);
		threadTable.setAutoCreateColumnsFromModel(false);
		threadTable.setColumnModel(new DefaultTableColumnModel());
		JScrollPane resultPane = new JScrollPane( threadTable , 
	      		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		cc.gridx=0;
		cc.gridy=0;
		cc.gridwidth=4;
		cc.gridheight=4;
		cc.weightx = 1;
		cc.weighty = 1;
		p3.add(resultPane, cc);
		
		//create column and Header renderers
		for (int k = 0; k < tableModel.getColumnCount(); k++) {
			
			DefaultTableCellRenderer renderer = new ThreadTableCellRenderer();
			renderer.setHorizontalAlignment(tableModel.cdata[k].alignment);
			TableColumn column = new TableColumn(k, tableModel.cdata[k].length, renderer, null);
			column.setHeaderRenderer(createDefaultRenderer());
			threadTable.addColumn(column);
		}
		JTableHeader header = threadTable.getTableHeader();
		header.setUpdateTableInRealTime(true);
		header.setReorderingAllowed(false);
		colListener = new ColumnListener(threadTable, tableModel);
		header.addMouseListener(colListener);
		
//		Set up a selection model for the table
		ListSelectionModel rowSM = threadTable.getSelectionModel();
		// Create the listener for the table selection model
		rowSM.addListSelectionListener(new ListSelectionListener() {
	          public void valueChanged(ListSelectionEvent e) {
	              
	          	  //don't care if this is firing because we are updating the table
	              if (e.getValueIsAdjusting()) return;

	              
	              //If they have not selected anything, we want to prevent them
	              // from having access to the various menus
	              ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	              if (!lsm.isSelectionEmpty()) {
	                  
	                  int selectedRow = lsm.getMinSelectionIndex();
	                  String data = (String) threadTable.getValueAt(selectedRow,0);
	                  Long thid = (Long) threadTable.getValueAt(selectedRow,1);
	                 
	                  if (!ui.isPlaybackMode()) {
    	                  if (pcm == null || !pcm.isConnected()) {
    	                      return;
    	                  }
    	                  new ThreadInfoDialog(
    	                  				data,
    	                  				ui.getProbeName(),
    									thid.longValue(),
    									options,
    									pcm,
    									ui);
	                  } else {
	                      
	                      new ThreadStackDialog(
                                  data,
                                  options,
                                  thid.longValue(),
                                  ui);
	                  }
	                  
	                  
	                  
	              }
	              
	          }
	      });
		
		/*
		 * Restore The Sort Order in the table to the one saved in the UIOptions
		 */
        int sortCol = options.getSortCol();
        TableColumnModel colModel = threadTable.getColumnModel();
        int columnModelIndex = colModel.getColumnIndexAtX(sortCol);
        int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

        if (modelIndex >= 0) {
    
            TableColumn column = colModel.getColumn(sortCol);
            int index = column.getModelIndex();
            JLabel renderer = (JLabel)column.getHeaderRenderer();
            renderer.setIcon(tableModel.getColumnIcon(index));
            threadTable.getTableHeader().repaint();
            threadTable.tableChanged(new TableModelEvent(tableModel));
            threadTable.repaint();
        }
        
        
        
		return p3;
	}
	public void update(ProbeResponse res) {
		
		 	endTime = System.nanoTime();
		 	double cpuRelativeValue =  //1 / (double) res.getNumberOfCPUs();
		 		(options.isCpuIsRelative() ?   (1 / (double) res.getNumberOfCPUs()) : 1);
	        long currentTime = endTime - startTime;
	        long intervalTime = currentTime - lastWCTime;
	        
	        long cpuDelta = res.getTotalCPUTime() - lastCPUTime;
	        long gcTimeDelta = res.getTotalGCTime() - lastGCTime;
	        cpuPercent = ((double)cpuDelta / (double)intervalTime) * 100 ;
        
	        cpuPercent *= cpuRelativeValue;
	        avecpuPercent = ((double)((int)((res.getTotalCPUTime() / 
	        		(double)res.getTotalExecutionTime()) * 10000)) /100);
	        avecpuPercent *= cpuRelativeValue;
	        lastCPUTime = res.getTotalCPUTime();
	    	lastWCTime = currentTime;
	    	lastGCTime = res.getTotalGCTime();
	    		    	
	    	currentCPUUsage.setValue((int)cpuPercent);
	    	currentCPUUsage.setString(new Integer((int) cpuPercent).toString() + "%");
	    	
	    	if (cpuPercent < options.getCpuThresholdWarn()) {
	    		currentCPUUsage.setForeground(options.getCpuColorOk());
	    	}else if (cpuPercent < options.getCpuThresholdBad()) {
	    		currentCPUUsage.setForeground(options.getCpuColorWarn());
	    	}else{
	    		currentCPUUsage.setForeground(options.getCpuColorBad());
	    	}
	    	
	    	int systemCPUPercent = (int) (res.getSystemCpuLoad() * 100);
	    	if (systemCPUPercent < 0) {
	    	    systemCPUUsage.setValue(0);
                systemCPUUsage.setString("N/A");
	    	} else {
	    	    systemCPUUsage.setValue(systemCPUPercent);
	    	    systemCPUUsage.setString(systemCPUPercent + "%");
            
                if (systemCPUPercent < options.getCpuThresholdWarn()) {
                    systemCPUUsage.setForeground(options.getCpuColorOk());
                }else if (systemCPUPercent < options.getCpuThresholdBad()) {
                    systemCPUUsage.setForeground(options.getCpuColorWarn());
                }else{
                    systemCPUUsage.setForeground(options.getCpuColorBad());
                }
	    	}
            
	    	heapUsagePct = ((double)res.getCurrentHeapSize() / 
	    			(double)res.getMaxHeapSize()) * 100;
	    	
	        nonHeapUsagePct = ((double)res.getCurrentNonHeapSize() / 
	        		(double)res.getMaxNonHeapSize()) * 100; 
	        
	    	currentHeapUsage.setValue((int)heapUsagePct);
	    	
	    	if (heapUsagePct < options.getHeapThresholdWarn()) {
	    		currentHeapUsage.setForeground(options.getHeapColorOk());
	    	}else if (heapUsagePct < options.getHeapThresholdBad()) {
	    		currentHeapUsage.setForeground(options.getHeapColorWarn());
	    	}else {
	    		currentHeapUsage.setForeground(options.getHeapColorBad());
	    	}
	    	
	    	if (!isJavaVersion8orHigher) {
	    	    currentNonHeapUsage.setValue((int)nonHeapUsagePct);
	    	
    	    	if (nonHeapUsagePct < options.getNonHeapThresholdWarn()) {
    	    		currentNonHeapUsage.setForeground(options.getNonHeapColorOk());
    	    	}else if (nonHeapUsagePct < options.getNonHeapThresholdBad()) {
    	    		currentNonHeapUsage.setForeground(options.getNonHeapColorWarn());
    	    	}else {
    	    		currentNonHeapUsage.setForeground(options.getNonHeapColorBad());
    	    	}
	    	}
	    	long totalCPU = res.getTotalCPUTime();
	    	long totalGc = res.getTotalGCTime() * NANOS_PER_MILLI;
	    	
	    	double aveGcPercent = ((double)totalGc  / 
	        		(double)totalCPU ) * 100;	
	    	
	    	double currentGcPercent = ((double)( gcTimeDelta * NANOS_PER_MILLI)  / 
	        		(double)cpuDelta ) * 100;	
	    	
	    	if (aveGcPercent < options.getGcThresholdWarn()) {
	    		gcCpuRatio.setForeground(options.getGcColorOk());
	    	}else if (aveGcPercent < options.getGcThresholdBad()) {
	    		gcCpuRatio.setForeground(options.getGcColorWarn());
	    	}else {
	    		gcCpuRatio.setForeground(options.getGcColorBad());
	    	}
	    	
	    	if (currentGcPercent < options.getGcThresholdWarn()) {
	    		currentGcCpuRatio.setForeground(options.getGcColorOk());
	    	}else if (currentGcPercent < options.getGcThresholdBad()) {
	    		currentGcCpuRatio.setForeground(options.getGcColorWarn());
	    	}else {
	    		currentGcCpuRatio.setForeground(options.getGcColorBad());
	    	}
	    		    		    	
	    	numObjPendingFinal.setText(
	    			format(res.getNumberOfObjectsPendingFinalization()));
	    	
	    	hwmObjPendingFinal.setText( 
	    			format(res.getNumberOfObjectsPendingFinalizationHWM()));
	    	
	    	currentGcCpuRatio.setValue((int)currentGcPercent);
	    	gcCpuRatio.setValue((int)aveGcPercent);
	    	
	    	String tag = "";
	    	long cpuDiff = res.getTotalCPUTime() - lastCPUUsed;
	    	
	    	if (cpuDiff > 0 && lastCPUUsed > 0 && options.isShowDeltas()) {
	    	    tag = "\t +" + format(cpuDiff / NANOS_PER_MILLI) + "ms";
	    	}
	    	
	    	totalCPUTime.setText(formatElapsedTime(res.getTotalCPUTime() / 
	    			NANOS_PER_SECOND) + tag);
	    	
	    	totalTime.setText(formatElapsedTime(res.getTotalExecutionTime() /
	    			NANOS_PER_SECOND));
	    	
	    	averageCPU.setText(format(avecpuPercent,2));
	    	res.setAverageCPUPercent(avecpuPercent);
	    	res.setCurrentCPUPercent(cpuPercent);
	    	
	    	tag = "";
            long heapDiff = res.getCurrentHeapSize() - lastHeapUsed;
            
            if (heapDiff != 0 && lastHeapUsed > 0 && options.isShowDeltas()) {
                String sign = (heapDiff > 0 ? "+" : "");
                tag = "\t " + sign +  format(heapDiff , true);
            }
	    	currentHeapSize.setText(format(res.getCurrentHeapSize(), true) + tag);
	    	
	    	tag = "";
            long nonHeapDiff = res.getCurrentNonHeapSize() - lastNonHeapUsed;
            
            if (nonHeapDiff != 0 && lastNonHeapUsed > 0 && options.isShowDeltas()) {
                String sign = (nonHeapDiff > 0 ? "+" : "");
                tag = "\t " + sign + format(nonHeapDiff , true);
            }
	    	currentNonHeapSize.setText(format(res.getCurrentNonHeapSize(), true) + tag);
	    	
	    	maxHeapSize.setText(format(res.getMaxHeapSize(), true));
	    	
	    	if (!isJavaVersion8orHigher) {
	    	    maxNonHeapSize.setText(format(res.getMaxNonHeapSize(), true));
	    	}
	    	
	    	tag = "";
	    	if (gcTimeDelta > 0 && lastGCTime > 0 && options.isShowDeltas()) {
                tag = "\t +" + format(gcTimeDelta , true) + "ms";
            }
	    	
	    	totalGCTime.setText(format(res.getTotalGCTime()) + tag);
	    	
	    	tag = "";
	    	long gcCountDelta = res.getTotalGCs() - lastGCCount;
	    	if (gcCountDelta > 0 && lastGCCount > 0 && options.isShowDeltas()) {
                tag = "\t +" + format(gcCountDelta , true);
            }
	    	
	    	totalGCs.setText(format(res.getTotalGCs()) + tag);
	    	
	    	
	    	totalClassesLoaded.setText(format(res.getTotalClassesLoaded()) );
	    	tableModel.update(res, cpuRelativeValue, cpuDelta);
	    	totalThreads.setText(new Integer(
	    			res.getNumberOfActiveThreads()).toString());
	    	totalDaemonThreads.setText(new Integer(
	    			res.getNumberOfDaemonThreads()).toString());
	    	blockingThreads.setText(new Integer(
	    			res.getNumberOfBlockedThreads()).toString());
			
	    	double percentBlocked = ((double)res.getNumberOfBlockedThreads() / 
	        		(double)res.getNumberOfActiveThreads()) * 100; 
			
			currentPercentBlocking.setValue((int) percentBlocked);
			if (percentBlocked > 0 ) {
				currentPercentBlocking.setForeground(Color.red);
				blockingThreads.setForeground(Color.red);
			} else {
				currentPercentBlocking.setForeground(Color.green);
				blockingThreads.setForeground(Color.black);
			}
			
	    	if (res.isIoCountersAvailable()) {
	    	    long ioCount = res.getIoOtherCount() + 
                               res.getIoReadCount() +
                               res.getIoWriteCount();
	    	    long ioBytes = res.getIoOtherBytes() + 
                               res.getIoReadBytes() +
                               res.getIoWriteBytes();
	    	    long ioCountDelta = ioCount - lastIOCount;
	    	    long ioBytesDelta = ioBytes - lastIOBytes;
	    	    
	    	    tag = "";
	    	    if (ioCountDelta > 0 && lastIOCount > 0 && options.isShowDeltas()) {
	                tag = "\t +" + format(ioCountDelta);
	            }
		    	totalIOCount.setText(format(ioCount) + tag);
		    	
		    	tag = "";
                if (ioBytesDelta > 0 && lastIOBytes > 0 && options.isShowDeltas()) {
                    tag = "\t +" + format(ioBytesDelta, true);
                }
		    	totalIOBytes.setText(format(ioBytes, true ) + tag);
		    	cpuAffinity.setText(getBitMask( res.getCpuAffinity(), 
		    	        (int)res.getNumberOfPhysicalCPUs(), false));
		    	String cpuInf = res.getNumberOfCPUs() + " of " + res.getNumberOfPhysicalCPUs();
		    	totalCPUs.setText(cpuInf);
		    	lastCPUCount = res.getNumberOfPhysicalCPUs();
		    	lastAffinity = res.getCpuAffinity();
		    	lastIOCount = ioCount;
		    	lastIOBytes = ioBytes;
	    	}  else {
	    	    totalCPUs.setText(Utilities.format(res.getNumberOfCPUs()));
	    	}
	    	
	    	lastCPUUsed = res.getTotalCPUTime();
	    	lastHeapUsed = res.getCurrentHeapSize();
	    	lastNonHeapUsed = res.getCurrentNonHeapSize();
	    	lastGCCount = res.getTotalGCs();
			
	}
	public void resetPanel() {
		
		totalTime.setText("");
		totalCPUTime.setText("");
		currentHeapSize.setText("");
		currentNonHeapSize.setText("");
		maxHeapSize.setText("");
		if (!isJavaVersion8orHigher) {
		    maxNonHeapSize.setText("");
		}
		totalGCs.setText("");
		totalGCTime.setText("");
		totalClassesLoaded.setText("");
		averageCPU.setText("");
		totalThreads.setText("");
		totalDaemonThreads.setText("");
		currentHeapUsage.setValue(0);
		if (!isJavaVersion8orHigher) {
		    currentNonHeapUsage.setValue(0);
		}
		currentCPUUsage.setValue(0);
		tableModel.resetTable();
		
	}
	protected TableCellRenderer createDefaultRenderer() {
		DefaultTableCellRenderer label = new DefaultTableCellRenderer() {
			
            private static final long serialVersionUID = 1L;

            public Component getTableCellRendererComponent(JTable table, Object value,
							 boolean isSelected, boolean hasFocus, int row, int column) {
				if (table != null) {
					
					JTableHeader header = table.getTableHeader();
					if (header != null) {
						setForeground(header.getForeground());
						setBackground(header.getBackground());
						setFont(header.getFont());
					}
				}

				setText((value == null) ? "" : value.toString());
				setBorder(UIManager.getBorder("TableHeader.cellBorder"));
				return this;
			}
		};
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
    }
	
	public void toggleZeroCPU() {
		
		tableModel.showZeroCPU = 
			(tableModel.showZeroCPU ? false: true);
	}
	/**
	 * @return the panelBuilt
	 */
	public boolean isPanelBuilt() {
		return panelBuilt;
	}
	
	
	
}
class TextRenderer extends JTextField implements TableCellRenderer {

    private static final long serialVersionUID = 1L;
	private ThreadDataModel tdm;
	//private Font italic;
	private Font bold;
	private UIOptions opt;
	public TextRenderer(TableModel tdm, UIOptions opt) {
		
		bold = this.getFont();
		bold = bold.deriveFont(Font.BOLD);
		this.opt = opt;
		this.setFont(bold);
		this.tdm = (ThreadDataModel) tdm;
		
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (value instanceof String ) {
			String val = (String) value;
			
			if (row % 2 == 1) {
				this.setBackground(Color.LIGHT_GRAY);
			} else {
				this.setBackground(Color.WHITE);
			}
			Color color = opt.getNonDaemonThreadColor();
			
			if (column == ColumnData.COL_NAME) {
			    
			    if (tdm.isThreadBlocked(row)) {
			        color = opt.getBlockingThreadColor();
			    } else if (tdm.isDaemonThread(row)) {
			        color = opt.getDaemonThreadColor();
			    }
			}
			        
			this.setForeground(color);
			this.setText(val);
		} else if (value instanceof Long ) {
			Long vl = (Long) value;
			String val = new String(vl.toString());
			this.setText(val);
			if (row % 2 == 1) {
				this.setBackground(Color.LIGHT_GRAY);
			} else {
				this.setBackground(Color.WHITE);
			}
		}
				
			return this;
	}
	
}
class TTable extends JTable {
	
    private static final long serialVersionUID = 1L;	
	private TableModel mod;
	private UIOptions options;
	
	public TTable(TableModel mod, UIOptions options) {
		
		super(mod);
		this.mod = mod;
		this.options = options;
		
		super.setRowHeight(super.getRowHeight() + options.getRowHeightAdjustment());
	}
	public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == ColumnData.COL_PERCENT || column == ColumnData.COL_CURR_PERCENT) {
            return new ProgressBarRenderer(options);
        }
    	return new TextRenderer(mod, options);
    }
}


class ColumnListener extends MouseAdapter {

	private JTable table;
	private ThreadDataModel model;
	
	public ColumnListener(JTable table, ThreadDataModel model) {
		
		this.table = table;
		this.model = model;
		
	}
	public void mouseClicked(MouseEvent e) {
	    doSort(e.getX());
	}
	public void doSort(int idx) {
        TableColumnModel colModel = table.getColumnModel();
        int columnModelIndex = colModel.getColumnIndexAtX(idx);
        int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();
        
        if (modelIndex < 0)
            return;
        if (model.getSortCol() == modelIndex)
            model.setSortAsc(!model.isSortAsc());
        else
            model.setSortCol(modelIndex);

        for (int i=0; i < model.getColumnCount(); i++) {
            TableColumn column = colModel.getColumn(i);
            JLabel renderer = (JLabel)column.getHeaderRenderer();
            renderer.setIcon(model.getColumnIcon(i));
        }
        
        table.getTableHeader().repaint();

        model.sortData();
        table.tableChanged(new TableModelEvent(model));
        table.repaint();
	}
}
class ThreadTableCellRenderer extends DefaultTableCellRenderer {

	private Color defaultColor;
	private static final long serialVersionUID = 1L;
	public ThreadTableCellRenderer() {
		defaultColor = this.getBackground();
	}
	public void setValue(Object value) {
	    Logger.getLogger().debug("name = " + value.getClass().getName());
		if (value instanceof IconData) {
			IconData ivalue = (IconData)value;
			setIcon(ivalue.m_icon);
			setText(ivalue.m_data.toString());
			this.setBackground(defaultColor); 
		}
		else {
			super.setValue(value);
			setText(value.toString());
			this.setBackground(defaultColor); 
		}
	}
}


class IconData {

	public ImageIcon	m_icon;
	public Object m_data;

	public IconData(ImageIcon icon, Object data) {
		m_icon = icon;
		m_data = data;
	}

	public String toString() {
		return m_data.toString();
	}
}