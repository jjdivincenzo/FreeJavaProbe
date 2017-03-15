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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import edu.regis.jprobe.jni.OSSystemInfo;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.Utilities;
import edu.regis.jprobe.ui.helpers.GridBagLayoutHelper;
import edu.regis.jprobe.ui.helpers.JLabelHelper;
import edu.regis.jprobe.ui.helpers.JProgressBarHelper;
import edu.regis.jprobe.ui.helpers.JTextFieldHelper;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author jdivince
 *
 * This is the internal frame for a connected or playback client.
 */
public class OSProcessClientFrame extends JInternalFrame implements Thread.UncaughtExceptionHandler{
	
    private static final long serialVersionUID = 1L;
	private String hostName;


	private UIOptions options;
	private Logger logger;
	private boolean hasEnded = false;
	private int updateInterval = 1000;
	
	//Swing Components
	private JTabbedPane tabPerf;
	private TimerThread timer;
	private ActionListener updateAL;

	protected JProbeUI parent;
	private OSProcessesPanel processPanel;
	private String title = "Processes on System ";
	private OSProcessClientFrame instance;
	private OperatingSystemMXBean osmb;
	
	private JProgressBar totalCPU;
	private JTextField totalProcesses;
	private JTextField totalThreads;
	private JTextField totalHandles;
	private JProgressBar totalPhysical;
	private JProgressBar totalCommitted;
	private boolean allUsers = false;
	private String userName;
	
	public OSProcessClientFrame( 
							 UIOptions uio,
							 JProbeUI parent,
							 boolean allUsers) {
		
		super("OS Processes", true,true, true, true);
		logger = Logger.getLogger();
		
		this.allUsers = allUsers;
		this.options = uio;
		this.parent = parent;
		this.instance = this;
		this.userName = System.getProperty("user.name");
		try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "localhost";
        }
		title = title + hostName;
		
		setTitleName(false);
        
        logger.info("Starting OS Process Client Frame for " + hostName );

        osmb = Utilities.getSunOSBean();
        
        
        
