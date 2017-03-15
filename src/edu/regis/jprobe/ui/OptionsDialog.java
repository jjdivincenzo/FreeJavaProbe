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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * @author jdivince
 *
 */
public class OptionsDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private int frameWidth = 500;
    private int frameHeight = 600;
    private UIOptions prop;  
    
    public OptionsDialog(final JFrame owner, final UIOptions prop) {

        super(owner, true);
        this.prop = prop;
        this.setTitle("Probe User Interface Options");
                 
        setSize(frameWidth,frameHeight);
        
        try {
            
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
        } catch (Exception e1) {}       
                
        centerRelativeToParent(owner, this, frameWidth, frameHeight);
                
                    
       
        //CPU Threshold Panel
        JPanel optionsPanel = formatObject(prop);
        optionsPanel.setLayout(new GridBagLayout());
        TitledBorder tb0 = new TitledBorder( new EtchedBorder(),
                "Change a Column Value, and press Enter");
        optionsPanel.setBorder(tb0);
        
        
        
        // Panel for our action buttons
        JPanel p3 = new JPanel();
        p3.setLayout(new GridLayout(1,2,5,5));
        p3.setBorder(new EtchedBorder( EtchedBorder.LOWERED));

        // Save button and event handler
        JButton b1 = new JButton();
        b1.setText(" Save ");
        b1.setToolTipText("To Save These Options...");
        //b1.setSize(buttonWidth, buttonHeight);
        b1.setDefaultCapable(true);
        b1.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        save();
                        
                     }
                });

        p3.add(b1);

        // Cancel button and event handler
        JButton b3 = new JButton();
        b3.setText("Cancel");
        b3.setToolTipText("To Discard Any Changes...");
        //b3.setSize(buttonWidth, buttonHeight);
        b3.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                         cancel();
                     }
                });
        
        p3.add(b3);
        
        setLayout(new  GridBagLayout());
        GridBagConstraints c3 = new GridBagConstraints();
        c3.insets= new Insets(1,1,1,1);
        c3.fill = GridBagConstraints.BOTH;
        
        //CPU panel
        c3.gridx=0;
        c3.gridy=0;
        c3.gridwidth=1;
        c3.gridheight=4;
        c3.weightx=1;
        c3.weighty=1;
        add(optionsPanel,c3);
        
        //Heap panel
        c3.gridx=0;
        c3.gridy=4;
        c3.gridwidth=1;
        c3.gridheight=1;
        c3.weightx=0;
        c3.weighty=0;
        add(p3,c3);
        
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        
        
        // Window close handler
        addWindowListener( new WindowAdapter () {
            public void windowClosing (WindowEvent e) {
                                
                cancel();
            }
        });
        
        setVisible(true);
    }
    
    private void save() {
        prop.save();
        prop.notifyChangeListeners();
        this.dispose();
    }
    private void cancel() {
        this.dispose();
    }
    public JPanel formatObject(Object obj) {
        
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        
        TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Options");
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
    private JTable getObjectTable(Object obj) {
            
        
        String[] colNames = {"Option", "Value"};
        Class<?> me = obj.getClass();
        Map<String, Object> opts = new HashMap<String, Object>();
        Field[] field = me.getDeclaredFields();
       
        for (Field fld : field) {
            if (Modifier.isStatic(fld.getModifiers()) || 
                    Modifier.isTransient(fld.getModifiers())) {
                continue;
            }
            if (isValidType(fld.getType())){
                fld.setAccessible(true);
                Object value;
                try {
                    value = fld.get(obj);
                } catch (IllegalArgumentException e) {
                    value = "<Illegal Argument>";
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    value = "<Not Accessable>";
                }
                opts.put(fld.getName(), value);
            }
        }
        
        
        Object[][] data = new Object[opts.size()] [2];
        
        Set<String> keys = opts.keySet();
        List<String> list = new ArrayList<String>(keys);
        Collections.sort(list);
        //Iterator<String> iter = keys.iterator();
        int idx = 0;
        
        //while (iter.hasNext()) {
        for (String key : list)  {   
            //String key = iter.next();
            Object value = opts.get(key);
            data[idx][0] = key;
            data[idx][1] = value;
            idx++;
        }
        
       DefaultTableModel model = new DefaultTableModel(data, colNames);
        
       JTable ret = new OptionsTable(model, null, prop );
       ret.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
       ret.setColumnSelectionAllowed(true);
       ret.setCellSelectionEnabled(true);
       return ret;
    }
    private boolean isValidType(Class<?> clazz) {
        
        if (clazz == Integer.TYPE) {
            return true;
        }
        if (clazz == Long.TYPE) {
            return true;
        }
        if (clazz == Float.TYPE) {
            return true;
        }
        if (clazz == Double.TYPE) {
            return true;
        }
        if (clazz == String.class) {
            return true;
        }

        if (clazz == Boolean.TYPE) {
            return true;
        }
        
        return false;
        
    }
    public void centerRelativeToParent(Component parent, 
            Component child, 
            int frameWidth, 
            int frameHeight) {
            

      if (parent != null) {
          Point p = parent.getLocation();
          Dimension pd = parent.getSize();
          int x = p.x + ((pd.width - frameWidth) / 2);
          int y = p.y + ((pd.height - frameHeight) / 2);
          
          child.setLocation (x,  y);
      } else {
          Dimension d = new Dimension();
          d = Toolkit.getDefaultToolkit().getScreenSize();
          child.setLocation((( (int) d.getWidth() /2)  - (frameWidth / 2)),
                  (( (int) d.getHeight() / 2) - (frameHeight /2)));
      }

  }
    public static void main(String[] args) {
        new OptionsDialog(null, new UIOptions());
    }
}
