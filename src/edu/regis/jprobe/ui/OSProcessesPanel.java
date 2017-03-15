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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.regis.jprobe.jni.OSLibInfo;
import edu.regis.jprobe.jni.OSProcessInfo;
import edu.regis.jprobe.jni.OSSystemInfo;
import edu.regis.jprobe.model.BroadcastMessage;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.Utilities;
import edu.regis.jprobe.ui.helpers.GridBagLayoutHelper;
import edu.regis.jprobe.ui.helpers.JLabelHelper;
import edu.regis.jprobe.ui.helpers.JProgressBarHelper;
import edu.regis.jprobe.ui.helpers.JTextFieldHelper;

import com.sun.management.OperatingSystemMXBean;
//import edu.regis.guitools.PercentageGraph;

/**
 * @author jdivince
 *
 * This is the Overview Panel.
 */
public class OSProcessesPanel extends PerformancePanel  {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JTextField processID;
    private JTextField parentProcess;
    private JTextField imageName;
	private JTextField imagePath;
	private JTextField imageType;
	private JTextField threadCount;
	private JTextField handleCount;
	private JTextField totalKernelTime;
	private JTextField totalUserTime;
	private JTextField totalCPUTime;
	private JTextField ioReads;
	private JTextField ioWrites;
	private JTextField ioOther;
	private JTextField ioReadsDelta;
    private JTextField ioWritesDelta;
    private JTextField ioOtherDelta;
    private JTextField ioReadBytes;
    private JTextField ioWriteBytes;
    private JTextField ioOtherBytes;
    private JTextField ioReadBytesDelta;
    private JTextField ioWriteBytesDelta;
    private JTextField ioOtherBytesDelta;
	private JTextField pageFaults;
	private JTextField pageFaultsDelta;
	private JTextField wsSize;
	private JTextField peakWSSize;
	private JTextField pageFileSize;
	private JTextField privateUsage;
	
	
	private MiniCPUGraph cpuGraph;
    private MiniIOActivityGraph ioGraph;
    private MiniIOBytesGraph ioBytesGraph;
    private MiniMemoryGraph memoryGraph;
    private MiniWSSDeltaGraph wssGraph;
    private MiniPagingGraph pageFaultsGraph;
	
	private JTable processTable;
	private ProcessDataModel processTableModel;
	
	private long startTime = 0;
	
	protected OSProcessClientFrame ui;
	private UIOptions options;
	private boolean panelBuilt = false;
	private boolean paused = false;
	private ProcessColumnListener colListener;
	private TitledBorder detailsBorder;
	protected Dimension buttonSize;
	protected Dimension headingSize;
	public Dimension largeTextSize;
	private long lastTime;
	private long numCPUs = 1;
	private long selectedProcess = -1;
	protected long maxVirt = 1000000000;
	private OSProcessInfo selectedPDlast = new OSProcessInfo();
	private JPanel selectedPanel;
	private JPanel modulePanel;
	private JPanel detailPanel;
	private JSplitPane sp;
	private OSProcessesPanel instance;
	private List<OSLibInfo> libs = new ArrayList<OSLibInfo>();
	private static final double LABEL_WEIGHTP2 = 0.05;
	private static final double VALUE_WEIGHTP2 = 0.1;
	public static final long NANOS_PER_MILLI = 1000000;
	public static final long NANOS_PER_SECOND = NANOS_PER_MILLI * 1000;
	private BroadCastScanner scanner;
	
	public OSProcessesPanel(OSProcessClientFrame u, UIOptions options) {
		
		buttonSize = new Dimension(30,40);
		headingSize = new Dimension(50,30);
		largeTextSize = new Dimension(100,30);
		instance = this;
		ui = u;
		lastTime = System.currentTimeMillis();
		scanner = BroadCastScanner.getInstance();
		this.options = options;
		
		if (OSSystemInfo.isOperational()) {
            numCPUs = OSSystemInfo.getNumberOfCPUs();
        }
		OperatingSystemMXBean osmb = Utilities.getSunOSBean();
		
		if (osmb != null) {
		    maxVirt = osmb.getTotalSwapSpaceSize();
		}
		buildPanel();
		update();
		
	}
	
