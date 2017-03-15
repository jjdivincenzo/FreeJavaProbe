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
import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import edu.regis.jprobe.model.Utilities;

class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {

	
    private static final long serialVersionUID = 1L;
    private UIOptions options;
	
	public ProgressBarRenderer(UIOptions options) {
		this.setMaximum(100);
		this.setMinimum(0);
		this.setForeground(Color.GREEN);
		this.setStringPainted(true);
		this.options = options;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (value instanceof Double ) {
			Double val = (Double) value;
			int iVal = val.intValue();
			
			if (iVal > 110) {
			    iVal = 0;
			}
			String txt = Utilities.format(val, 1) + "%";
			
			if (iVal < options.getCpuThresholdWarn()) {
				this.setForeground(options.getCpuColorOk());
			} else if (iVal < options.getCpuThresholdBad()) {
				this.setForeground(options.getCpuColorWarn());
			}else {
				this.setForeground(options.getCpuColorBad());
			}
			
			if (row % 2 == 1) {
				this.setBackground(Color.LIGHT_GRAY);
			} else {
				this.setBackground(Color.WHITE);
			}
			this.setString(txt);
			this.setValue(iVal);
		} 
			
		return this;
	}
	
}