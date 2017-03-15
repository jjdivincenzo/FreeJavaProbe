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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;




/**
 * @author jdivincenzo
 *
 * This Class is the GUI Dialog for displaying vehicle data so that the user can
 * add, update or delete vehicles.
 * 
 */
public class ThresholdDialog extends JDialog {
	
    private static final long serialVersionUID = 1L;
	private int frameWidth = 500;
	private int frameHeight = 600;
	
	// Swing components
	private JTextField cpuThresholdOK;
	private JTextField cpuThresholdWarn;
	private JTextField cpuThresholdBad;
	private JTextField cpuColorOK;
	private JTextField cpuColorWarn;
	private JTextField cpuColorBad;
	private JTextField heapThresholdOK;
	private JTextField heapThresholdWarn;
	private JTextField heapThresholdBad;
	private JTextField heapColorOK;
	private JTextField heapColorWarn;
	private JTextField heapColorBad;
	private JTextField nonHeapThresholdOK;
	private JTextField nonHeapThresholdWarn;
	private JTextField nonHeapThresholdBad;
	private JTextField nonHeapColorOK;
	private JTextField nonHeapColorWarn;
	private JTextField nonHeapColorBad;
	private JTextField gcThresholdOK;
	private JTextField gcThresholdWarn;
	private JTextField gcThresholdBad;
	private JTextField gcColorOK;
	private JTextField gcColorWarn;
	private JTextField gcColorBad;
	private JTextField threadBlocking;
	private JTextField threadDaemon;
	private JTextField threadNonDaemon;
	
	private JTextField graphForeground;
	private JTextField graphBackground;
	private JFrame owner;
	private ThresholdDialog instance;

	//Data Management and utility classes
	private UIOptions prop;
	private boolean dialogCanceled = false;
	
