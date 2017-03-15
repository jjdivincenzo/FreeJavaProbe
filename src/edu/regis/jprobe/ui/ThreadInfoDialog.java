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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.ProbeCommunicationsException;
import edu.regis.jprobe.model.ProbeCommunicationsManager;
import edu.regis.jprobe.model.ProbeRequest;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.ThreadInfoData;
import edu.regis.jprobe.model.Utilities;

/**
 * 
 * 
 * @author jdivincenzo
 *
 * 
 */
public class ThreadInfoDialog extends JFrame { 
	

    private static final long serialVersionUID = 1L;
    private int frameWidth = 1000;
	private int frameHeight = 700;
	private int buttonWidth=30;
	private int buttonHeight=30;

	// Swing component
	private JProgressBar currentCPUUsage;
	private JTextArea stackTrace;
	private JTextArea monitorLocks;
	private JTextField totalCPU;
	private JTextField totalUserCPU;
	private JTextField blockCount;
	private JTextField blockTime;
	private JTextField lockName;
	private JTextField lockOwnerId;
	private JTextField lockOwnerName;
	private JTextField threadState;
	private JTextField waitCount;
	private JTextField waitTime;
	private JTextField inNative;
	private JTextField isSuspended;
	private JTextField priority;
	private JTextField daemon;
	private JTextField classLoader;
	private JTextField bytesAllocated;
	private JButton btnClose;
	private JButton btnPause;
	private JButton btnInterrupt;
	private JButton btnCopy;
	private JButton btnCopyAll;
	private JButton btnKill;
	private JButton btnDock;
	private JPanel p3;
	private Timer timer;
	public static final long NANOS_PER_MILLI = 1000000;
	private static final int CLOSE_DEAD_COUNT = 5;
	private int countDown = CLOSE_DEAD_COUNT;
	private long threadId;
	private String threadName;
	private String hostIP;
	private UIOptions options;
	private long startTime;
	private long endTime;
	private long lastCPUTime;
	private long lastWCTime; 
	private int threadDiedCount=0;
	private int maxSize = 1000;
	private int traceNo = 0;
	private ThreadMXBean threadBean;
	private ProbeCommunicationsManager pcm;
	private ThreadInfoData tid;
	private Vector<String> stackTraces;
	private SelectedThreadCPUGraph cpuGraph; 
	private String[] legend;
	private JProbeClientFrame client;
	private JPanel mainPanel;
	private boolean docked = false;

	/**
	 * This is the default ctor to initialize all of the swing components
	 *
	 */

	
	public ThreadInfoDialog(String threadName,
				String hostIP,
				long threadId,
				UIOptions options,
				ProbeCommunicationsManager pcm, 
				JProbeClientFrame client) {
	    
	    this.threadId = threadId;
        this.pcm = pcm;
        this.hostIP = hostIP;
        this.threadName = threadName;
        this.options = options;
        this.client = client;
        
        stackTraces = new Vector<String>();
        maxSize = this.options.getMaxTraceSize();

		startTime = System.nanoTime();
		threadBean = ManagementFactory.getThreadMXBean();
		try {threadBean.setThreadContentionMonitoringEnabled(true);
		} catch(Exception e) {}
		 
		ImageIcon icon = IconManager.getIconManager().getThreadIcon();	
 		if (icon != null) this.setIconImage(icon.getImage());
		
		Point p = new Point(options.getParentX(), options.getParentY());
		int x = p.x + ((options.getParentWidth() - frameWidth) / 2);
		int y = p.y + ((options.getParentHeight() - frameHeight) / 2);
		setLocation (x,  y);
		
		
		setSize(frameWidth,frameHeight);
		this.setResizable(true);
		int yPos = 0;
		
		mainPanel = new JPanel();
		
		setTitle("Thread Info for [" + hostIP + ":" + threadName + "] - [" +
				threadId +"]");		
		Color lblColor = Color.GRAY;
					
		Dimension headingSize = new Dimension(50,30);
		Dimension largeTextSize = new Dimension(100,30);
		Dimension hugeTextSize = new Dimension(350,80);
		legend = new String[1];
		legend[0] = threadName;
		
		cpuGraph = new SelectedThreadCPUGraph(options.getSampleRate(), legend,
                "CPU History", options);
		
		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());
		TitledBorder tb0 = new TitledBorder( 
		        new EtchedBorder(), "Thread Info [" + threadName + ":" + threadId +"]");
		p1.setBorder(tb0);
		tb0.setTitleColor(Color.blue);
		GridBagConstraints c = new GridBagConstraints();
		c.insets= new Insets(1,1,1,1);
		c.fill = GridBagConstraints.BOTH;
		
