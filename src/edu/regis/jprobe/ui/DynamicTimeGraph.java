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

public class DynamicTimeGraph extends JPanel
{
   
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private TimeSeries series[];
    private ChartPanel chartpanel;
    private String seriesName[];
    private int numberOfSeries = 0;
    private Color seriesColor[] = { Color.red, Color.blue, Color.green,
    								Color.yellow, Color.magenta, Color.cyan,
									Color.pink, Color.black, Color.orange,
									Color.darkGray,	Color.gray, Color.lightGray};
    
    private long lastTime = 0;   
    /**
     * Constructor
     * @param i number of samples
     * @param seriesLegendName series name
     * @param axisName axis name
     * @param chartName chart name
     * @throws Exception
     */
    public DynamicTimeGraph(int i, String seriesLegendName[], 
            String axisName, String chartName) throws Exception
    {
        super(new BorderLayout());
        this.seriesName = seriesLegendName;
        numberOfSeries = seriesName.length;
        
        if (numberOfSeries > seriesColor.length) throw new Exception("Too Many Series");
        series = new TimeSeries[numberOfSeries];
        TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
        XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer(true, false);
        
        for (int j = 0; j < seriesName.length; j++) { 
        	series[j] = new TimeSeries(seriesName[j], org.jfree.data.time.Second.class);
        	series[j].setMaximumItemCount(i);
        	//series[j].setHistoryCount(i);
        	xylineandshaperenderer.setSeriesPaint(j, seriesColor[j]);
        	timeseriescollection.addSeries(series[j]);
        }
        
        
      
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
        //lastTime = System.currentTimeMillis();
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
    	for (int i = 0; i < numberOfSeries; i++) {
    		if (name.equals(seriesName[i])) {
    			series[i].addOrUpdate(period, value); //new Second(), value);
    			
    		}
    	}
    	
    }

    public void setSampleSize(int val) {
    	
    	for (int j = 0; j < seriesName.length; j++) { 
        	series[j].setMaximumItemCount(val);
        }
    }
    /**
     * This method is used to clear the graph.
     *
     */
    public void reset() {
     
    	for (int i = 0; i < numberOfSeries; i++) {
    		series[i].clear();
    	}
    	lastTime = 0;
    }
    
    public void print(Graphics g, PageFormat pf, int pageno) {
 	   
 	   chartpanel.print(g, pf, pageno);
    }


}
