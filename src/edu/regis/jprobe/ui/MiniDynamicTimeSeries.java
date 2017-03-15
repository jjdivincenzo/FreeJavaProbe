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
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.regis.jprobe.model.Logger;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

public abstract class MiniDynamicTimeSeries extends JPanel implements OptionChangeListener
{
   
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private HashMap <String,TimeSeries> series;
    protected JFreeChart jfreechart;
    protected ChartPanel chartpanel;
    protected int numberOfSeries = 0;
    protected int numberOfSamples = 1;
    protected String chartTitle;
    protected Color seriesColor[] = { Color.red, Color.yellow, Color.cyan,
    								Color.yellow, Color.magenta, Color.cyan,
									Color.pink, Color.black, Color.orange,
									Color.darkGray,	Color.gray, Color.lightGray};
    protected Font textFont;
    protected Font titleFont;
    protected Color foreground = Color.GREEN;
    protected Color background = Color.BLACK;
    protected boolean showTickLines = false;
    protected boolean showTickLabels = false;
    protected boolean autoRange = true;
    protected BasicStroke domainGridlineStroke = new BasicStroke(1F, 0, 1);
    protected BasicStroke rangeGridlineStroke  = new BasicStroke(1F, 0, 1);
    protected BasicStroke baseStroke  = new BasicStroke(1F, 0, 1);
    protected RectangleInsets axisOffset = new RectangleInsets(5D, 5D, 5D, 5D);
    
    protected TimeSeriesCollection timeseriescollection;
    protected XYLineAndShapeRenderer xylineandshaperenderer;
    protected XYPlot xyplot;
    protected NumberAxis numberaxis;
    protected DateAxis dateaxis;
    private long lastTime = 0;   
    /**
     * Constructor
     * @param i number of samples
     * @param seriesLegendName series name
     * @param axisName axis name
     * @param chartName chart name
     * @throws Exception
     */
    public MiniDynamicTimeSeries(int numberOfSamples, 
            String chartName)  {
        
        super(new BorderLayout());
        
        series = new HashMap<String, TimeSeries>();
        this.chartTitle = chartName;
        this.numberOfSamples = numberOfSamples;
        Font baseFont = new JLabel().getFont();
        textFont = baseFont.deriveFont(Font.PLAIN, 10);
        titleFont = baseFont.deriveFont(Font.BOLD, 12);
    }
    
    public void init() {
        
        
        timeseriescollection = new TimeSeriesCollection();
        xylineandshaperenderer = new XYLineAndShapeRenderer(true, false);
       
        dateaxis = new DateAxis();
        numberaxis = new NumberAxis();
        numberaxis.setAutoRange(autoRange);
        
        dateaxis.setTickLabelFont(textFont);
        numberaxis.setTickLabelFont(textFont);
        dateaxis.setLabelFont(textFont);
        numberaxis.setLabelFont(textFont);
        xylineandshaperenderer.setBaseStroke(baseStroke);
        xyplot = new XYPlot(timeseriescollection, dateaxis, numberaxis, xylineandshaperenderer);
        xyplot.setBackgroundPaint(background);
        xyplot.setDomainGridlinePaint(foreground);
        xyplot.setRangeGridlinePaint(foreground);
        xyplot.setAxisOffset(axisOffset);
        xyplot.setDomainGridlineStroke(domainGridlineStroke);
        xyplot.setRangeGridlineStroke(rangeGridlineStroke);
        xyplot.setRangeCrosshairLockedOnData(true);
        numberaxis.setAxisLinePaint(foreground);
        dateaxis.setAxisLinePaint(foreground);
        dateaxis.setTickMarksVisible(showTickLines);
        numberaxis.setTickLabelPaint(foreground);
        numberaxis.setTickMarkPaint(foreground);
        
        dateaxis.setAutoRange(true);
        dateaxis.setLowerMargin(0.0D);
        dateaxis.setUpperMargin(0.0D);
        dateaxis.setTickLabelsVisible(showTickLines);
        dateaxis.setAxisLineVisible(showTickLines);
        dateaxis.setAutoTickUnitSelection(true);
        dateaxis.setVerticalTickLabels(showTickLabels);
        dateaxis.setTickLabelPaint(foreground);
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        jfreechart = new JFreeChart(chartTitle, titleFont, xyplot, true);
        jfreechart.setBackgroundPaint(background);
        
        LegendTitle legend = jfreechart.getLegend();
        
        legend.setBackgroundPaint(background);
        legend.setItemPaint(foreground);
        TextTitle tt = jfreechart.getTitle();
        tt.setPaint(foreground);
        tt.setBackgroundPaint(background);
        
        chartpanel = new ChartPanel(jfreechart);
        chartpanel.setBorder(BorderFactory.createLineBorder(background));
        add(chartpanel);
        
    }

