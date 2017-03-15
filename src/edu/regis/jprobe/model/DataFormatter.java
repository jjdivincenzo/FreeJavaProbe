package edu.regis.jprobe.model;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import edu.regis.jprobe.ui.AttributeTable;

public class DataFormatter {
    
    private static final Dimension LARGE_SIZE = new Dimension(200,150);
    private static final Dimension MEDIUM_SIZE = new Dimension(200,80);
    
    public static Container format(Object value) {
        
        if (value instanceof String) {
            
            return formatString((String)value) ;

        }
        if (value instanceof TabularData) {
            
            return formatTabularData((TabularData)value) ;

       }
       if (value instanceof CompositeData) {
           
           return formatCompositeData((CompositeData)value) ;
   
       }
       if (value instanceof Map) {
           
           return formatMap((Map<?,?>)value) ;
           
       }
       if (value instanceof Collection) {
           
           return formatCollection((Collection<?>)value) ;
 
       }

           
       return formatObject(value) ;
        
    }
    
    public static Container formatCompositeData(CompositeData cds) {
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Composite Data");
        tb1.setTitleColor(Color.BLUE);
        p2.setBorder(tb1);
        p2.setForeground(Color.BLUE);
        
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets= new Insets(1,1,1,1);
        c2.fill = GridBagConstraints.BOTH;
        c2.ipady = 0;
        JTable compositeData = formatInfo(cds);
        
        Font fontTF = compositeData.getFont();
        fontTF = fontTF.deriveFont(Font.BOLD);
        compositeData.setFont(fontTF);
        JScrollPane scroll = new JScrollPane(compositeData);
        scroll.setWheelScrollingEnabled(true);
        scroll.setPreferredSize(MEDIUM_SIZE);
        c2.gridx=0;
        c2.gridy=0;
        c2.gridwidth=1;
        c2.gridheight=1;
        c2.weightx=1.0;
        c2.weighty=.1;
        c2.fill = GridBagConstraints.BOTH;
        p2.add(scroll,c2);
        
        JTable values = buildProperties(cds);
        JScrollPane scroll2 = new JScrollPane(values);
        scroll2.setWheelScrollingEnabled(true);
        scroll2.setPreferredSize(LARGE_SIZE);
        c2.gridx=0;
        c2.gridy=1;
        c2.gridwidth=1;
        c2.gridheight=4;
        c2.weightx=1.0;
        c2.weighty=.9;
        p2.add(scroll2, c2);
       
        return p2;       
         
        
    }
    private static JTable formatInfo(CompositeData cds) {
        
        String[] colNames = {"Property", "Value"};
        String[][] data = new String[3] [2];
        
        CompositeType type = cds.getCompositeType();
        data [0][0] = "Description";
        data [0][1] = type.getDescription();
        
        data [1][0] = "Class Name";
        data [1][1] = type.getClassName();
        
        data [2][0] = "Type Name";
        data [2][1] = type.getTypeName();
        
        
        DefaultTableModel model = new DefaultTableModel(data, colNames);
        
        JTable ret = new JTable(model);
        
        ret.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return ret;
 
        
        
    }
    private static JTable formatInfo(TabularData tds) {
        
        String[] colNames = {"Property", "Value"};
        String[][] data = new String[3] [2];
        
        TabularType type = tds.getTabularType();
        data [0][0] = "Description";
        data [0][1] = type.getDescription();
        
        data [1][0] = "Class Name";
        data [1][1] = type.getClassName();
        
        data [2][0] = "Type Name";
        data [2][1] = type.getTypeName();
        
        
        DefaultTableModel model = new DefaultTableModel(data, colNames);
        
        JTable ret = new JTable(model);
        ret.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return ret;
 
        
        
    }
    private static JTable buildProperties(CompositeData cds) {
        
        CompositeType type = cds.getCompositeType();
        String[] colNames = {"Property", "Value"};
        
        
        Set<String> keySet = type.keySet();
        List<String>keys = new ArrayList<String>();
        Iterator<String> iter = keySet.iterator();
        int i = 0;

        while (iter.hasNext()) {
             keys.add(iter.next());
       }
        
        Object[][] data = new Object[keys.size()][2];
        
       
        for (i=0; i < keys.size(); i++) {
            
            data[i] [0] = keys.get(i);
            data[i] [1] = cds.get(keys.get(i));//(get == null ? "<null>" : get.toString());
            
        }
        
        DefaultTableModel model = new DefaultTableModel(data, colNames);
        
        JTable ret = new AttributeTable(model);
        ret.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ret.setColumnSelectionAllowed(true);
        ret.setCellSelectionEnabled(true);
        
       
        return ret;
        
    }
    private static JTable buildProperties(TabularData tds) {
        
        String[] colNames = {"Property"};
        List<Object> values = new ArrayList<Object>();
        Collection<?> valueCollection = tds.values();
        Iterator<?> iterVal = valueCollection.iterator();
        int i = 0;

        while (iterVal.hasNext()) {
            values.add(iterVal.next());
        }
        int len = values.size();
        Object[][] data = new Object[len][1];
        
       
        for (i=0; i < len; i++) {
            data[i] [0] = values.get(i);
        }
        
        DefaultTableModel model = new DefaultTableModel(data, colNames);
        
        JTable ret = new AttributeTable(model);
        ret.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ret.setColumnSelectionAllowed(true);
        ret.setCellSelectionEnabled(true);
        return ret;
        
    }
    public static Container formatObject(Object obj) {
        
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        String name = obj.getClass().getName();
        TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Object Data Class(" + name + ")");
        tb1.setTitleColor(Color.BLUE);
        p2.setBorder(tb1);
        p2.setForeground(Color.BLUE);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets= new Insets(2,2,2,2);
        c2.fill = GridBagConstraints.BOTH;
 
        JTable compositeData = getObjectTable(obj);
        
        Font fontTF = compositeData.getFont();
        fontTF = fontTF.deriveFont(Font.BOLD);
        compositeData.setFont(fontTF);
        JScrollPane scroll = new JScrollPane(compositeData);
        scroll.setWheelScrollingEnabled(true);
        c2.gridx=0;
        c2.gridy=0;
        c2.gridwidth=3;
        c2.gridheight=4;
        c2.weightx=1.0;
        c2.weighty=1;
        c2.fill = GridBagConstraints.BOTH;
        p2.add(scroll,c2);

        return p2; 
    }
    public static Container formatMap(Map<?,?> map) {
        
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Map Elements[" + map.size() + "]");
        tb1.setTitleColor(Color.BLUE);
        p2.setBorder(tb1);
        p2.setForeground(Color.BLUE);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets= new Insets(2,2,2,2);
        c2.fill = GridBagConstraints.BOTH;
 
         JTable compositeData = getMapTable(map);
        
        Font fontTF = compositeData.getFont();
        fontTF = fontTF.deriveFont(Font.BOLD);
        compositeData.setFont(fontTF);
        JScrollPane scroll = new JScrollPane(compositeData);
        scroll.setWheelScrollingEnabled(true);
        c2.gridx=0;
        c2.gridy=0;
        c2.gridwidth=3;
        c2.gridheight=4;
        c2.weightx=1.0;
        c2.weighty=1;
        c2.fill = GridBagConstraints.BOTH;
        p2.add(scroll,c2);
        p2.setToolTipText("Right Click To Pop This Out To A Dialog");
        
        
        return p2;   
    }
    public static Container formatString(String data) {
        
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Text Data");
        tb1.setTitleColor(Color.BLUE);
        p2.setBorder(tb1);
        p2.setForeground(Color.BLUE);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets= new Insets(2,2,2,2);
        c2.fill = GridBagConstraints.BOTH;
 
        JTextArea ta = new JTextArea(data);
        
        Font fontTF = ta.getFont();
        fontTF = fontTF.deriveFont(Font.BOLD);
        ta.setFont(fontTF);
        JScrollPane scroll = new JScrollPane(ta);
        scroll.setWheelScrollingEnabled(true);
        c2.gridx=0;
        c2.gridy=0;
        c2.gridwidth=3;
        c2.gridheight=4;
        c2.weightx=1.0;
        c2.weighty=1;
        c2.fill = GridBagConstraints.BOTH;
        p2.add(scroll,c2);
        p2.setToolTipText("Right Click To Pop This Out To A Dialog");
        
        
        return p2;   
    }
    public static Container formatCollection(Collection<?> col) {
        
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Collection Elements[" + col.size() + "]");
        tb1.setTitleColor(Color.BLUE);
        p2.setBorder(tb1);
        p2.setForeground(Color.BLUE);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets= new Insets(2,2,2,2);
        c2.fill = GridBagConstraints.BOTH;
 
        //System Proerties text box
        JTable compositeData = getCollectionTable(col);
        
        Font fontTF = compositeData.getFont();
        fontTF = fontTF.deriveFont(Font.BOLD);
        compositeData.setFont(fontTF);
        JScrollPane scroll = new JScrollPane(compositeData);
        scroll.setWheelScrollingEnabled(true);
        c2.gridx=0;
        c2.gridy=0;
        c2.gridwidth=3;
        c2.gridheight=4;
        c2.weightx=1.0;
        c2.weighty=1;
        c2.fill = GridBagConstraints.BOTH;
        p2.add(scroll,c2);
        
        return p2;   
    }
        
