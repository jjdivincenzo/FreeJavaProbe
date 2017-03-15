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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.GarbageCollectorProperties;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.ui.helpers.GridBagLayoutHelper;

/**
 * @author jdivinc
 *
 */
public class GarbageCollectorsPanel extends PerformancePanel implements IPerformancePanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private boolean panelBuilt = false;
    private GridBagLayoutHelper gbHelper;
    private GridBagConstraints gbc;
    
    private Map<String, GarbageCollectorPane> panes = new HashMap<String, GarbageCollectorPane>();
    
    public GarbageCollectorsPanel() {
        
        this.setBorder(new TitledBorder( new EtchedBorder(), "Garbage Collectors"));
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(1,1,1,1);
        gbc.fill = GridBagConstraints.BOTH;
        gbHelper = new  GridBagLayoutHelper(this, gbc);
        
        
    }

    @Override
    public void update(ProbeResponse res) {
        
        if (!panelBuilt) {
            buildPanel(res);
        }
        
        List<GarbageCollectorProperties> props = res.getGarbageCollectors();
        
        if (props == null) {
            return;
        }
        for (GarbageCollectorProperties prop : props) {
            GarbageCollectorPane pane = panes.get(prop.getName());
            
            if (pane != null) {
                pane.update(prop);
            }
        }
        
    }
    
    public void buildPanel(ProbeResponse res) {
        
        
        List<GarbageCollectorProperties> props = res.getGarbageCollectors();
        
        
        if (props != null) {
            for (GarbageCollectorProperties prop : props) {
                GarbageCollectorPane pane = new GarbageCollectorPane(prop, res.getStartupTime());
                panes.put(prop.getName(), pane);
                gbHelper.addColumn(pane, 1, 1, 1.0, 1.0);
                gbHelper.newRow();
                
            }
        
            panelBuilt = true;
        }
    }


    public void resetPanel() {
        
        panelBuilt = false;
        super.removeAll();
    }
    
    /**
     * @return Returns the panelBuilt.
     */
    public boolean isPanelBuilt() {
        return panelBuilt;
    }

}
