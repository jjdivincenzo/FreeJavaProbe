package edu.regis.jprobe.ui;

import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.regis.jprobe.model.Logger;

public class AttributeTable extends JTable {
    
    private static final long serialVersionUID = 1L;
    private AttributeCellRenderer tcr;
    private TableColumnModel tcm;
    private int[] colSizes;

    public AttributeTable(TableModel model) {
        this(model, null, null, null, null);
    }
    public AttributeTable(TableModel model, JPanel parent, 
            ObjectName objectName, 
            Map<String, MBeanAttributeInfo> attrInfoMap,
            Map<String, Attribute> attrMap) {
        super(model);
        this.setAutoCreateColumnsFromModel(true);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getTableHeader().setReorderingAllowed(false);
        int pwidth = 200;
        
        colSizes = new int[2];
        if (parent != null) {
            pwidth = parent.getWidth();
        }
        colSizes[0] = (int)(pwidth * .25);
        colSizes[1] = (int)(pwidth * .75);
        tcr = new AttributeCellRenderer(parent, objectName, attrInfoMap, attrMap);
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