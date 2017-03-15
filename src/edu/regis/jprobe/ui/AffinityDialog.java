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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

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
public class AffinityDialog extends JDialog {
	
    private static final long serialVersionUID = 1L;
    private int frameWidth = 400;
	private int widthPad = 80;
	private int checkWidth = 50;
	private int frameHeight = 200;
	private int buttonWidth=30;
	private int buttonHeight=30;

	// Swing component
	private JCheckBox cpus[];
	private long newMask;
	private long numCPUs;
	private boolean dialogCanceled = false;
	

	
	/**
	 * This is the default ctor to initialize all of the swing components
	 *
	 */
	
	public AffinityDialog(JFrame parent, long numCPUs, long mask) { 
				
		
		super(parent, true);				
		setTitle("Set CPU Affinity(" + Utilities.getBitMask(mask, (int)numCPUs, false) + ")");		
		newMask = mask;
		this.numCPUs = numCPUs;
					
		int rows = (int) (numCPUs / 4);	
		if (numCPUs % 4 != 0) {
		    rows++;
		}
		//The System Properties Panel
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(rows,4,1,1));
		TitledBorder tb1 = new TitledBorder( new EtchedBorder(), "CPU's");
		tb1.setTitleColor(Color.BLUE);
		p2.setBorder(tb1);
		p2.setForeground(Color.BLUE);
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(2,2,2,2);
		c2.fill = GridBagConstraints.BOTH;
		int minlen = 0;
		
		cpus = new JCheckBox[(int)numCPUs];
		
		for (int i = 0; i < numCPUs; i++) {
			
			cpus[i] = new JCheckBox("CPU" + i);
			minlen += checkWidth;
			p2.add(cpus[i]);
		}
		minlen += widthPad;
		
		if (minlen > frameWidth) frameWidth = minlen;
		
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
						saveMask();
						
					 }
				});
		
		JButton b2 = new JButton();
		//p5.add(new JLabel(" "));
		b2.setText("Cancel");
		b2.setSize(buttonWidth, buttonHeight);
		
		p5.add(b2);
		p5.add(new JLabel(" "));
		b2.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						dialogCanceled = true;
						closeDialog();
					 }
				});

				
		
		// Add the panels to the frame
		setLayout(new  GridBagLayout());
 		GridBagConstraints c4 = new GridBagConstraints();
		c4.insets= new Insets(1,1,1,1);
		c4.fill = GridBagConstraints.BOTH;
		
		// About license info panel
		c4.gridx=0;
		c4.gridy=0;
		c4.gridwidth=1;
		c4.gridheight=3;
		c4.weightx=1;
		c4.weighty=.6;
		add(p2,c4);

				
		// Class Path
		c4.gridx=0;
		c4.gridy=4;
		c4.weightx=1;
		c4.weighty=.2;
		c4.gridheight=1;
		add(p5,c4);

		

		//Dispose of the Dialog when we are through
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Window close handler
		addWindowListener( new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				
				dialogCanceled = true;
				closeDialog();
               
			}
		});
		
		
		setMask(mask);
		
		if (parent != null) {
			Point p = parent.getLocation();
			Dimension pd = parent.getSize();
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
		setVisible(true);
		
	}
	private void setMask(long mask) {
		
		String smask = Utilities.getBitMask(mask, (int)numCPUs, false);
		char mask1[] = smask.toCharArray();
		
		char maskarr[] = reverse(mask1);
						
		for (int i = 0; i < cpus.length; i++) {
			if (maskarr[i] == '1') cpus[i].setSelected(true);
		}
	}
	private char[] reverse(char val[]) {
		
		char newarr[] = new char[val.length];
		
		int offset = val.length -1;
		
		for (int i = 0; i < newarr.length; i++) {
			newarr[i] = val[offset--];
		}
		
		return newarr;
	}
	private void saveMask() {
		boolean oneSelected = false;
		
		int maskValues[] = new int[Long.SIZE];
		int last = 1;
		for (int i = 0; i < maskValues.length; i++) {
		    maskValues[i] = last;
		    last *= 2;
		}
		char newarr[] = new char[cpus.length];
		newMask = 0;
		
		for (int i = 0; i < cpus.length; i++) {
			if (cpus[i].isSelected()) {
				oneSelected = true;
				newMask +=maskValues[i];
			} else {
				newarr[i] = '0';
			}
		}
		
		if (!oneSelected) {
			JOptionPane.showMessageDialog(this,"At Least One CPU Must Be Selected",
	    			   "Error",JOptionPane.ERROR_MESSAGE);
	    	return;
		}
		
		
		
		closeDialog();
	}
	/**
	 * This simply closes the dialog
	 */
	private void closeDialog() {
		this.dispose();
	}
	
	
	
	/**
	 * @return the dialogCanceled
	 */
	public boolean isDialogCanceled() {
		return dialogCanceled;
	}
	/**
	 * @return the newMask
	 */
	public long getNewMask() {
		return newMask;
	}
	public static void main(String args[]) {
		
		int numCPU = 24;
		int oldMask = 16777215;
		AffinityDialog dialog = new AffinityDialog(null,numCPU,oldMask); 
		
		System.out.println("For " + numCPU + " cpus, the old mask is " + oldMask + 
				" the new Mask is " + dialog.getNewMask());
	}
	
}
