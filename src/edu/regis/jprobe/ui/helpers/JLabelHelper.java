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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

/**
 * @author jdivince
 *
 */
public class JLabelHelper {

		private int defaultHorizontalAlignment;
 		private Dimension defaultPreferredSize;
 		private Color defaultForeground;
 		private Color defaultBackground;
 		private Font defaultFont;
 		
 		public JLabelHelper() {
 			
 			defaultHorizontalAlignment = JLabel.LEFT;
 	 		defaultPreferredSize = null;
 	 		defaultForeground = null;
 	 		defaultBackground = null;
 	 		defaultFont = null;
 			
 		}
 		
 		public JLabelHelper(int defaultHorizontalAlignment, 
 				Dimension defaultPreferredSize,
 				Color defaultForeground,
 				Color defaultBackground,
 				Font defaultFont) {
 			
 			this.defaultHorizontalAlignment = defaultHorizontalAlignment;
 	 		this.defaultPreferredSize = defaultPreferredSize;
 	 		this.defaultForeground = defaultForeground;
 	 		this.defaultBackground = defaultBackground;
 	 		this.defaultFont = defaultFont;
 			
 		}
 		
 		public JLabel newLabel(String text) {
 			
 			JLabel ret = new JLabel(text);
 			ret.setHorizontalAlignment(defaultHorizontalAlignment);
 			
 			if (defaultPreferredSize != null) {
 				ret.setPreferredSize(defaultPreferredSize);
 			}
 			
 			if (defaultForeground != null) {
 				ret.setForeground(defaultForeground);
 			}
 			
 			if (defaultBackground != null) {
 				ret.setBackground(defaultBackground);
 			}
 			
  			if (defaultFont != null) {
 				ret.setFont(defaultFont);
 			}
  			
  			return ret;
 		}

		/**
		 * @return the defaultHorizontalAlignment
		 */
		public int getDefaultHorizontalAlignment() {
			return defaultHorizontalAlignment;
		}

		/**
		 * @param defaultHorizontalAlignment the defaultHorizontalAlignment to set
		 */
		public void setDefaultHorizontalAlignment(int defaultHorizontalAlignment) {
			this.defaultHorizontalAlignment = defaultHorizontalAlignment;
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
		 * @return the defaultForeground
		 */
		public Color getDefaultForeground() {
			return defaultForeground;
		}

		/**
		 * @param defaultForeground the defaultForeground to set
		 */
		public void setDefaultForeground(Color defaultForeground) {
			this.defaultForeground = defaultForeground;
		}

		/**
		 * @return the defaultBackground
		 */
		public Color getDefaultBackground() {
			return defaultBackground;
		}

		/**
		 * @param defaultBackground the defaultBackground to set
		 */
		public void setDefaultBackground(Color defaultBackground) {
			this.defaultBackground = defaultBackground;
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
