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
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.Utilities;

/**
 * @author jdivince
 *
 */
public class OptionCellRenderer implements TableCellRenderer, TableCellEditor {
   
    private ObjectName objectName;
    private JMXBeanPanel parent;
    private Font bold;
    private Map<String, Component> compMap = new HashMap<String, Component>();
    private UIOptions options;
    private double textCellHeightOffset = 2.0;
    private Field[] fields;
    private Map<String, Method> methodMap = new HashMap<String, Method>();
    //private JTable table;
    /**
     * 
     * @param parent
     * @param options
     */
    public OptionCellRenderer(JPanel parent, UIOptions options) {
        
        
       this.options = options;
        
       fields = options.getClass().getDeclaredFields();

       for (Field field : fields) {
           String name = field.getName();
           String start = name.substring(0, 1);
           Class<?> type = field.getType();
           String pfx = "set";
           
           Class<?>[] parms = {type};


           
           String methodName = pfx + name.replaceFirst(start, start.toUpperCase());
           try {
               Method meth = options.getClass().getDeclaredMethod(methodName, parms);
               methodMap.put(name, meth);
            } catch (Exception e) {
                
            } 
           
       }
       if (options != null) {
            textCellHeightOffset = options.getTextCellHeightOffset(); 
        }
        
        JTextField fld = new JTextField();
        bold = fld.getFont().deriveFont(Font.BOLD);
        
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        
       // this.table = table;
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
        
        if (column == 1) {
            attrName = (String)table.getValueAt(row, 0);
        }
        int textSize = (int)(bold.getSize2D() * textCellHeightOffset);
        
        if (column == 1) {
            Object val = table.getValueAt(row, 0);
            
            if (val != null) {
                writeable = true;
                
            }
        }
        
        
        /*int panelHeight = 150;
        
        if (parentHeight / 2 > 150) {
            panelHeight = parentHeight / 2;
            
        }
        if (hasFocus) {
            //System.out.println("Has Focus on Row " + row + ", col " + column);
            panelHeight = parentHeight;
        }*/
        
         
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
                tf.addFocusListener(buildFocusListener(tf, attrName));
                tf.addKeyListener(buildKeyListener(tf, attrName));
                
            } else {
                tf.setForeground(Color.blue);
                tf.setEditable(false);
            }
            table.setRowHeight(row, textSize);
            return tf; 
        }
            
        if (value instanceof String) {
            JTextField tf = new JTextField(value.toString()); 
            
            
            if (writeable){
               
               tf.setFont(bold);
               tf.addFocusListener(buildFocusListener(tf, attrName));
               tf.addKeyListener(buildKeyListener(tf, attrName));
              
            } else {
                tf.setEditable(false); 
            }
            table.setRowHeight(row, textSize);
            return tf;
        }
            
        if (value instanceof Number) {
            JTextField tf = new JTextField(formatNumber((Number)value));
            tf.setToolTipText("Enter New Value (" + value.getClass().getSimpleName() + ")");
            if (writeable) {
                
                tf.setFont(bold);
                tf.addFocusListener(buildFocusListener(tf, attrName));
                tf.addKeyListener(buildKeyListener(tf, attrName));
                
            } else {
                tf.setEditable(false);
            }
            registerWatchListener(tf, objectName, attrName);
            table.setRowHeight(row, textSize);
            return tf;
        }
        
        if (value instanceof Boolean) {
            JTextField tf = new JTextField(value.toString());
            tf.setToolTipText("Enter New Value (" + value.getClass().getSimpleName() + ")");
            if (writeable) {
                
                tf.setFont(bold);
                tf.addFocusListener(buildFocusListener(tf, attrName));
                tf.addKeyListener(buildKeyListener(tf, attrName));
               
            } else {
                tf.setEditable(false);
            }
            table.setRowHeight(row, textSize);
            return tf;
        }
            
        
        JTextField tf = new JTextField("Unsupported Data Type");
        tf.setFont(bold);
        tf.addFocusListener(buildFocusListener(tf, attrName));
        tf.addKeyListener(buildKeyListener(tf, attrName));
        tf.setForeground(Color.RED);
    
        tf.setEditable(false);   
    
        return tf;
            
        
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
                    if (parent.isConnectedToJMX()) {
                        new WatchFrame(JProbeUI.getStaticFrame(), parent, objectName, attrName);
                    }
                      
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
    
    private FocusListener buildFocusListener(final JTextField field, final String fieldName) {
        
        FocusListener fl = new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                setField(fieldName, field);
                //System.out.println("Setting " + fieldName + 
                //        " to " + field.getText());
            }
            
            
        };
        
        return fl;
        
    }
    private KeyListener buildKeyListener(final JTextField field, final String fieldName) {
        
        KeyListener kl = new KeyAdapter() {

             public void keyReleased(KeyEvent e) {
                //System.out.println("Key=" + e.toString());
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {  
                    setField(fieldName, field);
                    System.out.println("Setting " + fieldName + 
                            " to " + field.getText());
                }
                
            }
            
            
        };
        
        return kl;
        
    }
    
    private void setField(String name,
            JTextField data) {
        
        for (Field fld : fields) {
            
            if (fld.getName().equals(name)) {
                try {
                    Object val = getObject(fld.getType(), data.getText());
                    Method meth = methodMap.get(fld.getName());
                    try {
                        if (meth != null) {
                            meth.setAccessible(true);
                            meth.invoke(options, val);
                            continue;
                        }
                    } catch (Exception e) {
                        
                    }
                    fld.setAccessible(true);
                    fld.set(options, val);
                } catch (Exception e) {
                    String error = "Exception(" + e.getClass().getName() + 
                            ") ocurred  \nSetting Option(" + name +
                            "), error is " + e.getMessage() ;
                    Logger.getLogger().logException(e, this);
                    JOptionPane.showMessageDialog(parent, 
                            error,
                             "Set Attribute Error", JOptionPane.ERROR_MESSAGE, null);
                    try {
                        fld.setAccessible(true);
                        data.setText(format(fld.get(options)));
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        data.setText(e1.getMessage());
                    }
                    
                }
            }
        }
        Logger.getLogger().info("Setting Attribute " + 
            name + " to " + data + ")"
            );
        
        
    }
    private Object getObject(Class<?> fieldType, String data) throws Exception {
        
        if (fieldType == Integer.TYPE) {
            return new Integer(data.replaceAll(",", ""));
        }
        if (fieldType == Long.TYPE) {
            return new Long(data.replaceAll(",", ""));
        }
        if (fieldType == Float.TYPE) {
            return new Float(data);
        }
        if (fieldType == Double.TYPE) {
            return new Double(data);
        }
        if (fieldType == String.class) {
            return data;
        }

        if (fieldType == Boolean.TYPE) {
            return new Boolean(data);
        }
        
        throw new Exception("Unsupported Data Type of " + fieldType.toString());
        
    }
    public void reset() {
        compMap.clear();
    }

    private String format(Object value) {
        
        if (value instanceof Number) {
            return formatNumber((Number) value);
        }
        
        return (value == null ? "<null>" : value.toString());
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
