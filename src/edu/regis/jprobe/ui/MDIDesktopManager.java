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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultDesktopManager;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MDIDesktopManager extends DefaultDesktopManager implements ContainerListener, ActionListener, PropertyChangeListener
{

    private static final long serialVersionUID = 1L;
    private JDesktopPane desktop;
	private JMenu windowMenu = new JMenu("Window");

	/**
	 * Constructor that takes the desktop pane as a parameter
	 */
	public MDIDesktopManager(JDesktopPane theDesktop)
	{
		desktop = theDesktop;
		desktop.addContainerListener(this);
	}

	/**
	 * adds an Internal Frame to the desktop
	 */
	public void addInternalFrame(JInternalFrame f)
	{
		//add property change listener
		f.addPropertyChangeListener(this);

		//make it visible
		f.setVisible(true);

		//add it
		desktop.add(f);

		//make sure its selected and activated
		activateFrame(f);
		try{
			f.setSelected(true);
		} catch(Exception e){}
			
	}

	/**
	 * cascades all un-iconified windows
	 */
	public void cascade()
	{
		JInternalFrame[] allFrames = desktop.getAllFrames();

		int width = desktop.getWidth();
		int height = desktop.getHeight();

		//int frameWidth = 400;
		//int frameHeight = 400;
		int frameWidth = (int)(width * 0.7);
		int frameHeight = (int)(height * 0.7);

		int x = 0;
		int y = 0;

		for(int i=allFrames.length-1; i>=0; i--)
		{
			if(!allFrames[i].isIcon())
			{
				setBoundsForFrame(allFrames[i], x, y, frameWidth, frameHeight);
				x += 30;
				y += 30;
				if((x + frameWidth) > width)
				{
					x = 0;
					y = 0;
				}
				if((y + frameHeight) > height)
				{
					x = 0;
					y = 0;
				}
			}
		}
	}

	/**
	 * tiles all un-iconified windows horizontally
	 */
	public void tileHorizontally()
	{
		JInternalFrame[] allFrames = desktop.getAllFrames();
		int num = getNumberOfVisible(allFrames);
		if (num == 0) return;
		int width = desktop.getWidth() / num;

		int numDone = 0;

		for(int i=0; i<allFrames.length; i++)
		{
			if(!allFrames[i].isIcon())
			{
				setBoundsForFrame(allFrames[i], numDone*width, 0, width, desktop.getHeight());
				numDone++;
			}
		}
	}

	/**
	 * tiles all un-iconified windows vertically
	 */
	public void tileVertically()
	{
		JInternalFrame[] allFrames = desktop.getAllFrames();
		int num = getNumberOfVisible(allFrames);
		
		if (num == 0) return;
		
		int height = desktop.getHeight() / num;

		int numDone = 0;

		for(int i=0; i<allFrames.length; i++)
		{
			if(!allFrames[i].isIcon())
			{
				setBoundsForFrame(allFrames[i], 0, numDone*height, desktop.getWidth(), height);
				numDone++;
			}
		}
	}

	public void minimizeAll()
	{
		JInternalFrame[] allFrames = desktop.getAllFrames();

		for(int i=0; i<allFrames.length; i++)
		{
			if(!allFrames[i].isIcon())
			{
				iconifyFrame(allFrames[i]);
				
			}
		}
	}
	
	public void maximizeAll()
	{
		JInternalFrame[] allFrames = desktop.getAllFrames();

		for(int i=0; i<allFrames.length; i++)
		{
			if(allFrames[i].isIcon())
			{
				deiconifyFrame(allFrames[i]);
				
			}
		}
	}
	/**
	 * arranges all of the iconified icons into rows at the bottom of the desktop
	 */
	public void arrangeIcons()
	{
		JInternalFrame[] allFrames = desktop.getAllFrames();

		Rectangle r;
		JInternalFrame.JDesktopIcon icon;

		int height = desktop.getHeight();
		int width = desktop.getWidth();

		int row = 1;
		int column = 0;

		for(int i=0; i<allFrames.length; i++)
		{
			if(allFrames[i].isIcon())
			{
				icon = allFrames[i].getDesktopIcon();
				r = getBoundsForIconOf(allFrames[i]);

				icon.setBounds(column*r.width, height - (row*r.height), r.width, r.height);

				column++;
				if((column+1) * r.width > width)
				{
					column = 0;
					row++;
				}
			}
		}
	}

	/**
	 * selects the frame specified in index
	 */
	public void selectInternalFrame(int index)
	{
		JInternalFrame[] allFrames = desktop.getAllFrames();
		activateFrame(allFrames[index]);

		try{
			allFrames[index].setSelected(true);
		}catch(Exception e){}
	}

 	/**
 	 * this creates the window menu containing all the windows name.
 	 * When frames are added or removed this menu is updated automatically
 	 */
 	public JMenu createWindowMenu()
	{
		windowMenu.removeAll();

		//add windows
		JInternalFrame[] allFrames = desktop.getAllFrames();

		boolean enabled = allFrames.length != 0;

		//add normal windows
		windowMenu.add(createMenuItem("Cascade", null, enabled, 'C', KeyEvent.VK_C));
		windowMenu.add(createMenuItem("Tile Horizontally", null, enabled, 'H', KeyEvent.VK_H));
		windowMenu.add(createMenuItem("Tile Vertically", null, enabled, 'V', KeyEvent.VK_V));
		windowMenu.add(createMenuItem("Arrange Icons", null, enabled, 'A', KeyEvent.VK_A));
		//Max and Min don't work...
		//windowMenu.add(createMenuItem("Minimize All", null, enabled));
		//windowMenu.add(createMenuItem("Maximize All", null, enabled));


		//if there are no windows
		if(allFrames.length == 0)
			return windowMenu;

		windowMenu.addSeparator();
		for(int i=0; i<allFrames.length; i++)
			windowMenu.add(createMenuItem((i+1) + ".  " + allFrames[i].getTitle(), "" + i, enabled));

		return windowMenu;
	}

 	/**
 	 * Processes events from the windowMenu
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		String command = e.getActionCommand();
 		if(command.equals("Cascade"))
 			cascade();
 		else if(command.equals("Tile Horizontally"))
 			tileHorizontally();
 		else if(command.equals("Tile Vertically"))
 			tileVertically();
 		else if(command.equals("Arrange Icons"))
 			arrangeIcons();
 		else if(command.equals("Minimize All"))
 			minimizeAll();
 		else if(command.equals("Maximize All"))
 			maximizeAll();
 		else
 		{
			try
			{
				selectInternalFrame(Integer.parseInt(command));
			}
			catch(NumberFormatException e1){}//if its not an integer ignore it
		}
 	}

 	/**
 	 * Used for convenience to set up a JMenuItem
 	 */
 	private JMenuItem createMenuItem(String title, String actionCommand, boolean enabled, char mnemonic, int accelorator )
 	{
 		JMenuItem item = new JMenuItem(title);

 		item.setEnabled(enabled);

 		if(actionCommand != null)
 			item.setActionCommand(actionCommand);

 		item.addActionListener(this);
 		if (mnemonic != 0) {
 			item.setMnemonic(mnemonic);
 			item.setAccelerator(KeyStroke.getKeyStroke(
 					accelorator, ActionEvent.CTRL_MASK));
 		}
 		return item;
 	}
 	private JMenuItem createMenuItem(String title, String actionCommand, boolean enabled)
 	{
 		return createMenuItem(title, actionCommand, enabled, (char) 0, 0);
 	}

 	/*
 	 * CONTAINER LISTENER
 	 */
 	public void componentAdded(ContainerEvent e){
 		createWindowMenu();
 	}
 	public void componentRemoved(ContainerEvent e){
 		createWindowMenu();
 	}

 	//Property Listener to update window menu when title changes
 	public void propertyChange(PropertyChangeEvent evt)
 	{
 		String property = evt.getPropertyName();

 		if(property.equals("title"))
 			createWindowMenu();
 	}

 	/**
 	 * returns the number of frames which haven't been iconified
 	 */
 	private int getNumberOfVisible(JInternalFrame[] frames)
 	{
 		int number = 0;

 		for(int i=0; i<frames.length; i++)
 			if(!frames[i].isIcon())
 				number++;

 		return number;
 	}
}
