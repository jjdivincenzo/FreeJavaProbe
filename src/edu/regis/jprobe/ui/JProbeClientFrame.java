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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import edu.regis.jprobe.model.JavaVersion;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.ProbeCommunicationsException;
import edu.regis.jprobe.model.ProbeCommunicationsManager;
import edu.regis.jprobe.model.ProbeRequest;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.RecordingManager;
import edu.regis.jprobe.model.Utilities;

/**
 * @author jdivince
 *
 * This is the internal frame for a connected or playback client.
 */
public class JProbeClientFrame extends JInternalFrame implements Thread.UncaughtExceptionHandler{
	
    private static final long serialVersionUID = 1L;
    private String probeName;
	private String hostName;
	private String hostIP;
	private String clientName;
	private String userid;
	private String password;
	private String playBackFile;
	private String ourIPaddress;
	private int hostPort;
	private UIOptions options;
	private JProbeClientFrame instance;
	private ProbeCommunicationsManager commMgr;
	private Logger logger;
	private RecordingManager recMgr;
	
	protected boolean isConnected = false;
	private boolean paused = false;
	private boolean hasEnded = false;
	private boolean resetOptions = false;
	private boolean recording = false;
	private boolean playbackMode = false;
	private boolean playbackInProgress = false;
	private boolean probeIsLocal;
	private boolean debugProbe = false;
			
	private int updateInterval = 1000;
	protected int playbackInterval = 1000;
	private int numberOfSamples = 100;
	private int buttonWidth=80;
	private int buttonHeight=40;
	private int lastTick = 0;
	
	
	//Swing Components
	private JTextField statusMsg;
	private JTabbedPane tabPerf;
	private JPanel buttonPanel;
	private JButton prButton;
	private JButton shutdownBtn;
	private JButton gcBtn;
	private JButton terminateBtn;
	private JButton getStackTrace;
	private JButton dumpHeap;
	private JButton recordBtn;
	private JButton forward1Btn;
	private JButton backward1Btn;
	private JSlider playbackSlider;
	private JSpinner playbackSpeed;
	private TimerThread timer;
	private ActionListener updateAL;
	
	//Swing Menu items
	protected JCheckBoxMenuItem showOverview;
	protected JCheckBoxMenuItem showCPUHistory;
	protected JCheckBoxMenuItem showThreadCPU;
	protected JCheckBoxMenuItem showMemoryHistory;
	protected JCheckBoxMenuItem showPoolHistory;
	protected JCheckBoxMenuItem showIOCount;
	protected JCheckBoxMenuItem showIOBytes;
	protected JCheckBoxMenuItem showPaging;
	protected JCheckBoxMenuItem showMemStats;
	protected JCheckBoxMenuItem showProperties;
	protected JCheckBoxMenuItem showClassInfo;
	protected JCheckBoxMenuItem showJMXBeans;
	protected JCheckBoxMenuItem showLocks;
	protected JCheckBoxMenuItem showGC;
	protected JMenuItem reconnectItem;
	
	//Tabbed Panel Child Panels
	private MemoryStatsPanel memStatsPanel;
	private JVMPropertiesPanel propertiesPanel;
	private ClassInformationPanel classInfoPanel;
	private MemoryPoolHistoryPanel poolHistoryPanel;
	private MemoryHistoryPanel memoryHistoryPanel;
	private CPUHistoryPanel cpuHistoryPanel;
	private IOCountHistoryPanel ioCountHistoryPanel;
	private IOBytesHistoryPanel ioBytesHistoryPanel;
	private PagingHistoryPanel pagingHistoryPanel;
	private ThreadCPUPanel threadCPUPanel;
	private OverviewPanel overviewPanel;
	private JMXBeanPanel jmxBeanPanel;
	private BlockingLocksPanel locksPanel;
	private GarbageCollectorsPanel gcPanel;
	protected JProbeUI parent;
		
	public JProbeClientFrame(File recordingFile, 
            UIOptions uio,
            JProbeUI parent) throws ProbeCommunicationsException {
	   
	    super("Probe", true,true, true, true);
        logger = Logger.getLogger();
	    this.parent = parent;
        this.numberOfSamples = uio.getSampleRate();
        instance = this;
        
        this.probeName = "PlayBack";
        this.hostName = "N/A";
        this.hostIP = "N/A";
        this.hostPort = 0;
        this.options = uio;
        this.userid = "N/A";
        this.password = "N/A";
        
      
        logger.info("Starting Client Playback for " + recordingFile.getAbsolutePath());
        this.setTitle("Playback of Session[" + recordingFile.getAbsolutePath() + "]");
        
        this.playBackFile = recordingFile.getAbsolutePath();
        this.playbackMode = true;
	    init(playbackMode);
	    
	}
	
