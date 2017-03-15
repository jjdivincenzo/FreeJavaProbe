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

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.regis.jprobe.model.Logger;

/**
 * @author jdivince
 *
 */
public class OptionsTable extends JTable {
    
    private static final long serialVersionUID = 1L;
    private OptionCellRenderer tcr;
    private TableColumnModel tcm;
    private int[] colSizes;

    
    public OptionsTable(TableModel model, JPanel parent, UIOptions options) {
        super(model);
        this.setAutoCreateColumnsFromModel(true);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getTableHeader().setReorderingAllowed(false);
        
        
        colSizes = new int[2];

        tcr = new OptionCellRenderer(parent, options);
        tcm = getColumnModel();
        sizeColumns(colSizes);
    }

    private void sizeColumns(int[] sizes) {
        
        
        int colCount = tcm.getColumnCount();
        
        if (sizes.length < colCount) {
            Logger.getLogger().error("Number of Columns Specified(" + 
                    sizes.length + ") is Less Than Actual(" + colCount + ")");
            return;
        }
        for (int i = 0; i < colCount; i++) {
            tcm.getColumn(i).setPreferredWidth(sizes[i]);
        }
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {

        return tcr;
    }
    public TableCellEditor getCellEditor(int row, int column)
    {
        return tcr;
    }
    public void refresh() {
        
        tcr.reset();
        sizeColumns(colSizes);
    }

}
