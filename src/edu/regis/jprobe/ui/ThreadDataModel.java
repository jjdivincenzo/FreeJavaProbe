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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.ResponseThreadData;
import edu.regis.jprobe.model.StackExcludeFilters;
import edu.regis.jprobe.model.Utilities;

/**
 * @author jdivince
 *
 * This is the Data Model for the task panel in the Overview
 */
public class ThreadDataModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1L;
	public static final long NANOS_PER_MILLI = 1000000;
	
	public ColumnData cdata[] = {
		new ColumnData("Thread Name", 200, JLabel.LEFT, null),
		new ColumnData("Id", 30, JLabel.LEFT, null),
		new ColumnData("Total CPU(ms)", 40, JLabel.LEFT, null),
		new ColumnData("% of Total CPU", 120, JLabel.LEFT, null),
		new ColumnData("Current CPU(ms)", 40, JLabel.LEFT, null),
		new ColumnData("Current CPU %", 120, JLabel.LEFT, null),
		new ColumnData("Allocated Bytes", 40, JLabel.LEFT, null),
		new ColumnData("Current Stack Frame", 300, JLabel.LEFT, null)
	};
	
	protected Map<Long, ThreadData> threadMap;
	protected Vector<Object> threadVector;
	protected long currentTime;
	protected long startTime;
	protected static ImageIcon COL_UP; 
	protected static ImageIcon COL_DOWN;
	private int sortCol = 0;
	private boolean sortAsc = true;
	public boolean showZeroCPU = false;
	private JTable owner;
	private UIOptions options;
	private StackExcludeFilters filters;
	
	
	public ThreadDataModel(long startTime, UIOptions o) {
		threadMap = new HashMap<Long, ThreadData>();
		threadVector = new Vector<Object>();
		this.startTime = startTime;
		this.options = o;
		//threadBean = ManagementFactory.getThreadMXBean();
		
		IconManager iman = IconManager.getIconManager();
		
		if (o.getSortCol() < 0 || o.getSortCol() > cdata.length -1) {
		    sortCol = 0;
		    o.setSortCol(0);
		} else {
		    sortCol = o.getSortCol();
		}
		sortAsc = o.isSortAsc();
		COL_UP = iman.getUpIcon();
		COL_DOWN = iman.getDownIcon();
		this.filters = o.getFilters();
		
				
		if (sortCol < cdata.length) {
		    
		    if (sortAsc) {
		        cdata[sortCol].icon = COL_UP;
		    } else {
		        cdata[sortCol].icon = COL_DOWN;
		    }
		}
		
	}
	/**
	 * 
	 * @param res
	 * @param cpuRelativeValue
	 * @param cpuDelta
	 */
	public synchronized void update(ProbeResponse res, double cpuRelativeValue, long cpuDelta) 
	   
	{
		
	    
	    owner = this.getOwner();
		currentTime = System.nanoTime() - startTime;
		
		long allThreads[] = res.getAllThreads();	
		
		if (allThreads == null) return; //WTF!!! 
				
		for (int j = 0; j < res.getNumberOfThreadInfo(); j++) {
			
			ResponseThreadData rtd = res.getThreadInfo(j);
			
			String thdName  = rtd.getThreadName();
			Long thdId = new Long(rtd.getThreadId());
			
			ThreadData td = threadMap.get(thdId);
			
			if (td == null ) td = new ThreadData();
			
			td.cpuTime = rtd.getThreadCPU();
			td.name = thdName;
			StackTraceElement[] ste  = rtd.getCurrentStackFrame();
			if (ste == null || ste.length == 0) {
			    td.currentStackFrame = "N/A";
			} else {
		        td.currentStackFrame = filters.getCurrentStackTrace(ste);
			}
			td.threadId = thdId.longValue(); 
			td.allocatedBytes = rtd.getAllocatedBytes();
			td.threadBlocked = rtd.isThreadBlocked();
			td.daemonThread = rtd.isDeamon();
			
			td.currentcpuTime = td.cpuTime - td.lastcpuTime;
			
			td.cpuPercentage = ( (double) 
										((int) ( 
												(td.currentcpuTime / (double)cpuDelta) * 10000)) / 100);
			
			td.currentcpuPercentage = ( ( 
                    ((double) td.currentcpuTime / ( (double) (currentTime - td.lastTime)  ) 
                    * 10000d ) ) / 100d);
			
			td.lastcpuTime = td.cpuTime;
			td.lastTime = currentTime;
			
			if (rtd.isProbeThread()) {
			    if (!options.isShowProbeThread()) {
			        threadMap.remove(thdId);
			        continue;
			    }
			    
			} 
			threadMap.put(thdId,td);
			
			
        }
		
		
		threadVector.clear();
	    Set<Long> keys = threadMap.keySet();				//Get the collection of keys
	    Iterator<Long> iter = keys.iterator();		//create an iterator for them
	    
	    
	    //Loop thru the map
	    while (iter.hasNext()) {
	    	Object obj = iter.next();
	    	ThreadData td = threadMap.get(obj);
	    	
	    	if (td.cpuTime > 0 || options.isShowZeroCPU()) {
	    		
	    		for (int k = 0; k < allThreads.length; k++) {
	    			//only add threads that are still alive...
					if (td.threadId == allThreads[k]) {
						threadVector.add(td);
						
					}
					
				}
	    	}
	    	
	    }
	    
	    
	    sortData();
	    
		fireTableStructureChanged();
	}
	public void resetTable() {
		threadVector.clear();
		fireTableStructureChanged();
	}
	@Override
	public int getRowCount() {
		return threadVector.size();
	}
	@Override
	public int getColumnCount() {
		return cdata.length;
	}

	@Override
	public Object getValueAt(int row, int col) {
		
		if (row >= threadVector.size()) return "Empty";
		ThreadData td = (ThreadData)threadVector.get(row);
		if (td == null) return "";
	   
		switch (col) {
			case ColumnData.COL_NAME: 
				return td.name;
			case ColumnData.COL_ID: 
				return new Long(td.threadId);
			case ColumnData.COL_TOTAL:
				return Utilities.format(td.cpuTime / NANOS_PER_MILLI);
			case ColumnData.COL_PERCENT:
				return new Double(td.cpuPercentage); 
			case ColumnData.COL_CURRENT:
				return Utilities.format((double)td.currentcpuTime / NANOS_PER_MILLI, 2);
			case ColumnData.COL_CURR_PERCENT:
				return new Double(td.currentcpuPercentage); 
			case ColumnData.COL_BYTES:
			    if (td.allocatedBytes < 0) {
			        return "N/A";
			    }
                return Utilities.formatBytes(td.allocatedBytes); 
			case ColumnData.COL_CURR_STACK:
                return td.currentStackFrame; 
		}
		
		return "";
	}
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	@Override
	public String getColumnName(int col) {
		return cdata[col].name;
	}
	
	public boolean isThreadBlocked(int row) {
		
		if (row >= threadVector.size()) return false;
		ThreadData td = (ThreadData)threadVector.get(row);
		if (td == null) return false;
		return td.threadBlocked;
	}
   public boolean isDaemonThread(int row) {
        
        if (row >= threadVector.size()) return false;
        ThreadData td = (ThreadData)threadVector.get(row);
        if (td == null) return false;
        return td.daemonThread;
    }
	public Icon getColumnIcon(int col) {
		
		Icon i = null;
		
		if (col == sortCol) {
			if (sortAsc) {
				i = COL_UP;
			}else {
				i = COL_DOWN;
			}
			cdata[col].icon = i;
		} else {
			cdata[col].icon = null;
		}
		
		
		return i;
		
	}
	protected void sortData() {
		
		Collections.sort(threadVector, 
						 new ThreadComparator(sortCol, sortAsc));
		
	}
	
	/**
	 * @return Returns the owner.
	 */
	public JTable getOwner() {
		return owner;
	}
	/**
	 * @param owner The owner to set.
	 */
	public void setOwner(JTable owner) {
		this.owner = owner;
	}
	public long getThreadId(String name) {
		
		ThreadData td = threadMap.get(name);
		if (td == null) return 0;
		
		return td.threadId;
	}
	public void setDefaultData() {
	    Logger.getLogger().debug("Setting Default Data");
	}

    public int getSortCol() {
        return sortCol;
    }

    public void setSortCol(int sortCol) {
        this.sortCol = sortCol;
        options.setSortCol(sortCol);
    }

    public boolean isSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
        options.setSortAsc(sortAsc);
    }
	
	
}
class ColumnData {
	
