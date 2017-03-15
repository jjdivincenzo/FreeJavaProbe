package edu.regis.jprobe.ui;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;

import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.Utilities;

/**
 * This Class is a Generic About Dialog that will also
 * display the current Java System properties, Classpath and 
 * loaded packages.
 * 
 * @author jdivincenzo
 *
 * 
 */
public class ErrorLogDialog extends JDialog {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int frameWidth = 600;
	private int frameHeight = 550;
	private int buttonWidth=30;
	private int buttonHeight=30;
	private static final float WINDOW_SCALE = .85f;

	// Swing component
	private JTextArea dbProperties;
	private ErrorLogDialog instance;
	private static File path = new File(".");
	private Logger logger;
	/**
	 * This is the default ctor to initialize all of the swing components
	 *
	 */
	
	public ErrorLogDialog(Component parent) {
		
		//If they provide a parent frame, we will position this dialog in the
		//center relative to the parent, if they don't, we will just center it in 
		//the screen
		super((Frame) parent, true);
		instance = this;
		if (parent != null ) {
			frameWidth = (int)(parent.getWidth() * WINDOW_SCALE);
			frameHeight = (int)(parent.getHeight()* WINDOW_SCALE);
		}
		setSize(frameWidth,frameHeight);
		
		Utilities.centerRelativeToParent(parent, this, frameWidth, frameHeight);
		this.setResizable(true);
				
		logger = Logger.getLogger();
		setTitle("Error Log");		
		createMenu();
					
		//Our field dimensions
		//Dimension headingSize = new Dimension(250,20);
 		Dimension hugeTextSize = new Dimension(250,80);
		
		//About Info Panel
		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());
		p1.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
		GridBagConstraints c = new GridBagConstraints();
		c.insets= new Insets(1,1,1,1);
		c.fill = GridBagConstraints.BOTH;
		
				
		//The System Properties Panel
		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());
		TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Error Log");
		tb1.setTitleColor(Color.BLUE);
		p2.setBorder(tb1);
		p2.setForeground(Color.BLUE);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(2,2,2,2);
		c2.fill = GridBagConstraints.BOTH;
 
		//System Proerties text box
		dbProperties = new JTextArea();
		Font newfont = dbProperties.getFont();
		newfont = newfont.deriveFont(Font.BOLD);
		dbProperties.setFont(newfont);
		dbProperties.setEditable(false);
		JScrollPane scroll = new JScrollPane(dbProperties);
		scroll.setPreferredSize(hugeTextSize);
		c2.gridx=0;
		c2.gridy=0;
		c2.gridwidth=3;
		c2.gridheight=6;
		c2.weightx=1.0;
		c2.weighty=1.0;
		c2.fill = GridBagConstraints.BOTH;
		p2.add(scroll,c2);
		
			
		
		// Panel for our action buttons
		JPanel p5 = new JPanel();
		p5.setLayout(new GridLayout(1,1,5,5));
		p5.setBorder(new EtchedBorder( EtchedBorder.LOWERED));

		
		// OK button and event handler
		JButton b1 = new JButton();
		b1.setText("Close");
		b1.setToolTipText("Dismiss This Dialog");
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

		//Save button and event handler
		JButton b2 = new JButton();
		b2.setText("Save");
		b2.setToolTipText("Save The Log to a File...");
		b2.setSize(buttonWidth, buttonHeight);
		
		p5.add(b2);
		p5.add(new JLabel(" "));
		b2.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						String defaultFileName = "JProbe" + 
												 Utilities.getDateTime("yyyyMMdd-HHmmss") +
												 ".log";
						JFileChooser chooser = new JFileChooser();
						chooser.setSelectedFile(new File(defaultFileName));
						chooser.setDialogType(JFileChooser.SAVE_DIALOG);
						chooser.setDialogTitle("Save The Log File ");
					    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					    chooser.setMultiSelectionEnabled(true);
					    String ext[] = {"log"}; 
					    UIFileFilter filter = new UIFileFilter(ext, "Log Files (*.log)");
					    chooser.setFileFilter(filter);
					    chooser.setCurrentDirectory(path);
					    
					    	     
					    int returnVal = chooser.showSaveDialog(instance);
					    
					    if(returnVal == JFileChooser.APPROVE_OPTION) {
					    	File f = chooser.getSelectedFile();
					    	path = chooser.getCurrentDirectory();
					    	
					    	if (f.exists()) {
					    		if ( JOptionPane.showConfirmDialog(instance, 
					     				"File Exists, Overwrite?",
					     				"Replace File?",
					     				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) 
					             {
					    			return;
					     	     }
					    	}
					    	
					    	String data = "DBTools Error Log - " + 
					    				  Utilities.getDateTime() +
					    				  System.getProperty("line.separator");
					    	
					    	data += dbProperties.getText();
					    	
					    	//Don't write out an empty file...
					    	if (data == null || data.trim().equals("") ) return;
						    
						    byte[] outdata = data.getBytes();
						    
						    try {
					            FileOutputStream fos = new FileOutputStream(f);
					            fos.write(outdata);
					            fos.close();
					        } catch (Exception exc) {
					            JOptionPane.showMessageDialog(instance,"File Save Error, Error is " +
					                    exc.getLocalizedMessage(),"File Save Error",JOptionPane.ERROR_MESSAGE);
					        } 			    	
					    	
					    }
					 }
				});

		//Clear button and event handler
		JButton b3 = new JButton();
		b3.setText("Clear");
		b3.setToolTipText("Clear The Log...");
		b3.setSize(buttonWidth, buttonHeight);
		
		p5.add(b3);
		
		b3.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						if ( JOptionPane.showConfirmDialog(instance, 
				 				"Are You Sure You Want to Clear The Error Log?",
				 				"Clear The Log?",
				 				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) 
				         {
				         		
							Logger.getLogger().clearLog();
							dbProperties.setText("");
							logger.info("Log Cleared by User Request");
				 	     } 
						
					 }
				});		
		p5.add(new JLabel(" "));
		JButton b4 = new JButton();
        b4.setText("Refresh");
        b4.setToolTipText("Reload the Log...");
        b4.setSize(buttonWidth, buttonHeight);
        
        p5.add(b4);
        
        b4.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        loadErrorLogData(true);
                        
                     }
                });     
				
		
		// Add the panels to the frame
		setLayout(new  GridBagLayout());
 		GridBagConstraints c5 = new GridBagConstraints();
		c5.insets= new Insets(1,1,1,1);
		c5.fill = GridBagConstraints.BOTH;
		
		// About info panel
		c5.gridx=0;
		c5.gridy=0;
		c5.gridwidth=1;
		c5.gridheight=4;
		c5.weightx=1;
		c5.weighty=1;
		add(p2,c5);

		

		// Buttons
		c5.gridx=0;
		c5.gridy=4;
		c5.weightx=0;
		c5.weighty=0;
		c5.gridwidth=1;
		c5.gridheight=1;
		add(p5,c5);

		//Dispose of the Dialog when we are through
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Window close handler
		addWindowListener( new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				
				 {
				closeDialog();
                 }
				
			}
		});
		loadErrorLogData(false);
		setVisible(true);
		
	}
	/**
	 * This simply closes the dialog
	 */
	private void closeDialog() {
		this.dispose();
	}
	private void loadErrorLogData(boolean atBottom) {
	    
	    String errors[] = Logger.getLogger().getLogData();
        StringBuilder data = new StringBuilder();
        
        for (int i = 0; i < errors.length; i++) {
            data.append(errors[i]).append(System.getProperty("line.separator"));
        }
        //get the properties and show the dialog
        dbProperties.setText(data.toString());
        
        int pos = (atBottom ? data.length() : 0);
        dbProperties.setCaretPosition(pos);
	}
	private void createMenu() {
	    
	    
	    
	    
        
	       //    Create the File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
     //       Add the Exit
        
        JMenuItem exitItem = new JMenuItem("Close");
        exitItem.setMnemonic('C');
        exitItem.setToolTipText("To Close This Dialog...");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        
        //create a listener for it.
        exitItem.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        closeDialog();
                    }
                }
                );
        //       Add the Exit
        fileMenu.add(exitItem);
        
        // create the Edit Menu
        JMenu optionMenu = new JMenu("Log Levels");
        optionMenu.setMnemonic('o');
        //Add the Rent 
        
        String[] levels = Logger.getSetableLevelNames();
        int curLevel = logger.getLogLevel();
        String curLevelName = Logger.getSetableLevelName(curLevel);
        setTitle("Error Log - Current Log Level(" + curLevelName + ")");
        final JCheckBoxMenuItem[] logLevelItem = new JCheckBoxMenuItem[levels.length];
        int idx = 0;
        
        ActionListener levelListener  =  new ActionListener()
        {
            public void actionPerformed (ActionEvent e)
            {
                String selected = e.getActionCommand();
                logger.setLevelByName(selected);
                for (JCheckBoxMenuItem item : logLevelItem) {
                    if (item.getText().equalsIgnoreCase(selected)) {
                        item.setSelected(true);
                    } else {
                        item.setSelected(false);
                    }
                }
                loadErrorLogData(true);
                setTitle("Error Log - Current Log Level(" + selected + ")");
            }
        };
        
        for (String level : levels) {
            String lcLevel = level.toLowerCase();
            char mnumonic = lcLevel.charAt(0);
            logLevelItem[idx] = new JCheckBoxMenuItem(level);
            logLevelItem[idx].setMnemonic(mnumonic);
            logLevelItem[idx].setEnabled(true);
            logLevelItem[idx].setToolTipText(level + " Level Logging");
            logLevelItem[idx].addActionListener(levelListener);
            if (level.equalsIgnoreCase(curLevelName)) {
                logLevelItem[idx].setSelected(true); 
            } else {
                logLevelItem[idx].setSelected(false);
            }
            optionMenu.add(logLevelItem[idx]);
            idx++;
        }
        
        
        
        
        //create a listener for it.
        
        
       
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar (menuBar);
        menuBar.add(fileMenu);
        menuBar.add(optionMenu);
	}
	
}
