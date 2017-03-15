package edu.regis.jprobe.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.Utilities;

public class NotificationSubscriberPanel extends JPanel implements NotificationListener {
   
    private static final long serialVersionUID = 1L;
    private MBeanServerConnection connection;
    private ObjectName objectName;
    private JTable infoTable;
    private DefaultTableModel model;
    private List<List<Object>> dataList;
    private int columns = 0;
    private String[] colNames = {"TimeStamp", "Type", " User Data", "Seq", "Message", "Event", " Source"};
    private int[] colWidths = {80, 60, 150, 30, 400, 100, 100};
    private boolean connected = false;
    private Logger logger;
    private TableColumnModel tcm;
    
    public NotificationSubscriberPanel(ObjectName objectName, 
            MBeanServerConnection connection) {
            
        this.connection = connection;
        this.objectName = objectName;
        this.dataList = new ArrayList<List<Object>>();
        logger = Logger.getLogger();
        
        setBorder(new EtchedBorder());
        setLayout(new GridLayout(1,1,1,1));

        
        columns = colNames.length;
        model = new DefaultTableModel();
        model.setColumnIdentifiers(colNames);
        model.setColumnCount(columns);

        //infoTable = new JTable(model);
        infoTable = new SubscriptionTable(model);
        infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        infoTable.getTableHeader().setReorderingAllowed(false);
        tcm = infoTable.getColumnModel();
        tcm.addColumnModelListener(new TableColumnModelListener() {

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                colWidths = getColumnSizes();
                
            }

            public void columnMoved(TableColumnModelEvent e) {}
            public void columnAdded(TableColumnModelEvent e) {}
            public void columnRemoved(TableColumnModelEvent e) {}
            public void columnSelectionChanged(ListSelectionEvent e) {}
            
        });
        
        JScrollPane sp = new JScrollPane(infoTable);
        add(sp);
        subscribe();
        sizeColumns(colWidths);
            
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {

        logger.info("Notification Recieved from " + 
                (handback == null ? "<null>" : handback.toString()));
  
        List<Object> data = new ArrayList<Object>();
        
        data.add(Utilities.formatTimeStamp(notification.getTimeStamp()));
        data.add(notification.getType());
        data.add(notification.getUserData());
        data.add(Utilities.format(notification.getSequenceNumber()));
        data.add(notification.getMessage());
        data.add(notification);
        data.add(notification.getSource());
        dataList.add(data);
        populate();
        
    } 
    
    public void clear() {
        dataList.clear();
        populate();
    }
 
    private void populate() {
        
        int rows = dataList.size();
        
        Object[][] data = new Object[rows] [columns];
        
        for (int i = 0; i < rows; i++) {
            List<Object> row = dataList.get(i);
            for (int k = 0; k < columns; k++) {
                data[i][k] = row.get(k);
            }
        }
                
        
        model.setDataVector(data, colNames);
        model.fireTableDataChanged();
        int[] currentWidth = getColumnSizes();
        
        if (Arrays.equals(colWidths, currentWidth)) {
            System.out.println("Sizes Changed");
        }
        
            sizeColumns(colWidths);
        //} else {
        //    sizeColumns(currentWidth);
        //}
        
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
    
    private int[] getColumnSizes() {
        
        TableColumnModel tcm = infoTable.getColumnModel();
        int colCount = tcm.getColumnCount();
        int[] ret = new int[colCount];
        
        
        for (int i = 0; i < colCount; i++) {
            ret[i] = tcm.getColumn(i).getPreferredWidth();
        }
        
        return ret;
    }
    private void subscribe() {
        if (connected || connection == null) {
            return;
        }
        try {
            connection.addNotificationListener(objectName, this, null, objectName);
            connected = true;
            logger.info("Now Receiving Notifications from " + objectName.toString());
        } catch (Exception e) {
            logger.error("Unable to subscribe to Notification MBean " + objectName.toString());
            logger.logException(e, this);
            String error = "Exception " + e.getClass().getName() + 
                    "\nocurred , error(" + e.getMessage() + ")" + 
                    "\nAttempting to Subscribe to MBean Notification " + objectName.toString();
            logger.logException(e, this);
            JOptionPane.showMessageDialog(this, 
                    error,
                     "Subscribe Error", JOptionPane.ERROR_MESSAGE, null);
        }
    }
    public void unsubscribe() {
        if (!connected) {
            return;
        }
        try {
            connection.removeNotificationListener(objectName, this);
            connected = false;
            logger.info("Unsubscribing from Notification MBean " + objectName.toString());
        } catch (Exception e) {
            logger.error("Error Unsubscribing to Notification MBean " + objectName.toString());
            logger.logException(e, this);
        } 
    }

    public ObjectName getObjectName() {
        return objectName;
    }
    
}
class SubscriptionTable extends JTable {
    
    private static final long serialVersionUID = 1L;
    private SubscriptionCellRenderer cr;
    
    public SubscriptionTable(TableModel model) {
        super(model);
        
        cr = new SubscriptionCellRenderer();
    }
    
    public TableCellRenderer getCellRenderer(int row, int column) {

        return cr;
    }
    public TableCellEditor getCellEditor(int row, int column)
    {
        return cr;
    }
   
    
    
}

class SubscriptionCellRenderer implements TableCellRenderer, TableCellEditor {

    private Map<String, Component> compMap = new HashMap<String, Component>();
    private Font bold;
    
    
    public SubscriptionCellRenderer() {
        JTextField fld = new JTextField();
        bold = fld.getFont().deriveFont(Font.BOLD);
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

    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        Component comp = compMap.get(row + ":" + column);
        
        if (comp != null) {
            return comp;
        }
        
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
    private Component renderComponent(JTable table, final Object value, boolean isSelected, boolean hasFocus,
            int row, int column) { 
        
        if (value == null) {
            
            JTextField tf = new JTextField("<null>");
            tf.setEditable(false);
            return tf; 
        }
        
        if (value instanceof String) {
            
            JTextField tf = new JTextField(value.toString());
            tf.setEditable(false);
            if (((String) value).length() > 20) {
                registerListener(tf, value);
                tf.setForeground(Color.BLUE);
            }
            return tf; 
        }
        
        if (value instanceof Number) {
                JTextField tf = new JTextField(value.toString());
                return tf;
            
        }
        
        if (value instanceof Boolean) {
                        
                JTextField tf = new JTextField(value.toString());
                tf.setToolTipText("Click to Pop Out");
                return tf;
            
        }
        
        JTextField tf = new JTextField(value.toString());
        tf.setEditable(false);
        registerListener(tf, value);
        tf.setForeground(Color.BLUE);
        tf.setFont(bold);
        return tf; 
       
        
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
    
}
class BigString {
    private String str;
    public BigString(String str) {
        this.str = str;
    }
    
    public String toString() {
        return str;
    }
}