	public String name;
	public int length;
	public int alignment;
	public Icon icon;
	public static final int COL_NAME = 0;
	public static final int COL_ID = 1;
	public static final int COL_TOTAL = 2;
	public static final int COL_PERCENT = 3;
	public static final int COL_CURRENT = 4;
	public static final int COL_CURR_PERCENT = 5;
	public static final int COL_BYTES = 6;
	public static final int COL_CURR_STACK = 7;
	
	
	public ColumnData(String n, int l, int a, Icon i) {
	
		name = n;
		length = l;
		alignment = a;
		icon = i;
		//classType = type;
	}
}
class ThreadData {
	
	public long threadId = 0;
	public String name = "";
	public long cpuTime = 0;
	public long lastcpuTime = 0;
	public long lastTime = 0;
	public long currentcpuTime = 0;
	public long allocatedBytes = -1;
	public double cpuPercentage = 0;
	public double currentcpuPercentage = 0;
	public boolean threadBlocked = false;
	public boolean daemonThread = false;
	public String currentStackFrame = "";
	
	public ThreadData() {
		
	}
	public String toString() {
		
		return "Name = " + name + "\n" +
				"CPU Time = " + cpuTime + "\n" +
				"Last CPU Time = " + lastcpuTime + "\n" +
				"Last Time = " + lastTime + "\n" +
				"Current CPU Time = " + currentcpuTime + "\n" +
				"CPU % = " + cpuPercentage + "\n" +
				"Current CPU % = " + currentcpuPercentage + "\n" +
				"Allocated Bytes = " + allocatedBytes + "\n" +
				"Current Stack Frame = " + currentcpuPercentage;
	}
}
class ThreadComparator implements Comparator<Object> {