	/**
	 * This is the default ctor for this class.
	 * @param owner
	 * @param prop
	 */
	public ThresholdDialog(final JFrame owner, final UIOptions prop) {
		
		super(owner, true);
		this.owner = owner;
		instance = this;
		this.setTitle("Threshold Values and Colors");
		
		this.prop = prop;
		
		setSize(frameWidth,frameHeight);
		
		try {
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
		} catch (Exception e1) {}		
				
		centerRelativeToParent(owner, this, frameWidth, frameHeight);
				
					
		//Our field dimensions
		Dimension headingSize = new Dimension(50,20);
		Dimension largeTextSize = new Dimension(250,20);
		
		//CPU Threshold Panel
		JPanel cpuPanel = new JPanel();
		cpuPanel.setLayout(new GridBagLayout());
		TitledBorder tb0 = new TitledBorder( new EtchedBorder(), "CPU");
		cpuPanel.setBorder(tb0);
		GridBagConstraints c = new GridBagConstraints();
		c.insets= new Insets(2,2,2,2);
		c.fill = GridBagConstraints.BOTH;
		
		//Properties File label& Field
		JLabel lbnst = new JLabel("Normal Status Threshold");
		lbnst.setHorizontalAlignment(JLabel.RIGHT);
		lbnst.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		cpuPanel.add(lbnst,c);
		cpuThresholdOK = new JTextField();
		c.gridx=1;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		cpuThresholdOK.setPreferredSize(largeTextSize);
		cpuThresholdOK.setText(new Double(prop.getCpuThresholdOk()).toString());
		cpuPanel.add(cpuThresholdOK,c);
		JLabel lbnsc = new JLabel("Normal Status Color");
		lbnsc.setHorizontalAlignment(JLabel.RIGHT);
		lbnsc.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		cpuPanel.add(lbnsc,c);
		cpuColorOK = new JTextField();
		c.gridx=3;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		cpuColorOK.setPreferredSize(largeTextSize);
		cpuColorOK.setBackground(prop.getCpuColorOk());
		cpuColorOK.setEditable(false);
		cpuColorOK.setText("   ");
		cpuPanel.add(cpuColorOK,c);
		JButton btnColorOK = new JButton("...");
		c.gridx=4;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		cpuPanel.add(btnColorOK,c);
		btnColorOK.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getCpuColorOk(), 
								"Normal Status");
						cpuColorOK.setBackground(dlg.getColor());
					 }
				});

		
				

		
		//Properties File label& Field
		JLabel lbwst = new JLabel("Warning Status Threshold");
		lbwst.setHorizontalAlignment(JLabel.RIGHT);
		lbwst.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=1;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		cpuPanel.add(lbwst,c);
		cpuThresholdWarn = new JTextField();
		c.gridx=1;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		cpuThresholdWarn.setPreferredSize(largeTextSize);
		cpuThresholdWarn.setText(new Double(prop.getCpuThresholdWarn()).toString());
		cpuPanel.add(cpuThresholdWarn,c);
		JLabel lbwsc = new JLabel("Warning Status Color");
		lbwsc.setHorizontalAlignment(JLabel.RIGHT);
		lbwsc.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=1;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		cpuPanel.add(lbwsc,c);
		cpuColorWarn = new JTextField();
		c.gridx=3;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		cpuColorWarn.setPreferredSize(largeTextSize);
		cpuColorWarn.setBackground(prop.getCpuColorWarn());
		cpuColorWarn.setText("   ");
		cpuColorWarn.setEditable(false);
		cpuPanel.add(cpuColorWarn,c);
		JButton btnColorWarn = new JButton("...");
		c.gridx=4;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		cpuPanel.add(btnColorWarn,c);
		btnColorWarn.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getCpuColorWarn(),
								"Warning Status");
						cpuColorWarn.setBackground(dlg.getColor());
					 }
				});
				
		
		//Properties File label& Field
		JLabel lbcst = new JLabel("Critical Status Threshold");
		lbcst.setHorizontalAlignment(JLabel.RIGHT);
		lbcst.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=2;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		cpuPanel.add(lbcst,c);
		cpuThresholdBad = new JTextField();
		c.gridx=1;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		cpuThresholdBad.setPreferredSize(largeTextSize);
		cpuThresholdBad.setText(new Double(prop.getCpuThresholdBad()).toString());
		cpuPanel.add(cpuThresholdBad,c);
		JLabel lbcsc = new JLabel("Critical Status Color");
		lbcsc.setHorizontalAlignment(JLabel.RIGHT);
		lbcsc.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=2;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		cpuPanel.add(lbcsc,c);
		cpuColorBad = new JTextField();
		c.gridx=3;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		cpuColorBad.setPreferredSize(largeTextSize);
		cpuColorBad.setBackground(prop.getCpuColorBad());
		cpuColorBad.setText("   ");
		cpuColorBad.setEditable(false);
		cpuPanel.add(cpuColorBad,c);
		JButton btnColorBad = new JButton("...");
		c.gridx=4;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		cpuPanel.add(btnColorBad,c);
		btnColorBad.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getCpuColorBad(),
								"Critical Status");
						cpuColorBad.setBackground(dlg.getColor());
					 }
				});
		
		
		//Heap Threshold Panel
		JPanel heapPanel = new JPanel();
		heapPanel.setLayout(new GridBagLayout());
		TitledBorder tbheap = new TitledBorder( new EtchedBorder(), "Heap");
		heapPanel.setBorder(tbheap);
		//GridBagConstraints c = new GridBagConstraints();
		c.insets= new Insets(2,2,2,2);
		c.fill = GridBagConstraints.BOTH;
		
		//Properties File label& Field
		JLabel lbnsth = new JLabel("Normal Status Threshold");
		lbnsth.setHorizontalAlignment(JLabel.RIGHT);
		lbnsth.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		heapPanel.add(lbnsth,c);
		heapThresholdOK = new JTextField();
		c.gridx=1;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		heapThresholdOK.setPreferredSize(largeTextSize);
		heapThresholdOK.setText(new Double(prop.getHeapThresholdOk()).toString());
		heapPanel.add(heapThresholdOK,c);
		JLabel lbnsch = new JLabel("Normal Status Color");
		lbnsch.setHorizontalAlignment(JLabel.RIGHT);
		lbnsch.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		heapPanel.add(lbnsch,c);
		heapColorOK = new JTextField();
		c.gridx=3;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		heapColorOK.setPreferredSize(largeTextSize);
		heapColorOK.setBackground(prop.getHeapColorOk());
		heapColorOK.setEditable(false);
		heapColorOK.setText("   ");
		heapPanel.add(heapColorOK,c);
		JButton btnColorOKh = new JButton("...");
		c.gridx=4;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		heapPanel.add(btnColorOKh,c);
		btnColorOKh.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getHeapColorOk(), 
								"Normal Status");
						heapColorOK.setBackground(dlg.getColor());
					 }
				});

		
				

		
		//Properties File label& Field
		JLabel lbwsth = new JLabel("Warning Status Threshold");
		lbwsth.setHorizontalAlignment(JLabel.RIGHT);
		lbwsth.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=1;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		heapPanel.add(lbwsth,c);
		heapThresholdWarn = new JTextField();
		c.gridx=1;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		heapThresholdWarn.setPreferredSize(largeTextSize);
		heapThresholdWarn.setText(new Double(prop.getHeapThresholdWarn()).toString());
		heapPanel.add(heapThresholdWarn,c);
		JLabel lbwsch = new JLabel("Warning Status Color");
		lbwsch.setHorizontalAlignment(JLabel.RIGHT);
		lbwsch.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=1;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		heapPanel.add(lbwsch,c);
		heapColorWarn = new JTextField();
		c.gridx=3;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		heapColorWarn.setPreferredSize(largeTextSize);
		heapColorWarn.setBackground(prop.getHeapColorWarn());
		heapColorWarn.setText("   ");
		heapColorWarn.setEditable(false);
		heapPanel.add(heapColorWarn,c);
		JButton btnColorWarnh = new JButton("...");
		c.gridx=4;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		heapPanel.add(btnColorWarnh,c);
		btnColorWarnh.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getHeapColorWarn(),
								"Warning Status");
						heapColorWarn.setBackground(dlg.getColor());
					 }
				});
				
		
		//Properties File label& Field
		JLabel lbcsth = new JLabel("Critical Status Threshold");
		lbcsth.setHorizontalAlignment(JLabel.RIGHT);
		lbcsth.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=2;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		heapPanel.add(lbcsth,c);
		heapThresholdBad = new JTextField();
		c.gridx=1;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		heapThresholdBad.setPreferredSize(largeTextSize);
		heapThresholdBad.setText(new Double(prop.getHeapThresholdBad()).toString());
		heapPanel.add(heapThresholdBad,c);
		JLabel lbcsch = new JLabel("Critical Status Color");
		lbcsch.setHorizontalAlignment(JLabel.RIGHT);
		lbcsch.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=2;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		heapPanel.add(lbcsch,c);
		heapColorBad = new JTextField();
		c.gridx=3;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		heapColorBad.setPreferredSize(largeTextSize);
		heapColorBad.setBackground(prop.getHeapColorBad());
		heapColorBad.setText("   ");
		heapColorBad.setEditable(false);
		heapPanel.add(heapColorBad,c);
		JButton btnColorBadh = new JButton("...");
		c.gridx=4;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		heapPanel.add(btnColorBadh,c);
		btnColorBadh.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getHeapColorBad(),
								"Critical Status");
						heapColorBad.setBackground(dlg.getColor());
					 }
				});
		
		//nonHeap Threshold Panel
		JPanel nonHeapPanel = new JPanel();
		nonHeapPanel.setLayout(new GridBagLayout());
		TitledBorder tbnonheap = new TitledBorder( new EtchedBorder(), "Non Heap");
		nonHeapPanel.setBorder(tbnonheap);
		//GridBagConstraints c = new GridBagConstraints();
		c.insets= new Insets(2,2,2,2);
		c.fill = GridBagConstraints.BOTH;
		
		//Properties File label& Field
		JLabel lbnstn = new JLabel("Normal Status Threshold");
		lbnstn.setHorizontalAlignment(JLabel.RIGHT);
		lbnstn.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		nonHeapPanel.add(lbnstn,c);
		nonHeapThresholdOK = new JTextField();
		c.gridx=1;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		nonHeapThresholdOK.setPreferredSize(largeTextSize);
		nonHeapThresholdOK.setText(new Double(prop.getNonHeapThresholdOk()).toString());
		nonHeapPanel.add(nonHeapThresholdOK,c);
		JLabel lbnscn = new JLabel("Normal Status Color");
		lbnscn.setHorizontalAlignment(JLabel.RIGHT);
		lbnscn.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		nonHeapPanel.add(lbnscn,c);
		nonHeapColorOK = new JTextField();
		c.gridx=3;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		nonHeapColorOK.setPreferredSize(largeTextSize);
		nonHeapColorOK.setBackground(prop.getNonHeapColorOk());
		nonHeapColorOK.setEditable(false);
		nonHeapColorOK.setText("   ");
		nonHeapPanel.add(nonHeapColorOK,c);
		JButton btnColorOKn = new JButton("...");
		c.gridx=4;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		nonHeapPanel.add(btnColorOKn,c);
		btnColorOKn.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getNonHeapColorOk(), 
								"Normal Status");
						nonHeapColorOK.setBackground(dlg.getColor());
					 }
				});

		
				

		
		//Properties File label& Field
		JLabel lbwstn = new JLabel("Warning Status Threshold");
		lbwstn.setHorizontalAlignment(JLabel.RIGHT);
		lbwstn.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=1;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		nonHeapPanel.add(lbwstn,c);
		nonHeapThresholdWarn = new JTextField();
		c.gridx=1;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		nonHeapThresholdWarn.setPreferredSize(largeTextSize);
		nonHeapThresholdWarn.setText(new Double(prop.getNonHeapThresholdWarn()).toString());
		nonHeapPanel.add(nonHeapThresholdWarn,c);
		JLabel lbwscn = new JLabel("Warning Status Color");
		lbwscn.setHorizontalAlignment(JLabel.RIGHT);
		lbwscn.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=1;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		nonHeapPanel.add(lbwscn,c);
		nonHeapColorWarn = new JTextField();
		c.gridx=3;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		nonHeapColorWarn.setPreferredSize(largeTextSize);
		nonHeapColorWarn.setBackground(prop.getNonHeapColorWarn());
		nonHeapColorWarn.setText("   ");
		nonHeapColorWarn.setEditable(false);
		nonHeapPanel.add(nonHeapColorWarn,c);
		JButton btnColorWarnn = new JButton("...");
		c.gridx=4;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		nonHeapPanel.add(btnColorWarnn,c);
		btnColorWarnn.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getNonHeapColorWarn(),
								"Warning Status");
						nonHeapColorWarn.setBackground(dlg.getColor());
					 }
				});
				
		
		//Properties File label& Field
		JLabel lbcstn = new JLabel("Critical Status Threshold");
		lbcstn.setHorizontalAlignment(JLabel.RIGHT);
		lbcstn.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=2;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		nonHeapPanel.add(lbcstn,c);
		nonHeapThresholdBad = new JTextField();
		c.gridx=1;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		nonHeapThresholdBad.setPreferredSize(largeTextSize);
		nonHeapThresholdBad.setText(new Double(prop.getNonHeapThresholdBad()).toString());
		nonHeapPanel.add(nonHeapThresholdBad,c);
		JLabel lbcscn = new JLabel("Critical Status Color");
		lbcscn.setHorizontalAlignment(JLabel.RIGHT);
		lbcscn.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=2;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		nonHeapPanel.add(lbcscn,c);
		nonHeapColorBad = new JTextField();
		c.gridx=3;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		nonHeapColorBad.setPreferredSize(largeTextSize);
		nonHeapColorBad.setBackground(prop.getNonHeapColorBad());
		nonHeapColorBad.setText("   ");
		nonHeapColorBad.setEditable(false);
		nonHeapPanel.add(nonHeapColorBad,c);
		JButton btnColorBadn = new JButton("...");
		c.gridx=4;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		nonHeapPanel.add(btnColorBadn,c);
		btnColorBadn.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getNonHeapColorBad(),
								"Critical Status");
						nonHeapColorBad.setBackground(dlg.getColor());
					 }
				});
		
		
		//nonHeap Threshold Panel
		JPanel gcPanel = new JPanel();
		gcPanel.setLayout(new GridBagLayout());
		TitledBorder tbgc = new TitledBorder( new EtchedBorder(), "Garbage Collector");
		gcPanel.setBorder(tbgc);
		//GridBagConstraints c = new GridBagConstraints();
		c.insets= new Insets(2,2,2,2);
		c.fill = GridBagConstraints.BOTH;
		
		//Properties File label& Field
		JLabel lbnstg = new JLabel("Normal Status Threshold");
		lbnstg.setHorizontalAlignment(JLabel.RIGHT);
		lbnstg.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		gcPanel.add(lbnstg,c);
		gcThresholdOK = new JTextField();
		c.gridx=1;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		gcThresholdOK.setPreferredSize(largeTextSize);
		gcThresholdOK.setText(new Double(prop.getGcThresholdOk()).toString());
		gcPanel.add(gcThresholdOK,c);
		JLabel lbnscg = new JLabel("Normal Status Color");
		lbnscg.setHorizontalAlignment(JLabel.RIGHT);
		lbnscg.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		gcPanel.add(lbnscg,c);
		gcColorOK = new JTextField();
		c.gridx=3;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		gcColorOK.setPreferredSize(largeTextSize);
		gcColorOK.setBackground(prop.getGcColorOk());
		gcColorOK.setEditable(false);
		gcColorOK.setText("   ");
		gcPanel.add(gcColorOK,c);
		JButton btnColorOKg = new JButton("...");
		c.gridx=4;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		gcPanel.add(btnColorOKg,c);
		btnColorOKg.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getGcColorOk(), 
								"Normal Status");
						gcColorOK.setBackground(dlg.getColor());
					 }
				});

		
				

		
		//Properties File label& Field
		JLabel lbwstg = new JLabel("Warning Status Threshold");
		lbwstg.setHorizontalAlignment(JLabel.RIGHT);
		lbwstg.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=1;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		gcPanel.add(lbwstg,c);
		gcThresholdWarn = new JTextField();
		c.gridx=1;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		gcThresholdWarn.setPreferredSize(largeTextSize);
		gcThresholdWarn.setText(new Double(prop.getGcThresholdWarn()).toString());
		gcPanel.add(gcThresholdWarn,c);
		JLabel lbwscg = new JLabel("Warning Status Color");
		lbwscg.setHorizontalAlignment(JLabel.RIGHT);
		lbwscg.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=1;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		gcPanel.add(lbwscg,c);
		gcColorWarn = new JTextField();
		c.gridx=3;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		gcColorWarn.setPreferredSize(largeTextSize);
		gcColorWarn.setBackground(prop.getGcColorWarn());
		gcColorWarn.setText("   ");
		gcColorWarn.setEditable(false);
		gcPanel.add(gcColorWarn,c);
		JButton btnColorWarng = new JButton("...");
		c.gridx=4;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		gcPanel.add(btnColorWarng,c);
		btnColorWarng.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getGcColorWarn(),
								"Warning Status");
						gcColorWarn.setBackground(dlg.getColor());
					 }
				});
				
		
		//Properties File label& Field
		JLabel lbcstg = new JLabel("Critical Status Threshold");
		lbcstg.setHorizontalAlignment(JLabel.RIGHT);
		lbcstg.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=2;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		gcPanel.add(lbcstg,c);
		gcThresholdBad = new JTextField();
		c.gridx=1;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		gcThresholdBad.setPreferredSize(largeTextSize);
		gcThresholdBad.setText(new Double(prop.getGcThresholdBad()).toString());
		gcPanel.add(gcThresholdBad,c);
		JLabel lbcscg = new JLabel("Critical Status Color");
		lbcscg.setHorizontalAlignment(JLabel.RIGHT);
		lbcscg.setPreferredSize(headingSize);
		c.gridx=2;
		c.gridy=2;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0;
		c.weighty = 0;
		gcPanel.add(lbcscg,c);
		gcColorBad = new JTextField();
		c.gridx=3;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .3;
		c.weighty = 0;
		gcColorBad.setPreferredSize(largeTextSize);
		gcColorBad.setBackground(prop.getCpuColorBad());
		gcColorBad.setText("   ");
		gcColorBad.setEditable(false);
		gcPanel.add(gcColorBad,c);
		JButton btnColorBadg = new JButton("...");
		c.gridx=4;
		c.gridy=2;
		c.gridwidth=1;
		c.weightx = .1;
		c.weighty = 0;
		gcPanel.add(btnColorBadg,c);
		btnColorBadg.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						ColorDialog dlg = new ColorDialog(owner, prop.getGcColorBad(),
								"Critical Status");
						gcColorBad.setBackground(dlg.getColor());
					 }
				});
		
		JPanel threadPanel = new JPanel();
		threadPanel.setLayout(new GridBagLayout());
        threadPanel.setBorder(new TitledBorder( new EtchedBorder(), "Threads"));
        //GridBagConstraints c = new GridBagConstraints();
        c.insets= new Insets(2,2,2,2);
        c.fill = GridBagConstraints.BOTH;
        
        //Blocking Thread Color
        JLabel lth1 = new JLabel("Blocking Thread");
        lth1.setHorizontalAlignment(JLabel.RIGHT);
        lth1.setPreferredSize(headingSize);
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight=1;
        c.weightx = .4;
        c.weighty = 0;
        threadPanel.add(lth1,c);
        threadBlocking = new JTextField();
        c.gridx=2;
        c.gridy=0;
        c.gridwidth=2;
        c.weightx = .5;
        c.weighty = 0;
        threadBlocking.setPreferredSize(largeTextSize);
        threadBlocking.setText(" ");
        threadBlocking.setBackground(prop.getBlockingThreadColor());
        threadBlocking.setEditable(false);
        threadPanel.add(threadBlocking,c);
        JButton btnBlockingThread = new JButton("...");
        c.gridx=4;
        c.gridy=0;
        c.gridwidth=1;
        c.weightx = .1;
        c.weighty = 0;
        threadPanel.add(btnBlockingThread,c);
        btnBlockingThread.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        ColorDialog dlg = new ColorDialog(owner, prop.getBlockingThreadColor(), 
                                "BlockingThread");
                        threadBlocking.setBackground(dlg.getColor());
                     }
                });
        
        JLabel lth2 = new JLabel("Daemon Thread");
        lth2.setHorizontalAlignment(JLabel.RIGHT);
        lth2.setPreferredSize(headingSize);
        c.gridx=0;
        c.gridy=1;
        c.gridwidth=2;
        c.gridheight=1;
        c.weightx = .4;
        c.weighty = 0;
        threadPanel.add(lth2,c);
        threadDaemon = new JTextField();
        c.gridx=2;
        c.gridy=1;
        c.gridwidth=2;
        c.weightx = .5;
        c.weighty = 0;
        threadDaemon.setPreferredSize(largeTextSize);
        threadDaemon.setText(" ");
        threadDaemon.setBackground(prop.getDaemonThreadColor());
        threadDaemon.setEditable(false);
        threadPanel.add(threadDaemon,c);
        JButton btnDaemonThread = new JButton("...");
        c.gridx=4;
        c.gridy=1;
        c.gridwidth=1;
        c.weightx = .1;
        c.weighty = 0;
        threadPanel.add(btnDaemonThread,c);
        btnDaemonThread.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        ColorDialog dlg = new ColorDialog(owner, prop.getDaemonThreadColor(), 
                                "Daemon Thread");
                        threadDaemon.setBackground(dlg.getColor());
                     }
                });
        
        JLabel lth3 = new JLabel("Non Daemon Thread");
        lth3.setHorizontalAlignment(JLabel.RIGHT);
        lth3.setPreferredSize(headingSize);
        c.gridx=0;
        c.gridy=2;
        c.gridwidth=2;
        c.gridheight=1;
        c.weightx = .4;
        c.weighty = 0;
        threadPanel.add(lth3,c);
        threadNonDaemon = new JTextField();
        c.gridx=2;
        c.gridy=2;
        c.gridwidth=2;
        c.weightx = .5;
        c.weighty = 0;
        threadNonDaemon.setPreferredSize(largeTextSize);
        threadNonDaemon.setText(" ");
        threadNonDaemon.setBackground(prop.getNonDaemonThreadColor());
        threadNonDaemon.setEditable(false);
        threadPanel.add(threadNonDaemon,c);
        JButton btnNonDaemonThread = new JButton("...");
        c.gridx=4;
        c.gridy=2;
        c.gridwidth=1;
        c.weightx = .1;
        c.weighty = 0;
        threadPanel.add(btnNonDaemonThread,c);
        btnNonDaemonThread.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        ColorDialog dlg = new ColorDialog(owner, prop.getNonDaemonThreadColor(), 
                                "Non Daemon Thread");
                        threadNonDaemon.setBackground(dlg.getColor());
                     }
                });


        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new GridBagLayout());
        graphPanel.setBorder(new TitledBorder( new EtchedBorder(), "Graphs"));
        //GridBagConstraints c = new GridBagConstraints();
        c.insets= new Insets(2,2,2,2);
        c.fill = GridBagConstraints.BOTH;
        
        //Blocking Thread Color
        JLabel lg1 = new JLabel("Graph Foreground");
        lg1.setHorizontalAlignment(JLabel.RIGHT);
        lg1.setPreferredSize(headingSize);
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight=1;
        c.weightx = .4;
        c.weighty = 0;
        graphPanel.add(lg1,c);
        graphForeground = new JTextField();
        c.gridx=2;
        c.gridy=0;
        c.gridwidth=2;
        c.weightx = .5;
        c.weighty = 0;
        graphForeground.setPreferredSize(largeTextSize);
        graphForeground.setText(" ");
        graphForeground.setBackground(prop.getGraphForeground());
        graphForeground.setEditable(false);
        graphPanel.add(graphForeground,c);
        JButton btnGraphForeground = new JButton("...");
        c.gridx=4;
        c.gridy=0;
        c.gridwidth=1;
        c.weightx = .1;
        c.weighty = 0;
        graphPanel.add(btnGraphForeground,c);
        btnGraphForeground.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        ColorDialog dlg = new ColorDialog(owner, prop.getGraphForeground(), 
                                "Graph Foreground");
                        graphForeground.setBackground(dlg.getColor());
                     }
                });
        
        JLabel lg2 = new JLabel("Graph Background");
        lg2.setHorizontalAlignment(JLabel.RIGHT);
        lg2.setPreferredSize(headingSize);
        c.gridx=0;
        c.gridy=1;
        c.gridwidth=2;
        c.gridheight=1;
        c.weightx = .4;
        c.weighty = 0;
        graphPanel.add(lg2,c);
        graphBackground = new JTextField();
        c.gridx=2;
        c.gridy=1;
        c.gridwidth=2;
        c.weightx = .5;
        c.weighty = 0;
        graphBackground.setPreferredSize(largeTextSize);
        graphBackground.setText(" ");
        graphBackground.setBackground(prop.getGraphBackground());
        graphBackground.setEditable(false);
        graphPanel.add(graphBackground,c);
        JButton btnGraphBackground = new JButton("...");
        c.gridx=4;
        c.gridy=1;
        c.gridwidth=1;
        c.weightx = .1;
        c.weighty = 0;
        graphPanel.add(btnGraphBackground,c);
        btnGraphBackground.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed (ActionEvent e)
                    {
                        
                        ColorDialog dlg = new ColorDialog(owner, prop.getGraphBackground(), 
                                "Graph Background");
                        graphBackground.setBackground(dlg.getColor());
                     }
                });
        
                       

        
       
        
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
						
						saveData();
						
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
						 cancelData();
					 }
				});
		
		p3.add(b3);
		// Add the panels to the frame
