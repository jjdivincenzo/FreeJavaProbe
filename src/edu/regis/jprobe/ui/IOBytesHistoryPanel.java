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
public class IOBytesHistoryPanel extends PerformancePanel implements IPerformancePanel {
	
    private static final long serialVersionUID = 1L;
    private boolean panelBuilt = false;
	private IOBytesGraph ioGraph;
	public static final long ONE_MB = 1024 * 1024;
	private String names[] = {"Read", "Write", "Other"}; //, "Total"};
	private long lastReadBytes = 0;
	private long lastWriteBytes = 0;
	private long lastOtherBytes = 0;
	private UIOptions options;
	
	public IOBytesHistoryPanel(UIOptions options) {
		
		setBorder(new TitledBorder( new EtchedBorder(), "IO Bytes History"));
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
			ioGraph = new IOBytesGraph(numberOfSamples, names,
						"IO Bytes Transfered Per Second", options);
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
		
		
		long thisReadBytes = res.getIoReadBytes();
		long thisWriteBytes = res.getIoWriteBytes();
		long thisOtherBytes = res.getIoOtherBytes();
		long totalBytes = (thisReadBytes - lastReadBytes) + 
		        (thisWriteBytes - lastWriteBytes) + 
		        (thisOtherBytes - lastOtherBytes);
		
		if (lastReadBytes >= 0) {
			ioGraph.addObservation(names[0], thisReadBytes - lastReadBytes, res.getCurrentTime());
			
		}
		
		if (lastWriteBytes >= 0) {
			ioGraph.addObservation(names[1], thisWriteBytes - lastWriteBytes, res.getCurrentTime());
			
		}
		if (lastOtherBytes >= 0) {
			ioGraph.addObservation(names[2], thisOtherBytes - lastOtherBytes, res.getCurrentTime());
			
		}
		
	    	
		lastReadBytes = thisReadBytes;
		lastWriteBytes = thisWriteBytes;
		lastOtherBytes = thisOtherBytes;
		ioGraph.setTitle("I/O Bytes Transfered Per Second(" + Utilities.formatBytes(totalBytes) + ")"); 
		
	}
	public void resetPanel() {
		ioGraph.reset();
		lastReadBytes = 0;
		lastWriteBytes = 0;
		lastOtherBytes = 0;

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
class IOBytesGraph extends MiniDynamicGraph {


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
    public IOBytesGraph(int numberOfSamples, String[] seriesLegendName, String chartName, UIOptions options) {
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