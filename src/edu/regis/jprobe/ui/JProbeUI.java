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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import edu.regis.guitools.GraphStatusBar;
import edu.regis.guitools.StatusBar;
import edu.regis.jprobe.jni.OSSystemInfo;
import edu.regis.jprobe.model.BroadcastMessage;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.ProbeCommunicationsException;
import edu.regis.jprobe.model.Utilities;
import edu.regis.jprobe.ui.themes.ThemeManager;

/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JProbeUI extends JFrame{
	
    private static final long serialVersionUID = 1L;
    public int updateInterval = 1000;
	private int lookAndFeelIndex;
	
	private static final int  TEN_SEC = 10000;
	private static final int  FIVE_SEC = 5000;
	private static final int  THREE_SEC = 3000;
	private static final int  ONE_SEC = 1000;
	private static final int  HALF_SEC = 500;
	private static final int  QUARTER_SEC = 250;
	private static final long BROADCAST_AGE_OUT = 5000;
	private static final long EXPIRE = 1406872800000l;
	
	//Swing Components
	private UIManager.LookAndFeelInfo lookAndFeel[];
	private JCheckBoxMenuItem showZeroCpuItem;
	private JCheckBoxMenuItem relativeCpuItem; 
	private JCheckBoxMenuItem confirmExitItem;
	private JCheckBoxMenuItem stackFilterItem;
	private JCheckBoxMenuItem showDeltasItem;
	private JCheckBoxMenuItem tenSecItem;
	private JCheckBoxMenuItem fiveSecItem;
	private JCheckBoxMenuItem threeSecItem;
	private JCheckBoxMenuItem oneSecItem;
	private JCheckBoxMenuItem halfSecItem;
	private JCheckBoxMenuItem qtrSecItem;
	private JCheckBoxMenuItem style[];
	private JMenu quickConnect;
	private JMenuItem processItem;
	private JPanel status;
	public static Color bgColor;
	protected JDesktopPane desktop;
	protected ThemeManager tmgr;
	protected MDIDesktopManager dtm;
	
	private OSProcessClientFrame osProcessFrame;
		
    private int port;
    private String host;
    protected JProbeUI instance;
    private static JFrame staticFrame;
    private Vector<JProbeClientFrame> clients;
	private static UIOptions options;
	protected static int clientFrameCounter = 0;
	private Logger logger;
	private BroadCastScanner scanner;
	private Timer timer;
	private SplashFrame splash;
	private static final String SB_NONE = "None";
	private static final String SB_STANDARD = "Standard";
	private static final String SB_GRAPHICAL = "Graphical";
	
	/**
	 * 
	 */
	public JProbeUI(String filename) { 
		
		instance = this;
		splash = new SplashFrame(4000);
		clients = new Vector<JProbeClientFrame>();
		options = new UIOptions();
		options.setParentComponent(this);
		
		logger = Logger.getLogger();
		
		
		
		splash.setStatus("Scanning For Probed JVMs...");
		scanner = BroadCastScanner.getInstance();
		
				
		logger.info(Utilities.WINDOW_TITLE + " is Starting");
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		
		UIManager.put("ProgressBar.selectionBackground", Color.black);
        UIManager.put("ProgressBar.selectionForeground", Color.darkGray);
		int startX = options.getParentX();
		int startY = options.getParentY();
		int startWidth = options.getParentWidth();
		int startHeight = options.getParentHeight();
		
		
		//This will start the frame in the middle of the screen
		int x = (d.width - startWidth) / 2; 
		int y = (d.height - startHeight) / 2;
		if (options.isStartLastLocation()) {
		    if (startX > d.width || startY > d.height) {
		        setLocation (x,  y); 
		    } else {
		        setLocation (startX,  startY);
		    }
		} else {
		    setLocation (x,  y); 
		}
 		
		setSize(startWidth, startHeight);
		
 		ImageIcon icon = IconManager.getIconManager().getMainIcon();	
 		if (icon != null) setIconImage(icon.getImage());
		
 		//Font myfont = new Font("Courier New",Font.BOLD,12);		
	    //Color lblColor = Color.GRAY;
	    bgColor = this.getBackground();
		desktop = new JDesktopPane();	
		getContentPane().add(desktop, BorderLayout.CENTER);
		
		
		tmgr = new ThemeManager();
		dtm = new MDIDesktopManager(desktop);
		desktop.setDesktopManager(dtm);
		lookAndFeel = UIManager.getInstalledLookAndFeels();
		lookAndFeelIndex = options.getLookAndFeel();
		splash.setStatus("Loading Components...");
		createMenu();
		changeLookAndFeel(lookAndFeelIndex, lookAndFeel[lookAndFeelIndex].getName());
		setStatusBar();
		
		/*if (System.currentTimeMillis() > EXPIRE ) {
		    splash.close();
            JOptionPane.showMessageDialog(this,"This Copy Of Java VM Probe has Expired!",
                    "Expired",JOptionPane.ERROR_MESSAGE);
            
            System.exit(0);
        }*/
        
        logger.warning("Java VM Probe will Expire on " + 
                Utilities.formatTimeStamp(EXPIRE, "yyyy-MM-dd HH:mm:ss"));
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // We'll handle this
		
		
		// Window close handler
		addWindowListener( new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				
				exitProgram(); 
				
			}
							
		});
		
		addComponentListener ( new ComponentListener() {

			public void componentResized(ComponentEvent e) {
				updateLocation();
			}
			public void componentMoved(ComponentEvent e) {
				updateLocation();
			}

			public void componentShown(ComponentEvent e) {
				updateLocation();
			}

			public void componentHidden(ComponentEvent e) {
				updateLocation();
			}
			
			
		});
		
		addWindowFocusListener( new WindowAdapter() {

			public void windowGainedFocus(WindowEvent e) {
				updateLocation();
				paintAll(getGraphics());
			}

			
		});
		
		ActionListener act = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                buildQuickConnect();
            
            }
		    
		};
		timer = new Timer(1000, act);
		timer.start();
		this.setTitle(Utilities.WINDOW_TITLE);
		setVisible(true);
		staticFrame = instance;
		
		if (options.wasSelfElevated()) {
		    addProcessPanel(true);
		    options.setWasSelfElevated(false);
		    options.save();
		}
		
		if (filename == null) {
		    if (options.isShowConnectWhenEmpty()) {
		        connect();
		    }
		} else {
            addClient( "Playback", 
                    filename,
                    filename, 
                    0,
                    "N/A",
                    "N/A",
                    filename,
                    false,
                    true);
		}
		
		splash.setStatus("Scanning For Probed JVMs...");	
	}
	
	private void createMenu() {
	    
	    //	  Create the File Menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		// Add the Refresh
		
		
		// Add the Connect
		JMenuItem connectItem = new JMenuItem("Connect");
		connectItem.setMnemonic('C');
		connectItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		connectItem.setToolTipText("Show Connect Dialog");
		//create a listener for it.
		connectItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
							connect();
						
					}
				}
				);
		fileMenu.add(connectItem);
		
		
		
		quickConnect = new JMenu("Quick Connect");
		quickConnect.setMnemonic('Q');
		
		quickConnect.setToolTipText("Connect to a Local Probe");
	        //create a listener for it.
		
		fileMenu.add(quickConnect);
		
		JMenuItem openItem = new JMenuItem("Open A Recorded Session");
		openItem.setMnemonic('O');
		openItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openItem.setToolTipText("Playback A Recorded Session");
        //create a listener for it.
		openItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                            open();
                        
                    }
                }
                );
        fileMenu.add(openItem);
		fileMenu.addSeparator();
		
		if (OSSystemInfo.isOperational()) {
		    processItem = new JMenuItem("System Processes");
		    processItem.setMnemonic('P');
		    processItem.setToolTipText("View System Processes");
		    fileMenu.add(processItem);
		    fileMenu.addSeparator();
		    processItem.addActionListener(
	                new ActionListener()
	                {
	                    public void actionPerformed (ActionEvent e)
	                    {
	                        if (osProcessFrame == null) {
	                            addProcessPanel(false);
	                        }
	                    }
	                }
	                );
		}
		//		 Add the Exit
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setMnemonic('X');
		exitItem.setToolTipText("To Quit...");
		exitItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		
		//create a listener for it.
		exitItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						exitProgram();
					}
				}
				);
		//		 Add the Exit
		fileMenu.add(exitItem);
		
		// create the Edit Menu
		JMenu optionMenu = new JMenu("Options");
		optionMenu.setMnemonic('o');
		//Add the Rent 
		
			
		
		
		JMenu autoUpdateIntervalItem = new JMenu("Update Interval...");
		autoUpdateIntervalItem.setMnemonic('i');
		autoUpdateIntervalItem.setEnabled(true);
		autoUpdateIntervalItem.setToolTipText("Select the Refresh Speed");
		optionMenu.add(autoUpdateIntervalItem);

		tenSecItem = new JCheckBoxMenuItem("10 seconds");
		tenSecItem.setMnemonic('a');
		tenSecItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		tenSecItem.setEnabled(true);
		if (options.getUpdateInterval() == TEN_SEC) {
			tenSecItem.setSelected(true);
		} else {
			tenSecItem.setSelected(false);
		}
		autoUpdateIntervalItem.add(tenSecItem);
		//create a listener for it.
		tenSecItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    
					    options.setUpdateInterval(TEN_SEC);
						fiveSecItem.setSelected(false);
						threeSecItem.setSelected(false);
						oneSecItem.setSelected(false);
						halfSecItem.setSelected(false);
						qtrSecItem.setSelected(false);
					}
				}
				);
		
		fiveSecItem = new JCheckBoxMenuItem("5 seconds");
		fiveSecItem.setMnemonic('5');
		fiveSecItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_5, ActionEvent.CTRL_MASK));
		fiveSecItem.setEnabled(true);
		
		if (options.getUpdateInterval() == FIVE_SEC) {
			fiveSecItem.setSelected(true);
		} else {
			fiveSecItem.setSelected(false);
		}
		
		autoUpdateIntervalItem.add(fiveSecItem);
		//create a listener for it.
		fiveSecItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    
						options.setUpdateInterval(FIVE_SEC);
						tenSecItem.setSelected(false);
						threeSecItem.setSelected(false);
						oneSecItem.setSelected(false);
						halfSecItem.setSelected(false);
						qtrSecItem.setSelected(false);
					}
				}
				);
		
		threeSecItem = new JCheckBoxMenuItem("3 seconds");
		threeSecItem.setMnemonic('3');
		threeSecItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_3, ActionEvent.CTRL_MASK));
		threeSecItem.setEnabled(true);
		if (options.getUpdateInterval() == THREE_SEC) {
			threeSecItem.setSelected(true);
		} else {
			threeSecItem.setSelected(false);
		}
		autoUpdateIntervalItem.add(threeSecItem);
		//create a listener for it.
		threeSecItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    
						options.setUpdateInterval(THREE_SEC);
						fiveSecItem.setSelected(false);
						tenSecItem.setSelected(false);
						oneSecItem.setSelected(false);
						halfSecItem.setSelected(false);
						qtrSecItem.setSelected(false);
					}
				}
				);
		
		oneSecItem = new JCheckBoxMenuItem("1 second");
		oneSecItem.setMnemonic('1');
		oneSecItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_1, ActionEvent.CTRL_MASK));
		oneSecItem.setEnabled(true);
		if (options.getUpdateInterval() == ONE_SEC) {
			oneSecItem.setSelected(true);
		} else {
			oneSecItem.setSelected(false);
		}
		autoUpdateIntervalItem.add(oneSecItem);
		//create a listener for it.
		oneSecItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    
						options.setUpdateInterval(ONE_SEC);
						fiveSecItem.setSelected(false);
						threeSecItem.setSelected(false);
						tenSecItem.setSelected(false);
						halfSecItem.setSelected(false);
						qtrSecItem.setSelected(false);
					}
				}
				);
		halfSecItem = new JCheckBoxMenuItem(".5 second");
		halfSecItem.setMnemonic('h');
		halfSecItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		halfSecItem.setEnabled(true);
		if (options.getUpdateInterval() == HALF_SEC) {
			halfSecItem.setSelected(true);
		} else {
			halfSecItem.setSelected(false);
		}
		autoUpdateIntervalItem.add(halfSecItem);
		//create a listener for it.
		halfSecItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    
						options.setUpdateInterval(HALF_SEC);
						fiveSecItem.setSelected(false);
						threeSecItem.setSelected(false);
						tenSecItem.setSelected(false);
						oneSecItem.setSelected(false);
						qtrSecItem.setSelected(false);
					}
				}
				);
		
		qtrSecItem = new JCheckBoxMenuItem(".25 second");
		qtrSecItem.setMnemonic('q');
		qtrSecItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		qtrSecItem.setEnabled(true);
		if (options.getUpdateInterval() == QUARTER_SEC) {
			qtrSecItem.setSelected(true);
		} else {
			qtrSecItem.setSelected(false);
		}
		autoUpdateIntervalItem.add(qtrSecItem);
		//create a listener for it.
		qtrSecItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					   
						options.setUpdateInterval(QUARTER_SEC);
						fiveSecItem.setSelected(false);
						threeSecItem.setSelected(false);
						tenSecItem.setSelected(false);
						oneSecItem.setSelected(false);
						halfSecItem.setSelected(false);
					}
				}
				);
		
		
		optionMenu.addSeparator();
		JMenuItem thresholdMenu = new JMenuItem("Threshold Levels and Colors");
		thresholdMenu.setMnemonic('l');
		thresholdMenu.setEnabled(true);
		thresholdMenu.setToolTipText("Change The Color and Level of the Overview Graphs");
		optionMenu.add(thresholdMenu);
		thresholdMenu.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						new ThresholdDialog(instance, options);
					}
				}
				);
		
		optionMenu.addSeparator();
		JMenuItem filterMenu = new JMenuItem("Edit Exclusion Filters");
		filterMenu.setMnemonic('e');
		filterMenu.setEnabled(true);
		filterMenu.setToolTipText("Edit Exclusion Filters used in the Overview Task Table");
        optionMenu.add(filterMenu);
        filterMenu.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        new FiltersDialog(instance, options);
                    }
                }
                );
        JMenuItem libMenu = new JMenuItem("Manage Dynamic Libraries");
        libMenu.setMnemonic('e');
        libMenu.setEnabled(true);
        libMenu.setToolTipText("Edit Dynamically Added Libraries");
        optionMenu.add(libMenu);
        libMenu.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        new JarsDialog(instance, options);
                    }
                }
                );
        
        optionMenu.addSeparator();
		JMenu lookAndFeelMenu = new JMenu("UI Look and Feel");
		lookAndFeelMenu.setMnemonic('l');
		lookAndFeelMenu.setEnabled(true);
		lookAndFeelMenu.setToolTipText("Change The Look and Feel");
		optionMenu.add(lookAndFeelMenu);
		optionMenu.addSeparator();
		
		//		Add the Return
		showZeroCpuItem = new JCheckBoxMenuItem("Show Threads With 0 CPU Time");
		showZeroCpuItem.setMnemonic('s');
		showZeroCpuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		showZeroCpuItem.setEnabled(true);
		if (options.isShowZeroCPU()) {
			showZeroCpuItem.setSelected(true);
		} else {
			showZeroCpuItem.setSelected(false);
		}
		showZeroCpuItem.setToolTipText("Toggle Whether to Show All Threads");
		optionMenu.add(showZeroCpuItem);
		

		//create a listener for it.
		showZeroCpuItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setShowZeroCPU(options.isShowZeroCPU()? false: true);
					}
				}
				);
		
		//Add the Return
		relativeCpuItem = new JCheckBoxMenuItem("CPU Time is Relative");
		relativeCpuItem.setMnemonic('r');
		relativeCpuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		relativeCpuItem.setEnabled(true);
		if (options.isCpuIsRelative()) {
			relativeCpuItem.setSelected(true);
		} else {
			relativeCpuItem.setSelected(false);
		}
		relativeCpuItem.setToolTipText("Toggle CPU % As Either Absolute Or Relative");
		optionMenu.add(relativeCpuItem);
		

		//create a listener for it.
		relativeCpuItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setCpuIsRelative(options.isCpuIsRelative()? false : true);
					}
				}
				);
		
		stackFilterItem = new JCheckBoxMenuItem("Filter The StackTrace");
		stackFilterItem.setMnemonic('f');
		stackFilterItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		stackFilterItem.setEnabled(true);
        if (options.isFilterStack()) {
            stackFilterItem.setSelected(true);
        } else {
            stackFilterItem.setSelected(false);
        }
        stackFilterItem.setToolTipText("Filter Stack Entries in the Overview");
        optionMenu.add(stackFilterItem);
        

        //create a listener for it.
        stackFilterItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        options.setFilterStack(options.isFilterStack()? false : true);
                        options.getFilters().setEnabled(options.isFilterStack());
                    }
                }
                );
        
        showDeltasItem = new JCheckBoxMenuItem("Show Deltas");
        showDeltasItem.setMnemonic('d');
        showDeltasItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        showDeltasItem.setEnabled(true);
        if (options.isShowDeltas()) {
            showDeltasItem.setSelected(true);
        } else {
            showDeltasItem.setSelected(false);
        }
        showDeltasItem.setToolTipText("Show Deltas Values in the Overview");
        optionMenu.add(showDeltasItem);
        

        //create a listener for it.
        showDeltasItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        options.setShowDeltas(options.isShowDeltas()? false : true);
                        
                    }
                }
                );
        
        JCheckBoxMenuItem obtainMonitorsItem = new JCheckBoxMenuItem("Obtain Monitor Lock Info");
        obtainMonitorsItem.setMnemonic('m');
        obtainMonitorsItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_M, ActionEvent.CTRL_MASK));
        obtainMonitorsItem.setEnabled(true);
        if (options.isObtainMonitorInfo()) {
            obtainMonitorsItem.setSelected(true);
        } else {
            obtainMonitorsItem.setSelected(false);
        }
        obtainMonitorsItem.setToolTipText("Obtain Monitor Lock Information for Threads");
        optionMenu.add(obtainMonitorsItem);
        

        //create a listener for it.
        obtainMonitorsItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        options.setObtainMonitorInfo(options.isObtainMonitorInfo()? false : true);
                        
                    }
                }
                );
        
        JCheckBoxMenuItem obtainLocksItem = new JCheckBoxMenuItem("Obtain Synchronizer Lock Info");
        obtainLocksItem.setMnemonic('l');
        obtainLocksItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        obtainLocksItem.setEnabled(true);
        if (options.isObtainLockInfo()) {
            obtainLocksItem.setSelected(true);
        } else {
            obtainLocksItem.setSelected(false);
        }
        obtainLocksItem.setToolTipText("Obtain Synchronizer Lock Information for Threads");
        optionMenu.add(obtainLocksItem);
        

        //create a listener for it.
        obtainLocksItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        options.setObtainLockInfo(options.isObtainLockInfo()? false : true);
                        
                    }
                }
                );
        
        JCheckBoxMenuItem showProbeItem = new JCheckBoxMenuItem("Show Probe Thread");
        showProbeItem.setMnemonic('p');
        showProbeItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        showProbeItem.setEnabled(true);
        if (options.isShowProbeThread()) {
            showProbeItem.setSelected(true);
        } else {
            showProbeItem.setSelected(false);
        }
        showProbeItem.setToolTipText("Show Probe Thread");
        optionMenu.add(showProbeItem);
        

        //create a listener for it.
        showProbeItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        options.setShowProbeThread(options.isShowProbeThread() ? false : true);
                        
                    }
                }
                );
        