    private static JTable getObjectTable(Object obj) {
            
        
        String[] colNames = {"Field Name", "Value"};
        Class<?> me = obj.getClass();
        
        List<Field> allFields = getAllFields(me);
        Collections.sort(allFields, new FieldComparator());
        Field[] field = new Field[allFields.size()];
        int idx = 0;
        for (Field f : allFields) {
            field[idx++] = f;
        }
        
        int len = field.length;
        Object[][] data = new Object[len] [2];
        
        for (int i = 0; i < len; i++) {
            
            data[i][0] = field[i].getName();
            
            try {
                field[i].setAccessible(true);
                Object f = field[i].get(obj);
                if (field[i].getType().isArray()) {
                    if (f == null) {
                        data[i][1] =f;//"<null>";
                    } else {
                        int alen = Array.getLength(f);
                        StringBuilder sb = new StringBuilder();
                        sb.append("{ ");
                        for (int i2 = 0; i2 < alen; i2++) {
                            Object occ = Array.get(f, i2);
                            sb.append((occ == null ? "<null>" : occ.toString()));
                            if (i2 < alen -1) {
                                sb.append(", "); 
                            }
                        }
                        sb.append("}");
                        data[i][1] = sb.toString();
                    }
                    
                } else {
                    data[i][1] = f;//(f == null ? "<null>" : f.toString());
                }
            } catch (Exception e) {
                StringBuilder sb = new StringBuilder();
            
                sb.append("Exception Getting Object, Error is ");
                sb.append(e.getMessage());
                sb.append(", Exception is ");
                sb.append(e.getClass().getName());
                data[i][1] = sb.toString();
            } 
            
            
        }
        
        DefaultTableModel model = new DefaultTableModel(data, colNames);
        
         JTable ret = new AttributeTable(model);
        ret.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ret.setColumnSelectionAllowed(true);
        ret.setCellSelectionEnabled(true);
        return ret;
   }
    private static List<Field> getAllFields(Class<?> clazz) {
        
        List<Field> ret = new ArrayList<Field>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            ret.add(field);
        }
        Class<?> parent = clazz.getSuperclass();
        if (parent != null) {
            List<Field> parents = getAllFields(parent);
            ret.addAll(parents);
        }
        
