///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2006  James Di Vincenzo
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

import edu.regis.jprobe.model.Utilities;

/**
 * @author jdivinc
 *
 */
public class MiniCPUGraph extends MiniDynamicGraph {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static String[] valueNames = {"Total", "Kernel", "User"};
    private static Color[] colors = { Color.GREEN, Color.RED, Color.YELLOW};
    private static String title = "CPU Usage";
    /**
     * @param numberOfSamples
     * @param seriesLegendName
     * @param chartName
     */
    public MiniCPUGraph(int numberOfSamples) {
        super(numberOfSamples, valueNames, title);
        this.seriesColor = colors;
        
        init();
        numberaxis.setRange(0, 100);
    }
    
    public void addObservation(double kernelPercent, double userPercent) {
        double total = kernelPercent + userPercent;
        long interval = System.currentTimeMillis();
        addObservation(valueNames[0], total, interval);
        addObservation(valueNames[1], kernelPercent, interval);
        addObservation(valueNames[2], userPercent, interval);
        setTitle(title + " (" + Utilities.format(total, 2) + "%)");
    }

}
