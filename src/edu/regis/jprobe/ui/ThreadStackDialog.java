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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.RecordingManager;
import edu.regis.jprobe.model.ResponseThreadData;
import edu.regis.jprobe.model.Utilities;

/**
 * 
 * 
 * @author jdivincenzo
 *
 * 
 */
public class ThreadStackDialog extends JFrame { 
    
    private static final long serialVersionUID = 1L;
	private int frameWidth = 800;
	private int frameHeight = 600;
	private int buttonWidth=30;
	private int buttonHeight=30;

	// Swing component
	private JTextArea stackTrace;
	private JButton b2;
	private JButton btnDock;
	private Timer timer;
	public static final long NANOS_PER_MILLI = 1000000;
	private long threadId;
	private RecordingManager rm;
	private JProbeClientFrame client;
	private Logger logger;
	private int lastSample = 0;
    private SelectedThreadCPUGraph cpuGraph; 
	private String[] legend;
    private JPanel mainPanel;
    private String threadName;
    private boolean docked = false;
    private long lastCPU = 0;
    private long lastWCTime = 0;
 
	/**
	 * This is the default ctor to initialize all of the swing components
	 *
	 */
	   public ThreadStackDialog(String threadName,
	           UIOptions options,
               long threadId,
               JProbeClientFrame client) {
	       
        this.threadId = threadId;
        this.client = client;
        this.threadName = threadName;
        rm = client.getRecordingManager();
        logger = Logger.getLogger();
		
        ImageIcon icon = IconManager.getIconManager().getThreadIcon();  
        if (icon != null) this.setIconImage(icon.getImage());
        
		Point p = new Point(options.getParentX(), options.getParentY());
		int x = p.x + ((options.getParentWidth() - frameWidth) / 2);
		int y = p.y + ((options.getParentHeight() - frameHeight) / 2);
		setLocation (x,  y);
		
		
		setSize(frameWidth,frameHeight);
		this.setResizable(true);
		
		setTitle("Thread Stack for [" + threadName + "] - [" +
				threadId +"]");		
					
		Dimension hugeTextSize = new Dimension(350,80);
		mainPanel = new JPanel();
		legend = new String[1];
        legend[0] = threadName;
		cpuGraph = new SelectedThreadCPUGraph(options.getSampleRate(), legend,
                "CPU History", options);
		
		//About Info Panel
		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());
		TitledBorder tb0 = new TitledBorder( new EtchedBorder(), "Thread Stack");
		p1.setBorder(tb0);
		tb0.setTitleColor(Color.blue);
		GridBagConstraints c = new GridBagConstraints();
		c.insets= new Insets(1,1,1,1);
		c.fill = GridBagConstraints.BOTH;
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=5;
		c.weightx = 1.0;
		c.weighty = 1.0;
		//System Proerties text box
		stackTrace = new JTextArea();
		Font f = stackTrace.getFont();
		stackTrace.setFont(f.deriveFont(Font.BOLD));
		JScrollPane scroll = new JScrollPane(stackTrace);
		scroll.setPreferredSize(hugeTextSize);
		p1.add(scroll,c);
		
		// Panel for our action buttons
		JPanel p5 = new JPanel();
		p5.setLayout(new GridLayout(1,1,5,5));
		p5.setBorder(new EtchedBorder( EtchedBorder.LOWERED));

		
		// OK button and event handler
		JButton b1 = new JButton();
		p5.add(new JLabel(" "));
		b1.setText("Close");
		b1.setSize(buttonWidth, buttonHeight);
		
		p5.add(b1);
		p5.add(new JLabel(" "));
		b1.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
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
        
        p5.add(new JLabel(" "));
		b2 = new JButton();
		b2.setText("Pause");
		b2.setSize(buttonWidth, buttonHeight);
		