		JLabel l0 = new JLabel("Current CPU%)");
		l0.setHorizontalAlignment(JLabel.RIGHT);
		l0.setPreferredSize(headingSize);
		l0.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l0,c);
		currentCPUUsage = new JProgressBar();
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=3;
		c.gridheight=1;
		c.weightx=1.0;
		c.weighty = 1.0;
		currentCPUUsage.setStringPainted(true);
		currentCPUUsage.setPreferredSize(largeTextSize);
		currentCPUUsage.setValue(0);
		currentCPUUsage.setMaximum(0);
		currentCPUUsage.setMaximum(100);
		p1.add(currentCPUUsage,c);
		yPos++;
		// CPU
		JLabel l1 = new JLabel("Thread CPU Time(ms)");
		l1.setHorizontalAlignment(JLabel.RIGHT);
		l1.setPreferredSize(headingSize);
		l1.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l1,c);
		// CPU Time
		totalCPU = new JTextField();
		totalCPU.setEditable(false);
		totalCPU.setPreferredSize(largeTextSize);
		Font fontTF = totalCPU.getFont();
		fontTF = fontTF.deriveFont(Font.BOLD);
		totalCPU.setFont(fontTF);
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(totalCPU,c);
		// User Time
		JLabel l2 = new JLabel("Thread User Time(ms)");
		l2.setHorizontalAlignment(JLabel.RIGHT);
		l2.setPreferredSize(headingSize);
		l2.setForeground(lblColor);
		c.gridx=2;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l2,c);
		totalUserCPU = new JTextField();
		totalUserCPU.setEditable(false);
		totalUserCPU.setPreferredSize(largeTextSize);
		totalUserCPU.setFont(fontTF);
		c.gridx=3;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(totalUserCPU,c);
		yPos++;
		
//		 CPU
		JLabel l3 = new JLabel("Block Count");
		l3.setHorizontalAlignment(JLabel.RIGHT);
		l3.setPreferredSize(headingSize);
		l3.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l3,c);
		// CPU Time
		blockCount = new JTextField();
		blockCount.setEditable(false);
		blockCount.setPreferredSize(largeTextSize);
		blockCount.setFont(fontTF);
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(blockCount,c);
		// User Time
		JLabel l4 = new JLabel("Block Time(ms)");
		l4.setHorizontalAlignment(JLabel.RIGHT);
		l4.setPreferredSize(headingSize);
		l4.setForeground(lblColor);
		c.gridx=2;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l4,c);
		blockTime = new JTextField();
		blockTime.setEditable(false);
		blockTime.setPreferredSize(largeTextSize);
		blockTime.setFont(fontTF);
		c.gridx=3;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(blockTime,c);
		yPos++;	
		
		//		 CPU
		JLabel l5 = new JLabel("Lock Name");
		l5.setHorizontalAlignment(JLabel.RIGHT);
		l5.setPreferredSize(headingSize);
		l5.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l5,c);
		// CPU Time
		lockName = new JTextField();
		lockName.setEditable(false);
		lockName.setPreferredSize(largeTextSize);
		lockName.setFont(fontTF);
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=3;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(lockName,c);
		// User Time
		
		yPos++;	
		