        return ret;
    }
    private static JTable getMapTable(Map<?,?> map) {
            
        
        String[] colNames = {"Name", "Value"};
        
       
        
        int len = map.size();
        Object[][] data = new Object[len] [2];
        Set<?> keys = map.keySet();
        Iterator<?> iter = keys.iterator();
        int i = 0;
        
        while (iter.hasNext()) {
            Object key = iter.next();
            data[i] [0] = key;//(key == null ? "<null>" : key.toString());
            data[i] [1] = map.get(key);//(val == null ? "<null>" : val.toString());
            i++;
        }

        
        DefaultTableModel model = new DefaultTableModel(data, colNames);
        
        JTable ret = new AttributeTable(model);
        ret.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ret.setColumnSelectionAllowed(true);
        ret.setCellSelectionEnabled(true);
        return ret;
   }
    private static JTable getCollectionTable(Collection<?> col) {
            
        
        String[] colNames = {"Value"};
        
       
        
        int len = col.size();
        Object[][] data = new Object[len] [1];
        
        Iterator<?> iter = col.iterator();
        int i = 0;
        
        while (iter.hasNext()) {
            Object val = iter.next();
            data[i] [0] = val;//(val == null ? "<null>" : val.toString());
            i++;
        }

        
        DefaultTableModel model = new DefaultTableModel(data, colNames);
        
        JTable ret = new AttributeTable(model);
        ret.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        ret.setColumnSelectionAllowed(true);
        ret.setCellSelectionEnabled(true);
        return ret;
   }
    public static Container formatTabularData(TabularData tds) {
        
        JPanel p2 = new JPanel();
        
        p2.setLayout(new GridBagLayout());
        TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Tabular Data");
        tb1.setTitleColor(Color.BLUE);
        p2.setBorder(tb1);
        p2.setForeground(Color.BLUE);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets= new Insets(2,2,2,2);
        c2.fill = GridBagConstraints.BOTH;
 
        //System Proerties text box
        JTable compositeData = formatInfo(tds);
        
        Font fontTF = compositeData.getFont();
        fontTF = fontTF.deriveFont(Font.BOLD);
        compositeData.setFont(fontTF);
        JScrollPane scroll = new JScrollPane(compositeData);
        scroll.setWheelScrollingEnabled(true);
        scroll.setPreferredSize(MEDIUM_SIZE);
        c2.gridx=0;
        c2.gridy=0;
        c2.gridwidth=1;
        c2.gridheight=1;
        c2.weightx=1.0;
        c2.weighty=.3;
        c2.fill = GridBagConstraints.BOTH;
        p2.add(scroll,c2);
        
        JPanel prop = new JPanel();
        TitledBorder tb2 = new TitledBorder( new EtchedBorder(), "Values");
        tb2.setTitleColor(Color.BLUE);
        prop.setBorder(tb2);
        prop.setLayout(new GridLayout(1,1,1,1));
        JTable values = buildProperties(tds);
        JScrollPane scroll2 = new JScrollPane(values);
        scroll2.setWheelScrollingEnabled(true);
        scroll.setPreferredSize(LARGE_SIZE);
        prop.add(scroll2);
        c2.gridx=0;
        c2.gridy=4;
        c2.gridwidth=1;
        c2.gridheight=values.getRowCount();
        c2.weightx=1.0;
        c2.weighty=.7;
        p2.add(scroll2, c2);
         return p2;     

        
    }
    

}
class FieldComparator implements Comparator<Field> {

    
    @Override
    public int compare(Field f1, Field f2) {
        
        
        return f1.getName().compareTo(f2.getName());
    }
    
}