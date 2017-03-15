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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.regis.jprobe.model.StackExcludeFilters;




/**
 * @author jdivincenzo
 *
 * This Class is the GUI Dialog for displaying vehicle data so that the user can
 * add, update or delete vehicles.
 * 
 */
public class FiltersDialog extends JDialog {
	

    private static final long serialVersionUID = 1L;
    private int frameWidth = 500;
	private int frameHeight = 500;

	private JList<String> filterList;
	private JTextField addedFilter;


	private StackExcludeFilters stackExclusionfilters;
	private boolean dialogCanceled = false;
	private String[] filterString;
	private List<String> filters;
	
	/**
	 * This is the default ctor for this class.
	 * @param owner
	 * @param prop
	 */ 
	public FiltersDialog(final JFrame owner, final UIOptions prop) {
		
		super(owner, true);
		this.stackExclusionfilters = prop.getFilters();
		this.setTitle("Stack Trace Exclusion Filters");
		this.filters = stackExclusionfilters.getAllFilters();
		
		setSize(frameWidth,frameHeight);
		
		try {
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
		} catch (Exception e1) {}		
				
		centerRelativeToParent(owner, this, frameWidth, frameHeight);
				
					
		//Our field dimensions
		Dimension headingSize = new Dimension(50,20);
		
		
		JPanel cpuPanel = new JPanel();
		cpuPanel.setLayout(new GridBagLayout());
		TitledBorder tb0 = new TitledBorder( new EtchedBorder(), "Filters");
		cpuPanel.setBorder(tb0);
		GridBagConstraints c = new GridBagConstraints();
		c.insets= new Insets(2,2,2,2);
		c.fill = GridBagConstraints.BOTH;
		
		
		JLabel lbnst = new JLabel("Excluded Stack Class Name Prefixes");
		lbnst.setHorizontalAlignment(JLabel.CENTER);
		lbnst.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=4;
		c.gridheight=1;
		c.weightx = 1;
		c.weighty = 0;
		cpuPanel.add(lbnst,c);
		filterList= new JList<String>();
		filterList.setBorder(new EtchedBorder());
		filterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		c.gridx=0;
		c.gridy=1;
		c.gridwidth=4;
		c.gridheight=5;
		c.weightx = 1;
		c.weighty = .8;
 
		cpuPanel.add(filterList,c);
		JLabel l2 = new JLabel("Class Name Prefix to Add or Delete");
        l2.setHorizontalAlignment(JLabel.LEFT);
        l2.setPreferredSize(headingSize);
        c.gridx=0;
        c.gridy=6;
        c.gridwidth=3;
        c.gridheight=1;
        c.weightx = 1;
        c.weighty = 0;
        cpuPanel.add(l2,c);
        c.gridx=3;
        c.gridy=6;
        c.gridwidth=1;
        c.gridheight=1;
        c.weightx = 0;
        c.weighty = 0;
        cpuPanel.add(new JLabel(" "),c);
        addedFilter = new JTextField();
        c.gridx=0;
        c.gridy=7;
        c.gridwidth=2;
        c.gridheight=1;
        c.weightx = .9;
        c.weighty = .1;
        cpuPanel.add(addedFilter,c);
        JButton addBtn = new JButton("Add");
        c.gridx=2;
        c.gridy=7;
        c.gridwidth=1;
        c.gridheight=1;
        c.weightx = .1;
        c.weighty = .1;
        cpuPanel.add(addBtn,c);
        JButton deleteBtn = new JButton("Delete");
        c.gridx=3;
        c.gridy=7;
        c.gridwidth=1;
        c.gridheight=1;
        c.weightx = .1;
        c.weighty = .1;
        cpuPanel.add(deleteBtn,c);
        
        String[] filterString = new String[stackExclusionfilters.getSize()];
        
        for (int i = 0; i < filterString.length; i++) {
            filterString[i] = stackExclusionfilters.getAllFilters().get(i);
        }

		filterList.setListData(getFilterData(filters));
		
		filterList.addListSelectionListener(
		        new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                       Object sel =  filterList.getSelectedValue();
                       
                       if (sel != null) {
                           addedFilter.setText(sel.toString());
                       }
                        
                    }
		            
		        });
		addBtn.addActionListener(new 
		        ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (!filters.contains(addedFilter.getText().trim())) {
                            filters.add(addedFilter.getText().trim());
                            filterList.setListData(getFilterData(filters));
                        } 
                        
                    }
		    
		});
		deleteBtn.addActionListener(new 
                ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (filters.contains(addedFilter.getText().trim())) {
                            filters.remove(addedFilter.getText().trim());
                            filterList.setListData(getFilterData(filters));
                        } 
                        
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
		c3.gridheight=4;
		c3.weightx=1;
		c3.weighty=.9;
		add(cpuPanel,c3);
		



				
		// Buttons
		c3.gridx=0;
		c3.gridy=4;
		c3.weightx=1;
		c3.weighty=.1;
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

		if (filters != null) {
		    stackExclusionfilters.setAllFilters(filters);
		    stackExclusionfilters.save();
		}
		this.dispose(); 	
		
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


	private String[] getFilterData(List<String> filters) {
	    
	    filterString = new String[filters.size()];
        
        for (int i = 0; i < filterString.length; i++) {
            filterString[i] = filters.get(i);
        }
        
        return filterString;
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
}
