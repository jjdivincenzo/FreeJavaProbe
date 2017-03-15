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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.Utilities;



/**
 * @author jdivince
 *
 */
public class WatchFrame extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private SingleTimeGraph graph;
    private int frameWidth = 800;
    private int frameHeight = 600;
    private JMXBeanPanel comm;
    private JFrame parent;
    private ObjectName objectName;
    private String attrName;
    private String methodName;
    private  String[] parmSignatures;
    private Object[] parameters;
    private Timer timer;
    private JPanel mainPanel;
    private double lastObs = Double.MIN_VALUE;
    private JTextField lastValue;
    private JTextField thisValue;
    private JTextField deltaValue;
    private JButton close;
    private JButton closeAll;
    private JButton pause;
    private JButton dock;
    private JMenuItem pauseResumeItem;
    private int updateInterval = ONE_SEC;
    private static int offset = 0;
    private boolean docked = false;
    private boolean attributeMode = false;
    
    private JCheckBoxMenuItem graphDeltasItem;
    private JCheckBoxMenuItem graphValuesItem;
    
    private JCheckBoxMenuItem tenSecItem;
    private JCheckBoxMenuItem fiveSecItem;
    private JCheckBoxMenuItem threeSecItem;
    private JCheckBoxMenuItem oneSecItem;
    private JCheckBoxMenuItem halfSecItem;
    private JCheckBoxMenuItem qtrSecItem;
    
    
    private JCheckBoxMenuItem[] colorItems;
    private Color selectedColor = Color.red;
    
    private Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA,
            Color.ORANGE, Color.PINK, Color.WHITE
    };
    private String[] colorNames = {"Red", "Blue", "Green", "Yellow", "Magenta",
            "Orange", "Pink", "White"
    };
    
    private static List<WatchFrame> openDialogs = new ArrayList<WatchFrame>();
    
    private static final int  TEN_SEC = 10000;
    private static final int  FIVE_SEC = 5000;
    private static final int  THREE_SEC = 3000;
    private static final int  ONE_SEC = 1000;
    private static final int  HALF_SEC = 500;
    private static final int  QUARTER_SEC = 250;
    
    public WatchFrame(JFrame parent, JMXBeanPanel comm, ObjectName objectName, String methodName, 
            String[] parmSignatures, Object[] parameters) {
        
        this.parent = parent;
        this.comm = comm;
        this.objectName = objectName;
        this.methodName = methodName;
        this.parmSignatures = parmSignatures;
        this.parameters = parameters;
        attributeMode = false;
        init();
    }
    
    public WatchFrame(JFrame parent, JMXBeanPanel comm, ObjectName objectName, String attrName) {
        
        this.parent = parent;
        this.comm = comm;
        this.objectName = objectName;
        this.attrName = attrName;
        attributeMode = true;
        init();
    }
    
   private void init() {     
        int sampleRate = (UIOptions.getOptions() == null ? 100 : UIOptions.getOptions().getSampleRate());
        if (comm != null) {
            if (attributeMode) {
                setTitle("Graph of Attribute from (" + objectName.toString() + ")[" + comm.getConnectionName() +"]");
            } else {
                setTitle("Graph of Method from (" + objectName.toString() + ")[" + comm.getConnectionName() +"]");
            }
        } else {
            setTitle("Graph of Object(" + objectName.toString() + ")");
        }
        ImageIcon icon = IconManager.getIconManager().getMainIcon();    
        if (icon != null) setIconImage(icon.getImage());
        
        mainPanel = new JPanel();
        
        if (attributeMode) {
            graph = new SingleTimeGraph(sampleRate, attrName, "Value", attrName, Color.red);
        } else {
            String title = methodName.replace("get", "");
            graph = new SingleTimeGraph(sampleRate, methodName, "Value", title, Color.red);
        }
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();
        cc.insets= new Insets(1,1,1,1);
        cc.fill = GridBagConstraints.BOTH;
        cc.gridx=0;
        cc.gridy=0;
        cc.gridwidth=1;
        cc.gridheight=5;
        cc.weightx = 1;
        cc.weighty = 1;
        mainPanel.add(graph, cc);
        int pos = 6;
        
        if (!attributeMode) {
            JTextField sig = new JTextField();
            
            StringBuilder sb = new StringBuilder();
            sb.append("Current Value = ");
            sb.append(methodName);
            sb.append("( ");
            
            for (int i = 0; i < parameters.length; i++) {
                sb.append(parameters[i].toString());
                if (i < parameters.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(" )");
            
            sig.setText(sb.toString());
            sig.setEditable(false);
            Font bold = sig.getFont().deriveFont(Font.BOLD);
            sig.setFont(bold);
            cc.gridx=0;
            cc.gridy=pos++;
            cc.gridwidth=1;
            cc.gridheight=1;
            cc.weightx = 0;
            cc.weighty = 0;
            mainPanel.add(sig, cc);
        }
        JPanel valuePanel = new JPanel();
        valuePanel.setBorder(LineBorder.createBlackLineBorder());
        valuePanel.setLayout(new GridLayout(1,6,1,1));
        JLabel l1 = new JLabel("Last Value");
        l1.setHorizontalAlignment(JLabel.RIGHT);
        valuePanel.add(l1);
        lastValue = new JTextField("N/A");
        lastValue.setEditable(false);
        Font bold = lastValue.getFont().deriveFont(Font.BOLD);
        lastValue.setFont(bold);
        lastValue.setBackground(Color.black);
        lastValue.setForeground(Color.WHITE);
        valuePanel.add(lastValue);
        JLabel l2 =new JLabel("Current Value");
        l2.setHorizontalAlignment(JLabel.RIGHT);
        valuePanel.add(l2);
        thisValue = new JTextField("N/A");
        thisValue.setEditable(false);
        thisValue.setFont(bold);
        thisValue.setBackground(Color.black);
        thisValue.setForeground(Color.WHITE);
        valuePanel.add(thisValue);
        JLabel l3 =new JLabel("Delta");
        l3.setHorizontalAlignment(JLabel.RIGHT);
        valuePanel.add(l3);
        deltaValue = new JTextField("N/A");
        deltaValue.setEditable(false);
        deltaValue.setForeground(Color.RED);
        deltaValue.setFont(bold);
        deltaValue.setBackground(Color.black);
        
        valuePanel.add(deltaValue);
        cc.gridx=0;
        cc.gridy=pos++;
        cc.gridwidth=1;
        cc.gridheight=1;
        cc.weightx = 0;
        cc.weighty = 0;
        mainPanel.add(valuePanel, cc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1,3,1,1));
        buttonPanel.add(new JLabel());
        pause = new JButton("Pause");
        pause.setToolTipText("Pause/Resume Updates");
        buttonPanel.add(pause);
        close = new JButton("Close");
        close.setToolTipText("Close This Watch Window");
        buttonPanel.add(close);
        closeAll = new JButton("Close All");
        closeAll.setToolTipText("Close All of the Open Watch Windows");
        buttonPanel.add(closeAll);
        dock = new JButton("Dock");
        dock.setToolTipText("Dock This Window to the Client Tabbed Frame");
        buttonPanel.add(dock);
        buttonPanel.add(new JLabel());
        cc.gridx=0;
        cc.gridy=pos++;
        cc.gridwidth=1;
        cc.gridheight=1;
        cc.weightx = 0;
        cc.weighty = 0;
        mainPanel.add(buttonPanel, cc);
        
        close.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        closeDialog();
                     }
                });
        
        closeAll.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        closeAllDialogs();
                     }
                });
        dock.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        if (docked) {
                            undock();
                        } else {
                            dock();
                        }
                     }
                });
        
        pause.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        pauseResume();
                     }
                });
        
        if (parent != null) {
            Point p = parent.getLocation();
            Dimension pd = parent.getSize();
            frameWidth = (int) (pd.width * .5);
            frameHeight = (int) (pd.height * .5);
            int x = p.x + ((pd.width - frameWidth) / 2);
            int y = p.y + ((pd.height - frameHeight) / 2);
            setLocation (x + offset,  y + offset);
            offset += 20;
        } else {
            Dimension d = new Dimension();
            d = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((( (int) d.getWidth() /2)  - (frameWidth / 2)),
                        (( (int) d.getHeight() / 2) - (frameHeight /2)));
        }
        setSize(frameWidth,frameHeight);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Window close handler
        addWindowListener( new WindowAdapter () {
            public void windowClosing (WindowEvent e) {
                
                closeDialog();
               
            }
        });
        ActionListener timerListener = new ActionListener(){
        
            public void actionPerformed (ActionEvent e)
            {
                update();
             }
        };
        timer = new Timer(updateInterval, timerListener);
        add(mainPanel);
        createMenu();
        setVisible(true);
        timer.start();
        update();
        openDialogs.add(this);
        
    }
    private void closeDialog() {
        
        timer.stop();
        
        if (docked) {
            comm.getClient().undock(mainPanel); 
        } else {
            openDialogs.remove(this);
            this.dispose();
        }
        
    }
    private void pauseResume() {
        
        if (timer.isRunning()) {
            timer.stop();
            pause.setText("Resume");
            pauseResumeItem.setText("Resume");
        } else {
            timer.start();
            pause.setText("Pause");
            pauseResumeItem.setText("Pause");
        }
    }
    
    private void dock() {
        
        if (attributeMode) {
            comm.getClient().dock(mainPanel, attrName);
        } else {
            comm.getClient().dock(mainPanel, methodName);
        }
        openDialogs.remove(this);
        dock.setText("Un Dock");
        closeAll.setEnabled(false);
        docked = true;
        this.dispose();
        
    }
    private void undock() {
        
        comm.getClient().undock(mainPanel);
        add(mainPanel);
        openDialogs.add(this);
        dock.setText("Dock");
        closeAll.setEnabled(true);
        docked = false;
        setVisible(true);
       
    }
    private void closeAllDialogs() {
        
        WatchFrame[] frames = new WatchFrame[openDialogs.size()];
        int idx = 0;
        for (WatchFrame frame : openDialogs) {
            frames[idx++] = frame;
           
        }
        for (WatchFrame frame : frames) {
            
            frame.closeDialog();
        }
        openDialogs.clear();
        offset = 0;
    }
    private void setUpdateInterval(int interval) {
        updateInterval = interval;
        timer.setDelay(updateInterval);
    }
    private void update() {
        
        if (parent == null) {
            return;
        }
        
        Object val = null;
        
        if (attributeMode) {
            try {
                val = comm.getAttribute(objectName, attrName);
            } catch (Exception e) {
                String error = "Exception " + e.getClass().getName() + 
                        "\nocurred , error is " + e.getMessage() + 
                        "\nGetting Attribute " +attrName;
                Logger.getLogger().logException(e, this);
                JOptionPane.showMessageDialog(this, 
                        error,
                         "Invokation Error", JOptionPane.ERROR_MESSAGE, null);
                pauseResume();
                return;
            } 
        } else {
            try {
                val = comm.invoke(objectName, methodName, parameters, parmSignatures);
            } catch (Exception e) {
                String error = "Exception " + e.getClass().getName() + 
                        "\nocurred , error is " + e.getMessage() + 
                        "\nInvoking Method " + methodName;
                Logger.getLogger().logException(e, this);
                JOptionPane.showMessageDialog(this, 
                        error,
                         "Invokation Error", JOptionPane.ERROR_MESSAGE, null);
                pauseResume();
                return;
            } 
        }
        
        if (val == null) {
            pauseResume();
            return; 
        }
        
        if (val instanceof Number) {
            boolean isDecimal = false;
            if (val instanceof Double ||
                    val instanceof Float ||
                    val instanceof BigDecimal) {
                isDecimal = true;
            }
            double thisObs = ((Number)val).doubleValue();
            if (lastObs == Double.MIN_VALUE) {
                lastObs = thisObs;
                return;
            }
            
            double delta = thisObs - lastObs;
            
            if (graphDeltasItem.isSelected()) {
                graph.addTotalObservation(delta, System.currentTimeMillis());
            } else {
                graph.addTotalObservation(thisObs, System.currentTimeMillis());
            }
            
            lastValue.setText(format(lastObs, isDecimal));
            thisValue.setText(format(thisObs, isDecimal));
            deltaValue.setText(format(delta, isDecimal));
            lastObs = thisObs;
        }
    }
    private String format(double value, boolean isDecimal) {
        
        if (!isDecimal) {
            return Utilities.format((long)value);
            
        }
        return Utilities.format(value, 9);
    }
    private void createMenu() {
        
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
       
        JMenuItem exitItem = new JMenuItem("Close");
        exitItem.setMnemonic('C');
        exitItem.setToolTipText("To Close...");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        
        
        exitItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        closeDialog();
                    }
                }
                );
       
        fileMenu.add(exitItem);
        
        JMenuItem exitAllItem = new JMenuItem("Close All");
        exitAllItem.setMnemonic('A');
        exitAllItem.setToolTipText("To Close All Open Watch Frames...");
        exitAllItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        
        
        exitAllItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        closeAllDialogs();
                    }
                }
                );
        
        fileMenu.add(exitAllItem);
        
        pauseResumeItem = new JMenuItem("Pause");
        pauseResumeItem.setMnemonic('P');
        pauseResumeItem.setToolTipText("To Pause or Resume Updating...");
        pauseResumeItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        
        
        pauseResumeItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        pauseResume();
                    }
                }
                );
        
        fileMenu.add(pauseResumeItem);
        
        JMenuItem dockItem = new JMenuItem("Dock");
        dockItem.setMnemonic('D');
        dockItem.setToolTipText("To Dock it to the Client Tabbed Pane...");
        dockItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        
        
        dockItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        dock();
                    }
                }
                );
        fileMenu.addSeparator();
        fileMenu.add(dockItem);
        
        
        JMenu optionMenu = new JMenu("Options");
        optionMenu.setMnemonic('o');
       
        JMenu autoUpdateIntervalItem = new JMenu("Update Interval...");
        autoUpdateIntervalItem.setMnemonic('i');
        autoUpdateIntervalItem.setEnabled(true);
        autoUpdateIntervalItem.setToolTipText("Select the Refresh Speed");
        optionMenu.add(autoUpdateIntervalItem);

        tenSecItem = new JCheckBoxMenuItem("10 seconds");
        tenSecItem.setMnemonic('a');
        tenSecItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        tenSecItem.setEnabled(true);
        if (updateInterval == TEN_SEC) {
            tenSecItem.setSelected(true);
        } else {
            tenSecItem.setSelected(false);
        }
        autoUpdateIntervalItem.add(tenSecItem);
        
        tenSecItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        setUpdateInterval(TEN_SEC);
                        fiveSecItem.setSelected(false);
                        threeSecItem.setSelected(false);
                        oneSecItem.setSelected(false);
                        halfSecItem.setSelected(false);
                        qtrSecItem.setSelected(false);
                    }
                }
                );
        
        fiveSecItem = new JCheckBoxMenuItem("5 seconds");
        fiveSecItem.setMnemonic('5');
        fiveSecItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_5, ActionEvent.CTRL_MASK));
        fiveSecItem.setEnabled(true);
        
        if (updateInterval == FIVE_SEC) {
            fiveSecItem.setSelected(true);
        } else {
            fiveSecItem.setSelected(false);
        }
        
        autoUpdateIntervalItem.add(fiveSecItem);
        
        fiveSecItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        setUpdateInterval(FIVE_SEC);
                        tenSecItem.setSelected(false);
                        threeSecItem.setSelected(false);
                        oneSecItem.setSelected(false);
                        halfSecItem.setSelected(false);
                        qtrSecItem.setSelected(false);
                    }
                }
                );
        
        threeSecItem = new JCheckBoxMenuItem("3 seconds");
        threeSecItem.setMnemonic('3');
        threeSecItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_3, ActionEvent.CTRL_MASK));
        threeSecItem.setEnabled(true);
        if (updateInterval == THREE_SEC) {
            threeSecItem.setSelected(true);
        } else {
            threeSecItem.setSelected(false);
        }
        autoUpdateIntervalItem.add(threeSecItem);
        
        threeSecItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        setUpdateInterval(THREE_SEC);
                        fiveSecItem.setSelected(false);
                        tenSecItem.setSelected(false);
                        oneSecItem.setSelected(false);
                        halfSecItem.setSelected(false);
                        qtrSecItem.setSelected(false);
                    }
                }
                );
        
        oneSecItem = new JCheckBoxMenuItem("1 second");
        oneSecItem.setMnemonic('1');
        oneSecItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_1, ActionEvent.CTRL_MASK));
        oneSecItem.setEnabled(true);
        if (updateInterval == ONE_SEC) {
            oneSecItem.setSelected(true);
        } else {
            oneSecItem.setSelected(false);
        }
        autoUpdateIntervalItem.add(oneSecItem);
        
        oneSecItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        setUpdateInterval(ONE_SEC);
                        fiveSecItem.setSelected(false);
                        threeSecItem.setSelected(false);
                        tenSecItem.setSelected(false);
                        halfSecItem.setSelected(false);
                        qtrSecItem.setSelected(false);
                    }
                }
                );
        halfSecItem = new JCheckBoxMenuItem(".5 second");
        halfSecItem.setMnemonic('h');
        halfSecItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        halfSecItem.setEnabled(true);
        if (updateInterval == HALF_SEC) {
            halfSecItem.setSelected(true);
        } else {
            halfSecItem.setSelected(false);
        }
        autoUpdateIntervalItem.add(halfSecItem);
        
        halfSecItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        setUpdateInterval(HALF_SEC);
                        fiveSecItem.setSelected(false);
                        threeSecItem.setSelected(false);
                        tenSecItem.setSelected(false);
                        oneSecItem.setSelected(false);
                        qtrSecItem.setSelected(false);
                    }
                }
                );
        
        qtrSecItem = new JCheckBoxMenuItem(".25 second");
        qtrSecItem.setMnemonic('q');
        qtrSecItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        qtrSecItem.setEnabled(true);
        if (updateInterval == QUARTER_SEC) {
            qtrSecItem.setSelected(true);
        } else {
            qtrSecItem.setSelected(false);
        }
        autoUpdateIntervalItem.add(qtrSecItem);
        
        qtrSecItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                       
                        setUpdateInterval(QUARTER_SEC);
                        fiveSecItem.setSelected(false);
                        threeSecItem.setSelected(false);
                        tenSecItem.setSelected(false);
                        oneSecItem.setSelected(false);
                        halfSecItem.setSelected(false);
                    }
                }
                );
        
        
        optionMenu.addSeparator();
        JMenu graphTypeItem = new JMenu("Graph Data Point...");
        graphTypeItem.setMnemonic('g');
        graphTypeItem.setEnabled(true);
        graphTypeItem.setToolTipText("Select the Data Point to Graph");
        optionMenu.add(graphTypeItem);

        graphDeltasItem = new JCheckBoxMenuItem("Graph Delta");
        graphDeltasItem.setMnemonic('d');
        graphDeltasItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        graphDeltasItem.setEnabled(true);
        graphDeltasItem.setSelected(true);
        graphDeltasItem.setToolTipText("Graph The Delta value (Difference Between Current and Last Values)");
        
        graphTypeItem.add(graphDeltasItem);
        
        graphDeltasItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        graphValuesItem.setSelected(false);
                        deltaValue.setForeground(selectedColor);
                        thisValue.setForeground(Color.WHITE);
                        graph.reset();
                    }
                }
        );
        graphValuesItem = new JCheckBoxMenuItem("Graph Current Value");
        graphValuesItem.setMnemonic('v');
        graphValuesItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        graphValuesItem.setEnabled(true);
        graphValuesItem.setSelected(false);
        graphValuesItem.setToolTipText("Graph The Current Value");
        
        graphTypeItem.add(graphValuesItem);
        
        graphValuesItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        graphDeltasItem.setSelected(false);
                        thisValue.setForeground(selectedColor);
                        deltaValue.setForeground(Color.WHITE);
                        graph.reset();
                       
                    }
                }
                );
        
        optionMenu.addSeparator();
        JMenu colorItem = new JMenu("Graph Point Color...");
        graphTypeItem.setMnemonic('c');
        graphTypeItem.setEnabled(true);
        graphTypeItem.setToolTipText("Select the Data Point Color");
        optionMenu.add(colorItem);
        colorItems = new JCheckBoxMenuItem[colors.length];
        
        for (int i = 0; i < colorItems.length; i++) {
            colorItems[i] = new JCheckBoxMenuItem(colorNames[i]);
            colorItems[i].setEnabled(true);
            colorItem.add(colorItems[i]);
            final int idx = i;
            colorItems[i].addActionListener(
                    new ActionListener()
                    {
                        public void actionPerformed (ActionEvent e)
                        {
                           
                            graph.setColor(colors[idx]);
                            selectedColor = colors[idx];
                            if (graphDeltasItem.isSelected()) {
                                deltaValue.setForeground(selectedColor);
                            } else {
                                thisValue.setForeground(selectedColor);
                            }
                            for (int k = 0; k < colors.length; k++) {
                                if (k != idx) {
                                    colorItems[k].setSelected(false);
                                }
                            }
                            
                        }
                    }
                    );
        }
        colorItems[0].setSelected(true);
        
        JMenuBar menuBar = new JMenuBar();
        
        setJMenuBar (menuBar);
        menuBar.add (fileMenu);
        menuBar.add (optionMenu);
        
    }
    public static void main(String[] args) throws Exception{
        
        new WatchFrame(null, null, new ObjectName("d:type=F*o,name=Bar"), "TestAttr");
    }

}
