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

import static edu.regis.jprobe.model.Utilities.format;
import static edu.regis.jprobe.model.Utilities.formatBytes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import edu.regis.jprobe.jni.OSProcessInfo;
import edu.regis.jprobe.jni.OSSystemInfo;
import edu.regis.jprobe.model.Logger;

/**
 * @author jdivince
 *
 * This is the Data Model for the task panel in the Overview
 */
public class ProcessDataModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1L;
	public static final long NANOS_PER_MILLI = 1000000;
	
	public ProcessColumnData cdata[] = {
		new ProcessColumnData("Process Name", 200, JLabel.LEFT, null),
		new ProcessColumnData("User Name", 150, JLabel.LEFT, null),
		new ProcessColumnData("PID", 30, JLabel.LEFT, null),
		new ProcessColumnData("Image Type", 40, JLabel.LEFT, null),
		new ProcessColumnData("Total CPU(ms)", 50, JLabel.LEFT, null),
		new ProcessColumnData("Current CPU %", 150, JLabel.LEFT, null),
		new ProcessColumnData("IO Reads", 50, JLabel.LEFT, null),
		new ProcessColumnData("IO Writes", 50, JLabel.LEFT, null),
		new ProcessColumnData("IO Other", 50, JLabel.LEFT, null),
		new ProcessColumnData("Page Faults", 50, JLabel.LEFT, null),
		new ProcessColumnData("PF Delta", 40, JLabel.LEFT, null),
		new ProcessColumnData("WS Size", 60, JLabel.LEFT, null),
		new ProcessColumnData("Priv Size", 60, JLabel.LEFT, null)
	};
	
	protected Map<Long, ProcessData> processMap;
	protected Vector<ProcessData> processVector;
	protected long currentTime;
	protected long startTime;
	private long totalProcesses = 0;
	private long totalThreads = 0;
	private long totalHandles = 0;
	protected static ImageIcon COL_UP; 
	protected static ImageIcon COL_DOWN;
	private int sortCol = 0;
	private long numCPUs = 1;
	private boolean sortAsc = true;
	public boolean showZeroCPU = false;
	private JTable owner;
	private UIOptions options;
	
	
	
	public ProcessDataModel(long startTime, UIOptions o) {
		processMap = new HashMap<Long, ProcessData>();
		processVector = new Vector<ProcessData>();
		this.startTime = startTime;
		this.options = o;
		
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
		
		if (OSSystemInfo.isOperational()) {
		    numCPUs = OSSystemInfo.getNumberOfCPUs();
		}
				
		if (sortCol < cdata.length) {
		    
		    if (sortAsc) {
		        cdata[sortCol].icon = COL_UP;
		    } else {
		        cdata[sortCol].icon = COL_DOWN;
		    }
		}
		
	}
	
	public synchronized void update(List<OSProcessInfo> piList, long timeDelta) {
		
	    totalProcesses = piList.size();
	    totalThreads = 0;
	    totalHandles = 0;
	    owner = this.getOwner();
		currentTime = System.nanoTime() - startTime;
		Set<Long> allPIDS = new HashSet<Long>();
						
		for (OSProcessInfo process : piList) {
			
			Long pid = process.getProcessId();
			allPIDS.add(pid);
			
			totalThreads += process.getThreadCount();
			totalHandles += process.getHandleCount();
			
			ProcessData pd = processMap.get(pid);
			
			if (pd == null ) {
			    pd = new ProcessData(process);
			    processMap.put(pid,pd);
			}
			
			pd.pageFaultsDelta = process.getPageFaults() - pd.pageFaults;
						
			long cpuDiff = process.getTotalCPU() - pd.totalCPU;
			
			pd.percentCPU = ((((double)cpuDiff / (double)timeDelta) * 100) / numCPUs);

			pd.update(process);
			
			
        }
		
		
		processVector.clear();
	    Set<Long> keys = processMap.keySet();				//Get the collection of keys
	    Iterator<Long> iter = keys.iterator();		//create an iterator for them
	    List<Long> dead = new ArrayList<Long>();
	    
	    //Loop thru the map 
	    while (iter.hasNext()) {
	    	Long obj = iter.next();
	    	ProcessData td = processMap.get(obj);
	    	if (allPIDS.contains(td.processId)) {
	    	    processVector.add(td); 
	    	} else {
	    	    dead.add(td.processId);
	    	}
			
	    }
	    //remove dead processes
	    for (Long pid : dead) {
	        processMap.remove(pid);
	    }
	    
	    sortData();
	    
		fireTableStructureChanged();
	}
	public void resetTable() {
		processVector.clear();
		fireTableStructureChanged();
	}
	@Override
	public int getRowCount() {
		return processVector.size();
	}
	@Override
	public int getColumnCount() {
		return cdata.length;
	}

	@Override
	public Object getValueAt(int row, int col) {
		
		if (row >= processVector.size()) return "Empty";
		ProcessData td = processVector.get(row);
		if (td == null) return "";
	   
		switch (col) {
		case ProcessColumnData.COL_NAME:
            return td.processName;
		case ProcessColumnData.COL_USER:
            return td.user;

        case ProcessColumnData.COL_ID:
            return new Long(td.processId);

        case ProcessColumnData.COL_TYPE:
            return td.imageType;

        case ProcessColumnData.COL_TOTAL_CPU:
            return format(td.totalCPU);

        case ProcessColumnData.COL_PERCENT_CPU:
            return new Double(td.percentCPU);
 
        case ProcessColumnData.COL_IO_READS:
            return format(td.ioReads);
            
        case ProcessColumnData.COL_IO_WRITES:
            return format(td.ioWrites);
            
        case ProcessColumnData.COL_IO_OTHER:
            return format(td.ioOther);
           
        case ProcessColumnData.COL_PAGE_FAULTS_DELTA:
            return format(td.pageFaultsDelta);
            
        case ProcessColumnData.COL_PAGE_FAULTS:
            return format(td.pageFaults);
            
        case ProcessColumnData.COL_WS_SIZE:
            return formatBytes(td.workingSetSize);
            
        case ProcessColumnData.COL_PRIVATE_SIZE:
            return formatBytes(td.privateUsage);
            
			
		}
		
		return "";
	}
	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	public ProcessData getProcess(long pid) {
	    return processMap.get(pid);
	}
	@Override
	public String getColumnName(int col) {
		return cdata[col].name;
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
		
		Collections.sort(processVector, 
						 new ProcessComparator(sortCol, sortAsc));
		
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

    /**
     * @return the totalProcesses
     */
    public final long getTotalProcesses() {
        return totalProcesses;
    }

    /**
     * @return the totalThreads
     */
    public final long getTotalThreads() {
        return totalThreads;
    }

    /**
     * @return the totalHandles
     */
    public final long getTotalHandles() {
        return totalHandles;
    }
	
	
}
class ProcessColumnData {
	
