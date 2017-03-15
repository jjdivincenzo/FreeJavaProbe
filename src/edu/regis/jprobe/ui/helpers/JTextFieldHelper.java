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

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JTextField;

/**
 * @author jdivince
 *
 */
public class JTextFieldHelper {
	
	private boolean defaultEditable;
	private Dimension defaultPreferredSize;
	private Font defaultFont;
	

	public JTextFieldHelper () {
		this.defaultEditable = false;
		this.defaultPreferredSize = null;
		this.defaultFont = null;
	}
	
	public JTextFieldHelper (boolean defaultEditable, Dimension defaultPreferredSize, Font defaultFont) {
		this.defaultEditable = defaultEditable;
		this.defaultPreferredSize = defaultPreferredSize;
		this.defaultFont = defaultFont;
	}
	
	public JTextField newTextField(String toolTip) {
		
		JTextField ret = new JTextField();
		ret.setEditable(defaultEditable);
		
		if (toolTip != null) ret.setToolTipText(toolTip);
		if (defaultPreferredSize != null) ret.setPreferredSize(defaultPreferredSize);
		if (defaultFont != null) ret.setFont(defaultFont);
		
		return ret;
		
	}
	
	public JTextField newTextField() { 
		return newTextField(null);
	}

	/**
	 * @return the defaultEditable
	 */
	public boolean isDefaultEditable() {
		return defaultEditable;
	}

	/**
	 * @param defaultEditable the defaultEditable to set
	 */
	public void setDefaultEditable(boolean defaultEditable) {
		this.defaultEditable = defaultEditable;
	}

	/**
	 * @return the defaultPreferredSize
	 */
	public Dimension getDefaultPreferredSize() {
		return defaultPreferredSize;
	}

	/**
	 * @param defaultPreferredSize the defaultPreferredSize to set
	 */
	public void setDefaultPreferredSize(Dimension defaultPreferredSize) {
		this.defaultPreferredSize = defaultPreferredSize;
	}

	/**
	 * @return the defaultFont
	 */
	public Font getDefaultFont() {
		return defaultFont;
	}

	/**
	 * @param defaultFont the defaultFont to set
	 */
	public void setDefaultFont(Font defaultFont) {
		this.defaultFont = defaultFont;
	}
	
	
	
}