	public JProbeClientFrame(String name, 
							 String host, 
							 String hostIP, 
							 int port,
							 boolean isRemote,
							 String userid,
							 String password,
							 UIOptions uio,
							 JProbeUI parent) throws ProbeCommunicationsException {
		
		super("Probe", true,true, true, true);
		logger = Logger.getLogger();
		this.probeName = name;
		this.hostName = host;
		this.hostIP = hostIP;
		this.hostPort = port;
		this.options = uio;
		this.userid = userid;
		this.password = password;
		this.parent = parent;
		this.numberOfSamples = uio.getSampleRate();
		instance = this;
		
		try {
            this.ourIPaddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            this.ourIPaddress = "127.0.0.1";
            logger.logException(e, this);
        }
		
		commMgr = new ProbeCommunicationsManager(this.hostIP, this.hostPort, this.userid, this.password);
		logger.info("Starting Client Frame for " + name + " at " + host + ":" + hostIP + ":" + port);
		
 		
		if (isRemote) {
			this.setTitle("[Remote] - " + hostIP + 
					":" + hostPort);
		}else {
			this.setTitle("[" + probeName + "] - " + hostName + 
						"(" + hostIP + "):" + hostPort);
		}
		
		try {
			clientName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			clientName = "localhost";
		}
		
		probeIsLocal = isProbeLocal(hostName, clientName);
		logger.info("Client name is: " + clientName + ", host name is " + hostName);
		init(false);
		
	}
	private void init(boolean playBackMode) throws ProbeCommunicationsException {
	    
	    Icon icon = IconManager.getIconManager().getFrameIcon();   
        if (icon != null) setFrameIcon(icon);
        
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		//Create the Tabbed Panel and all of the tab components
		tabPerf = new JTabbedPane(JTabbedPane.TOP);
		tabPerf.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		buttonPanel = createButtonPanel(playBackMode);
		createMenu();
		
		if (!playBackMode) {
		    connect();
		} else {
		    openPlaybackStream();
		    dumpHeap.setEnabled(false);
		    getStackTrace.setEnabled(false);
		    playbackInProgress = true;
		    recordBtn.setText("Stop");
		   
		}
		showPanels();		
		setLayout(new GridBagLayout());
		GridBagConstraints cc = new GridBagConstraints();
		cc.fill = GridBagConstraints.BOTH;
		cc.gridx=0;
		cc.gridy=0;
		cc.gridwidth=1;
		cc.gridheight=1;
		cc.weightx=1;
		cc.weighty=1;
		add(tabPerf,cc); 
		
		cc.weightx=.10;
		cc.weighty=.10;
		cc.gridy=1;
		add(buttonPanel,cc); 
		
		updateAL = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        update();
		        
		    }
		};
				
		timer = new TimerThread(updateAL, updateInterval, probeName);
		timer.setUncaughtExceptionHandler(this);
		timer.setRunning(true);
		
		
		
		addInternalFrameListener( new InternalFrameAdapter () {
			
			public void internalFrameClosing(InternalFrameEvent e) {
				disconnect();
				closeClient();
			}
		});
		
		
	}

	public boolean connect() throws ProbeCommunicationsException {
		
		
		
		commMgr.connect();
					      
		isConnected = true;
	    statusMsg.setText("Connected To " + probeName + " - " + hostName + ":" + hostPort);
	    logger.info("Connected To " + probeName + " - " + hostName + ":" + hostPort);
	    reconnectItem.setEnabled(false);  
	    recordBtn.setEnabled(true);
	    return true; 
		
	}
	
	public void disconnect() {
		
		if (isConnected) {
            
			isConnected = false;
            logger.info("Client Frame " + probeName + " at " + hostName + ":" + hostIP + ":" + hostPort + 
                    " has disconnected");         
            timer.setRunning(false);
            this.setTitle("(Disconnected)" + this.getTitle());
            try {
				commMgr.disconnect();
			} catch (ProbeCommunicationsException e) {
				logger.logException(e,this);
			}
			reconnectItem.setEnabled(true);
	       
		}
		
	}
	protected void resetDisplay() {
	    
	    logger.debug("In resetDisplay for " + probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		
		if (overviewPanel != null) {
		    overviewPanel.resetPanel();
		}
		
		if (cpuHistoryPanel != null) {
		    cpuHistoryPanel.resetPanel();
		}
		
		if (threadCPUPanel != null) {
		    threadCPUPanel.resetPanel();
		}
		
		if (memoryHistoryPanel != null) {
		    memoryHistoryPanel.resetPanel();
		}
		
		if (poolHistoryPanel != null) {
		    poolHistoryPanel.resetPanel();
		}
		
		if (ioCountHistoryPanel != null) {
		    ioCountHistoryPanel.resetPanel();
		}
		
		if (ioBytesHistoryPanel != null) {
		    ioBytesHistoryPanel.resetPanel();
		}
		
		if (memStatsPanel != null) {
		    memStatsPanel.resetPanel();
		}
		
		if (propertiesPanel != null) {
		    propertiesPanel.resetPanel();
		}
		
		if (classInfoPanel != null) {
		    classInfoPanel.resetPanel();
		}
		
		if (pagingHistoryPanel != null) {
		    pagingHistoryPanel.resetPanel();
		}
		this.repaint();
		
		
	}
	protected JPanel createButtonPanel(boolean playBackMode) {


		Dimension buttonSize = new Dimension(30,40);
		Dimension largeTextSize = new Dimension(100,30);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setSize(buttonSize);
		buttonPanel.setLayout(new GridBagLayout());
		buttonPanel.setBorder((new TitledBorder( new EtchedBorder(), "")));
		GridBagConstraints cb = new GridBagConstraints();
		cb.insets= new Insets(1,1,1,3);
		cb.fill = GridBagConstraints.HORIZONTAL;
		cb.anchor = GridBagConstraints.CENTER;
		//int p3_gridy = 0;

		// Close button and event handler
		JButton closeButton = new JButton();
		closeButton.setText("Close");
		closeButton.setToolTipText("Disconnect and Close The Frame");
		closeButton.setSize(buttonWidth, buttonHeight);
		cb.gridx=0;
		cb.gridy=0;
		cb.gridwidth=1;
		cb.gridheight=1;
		cb.weightx=0;
		cb.weighty=.5;
		buttonPanel.add(closeButton,cb);
		closeButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						disconnect();
						closeClient();
					 }
				});

		//Pause button and event handler
		prButton = new JButton();
		prButton.setText("Pause");
		prButton.setToolTipText("Pause/Resume Auto Update");
		prButton.setSize(buttonWidth, buttonHeight);
		cb.gridx=1;
		cb.gridy=0;
		cb.gridwidth=1;
		cb.gridheight=1;
		cb.weightx=0;
		cb.weighty=.5;
		buttonPanel.add(prButton,cb);
		prButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						pauseResume();
					 }
				});
		
		
		
		// Shutdown button and event handler
		shutdownBtn = new JButton();
		shutdownBtn.setText("Shutdown Probe");
		shutdownBtn.setToolTipText("Terminate the Probe on the Target JVM");
		shutdownBtn.setSize(buttonWidth, buttonHeight);
		cb.gridx=2;
		cb.gridy=0;
		cb.gridwidth=1;
		cb.gridheight=1;
		//cb.weightx=0;
		buttonPanel.add(shutdownBtn,cb);
		shutdownBtn.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    	stopProbe();
					        
					     
					 }
				});
		
		//GC button and event handler
		gcBtn = new JButton();
		gcBtn.setText("Request a GC");
		gcBtn.setToolTipText("Issue System.gc() on the Target JVM");
		gcBtn.setSize(buttonWidth, buttonHeight);
		cb.gridx=3;
		cb.gridy=0;
		cb.gridwidth=1;
		cb.gridheight=1;
		
		buttonPanel.add(gcBtn,cb);
		gcBtn.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    	sendGC();
					        
					     
					 }
				});
		
		//GC button and event handler
		getStackTrace = new JButton();
		getStackTrace.setText("Dump Stack");
		getStackTrace.setToolTipText("Dump the Stack Trace on the Target JVM");
		getStackTrace.setSize(buttonWidth, buttonHeight);
		cb.gridx=4;
		cb.gridy=0;
		cb.gridwidth=1;
		cb.gridheight=1;
		
		buttonPanel.add(getStackTrace,cb);
		getStackTrace.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    	getStackTrace();
					        
					     
					 }
				});
		
		//Dump Heap Button
		dumpHeap = new JButton();
		dumpHeap.setText("Dump Heap");
		dumpHeap.setToolTipText("Dump the Java Heap on the Target JVM");
		dumpHeap.setSize(buttonWidth, buttonHeight);
        cb.gridx=5;
        cb.gridy=0;
        cb.gridwidth=1;
        cb.gridheight=1;
        
        buttonPanel.add(dumpHeap,cb);
        dumpHeap.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                            dumpHeap();
                            
                         
                     }
                });
