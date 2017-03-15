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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.ResponseThreadData;



/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ThreadCPUPanel extends PerformancePanel implements IPerformancePanel {

    private static final long serialVersionUID = 1L;
	private ThreadCPUGraph cpuGraph;
	protected long currentTime;
	protected long startTime;
	public static final long NANOS_PER_MILLI = 1000000;
	protected static final long GRAPHING_CPU_THRESHOLD = 500 * NANOS_PER_MILLI; 
	protected Map<Long, ThreadData> threadMap;
	protected Vector<Object> threadVector;
	
	public ThreadCPUPanel(int numberOfSamples, UIOptions options) {
		
		threadMap = new HashMap<Long, ThreadData>();
		threadVector = new Vector<Object>();
		this.startTime = System.nanoTime();
		
		cpuGraph = new ThreadCPUGraph(numberOfSamples, 
	        	"CPU Usage by Thread", options);
		
		JScrollPane resultPane = new JScrollPane( cpuGraph , 
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		
		setBorder(new TitledBorder( new EtchedBorder(), "Thread CPU History"));
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
		add(resultPane,c5);
		
	}
	public void update(ProbeResponse res) {
		
		currentTime = System.nanoTime() - startTime;
		
		long allThreads[] = res.getAllThreads();		
				
		for (int j = 0; j < res.getNumberOfThreadInfo(); j++) {
			
			ResponseThreadData rtd = res.getThreadInfo(j);
			
			String thdName  = rtd.getThreadName();
			Long thdId = new Long(rtd.getThreadId());
			
			ThreadData td = threadMap.get(thdId);
			
			if (td == null ) td = new ThreadData();
			
			td.cpuTime = rtd.getThreadCPU();
			td.name = thdName;
			td.threadId = thdId.longValue(); 
			td.currentcpuTime = td.cpuTime - td.lastcpuTime;
			
			
			td.currentcpuPercentage = ( ( 
                    ((double) td.currentcpuTime / ( (double) (currentTime - td.lastTime)  ) 
                    * 10000d ) ) / 100d);
			
			if (td.currentcpuPercentage > 100) td.currentcpuPercentage = 100;
			
			td.lastcpuTime = td.cpuTime;
			td.lastTime = currentTime;
			
			threadMap.put(thdId,td);
			
        }
		
				
		threadVector.clear();
	    Set<Long> keys = threadMap.keySet();				//Get the collection of keys
	    Iterator<Long> iter = keys.iterator();		//create an iterator for them
	    
	    Vector<Long> deadList = new Vector<Long>();
	    
	    //Loop thru the map
	    while (iter.hasNext()) {
	    	//boolean isAlive = false;
	    	Object obj = iter.next();
	    	ThreadData td = threadMap.get(obj);
	    	if (td.cpuTime > 0 ) {
	    		
	    		if (findThread(td.threadId, allThreads)) {
	    			threadVector.add(td);
	    		} else {
	    		    String entryName = getEntryName(td.name, td.threadId);
	    	    	cpuGraph.removeTimeSeries(entryName);
	    	    	Logger.getLogger().debug("Removing Entry " + entryName + " From Graph");
	    	    	deadList.add(new Long(td.threadId));
	    		}
	    		
	    	}
	    	
	    }
	    
	    for (int i = 0; i < threadVector.size(); i++) {
	    	ThreadData td = (ThreadData) threadVector.get(i);
	    	
	    	if (td.cpuTime > GRAPHING_CPU_THRESHOLD) {
	    		String entryName = getEntryName(td.name, td.threadId);
	    		cpuGraph.addTimeSeries(entryName);
	    		cpuGraph.addObservation(entryName, td.currentcpuPercentage, res.getCurrentTime());
	    	}
	    	
	    }
		
	    for (int i = 0; i < deadList.size(); i++) {
	    	Long thdid = deadList.get(i);
	    	threadMap.remove(thdid);
	    	Logger.getLogger().debug("Removing " + thdid + " From ThreadMap");
	    }
    	
		
	}
	public void resetPanel() {
		
		cpuGraph.reset();
		threadMap.clear();
		threadVector.clear();
	}
	public void setSampleSize(int size) {
		cpuGraph.setSampleSize(size);
		
	}
	protected boolean findThread(long id, long list[]) {
		
		for (int i =0; i < list.length; i++) {
			
			if (id == list[i]) return true;
		}
		
		return false;
	}
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
		throws PrinterException {

		cpuGraph.print(graphics, pageFormat, pageIndex);
		return PAGE_EXISTS;
	}
	
	private String getEntryName(String thName, long thid) {
	    
	    String ret = thid + ":";
	    
	    if (thName.length() > 12) {
	        ret += thName.substring(0, 10) + "...";
	    } else {
	        ret += thName;
	    }
	    
	    return ret;
	}
}
class ThreadCPUGraph extends MiniDynamicTimeSeries {


    private static final long serialVersionUID = 1L;
     /**
     * @param numberOfSamples
     * @param seriesLegendName
     * @param chartName
     */
    public ThreadCPUGraph(int numberOfSamples, String chartName, UIOptions options) {
        super(numberOfSamples, chartName);
        
        
        
        Font baseFont = new JLabel().getFont();
        
        textFont = baseFont.deriveFont(Font.PLAIN, 14);
        titleFont= baseFont.deriveFont(Font.BOLD, 18);
        init();
        numberaxis.setRange(0, 100);
        updateStyle(options);
        options.addChangeListener(this);
    }
    
}