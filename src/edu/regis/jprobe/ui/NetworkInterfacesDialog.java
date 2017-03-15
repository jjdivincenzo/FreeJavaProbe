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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

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
public class NetworkInterfacesDialog extends JDialog {
	
    private static final long serialVersionUID = 1L;
    private int frameWidth = 600;
	private int frameHeight = 700;
	private int buttonWidth=30;
	private int buttonHeight=30;

	// Swing component
	private JTextArea textArea;
		

	/**
	 * This is the default ctor to initialize all of the swing components
	 *
	 */
	
	public NetworkInterfacesDialog(Frame parent) { 
				
		
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
				
		setTitle("Network Interfaces for " + Utilities.getHostName());		
 		Dimension hugeTextSize = new Dimension(250,80);

		//The Help Panel
		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());
		p2.setBorder(new EtchedBorder());
		p2.setForeground(Color.BLUE);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(2,2,2,2);
		c2.fill = GridBagConstraints.BOTH;
 

		textArea = new JTextArea();
		textArea.setEditable(false);
		Font font = new Font(Font.MONOSPACED, Font.BOLD, 14);
		
		textArea.setFont(font); //textArea.getFont().deriveFont(Font.BOLD));
		JScrollPane scroll = new JScrollPane(textArea);
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
		p5.setLayout(new GridLayout(1,5,5,5));
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

				
		final JCheckBox showDisabled = new JCheckBox("Show Disabled");
		p5.add(showDisabled);
		p5.add(new JLabel(" "));
		showDisabled.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        textArea.setText(IPInfo.getInfo(showDisabled.isSelected()));
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
		c5.gridheight=5;
		c5.weightx=1;
		c5.weighty=.9;
		add(p2,c5);

		// Buttons
		c5.gridx=0;
		c5.gridy=5;
		c5.weightx=.1;
		c5.weighty=.0;
		c5.gridheight=1;
		add(p5,c5);

		//Dispose of the Dialog when we are through
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Window close handler
		addWindowListener( new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				
				closeDialog();
               
			}
		});
		

		textArea.setCaretPosition(0);
		textArea.setText(IPInfo.getInfo(false));

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



	public static void main(String args[]) {
		
	    
		new NetworkInterfacesDialog(null); 
		
		
	}

	
}