//		GC button and event handler
		terminateBtn = new JButton();
		terminateBtn.setText("Terminate JVM");
		terminateBtn.setToolTipText("Issue System.exit() to Terminate the Target JVM");
		terminateBtn.setSize(buttonWidth, buttonHeight);
		cb.gridx=6;
		cb.gridy=0;
		cb.gridwidth=1;
		cb.gridheight=1;
		
		buttonPanel.add(terminateBtn,cb);
		terminateBtn.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    	sendExit();
					        
					     
					 }
				});
		
		recordBtn = new JButton();
		if (playBackMode) {
		    recordBtn.setText("Play");
		    recordBtn.setToolTipText("Start Playing This Session");
		    recordBtn.setEnabled(true);
		} else {
		    recordBtn.setText("Record");
            recordBtn.setToolTipText("Record This Session for Playback Later");
            recordBtn.setEnabled(true);
		}
		recordBtn.setSize(buttonWidth, buttonHeight);
        cb.gridx=7;
        cb.gridy=0;
        cb.gridwidth=1;
        cb.gridheight=1;
        
        buttonPanel.add(recordBtn,cb);
        recordBtn.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        if (playbackMode) {
                            if (!playbackInProgress) {
                                resetDisplay();
                                openPlaybackStream();
                                statusMsg.setText("Playback Started");
                                timer.setInterval(1000);
                                //lastPlaybackResponse = 0;
                                timer.setRunning(true);
                                recordBtn.setText("Stop");
                                playbackInProgress = true;
                            } else {
                                timer.setRunning(false);
                                closePlaybackFile();
                                recordBtn.setText("Play");
                                statusMsg.setText("Playback Stopped");
                                playbackInProgress = false;
                            }
                            
                        } else {
                            doRecord();
                        }   
                         
                     }
                });
		
		//if the probe is on a remote machine, do not allow them to
		// issue shutdown or gc calls...
        
        if (playbackMode) {
            playbackSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
            playbackSlider.setToolTipText("Current Playback Position");
            playbackSlider.setBorder(new EtchedBorder());
            cb.gridx=8;
            cb.gridy=0;
            cb.gridwidth=4;
            cb.gridheight=1;
            cb.weightx=1;
            buttonPanel.add(playbackSlider,cb);
            playbackSlider.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    if (paused) {
                        lastTick = playbackSlider.getValue();
                        
                    }
                    
                }
                
            });
            forward1Btn = new JButton(">");
            forward1Btn.setToolTipText("Next Sample");
            cb.gridx=12;
            cb.gridy=0;
            cb.gridwidth=1;
            cb.gridheight=1;
            cb.weightx=0;
            buttonPanel.add(forward1Btn,cb);
            backward1Btn = new JButton("<");
            backward1Btn.setToolTipText("Previous Sample");
            cb.gridx=13;
            cb.gridy=0;
            cb.gridwidth=1;
            cb.gridheight=1;
            cb.weightx=0;
            buttonPanel.add(backward1Btn,cb);
            JLabel speedLbl = new JLabel("Playback Speed(ms)");
            cb.gridx=14;
            cb.gridy=0;
            cb.gridwidth=1;
            cb.gridheight=1;
            cb.weightx=0;
            buttonPanel.add(speedLbl,cb);
            SpinnerNumberModel model = new SpinnerNumberModel(1000, 100, 1000, 100);
            playbackSpeed = new JSpinner(model);
            cb.gridx=15;
            cb.gridy=0;
            cb.gridwidth=1;
            cb.gridheight=1;
            cb.weightx=0;
            buttonPanel.add(playbackSpeed,cb);
            ActionListener frameListener = new ActionListener()
            {
                public void actionPerformed (ActionEvent e)
                {
                    if (!paused) {
                        pauseResume();
                    }
                    if (e.getSource() == forward1Btn) {
                        updateByStep(lastTick + 1);
                    } else {
                        updateByStep(lastTick - 1);
                    }
                    
                 }
            };
            backward1Btn.addActionListener(frameListener);
            forward1Btn.addActionListener(frameListener);
        } else {
            cb.gridx=8;
            cb.gridy=0;
            cb.gridwidth=4;
            cb.gridheight=1;
            buttonPanel.add(new JLabel(" "),cb);
        }
		
		if (probeIsLocal) {
			shutdownBtn.setEnabled(true);
			gcBtn.setEnabled(true);
			terminateBtn.setEnabled(true);
		} else {
			shutdownBtn.setEnabled(false);
			gcBtn.setEnabled(false);
			terminateBtn.setEnabled(false);
		}
		
		
		statusMsg = new JTextField();
		statusMsg.setBorder(new BevelBorder(BevelBorder.LOWERED));
		Font fontTF = statusMsg.getFont();
		fontTF = fontTF.deriveFont(Font.BOLD);
		statusMsg.setFont(fontTF);
		cb.gridx=0;
		cb.gridy=1;
		cb.gridwidth=16;
		cb.gridheight=1;
		cb.weightx=1; 
		cb.weighty=.5; 
		statusMsg.setEditable(false);
		statusMsg.setPreferredSize(largeTextSize);
		buttonPanel.add(statusMsg,cb);
		
		statusMsg.setForeground(Color.RED);
		
		return buttonPanel;

	}
	private void createMenu() {
	    
	    //Create the File Menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
						
		//Add the close
		reconnectItem = new JMenuItem("Reconnect");
		reconnectItem.setMnemonic('R');
		reconnectItem.setEnabled(false);
		reconnectItem.setToolTipText("To Reconnect to this JVM");
		
		
		//create a listener for it.
		reconnectItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						try {
							connect();
							resetDisplay();
							//timer.start();
							timer.setRunning(true);
							recordBtn.setEnabled(true);
						} catch (ProbeCommunicationsException exc) {
							JOptionPane.showMessageDialog(instance,exc.getMessage(), 
									"Reconnect Failed",JOptionPane.ERROR_MESSAGE);
							return;
						} catch (Exception exc) {
                            JOptionPane.showMessageDialog(instance,
                                    exc.getClass().getName() + " : " + exc.getMessage(), 
                                    "Reconnect Error",JOptionPane.ERROR_MESSAGE);
                            exc.printStackTrace();
                            return;
						}
						// if the probe is on a remote machine, do not allow them to
						// issue shutdown or gc calls...
						
						if (probeIsLocal) {
							shutdownBtn.setEnabled(true);
							gcBtn.setEnabled(true);
							terminateBtn.setEnabled(true);
						} else {
							shutdownBtn.setEnabled(false);
							gcBtn.setEnabled(false);
							terminateBtn.setEnabled(false);
						}
						prButton.setEnabled(true);
						getStackTrace.setEnabled(true);
						recordBtn.setEnabled(true);
					}
				}
				);
		
		fileMenu.add(reconnectItem);
		
		JCheckBoxMenuItem debugItem = new JCheckBoxMenuItem("Turn on Probe Debug");
		debugItem.setMnemonic('d');
		debugItem.setEnabled(true);
		debugItem.setSelected(false);
		debugItem.setToolTipText("To Turn on Probe Debug Messages");
        
        
        //create a listener for it.
		debugItem.addActionListener(
                new ActionListener()
                {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        debugProbe = (debugProbe ? false : true);
                    }
                }
        );
		fileMenu.add(debugItem);
		//Add the close
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.setMnemonic('C');
		closeItem.setToolTipText("To Close This Connection");
		
		
		//create a listener for it.
		closeItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						disconnect();
						closeClient();
					}
				}
				);
		
		fileMenu.add(closeItem);
		
		//Add the print 
		JMenuItem printItem = new JMenuItem("Print");
		printItem.setMnemonic('P');
		printItem.setToolTipText("To Print");
		printItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		
		//create a listener for it.
		printItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						print();
					}
				}
				);
		//		 Add the Close
		//fileMenu.add(printItem); //<<-- Not done yet
		
		// create the View Menu
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		
				
		showOverview = new JCheckBoxMenuItem("Overview Tab");
		showOverview.setMnemonic('O');
		showOverview.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		showOverview.setEnabled(true);
		showOverview.setSelected(options.isShowOverviewTab());
		showOverview.setToolTipText("Show/Hide Overview Tab");
		viewMenu.add(showOverview);
		showOverview.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowOverviewTab(showOverview.isSelected());
						showPanels();
					}
				}
				);
		
		showLocks = new JCheckBoxMenuItem("Monitor Locks Tab");
		showLocks.setMnemonic('L');
		showLocks.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		showLocks.setEnabled(true);
		showLocks.setSelected(options.isShowLocksTab());
		showLocks.setToolTipText("Show/Hide Monitor Locks Tab");
        viewMenu.add(showLocks);
        showLocks.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        options.setShowLocksTab(showLocks.isSelected());
                        showPanels();
                    }
                }
                );
		
		showCPUHistory = new JCheckBoxMenuItem("CPU History Tab");
		showCPUHistory.setMnemonic('C');
		showCPUHistory.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		showCPUHistory.setEnabled(true);
		showCPUHistory.setSelected(options.isShowCPUHistoryTab());
		showCPUHistory.setToolTipText("Show/Hide CPU History Tab");
		viewMenu.add(showCPUHistory);
		showCPUHistory.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowCPUHistoryTab(showCPUHistory.isSelected());
						showPanels();
					}
				}
				);
		
		showThreadCPU = new JCheckBoxMenuItem("Thread CPU Tab");
		showThreadCPU.setMnemonic('T');
		showThreadCPU.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		showThreadCPU.setEnabled(true);
		showThreadCPU.setSelected(options.isShowThreadCPUTab());
		showThreadCPU.setToolTipText("Show/Hide Thread CPU Tab");
		viewMenu.add(showThreadCPU);
		showThreadCPU.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowThreadCPUTab(showThreadCPU.isSelected());
						showPanels();
					}
				}
				);
		
		showMemoryHistory = new JCheckBoxMenuItem("Memory History Tab");
		showMemoryHistory.setMnemonic('M');
		showMemoryHistory.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		showMemoryHistory.setEnabled(true);
		showMemoryHistory.setSelected(options.isShowMemoryHistoryTab());
		showMemoryHistory.setToolTipText("Show/Hide Memory History Tab");
		viewMenu.add(showMemoryHistory);
		showMemoryHistory.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowMemoryHistoryTab(showMemoryHistory.isSelected());
						showPanels();
					}
				}
				);
		
		showPoolHistory = new JCheckBoxMenuItem("Memory Pool History Tab");
		showPoolHistory.setMnemonic('P');
		showPoolHistory.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		showPoolHistory.setEnabled(true);
		showPoolHistory.setSelected(options.isShowMemoryPoolHistoryTab());
		showPoolHistory.setToolTipText("Show/Hide Memory Pool History Tab");
		viewMenu.add(showPoolHistory);
		showPoolHistory.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowMemoryPoolHistoryTab(showPoolHistory.isSelected());
						showPanels();
					}
				}
				);
		showIOCount = new JCheckBoxMenuItem("IO Count History Tab");
		showIOCount.setMnemonic('I');
		showIOCount.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		showIOCount.setEnabled(true);
		showIOCount.setSelected(options.isShowIOCountHistoryTab());
		showIOCount.setToolTipText("Show/Hide IO Count History Tab");
		viewMenu.add(showIOCount);
		showIOCount.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowIOCountHistoryTab(showIOCount.isSelected());
						showPanels();
					}
				}
				);
		
		showIOBytes = new JCheckBoxMenuItem("IO Transfer Bytes History Tab");
		showIOBytes.setMnemonic('I');
		showIOBytes.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		showIOBytes.setEnabled(true);
		showIOBytes.setSelected(options.isShowIOBytesHistoryTab());
		showIOBytes.setToolTipText("Show/Hide IO Count History Tab");
		viewMenu.add(showIOBytes);
		showIOBytes.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowIOBytesHistoryTab(showIOBytes.isSelected());
						showPanels();
					}
				}
				);
		
		showPaging = new JCheckBoxMenuItem("Paging History Tab");
		showPaging.setMnemonic('P');
		showPaging.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		showPaging.setEnabled(true);
		showPaging.setSelected(options.isShowPagingHistoryTab());
		showPaging.setToolTipText("Show/Hide Paging History Tab");
		viewMenu.add(showPaging);
		showPaging.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowPagingHistoryTab(showPaging.isSelected());
						showPanels();
					}
				}
				);
		
		
		showMemStats = new JCheckBoxMenuItem("Memory Stats Tab");
		showMemStats.setMnemonic('S');
		showMemStats.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		showMemStats.setEnabled(true);
		showMemStats.setSelected(options.isShowMemoryStatsTab());
		showMemStats.setToolTipText("Show/Hide Memory Stats Tab");
		viewMenu.add(showMemStats);
		showMemStats.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowMemoryStatsTab(showMemStats.isSelected());
						showPanels();
					}
				}
				);
		
		showGC = new JCheckBoxMenuItem("Garbage Collector Tab");
		showGC.setMnemonic('G');
		showGC.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		showGC.setEnabled(true);
		showGC.setSelected(options.isShowMemoryStatsTab());
		showGC.setToolTipText("Show/Hide Garbage Collector Tab");
        viewMenu.add(showGC);
        showGC.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        options.setShowGCTab(showGC.isSelected());
                        showPanels();
                    }
                }
                );
		
		
		showProperties = new JCheckBoxMenuItem("JVM Properties Tab");
		showProperties.setMnemonic('J');
		showProperties.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_J, ActionEvent.CTRL_MASK));
		showProperties.setEnabled(true);
		showProperties.setSelected(options.isShowJVMPropertiesTab());
		showProperties.setToolTipText("Show/Hide JVM Properties Tab");
		viewMenu.add(showProperties);
		showProperties.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowJVMPropertiesTab(showProperties.isSelected());
						showPanels();
					}
				}
				);
		
		
		showClassInfo = new JCheckBoxMenuItem("Class Info Tab");
		showClassInfo.setMnemonic('I');
		showClassInfo.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		showClassInfo.setEnabled(true);
		showClassInfo.setSelected(options.isShowClassInfoTab());
		showClassInfo.setToolTipText("Show/Hide Class Info Tab");
		viewMenu.add(showClassInfo);
		showClassInfo.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowClassInfoTab(showClassInfo.isSelected());
						showPanels();
					}
				}
				);
		
		showJMXBeans = new JCheckBoxMenuItem("JMX Beans");
		showJMXBeans.setMnemonic('B');
		showJMXBeans.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		showJMXBeans.setEnabled(true);
		showJMXBeans.setSelected(options.isShowJMXBeansTab());
		showJMXBeans.setToolTipText("Show/Hide JMX Bean Info Tab");
		viewMenu.add(showJMXBeans);
		showJMXBeans.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowJMXBeansTab(showJMXBeans.isSelected());
						showPanels();
					}
				}
				);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar (menuBar);
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
	}

	/**
	 * This method will display/hide any tab panel that is not selected in the menu
	 *
	 */
	private void showPanels() {
		
		if (showOverview.isSelected()) {
			if (overviewPanel == null) {
				overviewPanel = new OverviewPanel(this, options, commMgr);
				tabPerf.addTab("Overview", null, overviewPanel, 
				        "This is the Performance Tab for CPU, Memory and Thread Usage");
			}
		} else {
			if (overviewPanel != null) {
				tabPerf.remove(overviewPanel);
				overviewPanel.removeAll();
				overviewPanel = null;
			}
		}
		
		if (showLocks.isSelected()) {
            if (locksPanel == null) {
                locksPanel = new BlockingLocksPanel(this);
                tabPerf.addTab("Monitor Locks", null, locksPanel, 
                        "This tab Shows Observed Locks Contentention between Threads");
            }
        } else {
            if (locksPanel != null) {
                tabPerf.remove(locksPanel);
                locksPanel.removeAll();
                locksPanel = null;
            }
        }
				
		if (showCPUHistory.isSelected()) {
			if (cpuHistoryPanel == null) { 
				cpuHistoryPanel = new CPUHistoryPanel(options);
				tabPerf.addTab("CPU History", null, cpuHistoryPanel,
				        "This Tab Shows a Time Graph of CPU usage for the JVM");
			}
		} else {
			if (cpuHistoryPanel != null) { 
				tabPerf.remove(cpuHistoryPanel);
				cpuHistoryPanel.removeAll();
				cpuHistoryPanel = null;
			}
		}
		
		if (showThreadCPU.isSelected()) {
			if  (threadCPUPanel == null) {
				threadCPUPanel = new ThreadCPUPanel(numberOfSamples, options);
				tabPerf.addTab("CPU By Thread", null, threadCPUPanel, 
				        "This Tab Shows a Time Graph of CPU usage by Thread");
			}
		} else {
			if  (threadCPUPanel != null) {
				tabPerf.remove(threadCPUPanel);
				threadCPUPanel.removeAll();
				threadCPUPanel = null;
			}
		}
		
		if (showMemoryHistory.isSelected()) {
			if (memoryHistoryPanel == null) {
				memoryHistoryPanel = new MemoryHistoryPanel(numberOfSamples, options);
				tabPerf.addTab("Memory History", null, memoryHistoryPanel,
				        "This Tab Shows a Time Graph of Memory (Heap and Non-Heap) usage for the JVM" );
			}
		} else {
			if (memoryHistoryPanel != null) {
				tabPerf.remove(memoryHistoryPanel);
				memoryHistoryPanel.removeAll();
				memoryHistoryPanel = null;
			}
		}				
		
		if (showPoolHistory.isSelected()) {
			if (poolHistoryPanel == null) {
				poolHistoryPanel = new MemoryPoolHistoryPanel(options);
				tabPerf.addTab("Memory Pool History", null, poolHistoryPanel,
				        "This Tab Shows a Time Graph Memory Usage by Pool");
			}
		} else {
			if (poolHistoryPanel != null) {
				tabPerf.remove(poolHistoryPanel);
				poolHistoryPanel.removeAll();
				poolHistoryPanel = null;
			}
		}
		
		if (showIOCount.isSelected()) {
			if (ioCountHistoryPanel == null) {
				ioCountHistoryPanel = new IOCountHistoryPanel(options);
				tabPerf.addTab("IO Count History", null, ioCountHistoryPanel, 
				        "This Tab Shows a Time Graph of IO Counts for the JVM");
			}
		} else {
			if (ioCountHistoryPanel != null) {
				tabPerf.remove(ioCountHistoryPanel);
				ioCountHistoryPanel.removeAll();
				ioCountHistoryPanel = null;
			}
		}
		
		if (showIOBytes.isSelected()) {
			if (ioBytesHistoryPanel == null) {
				ioBytesHistoryPanel = new IOBytesHistoryPanel(options);
				tabPerf.addTab("IO Bytes History", null, ioBytesHistoryPanel,
				        "This Tab Shows a Time Graph of IO Bytes for the JVM");
			}
		} else {
			if (ioBytesHistoryPanel != null) {
				tabPerf.remove(ioBytesHistoryPanel);
				ioBytesHistoryPanel.removeAll();
				ioBytesHistoryPanel = null;
			}
		}
		
		if (showPaging.isSelected()) {
			if (pagingHistoryPanel == null) {
				pagingHistoryPanel = new PagingHistoryPanel(options);
				tabPerf.addTab("Paging History", null, pagingHistoryPanel, 
				        "This Tab Shows a Time Graph of Page Faults for the JVM");
			}
		} else {
			if (pagingHistoryPanel != null) {
				tabPerf.remove(pagingHistoryPanel);
				pagingHistoryPanel.removeAll();
				pagingHistoryPanel = null;
			}
		}
				
		if (showMemStats.isSelected()) {
			if (memStatsPanel == null) {
				memStatsPanel = new MemoryStatsPanel(options); 
				tabPerf.addTab("Memory Stats", null, memStatsPanel, 
				        "This Tab Displays Memory Usage (JVM, Process and System)");
			}
		} else {
			if (memStatsPanel != null) {
				tabPerf.remove(memStatsPanel);
				memStatsPanel.removeAll();
				memStatsPanel = null;
			}
		}
		
		if (showGC.isSelected()) {
            if (gcPanel == null) {
                gcPanel = new GarbageCollectorsPanel(); 
                tabPerf.addTab("Garbage Collectors", null, gcPanel, 
                        "This Tab Displays The Statisctics of the Garbage Collectors");
            }
        } else {
            if (gcPanel != null) {
                tabPerf.remove(gcPanel);
                gcPanel.removeAll();
                gcPanel = null;
            }
        }
		
		if (showProperties.isSelected()) {
			if (propertiesPanel == null) {
				propertiesPanel = new JVMPropertiesPanel(this);
				tabPerf.addTab("JVM Properties", null, propertiesPanel, 
				        "This Tab Displays The Java Properties, Environment Properties, Classpath, " +
				        "Startup Options and Native Libraries");
				resetOptions = true;
			}
		} else {
			if (propertiesPanel != null) {
				tabPerf.remove(propertiesPanel);
				propertiesPanel.removeAll();
				propertiesPanel = null;
			}
		}	
		
		if (showClassInfo.isSelected()) {
			if (classInfoPanel == null) {
				classInfoPanel = new ClassInformationPanel(this);
				tabPerf.addTab("Class Info", null, classInfoPanel, 
				        "This Tab Displays The Loaded Classes, Packages and Class Loaders");
				resetOptions = true;
			}
		} else {
			if (classInfoPanel != null) {
				tabPerf.remove(classInfoPanel);
				classInfoPanel.removeAll();
				classInfoPanel = null;
			}
		}	
		
		if (showJMXBeans.isSelected() && !playbackMode) {
			if (jmxBeanPanel == null) {
				jmxBeanPanel = new JMXBeanPanel(commMgr, options, this);
				tabPerf.addTab("JMX Beans", null, jmxBeanPanel, 
				        "This Tab Displays The JMX MBeans Defined in the JVM");
				resetOptions = true;
			}
		} else {
			if (jmxBeanPanel != null) {
				tabPerf.remove(jmxBeanPanel);
				jmxBeanPanel.removeAll();
				jmxBeanPanel = null;
			}
		}	
		
	}
	/**
	 * This method will toggle between auto update and paused modes
	 *
	 */
	private void pauseResume() {
		
		if (paused) {
		    logger.debug("Resuming Probe " + probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		    timer.setRunning(true);
			prButton.setText("Pause");
			paused = false;
			statusMsg.setText("Running...");
			
		} else {
		    logger.debug("Pausing Probe " + probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		    timer.setRunning(false);
			prButton.setText("Resume");
			paused = true;
			statusMsg.setText("Updates Are Paused");
		}
		
	}
	/**
	 * This method will stop the probe on the remote system.
	 *
	 */
	private void stopProbe() {
		
		if (!isConnected) return;
		
		statusMsg.setText("Sending Stop Probe Request");
		logger.debug("Stopping Probe " + probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		isConnected = false;
        sendRequest(ProbeRequest.REQ_STOP_PROBE);
     	
	}
	/**
	 * This will send a System.gc() command to a probe.
	 *
	 */
	private void sendGC() {
		
		System.runFinalization();
		System.gc();
		if (!isConnected) return;
		logger.debug("Sending gc Request to Probe " + probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		
        statusMsg.setText("Sending Garbage Collect Request");
        sendRequest(ProbeRequest.REQ_SEND_GC);
        	
	}
	   /**
     * This will send a System.gc() command to a probe.
     *
     */
    private void dumpHeap() {
        
        
        if (!isConnected) return;
        
        
        statusMsg.setText("Sending Request to Dump the Heap");
        logger.debug("Sending Request to Dump the Heap to " + 
                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
        
        ProbeResponse pr = sendRequest(ProbeRequest.REQ_DUMP_HEAP);
        String msg = "";
        if (pr != null) {
           msg = "ERROR: Dump Request Failed"; 
        }
        msg = pr.getResponseErrorMessage();
        if (msg.startsWith("ERROR:")){
            JOptionPane.showMessageDialog(this, msg, 
                    "Dump Heap Error",JOptionPane.ERROR_MESSAGE); 
        } else {
            JOptionPane.showMessageDialog(this, msg, 
                    "Dump Heap Complete",JOptionPane.INFORMATION_MESSAGE); 
        }
        
            
    }
	private void getStackTrace() {
		
		
		if (!isConnected) return;
		logger.debug("Sending Request to Dump Stack Trace to " + 
                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		
        statusMsg.setText("Dumping Stack Trace");
        ProbeResponse res = sendRequest(ProbeRequest.REQ_DUMP_STACKTRACE);
        new StackTraceDialog(parent, res.getStackTraceInfo());
        	
	}
	/**
	 * This will send a System.gc() command to a probe.
	 *
	 */
	private void sendExit() {
		
		
		if (!isConnected) return;
		if ( JOptionPane.showConfirmDialog(this, 
 				"This Will Terminate the the Target JVM, " +
				"Are You Sure You Want to Do This?",
 				"Are You Sure",
 				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
			
			//timer.stop();
			timer.setRunning(false);
			statusMsg.setText("Sending JVM Termination Request");
			logger.info("Sending JVM Termination Request to " + 
	                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
	        sendRequest(ProbeRequest.REQ_JVM_EXIT);
	        
 	     }
		
        
        	
	}
	private void doRecord() {
	    
	    if (!recording) {
	        String path = options.getRecordingDirectory();
	        File recPath = new File(path);
	        recPath.mkdir();
	        String fileName = path + probeName + "_" + hostName + "_" +
	                Utilities.formatTimeStamp(System.currentTimeMillis(), "MMddyyyyHHmmss")
	                + UIOptions.RECORDING_SUFFIX;
	        try {
	            recMgr = new RecordingManager(fileName, RecordingManager.RECORDING_MODE);
	            logger.info("Recording Session to " + recMgr.getRecordingFileName());
	        } catch (IOException e) {
	            String error = "Error Opening Recording file, Error is " + e.getMessage() + 
	                    "\nRecording Terminated";
	            logger.logException(e, this);
	            JOptionPane.showMessageDialog(this, error, 
	                    "Recording Error",JOptionPane.ERROR_MESSAGE);
	            return;
	        }
	        recordBtn.setText("Stop");
	        recording = true;
	        statusMsg.setText("Recording Started, writing to file " + 
	                recMgr.getRecordingFileName());
	       
	        sendRequest(ProbeRequest.REQ_CLASS_INFO);
	    } else {
	        stopRecording();
	        
	        statusMsg.setText("Recording Ended");
	    }
	    
	}
	private void stopRecording() {
	    
	    if (recMgr == null) {
	        return;
	    }
	    try {
	        recMgr.close();
	        logger.info("Closing Recording Session to " + 
	                recMgr.getRecordingFileName() + ", " +
	                Utilities.format(recMgr.getWriteCount()) +
	                " record written for " + 
	                Utilities.format(recMgr.getWriteBytes()) +
	                " bytes");

	        recording = false;
	    } catch (IOException e) {
	        logger.logException(e, this);
	    }
	    recordBtn.setText("Record");
        
	}
	private void writeResponse(ProbeResponse pr) {
	    
	    if (!recording) {
	        return;
	    }
	    if (recMgr == null) {
	        return;
	    }
	    
        try {
            recMgr.writeResponse(pr);
         } catch (IOException e) {
            logger.logException(e, this);
            stopRecording();
        }
	   
	}
	private void openPlaybackStream() {
	    
	    try {
	        recMgr = new RecordingManager(playBackFile, RecordingManager.PLAYBACK_MODE);
        } catch (FileNotFoundException e) {
            String error = "Requested Recording file " + 
                    playBackFile + " is not Found" + 
                    "\nPlayback Terminated";
            logger.logException(e, this);
            JOptionPane.showMessageDialog(this, error, 
                    "Playback Error",JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(error, e);
        } catch (IOException e) {
            String error = "Error Opening Recording file, Error is " + e.getMessage() + 
                    "\nPlayback Terminated";
            logger.logException(e, this);
            JOptionPane.showMessageDialog(this, error, 
                    "Playback Error",JOptionPane.ERROR_MESSAGE);
             
            throw new RuntimeException(error, e);
        }
	    int totalRecs = recMgr.getNumberOfRecords();
	    logger.info("Playback file has " + totalRecs + " Observations");
	    
	    int interval = 10;
	    if (totalRecs > 1000) {
	        interval = 5;
	    }
	    int ticks = (totalRecs < interval ? interval : totalRecs / interval);
	    Dictionary<Integer, JLabel> dict = new SliderDictionary<Integer, JLabel>();
	    
	    for (int i = 0; i < interval ; i++ ) {
	        
	        JLabel label = new JLabel(Utilities.format(ticks * i));
	        dict.put(ticks * i, label);
	        
	    }
	    JLabel label = new JLabel(Utilities.format(totalRecs));
	    dict.put(totalRecs, label);
	    
	    playbackSlider.setLabelTable(dict);
        playbackSlider.setMaximum(totalRecs);
        playbackSlider.setPaintTicks(true);
        playbackSlider.setPaintLabels(true);
        playbackSlider.setPaintTrack(true);
        playbackSlider.setMinorTickSpacing(ticks);
        
	    
	}
	/**
	 * This will request a connected probe to refresh the class/package info. We
	 * only do this on request since it is a very cpu intesive operation on the 
	 * remote system.
	 *
	 */
	public void getClassInfo() {
		
		if (!isConnected) return;
		
		logger.debug("Sending Get Class Info Request to " + 
                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		sendRequest(ProbeRequest.REQ_CLASS_INFO);	
		statusMsg.setText("Sending Class Info Request");
        
    }
	public String getClassProperties(String className) {
		
		if (!isConnected) return "";
		
		logger.debug("Sending Get Class Properties Request to " + 
                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		
		ProbeResponse res = sendRequest(ProbeRequest.REQ_GET_CLASS, className, "");	
		statusMsg.setText("Sending Get Class Properties Request");
		return res.getClassInfo();
        
    }
	public void setProperty(String key, String value) {
		
		if (!isConnected) return;
		
		logger.info("Sending Set Property(" + 
		        key + "=" + value + ") Request to " + 
                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		sendRequest(ProbeRequest.REQ_SET_PROPERTY, key, value);	
		statusMsg.setText("Sending Set Property Request");
	}

	public void clearProperty(String key) {
		
		if (!isConnected) return;
		
		logger.info("Sending Clear Property(" + 
                key +  ") Request to " + 
                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		sendRequest(ProbeRequest.REQ_REMOVE_PROPERTY, key, null);	
		statusMsg.setText("Sending Clear Property Request");
	}
	/**
     * This method is called by the timer event to get updated info from the probe.
     *
     */
    protected void update() {
    	
    	
    	if (!isConnected && !playbackMode) return;
    	
    	
    	ProbeResponse res = null;
    	
    	if (playbackMode) {
    	    logger.debug("In update() for " + playBackFile);
    	    
    	    res = readPlaybackFile();
    	    if (res == null) {
    	        logger.error("End of File for Playback of  " + playBackFile); 
                closePlaybackFile();
                timer.setRunning(false);
                recordBtn.setEnabled(true);
                recordBtn.setText("Play");
                statusMsg.setText("Playback Has Ended");
                playbackSlider.setValue(0);
                playbackSlider.setToolTipText("Current Playback Position");
                playbackInProgress = false;
                JOptionPane.showMessageDialog(this,"Playback Ended ", 
                        "Playback Ended",JOptionPane.INFORMATION_MESSAGE);
                return;
    	    }
    	    this.setTitle("Playback of Session[" + playBackFile + "] [" +
    	            res.getProbeName() + "] - " + res.getHostName() + 
                    "(" + res.getHostIP() + ")");
    	} else {
    	    
    	    logger.debug("In update() for " + 
    	            probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
    	
    	    res = sendRequest(ProbeRequest.REQ_STATS);
    	    
    	    if (res == null || res.getProbeName() == null || res.getHostName() == null ) return;
            String rec = "";
            if (recording) {
                rec = "(*** Recording ***)";
            }
            this.setTitle(rec + "[" + res.getProbeName() + "] - " + res.getHostName() + 
                    "(" + res.getHostIP() + "):" + hostPort);
            writeResponse(res);
            if (updateInterval != options.getUpdateInterval()) {
                updateInterval = options.getUpdateInterval();
                //timer.setDelay(updateInterval);
                timer.setInterval(updateInterval);
            }
    	}
    	updatePanel(res);
    }
    protected void updateByStep(int record) {
        
        ProbeResponse res = null;
        
 
            logger.debug("In update() for " + playBackFile);
            
            res = readPlaybackFile(record);
            if (res == null) {
                logger.error("End of File for Playback of  " + playBackFile); 
                closePlaybackFile();
                timer.setRunning(false);
                recordBtn.setEnabled(true);
                recordBtn.setText("Play");
                statusMsg.setText("Playback Has Ended");
                playbackSlider.setValue(0);
                playbackSlider.setToolTipText("Current Playback Position");
                playbackInProgress = false;
                JOptionPane.showMessageDialog(this,"Playback Ended ", 
                        "Playback Ended",JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            this.setTitle("Playback of Session[" + playBackFile + "] [" +
                    res.getProbeName() + "] - " + res.getHostName() + 
                    "(" + res.getHostIP() + ")");
        
        updatePanel(res);
    }
    private void updatePanel(ProbeResponse res) {
        
        JavaVersion version = new JavaVersion(res.getJavaVersion());
        boolean showNonHeap = (version.compareTo(JavaVersion.java1_8) < 0);

    	if (poolHistoryPanel != null && !poolHistoryPanel.isPanelBuild()) { 
    		
    		poolHistoryPanel.buildPanel(res, numberOfSamples);
    	}
    	if (ioCountHistoryPanel != null && !ioCountHistoryPanel.isPanelBuild()) { 
    		
    		ioCountHistoryPanel.buildPanel(res, numberOfSamples);
    	}
    	if (ioBytesHistoryPanel != null && !ioBytesHistoryPanel.isPanelBuild()) { 
    		
    		ioBytesHistoryPanel.buildPanel(res, numberOfSamples);
    	}
    	
    	if (pagingHistoryPanel != null && !pagingHistoryPanel.isPanelBuild()) { 
    		
    		pagingHistoryPanel.buildPanel(res, numberOfSamples);
    	}
    	
    	if (memStatsPanel != null && !memStatsPanel.isPanelBuilt()) { 
    		memStatsPanel.buildPanel(res);
    		
    	}
    	
    	if (cpuHistoryPanel != null && !cpuHistoryPanel.isPanelBuilt()) { 
    		cpuHistoryPanel.buildPanel(res, numberOfSamples);
    		
    	}
    	
    	if (overviewPanel != null && !overviewPanel.isPanelBuilt()) { 
    		overviewPanel.buildPanel(res);
    		
    	}
    		    
    	if (overviewPanel != null) overviewPanel.update(res);
    	if (locksPanel != null) locksPanel.update(res);
    	if (cpuHistoryPanel != null) cpuHistoryPanel.update(res);
    	if (threadCPUPanel != null) threadCPUPanel.update(res);
    	if (memoryHistoryPanel != null) {
    	    memoryHistoryPanel.setShowNonHeap(showNonHeap);
    	    memoryHistoryPanel.update(res);
    	}
    	if (poolHistoryPanel != null) poolHistoryPanel.update(res);
    	if (pagingHistoryPanel != null) pagingHistoryPanel.update(res);
    	if (memStatsPanel != null) memStatsPanel.update(res);
    	if (gcPanel != null) gcPanel.update(res);
    	if (propertiesPanel != null) propertiesPanel.update(res);
    	if (classInfoPanel != null) classInfoPanel.update(res);
    	if (jmxBeanPanel != null) jmxBeanPanel.update(res);
    	
    	if (res.isIoCountersAvailable()) {
    		if (ioCountHistoryPanel != null) ioCountHistoryPanel.update(res);
    		if (ioBytesHistoryPanel != null) ioBytesHistoryPanel.update(res);
    	} else {
    		if (ioCountHistoryPanel != null) {
    			tabPerf.remove(ioCountHistoryPanel);
    			ioCountHistoryPanel.removeAll();
    			ioCountHistoryPanel = null;
    			showIOCount.setSelected(false);
    		}
    		if (ioBytesHistoryPanel != null) {
    			tabPerf.remove(ioBytesHistoryPanel);
    			ioBytesHistoryPanel.removeAll();
    			ioBytesHistoryPanel = null;
    			showIOBytes.setSelected(false);
    		}
    		if (pagingHistoryPanel != null) {
    			tabPerf.remove(pagingHistoryPanel);
    			pagingHistoryPanel.removeAll();
    			pagingHistoryPanel = null;
    			showPaging.setSelected(false);
    		}
    	}
    	
    	if (playbackMode) {
    	    statusMsg.setText("Playback, From " + 
    	        Utilities.formatTimeStamp(res.getCurrentTime(),"MM-dd-yyyy HH:mm:ss.SSS") + 
    	        ", Sample(" + lastTick + " of " + recMgr.getNumberOfRecords() + ")");
    	    playbackInterval = Integer.parseInt(playbackSpeed.getValue().toString());
    	    timer.setInterval(playbackInterval);
    	   
    	} else {
    	    if (recording) {
    	        statusMsg.setText("Recording, Last Update " + Utilities.getDateTime("HH:mm:ss.SSS"));
    	    } else {
    	        statusMsg.setText("Running, Last Update " + Utilities.getDateTime("HH:mm:ss.SSS"));
    	    }
    	}
    	res.clear();
    	
    	
    }

 

	protected ProbeResponse sendRequest(int request) {
		return sendRequest(request, null, null);
	}

	protected ProbeResponse sendRequest(int request, String key, String value) {
		
	    logger.debug("Sending Request " + request + " to " + 
	                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);		
		ProbeResponse res = null;
		ProbeRequest pr = new ProbeRequest();
        pr.setRequestType(request);
        pr.setCollectMonitorInfo(options.isObtainMonitorInfo());
        pr.setCollectLockInfo(options.isObtainLockInfo());
        pr.setProbeDebug(debugProbe);
        
        if (request == ProbeRequest.REQ_SET_PROPERTY || request == ProbeRequest.REQ_REMOVE_PROPERTY) {
        	pr.setPropertyKey(key);
        	pr.setPropertyValue(value);
        }
        
        if (request == ProbeRequest.REQ_GET_CLASS) {
        	pr.setClassName(key);
        }
        
        if (request == ProbeRequest.REQ_JVM_EXIT) {
        	pr.setExitCode(options.getExitCode());
        }
        
        if (resetOptions) {
        	pr.setResetOptionData(true);
        	resetOptions = false;
        }
        
        try {
			res = commMgr.sendCommand(pr);
			logger.debug(res.toString());
			
		} catch (ProbeCommunicationsException e) {
			JOptionPane.showMessageDialog(this,e.getMessage(), 
					"Connection Lost",JOptionPane.ERROR_MESSAGE);
			
			logger.error("Connection Lost to " + 
			        probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
			logger.logException(e, this);
			closeConnection();
			if (recording) {
			    stopRecording();
			    recordBtn.setEnabled(false);
			}
			
		} 
		
		return res;
	}
	private void closeConnection() {
		
		timer.setRunning(false);
		commMgr.close();
		logger.info("Closing Connection to " + 
                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
        this.setTitle("(Disconnected)" + this.getTitle());
        prButton.setEnabled(false);
        getStackTrace.setEnabled(false);
        shutdownBtn.setEnabled(false);
		gcBtn.setEnabled(false);
		terminateBtn.setEnabled(false);
		dumpHeap.setEnabled(false);
		recordBtn.setEnabled(false);
		isConnected = false;
		statusMsg.setText("Disconnected at " + Utilities.getDateTime("HH:mm:ss.SSS"));
		reconnectItem.setEnabled(true);
	}
	private void closePlaybackFile() {
	    
	    
	    if (recMgr == null) {
	        return;
	    }
	    
	    try {
	        recMgr.close();
	        logger.info("Recording File " + recMgr.getRecordingFileName() + 
	                " Closed, " + Utilities.format(recMgr.getReadCount()) + 
	                " Records Read for " + 
	                Utilities.format(recMgr.getReadBytes()) + " bytes");
	        playbackSlider.setValue(0);
	        playbackSlider.setToolTipText("Current Playback Position");
	        
	    } catch(IOException e) {
	        logger.error("Error Closing Playback File " + recMgr.getRecordingFileName()); //playBackFile);
	        logger.logException(e, this);
	    }
	}
	
	private ProbeResponse readPlaybackFile() {
 
	    int tick = playbackSlider.getValue();
	    playbackSlider.setToolTipText("Current Playback Position(" + 
	            Utilities.format(tick) + ")");
	    /*
	     * If we are going backward, force a pause and reset...
	     */
	    if (tick > 0 && tick  < lastTick) {
	        resetDisplay();
	        if (!paused) {
	            pauseResume();
	            resetDisplay();
	        }
	    }
	    
	    return readPlaybackFile(tick);
	    
	}
	   private ProbeResponse readPlaybackFile(int recordNumber) {
	        
	       ProbeResponse res = null;
	        
	        try {
	            //res = (ProbeResponse) ois.readObject();
	            res = recMgr.readResponse(recordNumber);
	            playbackSlider.setValue(recordNumber + 1);
	        } catch (EOFException e) {
	            return null;
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        } catch (ClassNotFoundException e) {
	            throw new RuntimeException(e);
	        }
	        lastTick = recordNumber;
	        return res;
	        
	    }
	protected ProbeResponse setAffinity(long mask) {
		
		
		ProbeResponse res = null;
		ProbeRequest pr = new ProbeRequest();
        pr.setRequestType(ProbeRequest.REQ_SET_AFFINITY);
        pr.setAffinity(mask);
        
        
        
        if (resetOptions) {
        	pr.setResetOptionData(true);
        	resetOptions = false;
        }
        
        try {
			res = commMgr.sendCommand(pr);
		} catch (ProbeCommunicationsException e) {
			JOptionPane.showMessageDialog(this,e.getMessage(), 
					"Connection Lost",JOptionPane.ERROR_MESSAGE);
			logger.logException(e, this);
			closeConnection();
        	
		} 
		
		return res;
	}
	protected void print() {
		
		//Work in progress...
		//if (true) return;
		PrinterJob pjob = PrinterJob.getPrinterJob();
		
		PageFormat pf = pjob.defaultPage();
		pf.setOrientation(PageFormat.LANDSCAPE);
		
		if (!pjob.printDialog()) return;
		
		
		if (overviewPanel != null) {
			pjob.setPrintable(overviewPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		if (cpuHistoryPanel != null) { 
			pjob.setPrintable(cpuHistoryPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		if (threadCPUPanel != null) {
			pjob.setPrintable(threadCPUPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		if (memoryHistoryPanel != null) {
			pjob.setPrintable(memoryHistoryPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		if (poolHistoryPanel != null) {
			pjob.setPrintable(poolHistoryPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		if (ioCountHistoryPanel != null) {
			pjob.setPrintable(ioCountHistoryPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		if (ioBytesHistoryPanel != null) {
			pjob.setPrintable(ioBytesHistoryPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		if (memStatsPanel != null) {
			pjob.setPrintable(memStatsPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		if (propertiesPanel != null) {
			pjob.setPrintable(propertiesPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		if (classInfoPanel != null) {
			pjob.setPrintable(classInfoPanel, pf);
			try {pjob.print();} catch (PrinterException e) {logger.logException(e,this);}
		}
		
		
	}

	
	protected void closeClient() {
		hasEnded = true;
		//if (timer != null) timer.stop();
		if (timer != null) timer.shutdown();
		logger.info("Closing Client " + 
                probeName + " at " + hostName + ":" + hostIP + ":" + hostPort);
		stopRecording();
		
		if (playbackMode) {
		    closePlaybackFile();
		    String recFile = recMgr.getRecordingFileName();
		    if ( JOptionPane.showConfirmDialog(this, 
	                "Do you want to Delete recording file \n" + recFile + "?",
	                "Delete Playback File?",
	                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		        File file = new File(recFile);
		        if (!file.delete()) {
		            JOptionPane.showMessageDialog(this,
		                    "Delete of Recording file " + recFile + " Failed.\n" +
		                    "You can delete it manually", 
		                    "Delete Failed",JOptionPane.ERROR_MESSAGE);
		        }
	        }
		}
		
		this.dispose();
		parent.removeClient(this);
	}
	public void dock(JPanel panel, String name) {
	    
	    tabPerf.addTab(name, panel);
	    tabPerf.setSelectedComponent(panel);
	}
	public void undock(JPanel panel) {
        tabPerf.remove(panel);
        tabPerf.setSelectedIndex(0);
    }
	/**
	 * @return Returns the hasEnded.
	 */
	public boolean hasEnded() {
		return hasEnded;
	}

	/**
	 * @return the hostIP
	 */
	public String getHostIP() {
		return hostIP;
	}

	
	/**
	 * @return the hostPort
	 */
	public int getHostPort() {
		return hostPort;
	}

	/**
	 * @return the probeName
	 */
	public String getProbeName() {
		return probeName;
	}
    public boolean isPlaybackMode() {
        return playbackMode;
    }
    private boolean isProbeLocal(String host, String client) {
        
        if (host.equals(client)) {
            return true;
        }
        if (host.startsWith(client)) {
            return true;
        }
        if (client.startsWith(host)) {
            return true;
        }
        if (host.equals(ourIPaddress)) {
            return true;
        }
        if (client.startsWith(ourIPaddress)) {
            return true;
        }
        
        return false;
    }
    protected RecordingManager getRecordingManager() {
        return recMgr;
    }
    protected int getLastTick() {
        return lastTick;
    }
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        
        String message = "The UI Update Thread has experienced an Unhandled Exception. \nException is " +
                e.getClass().getCanonicalName() + ", Error is " +
                e.getMessage() +", \nSelect Yes to Close the Client, No to Disconnect";
        logger.error(message);
        logger.logException(e, t);
        if ( JOptionPane.showConfirmDialog(this, 
                message,
                "Close This Client?",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) 
            {
                closeClient();
            } else {
                timer = new TimerThread(updateAL, updateInterval, probeName);
                timer.setUncaughtExceptionHandler(this);
                closeConnection();
            }
    }

	
	
	
}
class SliderDictionary<K,V> extends Dictionary<K,V> {

    private Map<K,V> labels = new HashMap<K,V>();
    public SliderDictionary() {
        
    }
    @Override
    public int size() {
        return labels.size();
    }

   @Override
    public boolean isEmpty() {
         return labels.isEmpty();
    }

   @Override
    public Enumeration<K> keys() {
        
        Set<K> keys = labels.keySet();
        
        return new DictionaryEnumeration<K>(keys.iterator());
    }
   

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Enumeration<V> elements() {
        Set<Entry<K, V>> values = labels.entrySet();
        return new DictionaryEnumeration(values.iterator());
    }

    @Override
    public V get(Object key) {
        return labels.get(key);
    }

    @Override
    public V put(K key, V value) {
        return labels.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return labels.remove(key);
    }
    
}
class DictionaryEnumeration<E> implements Enumeration<E> {

    private Iterator<E> iter;
    public DictionaryEnumeration(Iterator<E> iter) {
        this.iter = iter;
    }
    @Override
    public boolean hasMoreElements() {
        return iter.hasNext();
    }

 
    @Override
    public E nextElement() {
        return iter.next();
    }
    
}