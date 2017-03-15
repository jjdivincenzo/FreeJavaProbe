package edu.regis.jprobe.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import edu.regis.jprobe.model.JMXUtils;
import edu.regis.jprobe.model.MemoryPoolData;

public class CompositeDataDialog extends JDialog {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int frameWidth = 640;
    private int frameHeight = 480;
    private int buttonWidth=30;
    private int buttonHeight=30;

    // Swing component
    private JTable compositeData;
    private JTable values;
    //private DefaultTableModel model;
    private String[] colNames = {"Name", "Value"};
    
    

    /**
     * This is the default ctor to initialize all of the swing components
     *
     */
    
    public CompositeDataDialog(Frame parent, CompositeData cds, String title) { 
                
        
        super(parent, true);
        //If they provide a parent frame, we will position this dialog in the
        //center relative to the parent, if they don't, we will just center it in 
        //the screen
        if (parent != null) {
            Point p = parent.getLocation();
            Dimension pd = parent.getSize();
            frameWidth = (int) (pd.width * .90);
            frameHeight = (int) (pd.height * .70);
            int x = p.x + ((pd.width - frameWidth) / 2);
            int y = p.y + ((pd.height - frameHeight) / 2);
            setLocation (x,  y);
        } else {
            Dimension d = new Dimension();
            d = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((( (int) d.getWidth() /2)  - (frameWidth / 2)),
                        (( (int) d.getHeight() / 2) - (frameHeight /2)));
        }
        
        setModal(true);
        setSize(frameWidth,frameHeight);
        this.setResizable(true);
                
        setTitle(title);      
        
                    
        //Our field dimensions
        //Dimension headingSize = new Dimension(250,20);
       // Dimension hugeTextSize = new Dimension(250,80);
        
                
        //The System Properties Panel
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Composite Data");
        tb1.setTitleColor(Color.BLUE);
        p2.setBorder(tb1);
        p2.setForeground(Color.BLUE);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets= new Insets(2,2,2,2);
        c2.fill = GridBagConstraints.BOTH;
 
        //System Proerties text box
        compositeData = formatInfo(cds);
        
        Font fontTF = compositeData.getFont();
        fontTF = fontTF.deriveFont(Font.BOLD);
        compositeData.setFont(fontTF);
        JScrollPane scroll = new JScrollPane(compositeData);
        //scroll.setPreferredSize(hugeTextSize);
        c2.gridx=0;
        c2.gridy=0;
        c2.gridwidth=3;
        c2.gridheight=4;
        c2.weightx=1.0;
        c2.weighty=.2;
        c2.fill = GridBagConstraints.BOTH;
        p2.add(scroll,c2);
        
        JPanel prop = new JPanel();
        prop.setBorder(new TitledBorder( new EtchedBorder(), "Values"));
        prop.setLayout(new GridLayout(1,1,1,1));
        //model = new DefaultTableModel();
        //model.setColumnIdentifiers(colNames);
        //values = new JTable(model);
        values = buildProperties(cds);
        JScrollPane scroll2 = new JScrollPane(values);
        prop.add(scroll2);
        c2.gridx=0;
        c2.gridy=4;
        c2.gridwidth=3;
        c2.gridheight=6;
        c2.weightx=1.0;
        c2.weighty=.8;
        p2.add(prop, c2);
        
        
                
        // Panel for our action buttons
        JPanel p5 = new JPanel();
        p5.setLayout(new GridLayout(1,1,5,5));
        p5.setBorder(new EtchedBorder( EtchedBorder.LOWERED));

        
        // OK button and event handler
        JButton b1 = new JButton();
        p5.add(new JLabel(" "));
        b1.setText("Close");
        b1.setSize(buttonWidth, buttonHeight);
        
        p5.add(b1);
        p5.add(new JLabel(" "));
        b1.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        closeDialog();
                     }
                });

                
        
        // Add the panels to the frame
        setLayout(new  GridBagLayout());
        GridBagConstraints c5 = new GridBagConstraints();
        c5.insets= new Insets(1,1,1,1);
        c5.fill = GridBagConstraints.BOTH;
        GridBagConstraints c4 = new GridBagConstraints();
        c4.insets= new Insets(2,2,2,2);
        c4.fill = GridBagConstraints.BOTH;
        
                
        // System info panel
        c4.gridx=0;
        c4.gridy=0;
        c4.gridwidth=1;
        c4.gridheight=5;
        c4.weightx=1;
        c4.weighty=.2;
        add(p2,c4);
            
        
        // Buttons
        c4.gridx=0;
        c4.gridy=5;
        c4.weightx=.1;
        c4.weighty=.0;
        c4.gridheight=1;
        add(p5,c4);

        //Dispose of the Dialog when we are through
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Window close handler
        addWindowListener( new WindowAdapter () {
            public void windowClosing (WindowEvent e) {
                
                closeDialog();
               
            }
        });
        
        //get the properties and show the dialog
        //compositeData.setText(formatData(cds));
        //compositeData.setCaretPosition(0);
        
        setVisible(true);
        
    }
    /**
     * This simply closes the dialog
     */
    private void closeDialog() {
        this.dispose();
    }
    
    private JTable formatInfo(CompositeData cds) {
        
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
    private JTable buildProperties(CompositeData cds) {
        
        CompositeType type = cds.getCompositeType();
        
        
        
        Set<String> keySet = type.keySet();
        List<String>keys = new ArrayList<String>();
        Iterator<String> iter = keySet.iterator();
        int i = 0;

        while (iter.hasNext()) {
             keys.add(iter.next());
       }
        
        String[][] data = new String[keys.size()][2];
        
       
        for (i=0; i < keys.size(); i++) {
            
            data[i] [0] = keys.get(i);
            Object get = cds.get(keys.get(i));
            data[i] [1] = (get == null ? "<null>" : get.toString());
            
        }
        
        DefaultTableModel model = new DefaultTableModel(data, colNames);
        
        JTable ret = new JTable(model);
        ret.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return ret;
        
    }
    public static void main(String args[]) {
        
        MemoryPoolData mpd = new MemoryPoolData();
        CompositeData csd = null;
        String[] skip = {"toString"};
        try {
             csd = JMXUtils.buildCompositeData(mpd, "Options", "My Options", skip);
        } catch (OpenDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        new CompositeDataDialog(null, csd, "Testing"); 
        
        
    }
    
}