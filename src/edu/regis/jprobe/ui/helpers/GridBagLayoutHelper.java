///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2006  James Di Vincenzo
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
package edu.regis.jprobe.ui.helpers;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;

/**
 * @author jdivince
 *
 */
public class GridBagLayoutHelper {
	
	private Container container;
	private GridBagConstraints constraints;
	private double defaultWeightX;
	private double defaultWeightY;
	private int gridX = 0;
	private int gridY = 0;
	private int lastHeight = 0;

	public GridBagLayoutHelper(Container container, GridBagConstraints constraints, 
			double defaultWeightX, double defaultWeightY) {
		
		this.container = container;
		this.constraints = constraints;
		this.defaultWeightX = defaultWeightX;
		this.defaultWeightY = defaultWeightY;
		
	}
	
	public GridBagLayoutHelper(Container container, GridBagConstraints constraints) {
		
		this.container = container;
		this.constraints = constraints;
		this.defaultWeightX = 0.0;
		this.defaultWeightY = 0.0;
		
	}
	
	public void addColumn(Component c, int width, int height, double weightX, double weightY) {
		
		constraints.gridx=gridX;
		constraints.gridy=gridY;
		constraints.gridwidth=width;
		constraints.gridheight=height;
		constraints.weightx = weightX;
		constraints.weighty = weightY;
		container.add(c, constraints);
		gridX +=width;
		
		if (lastHeight < height)  lastHeight = height;
	}
	
	public void addColumn(Component c, int width) {
		
		addColumn(c, width, 1, defaultWeightX, defaultWeightY);
		
	}
	
	public void addColumn(Component c, int width, int height) {
		
		addColumn(c, width, height, defaultWeightX, defaultWeightY);
		
	}
	public void newRow() {
		gridX = 0;
		gridY += lastHeight;
		lastHeight = 0;
	}
	/**
	 * @return the defaultWeightX
	 */
	public double getDefaultWeightX() {
		return defaultWeightX;
	}

	/**
	 * @param defaultWeightX the defaultWeightX to set
	 */
	public void setDefaultWeightX(double defaultWeightX) {
		this.defaultWeightX = defaultWeightX;
	}

	/**
	 * @return the defaultWeightY
	 */
	public double getDefaultWeightY() {
		return defaultWeightY;
	}

	/**
	 * @param defaultWeightY the defaultWeightY to set
	 */
	public void setDefaultWeightY(double defaultWeightY) {
		this.defaultWeightY = defaultWeightY;
	}

	
}
