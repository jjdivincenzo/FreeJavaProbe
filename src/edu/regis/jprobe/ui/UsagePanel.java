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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.MemoryUsage;
import edu.regis.jprobe.model.Utilities;
import edu.regis.jprobe.ui.helpers.GridBagLayoutHelper;
import edu.regis.jprobe.ui.helpers.JLabelHelper;
import edu.regis.jprobe.ui.helpers.JTextFieldHelper;

/**
 * @author Jim Di Vincenzo
 *
 */
public class UsagePanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    private String name;
    private Font bold;
    
    private JTextField[] poolName;  
    private JTextField[] usageCommitted;
    private JTextField[] usageInit;
    private JTextField[] usageMax;
    private JTextField[] usageUsed;
    private JTextField[] reclaimed;
    
    private String[] poolNames;
    private boolean afterUsage;
    
    /**
     * CTOR:
     * @param usage MemoryPool usage
     * @param name panel name
     * @param poolNames Pool Names
     * @param afterUsage if null, this is an before GC usage Panel
     */
    public UsagePanel(Map<String, MemoryUsage> usage, 
            String name, String[] poolNames, 
            Map<String, MemoryUsage> afterUsage) {
        this.name = name;
        this.poolNames = poolNames;
        this.afterUsage = (afterUsage != null);
        
        setBorder(new TitledBorder(name));
        bold = this.getFont().deriveFont(Font.BOLD);
        
        int len = poolNames.length;
        
        poolName = new JTextField[len];
        usageCommitted = new JTextField[len];
        usageInit = new JTextField[len];
        usageMax = new JTextField[len];
        usageUsed = new JTextField[len];
        reclaimed = new JTextField[len];
         
        buildPanel(usage);
        update(usage, afterUsage);
    }
    /**
     * 
     * @param usageMap
     */
    private void buildPanel(Map<String, MemoryUsage> usageMap) {
        
        /*
         * Helpers
         */
        JTextFieldHelper textHelper = new JTextFieldHelper();
        textHelper.setDefaultFont(bold);
        JLabelHelper labelHelper = new JLabelHelper();
        labelHelper.setDefaultHorizontalAlignment(JLabel.RIGHT);
        labelHelper.setDefaultForeground(Color.GRAY);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1,1,1,1);
        gbc.fill = GridBagConstraints.BOTH;
        GridBagLayoutHelper gbHelper = new  GridBagLayoutHelper(this, gbc);
        
        for (int i = 0; i < poolNames.length; i++) {
        
            JLabel lbl9 = labelHelper.newLabel("Pool");
            gbHelper.addColumn(lbl9, 1, 1, .1, 1.0);
            poolName[i] = textHelper.newTextField("Memory Pool Name");
            poolName[i].setText(poolNames[i]);
            gbHelper.addColumn(poolName[i], 1, 1, .1, 1.0);
            
            JLabel lbl10 = labelHelper.newLabel("Committed");
            gbHelper.addColumn(lbl10, 1, 1, .1, 1.0);
            usageCommitted[i] = textHelper.newTextField("Committed Bytes");
            usageCommitted[i].setText("N/A");
            gbHelper.addColumn(usageCommitted[i], 1, 1, .1, 1.0);
            
            JLabel lbl11 = labelHelper.newLabel("Initial");
            gbHelper.addColumn(lbl11, 1, 1, .1, 1.0);
            usageInit[i] = textHelper.newTextField("Initial Bytes");
            usageInit[i].setText("N/A");
            gbHelper.addColumn(usageInit[i], 1, 1, .1, 1.0);
            
            JLabel lbl12 = labelHelper.newLabel("Maximum");
            gbHelper.addColumn(lbl12, 1, 1, .1, 1.0);
            usageMax[i] = textHelper.newTextField("Maximum Bytes");
            usageMax[i].setText("N/A");
            gbHelper.addColumn(usageMax[i], 1, 1, .1, 1.0);
            
            JLabel lbl13 = labelHelper.newLabel("Used");
            gbHelper.addColumn(lbl13, 1, 1, .1, 1.0);
            usageUsed[i] = textHelper.newTextField("Used Bytes");
            usageUsed[i].setText("N/A");
            gbHelper.addColumn(usageUsed[i], 1, 1, .1, 1.0);
            
            if (afterUsage) {
                JLabel lbl14 = labelHelper.newLabel("Difference");
                
                gbHelper.addColumn(lbl14, 1, 1, .1, 1.0);
                reclaimed[i] = textHelper.newTextField("Bytes Reclaimed after GC");
                reclaimed[i].setText("N/A");
                gbHelper.addColumn(reclaimed[i], 1, 1, .1, 1.0);
            } 
            gbHelper.newRow();
        }
    }
    
    /**
     * Update the panel
     * @param usageMap MemoryUsage data
     * @param beforeUsageMap MemoryUsage data
     */
    public void update(Map<String, MemoryUsage> usageMap, Map<String, MemoryUsage> beforeUsageMap) {
       
        for (int i = 0; i < poolNames.length; i++) {
            
            MemoryUsage usage = usageMap.get(poolNames[i]);
            MemoryUsage before = null;
            
            if (afterUsage) {
                before = beforeUsageMap.get(poolNames[i]);
            }
            if (usage != null) {
                usageCommitted[i].setText(Utilities.formatBytes(usage.getCommitted()));
                usageInit[i].setText(Utilities.formatBytes(usage.getInit()));
                usageMax[i].setText(Utilities.formatBytes(usage.getMax()));
                usageUsed[i].setText(Utilities.formatBytes(usage.getUsed()));
                if (afterUsage && before != null) {
                    reclaimed[i].setText(Utilities.formatBytes(
                            before.getUsed() - usage.getUsed()));   
                }
            }
        }
        
        
        
    }
    /**
     * 
     * @return the name of this panel
     */
    public String getPanelName() {
        
        return name;
        
    }

}
