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

import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.Utilities;

/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IOCountHistoryPanel extends PerformancePanel implements IPerformancePanel {
	
    private static final long serialVersionUID = 1L;
    private boolean panelBuilt = false;
	private IOCountGraph ioGraph;
	public static final long ONE_MB = 1024 * 1024;
	private String names[] = {"Read", "Write", "Other"}; //, "Total"};
	private long lastReadCount = 0;
	private long lastWriteCount = 0;
	private long lastOtherCount = 0;
	private UIOptions options;
	
	public IOCountHistoryPanel(UIOptions options) {
		
		setBorder(new TitledBorder( new EtchedBorder(), "IO Count History"));
		setLayout(new GridLayout(1,1,1,1));
		this.options = options;
	}
	/**
	 * 
	 * @param res
	 * @param numberOfSamples
	 */
	public void buildPanel(ProbeResponse res, int numberOfSamples) {
		
		
		
			    
		
	    try {
			ioGraph = new IOCountGraph(numberOfSamples, names,
						"IO Operations Per Second", options);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,"Unable to Build IO Count Graph" +
	                ", Error is " +
                    e.getMessage(),"Graph Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
    
		add(ioGraph);
		repaint(10);
		panelBuilt = true;
		
	}
	public void update(ProbeResponse res) {
		
		long thisReadCount = res.getIoReadCount();
		long thisWriteCount = res.getIoWriteCount();
		long thisOtherCount = res.getIoOtherCount();
		long totalCount = (thisReadCount - lastReadCount) +
		        (thisWriteCount - lastWriteCount) + 
		        (thisOtherCount - lastOtherCount);
		
		if (lastReadCount != 0) {
			ioGraph.addObservation(names[0], thisReadCount - lastReadCount, res.getCurrentTime());
			
		}
		
		if (lastWriteCount != 0) {
			ioGraph.addObservation(names[1], thisWriteCount - lastWriteCount, res.getCurrentTime());
			
		}
		if (lastOtherCount != 0) {
			ioGraph.addObservation(names[2], thisOtherCount - lastOtherCount, res.getCurrentTime());
			
		}
		
		/*if (lastTotalCount != 0) {
			ioGraph.addObservation(names[3], thisTotalCount - lastTotalCount);
			
		}*/
	    	
		lastReadCount = thisReadCount;
		lastWriteCount = thisWriteCount;
		lastOtherCount = thisOtherCount;
		ioGraph.setTitle("I/O Bytes Operations Per Second(" + Utilities.format(totalCount) + ")"); 
	}
	public void resetPanel() {
		ioGraph.reset();
		lastReadCount = 0;
		lastWriteCount = 0;
		lastOtherCount = 0;
		//lastTotalCount = 0;
		//removeAll();
		//panelBuilt = false;
	}
	public boolean isPanelBuild() {
		return panelBuilt;
	}
	public void setSampleSize(int size) {
		ioGraph.setSampleSize(size);
		
	}
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
		throws PrinterException {

		ioGraph.print(graphics, pageFormat, pageIndex);
		return PAGE_EXISTS;
	}
}
class IOCountGraph extends MiniDynamicGraph {


    private static final long serialVersionUID = 1L;
    private Color[] myColor = { Color.red, Color.blue, Color.green,
            Color.yellow, Color.magenta, Color.cyan,
            Color.pink, Color.orange,
            Color.darkGray, Color.gray, Color.lightGray};
    /**
     * @param numberOfSamples
     * @param seriesLegendName
     * @param chartName
     */
    public IOCountGraph(int numberOfSamples, String[] seriesLegendName, String chartName, UIOptions options) {
        super(numberOfSamples, seriesLegendName, chartName);
        
        seriesColor = myColor;
        
        Font baseFont = new JLabel().getFont();
        
        textFont = baseFont.deriveFont(Font.PLAIN, 14);
        titleFont= baseFont.deriveFont(Font.BOLD, 18);
        init();
        autoRange = true;
        updateStyle(options);
        options.addChangeListener(this);
       
    }
    
}