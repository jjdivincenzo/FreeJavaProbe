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
package edu.regis.jprobe.ui;

import java.awt.Color;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author jdivince
 *
 */
public class TableHeaderRenderer extends DefaultTableCellRenderer{

    private static final long serialVersionUID = 1L;
    private Color foreground;
    private Color background; 
    
    public TableHeaderRenderer() {
        
        foreground = getForeground();
        background = this.getBackground();
        
     }
    public TableHeaderRenderer(Color foreground) {
        
        this.foreground = foreground;
        background = this.getBackground(); 
     }
    
    @Override
    public void setValue(Object value) {

        String text = "<null>";
        
        if (value != null) {
            text = value.toString();
        }
        
       
        setForeground(foreground);
        setBackground(background);
        setText(text);
       
    }

}
