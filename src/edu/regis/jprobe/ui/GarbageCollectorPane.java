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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.GCInfo;
import edu.regis.jprobe.model.GarbageCollectorProperties;
import edu.regis.jprobe.model.Utilities;
import edu.regis.jprobe.ui.helpers.GridBagLayoutHelper;
import edu.regis.jprobe.ui.helpers.JLabelHelper;
import edu.regis.jprobe.ui.helpers.JTextFieldHelper;

/**
 * @author Jim Di Vincenzo
 *
 */
public class GarbageCollectorPane extends JPanel{
    
    private static final long serialVersionUID = 1L;

    private Font bold;
    private String collectorName;
    
    private JTextField duration;
    private JTextField startTime;
    private JTextField endTime;
    private JTextField gcThreadCount;
    private JTextField id;
    private JTextField count;
    private JTextField time;
    private JTextField averageTime;
    private JTextField name;
    
    private UsagePanel usageBefore;
    private UsagePanel usageAfter;
    private long vmStartTime;
    
    /**
     * CTOR:
     * @param gcProps GarbageCollectorProperties
     */
    public GarbageCollectorPane(GarbageCollectorProperties gcProps, long vmStartTime) {
        this.vmStartTime = vmStartTime;
        bold = this.getFont().deriveFont(Font.BOLD);
        collectorName = gcProps.getName();
        TitledBorder border = new TitledBorder(collectorName);
        border.setTitleFont(bold);
        setBorder(border);
        buildPanel(gcProps);
        update(gcProps);
    }
    
    private void buildPanel(GarbageCollectorProperties gcProps) {
        
        GCInfo lastGC = gcProps.getLastGCInfo();
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
        
        JLabel lbl1 = labelHelper.newLabel("Collector Name");
        gbHelper.addColumn(lbl1, 1, 1, .1, 1.0);
        name = textHelper.newTextField("Collector Name");
        gbHelper.addColumn(name, 1, 1, .1, 1.0);
        
        JLabel lbl2 = labelHelper.newLabel("Collection Count");
        gbHelper.addColumn(lbl2, 1, 1, .1, 1.0);
        count = textHelper.newTextField("Collection Count");
        gbHelper.addColumn(count, 1, 1, .1, 1.0);
        
        JLabel lbl3 = labelHelper.newLabel("Collection Time");
        gbHelper.addColumn(lbl3, 1, 1, .1, 1.0);
        time = textHelper.newTextField("Collection Time in Milliseconds");
        gbHelper.addColumn(time, 1, 1, .1, 1.0);
        
        JLabel lbl4 = labelHelper.newLabel("Average Collection Time");
        gbHelper.addColumn(lbl4, 1, 1, .1, 1.0);
        averageTime = textHelper.newTextField("Average Collection Time in Milliseconds");
        gbHelper.addColumn(averageTime, 1, 1, .1, 1.0);
        gbHelper.newRow();
        
        JLabel lbl10 = labelHelper.newLabel("Last GC Id");
        gbHelper.addColumn(lbl10, 1, 1, .1, 1.0);
        id = textHelper.newTextField("Last GC Collection Id");
        id.setText("N/A");
        gbHelper.addColumn(id, 1, 1, .1, 1.0);
        
        JLabel lbl11 = labelHelper.newLabel("Last Thread Count");
        gbHelper.addColumn(lbl11, 1, 1, .1, 1.0);
        gcThreadCount = textHelper.newTextField("GC Threads Spawned");
        gcThreadCount.setText("N/A");
        gbHelper.addColumn(gcThreadCount, 1, 1, .1, 1.0);
        
        JLabel lbl12 = labelHelper.newLabel("Last Duration");
        gbHelper.addColumn(lbl12, 1, 1, .1, 1.0);
        duration = textHelper.newTextField("GC Duration in Milliseconds");
        duration.setText("N/A");
        gbHelper.addColumn(duration, 1, 1, .1, 1.0);
        
        JLabel lbl13 = labelHelper.newLabel("Last Start Time");
        gbHelper.addColumn(lbl13, 1, 1, .1, 1.0);
        startTime = textHelper.newTextField("Start Time of the Last Collection");
        startTime.setText("N/A");
        gbHelper.addColumn(startTime, 1, 1, .1, 1.0);
        
        JLabel lbl14 = labelHelper.newLabel("Last End Time");
        gbHelper.addColumn(lbl14, 1, 1, .1, 1.0);
        endTime = textHelper.newTextField("End Time of the Last Collection");
        endTime.setText("N/A");
        gbHelper.addColumn(endTime, 1, 1, .1, 1.0);
        gbHelper.newRow();
        
        
        usageBefore = new UsagePanel(lastGC.getUsageBefore(), "Before GC", gcProps.getPoolNames(), null);
        usageAfter = new UsagePanel(lastGC.getUsageAfter(), "After GC", gcProps.getPoolNames(), 
                lastGC.getUsageBefore());
        gbHelper.addColumn(usageBefore, 10, 1, .1, 1.0);
        gbHelper.newRow();
        gbHelper.addColumn(usageAfter, 10, 1, .2, 1.0);
        gbHelper.newRow();
        
    }

    /**
     * Update the panel
     * @param gcProps Garbage Collector data
     */
    public void update(GarbageCollectorProperties gcProps) {
        
        GCInfo lastGC = gcProps.getLastGCInfo();
        usageBefore.update(lastGC.getUsageBefore(), null);
        usageAfter.update(lastGC.getUsageAfter(), lastGC.getUsageBefore());
        
        double aveTime = (gcProps.getCollectionCount() == 0 ? 0.0 : 
            (double)gcProps.getCollectionTime() / (double)gcProps.getCollectionCount());
        
        if (gcProps.getCollectionCount() > 0) {
            id.setText(Utilities.format(lastGC.getId()));
            gcThreadCount.setText(Utilities.format(lastGC.getGcThreadCount()));
            duration.setText(Utilities.format(lastGC.getDuration()));
            if (lastGC.getStartTime() > 0) {
                startTime.setText(Utilities.formatTimeStamp(lastGC.getStartTime() + vmStartTime, "HH:mm:ss.SSS"));
                endTime.setText(Utilities.formatTimeStamp(lastGC.getEndTime() + vmStartTime, "HH:mm:ss.SSS"));
            } else {
                startTime.setText("N/A");
                endTime.setText("N/A");
            }
        }
        count.setText(Utilities.format(gcProps.getCollectionCount()));
        time.setText(Utilities.format(gcProps.getCollectionTime()));
        averageTime.setText(Utilities.format(aveTime,2));
        name.setText(gcProps.getName());
    }

}
