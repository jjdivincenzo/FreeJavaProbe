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

import javax.swing.JProgressBar;
import javax.swing.UIManager;

/**
 * @author jdivince
 *
 */
public class JProgressBarHelper {
	
	private Dimension defaultPreferredSize;
	private boolean defaultStringPainted;
	private int defaultValue;
	private int defaultMinimum;
	private int defaultMaximum;
	private Font defaultFont;
	
	public JProgressBarHelper() { 
		defaultPreferredSize = null;
		defaultStringPainted = true;
		defaultValue = 0;
		defaultMinimum = 0;
		defaultMaximum = 100;
		defaultFont = UIManager.getFont("ProgressBar.font");
		if (defaultFont != null) {
		    defaultFont = defaultFont.deriveFont(Font.BOLD);
		}
		
	}
	
	public JProgressBar newProgressBar(String toolTipHelp) {
		
		JProgressBar ret = new JProgressBar();
		ret.setToolTipText(toolTipHelp);
		ret.setValue(defaultValue);
		ret.setMinimum(defaultMinimum);
		ret.setMaximum(defaultMaximum);
		ret.setStringPainted(defaultStringPainted);
		if (defaultFont != null) {
		    ret.setFont(defaultFont);
		}
		
		if (defaultPreferredSize != null) {
			ret.setPreferredSize(defaultPreferredSize);
		}
		
		return ret;
		
	}
	
	public JProgressBar newProgressBar() {
		
		JProgressBar ret = new JProgressBar();
		ret.setValue(defaultValue);
		ret.setMinimum(defaultMinimum);
		ret.setMaximum(defaultMaximum);
		ret.setStringPainted(defaultStringPainted);
		
		if (defaultPreferredSize != null) {
			ret.setPreferredSize(defaultPreferredSize);
		}
		
		return ret;
		
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
	 * @return the defaultStringPainted
	 */
	public boolean isDefaultStringPainted() {
		return defaultStringPainted;
	}

	/**
	 * @param defaultStringPainted the defaultStringPainted to set
	 */
	public void setDefaultStringPainted(boolean defaultStringPainted) {
		this.defaultStringPainted = defaultStringPainted;
	}

	/**
	 * @return the defaultValue
	 */
	public int getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the defaultMinimum
	 */
	public int getDefaultMinimum() {
		return defaultMinimum;
	}

	/**
	 * @param defaultMinimum the defaultMinimum to set
	 */
	public void setDefaultMinimum(int defaultMinimum) {
		this.defaultMinimum = defaultMinimum;
	}

	/**
	 * @return the defaultMaximum
	 */
	public int getDefaultMaximum() {
		return defaultMaximum;
	}

	/**
	 * @param defaultMaximum the defaultMaximum to set
	 */
	public void setDefaultMaximum(int defaultMaximum) {
		this.defaultMaximum = defaultMaximum;
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
