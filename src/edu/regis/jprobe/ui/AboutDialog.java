///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2007  James Di Vincenzo
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
import java.awt.Desktop;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

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
public class AboutDialog extends JDialog {
	
    private static final long serialVersionUID = 1L;
    private int frameWidth = 600;
	private int frameHeight = 700;
	private int buttonWidth=30;
	private int buttonHeight=30;

	// Swing component
	private JTextArea systemProperties;
	private JTextArea classPath;
	private JTextArea loadedPackages;
	private JTextArea licenseInfo;
	

	// License Text
	private static final String[] LICENSE_TEXT = {
	  "Monitor Your Java VM and Application Without Making Code Changes!",
	  " ",
      "                    Copyright (C) 2007-2017",
      "                    James Di Vincenzo",
      "                    email: jjdivincenzo@gmail.com",
	  " ",
	  "	    This library is free software; you can redistribute it and/or",
	  "	    modify it under the terms of the GNU Lesser General Public",
	  "	    License as published by the Free Software Foundation; either",
	  "	    version 2.1 of the License, or (at your option) any later version.",
	  " ",	
	  "	    This library is distributed in the hope that it will be useful,",
	  "	    but WITHOUT ANY WARRANTY; without even the implied warranty of",
	  "	    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU",
	  "	    Lesser General Public License for more details.",
	  " ",	
	  "	    You should have received a copy of the GNU Lesser General Public",
	  "	    License along with this library; if not, write to the Free Software",
	  "	    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA",
	};
	/**
	 * This is the default ctor to initialize all of the swing components
	 *
	 */
	
	public AboutDialog(Frame parent) { 
				
		
		super(parent, true);
		//If they provide a parent frame, we will position this dialog in the
		//center relative to the parent, if they don't, we will just center it in 
		//the screen
		if (parent != null) {
			Point p = parent.getLocation();
			Dimension pd = parent.getSize();
			frameWidth = (int) (pd.width * .70);
			frameHeight = (int) (pd.height * .90);
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
				
		setTitle("About " + Utilities.WINDOW_TITLE);		
		Dimension headingSize = new Dimension(250,20);
					
		//Our field dimensions
 		Dimension hugeTextSize = new Dimension(250,80);
 		//About Info Panel
        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        p1.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
        GridBagConstraints c = new GridBagConstraints();
        c.insets= new Insets(1,1,1,1);
        c.fill = GridBagConstraints.BOTH;
        
        // Name and version
        JLabel l1 = new JLabel(Utilities.WINDOW_TITLE);
        l1.setHorizontalAlignment(JLabel.CENTER);
        l1.setPreferredSize(headingSize);
        Font font = l1.getFont().deriveFont(Font.BOLD + Font.ITALIC, 18);
        l1.setFont(font);
        l1.setForeground(Color.BLUE);
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=3;
        c.gridheight=1;
        c.weighty = 0;
        c.weightx=1;
        p1.add(l1,c);
        
        // build Date
        JLabel buildTS = new JLabel();
        buildTS.setHorizontalAlignment(JLabel.CENTER);
        buildTS.setPreferredSize(headingSize);
        c.gridx=0;
        c.gridy=1;
        c.gridwidth=3;
        c.gridheight=1;
        c.weighty = 0;
        c.weightx=1;
        p1.add(buildTS,c);
        buildTS.setText("Build Date: " + getBuildDate(Utilities.JAR_NAME));
        
        
        try {
            final URI uri = new URI("http://sourceforge.net/projects/javavmprobe");
            JLabel lblLink = new JLabel();
            lblLink.setText("<HTML>Download at <FONT color=\"#000099\"><U>SourceForge.net</U></FONT></HTML>");
            lblLink.setHorizontalAlignment(SwingConstants.CENTER);
            lblLink.setOpaque(false);
            lblLink.setBackground(Color.WHITE);
            lblLink.setToolTipText(uri.toString());
            lblLink.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    open(uri);
                }
            });
                          
