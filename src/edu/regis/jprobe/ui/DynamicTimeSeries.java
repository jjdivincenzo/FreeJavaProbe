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

/**
 * @author jdivince
 *
 * This class is our implementation of the Time Chart of JFreeChart. 
 */

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class DynamicTimeSeries extends JPanel
{
   
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private HashMap <String,TimeSeries> series;
    private XYLineAndShapeRenderer xylineandshaperenderer;
    private TimeSeriesCollection timeseriescollection;
    private ChartPanel chartpanel;
    //private String seriesName[];
    //private int numberOfSeries = 0;
    //private int colorIndex = 0;
    private int historyCount = 0;
    private long lastTime = 0;
    
    /**
     * This is the default ctor.
     * @param i int - number of observations in the series.
     * @param axisName  - String name for the Y-Axis. 
     * @param chartName - String Title for the chart. 
     */
    
    public DynamicTimeSeries(int i, String axisName, String chartName) {
        super(new BorderLayout());
        historyCount = i;
        
        series = new HashMap<String, TimeSeries>();
        timeseriescollection = new TimeSeriesCollection();
        xylineandshaperenderer = new XYLineAndShapeRenderer(true, false);
                      
        DateAxis dateaxis = new DateAxis("Time");
        NumberAxis numberaxis = new NumberAxis(axisName);
        dateaxis.setTickLabelFont(new Font("SansSerif", 0, 12));
        numberaxis.setTickLabelFont(new Font("SansSerif" , 0, 12));
        dateaxis.setLabelFont(new Font("SansSerif", 0, 14));
        numberaxis.setLabelFont(new Font("SansSerif", 0, 14));
        //xylineandshaperenderer.setStroke(new BasicStroke(3F, 0, 2));
        xylineandshaperenderer.setBaseStroke(new BasicStroke(3F, 0, 2));
        XYPlot xyplot = new XYPlot(timeseriescollection, dateaxis, numberaxis, xylineandshaperenderer);
        xyplot.setBackgroundPaint(Color.black);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);
        xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
        dateaxis.setAutoRange(true);
        dateaxis.setLowerMargin(0.0D);
        dateaxis.setUpperMargin(0.0D);
        dateaxis.setTickLabelsVisible(true);
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        JFreeChart jfreechart = new JFreeChart(chartName, new Font("SansSerif", 1, 24), xyplot, true);
        jfreechart.setBackgroundPaint(Color.white);
        chartpanel = new ChartPanel(jfreechart);
        chartpanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createLineBorder(Color.black)));
        add(chartpanel);
        
    }

    public void addTimeSeries(String name) {
    	
    	if (series.get(name) != null) return;
    	
    	TimeSeries ts = new TimeSeries(name, TimePeriod.class); 
    	ts.setMaximumItemCount(historyCount);
    	timeseriescollection.addSeries(ts);
    	series.put(name, ts);
    	    	
    	
    }
    public void removeTimeSeries(String name) {
    	
    	TimeSeries ts = series.get(name);
    	
    	if (ts != null) timeseriescollection.removeSeries(ts);
    }
    /**
     * This method is used to add an observation to the graph.
     * @param name String - series name value to add.
     * @param value double - series  value to add.
     */
    public void addObservation(String name, double value, long thisInterval)
    {
        if (lastTime == 0) {
            lastTime = thisInterval - 1000;
        }
        //long currentTime = System.currentTimeMillis();
        TimePeriod period = new TimePeriod(lastTime, thisInterval); //currentTime);
        lastTime = thisInterval;
    	TimeSeries ts = series.get(name);
    	
    	if (ts != null) ts.addOrUpdate(period, value);
    	
    }
    public void setSampleSize(int val) {
    	
    	Set<String> keys = series.keySet();
    	Iterator<String> iter = keys.iterator();
    	
    	while (iter.hasNext()) {
	    	
	    	String sampleName = iter.next();
	    	TimeSeries ts = series.get(sampleName);
	    	ts.setMaximumItemCount(val);
	    	
		}
    	
    	
    }
    /**
     * This method is used to clear the graph.
     *
     */
    public void reset() {
     
    	timeseriescollection.removeAllSeries();
    	series.clear();
    	lastTime = 0;
    }
    
    public void print(Graphics g, PageFormat pf, int pageno) {
 	   
 	   chartpanel.print(g, pf, pageno);
    }


}
