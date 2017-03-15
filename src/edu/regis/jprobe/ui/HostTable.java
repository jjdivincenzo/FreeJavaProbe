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

public class HostTable extends JTable {
    
    private static final long serialVersionUID = 1L;
    private TableModel model;
    private TableCellRenderer renderer;
    
    //private Integer selectedRow = null;

    public HostTable(TableModel model) {
        super(model);
        this.model = model;
        this.setAutoCreateColumnsFromModel(true);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        this.getTableHeader().setReorderingAllowed(false);
        renderer = new HostRenderer(model);
        
    }
    public void reset() {
        renderer = new HostRenderer(model);
    }
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        return renderer; // new HostRenderer(model);
        /*String key = row + ":" + column;
        
        TableCellRenderer ret = cellMap.get(key);
        
        if (ret == null) {
            ret = new HostRenderer(model);
            cellMap.put(key, ret);
        }
        
       return ret;*/
       // return super.getCellRenderer(row, column);
    }

    
}
class HostRenderer implements TableCellRenderer {

    //private static final long serialVersionUID = 1L;
    //private HostDataModel tdm;
    private Font italic;
    private Font bold;
    private int textSize = 20;
    private double textCellHeightOffset = 1.7;
    private Map<String, JTextField> cellMap;
    /**
     * 
     * @param tdm
     */
    public HostRenderer(TableModel tdm) {
        
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
        //this.tdm = (HostDataModel) tdm;
        
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        String key = row + ":" + column;
        
        JTextField fld = cellMap.get(key);
        
        if (fld != null) {
            if (isSelected) {
                fld.setBackground(Color.GRAY);
            } else {
                fld.setBackground(Color.WHITE);
            }
            return fld;
        }
        
        
        fld = new JTextField(value.toString());
        fld.setFont(bold);    
              
        if (isSelected) {
            fld.setBackground(Color.GRAY);
        } else {
            fld.setBackground(Color.WHITE);
        }
        
        
        //this.setText(value.toString());
        
        switch (column) {
        case HostColumnData.COL_NAME: 
            fld.setForeground(Color.BLACK);
            fld.setFont(bold);
            break;
        case HostColumnData.COL_HOST: 
            fld.setForeground(Color.BLACK);
            fld.setFont(italic);
            break;
        case HostColumnData.COL_IP:
            fld.setForeground(Color.BLUE);
            fld.setFont(italic);
            break;
        case HostColumnData.COL_PORT:
            fld.setForeground(Color.RED);
            fld.setFont(italic);
            break;
        case HostColumnData.COL_PID:
            fld.setForeground(Color.BLACK);
            fld.setFont(bold);
            break;
        
    }
        
            table.setRowHeight(row, textSize);  
            cellMap.put(key, fld);
            //System.out.println("adding " + key);
            return fld;
    }
    
}