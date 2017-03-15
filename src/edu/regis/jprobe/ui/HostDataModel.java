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

import edu.regis.jprobe.model.BroadcastMessage;

/**
 * @author jdivince
 *
 * This is the Data Model for the task panel in the Overview
 */
public class HostDataModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1L;
	public static final long NANOS_PER_MILLI = 1000000;
	
	public HostColumnData cdata[] = {
		new HostColumnData("Probe Name", 70, JLabel.LEFT),
		new HostColumnData("Host Name", 90, JLabel.LEFT),
		new HostColumnData("Host IP", 80, JLabel.LEFT),
		new HostColumnData("Port", 20, JLabel.LEFT),
		new HostColumnData("Process ID", 30, JLabel.LEFT),
		
	};
	
	
	protected Vector<BroadcastMessage> hostVector;
	
	public HostDataModel() {
		
		hostVector = new Vector<BroadcastMessage>();
		
		
		
	}
	public void update(Vector<BroadcastMessage> hostVector) {
	    this.hostVector = hostVector;
	    this.fireTableStructureChanged();
	}
	
	public void resetTable() {
		hostVector.clear();
		fireTableStructureChanged();
	}
	@Override
	public int getRowCount() {
		return hostVector.size();
	}
	@Override
	public int getColumnCount() {
		return cdata.length;
	}

	@Override
	public Object getValueAt(int row, int col) {
		
		if (row >= hostVector.size()) return "Empty";
		BroadcastMessage bm  = hostVector.get(row);
		if (bm == null) return "";
	   
		switch (col) {
			case HostColumnData.COL_NAME: 
				return bm.idName;
			case HostColumnData.COL_HOST: 
				return bm.hostName;
			case HostColumnData.COL_IP:
				return bm.hostIP;
			case HostColumnData.COL_PORT:
				return bm.portNumber; 
			case HostColumnData.COL_PID:
				return bm.processID;
			
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
class HostColumnData {
	
	public String name;
	public int length;
	public int alignment;
	
	public static final int COL_NAME = 0;
	public static final int COL_HOST = 1;
	public static final int COL_IP = 2;
	public static final int COL_PORT = 3;
	public static final int COL_PID = 4;
	
	
	
	public HostColumnData(String n, int l, int a) {
	
		name = n;
		length = l;
		alignment = a;
		
	}
}

	