//		 CPU
		JLabel l7 = new JLabel("Lock Owner Name");
		l7.setHorizontalAlignment(JLabel.RIGHT);
		l7.setPreferredSize(headingSize);
		l7.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l7,c);
		// CPU Time
		lockOwnerName = new JTextField();
		lockOwnerName.setEditable(false);
		lockOwnerName.setPreferredSize(largeTextSize);
		lockOwnerName.setFont(fontTF);
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=3;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(lockOwnerName,c);
		yPos++;
		// User Time
		JLabel l6 = new JLabel("Lock Owning Thread");
		l6.setHorizontalAlignment(JLabel.RIGHT);
		l6.setPreferredSize(headingSize);
		l6.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l6,c);
		lockOwnerId = new JTextField();
		lockOwnerId.setEditable(false);
		lockOwnerId.setPreferredSize(largeTextSize);
		lockOwnerId.setFont(fontTF);
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(lockOwnerId,c);
		JLabel l8 = new JLabel("Thread State");
		l8.setHorizontalAlignment(JLabel.RIGHT);
		l8.setPreferredSize(headingSize);
		l8.setForeground(lblColor);
		c.gridx=2;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l8,c);
		threadState = new JTextField();
		threadState.setEditable(false);
		threadState.setPreferredSize(largeTextSize);
		threadState.setFont(fontTF);
		c.gridx=3;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(threadState,c);
		yPos++;	
		
//		 CPU
		JLabel l9 = new JLabel("Wait Count");
		l9.setHorizontalAlignment(JLabel.RIGHT);
		l9.setPreferredSize(headingSize);
		l9.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l9,c);
		// CPU Time
		waitCount = new JTextField();
		waitCount.setEditable(false);
		waitCount.setPreferredSize(largeTextSize);
		waitCount.setFont(fontTF);
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(waitCount,c);
		// User Time
		JLabel l10 = new JLabel("Wait Time(ms)");
		l10.setHorizontalAlignment(JLabel.RIGHT);
		l10.setPreferredSize(headingSize);
		l10.setForeground(lblColor);
		c.gridx=2;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l10,c);
		waitTime = new JTextField();
		waitTime.setEditable(false);
		waitTime.setPreferredSize(largeTextSize);
		waitTime.setFont(fontTF);
		c.gridx=3;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(waitTime,c);
		yPos++;	
		
//		 CPU
		JLabel l11 = new JLabel("In Native Code");
		l11.setHorizontalAlignment(JLabel.RIGHT);
		l11.setPreferredSize(headingSize);
		l11.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l11,c);
		// CPU Time
		inNative = new JTextField();
		inNative.setEditable(false);
		inNative.setPreferredSize(largeTextSize);
		inNative.setFont(fontTF);
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(inNative,c);
		// User Time
		JLabel l12 = new JLabel("Thread Suspended");
		l12.setHorizontalAlignment(JLabel.RIGHT);
		l12.setPreferredSize(headingSize);
		l12.setForeground(lblColor);
		c.gridx=2;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l12,c);
		isSuspended = new JTextField();
		isSuspended.setEditable(false);
		isSuspended.setPreferredSize(largeTextSize);
		isSuspended.setFont(fontTF);
		c.gridx=3;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(isSuspended,c);
		yPos++;	
		
