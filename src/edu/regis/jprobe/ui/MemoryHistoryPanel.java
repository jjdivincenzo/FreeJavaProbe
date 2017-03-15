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
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;

import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.Utilities;


/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MemoryHistoryPanel extends PerformancePanel implements IPerformancePanel {

    private static final long serialVersionUID = 1L;
    private MemoryGraph memoryGraph;
    private boolean showNonHeap = true;
    private boolean panelBuilt  = false;
    private int numberOfSamples = 100;
    private UIOptions options;
    
    private String[] titles;// = {"Heap Usage %", "Non Heap Usage %"};
   
	
	public MemoryHistoryPanel(int numberOfSamples, UIOptions options) {
		
		this.numberOfSamples = numberOfSamples;
		this.options = options;
		/*
		memoryGraph = new MemoryGraph(numberOfSamples, titles, "Heap and Non Heap Usage %", options);
		setBorder(new TitledBorder( new EtchedBorder(), "Memory History"));
		setLayout(new GridLayout(1,1,1,1));
		GridBagConstraints c5 = new GridBagConstraints();
		c5.insets= new Insets(1,1,1,1);
		c5.fill = GridBagConstraints.BOTH;
		c5.gridx=0;
		c5.gridy=0;
		c5.gridwidth=1;
		c5.gridheight=1;
		c5.weightx=1.0;
		c5.weighty=1.0;
		add(memoryGraph,c5);*/
		
	}
	public void update(ProbeResponse res) {
	    
	    String title = "Heap Usage %";
	    if (!panelBuilt) {
	        
	        if (showNonHeap) {
	            titles = new String[2];
	            titles[0] = "Heap Usage %";
	            titles[1] = "Non Heap Usage %";
	            title = "Heap and Non Heap Usage %";
	        } else {
	            titles = new String[1];
	            titles[0] ="Heap Usage %";
	        }
	        
	        memoryGraph = new MemoryGraph(numberOfSamples, titles, title, options);
	        setBorder(new TitledBorder( new EtchedBorder(), "Memory History"));
	        setLayout(new GridLayout(1,1,1,1));
	        GridBagConstraints c5 = new GridBagConstraints();
	        c5.insets= new Insets(1,1,1,1);
	        c5.fill = GridBagConstraints.BOTH;
	        c5.gridx=0;
	        c5.gridy=0;
	        c5.gridwidth=1;
	        c5.gridheight=1;
	        c5.weightx=1.0;
	        c5.weighty=1.0;
	        add(memoryGraph,c5);
	        panelBuilt = true;
	    }
		
	    double heapUsagePct = ((double)res.getCurrentHeapSize() / 
                (double)res.getMaxHeapSize()) * 100;
	    
	    memoryGraph.addObservation(titles[0], heapUsagePct, res.getCurrentTime());
	    
	    if (showNonHeap) {
	        double nonHeapUsagePct = ((double)res.getCurrentNonHeapSize() / 
	                (double)res.getMaxNonHeapSize()) * 100;
	        memoryGraph.addObservation(titles[1], nonHeapUsagePct, res.getCurrentTime());
	    } else {
	        memoryGraph.setTitle("Heap Usage (" + Utilities.format(heapUsagePct, 2) + "%)");
	    }
	    
	    
	        
 	}
	public void resetPanel() {
	    memoryGraph.reset();
	}
	public void setSampleSize(int size) {
		memoryGraph.setSampleSize(size);
	}
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		memoryGraph.print(graphics, pageFormat, pageIndex);
		return PAGE_EXISTS;
	}
    public boolean isShowNonHeap() {
        return showNonHeap;
    }
    public void setShowNonHeap(boolean showNonHeap) {
        this.showNonHeap = showNonHeap;
    }
	
}
class MemoryGraph extends MiniDynamicGraph {


    private static final long serialVersionUID = 1L;
    private Color[] myColor = { Color.red, Color.yellow, Color.blue,
            Color.red, Color.magenta, Color.cyan,
            Color.pink, Color.black, Color.orange,
            Color.darkGray, Color.gray, Color.lightGray};
    /**
     * @param numberOfSamples
     * @param seriesLegendName
     * @param chartName
     */
    public MemoryGraph(int numberOfSamples, String[] seriesLegendName, String chartName, UIOptions options) {
        super(numberOfSamples, seriesLegendName, chartName);
        
        seriesColor = myColor;
        
        Font baseFont = new JLabel().getFont();
        
        textFont = baseFont.deriveFont(Font.PLAIN, 14);
        titleFont= baseFont.deriveFont(Font.BOLD, 18);
        init();
        numberaxis.setRange(0, 100);
        updateStyle(options);
        options.addChangeListener(this);
    }
    
}