		p5.add(b2);
		p5.add(new JLabel(" "));
		b2.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
					    pauseResume();
					 }
				});

		
		
		JButton b3 = new JButton();
        b3.setText("Next >>");
        b3.setToolTipText("Next Sample Stack Trace for this Thread");
        b3.setSize(buttonWidth, buttonHeight);
        
        p5.add(b3);
        p5.add(new JLabel(" "));
        //p5.add(new JLabel(" "));
        b3.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        if (!isPaused()) {
                            pauseResume();
                        }
                        
                        formatSample(lastSample + 1);
                     }
                });
        JButton b4 = new JButton();
        b4.setText("<< Previous");
        b4.setToolTipText("Previous Sample Stack Trace for this Thread");
        b4.setSize(buttonWidth, buttonHeight);
        
        p5.add(b4);
        p5.add(new JLabel(" "));
        b4.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        if (!isPaused()) {
                            pauseResume();
                        }
                        
                        formatSample(lastSample - 1);
                     }
                });
		
		
		// Add the panels to the frame
                
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, cpuGraph, p1);
        sp.setOneTouchExpandable(true);
        sp.setDividerLocation(frameHeight / 2);
                
        
		mainPanel.setLayout(new  GridBagLayout());
 		GridBagConstraints c5 = new GridBagConstraints();
		c5.insets= new Insets(1,1,1,1);
		c5.fill = GridBagConstraints.BOTH;
		
		// About info panel
		c5.gridx=0;
		c5.gridy=0;
		c5.gridwidth=1;
		c5.gridheight=5;
		c5.weightx=1;
		c5.weighty=1;
		mainPanel.add(sp,c5);


		// Buttons
		c5.gridx=0;
		c5.gridy=5;
		c5.weightx=.1;
		c5.weighty=.0;
		c5.gridwidth=1;
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
		
		add(mainPanel);
        
        update();
 
		
		ActionListener act = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		            update();
		    }
       
		};
		
		
		
				
		timer = new Timer(client.playbackInterval, act);
		timer.start();
		setVisible(true);
		
	}

	   public void update() {
	        
	        if (rm == null || !rm.isOpen()) {
	            logger.debug("Recording is Closed, Closing " + this.getTitle());
	            closeDialog();
	            return;
	        }
	        int sampleNumber = client.getLastTick();
	        formatSample(sampleNumber);
	 }
	 private void formatSample(int sample) {
	     
	     if (sample > rm.getNumberOfRecords()) {
	         stackTrace.setText("At End of Samples");
	         return;
	     }
	     if (sample < 0) {
	         stackTrace.setText("At Beginning of Samples");
	         return;
	     }
	     
	     
	     ProbeResponse res = null;
	        
            try {
                res = rm.readResponse(sample);
            } catch (IOException e) {
                logger.logException(e, this);
                return;
            } catch (ClassNotFoundException e) {
                logger.logException(e, this);
                return;
            }
	        if (res == null) {
	            stackTrace.setText("End of File Reading Sample");
	            return;
	        }
	        
	        int numThreads = res.getNumberOfThreadInfo();
	        
	        ResponseThreadData tid = null;
	        
	        for (int i = 0; i < numThreads; i++) {
	            ResponseThreadData t = res.getThreadInfo(i);
	            if (t.getThreadId() == threadId) {
	                tid = t;
	            }
	        }
	        
	        if (tid == null) {
	            stackTrace.setText("Thread has ended");
	            return;
	        }
	        
	        String heading = "Thread[" + tid.getThreadName() + "] Id[" + tid.getThreadId() + 
	                "] at [" + Utilities.formatTimeStamp(res.getCurrentTime(), "HH:mm:ss.SSS") + 
	                "] Sample[" + Utilities.format(sample) + "]\n";
	        stackTrace.setText(heading + Utilities.formatStackTrace(tid.getCurrentStackFrame()));
	        stackTrace.setCaretPosition(0);
	        lastSample = sample;
	        
	        long intervalTime = tid.getLastTime() - lastWCTime;
	        
	        long cpuDelta = tid.getThreadLastCPU() - lastCPU;
	        double cpuPercent = ((double)cpuDelta / (double)intervalTime) * 100 ; 
	        
	        if (cpuPercent < 101) {
	            cpuGraph.addObservation(threadName, cpuPercent, System.currentTimeMillis());
	            cpuGraph.setTitle("CPU Usage(" + Utilities.format(cpuPercent, 2) + "%)");
	        }
	        
	        lastCPU = tid.getThreadLastCPU();
	        lastWCTime = tid.getLastTime();
	    }
	/**
	 * This simply closes the dialog
	 */
	private void closeDialog() {
	    
	    if (docked) {
            undock();
        }
	    
		if (timer != null) { 
		    timer.stop();
		}
		this.dispose();
	}
	private void pauseResume() {
	    if (b2.getText().equalsIgnoreCase("Resume")) {
            timer.start();
            b2.setText("Pause");
        }else{
            timer.stop();
            b2.setText("Resume");
        }
	}
	
	private boolean isPaused() {
	    
	    return b2.getText().equalsIgnoreCase("Resume");
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