    protected void updateStyle(UIOptions opt) {
        
        try {
            xylineandshaperenderer.setBaseStroke(new BasicStroke(
                    opt.getBaseStrokeWidth(), opt.getBaseStrokeCap(), opt.getBaseStrokeJoin()));
            xyplot.setDomainGridlineStroke(new BasicStroke(
                    opt.getDomainStrokeWidth(), opt.getDomainStrokeCap(), opt.getDomainStrokeJoin()));
            xyplot.setRangeGridlineStroke(new BasicStroke(
                    opt.getRangeStrokeWidth(), opt.getRangeStrokeCap(), opt.getRangeStrokeJoin()));
            
            xyplot.setBackgroundPaint(opt.getGraphBackground());
            xyplot.setDomainGridlinePaint(opt.getGraphForeground());
            xyplot.setRangeGridlinePaint(opt.getGraphForeground());
            
    
            numberaxis.setAxisLinePaint(opt.getGraphForeground());
            numberaxis.setTickLabelPaint(opt.getGraphForeground());
            numberaxis.setTickMarkPaint(opt.getGraphForeground());
            
            dateaxis.setAxisLinePaint(opt.getGraphForeground());
            dateaxis.setTickMarksVisible(opt.isShowTickLines());
            dateaxis.setTickLabelsVisible(opt.isShowTickLines());
            dateaxis.setAxisLineVisible(opt.isShowTickLines());
            dateaxis.setVerticalTickLabels(opt.isShowTickLabels());
            dateaxis.setTickLabelPaint(opt.getGraphForeground());
            
            jfreechart.setBackgroundPaint(opt.getGraphBackground());
            
            LegendTitle legend = jfreechart.getLegend();
            
            legend.setBackgroundPaint(opt.getGraphBackground());
            legend.setItemPaint(opt.getGraphForeground());
            TextTitle tt = jfreechart.getTitle();
            tt.setPaint(opt.getGraphForeground());
            tt.setBackgroundPaint(opt.getGraphBackground());
        } catch (Exception e) {
            Logger.getLogger().logException(e, this);
        }
        
    }
    public void addTimeSeries(String name) {
        
        if (series.get(name) != null) return;
        
        TimeSeries ts = new TimeSeries(name, TimePeriod.class); //org.jfree.data.time.Second.class);
        ts.setMaximumItemCount(numberOfSamples);
        
        timeseriescollection.addSeries(ts);
        series.put(name, ts);
       
                
        
    }
    public void removeTimeSeries(String name) {
        
        TimeSeries ts = series.remove(name);
        
        if (ts != null) timeseriescollection.removeSeries(ts);
    }
    /**
     * This method is used to add an observation to the graph.
     * @param name String - series name value to add.
     * @param value double - series  value to add.
     */
    protected void addObservation(String name, double value, long thisInterval)
    {
        if (lastTime == 0) {
            lastTime = thisInterval - 1000;
        }
        TimeSeries ts = series.get(name);
        TimePeriod period = new TimePeriod(lastTime, thisInterval); //currentTime);
        if (ts != null) ts.addOrUpdate(period, value);
        lastTime = thisInterval;
    	
    	
    }
    @Override
    public void onOptionsChange(UIOptions options) {
        updateStyle(options);
    }
    public void setTitle(String title) {
        jfreechart.setTitle(title);
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

    /**
     * @return the seriesColor
     */
    public final Color[] getSeriesColor() {
        return seriesColor;
    }

    /**
     * @param seriesColor the seriesColor to set
     */
    public final void setSeriesColor(Color[] seriesColor) {
        this.seriesColor = seriesColor;
    }



}

    