            c.gridx=0;
            c.gridy=2;
            c.gridwidth=3;
            c.gridheight=1;
            c.weighty = 0;
            c.weightx=1;
            p1.add(lblLink,c);
        } catch (URISyntaxException e1) {
            Logger.getLogger().logException(e1, this);
        }
        

        
        
       		
				
		//The System Properties Panel
		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());
		TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "Java System Properties");
		tb1.setTitleColor(Color.BLUE);
		p2.setBorder(tb1);
		p2.setForeground(Color.BLUE);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(2,2,2,2);
		c2.fill = GridBagConstraints.BOTH;
 
		//System Proerties text box
		systemProperties = new JTextArea();
		systemProperties.setEditable(false);
		JScrollPane scroll = new JScrollPane(systemProperties);
		scroll.setPreferredSize(hugeTextSize);
		c2.gridx=0;
		c2.gridy=0;
		c2.gridwidth=3;
		c2.gridheight=6;
		c2.weightx=1.0;
		c2.weighty=1.0;
		c2.fill = GridBagConstraints.BOTH;
		p2.add(scroll,c2);
		
		//		The System Properties Panel
		JPanel p3 = new JPanel();
		p3.setLayout(new GridBagLayout());
		TitledBorder tb2 = new TitledBorder( new EtchedBorder(), "Java Class Path");
		tb2.setTitleColor(Color.BLUE);
		p3.setBorder(tb2);
		p3.setForeground(Color.BLUE);
		GridBagConstraints c3 = new GridBagConstraints();
		c3.insets= new Insets(2,2,2,2);
		c3.fill = GridBagConstraints.BOTH;
		
		//		System Proerties text box
		classPath = new JTextArea();
		classPath.setEditable(false);
		JScrollPane scroll2 = new JScrollPane(classPath);
		scroll.setPreferredSize(hugeTextSize);
		c3.gridx=0;
		c3.gridy=0;
		c3.gridwidth=3;
		c3.gridheight=6;
		c3.weightx=1.0;
		c3.weighty=1.0;
		c3.fill = GridBagConstraints.BOTH;
		p3.add(scroll2,c3);
		
