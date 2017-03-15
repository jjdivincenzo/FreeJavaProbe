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

import edu.regis.jprobe.model.MemoryPoolData;
import edu.regis.jprobe.model.ProbeResponse;

/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MemoryPoolHistoryPanel extends PerformancePanel implements IPerformancePanel {
	
    private static final long serialVersionUID = 1L;
	private boolean panelBuilt = false;
	private MemoryPoolGraph poolGraph;
	public static final long ONE_MB = 1024 * 1024;
	private UIOptions options;
	
	
	public MemoryPoolHistoryPanel(UIOptions options) {
		
		setBorder(new TitledBorder( new EtchedBorder(), "Memory Pool History"));
		setLayout(new GridLayout(1,1,1,1));
		this.options = options;
	}
	
	public void buildPanel(ProbeResponse res, int numberOfSamples) {
		
		String names[] = new String[res.getPoolDataSize()];
		
		for (int i = 0; i < res.getPoolDataSize(); i++) {
			MemoryPoolData mpd = res.getPoolData(i);
	    	
	    	names[i] = mpd.getName();
	    	
	   	}
	    
		
	    try {
			poolGraph = new MemoryPoolGraph(numberOfSamples, names,
						"Memory Pool Usage (MB)", options);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,"Unable to Build Memory Pool Usage Graph" +
	                ", Error is " +
                    e.getMessage(),"Graph Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
    
		add(poolGraph);
		repaint(10);
		panelBuilt = true;
		
	}
	public void update(ProbeResponse res) {
		
		for (int i = 0; i < res.getPoolDataSize(); i++) {
			MemoryPoolData mpd = res.getPoolData(i);
			poolGraph.addObservation(mpd.getName(),
					mpd.getCurrentUsage() / ONE_MB, res.getCurrentTime());
	    	
			
	   	}
		
	}
	public void resetPanel() {
		poolGraph.reset();
		removeAll();
		panelBuilt = false;
	}
	public boolean isPanelBuild() {
		return panelBuilt;
	}
	public void setSampleSize(int size) {
		poolGraph.setSampleSize(size);
		
	}
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
		throws PrinterException {

		poolGraph.print(graphics, pageFormat, pageIndex);
		return PAGE_EXISTS;
	}
}
class MemoryPoolGraph extends MiniDynamicGraph {


    private static final long serialVersionUID = 1L;
    private Color[] myColor = { Color.pink, Color.yellow, Color.green,
            Color.red, Color.magenta, Color.cyan,
            Color.pink, Color.orange,
            Color.darkGray, Color.gray, Color.lightGray};
    /**
     * @param numberOfSamples
     * @param seriesLegendName
     * @param chartName
     */
    public MemoryPoolGraph(int numberOfSamples, String[] seriesLegendName, String chartName, UIOptions options) {
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