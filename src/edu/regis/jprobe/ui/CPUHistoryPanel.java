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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.Utilities;



/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CPUHistoryPanel extends PerformancePanel implements IPerformancePanel {

	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    //private DynamicTimeGraph cpuGraph;
    private CPUGraph cpuGraph;
	private boolean firstTime = true;
	private boolean panelBuilt = false;
	private long lastCPU = 0;
	private long lastUser = 0;
	private long lastKernel = 0;
	private long lastWallclock = 0;
	private UIOptions options;
	
	private String namesBasic[] = {"Current CPU %", "Average CPU%"}; 
	private String namesAdvanced[] = {"Current CPU %", "Average CPU%",
									  "Current User %", "Current Kernel %"};
	private String names[];
	
	public CPUHistoryPanel( UIOptions options) {
		
		this.options = options;		
		setBorder(new TitledBorder( new EtchedBorder(), "CPU History"));
		setLayout(new GridLayout(1,1,1,1));
		
		
	}
	public void buildPanel(ProbeResponse res, int numberOfSamples) {
		
		
		
		
		if (res.isIoCountersAvailable()) {
			names = namesAdvanced;
		} else {
			names = namesBasic;
		}
			    
		
	    try {
			//cpuGraph = new DynamicTimeGraph(numberOfSamples, names,
			//			"Percentage", "CPU Usage");
			cpuGraph = new CPUGraph(numberOfSamples, names,
                    "CPU Usage", options);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,"Unable to Build CPU Usage Graph" +
	                ", Error is " +
                    e.getMessage(),"Graph Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
    
		add(cpuGraph);
		repaint(10);
		panelBuilt = true;
		
	}
	public void update(ProbeResponse res) {
		
		
		double graphcpu = res.getCurrentCPUPercent();
		double maxCPU = res.getNumberOfCPUs() * 100;
		double cpuRelativeValue =  //1 / (double) res.getNumberOfCPUs();
	 		(options.isCpuIsRelative() ?   (1 / (double) res.getNumberOfCPUs()) : 1);
		
		if (!res.isIoCountersAvailable()) {
			if (graphcpu > maxCPU) graphcpu = maxCPU;
		
			if (firstTime) {
				graphcpu = 0;
				firstTime = false;
			}
		
			if (graphcpu < 0) graphcpu = 0;
			cpuGraph.addObservation(names[0], graphcpu , res.getCurrentTime());
			cpuGraph.addObservation(names[1], res.getAverageCPUPercent(), res.getCurrentTime());
			cpuGraph.setTitle("CPU Usage(" + Utilities.format(graphcpu, 2) + "%)"); 
		} else {
			
			
			long thisKernel = res.getTotalKernelTime();
			long thisUser = res.getTotalUserTime();
			long thisCPU = thisKernel + thisUser;
			long thisWallclock = res.getCurrentTime();
			
			double cpu = ((double)(thisCPU - lastCPU) / (double) (thisWallclock - lastWallclock)) * 100;
			double user = ((double) (thisUser - lastUser ) / (double) (thisWallclock - lastWallclock)) * 100;
			double kernel = ((double)(thisKernel - lastKernel) / (double) (thisWallclock - lastWallclock)) * 100;
			
			if (lastCPU != 0) {
				cpuGraph.addObservation(names[0], cpu * cpuRelativeValue, res.getCurrentTime()); 
				cpuGraph.addObservation(names[1], res.getAverageCPUPercent(), res.getCurrentTime());
			}
			
			if (lastUser != 0) {
				cpuGraph.addObservation(names[2],user * cpuRelativeValue, res.getCurrentTime());
				
			}
			if (lastKernel != 0) {
				cpuGraph.addObservation(names[3], kernel * cpuRelativeValue, res.getCurrentTime());
				
			}
			cpuGraph.setTitle("CPU Usage(" + Utilities.format(cpu * cpuRelativeValue, 2) + "%)"); 
			lastCPU = thisCPU;
			lastKernel = thisKernel;
			lastUser = thisUser;
			lastWallclock = thisWallclock;
			
		}
		
	}
	public void resetPanel() {
		
		cpuGraph.reset();
		cpuGraph.updateStyle(options);
		lastCPU = 0;
		lastUser = 0;
		lastKernel = 0;
		lastWallclock = 0;
	}
	
	/**
	 * @return the panelBuilt
	 */
	public boolean isPanelBuilt() {
		return panelBuilt;
	}
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		Logger.getLogger().debug("Calling Print for " + this.getClass().getName());
		cpuGraph.print(graphics, pageFormat, pageIndex);
		return PAGE_EXISTS;
	}
	public void setSampleSize(int size) {
		cpuGraph.setSampleSize(size);
		
	}
}
class CPUGraph extends MiniDynamicGraph {


    private static final long serialVersionUID = 1L;
    private Color[] myColor = { Color.green, Color.blue, Color.yellow,
            Color.red, Color.magenta, Color.cyan,
            Color.pink, Color.black, Color.orange,
            Color.darkGray, Color.gray, Color.lightGray};
    /**
     * @param numberOfSamples
     * @param seriesLegendName
     * @param chartName
     */
    public CPUGraph(int numberOfSamples, String[] seriesLegendName, String chartName, UIOptions options) {
        super(numberOfSamples, seriesLegendName, chartName);
        
        seriesColor = myColor;
        autoRange = true;
        Font baseFont = new JLabel().getFont();
        
        textFont = baseFont.deriveFont(Font.PLAIN, 14);
        titleFont= baseFont.deriveFont(Font.BOLD, 18);
        
        init();
       
        updateStyle(options);
        
        options.addChangeListener(this);
       
    }
    
}