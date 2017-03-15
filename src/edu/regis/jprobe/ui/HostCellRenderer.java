package edu.regis.jprobe.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import edu.regis.jprobe.model.DataFormatter;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.Utilities;

public class HostCellRenderer implements TableCellRenderer, TableCellEditor {
    
    private int parentHeight = 100;
    private ObjectName objectName;
    private Map<String, MBeanAttributeInfo> attrInfoMap;
    private Map<String, Attribute> attrMap;
    private JMXBeanPanel parent;
    private Font bold;
    private Map<String, Component> compMap = new HashMap<String, Component>();
    private UIOptions options;
    private double textCellHeightOffset = 2.0;
    
    public HostCellRenderer(JPanel parent, 
            ObjectName objectName, 
            Map<String, MBeanAttributeInfo> attrInfoMap, 
            Map<String, Attribute> attrMap) {
        
        if (parent != null){
            this.parentHeight = parent.getHeight();//parentHeight;
            if (parent instanceof JMXBeanPanel) {
                this.parent = (JMXBeanPanel) parent;
            }
        }
        this.attrInfoMap = attrInfoMap;
        this.attrMap = attrMap;
        this.objectName = objectName;
        options = UIOptions.getOptions();
        
        if (options != null) {
            textCellHeightOffset = options.getTextCellHeightOffset(); 
        }
        
         JTextField fld = new JTextField();
        bold = fld.getFont().deriveFont(Font.BOLD);
        
    }
    
   

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        
        Component comp = compMap.get(row + ":" + column);
        
        if (comp != null) {
            return comp;
        }
        
        //final Component 
        comp = renderComponent(table, value, isSelected, hasFocus, row, column);
        