//		Add the Return
        optionMenu.addSeparator();
		confirmExitItem = new JCheckBoxMenuItem("Confirm Exit ");
		confirmExitItem.setMnemonic('c');
		confirmExitItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		confirmExitItem.setEnabled(true);
		if (options.isConfirmExitIfActive()) {
			confirmExitItem.setSelected(true);
		} else {
			confirmExitItem.setSelected(false);
		}
		confirmExitItem.setToolTipText("Confirm Exit if Active Clients Exists");
		optionMenu.add(confirmExitItem);


		//create a listener for it.
		confirmExitItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						options.setConfirmExitIfActive(options.isConfirmExitIfActive()? false : true);
					}
				}
				);
		
		style = new JCheckBoxMenuItem[lookAndFeel.length];
		
		for (int i = 0; i < lookAndFeel.length; i++) {
		    style[i] = new JCheckBoxMenuItem(lookAndFeel[i].getName());
		    //if (lookAndFeel[i].getClassName().equals(UIManager.getSystemLookAndFeelClassName())) 
		    //		style[i].setSelected(true);
		    if (i == options.getLookAndFeel()) style[i].setSelected(true);
		    lookAndFeelMenu.add(style[i]);
		    //lookAndFeelIndex = i;
		    style[i].addActionListener(
		            new ActionListener() 
		            {
		                public void actionPerformed(ActionEvent e)
		                {
		                    for (int j = 0; j < lookAndFeel.length; j++) {
	                          if (lookAndFeel[j].getName().equals(e.getActionCommand())) {
	                              changeLookAndFeel(j, e.getActionCommand() );
	                              style[j].setSelected(true);
	                              	                            
	                          } else {
	                          	  style[j].setSelected(false);
	                          }
	                      }
                            
		                }
		            }
		            );
		}
		
		optionMenu.addSeparator();
		
		JMenu statusBarMenu = new JMenu("Status Bar");
		statusBarMenu.setMnemonic('b');
		statusBarMenu.setToolTipText("Status Bar Type");
		optionMenu.add(statusBarMenu);
		
		final JCheckBoxMenuItem sbNoneItem = new JCheckBoxMenuItem(SB_NONE);
		sbNoneItem.setMnemonic('n');
		sbNoneItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		sbNoneItem.setEnabled(true);
		if (options.getStatusBarType() == UIOptions.NO_STATUS_BAR) {
			sbNoneItem.setSelected(true);
		} else {
			sbNoneItem.setSelected(false);
		}
		sbNoneItem.setToolTipText("No Status Bar");
		statusBarMenu.add(sbNoneItem);

		final JCheckBoxMenuItem sbStandardItem = new JCheckBoxMenuItem(SB_STANDARD);
		sbStandardItem.setMnemonic('s');
		sbStandardItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		sbStandardItem.setEnabled(true);
		if (options.getStatusBarType() == UIOptions.STANDARD_STATUS_BAR) {
			sbStandardItem.setSelected(true);
		} else {
			sbStandardItem.setSelected(false);
		}
		sbStandardItem.setToolTipText("Standard Status Bar");
		statusBarMenu.add(sbStandardItem);


		final JCheckBoxMenuItem sbGraphicItem = new JCheckBoxMenuItem(SB_GRAPHICAL);
		sbGraphicItem.setMnemonic('g');
		sbGraphicItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		sbGraphicItem.setEnabled(true);
		if (options.getStatusBarType() == UIOptions.GRAPHICAL_STATUS_BAR) {
			sbGraphicItem.setSelected(true);
		} else {
			sbGraphicItem.setSelected(false);
		}
		sbGraphicItem.setToolTipText("Standard Status Bar");
		statusBarMenu.add(sbGraphicItem);

		
		ActionListener sbListener = new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						sbNoneItem.setSelected(false);
						sbStandardItem.setSelected(false);
						sbGraphicItem.setSelected(false);
						String command = e.getActionCommand();
						
						if (command.equals(SB_NONE)) {
							sbNoneItem.setSelected(true);
							options.setStatusBarType(UIOptions.NO_STATUS_BAR);
						} else if (command.equals(SB_STANDARD)) {
							sbStandardItem.setSelected(true);
							options.setStatusBarType(UIOptions.STANDARD_STATUS_BAR);

						} else if (command.equals(SB_GRAPHICAL)) {
							sbGraphicItem.setSelected(true);
							options.setStatusBarType(UIOptions.GRAPHICAL_STATUS_BAR);

						}
						setStatusBar();
					}
				};
				
		sbNoneItem.addActionListener(sbListener);
		sbStandardItem.addActionListener(sbListener);
		sbGraphicItem.addActionListener(sbListener);
		
		optionMenu.addSeparator();
		
		JMenuItem allOther = new JMenuItem("Other Options...");
		allOther.setMnemonic('A');
		allOther.setToolTipText("View/Change Other Options");
		allOther.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		optionMenu.add(allOther);
		allOther.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        new OptionsDialog(instance, options);
                                                
                    }
                });
		//Create the Help Menu
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		
		//Add the About...
		JMenuItem aboutItem = new JMenuItem("About...");
		aboutItem.setMnemonic('A');
		aboutItem.setToolTipText("Show About Dialog");
		aboutItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		helpMenu.add(aboutItem);
		//create a listener for it.
		aboutItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						new AboutDialog(instance);
												
				  	}
				});
		//Add the About...
        JMenuItem errorLogItem = new JMenuItem("View The Error Log");
        errorLogItem.setMnemonic('E');
        errorLogItem.setToolTipText("Show About Dialog");
        errorLogItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        helpMenu.add(errorLogItem);
        //create a listener for it.
        errorLogItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        new ErrorLogDialog(instance);
                                                
                    }
                });
        
        JMenuItem usageItem = new JMenuItem("Probe Usage Help");
        usageItem.setMnemonic('U');
        usageItem.setToolTipText("Show Probe Usage");
        usageItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_U, ActionEvent.CTRL_MASK));
        helpMenu.addSeparator();
        helpMenu.add(usageItem);
        //create a listener for it.
        usageItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        new HelpDialog(instance, "Probe Usage Info", usageMessage());
                                                
                    }
                });
        JMenuItem networkItem = new JMenuItem("Network Interfaces Info");
        networkItem.setMnemonic('N');
        networkItem.setToolTipText("Show Network Interfaces");
        networkItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        helpMenu.addSeparator();
        helpMenu.add(networkItem);
        //create a listener for it.
        networkItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        new NetworkInterfacesDialog(instance);
                                                
                    }
                });
		
		//Create the Menu Bar and add the menu entries.
		JMenuBar menuBar = new JMenuBar();
		JMenu windowMenu = dtm.createWindowMenu();
		windowMenu.setMnemonic('W');
		setJMenuBar (menuBar);
		menuBar.add (fileMenu);
		menuBar.add (optionMenu);
		menuBar.add (windowMenu);
		menuBar.add( helpMenu);
	}
	
	
	
	protected void exitProgram() {
		
		boolean activeClients = false;
		
		for (int i = 0; i < clients.size(); i++) {
			JProbeClientFrame frame = clients.get(i);
			if (!frame.hasEnded()) {
				activeClients = true;
			}
		}
		
		if (!activeClients || !options.isConfirmExitIfActive()) {
			//no need to confirm
			
		}else if ( JOptionPane.showConfirmDialog(this, 
 				"You Have Open Client Frames, Are You Sure You Want to Exit?",
 				"Quit?",
 				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
     	} else {
     		return;
     	}
	    /*
	     * Disconnect any active clients,\
	     * save the options,
	     * free all Swing/AWT resources
	     * Notify "main" that we are done
	     */
		logger.info("Shutting Down...");
		scanner.shutdown();
		timer.stop();
		logger.info("Disconnecting Active Sessions");
		disconnect();
		options.save();
		this.dispose();
		logger.info(Utilities.WINDOW_TITLE + " is Terminating");
		logger.shutdown();
		
		System.exit(0);
	}
	private void changeLookAndFeel(int style) {
	    
	    
	    try {
	        UIManager.setLookAndFeel(lookAndFeel[style].getClassName());
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this,"Could not change the Look and Feel to " +
	                lookAndFeel[style].getClassName() + ", Error is " +
                    e.getLocalizedMessage(),"Look And Feel Error",JOptionPane.ERROR_MESSAGE);
	        logger.logException(e, this);
	    }
	    bgColor = this.getBackground();
	    this.style[style].setSelected(true);
	    SwingUtilities.updateComponentTreeUI(this);
	    options.setLookAndFeel(style);
	    
	}
	
	private void changeLookAndFeel(int style, String name) {
	    
	    logger.info("Changing Look and Feel to " + name);
		MetalTheme selectedTheme = tmgr.getTheme(name); 
		
		if (selectedTheme == null ) {
			changeLookAndFeel(style);
			return;
		}
		
		MetalLookAndFeel.setCurrentTheme(selectedTheme);
	    try {
		UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
	    } catch (Exception ex) {
	   
	    }
	    
	    
	    this.style[style].setSelected(true);
	    SwingUtilities.updateComponentTreeUI(this);
	    
	}
	
	protected void connect() {
		
		removeInactiveClients();
		
		
		ConnectDialog dlg = new ConnectDialog(instance, 
				instance.host, 
				instance.port,
				options); 
				
		
		if (!dlg.isDialogCanceled()) {
		    
		   
    			addClient( dlg.getName(), 
    					   dlg.getHost(),
    					   dlg.getIpAddress(), 
    					   dlg.getPort(),
    					   dlg.getClientUserId(),
    					   dlg.getClientPassword(),
    					   dlg.getSessionFile(),
    					   dlg.isRemoteConnection(),
    					   dlg.isRecordedSession());
    			 if (!dlg.isRecordedSession()) {
    			     logger.info("Adding Client " + dlg.getName() + " at " + 
    			        dlg.getHost() + ":" +
                        dlg.getIpAddress() + ":" + 
                        dlg.getPort());
    			 } else {
    			     logger.info("Adding Recorded Session Client " + dlg.getSessionFile());		    
	   			 }
		} 
		
		
		
		
	}
	private void updateLocation() {
		
		Point p = this.getLocation();
		
		options.setParentX(p.x);
		options.setParentY(p.y);
		options.setParentWidth(this.getWidth());
		options.setParentHeight(this.getHeight());
		
				
	}
	protected void addClient(BroadcastMessage bm) {
	    addClient(bm.idName, bm.hostName, bm.hostIP, bm.portNumber, 
	            null, null, null, false, false);
	}
	protected void addClient(String name, String host, String hostIP, 
					int port, String userid, String password, 
					String filename, boolean isRemote, boolean isRecorded) {
		
		for (int i = 0; i < clients.size(); i++) {
			JProbeClientFrame frame = clients.get(i);
			
			if (frame.getHostIP().equals(hostIP) && 
			        frame.getHostPort() == port && 
			        frame.isConnected) {
				JOptionPane.showMessageDialog(this,"You Are Already Connected to This Probe"
		                ,"Already Connected",JOptionPane.ERROR_MESSAGE);
				return;
			}
			
		}
		//Create a new Client Frame
		JProbeClientFrame cframe = null;
		try {
		    if (!isRecorded) {
		        cframe = new JProbeClientFrame(name, host, hostIP, port, 
											isRemote, userid, password, options, this);
		        logger.info("Adding Client Frame for " + host + ":" + 
		                port + "-" + name );
		    } else {
		        File recFile = new File(filename);
		        cframe = new JProbeClientFrame(recFile, options, this);
		        logger.info("Adding Client Frame for Replay File " + filename);
		    }
		} catch (ProbeCommunicationsException e) {
			JOptionPane.showMessageDialog(this,"Could not connect to " +
	                name  + "(" + hostIP + 
	                "):" + port + ", Error is " +
                    e.getLocalizedMessage(),"Conection Error",JOptionPane.ERROR_MESSAGE);
			logger.logException(e,this);
			return;
		}
		
				
		cframe.setBounds(clientFrameCounter * 20, clientFrameCounter * 20,
				(int)(options.getParentWidth() * options.getClientRatio()), 
				(int)(options.getParentHeight() * options.getClientRatio()));
		
		clientFrameCounter = (clientFrameCounter + 1 ) % 5;

		dtm.addInternalFrame(cframe);
			
		//desktop.add(cframe);
		clients.add(cframe);
		cframe.show();
			
		
	}
	protected void disconnect() {
		
		//disconnect any connected clients
	    logger.info("Disconnecting All Connected Clients");
		for (int i = 0; i < clients.size(); i++) {
			JProbeClientFrame frame = clients.get(i);
			if (!frame.hasEnded()) {
				frame.disconnect();
				desktop.remove(frame);
			}
		}
		
	}
	protected void removeInactiveClients() {
		
		
		
		for (int i = 0; i < clients.size(); i++) {
			JProbeClientFrame frame = clients.get(i);
			if (frame.hasEnded()) {
				logger.info("Removing Inactive Client " + frame.getHostIP() +
									":" + frame.getHostPort());
				clients.remove(i);
				desktop.remove(frame);
			}	 
		}
		
	}
   protected void removeClient(JInternalFrame frame) {
        
        
        
        logger.info("Removing Client " + frame.getTitle());
        desktop.remove(frame);
        
        if (frame instanceof OSProcessClientFrame) {
            osProcessFrame = null;
            processItem.setEnabled(true);
        } else {
            clients.remove(frame);
        }
        
        
       
        if (clients.size() == 0 && options.isShowConnectWhenEmpty()) {
            connect();
        }
        
    }

   private void buildQuickConnect() {
       
       if (quickConnect.isSelected()) {
           //System.out.println("Quick Connect is Selected");
           return;
       }
       quickConnect.removeAll();
       Map<String, BroadcastMessage> map = scanner.getHostMap();
       Collection<BroadcastMessage> probes = map.values();
       
       for (final BroadcastMessage bm : probes) {
           
           if (System.currentTimeMillis() - bm.receiveTime < BROADCAST_AGE_OUT ) {
               JMenuItem menuItem = new JMenuItem(bm.idName + ":" + 
                       bm.hostName + ":" + bm.hostIP + ":" + bm.portNumber);
               menuItem.addActionListener(
                   new ActionListener()
                   {
                       public void actionPerformed (ActionEvent e)
                       {
                           
                           addClient(bm);
                           
                       }
                   }
                   );
               quickConnect.add(menuItem);
           }
       }
       
	       
   }
   private void addProcessPanel(boolean allUsers) {
       
       osProcessFrame = new OSProcessClientFrame(options, instance, allUsers);
       osProcessFrame.setBounds(clientFrameCounter * 20, clientFrameCounter * 20,
               (int)(options.getParentWidth() * options.getClientRatio()),
               (int)(options.getParentHeight() * options.getClientRatio()));
       
       clientFrameCounter = (clientFrameCounter + 1 ) % 5;

       dtm.addInternalFrame(osProcessFrame);
       
       osProcessFrame.show();
       processItem.setEnabled(false);
   }
	protected void setStatusBar() {

		if (status != null) {
			remove(status);
			validate();
			status.setVisible(false);
			status = null;
		}
		
		if (options.getStatusBarType() == UIOptions.NO_STATUS_BAR) {
			return;
		} else if (options.getStatusBarType() == UIOptions.STANDARD_STATUS_BAR) {
			status = new StatusBar(1000, false);
		} else {
			status = new GraphStatusBar(1000, options.getGraphicStatusType());
		}
		
		add(status, BorderLayout.SOUTH);
		validate();
		repaint();

	}
	public static JFrame getStaticFrame() {
	    return staticFrame;
	}
	
	private void open() {
	    
	    File path = new File(options.getRecordingDirectory());
        JFileChooser chooser = new JFileChooser();
        
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle("Specify the Recording to Open");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilter() {
           public boolean accept(File f) {
               if (f.isDirectory()) {
                   return true;
               }
                return f.getAbsolutePath().endsWith(UIOptions.RECORDING_SUFFIX);
            }

            @Override
            public String getDescription() {
                return "Recorded Probe Files (*" + UIOptions.RECORDING_SUFFIX + ")";
            }
            
        });
        chooser.setCurrentDirectory(path);
        
                 
        int returnVal = chooser.showOpenDialog(instance);
 
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           String fileName = chooser.getSelectedFile().getAbsolutePath();
           addClient("PlayBack", fileName, fileName, 0, "", "", fileName, false, true);
        } 
	}
	private static void usage() {
	    
	   
	    
	  msg("\n" + Utilities.VERSION_HEADING + " Usage...");
	  msg("\tTo enable the Probe for a JVM, add the following VM Option:");
	  msg("\t\t-javaagent:<Path to ProbeJarFile>=<Name Identifier>");
	  msg("\t\t\tWhere: <Path to ProbeJarFile> is the path to the jar file");
	  msg("\t\t\t  And: <Name Identifier> is a name (no spaces) to identify the process");
	  msg("\n\t\tOptional Environment Variables:");
	  msg("\t\t\tJPROBE_PREFERRED_NETWORK: Specify the Interface for the probe to bind to");
	  msg("\t\t\tJPROBE_PREFERRED_IP: Specify the IP address for the probe to bind to");
	  msg("\t\t\tJPROBE_DEBUG: To see debug messages from the probe");
	  msg("\t\t\tJPROBE_ENABLE_RECORDING: Have probe record to a file for playback at a later time");
	  msg("\t\t\tJPROBE_RECORDING_INTERVAL: Specify interval (in milliseconds) for recording samples");
	  msg("\t\t\tJPROBE_RECORDING_DIRECTORY: Directory to write recording to");
	  msg("\n\tTo get information on the available network interfaces, " + 
	          "re-enter this command with the following option: /a "); 
	    
	    
	}
	private static String usageMessage() {
	    
	    StringBuilder sb = new StringBuilder();
	    
	    sb.append("To enable the Probe for a JVM, add the following VM Option:\n");
	    sb.append("\t-javaagent:<Path to ProbeJarFile>=<Name Identifier>\n");
	    sb.append("\t     Where: <Path to ProbeJarFile> is the path to the jar file\n");
	    sb.append("\t       And: <Name Identifier> is a name (no spaces) to identify the process\n");
	    sb.append("\n\tExample: java -javaagent:c:\\JProbe\\JProbe.jar=MyProgram HelloWorld\n");
	    sb.append("\nOptional Environment Variables:\n");
	    sb.append("\tJPROBE_PREFERRED_NETWORK: Specify the Interface for the probe to bind to\n");
	    sb.append("\tJPROBE_PREFERRED_IP: Specify the IP address for the probe to bind to\n");
	    sb.append("\tJPROBE_DEBUG: To see debug messages from the probe\n");
	    sb.append("\tJPROBE_ENABLE_RECORDING: Have probe record to a file for playback at a later time\n");
	    sb.append("\tJPROBE_RECORDING_INTERVAL: Specify interval (in milliseconds) for recording samples\n");
	    sb.append("\tJPROBE_RECORDING_DIRECTORY: Directory to write recording to\n");
	    
	    
	    return sb.toString();
	}
	private static void msg(String msg) {
	    System.out.println(msg);
	}
    public static UIOptions getOptions() {
        return options;
    }
	public static void main(String[] args) {
		String fileName = null;				
		//Fire it up!!!
	    if (args.length > 0) {
	        if (args[0].equals("/h")){
	            usage();
	            return;
	        } else if (args[0].equals("/a")){
	            try {
                    Utilities.showInterfaces(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
	            return;
	        } else {
	            
	            File f = new File(args[0]);
	            if (f.exists() && 
	                    args[0].endsWith(UIOptions.RECORDING_SUFFIX)) {
	                fileName = args[0];
	            }
	        }
	        		
	    }
		new JProbeUI(fileName);
		
		//Utilities.debugMsg("Main has Ended");
		
	}
}
