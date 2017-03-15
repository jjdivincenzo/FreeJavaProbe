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

import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;

import edu.regis.jprobe.model.LockData;


/**
 * @author jdivince
 *
 * This is the Data Model for the task panel in the Overview
 */
public class LockDataModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1L;
	public static final long NANOS_PER_MILLI = 1000000;
	
	public LockColumnData cdata[] = {
		new LockColumnData("Lock Name", 600, JLabel.LEFT),
		new LockColumnData("Observations", 90, JLabel.LEFT),
		
	};
	
	
	protected Vector<LockData> lockVector;
	
	public LockDataModel() {
		
		lockVector = new Vector<LockData>();
		
		
		
	}
	public void update(Vector<LockData> hostVector) {
	    this.lockVector = hostVector;
	    this.fireTableStructureChanged();
	}
	
	public void resetTable() {
		lockVector.clear();
		fireTableStructureChanged();
	}
	@Override
	public int getRowCount() {
		return lockVector.size();
	}
	@Override
	public int getColumnCount() {
		return cdata.length;
	}

	@Override
	public Object getValueAt(int row, int col) {
		
		if (row >= lockVector.size()) return "Empty";
		LockData bm  = lockVector.get(row);
		if (bm == null) return "";
	   
		switch (col) {
			case LockColumnData.COL_NAME: 
				return bm.getLockName();
			case LockColumnData.COL_COUNT: 
				return bm.getLockCount();
			
			
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
	public int getColumnWidth(int col) {
	    return cdata[col].length;
	}

	
	
	
	
}
class LockColumnData {
	
	public String name;
	public int length;
	public int alignment;
	
	public static final int COL_NAME = 0;
	public static final int COL_COUNT = 1;
	
	
	
	public LockColumnData(String n, int l, int a) {
	
		name = n;
		length = l;
		alignment = a;
		
	}
}

	