		init(false);
		
	}
	private void setTitleName(boolean paused) {
	    String msg = title;
	    
	    if (allUsers) {
	        msg += " for All Users";
	    } else {
	        msg += " for User " + userName;
	    }
	    
	    if (paused) {
	        msg += " (Paused)";
	    }
	    
	    this.setTitle(msg);
	}
	/**
	 * 
	 * @param playBackMode
	 */
	private void init(boolean playBackMode)  {
	    
	    Icon icon = IconManager.getIconManager().getFrameIcon();   
        if (icon != null) setFrameIcon(icon);
        
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		//Create the Tabbed Panel and all of the tab components
		tabPerf = new JTabbedPane(JTabbedPane.TOP);
		tabPerf.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		createMenu();
		processPanel = new OSProcessesPanel(this, options);
		
		setLayout(new GridBagLayout());
		GridBagConstraints cc = new GridBagConstraints();
		cc.fill = GridBagConstraints.BOTH;
		cc.gridx=0;
		cc.gridy=0;
		cc.gridwidth=1;
		cc.gridheight=15;
		cc.weightx=1;
		cc.weighty=1;
		add(processPanel, cc);
		
		updateAL = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        update();
		        
		    }
		};
		
		JPanel statusPanel = createStatusPanel();
		cc.gridx=0;
        cc.gridy=15;
        cc.gridwidth=1;
        cc.gridheight=1;
        cc.weightx=1;
        cc.weighty=0;
        
        if (osmb != null) {
            add(statusPanel, cc);
        }
		
		timer = new TimerThread(updateAL, updateInterval, "OSProcess");
		timer.setUncaughtExceptionHandler(this);
		timer.setRunning(true);
		
		
		
		addInternalFrameListener( new InternalFrameAdapter () {
			
			public void internalFrameClosing(InternalFrameEvent e) {
				
				closeClient();
			}
		});
		
		
	}


	private void createMenu() {
	    
	    //Create the File Menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		
		

		final JCheckBoxMenuItem allUsersItem = new JCheckBoxMenuItem("Show Processes for All Users");
		allUsersItem.setMnemonic('A');
		allUsersItem.setToolTipText("To Show Processes for All Users");
		
        if (allUsers) {
            allUsersItem.setSelected(true);
        }
        
		if (!OSSystemInfo.isOperational()) {
		    allUsersItem.setEnabled(false);
		} else {
		    if (!OSSystemInfo.isAdmin()) {
		        allUsersItem.setText("Show Processes for All Users (Will Force a Restart)");
		    }
		}
        
        //create a listener for it.
		allUsersItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        if (!allUsers) {
                            if (!OSSystemInfo.isAdmin()) {
                                long ret = OSSystemInfo.selfElevate();
                                if (ret == OSSystemInfo.SYSTEM_SELF_ELEVATE_CANCELLED) {
                                    allUsersItem.setSelected(false);
                                    return;
                                }
                                if (ret == OSSystemInfo.SYSTEM_SELF_ELEVATE_FAILED) {
                                    JOptionPane.showMessageDialog(instance, 
                                            "Unable to Self Elevate, Restart as Administrator", "Failed",
                                            JOptionPane.ERROR_MESSAGE);
                                    allUsersItem.setSelected(false);
                                    return;
                                }
                                options.setWasSelfElevated(true);
                                parent.exitProgram();
                            } else {
                                allUsers = true;
                                allUsersItem.setSelected(true);
                                setTitleName(false);
                            }
                        } else {
                            allUsers = false;
                            allUsersItem.setSelected(false);
                            setTitleName(false);
                        }
                    }
                }
        );
        
        fileMenu.add(allUsersItem);
        fileMenu.addSeparator();
        
		//Add the close
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.setMnemonic('C');
		closeItem.setToolTipText("To Close This Window");
		
		
		//create a listener for it.
		closeItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						closeClient();
					}
				}
				);
		
		fileMenu.add(closeItem);
		fileMenu.addSeparator();
		
		//Add the print 
		final JMenuItem pauseResumeItem = new JMenuItem("Pause");
		pauseResumeItem.setMnemonic('P');
		pauseResumeItem.setToolTipText("Pause Updates");
		pauseResumeItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		
		//create a listener for it.
		pauseResumeItem.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						if (processPanel.isPaused()) {
						    pauseResumeItem.setText("Pause");
						    pauseResumeItem.setMnemonic('P');
						    pauseResumeItem.setToolTipText("Pause Updates");
					        pauseResumeItem.setAccelerator(KeyStroke.getKeyStroke(
					                KeyEvent.VK_P, ActionEvent.CTRL_MASK));
					        processPanel.setPaused(false);
					        setTitleName(false);
						} else {
						    pauseResumeItem.setText("Resume");
                            pauseResumeItem.setMnemonic('R');
                            pauseResumeItem.setToolTipText("Resume Updates");
                            pauseResumeItem.setAccelerator(KeyStroke.getKeyStroke(
                                    KeyEvent.VK_R, ActionEvent.CTRL_MASK));
                            processPanel.setPaused(true);
                            setTitleName(true);
						}
					}
				}
				);
		//		 Add the Close
		fileMenu.add(pauseResumeItem); 
		

		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar (menuBar);
		menuBar.add(fileMenu);
	}



    protected void update() {

    	processPanel.update();
    	ProcessDataModel model = processPanel.getProcessTableModel();
 
    	
    	if (osmb == null) {
    	    return;
        }
    	
    	long phySize = osmb.getTotalPhysicalMemorySize();
    	long virtSize = osmb.getTotalSwapSpaceSize();
    	long phyFree = osmb.getFreePhysicalMemorySize();
    	long virtFree = osmb.getFreeSwapSpaceSize();
    	
    	double cpuPercent = osmb.getSystemCpuLoad() * 100;
    	double phyPercent = ((double)(phySize - phyFree) / (double) phySize) * 100d;
    	double virtPercent = ((double)(virtSize - virtFree) / (double) virtSize) * 100d;
    	
    	totalCPU.setString(Utilities.format(cpuPercent, 2) + "%");
    	totalPhysical.setString(Utilities.format(phyPercent, 2) + "%");
    	totalCommitted.setString(Utilities.format(virtPercent, 2) + "%");
    	
    	totalCPU.setValue((int) cpuPercent);
    	totalPhysical.setValue((int) phyPercent);
    	totalCommitted.setValue((int) virtPercent);
    	
    	//totalCPU.setForeground(getPBColor(cpuPercent, 0));
    	//totalPhysical.setForeground(getPBColor(phyPercent, 1));
    	//totalCommitted.setForeground(getPBColor(virtPercent, 1));
    	
    	totalProcesses.setText("Total Processes: " + Utilities.format(model.getTotalProcesses()));
    	totalThreads.setText("Total Threads: " + Utilities.format(model.getTotalThreads()));
    	totalHandles.setText("Total Handles: " + Utilities.format(model.getTotalHandles()));
    }

    private JPanel createStatusPanel() {
        
        JPanel ret = new JPanel();
        ret.setBorder(new BevelBorder(2));
        ret.setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();
        cc.fill = GridBagConstraints.BOTH;
        Color lblColor = Color.BLACK;
        Dimension largeTextSize  = new Dimension(100,30);
        Dimension headingSize  = new Dimension(50,30);
        double labelWeight = 0.05;
        double pbWeight = 0.1;
        double txtWeight = 0.15;
        
        GridBagLayoutHelper helper = new GridBagLayoutHelper(ret, cc, 0.0, 0.0);
        JProgressBarHelper jpbh = new JProgressBarHelper();
        JTextFieldHelper jtfh = new JTextFieldHelper(false, largeTextSize, null);
        JLabelHelper jlh = new JLabelHelper(JLabel.RIGHT, headingSize, lblColor, null, null);
        jtfh.setDefaultFont(ret.getFont().deriveFont(Font.BOLD));
        jpbh.setDefaultPreferredSize(largeTextSize);
        
        totalCPU = jpbh.newProgressBar("Total CPU%");        
        totalPhysical = jpbh.newProgressBar("Physical Usage%");
        totalCommitted = jpbh.newProgressBar("Commit Charge%");
        totalProcesses = jtfh.newTextField("Total Processes");
        totalThreads = jtfh.newTextField("Total Threads");
        totalHandles = jtfh.newTextField("Total Threads");
        JLabel l1 = jlh.newLabel("Total CPU");
        JLabel l2 = jlh.newLabel("Physical Usage");
        JLabel l3 = jlh.newLabel("Commit Charge");
        
        totalCPU.setStringPainted(true);
        totalCPU.setString("0.00%");
        //totalCPU.setForeground(Color.GREEN);
        totalCPU.setMaximum(0);
        totalCPU.setMaximum(100);
        totalPhysical.setStringPainted(true);
        totalPhysical.setString("0.00%");
        //totalPhysical.setForeground(Color.GREEN);
        totalPhysical.setMaximum(0);
        totalPhysical.setMaximum(100);
        totalCommitted.setStringPainted(true);
        totalCommitted.setString("0.00%");
        //totalCommitted.setForeground(Color.GREEN);
        totalCommitted.setMaximum(0);
        totalCommitted.setMaximum(100);
        
        helper.addColumn(totalProcesses, 2, 1, txtWeight, 1);
        helper.addColumn(totalThreads, 2, 1, txtWeight, 1);
        helper.addColumn(totalHandles, 2, 1, txtWeight, 1);
        helper.addColumn(l1, 1, 1, labelWeight, 1);
        helper.addColumn(totalCPU, 4, 1, pbWeight, 1);
        helper.addColumn(l2, 1, 1, labelWeight, 1);
        helper.addColumn(totalPhysical, 4, 1, pbWeight, 1);
        helper.addColumn(l3, 1, 1, labelWeight, 1);
        helper.addColumn(totalCommitted, 4, 1, pbWeight, 1);
        return ret;
        
    }
	
	protected void closeClient() {
		hasEnded = true;
		//if (timer != null) timer.stop();
		if (timer != null) timer.shutdown();
		logger.info("Closing Client for OS Process Info for " + 
                 hostName);
		
		
		this.dispose();
		parent.removeClient(this);
	}

	/**
	 * @return Returns the hasEnded.
	 */
	public boolean hasEnded() {
		return hasEnded;
	}

	public JProbeUI getParentUI() {
	    return parent;
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
                timer = new TimerThread(updateAL, updateInterval, "OSProcess");
                timer.setUncaughtExceptionHandler(this);
                
            }
    }

	protected Color getPBColor(double val, int type) {
	    
	    Color okColor = options.getCpuColorOk();
	    Color warnColor = options.getCpuColorWarn();
	    Color badColor = options.getCpuColorBad();
	    
	    if (type == 1) {
	        okColor = options.getHeapColorOk();
	        warnColor = options.getHeapColorWarn();
	        badColor = options.getHeapColorBad();
	    }
	    
	    
	    Color color = Color.GREEN;
	    
	    if (val < options.getCpuThresholdWarn()) {
	        color = okColor;
        } else if (val < options.getCpuThresholdBad()) {
            color = warnColor;
        }else {
            color = badColor;
        }
	    
	    return color;
	}
	
	public boolean allUsersSelected() {
	    return allUsers;
	}
    public void setAllUsersSelected(boolean val) {
        allUsers = val;
    }
}