//		 Add the panels to the frame
		setLayout(new  GridBagLayout());
 		GridBagConstraints c3 = new GridBagConstraints();
		c3.insets= new Insets(1,1,1,1);
		c3.fill = GridBagConstraints.BOTH;
		
		//CPU panel
		c3.gridx=0;
		c3.gridy=0;
		c3.gridwidth=1;
		c3.gridheight=1;
		c3.weightx=1;
		c3.weighty=.20;
		add(cpuPanel,c3);
		
		//Heap panel
		c3.gridx=0;
		c3.gridy=1;
		c3.gridwidth=1;
		c3.gridheight=1;
		c3.weightx=1;
		add(heapPanel,c3);

		
		//NonHeap panel
		c3.gridx=0;
		c3.gridy=2;
		c3.gridwidth=1;
		c3.gridheight=1;
		c3.weightx=1;
		add(nonHeapPanel,c3);

		
		//GC panel
		c3.gridx=0;
		c3.gridy=3;
		c3.gridwidth=1;
		c3.gridheight=1;
		c3.weightx=1;
		add(gcPanel,c3);
		
		//Thread Panel
        c3.gridx=0;
        c3.gridy=4;
        c3.gridwidth=1;
        c3.gridheight=1;
        c3.weightx=1;
        add(threadPanel,c3);
        
        c3.gridx=0;
        c3.gridy=5;
        c3.gridwidth=1;
        c3.gridheight=1;
        c3.weightx=1;
        add(graphPanel,c3);


				
		// Buttons
		c3.gridx=0;
		c3.gridy=6;
		c3.weightx=.3;
		c3.weighty=0;
		c3.gridheight=1;
		add(p3,c3);

		// We want to catch this
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		
		
		// Window close handler
		addWindowListener( new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
								
				cancelData();
			}
		});
		
		setVisible(true);
		
		
	}
	
	
	/**
	 * This method is used to save the options from the panel to the properties object 
	 */
	public void saveData()
	{
		boolean inError = false;
		StringBuffer sb = new StringBuffer();

		if (!validate(cpuThresholdOK, sb)) inError = true;
		if (!validate(cpuThresholdWarn, sb)) inError = true;
		if (!validate(cpuThresholdBad, sb)) inError = true;
		
		if (!validate(heapThresholdOK, sb)) inError = true;
		if (!validate(heapThresholdWarn, sb)) inError = true;
		if (!validate(heapThresholdBad, sb)) inError = true;

		if (!validate(nonHeapThresholdOK, sb)) inError = true;
		if (!validate(nonHeapThresholdWarn, sb)) inError = true;
		if (!validate(nonHeapThresholdBad, sb)) inError = true;

		if (!validate(gcThresholdOK, sb)) inError = true;
		if (!validate(gcThresholdWarn, sb)) inError = true;
		if (!validate(gcThresholdBad, sb)) inError = true;

		
		if (inError) {
			JOptionPane.showMessageDialog( instance.owner, 
	                   sb.toString(),
	                   "Entry Error", 
	                   JOptionPane.ERROR_MESSAGE );
			return;
		}
		
		prop.setCpuThresholdOk(new Double(cpuThresholdOK.getText()).doubleValue());
		prop.setCpuThresholdWarn(new Double(cpuThresholdWarn.getText()).doubleValue());
		prop.setCpuThresholdBad(new Double(cpuThresholdBad.getText()).doubleValue());
		prop.setCpuColorOk(cpuColorOK.getBackground());
		prop.setCpuColorWarn(cpuColorWarn.getBackground());
		prop.setCpuColorBad(cpuColorBad.getBackground());
		
		prop.setHeapThresholdOk(new Double(heapThresholdOK.getText()).doubleValue());
		prop.setHeapThresholdWarn(new Double(heapThresholdWarn.getText()).doubleValue());
		prop.setHeapThresholdBad(new Double(heapThresholdBad.getText()).doubleValue());
		prop.setHeapColorOk(heapColorOK.getBackground());
		prop.setHeapColorWarn(heapColorWarn.getBackground());
		prop.setHeapColorBad(heapColorBad.getBackground());
		
		prop.setNonHeapThresholdOk(new Double(nonHeapThresholdOK.getText()).doubleValue());
		prop.setNonHeapThresholdWarn(new Double(nonHeapThresholdWarn.getText()).doubleValue());
		prop.setNonHeapThresholdBad(new Double(nonHeapThresholdBad.getText()).doubleValue());
		prop.setNonHeapColorOk(nonHeapColorOK.getBackground());
		prop.setNonHeapColorWarn(nonHeapColorWarn.getBackground());
		prop.setNonHeapColorBad(nonHeapColorBad.getBackground());
		
		prop.setGcThresholdOk(new Double(gcThresholdOK.getText()).doubleValue());
		prop.setGcThresholdWarn(new Double(gcThresholdWarn.getText()).doubleValue());
		prop.setGcThresholdBad(new Double(gcThresholdBad.getText()).doubleValue());
		prop.setGcColorOk(gcColorOK.getBackground());
		prop.setGcColorWarn(gcColorWarn.getBackground());
		prop.setGcColorBad(gcColorBad.getBackground());
		
		prop.setBlockingThreadColor(threadBlocking.getBackground());
		prop.setDaemonThreadColor(threadDaemon.getBackground());
		
		prop.setGraphForeground(graphForeground.getBackground());
		prop.setGraphBackground(graphBackground.getBackground());
		prop.save();
		prop.notifyChangeListeners();
		this.dispose(); 	
		
	}
	
	private boolean validate(JTextField item, StringBuffer sb) {
		
		double value = 0;
		item.setForeground(Color.BLACK);
		
		try {
			value = Double.parseDouble(item.getText());
			if (value < 0 || value > 100 ) {
				item.setForeground(Color.RED);
				sb.append("Highlighted Value " + item.getText() + 
		                   " is not between 0 and 100\n");
				
				return false;
			}
			
		} catch (NumberFormatException e) {
			item.setForeground(Color.RED);
			sb.append("Highlighted Value " + item.getText() +  
	                   " is not Numeric\n");
			
			return false;
		}
		
		return true;
		
	}
	/**
	 * This method is used to save the options from the panel to the properties object 
	 */
	
	
	/**
	 * This method is used to handle the window close event, from the cancel button 
	 * or the window close button.
	 */
	public void cancelData()
	   {
	    
			dialogCanceled = true;
	     	this.dispose();

	     	
     }




	/**
	 * @return the dialogCanceled
	 */
	public boolean isDialogCanceled() {
		return dialogCanceled;
	}

	
	public static void centerRelativeToParent(Component parent, 
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
	    new ThresholdDialog(null, new UIOptions());
	}
}
class ColorDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
	private JColorChooser jcs;
	private Color clr;
	private ColorDialog instance;
	private int frameWidth = 450;
	private int frameHeight = 420;
	
	ColorDialog(JFrame owner, Color init, String msg) {
		super(owner, true);
		setTitle("Choose a Color for " + msg );
		ThresholdDialog.centerRelativeToParent(owner, this, frameWidth, frameHeight);
		setSize(frameWidth,frameHeight);
		instance = this;
		clr = init;
		jcs = new JColorChooser(init);
		add(jcs, BorderLayout.NORTH);
		JPanel p1 = new JPanel();
		p1.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
		JButton btnOK = new JButton("OK");
		p1.add(btnOK);
		JButton btnCancel = new JButton("Cancel");
		p1.add(btnCancel);
		
		add(p1, BorderLayout.SOUTH);
		
		btnOK.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						clr = jcs.getColor();
						instance.dispose();
					 }
				});
		btnCancel.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						instance.dispose();
					 }
				});
		setVisible(true);
	}
	public Color getColor() { return clr; }
}
