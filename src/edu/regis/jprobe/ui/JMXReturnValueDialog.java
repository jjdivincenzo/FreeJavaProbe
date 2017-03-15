package edu.regis.jprobe.ui;

import java.awt.Container;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import edu.regis.jprobe.model.DataFormatter;
import edu.regis.jprobe.model.JMXUtils;
import edu.regis.jprobe.model.MemoryPoolData;
import edu.regis.jprobe.model.ProbeResponse;

public class JMXReturnValueDialog extends JDialog {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int frameWidth = 640;
    private int frameHeight = 480;
    private int buttonWidth=30;
    private int buttonHeight=30;
    private boolean watchable = false;

    

    /**
     * This is the default ctor to initialize all of the swing components
     *
     */
    
    public JMXReturnValueDialog(Frame parent, Object value, String title) { 
                
        
        super(parent, true);
        //If they provide a parent frame, we will position this dialog in the
        //center relative to the parent, if they don't, we will just center it in 
        //the screen
       
        
        setModal(true);
        
        this.setResizable(true);
        
        String className = "";
        
        if (value != null) {
            className = " - Type(" + value.getClass().getName() + ")";
        }
        
        setTitle(title + className);      
        
                    
        //Our field dimensions
        //Dimension headingSize = new Dimension(250,20);
        //Dimension hugeTextSize = new Dimension(250,80);
        
                
        //The System Properties Panel
        Container p2 = formatReturnedData(value);
        
        
        
                
        // Panel for our action buttons
        JPanel p5 = new JPanel();
        p5.setLayout(new GridLayout(1,1,5,5));
        p5.setBorder(new EtchedBorder( EtchedBorder.LOWERED));

        
        // OK button and event handler
        JButton b1 = new JButton();
        p5.add(new JLabel(" "));
        b1.setText("Close");
        b1.setToolTipText("Close This Window");
        b1.setSize(buttonWidth, buttonHeight);
        
        p5.add(b1);
        
        JButton b2 = new JButton("Watch?");
        if (value instanceof Number) {
            b2.setToolTipText("Launch A Watch From This Invokation");
            b2.setSize(buttonWidth, buttonHeight);
            p5.add(b2);
            
        }
        p5.add(new JLabel(" "));
        b1.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        watchable = false;
                        closeDialog();
                     }
                });
        b2.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        watchable = true;
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
        if (parent != null) {
            Point p = parent.getLocation();
            Dimension pd = parent.getSize();
            //frameWidth = (int) (pd.width * .90);
            //frameHeight = (int) (pd.height * .70);
            int x = p.x + ((pd.width - frameWidth) / 2);
            int y = p.y + ((pd.height - frameHeight) / 2);
            setLocation (x,  y);
        } else {
            Dimension d = new Dimension();
            d = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((( (int) d.getWidth() /2)  - (frameWidth / 2)),
                        (( (int) d.getHeight() / 2) - (frameHeight /2)));
        }
        setSize(frameWidth,frameHeight);
        setVisible(true);
        
    }
    /**
     * This simply closes the dialog
     */
    private void closeDialog() {
        this.dispose();
    }
    
   
    private Container formatReturnedData(Object data) {
        
        if (data == null) {
            
            JPanel p =  formatSimpleObject("<null>");
            frameHeight = 100;
            frameWidth = 300;
            return p;
        }
        
        if (data instanceof String || 
            data instanceof Number ||
            data instanceof Boolean ||
            data instanceof Character ||
            data instanceof Enum ) {
            return formatSimpleObject(data);
        }
            
        if (data instanceof CompositeData) {
            return formatCompositeData((CompositeData)data);
        }
        if (data instanceof TabularData) {
            return formatTabularData((TabularData)data);
        }
        
        if (data instanceof Map) {
            return formatMap((Map<?,?>)data);
        }
        if (data instanceof Collection) {
            return formatCollection((Collection<?>)data);
        }
        
        
        return formatObject(data);
    }
    
    private JPanel formatSimpleObject(Object obj) {
        
                
        JPanel p = new JPanel();
        p.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
        p.setLayout(new GridLayout(1,1,1,1));
        JTextArea tf = new JTextArea(obj.toString());
        tf.setEditable(false);
        
        Font fontTF = tf.getFont();
        
        if (obj instanceof String) {
            if (((String)obj).length() < 500) {
                frameHeight = 300;
                frameWidth = 300;
            } else {
                frameHeight = 800;
                frameWidth = 600;
            }
            fontTF = fontTF.deriveFont(Font.BOLD);
        } else {
            frameHeight = 150;
            frameWidth = 350;
            fontTF = fontTF.deriveFont(Font.BOLD, 18);
        }
        
        
        tf.setFont(fontTF);
        JScrollPane scroll = new JScrollPane(tf);
        p.add(scroll);
        
        return p;
        
    }
    
    private Container formatCompositeData(CompositeData cds) {
        return DataFormatter.formatCompositeData(cds);
    }
    private Container formatTabularData(TabularData tds) {
        return DataFormatter.formatTabularData(tds);
    }
    private Container formatObject(Object obj) {
        return DataFormatter.formatObject(obj);
    }
    private Container formatMap(Map<?,?> map) {
        return DataFormatter.formatMap(map);
    }
    private Container formatCollection(Collection<?> col) {
        return DataFormatter.formatCollection(col);
    }
    public boolean isWatchable() {
        return watchable;
    }
    public static void main(String args[]) {
        
        Map<String, String> testMap = new HashMap<String, String>();
        Collection<String> testCol = new ArrayList<String>();
        
        for (int i = 0; i < 20; i++) {
            testMap.put("[" + i + "]", "Value of " + i);
            testCol.add("String" + i);
        }
        
        MemoryPoolData mpd = new MemoryPoolData();
        CompositeData csd = null;
        TabularData tds = null;
        String[] skip = {"toString"};
        try {
             csd = JMXUtils.buildCompositeData(mpd, "Options", "My Options", skip);
             tds = JMXUtils.buildTabularData(testMap, "OptionsMap", "My Options Map ", skip);
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
        new JMXReturnValueDialog(null, csd, "Testing CDS"); 
        
        String msg = "This is\na test of\nthe emergency\nbroadcast\nsystem";
        
        new JMXReturnValueDialog(null, msg, "Testing String");
        
        Boolean bol = new Boolean(false);
        
        new JMXReturnValueDialog(null, bol, "Testing Boolean");
        
        Double d = new Double("3.14159263");
        
        new JMXReturnValueDialog(null, d, "Testing Double");
        
        new JMXReturnValueDialog(null, new ProbeResponse(), "Testing Object");
        
        new JMXReturnValueDialog(null, tds, "Testing TDS");
        //new JMXReturnValueDialog(null, null, "Testing Null");
       //new JMXReturnValueDialog(null, testMap, "Testing Map");
        //new JMXReturnValueDialog(null, testCol, "Testing Collection");
        
    }
}