        compMap.put(row + ":" + column, comp);
        
          
        return comp;
    }
    /**
     * 
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return
     */
    private Component renderComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) { 
        
        boolean writeable = false;
        String attrName = null;
        MBeanAttributeInfo info = null;
        Attribute attr = null;
        int textSize = (int)(bold.getSize2D() * textCellHeightOffset);
        
        if (column == 1 && attrInfoMap != null) {
            Object val = table.getValueAt(row, 0);
            if (val != null) {
                attrName = val.toString();
                info = attrInfoMap.get(attrName);
                attr = attrMap.get(attrName);
                if (info != null) {
                    writeable = info.isWritable();
                }
            }
        }
        
        
        int panelHeight = 150;
        
        if (parentHeight / 2 > 150) {
            panelHeight = parentHeight / 2;
            
        }
        if (hasFocus) {
            //System.out.println("Has Focus on Row " + row + ", col " + column);
            panelHeight = parentHeight;
        }
        
         
        if (row == -1) {
            JLabel lbl = new JLabel();
            if (value == null) {
                lbl.setText("<null>");
                lbl.setForeground(Color.RED);
            } else {
                lbl.setForeground(Color.BLUE);
                lbl.setText(value.toString());
            }
            return lbl;
        }
        
            
        if (value == null) {
            JTextField tf = new JTextField("<null>");
            
            if (writeable){
                
                tf.setForeground(Color.pink);
                tf.setFont(bold);
                tf.addFocusListener(buildFocusListener(tf, info, attr));
                tf.addKeyListener(buildKeyListener(tf, info, attr));
                
            } else {
                tf.setForeground(Color.blue);
                tf.setEditable(false);
            }
            table.setRowHeight(row, textSize);
            return tf; 
        }
            
        if (value instanceof String) {
            JTextField tf = new JTextField(value.toString()); 
            
            if (value.toString().equals("<Not Serializable>")) {
                tf.setForeground(Color.red);
                tf.setToolTipText("This Object does not implement java.io.Serializable and " + 
                        "cannot be obtained");
                
                writeable = false;
            }
            if (writeable){
               
               tf.setFont(bold);
               tf.addFocusListener(buildFocusListener(tf, info, attr));
               tf.addKeyListener(buildKeyListener(tf, info, attr));
              
            } else {
                tf.setEditable(false); 
            }
            table.setRowHeight(row, textSize);
            return tf;
        }
            
        if (value instanceof Number) {
            JTextField tf = new JTextField(formatNumber((Number)value));
            
            if (writeable) {
                
                tf.setFont(bold);
                tf.addFocusListener(buildFocusListener(tf, info, attr));
                tf.addKeyListener(buildKeyListener(tf, info, attr));
                
            } else {
                tf.setEditable(false);
            }
            registerWatchListener(tf, objectName, attrName);
            table.setRowHeight(row, textSize);
            return tf;
        }
        
        if (value instanceof Boolean) {
            JTextField tf = new JTextField(value.toString());
            
            if (writeable) {
                
                tf.setFont(bold);
                tf.addFocusListener(buildFocusListener(tf, info, attr));
                tf.addKeyListener(buildKeyListener(tf, info, attr));
               
            } else {
                tf.setEditable(false);
            }
            table.setRowHeight(row, textSize);
            return tf;
        }
            
        Class<?> valClass = value.getClass();
        if (valClass.isArray()) {
               int len = Array.getLength(value);
               
               List<Object> data = new ArrayList<Object>();
               for (int i = 0; i < len; i++) {
                   data.add(Array.get(value, i));
                  
                   
               }
               Container p = DataFormatter.formatCollection(data);
               table.setRowHeight(row, panelHeight); 
               //JScrollPane sp = new JScrollPane(p);
               //sp.setWheelScrollingEnabled(true);
               //registerListener(sp, data);
               //return sp;
               
               registerListener(p, data);
               return p;
               
        }
            
        if (value instanceof TabularData) {
                
             Container p = DataFormatter.formatTabularData((TabularData)value) ;
             table.setRowHeight(row, panelHeight); //p.getHeight());
             JScrollPane sp = new JScrollPane(p);
             sp.setWheelScrollingEnabled(true);
             registerListener(sp, value);
             return sp;
             //registerListener(p, value);
             //return p;
        }
        if (value instanceof CompositeData) {
            
            Container p = DataFormatter.formatCompositeData((CompositeData)value) ;
            table.setRowHeight(row, panelHeight); //p.getHeight());
            JScrollPane sp = new JScrollPane(p);
            sp.setWheelScrollingEnabled(true);
            registerListener(sp, value);
            return sp;
            //registerListener(p, value);
            //return p;
            
        }
        if (value instanceof Map) {
            
            Container p = DataFormatter.formatMap((Map<?,?>)value) ;
            table.setRowHeight(row, panelHeight); //p.getHeight());
            //JScrollPane sp = new JScrollPane(p);
            //sp.setWheelScrollingEnabled(true);
            //registerListener(sp, value);
            //return sp;
            registerListener(p, value);
            return p;
        }
        if (value instanceof Collection) {
            
            Container p = DataFormatter.formatCollection((Collection<?>)value) ;
            table.setRowHeight(row, panelHeight); //p.getHeight());
            //JScrollPane sp = new JScrollPane(p);
            //sp.setWheelScrollingEnabled(true);
            //registerListener(sp, value);
            //return sp;
            registerListener(p, value);
            return p;
        }

            
        Container p = DataFormatter.formatObject(value) ;
        table.setRowHeight(row, panelHeight); //p.getHeight());
        //JScrollPane sp = new JScrollPane(p);
        //sp.setWheelScrollingEnabled(true);
        //registerListener(sp, value);
        //return sp;
        registerListener(p, value);
        return p;
    }


    private void registerListener(Component comp, final Object value) {
        //System.out.println("Register Listener for " + value.getClass().getName());
        comp.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    //System.out.println("Right Click");
                    new JMXPopoutDialog(JProbeUI.getStaticFrame(), value);
                      
                }
                if (e.getButton() == MouseEvent.BUTTON1 && 
                        e.getClickCount() > 1) {
                    //System.out.println("Left Double Click");
                    new JMXPopoutDialog(JProbeUI.getStaticFrame(), value);
                      
                }
                
             }
        });
    }
    private void registerWatchListener(Component comp, final ObjectName objectName, final String attrName) {
        //System.out.println("Register Listener for " + value.getClass().getName());
        comp.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (objectName != null &&
                    e.getButton() == MouseEvent.BUTTON1 && 
                        e.getClickCount() > 1) {
                    //System.out.println("Left Double Click");
                    new WatchFrame(JProbeUI.getStaticFrame(), parent, objectName, attrName);
                      
                }
                
             }
        });
    }
    @Override
    public Object getCellEditorValue() {
       
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        //System.out.println("isCellEditable:" + anEvent.toString());
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        //System.out.println("ShouldSelectCell:" + anEvent.toString());
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        //System.out.println("Stop Editing");
        return true;
    }

    @Override
    public void cancelCellEditing() {
        //System.out.println("Cancel Editing");
        
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Component comp = compMap.get(row + ":" + column);
        if (comp == null) {
            comp = new JLabel("<Component Not Found>");
        }
        
        comp.repaint();
        return comp;
    }
    /**
     * 
     * @param field
     * @param attrInfo
     * @param attr
     * @return
     */
    private FocusListener buildFocusListener(final JTextField field, 
            final MBeanAttributeInfo attrInfo,
            final Attribute attr) {
        
        FocusListener fl = new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                /*setAttribute(attrInfo,
                        attr,
                        field.getText());
                */
            }
            
            
        };
        
        return fl;
        
    }
    private KeyListener buildKeyListener(final JTextField field, 
            final MBeanAttributeInfo attrInfo,
            final Attribute attr) {
        
        KeyListener kl = new KeyAdapter() {

             public void keyReleased(KeyEvent e) {
                //System.out.println("Key=" + e.toString());
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {  
                    setAttribute(attrInfo,
                        attr,
                        field.getText());
                }
                
            }
            
            
        };
        
        return kl;
        
    }
    
    private void setAttribute(MBeanAttributeInfo attrInfo,
            Attribute attr, String data) {
        
        Logger.getLogger().info("Setting Attribute " + 
            attr.getName() + " to " + data + ", Class(" +
            attrInfo.getType() + ")"
            );
        
        try {
            Object obj = buildParm(data, attrInfo.getType());
            Attribute newAttr = new Attribute(attr.getName(), obj);
            parent.setAttribute(objectName, newAttr);
            JOptionPane.showMessageDialog(parent, 
                    "Attribute " + attr.getName() + " Has Been Set To " + data,
                     "Attribute Set", JOptionPane.INFORMATION_MESSAGE, null);
        } catch(Exception e) {
            String error = "Exception(" + e.getClass().getName() + 
                    ") ocurred  \nSetting Attribute(" + attr.getName() +
                    "), error is " + e.getMessage() ;
            Logger.getLogger().logException(e, this);
            JOptionPane.showMessageDialog(parent, 
                    error,
                     "Set Attribute Error", JOptionPane.ERROR_MESSAGE, null);
            return;
        }
    }
    public void reset() {
        compMap.clear();
    }
    private Object buildParm(String data, String sig) {
           
            if (sig.equals("java.lang.String")) {
                return data;
            }
            
            if (sig.equals("boolean")) {
                if (data.trim().equalsIgnoreCase("true") || 
                    data.trim().equalsIgnoreCase("false")) {
                    return Boolean.getBoolean(data.trim());
                }
                throw new IllegalArgumentException("Invalid Boolean Value");
            }
            
            if (sig.equals("int")) {
                    return Integer.valueOf(data.trim());
            }
            if (sig.equals("short")) {
                    return Short.valueOf(data.trim());
             }
            if (sig.equals("long")) {
                    return Long.valueOf(data.trim());
            }
            if (sig.equals("float")) {
                    return Float.valueOf(data.trim());
            }
            if (sig.equals("double")) {
                    return Double.valueOf(data.trim());
            }
        
        throw new IllegalArgumentException("Type " + sig + " is not supported");
    }
    
    private String formatNumber(Number number) {
        
        if (number instanceof Double) {
            return Utilities.format(number.doubleValue(), 9);
        }
        if (number instanceof Float) {
            return Utilities.format(number.floatValue(), 9);
        }
        if (number instanceof BigDecimal) {
            return Utilities.format(number.doubleValue(), 9);
        }
        if (number instanceof Integer ) {
            return Utilities.format(number.intValue());
        }
        if (number instanceof BigInteger) {
            return Utilities.format(number.longValue());
        }
        if (number instanceof Long ) {
            return Utilities.format(number.longValue());
        }
        if (number instanceof Short ) {
            return Utilities.format(number.shortValue());
        }
        
        
        return number.toString();
    }
    
}