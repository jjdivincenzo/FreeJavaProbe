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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

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
public class ExpireyDialog extends JDialog {
	
    private static final long serialVersionUID = 1L;
    private int frameWidth = 350;
	private int frameHeight = 200;
	private int buttonWidth=30;
	private int buttonHeight=30;

	// Swing component
	private JTextField requestedDate;
	private JTextField expireyMS;
		

	/**
	 * This is the default ctor to initialize all of the swing components
	 *
	 */
	
	public ExpireyDialog() { 
				
		
		super();
		
		Dimension d = new Dimension();
		d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((( (int) d.getWidth() /2)  - (frameWidth / 2)),
					(( (int) d.getHeight() / 2) - (frameHeight /2)));
		
		
 		setModal(true);
		setSize(frameWidth,frameHeight);
		this.setResizable(true);
				
		setTitle("Expire Time Generator ");		
 		
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(2,2,2,2);
		c2.fill = GridBagConstraints.BOTH;
 

		requestedDate = new JTextField(Utilities.formatTimeStamp(System.currentTimeMillis(), "yyyy-MM-dd"));
		requestedDate.setEditable(true);
		expireyMS = new JTextField();
		expireyMS.setEditable(false);
		JLabel l1 = new JLabel("Enter Date (yyyy-mm-dd)");
		JLabel l2 = new JLabel("Expirey Milliseconds");
		
		
		
		c2.gridx=0;
		c2.gridy=0;
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx=.3;
		c2.weighty=1.0;
		c2.fill = GridBagConstraints.BOTH;
		add(l1,c2);
		c2.gridx++;
		c2.weightx=.7;
		c2.gridwidth=2;
		add(requestedDate,c2);
		c2.gridx = 0;
		c2.gridy++;
		c2.weightx=.3;
		c2.gridwidth=1;
		add(l2,c2);
		c2.gridx++;
		c2.weightx=.1;
		c2.gridwidth=2;
		add(expireyMS,c2);
		c2.gridx = 0;
        c2.gridy++;
        c2.weightx=.33;
        c2.gridwidth=1;
		JButton b1 = new JButton();
		b1.setText("Create");
		b1.setSize(buttonWidth, buttonHeight);
		add(b1,c2);
		c2.gridx++;
		JButton b3 = new JButton();
        b3.setText("Validate");
        b3.setSize(buttonWidth, buttonHeight);
        add(b3,c2);
        c2.gridx++;
		JButton b2 = new JButton();
        b2.setText("Close");
        b2.setSize(buttonWidth, buttonHeight);
        add(b2,c2);
        
		
		
		b2.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						closeDialog();
					 }
				});
		
		b3.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        String ms = expireyMS.getText();
                        if (ms.trim().isEmpty()) {
                            return;
                        }
                        
                        long milli = Long.parseLong(ms);
                        String time = Utilities.formatTimeStamp(milli, "yyyy-MM-dd");
                        requestedDate.setText(time);
                     }
                });
		
		b1.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        try {
                            Date date = Date.valueOf(requestedDate.getText());
                            Calendar cal = new GregorianCalendar();
                            cal.setTime(date);
                            long ms = cal.getTimeInMillis();
                            expireyMS.setText(Long.toString(ms));
                        } catch (IllegalArgumentException ex) {
                            expireyMS.setText("Invalid Date Format");
                        }
                     }
                });

				
		

		setVisible(true);
		
	}
	/**
	 * This simply closes the dialog
	 */
	private void closeDialog() {
		this.dispose();
		System.exit(0);
	}
	
	/**
	 * This method will enumerate through the system properties to display them
	 */



	public static void main(String args[]) {
		
	    
		new ExpireyDialog(); 
		
		
	}

	
}
