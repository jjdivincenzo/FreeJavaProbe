package edu.regis.jprobe.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class LockTable extends JTable {
    
    private static final long serialVersionUID = 1L;
    private TableModel model;
    private LockRenderer renderer;
    
    //private Integer selectedRow = null;

    public LockTable(TableModel model) {
        super(model);
        this.model = model;
        this.setAutoCreateColumnsFromModel(true);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        this.getTableHeader().setReorderingAllowed(false);
        renderer = new LockRenderer(model);
        
    }
    public void reset() {
        renderer = new LockRenderer(model);
    }
    public void clearRenderer() {
        renderer.reset();
    }
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        return renderer; 
 
    }

    
}
class LockRenderer implements TableCellRenderer {

    //private LockDataModel tdm;
    private Font italic;
    private Font bold;
    private int textSize = 20;
    private int lastSelectedRow = -1;
    private double textCellHeightOffset = 1.7;
    private Map<String, JTextField> cellMap;

    /**
     * 
     * @param tdm
     */
    public LockRenderer(TableModel tdm) {
        
        bold = new JTextField().getFont();
        bold = bold.deriveFont(Font.BOLD);
        italic = bold.deriveFont(Font.ITALIC);
        cellMap = new HashMap<String, JTextField>();
        
        UIOptions options = UIOptions.getOptions();
        
        if (options != null) {
            textCellHeightOffset = options.getTextCellHeightOffset(); 
        }
      
        //this.setFont(bold);
        textSize = (int)(bold.getSize2D() * textCellHeightOffset);
        //this.tdm = (LockDataModel) tdm;
        
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JTextField fld = null;
        String key = row + ":" + column;
        
        fld = cellMap.get(key);
        
        if (fld != null) {
            if (isSelected) {
                lastSelectedRow = row;
            } 
            
            if (row == lastSelectedRow) {
                fld.setBackground(Color.LIGHT_GRAY);
                lastSelectedRow = row;
            } else {
                fld.setBackground(Color.WHITE);
            }
            return fld;
        }
            
        
        fld = new JTextField(value.toString());
        fld.setFont(bold);    
        if (isSelected) {
            fld.setBackground(Color.LIGHT_GRAY);
            lastSelectedRow = row;
        } else {
            fld.setBackground(Color.WHITE);
        }
        
        if (row == lastSelectedRow) {
            fld.setBackground(Color.LIGHT_GRAY);
            lastSelectedRow = row;
        } 
        
        switch (column) {
        case LockColumnData.COL_NAME: 
            fld.setForeground(Color.BLACK);
            fld.setFont(bold);
            break;
        case LockColumnData.COL_COUNT: 
            fld.setForeground(Color.RED);
            fld.setFont(italic);
            break;
        
        
    }
        
            table.setRowHeight(row, textSize);  
            cellMap.put(key, fld);
            //System.out.println("adding " + key);
            return fld;
    }
    public void reset() {
        cellMap.clear();
    }
    
}