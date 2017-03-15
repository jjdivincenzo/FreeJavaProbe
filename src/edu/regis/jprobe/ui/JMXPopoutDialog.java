package edu.regis.jprobe.ui;

import java.awt.Container;
import java.awt.Dimension;
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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import edu.regis.jprobe.model.DataFormatter;

public class JMXPopoutDialog extends JDialog {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int frameWidth = 600;
    private int frameHeight = 800;
    private int buttonWidth=30;
    private int buttonHeight=30;
    private static List<JMXPopoutDialog> openDialogs = new ArrayList<JMXPopoutDialog>();

    

    /**
     * This is the default ctor to initialize all of the swing components
     *
     */
    
    public JMXPopoutDialog(Frame parent, Object obj) { 
                
        
        super(parent, false);
       
        Container data = DataFormatter.format(obj);
        
        this.setResizable(true);
                
        setTitle("Attribute Data(" + obj.getClass().getName() + ")");      
        
        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        GridBagConstraints c5 = new GridBagConstraints();
        c5.insets= new Insets(1,1,1,1);
        c5.fill = GridBagConstraints.BOTH;
        c5.gridx=0;
        c5.gridy=0;
        c5.gridwidth=5;
        c5.gridheight=5;
        c5.weightx=1;
        c5.weighty=1;
        p1.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
        p1.add(data, c5);
                 
        // Panel for our action buttons
        JPanel p5 = new JPanel();
        p5.setLayout(new GridLayout(1,2,5,5));
        p5.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
        

        
        // OK button and event handler
        JButton b1 = new JButton();
        p5.add(new JLabel(" "));
        b1.setText("Close");
        b1.setToolTipText("Close this Attribute Popup");
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
        JButton b2 = new JButton();
        p5.add(new JLabel(" "));
        b2.setText("Close All");
        b2.setToolTipText("Close All Open Attribute Popups");
        b2.setSize(buttonWidth, buttonHeight);
        
        p5.add(b2);
        p5.add(new JLabel(" "));
        b2.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        closeAllDialogs();
                     }
                });
                
        
        // Add the panels to the frame
        setLayout(new  GridBagLayout());
        GridBagConstraints c4 = new GridBagConstraints();
        c4.insets= new Insets(2,2,2,2);
        c4.fill = GridBagConstraints.BOTH;
        
                
        // System info panel
        c4.gridx=0;
        c4.gridy=0;
        c4.gridwidth=1;
        c4.gridheight=5;
        c4.weightx=1;
        c4.weighty=1;
        add(p1,c4);
            
        
        // Buttons
        c4.gridx=0;
        c4.gridy=5;
        c4.weightx=.1;
        c4.weighty=0;
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
            frameWidth = (int) (pd.width * .5);
            frameHeight = (int) (pd.height * .5);
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
        openDialogs.add(this);
        setVisible(true);
        
    }
    /**
     * This simply closes the dialog
     */
    private void closeDialog() {
        

        openDialogs.remove(this);
        this.dispose();
    }
    private void closeAllDialogs() {
        
        for (JMXPopoutDialog dialog : openDialogs) {
            dialog.dispose();
        }
        openDialogs.clear();
        
    }
}