	public String name;
	public int length;
	public int alignment;
	public Icon icon;
	public static final int COL_NAME = 0;
	public static final int COL_USER = 1;
	public static final int COL_ID = 2;
	public static final int COL_TYPE = 3;
	public static final int COL_TOTAL_CPU = 4;
	public static final int COL_PERCENT_CPU = 5;
	public static final int COL_IO_READS = 6;
	public static final int COL_IO_WRITES = 7;
	public static final int COL_IO_OTHER = 8;
	public static final int COL_PAGE_FAULTS = 9;
	public static final int COL_PAGE_FAULTS_DELTA = 10;
	public static final int COL_WS_SIZE = 11;
	public static final int COL_PRIVATE_SIZE = 12;
	
	
	public ProcessColumnData(String n, int l, int a, Icon i) {
	
		name = n;
		length = l;
		alignment = a;
		icon = i;
		//classType = type;
	}
}
class ProcessComparator implements Comparator<ProcessData> {

	protected int sortCol;
	protected boolean sortAsc;
	
	public ProcessComparator(int col, boolean sortA) {
		sortCol = col;
		sortAsc = sortA;
		
	}
	public int compare(ProcessData d1, ProcessData d2) {
		

		
		int result = 0;
		
		switch (sortCol) {
			case ProcessColumnData.COL_NAME:
				result = d1.processName.toUpperCase().compareTo(d2.processName.toUpperCase());
				break;
			case ProcessColumnData.COL_USER:
                result = d1.user.toUpperCase().compareTo(d2.user.toUpperCase());
                break;
			case ProcessColumnData.COL_ID:
				result = new Long(d1.processId).compareTo(new Long(d2.processId));
				break;
			case ProcessColumnData.COL_TYPE:
				result = d1.imageType.compareTo(d2.imageType);
				break;
			case ProcessColumnData.COL_TOTAL_CPU:
				result = d1.totalCPU < d2.totalCPU ? -1 : (d1.totalCPU > d2.totalCPU ? 1 : 0 );
				break;
			case ProcessColumnData.COL_PERCENT_CPU:
				result = d1.percentCPU < d2.percentCPU ? -1 : (d1.percentCPU > d2.percentCPU ? 1 : 0 );
				break;
			case ProcessColumnData.COL_IO_READS:
				result = d1.ioReads < d2.ioReads ? -1 : (d1.ioReads > d2.ioReads ? 1 : 0 );
				break;
			case ProcessColumnData.COL_IO_WRITES:
                result =  d1.ioWrites < d2.ioWrites ? -1 : (d1.ioWrites > d2.ioWrites ? 1 : 0 );
                break;
            case ProcessColumnData.COL_IO_OTHER:
                result =  d1.ioOther < d2.ioOther ? -1 : (d1.ioOther > d2.ioOther ? 1 : 0 );
                break;
			case ProcessColumnData.COL_PAGE_FAULTS_DELTA:
                result = d1.pageFaultsDelta < d2.pageFaultsDelta ? -1 : (d1.pageFaultsDelta > d2.pageFaultsDelta ? 1 : 0 );
                break;
			case ProcessColumnData.COL_PAGE_FAULTS:
                result = d1.pageFaults < d2.pageFaults ? -1 : (d1.pageFaults > d2.pageFaults ? 1 : 0 );
                break;
			case ProcessColumnData.COL_WS_SIZE:
                result = d1.workingSetSize < d2.workingSetSize ? -1 : (d1.workingSetSize > d2.workingSetSize ? 1 : 0 );
                break;
			case ProcessColumnData.COL_PRIVATE_SIZE:
                result = d1.privateUsage < d2.privateUsage ? -1 : (d1.privateUsage > d2.privateUsage ? 1 : 0 );
                break;
		}
		
		if (!sortAsc) result = -result;
		return result;
	}
	
}