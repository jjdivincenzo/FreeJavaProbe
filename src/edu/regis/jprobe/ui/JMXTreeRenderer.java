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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public class JMXTreeRenderer  implements TreeCellRenderer {

    private Font normal;
    private Font bold;
        
	public JMXTreeRenderer() {
	    normal = new JLabel().getFont();
	    bold = normal.deriveFont(Font.ITALIC + Font.BOLD);
	}
	public Component getTreeCellRendererComponent(JTree tree, Object value,

	    boolean selected, boolean expanded, boolean leaf, int row,
		boolean hasFocus) {
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object o = node.getUserObject();
		JMXTreeItem ti = (JMXTreeItem) o;
		ImageIcon icon = ti.getIcon();
		
		JLabel retval = new JLabel(value.toString());
		Dimension d = retval.getSize();
				
		if (selected) {
		    retval.setFont(bold);
            retval.setForeground(Color.BLUE);
            d.width *= 2;
            retval.setSize(d);
            
		} else {
		    retval.setFont(normal);
		}
		
		
		if (icon != null) {
		    retval.setIcon(icon);
		    retval.setHorizontalAlignment(JLabel.RIGHT);
		}

		return retval;
	}

}