	protected int sortCol;
	protected boolean sortAsc;
	
	public ThreadComparator(int col, boolean sortA) {
		sortCol = col;
		sortAsc = sortA;
		
	}
	public int compare(Object arg0, Object arg1) {
		
		ThreadData d1 = (ThreadData) arg0;
		ThreadData d2 = (ThreadData) arg1;
		
		int result = 0;
		
		switch (sortCol) {
			case ColumnData.COL_NAME:
				result = d1.name.compareTo(d2.name);
				break;
			case ColumnData.COL_ID:
				result = new Long(d1.threadId).compareTo(new Long(d2.threadId));
				break;
			case ColumnData.COL_TOTAL:
				result = d1.cpuTime < d2.cpuTime ? -1 : (d1.cpuTime > d2.cpuTime ? 1 : 0 );
				break;
			case ColumnData.COL_PERCENT:
				result = d1.cpuPercentage < d2.cpuPercentage ? -1 : (d1.cpuPercentage > d2.cpuPercentage ? 1 : 0 );
				break;
			case ColumnData.COL_CURRENT:
				result = d1.currentcpuTime < d2.currentcpuTime ? -1 : (d1.currentcpuTime > d2.currentcpuTime ? 1 : 0 );
				break;
			case ColumnData.COL_CURR_PERCENT:
				result = d1.currentcpuPercentage < d2.currentcpuPercentage ? -1 : (d1.currentcpuPercentage > d2.currentcpuPercentage ? 1 : 0 );
				break;
			case ColumnData.COL_CURR_STACK:
                result = d1.name.compareTo(d2.name);
                break;
			case ColumnData.COL_BYTES:
                result = d1.allocatedBytes < d2.allocatedBytes ? -1 : (d1.allocatedBytes > d2.allocatedBytes ? 1 : 0 );
                break;
		}
		
		if (!sortAsc) result = -result;
		return result;
	}
	
}