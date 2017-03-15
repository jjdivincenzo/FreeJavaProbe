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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
//import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.ProbeResponse;
import static edu.regis.jprobe.model.Utilities.*;

/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClassInformationPanel extends PerformancePanel implements IPerformancePanel {
	
    private static final long serialVersionUID = 1L;
    private JTextArea loadedClasses;
	private JTextArea loadedPackages;
	private JTextArea classLoaders;
	private JTextField jitCompilerName;
	private JTextField totalCompilationTime;
	private JTextField aveCompilationTime;
	private TitledBorder loadedClassesBoarder;
	private TitledBorder loadedPackagesBoarder;
	private TitledBorder classLoadersBoarder;
	private TitledBorder jitBoarder;
	private JProbeClientFrame ui;
	private int buttonWidth=80;
	private int buttonHeight=40;
//	private ClassInformationPanel instance;
	
	private static final double LABEL_WEIGHT = 0.6;
	private static final double VALUE_WEIGHT = 0.4;
	
	public ClassInformationPanel(JProbeClientFrame u) {
		
		this.ui = u;
		//instance = this;
		Color lblColor = Color.GRAY;
		Dimension hugeTextSize = new Dimension(250,80);
		Dimension headingSize = new Dimension(50,30);
		setLayout(new  GridBagLayout());
		
		JPanel cp = new JPanel();
		cp.setLayout(new  GridBagLayout());
		loadedClassesBoarder = new TitledBorder( new EtchedBorder(), "Loaded Classes");
		cp.setBorder(loadedClassesBoarder);
 		GridBagConstraints cc = new GridBagConstraints();
		cc.insets= new Insets(1,1,1,1);
		cc.fill = GridBagConstraints.BOTH;
		
		//Thread Usage Table
		loadedClasses = new JTextArea();
		loadedClasses.setEditable(false);
		loadedClasses.setToolTipText("Highlight a Single Class Name and Right Click to View the Class Definition");
		JScrollPane resultPane = new JScrollPane( loadedClasses , 
	      		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPane.setPreferredSize(hugeTextSize);
		cc.gridx=0;
		cc.gridy=0;
		cc.gridwidth=1;
		cc.gridheight=1;
		cc.weightx = 1;
		cc.weighty = 1;
		//resultPane.setFont(fontTF);
		cp.add(resultPane, cc);
		
		JPanel jp = new JPanel();
		jp.setLayout(new  GridBagLayout());
		loadedPackagesBoarder = new TitledBorder(new EtchedBorder(), "Loaded Packages");
		jp.setBorder(loadedPackagesBoarder);
		GridBagConstraints cc2 = new GridBagConstraints();
		cc2.insets= new Insets(1,1,1,1);
		cc2.fill = GridBagConstraints.BOTH;
		cc2.gridx=0;
		cc2.gridy=0;
		cc2.gridwidth=1;
		cc2.gridheight=1;
		cc2.weightx = 1;
		cc2.weighty = 1;
		//Thread Usage Table
		loadedPackages = new JTextArea();
		loadedPackages.setEditable(false);
		JScrollPane resultPane2 = new JScrollPane( loadedPackages , 
	      		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPane2.setPreferredSize(hugeTextSize);
		//resultPane.setFont(fontTF);
		jp.add(resultPane2, cc2);
		
		JPanel cl = new JPanel();
		cl.setLayout(new  GridBagLayout());
		classLoadersBoarder = new TitledBorder( new EtchedBorder(), "Class Loaders");
		cl.setBorder(classLoadersBoarder);
		GridBagConstraints cc4 = new GridBagConstraints();
		cc4.insets= new Insets(1,1,1,1);
		cc4.fill = GridBagConstraints.BOTH;
		cc4.gridx=0;
		cc4.gridy=0;
		cc4.gridwidth=1;
		cc4.gridheight=1;
		cc4.weightx = 1;
		cc4.weighty = 1;
		//Thread Usage Table
		classLoaders = new JTextArea();
		classLoaders.setEditable(false);
		JScrollPane resultPane3 = new JScrollPane( classLoaders , 
	      		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPane3.setPreferredSize(hugeTextSize);
		//resultPane.setFont(fontTF);
		cl.add(resultPane3, cc4);
		
		JPanel jit = new JPanel();
		jit.setLayout(new  GridBagLayout());
		jitBoarder = new TitledBorder( new EtchedBorder(), "JIT Compiler Info");
		jit.setBorder(jitBoarder);
		GridBagConstraints cc5 = new GridBagConstraints();
		cc5.insets= new Insets(1,1,1,1);
		cc5.fill = GridBagConstraints.BOTH;
		JLabel l1 = new JLabel("JIT Compiler Name");
		l1.setHorizontalAlignment(JLabel.RIGHT);
		l1.setPreferredSize(headingSize);
		l1.setForeground(lblColor);
		cc5.gridx=0;
		cc5.gridy=0;
		cc5.gridwidth=1;
		cc5.gridheight=1;
		cc5.weightx = LABEL_WEIGHT;
		cc5.weighty = 1;
		jit.add(l1,cc5);
		//Thread Usage Table
		jitCompilerName = new JTextField();
		jitCompilerName.setEditable(false);
		cc5.gridx=1;
		cc5.gridy=0;
		cc5.gridwidth=1;
		cc5.gridheight=1;
		cc5.weightx = VALUE_WEIGHT;
		cc5.weighty = 1;
		Font fontTF = jitCompilerName.getFont();
		fontTF = fontTF.deriveFont(Font.BOLD);
		jitCompilerName.setFont(fontTF);
		jit.add(jitCompilerName, cc5);
		
		JLabel l2 = new JLabel("Total Compilation Time (ms)");
		l2.setHorizontalAlignment(JLabel.RIGHT);
		l2.setPreferredSize(headingSize);
		l2.setForeground(lblColor);
		cc5.gridx=3;
		cc5.gridy=0;
		cc5.gridwidth=1;
		cc5.gridheight=1;
		cc5.weightx = LABEL_WEIGHT;
		cc5.weighty = 1;
		jit.add(l2,cc5);
		//Thread Usage Table
		totalCompilationTime = new JTextField();
		totalCompilationTime.setEditable(false);
		cc5.gridx=4;
		cc5.gridy=0;
		cc5.gridwidth=1;
		cc5.gridheight=1;
		cc5.weightx = VALUE_WEIGHT;
		cc5.weighty = 1;
		totalCompilationTime.setFont(fontTF);
		jit.add(totalCompilationTime, cc5);
		
		JLabel l3 = new JLabel("Average Compilation Time(ms)");
		l3.setHorizontalAlignment(JLabel.RIGHT);
		l3.setPreferredSize(headingSize);
		l3.setForeground(lblColor);
		cc5.gridx=5;
		cc5.gridy=0;
		cc5.gridwidth=1;
		cc5.gridheight=1;
		cc5.weightx = LABEL_WEIGHT;
		cc5.weighty = 1;
		jit.add(l3,cc5);
		//Thread Usage Table
		aveCompilationTime = new JTextField();
		aveCompilationTime.setEditable(false);
		cc5.gridx=6;
		cc5.gridy=0;
		cc5.gridwidth=1;
		cc5.gridheight=1;
		cc5.weightx = VALUE_WEIGHT;
		cc5.weighty = 1;
		aveCompilationTime.setFont(fontTF);
		jit.add(aveCompilationTime, cc5);
		
		
		GridBagConstraints cc3 = new GridBagConstraints();
		cc3.insets= new Insets(1,1,1,1);
		cc3.fill = GridBagConstraints.BOTH;
		cc3.gridx=0;
		cc3.gridy=0;
		cc3.gridwidth=3;
		cc3.gridheight=3;
		cc3.weightx = 1;
		cc3.weighty = .3;
		add(cp,cc3);
		
		cc3.gridx=0;
		cc3.gridy=3;
		cc3.gridwidth=3;
		cc3.gridheight=3;
		cc3.weightx = 1;
		cc3.weighty = .3;
		add(jp,cc3);
		
		cc3.gridx=0;
		cc3.gridy=6;
		cc3.gridwidth=3;
		cc3.gridheight=2;
		cc3.weightx = 1;
		cc3.weighty = .3;
		add(cl,cc3);
		
		cc3.gridx=0;
		cc3.gridy=9;
		cc3.gridwidth=3;
		cc3.gridheight=1;
		cc3.weightx = 1;
		cc3.weighty = .05;
		add(jit,cc3);
		
		//Get Class Info Button
		JButton classButton = new JButton();
		JLabel ls1 = new JLabel("   ");
		JLabel ls2 = new JLabel("   ");
		classButton.setText("Refresh Class Info");
		classButton.setToolTipText("Refresh Class Info");
		classButton.setSize(buttonWidth, buttonHeight);
		if (u.isPlaybackMode()) {
		    classButton.setEnabled(false);
		} else {
		    classButton.setEnabled(true);
		}
		cc3.gridx=0;
		cc3.gridy=10;
		cc3.gridwidth=1;
		cc3.gridheight=1;
		cc3.weightx = .2;
		cc3.weighty = .1;
		add(ls1,cc3);
		cc3.gridx=1;
		cc3.weightx = .1;
		add(classButton,cc3);
		
		cc3.gridx=2;
		cc3.weightx = .2;
		add(ls2,cc3);
		classButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						if (!ui.isConnected) return;
						//if ( JOptionPane.showConfirmDialog(ui, 
				 		//		"This is a Very CPU Intensive Operation on the Target JVM, " +
						//		"Are You Sure You Want to Do This?",
				 		//		"Are You Sure",
				 		//		JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
						//		
								ui.setCursor(new Cursor(Cursor.WAIT_CURSOR));
								ui.getClassInfo();
								ui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				 	    // } 
						
					 }
				});
		
		loadedClasses.addMouseListener(
				new MouseAdapter()
				{
					public void mouseClicked(MouseEvent e)
					{
						if (e.getButton() == 3) {
							int start = loadedClasses.getSelectionStart();
							int end = loadedClasses.getSelectionEnd();
							String val = loadedClasses.getText().substring(start, end);
							String text = (ui.getClassProperties(val.trim()));
							new ClassInfoDialog(ui.parent,text, val);
							
						}
		                
					 }
				}
				);
	}
	public void update(ProbeResponse res) {
		
		
		
		if (res.isLoadedClasses_updated()) {
			loadedClasses.setText(res.getLoadedClasses());
			loadedClasses.setCaretPosition(0);
			
		}
		
		if (res.isLoadedPackages_updated()) {
			loadedPackages.setText(res.getLoadedPackages());
			loadedPackages.setCaretPosition(0);
			
		}
		
		if (res.isClassLoaders_updated()) {
			classLoaders.setText(res.getClassLoaders());
			classLoaders.setCaretPosition(0);
			
		}
		
		loadedClassesBoarder.setTitle("Loaded Classes(" + 
				format(res.getNumberOfLoadedClasses()) + ") Total Size(" +
				format(res.getTotalClassSize()) +")");
		loadedPackagesBoarder.setTitle("Loaded Packages(" + 
				format(res.getNumberOfLoadedPackages()) + ")");
		classLoadersBoarder.setTitle("Class Loaders(" + 
				format(res.getNumberOfClassLoaders()) +")");
		
		jitCompilerName.setText(res.getJitCompilerName());
		totalCompilationTime.setText(format(res.getTotalCompilationTime()));
		
		double aveComp = (double)res.getTotalCompilationTime() / (double)res.getTotalClassesLoaded();
		aveCompilationTime.setText(format(aveComp,2));
		this.repaint();
	}
	public void resetPanel() {
		loadedClasses.setText("");
		loadedPackages.setText("");
		classLoaders.setText("");
	}

	
	
}
