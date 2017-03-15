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

public class StackTable extends JTable {
    
    private static final long serialVersionUID = 1L;
    private TableModel model;
    private TableCellRenderer renderer;
    
    //private Integer selectedRow = null;

    public StackTable(TableModel model) {
        super(model);
        this.model = model;
        this.setAutoCreateColumnsFromModel(true);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        this.getTableHeader().setReorderingAllowed(false);
        renderer = new StackRenderer(model);
        
    }
    public void reset() {
        renderer = new StackRenderer(model);
    }
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        return renderer; 
 
    }

    
}
class StackRenderer implements TableCellRenderer {

    
    //private StackDataModel tdm;
    private Font italic;
    private Font bold;
    private int textSize = 20;
    private double textCellHeightOffset = 1.7;
    private Map<String, JTextField> cellMap;

    /**
     * 
     * @param tdm
     */
    public StackRenderer(TableModel tdm) {
        
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
        //this.tdm = (StackDataModel) tdm;
        
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        String key = row + ":" + column;
        
        JTextField fld = cellMap.get(key);
        
            
        
        fld = new JTextField(value.toString());
        fld.setFont(bold);    
        
        switch (column) {
        case StackColumnData.COL_NAME: 
            fld.setForeground(Color.BLACK);
            fld.setFont(bold);
            break;
        case StackColumnData.COL_COUNT: 
            fld.setForeground(Color.RED);
            fld.setFont(italic);
            break;
        
        
    }
        
            table.setRowHeight(row, textSize);  
            cellMap.put(key, fld);
            //System.out.println("adding " + key);
            return fld;
    }
    
}