//		The System Properties Panel
		JPanel p4 = new JPanel();
		p4.setLayout(new GridBagLayout());
		TitledBorder tb3 = new TitledBorder( new EtchedBorder(), "Loaded Packages");
		tb3.setTitleColor(Color.BLUE);
		p4.setBorder(tb3);
		p4.setForeground(Color.BLUE);
		GridBagConstraints c4 = new GridBagConstraints();
		c4.insets= new Insets(2,2,2,2);
		c4.fill = GridBagConstraints.BOTH;
		
		//		System Proerties text box
		loadedPackages = new JTextArea();
		loadedPackages.setEditable(false);
		JScrollPane scroll3 = new JScrollPane(loadedPackages);
		scroll.setPreferredSize(hugeTextSize);
		c4.gridx=0;
		c4.gridy=0;
		c4.gridwidth=3;
		c4.gridheight=6;
		c4.weightx=1.0;
		c4.weighty=1.0;
		c4.fill = GridBagConstraints.BOTH;
		p4.add(scroll3,c4);
		
		//The GNU License Panel
		JPanel p6 = new JPanel();
		p6.setLayout(new GridBagLayout());
		TitledBorder tb6 = new TitledBorder( new EtchedBorder(), "License Info");
		tb6.setTitleColor(Color.BLUE);
		p6.setBorder(tb6);
		p6.setForeground(Color.BLUE);
		GridBagConstraints c6 = new GridBagConstraints();
		c6.insets= new Insets(2,2,2,2);
		c6.fill = GridBagConstraints.BOTH;
		
		//		System Proerties text box
		licenseInfo = new JTextArea();
		licenseInfo.setEditable(false);
		JScrollPane scroll6 = new JScrollPane(licenseInfo);
		scroll6.setPreferredSize(hugeTextSize);
		c6.gridx=0;
		c6.gridy=0;
		c6.gridwidth=3;
		c6.gridheight=6;
		c6.weightx=1.0;
		c6.weighty=1.0;
		c6.fill = GridBagConstraints.BOTH;
		p6.add(scroll6,c6);
		
		
		// Panel for our action buttons
		JPanel p5 = new JPanel();
		p5.setLayout(new GridLayout(1,1,5,5));
		p5.setBorder(new EtchedBorder( EtchedBorder.LOWERED));

		
		// OK button and event handler
		JButton b1 = new JButton();
		p5.add(new JLabel(" "));
		b1.setText("OK");
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
		
		// About license info panel
		c5.gridx=0;
		c5.gridy=0;
		c5.gridwidth=1;
		c5.gridheight=1;
		c5.weightx=1;
		c5.weighty=0;
		add(p1,c5);
		
		c5.gridx=0;
        c5.gridy=1;
        c5.gridwidth=1;
        c5.gridheight=3;
        c5.weightx=1;
        c5.weighty=.6;
        add(p6,c5);

		
		// System info panel
		c4.gridx=0;
		c4.gridy=4;
		c4.gridwidth=1;
		c4.gridheight=3;
		c4.weightx=1;
		c4.weighty=.2;
		add(p2,c4);

				
		// Class Path
		c4.gridx=0;
		c4.gridy=7;
		c4.weightx=1;
		c4.weighty=.2;
		c4.gridheight=3;
		add(p3,c4);

		// Loaded Packages
		c4.gridx=0;
		c4.gridy=10;
		c4.weightx=1;
		c4.weighty=.3;
		c4.gridheight=1;
		//add(p4,c4);

		// Buttons
		c4.gridx=0;
		//c4.gridy=5;
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
		getSystemProperties();
		getLicenseText();
		systemProperties.setCaretPosition(0);
		classPath.setCaretPosition(0);
		loadedPackages.setCaretPosition(0);
		licenseInfo.setCaretPosition(0);
		setVisible(true);
		
	}
	/**
	 * This simply closes the dialog
	 */
	private void closeDialog() {
		this.dispose();
	}
	
	/**
	 * This method will enumerate through the system properties to display them
	 */
	private void getSystemProperties() {
		
		//Get an enumerate list of system property keys
		Properties prop = System.getProperties();
		
		//loop through each to display key=value
		for (Enumeration<?> e = prop.propertyNames() ; e.hasMoreElements() ;) {
			
			String key = e.nextElement().toString();
			String val = System.getProperty(key);
			
			systemProperties.append(key + "=" + val + "\n");

	     }
		
		String path = System.getProperty("java.class.path");
		
		   StringTokenizer st = new StringTokenizer(path,
		           	System.getProperty("path.separator"));
		   
		
		   while (st.hasMoreTokens()) {
		       classPath.append(st.nextToken() + "\n");
		
		   }
		  AboutClassLoader acl = new AboutClassLoader();
		  acl.getLoadedPackageNames(loadedPackages);
	}
	private void getLicenseText() {
		
		for (int i = 0; i < LICENSE_TEXT.length; i++) {
			licenseInfo.append(LICENSE_TEXT[i] + "\n");
		}
		
	}
	private void open(URI uri) {
	    
        if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                        desktop.browse(uri);
                } catch (IOException e) {
                    Logger.getLogger().logException(e, this);
                    JOptionPane.showMessageDialog(this, 
                            "Unable to Open Web Browser, Error is " + 
                                    e.getMessage(),
                             "Unable to Open Web Browser", 
                             JOptionPane.ERROR_MESSAGE, null);
                }
        } 
    }

	public static void main(String args[]) {
		
		new AboutDialog(null); 
		
		
	}
	   public static String getBuildDate(String jarName) {
	        
	        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
	            
	        URL urls[] = sysloader.getURLs();
	        String buildDate = "Unable to Obtain";
	        
	        for (int i = 0; i < urls.length; i++) {
	            File f = new File(urls[i].getFile());
	            if (f.getName().equalsIgnoreCase(jarName)) {
	                buildDate = new Date(f.lastModified()).toString();
	                
	            }
	            
	        }
	        
	        return buildDate;
	    }
	
}
/**
 * 
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class AboutClassLoader extends ClassLoader {
	
	 public void getLoadedPackageNames(JTextArea jta) {
	 	
	 	Package pks[] = getPackages();
	 	List<String> pkList = new ArrayList<String>();
	 	
	 	for(int a = 0; a < pks.length; a++ ) {
	 		pkList.add(pks[a].getName());
	 		
	 	}
	 	Collections.sort(pkList);
	 	
	 	for (int i = 0; i < pkList.size(); i ++ ) {
	 		jta.append(pkList.get(i) + "\n");
	 	}
	 	
	 	
	 }
}