//		 CPU
		JLabel l13 = new JLabel("Daemon Thread");
		l13.setHorizontalAlignment(JLabel.RIGHT);
		l13.setPreferredSize(headingSize);
		l13.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l13,c);
		// CPU Time
		daemon = new JTextField();
		daemon.setEditable(false);
		daemon.setPreferredSize(largeTextSize);
		daemon.setFont(fontTF);
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(daemon,c);
		// User Time
		JLabel l14 = new JLabel("Thread Priority");
		l14.setHorizontalAlignment(JLabel.RIGHT);
		l14.setPreferredSize(headingSize);
		l14.setForeground(lblColor);
		c.gridx=2;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l14,c);
		priority = new JTextField();
		priority.setEditable(false);
		priority.setPreferredSize(largeTextSize);
		priority.setFont(fontTF);
		c.gridx=3;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(priority,c);
		yPos++;	
		
		JLabel l15 = new JLabel("Class Loader");
		l15.setHorizontalAlignment(JLabel.RIGHT);
		l15.setPreferredSize(headingSize);
		l15.setForeground(lblColor);
		c.gridx=0;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(l15,c);
		// CPU Time
		classLoader = new JTextField();
		classLoader.setEditable(false);
		classLoader.setPreferredSize(largeTextSize);
		classLoader.setFont(fontTF);
		c.gridx=1;
		c.gridy=yPos;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		p1.add(classLoader,c);
		
		JLabel l16 = new JLabel("Bytes Allocated");
        l16.setHorizontalAlignment(JLabel.RIGHT);
        l16.setPreferredSize(headingSize);
        l16.setForeground(lblColor);
        c.gridx=2;
        c.gridy=yPos;
        c.gridwidth=1;
        c.gridheight=1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        p1.add(l16,c);
        // CPU Time
        bytesAllocated = new JTextField();
        bytesAllocated.setEditable(false);
        bytesAllocated.setPreferredSize(largeTextSize);
        bytesAllocated.setFont(fontTF);
        c.gridx=3;
        c.gridy=yPos;
        c.gridwidth=1;
        c.gridheight=1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        p1.add(bytesAllocated,c);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());
		TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Thread Stack Trace");
		tb1.setTitleColor(Color.BLUE);
		p2.setBorder(tb1);
		p2.setForeground(Color.BLUE);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(2,2,2,2);
		c2.fill = GridBagConstraints.BOTH;
 
		//System Proerties text box
		stackTrace = new JTextArea();
		JScrollPane scroll = new JScrollPane(stackTrace);
		scroll.setPreferredSize(hugeTextSize);
		c2.gridx=0;
		c2.gridy=0;
		c2.gridwidth=1;
		c2.gridheight=15;
		c2.weightx=1.0;
		c2.weighty=1.0;
		c2.fill = GridBagConstraints.BOTH;
		p2.add(scroll,c2);
		
		p3 = new JPanel();
		p3.setLayout(new GridBagLayout());
		TitledBorder tb2 = new TitledBorder( new EtchedBorder(), "Locks Held");
		tb2.setTitleColor(Color.BLUE);
		p3.setBorder(tb2);
		p3.setForeground(Color.BLUE);
		GridBagConstraints c3 = new GridBagConstraints();
		c3.insets= new Insets(2,2,2,2);
		c3.fill = GridBagConstraints.BOTH;
 
		//System Proerties text box
		monitorLocks = new JTextArea();
		JScrollPane scroll2 = new JScrollPane(monitorLocks);
		scroll2.setPreferredSize(hugeTextSize);
		c3.gridx=0;
		c3.gridy=0;
		c3.gridwidth=1;
		c3.gridheight=5;
		c3.weightx=1.0;
		c3.weighty=1.0;
		c3.fill = GridBagConstraints.BOTH;
		p3.add(scroll2,c3);
		
		
		
		
		// Panel for our action buttons
		JPanel p5 = new JPanel();
		p5.setLayout(new GridLayout(1,1,5,5));
		p5.setBorder(new EtchedBorder( EtchedBorder.LOWERED));

		
		// OK button and event handler
		btnClose = new JButton();
		btnClose.setText("Close");
		btnClose.setSize(buttonWidth, buttonHeight);
		
		p5.add(btnClose);
		btnClose.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)	{
						closeDialog();
					}
				});
		
		
		btnDock = new JButton();
		
		btnDock.setText("Dock");
		btnDock.setToolTipText("Dock it in the Main View as a Tab");
		btnDock.setSize(buttonWidth, buttonHeight);
        
        p5.add(btnDock);
        btnDock.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e) {
                        if (docked) {
                            undock();
                        } else {
                            dock();
                        }
                    }
                });
        
		btnPause = new JButton();
		btnPause.setText("Pause");
		btnPause.setSize(buttonWidth, buttonHeight);
		
		p5.add(btnPause);
		btnPause.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						if (btnPause.getText().equalsIgnoreCase("Pause")) {
							timer.stop();
							btnPause.setText("Resume");
						}else{
							timer.start();
							btnPause.setText("Pause");
						}
					 }
				});

		btnCopy = new JButton();
		
		btnCopy.setText("Copy");
		btnCopy.setToolTipText("Copy Current Stack Trace to the Clipboard");
		btnCopy.setSize(buttonWidth, buttonHeight);
		btnCopy.setEnabled(true);
		p5.add(btnCopy);
		btnCopy.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						copyStackTrace();
					 }
				});
		btnCopyAll = new JButton();
        
        btnCopyAll.setText("Copy All");
        btnCopyAll.setToolTipText("Copy All Stack Traces for this Thread Dialog to the Clipboard");
        btnCopyAll.setSize(buttonWidth, buttonHeight);
        btnCopyAll.setEnabled(true);
        p5.add(btnCopyAll);
        //p5.add(new JLabel(" "));
        btnCopyAll.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        copyAll();
                     }
                });
		
		btnInterrupt = new JButton();
        
        btnInterrupt.setText("Interrupt");
        btnInterrupt.setToolTipText("Interrupt This Thread");
        btnInterrupt.setSize(buttonWidth, buttonHeight);
        btnInterrupt.setEnabled(true);
        p5.add(btnInterrupt);
        //p5.add(new JLabel(" "));
        btnInterrupt.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        interruptThread();
                     }
                });
        
        btnKill = new JButton();
        
        btnKill.setText("Kill");
        btnKill.setToolTipText("Kill This Thread");
        btnKill.setSize(buttonWidth, buttonHeight);
        btnKill.setEnabled(true);
        p5.add(btnKill);
        //p5.add(new JLabel(" "));
        btnKill.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        killThread();
                     }
                });
		
		// Add the panels to the frame
		setLayout(new GridLayout(1,1,1,1));
		mainPanel.setLayout(new  GridBagLayout());
 		GridBagConstraints c5 = new GridBagConstraints();
		c5.insets= new Insets(1,1,1,1);
		c5.fill = GridBagConstraints.BOTH;
		
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(1,2,1,1));
		top.add(p1);
		top.add(cpuGraph);
		
		JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, top, p2);
	    sp.setOneTouchExpandable(true);
		
		// About info panel
		c5.gridx=0;
		c5.gridy=0;
		c5.gridwidth=1;
		c5.gridheight=6; //;2;
		c5.weightx=1;
		c5.weighty=.90;
		//add(p1,c5);
		mainPanel.add(sp, c5);

		// System info panel
		/*c5.gridx=0;
		c5.gridy=2;
		c5.gridwidth=1;
		c5.gridheight=4;
		c5.weightx=1;
		c5.weighty=.9;
		add(p2,c5);*/

		// System info panel
		c5.gridx=0;
		c5.gridy=6;
		c5.gridwidth=1;
		c5.gridheight=2;
		c5.weightx=1;
		c5.weighty=.1;
		mainPanel.add(p3,c5);		
		

		// Buttons
		c5.gridx=0;
		c5.gridy=8;
		c5.weightx=.1;
		c5.weighty=.0;
		c5.gridheight=1;
		mainPanel.add(p5,c5);

		//Dispose of the Dialog when we are through
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Window close handler
		addWindowListener( new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				
				 {
					 closeDialog();
                 }
				
			}
		});
		

        update();
		
		ActionListener act = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        
		            update();
		        
		    }
		};
		
		
		
				
		timer = new Timer(options.getUpdateInterval(), act);
		timer.start();
		add(mainPanel);
		setVisible(true);
		
	}
	public void update() {
		
		if (!pcm.isConnected()) {
		    Logger.getLogger().debug("Connection is Closed, Closing " + this.getTitle());
			closeDialog();
			return;
		}
		ProbeRequest pr = new ProbeRequest();
		pr.setThreadId(threadId);
        pr.setRequestType(ProbeRequest.REQ_THREAD_INFO);
               
        ProbeResponse res = null;
        
        try {
			res = pcm.sendCommand(pr);
		} catch (ProbeCommunicationsException e) {
			timer.stop();
			Logger.getLogger().logException(e,this);
			//JOptionPane.showMessageDialog(this,e.getMessage(), 
			//		"Connection Lost",JOptionPane.ERROR_MESSAGE);
			closeDialog();
			return;
		}
		
		if (res == null) return;
		
		tid = res.getThreadInfoData();
		
		if (tid == null) {
			threadState.setText("TERMINATED");
			if (++threadDiedCount > CLOSE_DEAD_COUNT) closeDialog();
			stackTrace.setText("Thread Has Ended\nClosing Dialog in " + countDown--);
			return;
		}
		
		long totCPU = tid.getTotalCPU();
		long totUserCPU = tid.getTotalUserTime();
		
		if (tid.isCanInterrupt()) {
			btnInterrupt.setEnabled(true);
		} else {
			btnInterrupt.setEnabled(false);
		}
				
		endTime = System.nanoTime();
        long currentTime = endTime - startTime;
        long intervalTime = currentTime - lastWCTime;
        
        long cpuDelta = totCPU - lastCPUTime;
        double cpuPercent = ((double)cpuDelta / (double)intervalTime) * 100 ; 
        
        currentCPUUsage.setValue((int)cpuPercent);
        if (cpuPercent < 34) {
    		currentCPUUsage.setForeground(Color.green);
    	}else if (cpuPercent < 67) {
    		currentCPUUsage.setForeground(Color.yellow);
    	}else{
    		currentCPUUsage.setForeground(Color.red);
    	}
		
		totalCPU.setText(Utilities.format(totCPU / NANOS_PER_MILLI));
		totalUserCPU.setText(Utilities.format(totUserCPU / NANOS_PER_MILLI));
		blockCount.setText(Utilities.format(tid.getBlockCount()));
		blockTime.setText(Utilities.format(tid.getBlockTime()));
		lockName.setText(tid.getLockName());
		
		bytesAllocated.setText(Utilities.format(tid.getAllocatedBytes()));
		
		lockOwnerId.setText(tid.getLockOwningThread());
		
		lockOwnerName.setText(tid.getLockOwner());
		
		waitCount.setText(Utilities.format(tid.getWaitCount()));
		waitTime.setText(Utilities.format(tid.getWaitTime()));
		
		inNative.setText( tid.getInNative());
		isSuspended.setText( tid.getSuspended());
		
		priority.setText("" + tid.getPriority());
		daemon.setText(tid.isDaemon() ? "Yes" : "No");
		classLoader.setText(tid.getContextClassLoader());
		classLoader.setToolTipText(tid.getContextClassLoader());
		
		stackTrace.setText(tid.getStackTrace());
		stackTrace.setCaretPosition(0);
		
		if (tid.getMonitorLockFrame().length() > 0) {
			monitorLocks.setText(tid.getMonitorLockFrame());
			monitorLocks.setCaretPosition(0);
			p3.setVisible(true);
		} else {
			p3.setVisible(false);
		}
		
		if (cpuPercent < 101) {
		    cpuGraph.addObservation(threadName, cpuPercent, System.currentTimeMillis());
		    //cpuGraph.setTitle("CPU Usage(" + Utilities.format(cpuPercent, 2) + "%)");
		}
        
		threadState.setText(tid.getState());
		
		//Highlight a blocked (deadlocked) thread
		if (tid.getState().equalsIgnoreCase("BLOCKED")) {
			threadState.setForeground(Color.RED);
			blockCount.setForeground(Color.RED);
			blockTime.setForeground(Color.RED);
			lockName.setForeground(Color.RED);
			lockOwnerId.setForeground(Color.RED);
			lockOwnerName.setForeground(Color.RED);
		}else {
			threadState.setForeground(Color.BLACK);
			blockCount.setForeground(Color.BLACK);
			blockTime.setForeground(Color.BLACK);
			lockName.setForeground(Color.BLACK);
			lockOwnerId.setForeground(Color.BLACK);
			lockOwnerName.setForeground(Color.BLACK);
		}
		
		lastCPUTime = totCPU;
		lastWCTime = currentTime; 
		
		if (stackTraces.size() > maxSize) {
		    stackTraces.remove(0);
		}
		stackTraces.add("*Seq(" + traceNo++ + ") - " + getStackTrace() + "\n");
		
		
	}

	/**
	 * This simply closes the dialog
	 */
	private void closeDialog() {
		if (timer != null) timer.stop();
		if (docked) {
		    undock();
		}
		this.dispose();
	}
	
	private void interruptThread() {
		
		if ( JOptionPane.showConfirmDialog(this, 
 				"This is a Potentially Fatal Operation on The Target JVM,\n" +
				"Only Use As A Last Resort. Are You Sure You Want to Do This?",
 				"Are You Sure",
 				JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
			return;
		}
		
		ProbeRequest pr = new ProbeRequest();
		pr.setThreadId(threadId);
        pr.setRequestType(ProbeRequest.REQ_THREAD_INTERRUPT);
               
               
        try {
			pcm.sendCommand(pr);
		} catch (ProbeCommunicationsException e) {
			
			JOptionPane.showMessageDialog(this,e.getMessage(), 
					"Interrupt Failed",JOptionPane.ERROR_MESSAGE);
			
			return;
		}
		
		
		JOptionPane.showMessageDialog(this,
				"Thread " + threadId + " was Interrupted", 
				"Interrupt Succeeded",JOptionPane.ERROR_MESSAGE);
		
	}
   private void killThread() {
	        
        if ( JOptionPane.showConfirmDialog(this, 
                "This is a Potentially Fatal Operation on The Target JVM, \n" +
                "A ThreadDeath Exception will be thrown in this thread.\n" +
                "Only Use As A Last Resort. Are You Sure You Want to Do This?",
                "Are You Sure",
                JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
            return;
        }
        
        ProbeRequest pr = new ProbeRequest();
        pr.setThreadId(threadId);
        pr.setRequestType(ProbeRequest.REQ_KILL_THREAD);
               
               
        try {
            pcm.sendCommand(pr);
        } catch (ProbeCommunicationsException e) {
            
            JOptionPane.showMessageDialog(this,e.getMessage(), 
                    "Kill Failed",JOptionPane.ERROR_MESSAGE);
            
            return;
        }
        
        
        JOptionPane.showMessageDialog(this,
                "Thread " + threadId + " was Killed", 
                "Kill Succeeded",JOptionPane.ERROR_MESSAGE);
        
    }
   private void copyAll() {
       
       StringBuilder sb = new StringBuilder();
       
       for (String trace : stackTraces) {
           sb.append(trace);
       }
       Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
       
       StringSelection data = new StringSelection(sb.toString());
       
       clipboard.setContents(data, data);
       
   }
	private void copyStackTrace() {
	    
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    
	    StringSelection data = new StringSelection(getStackTrace());
	    
	    clipboard.setContents(data, data);
	    
	}
	private String getStackTrace() {
	    
	    StringBuilder sb = new StringBuilder();
        sb.append("Thread ")
        .append(threadName)
        .append(" Id=")
        .append(threadId)
        .append(", on Probe ")
        .append(hostIP)
        .append(" at ")
        .append(Utilities.getDateTime())
        .append("\n");
        
        
        if (tid != null) {
            String[] excludes = {"stackTrace", "canInterrupt"};
            sb.append(Utilities.toStringFormatter(tid, excludes, "Thread Data", false));
            sb.append("\n");
            
        }
        sb.append("Stack Trace\n");
        sb.append(stackTrace.getText());
        
        return sb.toString();
	}
	
    private void dock() {
        
       
        client.dock(mainPanel, "Thread[" + threadName + "]");
        btnDock.setText("Un Dock");
        btnDock.setToolTipText("Un Dock it from the Main View");
        docked = true;
        this.dispose();
        
    }
    private void undock() {
        
        client.undock(mainPanel);
        add(mainPanel);
        btnDock.setText("Dock");
        btnDock.setToolTipText("Dock it in the Main View as a Tab");
        docked = false;
        setVisible(true);
       
    }
	
}
class SelectedThreadCPUGraph extends MiniDynamicGraph {


    private static final long serialVersionUID = 1L;
    private Color[] myColor = { Color.red, Color.blue, Color.yellow,
            Color.red, Color.magenta, Color.cyan,
            Color.pink, Color.black, Color.orange,
            Color.darkGray, Color.gray, Color.lightGray};
   
    /**
     * @param numberOfSamples
     * @param seriesLegendName
     * @param chartName
     */
    public SelectedThreadCPUGraph(int numberOfSamples, String[] seriesLegendName, String chartName, UIOptions options) {
        super(numberOfSamples, seriesLegendName, chartName);
        
        seriesColor = myColor;
        autoRange = false;
        Font baseFont = new JLabel().getFont();
        
        textFont = baseFont.deriveFont(Font.PLAIN, 14);
        titleFont= baseFont.deriveFont(Font.BOLD, 18);
        
        init();
        numberaxis.setRange(0, 100);
        updateStyle(options);
        jfreechart.removeLegend();
        options.addChangeListener(this);
       
    }
    
}