	public void buildPanel() {
		
		
		
		processTableModel = new ProcessDataModel(startTime, options);
		detailPanel = new JPanel();
		detailPanel.setLayout(new GridLayout(1,1,1,1));
		
 		// Thread Usage panel
		JPanel p1 = createProcessPanel();
		
 		// Panel for our action buttons
		sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, p1, detailPanel);
		sp.setOneTouchExpandable(true);
		sp.setDividerLocation(1.0);
        sp.setResizeWeight(1.0);
		this.add(sp);
		setLayout(new GridLayout(1,1,1,1));
		panelBuilt = true;


		detailPanel.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                
                int loc = sp.getDividerLocation();
                int size = sp.getSize().height;
                double ratio = ((double) loc / (double) size);
                //System.out.println("Loc = " + loc + ", size = " + size + 
                //        ", Ratio = " + Utilities.format((double)loc / (double)size, 9));
                
                if (ratio < .80) {
                    options.setProcessSplitRatio(ratio);
                }
           
            }
           
		});
		validate();
		
		repaint();
	}
	public JPanel createSelectedPanel(long pid) {
		
	    
	    final BroadcastMessage bm = getBroadcastMessage(pid);
	    Color lblColor = Color.GRAY;
	    double weightH = 0.0;
	    detailsBorder = new TitledBorder( new EtchedBorder(), "Select a Process");
		JPanel p2 = new JPanel();
 		p2.setLayout(new  GridBagLayout());
 		p2.setBorder(detailsBorder);
 		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(1,1,1,1);
		c2.fill = GridBagConstraints.BOTH;
		GridBagLayoutHelper helper = new GridBagLayoutHelper(p2, c2, 0.0, 0.0);
		JLabelHelper jlh = new JLabelHelper(JLabel.RIGHT, headingSize, lblColor, null, null);
		JProgressBarHelper jpbh = new JProgressBarHelper();
		JTextFieldHelper jtfh = new JTextFieldHelper(false, largeTextSize, null);
		jtfh.setDefaultFont(this.getFont().deriveFont(Font.BOLD));
		jpbh.setDefaultPreferredSize(largeTextSize);
	
		JLabel l1 = jlh.newLabel("Process ID");
        helper.addColumn(l1, 1, 1, LABEL_WEIGHTP2, weightH);
        processID = jtfh.newTextField(Utilities.format(pid));
        helper.addColumn(processID, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l1s = jlh.newLabel("Parent Process");
        helper.addColumn(l1s, 1, 1, LABEL_WEIGHTP2, weightH);
        parentProcess = jtfh.newTextField();
        helper.addColumn(parentProcess, 1, 1, VALUE_WEIGHTP2, weightH);
		JLabel l1a = jlh.newLabel("Image Name");
		helper.addColumn(l1a, 1, 1, LABEL_WEIGHTP2, weightH);
		imageName = jtfh.newTextField();
		helper.addColumn(imageName, 1, 1, VALUE_WEIGHTP2, weightH);
		JLabel l2a = jlh.newLabel("Image Type");
        helper.addColumn(l2a, 1, 1, LABEL_WEIGHTP2, weightH);
        imageType = jtfh.newTextField();
        helper.addColumn(imageType, 1, 1, VALUE_WEIGHTP2, weightH);
        //helper.newRow();
        
        JLabel l3a = jlh.newLabel("Image Path");
        helper.addColumn(l3a, 1, 1, LABEL_WEIGHTP2, weightH);
        imagePath= jtfh.newTextField();
        helper.addColumn(imagePath, 5, 1, VALUE_WEIGHTP2, weightH);
		helper.newRow();
		
		JLabel l4 = jlh.newLabel("Threads");
        helper.addColumn(l4, 1, 1, LABEL_WEIGHTP2, weightH);
        threadCount = jtfh.newTextField();
        helper.addColumn(threadCount, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l5 = jlh.newLabel("Handles");
        helper.addColumn(l5, 1, 1, LABEL_WEIGHTP2, weightH);
        handleCount = jtfh.newTextField();
        helper.addColumn(handleCount, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l6 = jlh.newLabel("Kernel Time");
        helper.addColumn(l6, 1, 1, LABEL_WEIGHTP2, weightH);
        totalKernelTime = jtfh.newTextField();
        helper.addColumn(totalKernelTime, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l7 = jlh.newLabel("User Time");
        helper.addColumn(l7, 1, 1, LABEL_WEIGHTP2, weightH);
        totalUserTime = jtfh.newTextField();
        helper.addColumn(totalUserTime, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l8 = jlh.newLabel("CPU Time");
        helper.addColumn(l8, 1, 1, LABEL_WEIGHTP2, weightH);
        totalCPUTime = jtfh.newTextField();
        helper.addColumn(totalCPUTime, 1, 1, VALUE_WEIGHTP2, weightH);
        helper.newRow();
        
        JLabel l9 = jlh.newLabel("I/O Reads");
        helper.addColumn(l9, 1, 1, LABEL_WEIGHTP2, weightH);
        ioReads = jtfh.newTextField();
        helper.addColumn(ioReads, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l10 = jlh.newLabel("I/O Writes");
        helper.addColumn(l10, 1, 1, LABEL_WEIGHTP2, weightH);
        ioWrites = jtfh.newTextField();
        helper.addColumn(ioWrites, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l11 = jlh.newLabel("I/O Other");
        helper.addColumn(l11, 1, 1, LABEL_WEIGHTP2, weightH);
        ioOther = jtfh.newTextField();
        helper.addColumn(ioOther, 1, 1, VALUE_WEIGHTP2, weightH);
        
        JLabel l9a = jlh.newLabel("Read Bytes");
        helper.addColumn(l9a, 1, 1, LABEL_WEIGHTP2, weightH);
        ioReadBytes = jtfh.newTextField();
        helper.addColumn(ioReadBytes, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l10a = jlh.newLabel("Write Bytes");
        helper.addColumn(l10a, 1, 1, LABEL_WEIGHTP2, weightH);
        ioWriteBytes = jtfh.newTextField();
        helper.addColumn(ioWriteBytes, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l11a = jlh.newLabel("Other Bytes");
        helper.addColumn(l11a, 1, 1, LABEL_WEIGHTP2, weightH);
        ioOtherBytes = jtfh.newTextField();
        helper.addColumn(ioOtherBytes, 1, 1, VALUE_WEIGHTP2, weightH);
        helper.newRow();
        
        JLabel l9b = jlh.newLabel("Reads Delta");
        helper.addColumn(l9b, 1, 1, LABEL_WEIGHTP2, weightH);
        ioReadsDelta = jtfh.newTextField();
        helper.addColumn(ioReadsDelta, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l10b = jlh.newLabel("Writes Delta");
        helper.addColumn(l10b, 1, 1, LABEL_WEIGHTP2, weightH);
        ioWritesDelta = jtfh.newTextField();
        helper.addColumn(ioWritesDelta, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l11b = jlh.newLabel("Other Delta");
        helper.addColumn(l11b, 1, 1, LABEL_WEIGHTP2, weightH);
        ioOtherDelta = jtfh.newTextField();
        helper.addColumn(ioOtherDelta, 1, 1, VALUE_WEIGHTP2, weightH);
        
        JLabel l9c = jlh.newLabel("Read Bytes Delta");
        helper.addColumn(l9c, 1, 1, LABEL_WEIGHTP2, weightH);
        ioReadBytesDelta = jtfh.newTextField();
        helper.addColumn(ioReadBytesDelta, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l10c = jlh.newLabel("Write Bytes Delta");
        helper.addColumn(l10c, 1, 1, LABEL_WEIGHTP2, weightH);
        ioWriteBytesDelta = jtfh.newTextField();
        helper.addColumn(ioWriteBytesDelta, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l11c = jlh.newLabel("Other Bytes Delta");
        helper.addColumn(l11c, 1, 1, LABEL_WEIGHTP2, weightH);
        ioOtherBytesDelta = jtfh.newTextField();
        helper.addColumn(ioOtherBytesDelta, 1, 1, VALUE_WEIGHTP2, weightH);
        helper.newRow();
		
        JLabel l12 = jlh.newLabel("Page Faults");
        helper.addColumn(l12, 1, 1, LABEL_WEIGHTP2, weightH);
        pageFaults = jtfh.newTextField();
        helper.addColumn(pageFaults, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l13 = jlh.newLabel("Page Faults Delta");
        helper.addColumn(l13, 1, 1, LABEL_WEIGHTP2, weightH);
        pageFaultsDelta = jtfh.newTextField();
        helper.addColumn(pageFaultsDelta, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l14 = jlh.newLabel("Working Set Size");
        helper.addColumn(l14, 1, 1, LABEL_WEIGHTP2, weightH);
        wsSize = jtfh.newTextField();
        helper.addColumn(wsSize, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l15 = jlh.newLabel("Peak WSS");
        helper.addColumn(l15, 1, 1, LABEL_WEIGHTP2, weightH);
        peakWSSize = jtfh.newTextField();
        helper.addColumn(peakWSSize, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l16 = jlh.newLabel("Page File Usage");
        helper.addColumn(l16, 1, 1, LABEL_WEIGHTP2, weightH);
        pageFileSize = jtfh.newTextField();
        helper.addColumn(pageFileSize, 1, 1, VALUE_WEIGHTP2, weightH);
        JLabel l17 = jlh.newLabel("Private Size");
        helper.addColumn(l17, 1, 1, LABEL_WEIGHTP2, weightH);
        privateUsage = jtfh.newTextField();
        helper.addColumn(privateUsage, 1, 1, VALUE_WEIGHTP2, weightH);
        helper.newRow();
        
        JButton close = new JButton("Close");
        close.setToolTipText("Close This Detail Panel");
        JButton kill = new JButton("Kill");
        kill.setToolTipText("Kill This Process");
        JButton attach = new JButton("Attach");
        attach.setToolTipText("Attach to the Probe in this Processes JVM");
        helper.addColumn(close, 1, 1, 0.0, weightH);
        
        if (OSSystemInfo.isOperational()) {
            helper.addColumn(kill, 1, 1, 0.0, weightH);
        }
        
        if (bm != null) {
            helper.addColumn(attach, 1, 1, 0.0, weightH);
        }
        
        close.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectedProcess = -1;
                selectedPDlast = new OSProcessInfo();
                detailPanel.removeAll(); 
                sp.setDividerLocation(1.0);
                sp.setResizeWeight(1.0);
                instance.updateUI();
            }
            
        });
        
        kill.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                String pid = processID.getText().replace(",", "");
                long killPid = Long.parseLong(pid);
                String name = imageName.getText();
                
                if ( JOptionPane.showConfirmDialog(instance, 
                        "Are You Sure You Want To Kill This Process?",
                        "Kill " + pid + " (" + name + ")?",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                } else {
                    return;
                }
                
                long rc = OSSystemInfo.killProcess(killPid);
                
                switch ((int)rc) {
                case 0:
                    JOptionPane.showMessageDialog(instance, 
                            "Process " + killPid +  "(" +
                                    name + ") Killed", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;
                case 5:
                    JOptionPane.showMessageDialog(instance, 
                            "Not Authorized to Kill Process " + killPid +  "(" +
                                    name + ")", "Failed", 
                            JOptionPane.ERROR_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(instance, 
                            "Kill of Process " + killPid +  "(" +
                                    name + ") Failed, Error Code is " +
                                    rc, "Failed", JOptionPane.ERROR_MESSAGE);   
                }
            }
            
        });
        
        attach.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ui.getParentUI().addClient(bm);
            }
            
        });
        
        
        helper.newRow();
        int numObs = 120;
       
        cpuGraph = new MiniCPUGraph(numObs);
        ioGraph = new MiniIOActivityGraph(numObs);
        ioBytesGraph = new MiniIOBytesGraph(numObs);
        wssGraph = new MiniWSSDeltaGraph(numObs);
        memoryGraph = new MiniMemoryGraph(numObs);
        pageFaultsGraph = new MiniPagingGraph(numObs);
        
        

        helper.addColumn(cpuGraph, 2, 4, 1, 1);
        helper.addColumn(ioGraph, 2, 4, 1, 1);
        helper.addColumn(ioBytesGraph, 2, 4, 1, 1);
        helper.addColumn(memoryGraph, 2, 4, 1, 1);
        helper.addColumn(wssGraph, 2, 4, 1, 1);
        helper.addColumn(pageFaultsGraph, 2, 4, 1, 1);
        
        p2.addMouseListener(new MouseAdapter() {


            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 3) {
                    
                    promptMenu();

                }
                
            }

            
            
        });
		return p2;
		
	}
	public JPanel createLibsPanel(List<OSLibInfo> libs) {
	    JPanel p3 = new JPanel();
        p3.setLayout(new  GridLayout(1,1,1,1));
        p3.setBorder(new TitledBorder( new EtchedBorder(), "Modules"));
        String[] colNames = {"Name", "Path", "Size", "File Date/Time", "Load Address"}; 
        String[][] data = new String[libs.size()][colNames.length];
        
        
        for (int i=0; i < libs.size(); i++) {
            OSLibInfo li = libs.get(i);
            data[i] [0] = li.getName();
            data[i] [1] = li.getPath();
            data[i] [2] = Utilities.format(li.getSize());
            data[i] [3] = li.getFileDate();
            data[i] [4] = li.getLoadAddress();
            
        }
        
        DefaultTableModel model = new DefaultTableModel(data, colNames);
        
        JTable tab = new JTable(model);
        tab.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane sp = new JScrollPane(tab, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        p3.add(sp);
        return p3;
	}
	public JPanel createProcessPanel() {
		
		JPanel p3 = new JPanel();
 		p3.setLayout(new  GridBagLayout());
 		p3.setBorder(new TitledBorder( new EtchedBorder(), "Processes"));
 		GridBagConstraints cc = new GridBagConstraints();
		cc.insets= new Insets(1,1,1,1);
		cc.fill = GridBagConstraints.BOTH;
		
		//Thread Usage Table
		processTable = new PTable( processTableModel, options);
		processTableModel.setOwner(processTable);
		processTable.setAutoCreateColumnsFromModel(false);
		processTable.setColumnModel(new DefaultTableColumnModel());
		JScrollPane resultPane = new JScrollPane( processTable , 
	      		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		cc.gridx=0;
		cc.gridy=0;
		cc.gridwidth=4;
		cc.gridheight=4;
		cc.weightx = 1;
		cc.weighty = 1;
		//resultPane.setFont(fontTF);
		p3.add(resultPane, cc);
		
		//create column and Header renderers
		for (int k = 0; k < processTableModel.getColumnCount(); k++) {
			
			DefaultTableCellRenderer renderer = new ThreadTableCellRenderer();
			renderer.setHorizontalAlignment(processTableModel.cdata[k].alignment);
			TableColumn column = new TableColumn(k, processTableModel.cdata[k].length, renderer, null);
			column.setHeaderRenderer(createDefaultRenderer());
			processTable.addColumn(column);
		}
		JTableHeader header = processTable.getTableHeader();
		header.setUpdateTableInRealTime(true);
		header.setReorderingAllowed(false);
		colListener = new ProcessColumnListener(processTable, processTableModel);
		header.addMouseListener(colListener);
		
//		Set up a selection model for the table
		ListSelectionModel rowSM = processTable.getSelectionModel();
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
	                  
	                  Long pid = (Long) processTable.getValueAt(selectedRow,2);
	                  ProcessData selectedPD = processTableModel.getProcess(pid);
	                  
	                  if (selectedPD != null && pid != selectedPDlast.getProcessId()) {
	                      JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
	                      selectedProcess = pid;
	                      selectedPDlast = new OSProcessInfo(selectedPD.pi);
	                      selectedPanel = createSelectedPanel(pid);
	                      detailPanel.removeAll();        
	                      updateSelected(selectedPD, 1);
	                      modulePanel = createLibsPanel(libs);
	                      tabPane.addTab("Performance Details", selectedPanel);
	                      tabPane.addTab("Loaded Modules", modulePanel);
	                      detailPanel.add(tabPane);
	                      sp.setDividerLocation(options.getProcessSplitRatio());
	                      sp.setResizeWeight(0.60);
	                      detailPanel.updateUI();
	                      instance.updateUI();
	                  }
	                  
	                  
	              }
	              
	          }
	      });
		
		/*
		 * Restore The Sort Order in the table to the one saved in the UIOptions
		 */
        int sortCol = options.getSortCol();
        //System.out.println("SortCol=" + sortCol);
        TableColumnModel colModel = processTable.getColumnModel();
        int columnModelIndex = colModel.getColumnIndexAtX(sortCol);
        int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();
        //System.out.println("Model Index=" + modelIndex);
        if (modelIndex >= 0) {
    
            TableColumn column = colModel.getColumn(sortCol);
            int index = column.getModelIndex();
            //System.out.println("Column Index=" + index);
            JLabel renderer = (JLabel)column.getHeaderRenderer();
            renderer.setIcon(processTableModel.getColumnIcon(index));
            processTable.getTableHeader().repaint();
            processTable.tableChanged(new TableModelEvent(processTableModel));
            processTable.repaint();
        }
        
        
        
		return p3;
	}
	public void update() {
		
		    if (paused) {
		        return;
		    }
		 	long time = System.currentTimeMillis();
		 	long timeDelta = time - lastTime;
		 	lastTime = time;
		 	List<OSProcessInfo> plist = OSSystemInfo.getOSProcesses(ui.allUsersSelected());
	    	processTableModel.update(plist, timeDelta);
	    	updateSelected(processTableModel.getProcess(selectedProcess), timeDelta);
			
	}
	public void updateSelected(ProcessData selectedPD, long timeDelta) {
	    
	    if (selectedPD == null || selectedPanel == null) {
	        return;
	    }
	    
	   
	    
	   detailsBorder = new TitledBorder( new EtchedBorder(),
	            "Process Details for " + selectedPD.processId + " (" + 
	           selectedPD.processName + ")");
	   selectedPanel.setBorder(detailsBorder);
	    
   
	    libs = OSSystemInfo.getNativeLibs(selectedPD.processId);
       
        
        for (OSLibInfo lib : libs) {
            if (lib.getName().equalsIgnoreCase(selectedPD.processName)) {
                imagePath.setText(lib.getPath());
            }
        }
        
	    OSProcessInfo pi = selectedPD.pi;
	    OSProcessInfo dpi = selectedPDlast;
	    ProcessData parent = processTableModel.getProcess(pi.getParentPID());
	    
	    long cpuKernelDelta = pi.getKernelTime() - dpi.getKernelTime();
	    double cpuKernelPercent = (((double)cpuKernelDelta / (double)timeDelta  ) * 100d ) / numCPUs;
	    long cpuUserDelta = pi.getUserTime() - dpi.getUserTime();
        double cpuUserPercent = (((double)cpuUserDelta / (double)timeDelta  ) * 100d ) / numCPUs;
	    
	    
	    long readDelta = pi.getIoReads() - dpi.getIoReads();
	    long writeDelta = pi.getIoWrites() - dpi.getIoWrites();
	    long otherDelta = pi.getIoOther() - dpi.getIoOther();
	    long readBytesDelta = pi.getIoReadBytes() - dpi.getIoReadBytes();
        long writeBytesDelta = pi.getIoWriteBytes() - dpi.getIoWriteBytes();
        long otherBytesDelta = pi.getIoOtherBytes() - dpi.getIoOtherBytes();
        
	    long pfDelta = pi.getPageFaults() - dpi.getPageFaults();
	    
	    //double privPercent = ((double) pi.getPrivateUsage() / (double) maxReal) * 100d;
	   // double wsPercent = ((double) pi.getWorkingSetSize() / (double) maxReal) * 100d;
	    double wsDelta = ((double) pi.getWorkingSetSize() - (double) dpi.getWorkingSetSize()) / 1024d; 
	    //double privDelta = ((double) pi.getPrivateUsage() - (double) dpi.getPrivateUsage()) / 1024d;
	    
	    
	    if (parent == null) {
	        parentProcess.setText(Long.toString(pi.getParentPID()));
	    } else {
	        parentProcess.setText(parent.processId + " (" + parent.processName + ")" );
	    }
	    
	    if (processID != null) {
	        processID.setText(Utilities.format(selectedPD.processId));
	    }
	    imageName.setText(selectedPD.processName);
	    imageType.setText(selectedPD.imageType);
	    
	    
	    threadCount.setText(Utilities.format(pi.getThreadCount()));
	    handleCount.setText(Utilities.format(pi.getHandleCount()));
	    
	    totalKernelTime.setText(Utilities.format(pi.getKernelTime()));
	    totalUserTime.setText(Utilities.format(pi.getUserTime()));
	    totalCPUTime.setText(Utilities.format(pi.getTotalCPU()));
	    
	    ioReads.setText(Utilities.format(pi.getIoReads()));
	    ioWrites.setText(Utilities.format(pi.getIoWrites()));
	    ioOther.setText(Utilities.format(pi.getIoOther()));
	    
	    ioReadBytes.setText(Utilities.formatBytes(pi.getIoReadBytes()));
        ioWriteBytes.setText(Utilities.formatBytes(pi.getIoWriteBytes()));
        ioOtherBytes.setText(Utilities.formatBytes(pi.getIoOtherBytes()));
        
        ioReadsDelta.setText(Utilities.format(pi.getIoReads() - dpi.getIoReads()));
        ioWritesDelta.setText(Utilities.format(pi.getIoWrites() - dpi.getIoWrites()));
        ioOtherDelta.setText(Utilities.format(pi.getIoOther() - dpi.getIoOther()));
        
        ioReadBytesDelta.setText(Utilities.format(pi.getIoReadBytes() - dpi.getIoReadBytes()));
        ioWriteBytesDelta.setText(Utilities.format(pi.getIoWriteBytes() - dpi.getIoWriteBytes()));
        ioOtherBytesDelta.setText(Utilities.format(pi.getIoOtherBytes() - dpi.getIoOtherBytes()));
        
        pageFaults.setText(Utilities.format(pi.getPageFaults()));
        pageFaultsDelta.setText(Utilities.format(pfDelta));
        wsSize.setText(Utilities.formatBytes(pi.getWorkingSetSize()));
        peakWSSize.setText(Utilities.formatBytes(pi.getPeakWorkingSetSize()));
        privateUsage.setText(Utilities.formatBytes(pi.getPrivateUsage()));
        pageFileSize.setText(Utilities.formatBytes(pi.getPageFileUsage()));
	    
        /*double pfDelta2 = pfDelta;
        if (pfDelta2 > pfMax) {
            pfMax *= 10;
            pfFactor *= 10;
        }
        pfDelta2 /= pfFactor;
        
        double ioDelta2 = ioDelta;
        if (ioDelta2 > ioMax) {
            ioMax *= 10;
            ioFactor *= 10;
        }
        ioDelta2 /= ioFactor;*/
        cpuGraph.addObservation(cpuKernelPercent, cpuUserPercent);
        ioGraph.addObservation(readDelta, writeDelta, otherDelta);
        ioBytesGraph.addObservation(readBytesDelta, writeBytesDelta, otherBytesDelta);
        memoryGraph.addObservation(pi.getWorkingSetSize(), pi.getPrivateUsage());
        wssGraph.addObservation(wsDelta);
        pageFaultsGraph.addObservation(pfDelta);
        /*cpuGraph.update(cpuDeltaPercent, "CPU Usage", Utilities.format(cpuDeltaPercent, 2) + "%");
        pageFaultsGraph.update(pfDelta2, "Page Faults",Integer.toString((int) pfDelta) );
        ioGraph.update(ioDelta2, "I/O Activity",Integer.toString((int) ioDelta) );
        
        privateGraph.update(privPercent, "Private Size", Utilities.formatBytes(pi.getPrivateUsage()));
        wsGraph.update(wsPercent,"Working Set Size", Utilities.formatBytes(pi.getWorkingSetSize()));
        int deltav = (int) (wsDelta < 0 ? wsDelta * -1 : wsDelta) / 10;
        wsDeltaGraph.update(deltav, "WS Size Delta", Utilities.format((int)wsDelta) + "K");*/
        
	    selectedPDlast = new OSProcessInfo(pi);
	}
	public void resetPanel() {
		
		
		
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
		
		processTableModel.showZeroCPU = 
			(processTableModel.showZeroCPU ? false: true);
	}
	/**
	 * @return the panelBuilt
	 */
	public boolean isPanelBuilt() {
		return panelBuilt;
	}


	

    
    private BroadcastMessage getBroadcastMessage(long pid) {
        
        Collection<BroadcastMessage> messages = scanner.getHostMap().values();
        
        for (BroadcastMessage msg : messages) {
            if (msg.processID == (int) pid) {
                return msg;
            }
        }
        return null;
    }


    public final boolean isPaused() {
        return paused;
    }

 
    public final void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * @return the processTableModel
     */
    public final ProcessDataModel getProcessTableModel() {
        return processTableModel;
    }
    
    protected void promptMenu() {
        final ProcessData pd = processTableModel.getProcess(selectedProcess);
        
        JPopupMenu queryPopup = new JPopupMenu("Status");
        
        if (pd != null && OSSystemInfo.isOperational()) {
            JMenuItem popupRefresh = new JMenuItem("Kill Process " + pd.processId + "(" +
                    pd.processName + ")");
            popupRefresh.setMnemonic('K');
            popupRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
            popupRefresh.setEnabled(true);
            queryPopup.add(popupRefresh);
            popupRefresh.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    
                    long rc = OSSystemInfo.killProcess(pd.processId);
                    
                    switch ((int)rc) {
                    case 0:
                        JOptionPane.showMessageDialog(instance, 
                                "Process " + pd.processId +  "(" +
                                        pd.processName + ") Killed", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case 5:
                        JOptionPane.showMessageDialog(instance, 
                                "Not Authorized to Kill Process " + pd.processId +  "(" +
                                pd.processName + ")", "Failed", 
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    default:
                        JOptionPane.showMessageDialog(instance, 
                                "Kill of Process " + pd.processId +  "(" +
                                pd.processName + ") Failed, Error Code is " +
                                        rc, "Failed", JOptionPane.ERROR_MESSAGE);   
                    }
                }
            });
        }
            JMenuItem popupClose = new JMenuItem("Close Details");
            popupClose.setMnemonic('C');
            popupClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
            popupClose.setEnabled(true);
            queryPopup.add(popupClose);

            popupClose.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectedProcess = -1;
                    selectedPDlast = new OSProcessInfo();
                    detailPanel.removeAll(); 
                    sp.setDividerLocation(1.0);
                    sp.setResizeWeight(1.0);
                    instance.updateUI();
                }
            });
            
            Point p = this.getMousePosition();
            queryPopup.setBorderPainted(true);
            if (p != null) {
                queryPopup.show(instance, p.x, p.y);
            }
        
    }
	
}
class ProcessTextRenderer extends JTextField implements TableCellRenderer {

    private static final long serialVersionUID = 1L;
	protected ProcessDataModel tdm;
	//private Font italic;
	private Font bold;
	protected UIOptions opt;
	private BroadCastScanner scanner;
	public ProcessTextRenderer(TableModel tdm, UIOptions opt) {
		
	    
		bold = this.getFont();
		bold = bold.deriveFont(Font.BOLD);
		this.opt = opt;
		this.setFont(bold);
		this.tdm = (ProcessDataModel) tdm;
		this.scanner = BroadCastScanner.getInstance();
		
	}

	public Component getTableCellRendererComponent(final JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (value instanceof String ) {
			final String val = (String) value;
			
			if (row % 2 == 1) {
				this.setBackground(Color.LIGHT_GRAY);
			} else {
				this.setBackground(Color.WHITE);
			}
			
			if (column == ProcessColumnData.COL_NAME) {
                final Object obj = tdm.getValueAt(row, ProcessColumnData.COL_ID);
                if (obj instanceof Long) {
                    if ( scanner.isConnectable((Long) obj)) {
                       this.setForeground(Color.RED);
                       this.setToolTipText("This is a Java Process that has an active Probe");
                    }
                    
                }
            }
			        

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
class PTable extends JTable {
	
    private static final long serialVersionUID = 1L;	
	private TableModel mod;
	private UIOptions options;
	
	public PTable(TableModel mod, UIOptions options) {
		
		super(mod);
		this.mod = mod;
		this.options = options;
		
		super.setRowHeight(super.getRowHeight() + options.getRowHeightAdjustment());
	}
	public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == ProcessColumnData.COL_PERCENT_CPU) {
            return new ProgressBarRenderer(options);
        }
    	return new ProcessTextRenderer(mod, options);

    }
}


class ProcessColumnListener extends MouseAdapter {

	private JTable table;
	private ProcessDataModel model;
	
	public ProcessColumnListener(JTable table, ProcessDataModel model) {
		
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
            //int index = column.getModelIndex();
            JLabel renderer = (JLabel)column.getHeaderRenderer();
            renderer.setIcon(model.getColumnIcon(i));
        }
        
        table.getTableHeader().repaint();

        model.sortData();
        table.tableChanged(new TableModelEvent(model));
        table.repaint();
	}
}
class ProcessTableCellRenderer extends DefaultTableCellRenderer {

	private Color defaultColor;
	private static final long serialVersionUID = 1L;
	public ProcessTableCellRenderer() {
		defaultColor = this.getBackground();
	}
	public void setValue(Object value) {
	    Logger.getLogger().debug("name = " + value.getClass().getName());
		if (value instanceof IconData) {
			IconData ivalue = (IconData)value;
			setIcon(ivalue.m_icon);
			setText(ivalue.m_data.toString());
			this.setBackground(defaultColor); //new Color(225,225,225));
		}
		else {
			super.setValue(value);
			setText(value.toString());
			this.setBackground(defaultColor); //new Color(225,225,225));
		}
	}
}

