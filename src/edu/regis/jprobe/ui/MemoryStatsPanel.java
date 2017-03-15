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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.regis.jprobe.model.GarbageCollectorData;
import edu.regis.jprobe.model.MemoryPoolData;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.Utilities;
import edu.regis.jprobe.ui.helpers.JProgressBarHelper;

/**
 * @author jdivince
 *
 * This class is repsonsible for building and updating the Memory Statistics Panel that
 * will be housed in a tabbed pane in the main UI.
 */
public class MemoryStatsPanel extends PerformancePanel implements IPerformancePanel {
	
    private static final long serialVersionUID = 1L;
	//private JPanel mstats;
	private boolean panelBuilt = false;
	private JTextField collectorName[];
	private JTextField collectorCount[];
	private JTextField collectorTime[];
	private JTextField collectorAverage[];
	private JTextField poolName[];
	private JTextField poolType[];
	private JTextField poolCurrentUsage[];
	private JTextField poolPeakUsage[];
	//private JTextField poolCollectionUsage[];
	private JTextField osTotalPMemSize;
	private JTextField osFreePMemSize;
	private JTextField osTotalSwapSize;
	private JTextField osFreeSwapSize;
	private JTextField osCommitedMemSize;
	private JTextField memPageFaults;
	private JTextField memWSSize;
	private JTextField memPeakWSSize;
	private JTextField memPageFileUsage;
	private JTextField memPeakPageFileUsage;
	private JTextField memPrivateBytes;
	private JTextField processID;
	private JProgressBar memPageFaultsDelta;
	private JProgressBar freePhysical;
	private JProgressBar freeSwap;
	private UIOptions options;

	private static final double LABEL_WEIGHT = 0.4;
	private static final double VALUE_WEIGHT = 0.5;
	
	private int maxProgressValue = 100;
	private long lastPageFaultValue = 0;
	private long observations = 0;
	private long accumulatedPageFaults = 0;
	
	private String poolDescription[][] = {
			{"Code Cache" ,"Contains memory used for compilation and storage of native code"},
			{"Eden Space", "Pool from which memory is initially allocated for most objects"},
			{"Survivor Space", "Pool containing objects that have survived Eden space garbage collection"}, 
			{"Tenured Gen", "Pool containing long-lived objects"},
			{"Perm Gen","Contains reflective data of the JVM itself, including class and memory objects"},
			{"Perm Gen [shared-ro]", "Read-only reflective data"},
			{"Perm Gen [shared-rw]", "Read-write reflective data"},
			{"CMS Old Gen", "Pool containing long-lived objects - incremental GC in use"},
			{"CMS Perm Gen","Contains reflective data of the JVM itself, including class and memory objects - incremental GC in use"}
			};
		
	private String gcDescription[][] = {
			{"Copy", "A minor collection runs relatively quickly and involves moving live data around the heap in the presence of running threads."}, 
			{"MarkSweepCompact", "A major collection is a much more intrusive garbage collection that suspends all execution threads while it completes its task."},
			{"ConcurrentMarkSweep", "A major collection is a much more intrusive garbage collection but does not suspend threads, it is specified by -Xincgc option for incremental garbage collection."}
			};
		

	private Color seriesColor[] = { Color.red, Color.blue, Color.green,
			Color.yellow, Color.magenta, Color.cyan,
			Color.pink, Color.black, Color.orange,
			Color.darkGray,	Color.gray, Color.lightGray};
	
	public MemoryStatsPanel(UIOptions options) {
	    this.options = options;
		this.setBorder(new TitledBorder( new EtchedBorder(), "Memory Statistics"));
		this.setLayout(new GridBagLayout());
		
	}
	
	public void buildPanel(ProbeResponse res) {
		
		//mstats = new JPanel();
		//Dimension buttonSize = new Dimension(30,40);
		Dimension headingSize = new Dimension(50,30);
		Dimension largeTextSize = new Dimension(100,30);
		//Font myfont = new Font("Courier New",Font.BOLD,12);		
	    Color lblColor = Color.GRAY;
	    JProgressBarHelper jpbh = new JProgressBarHelper();
	    
		JPanel p2 = new JPanel();
 		p2.setLayout(new  GridBagLayout());
 		p2.setBorder(new TitledBorder( new EtchedBorder(), "Garbage Collectors"));
 		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(1,1,1,1);
		c2.fill = GridBagConstraints.BOTH;
		
		JPanel p3 = new JPanel();
 		p3.setLayout(new  GridBagLayout());
 		p3.setBorder(new TitledBorder( new EtchedBorder(), "Memory Pool Usage"));
 		GridBagConstraints c3 = new GridBagConstraints();
		c3.insets= new Insets(1,1,1,1);
		c3.fill = GridBagConstraints.BOTH;
		
		Font fontTF = p2.getFont();
		fontTF = fontTF.deriveFont(Font.BOLD);
				
		int gcrows = res.getGCDataSize();
		int poolRows = res.getPoolDataSize();
		
		collectorName = new JTextField[gcrows];
		collectorCount = new JTextField[gcrows];
		collectorTime = new JTextField[gcrows];
		collectorAverage = new JTextField[gcrows];
		
		poolName = new JTextField[poolRows];
		poolType = new JTextField[poolRows];
		poolCurrentUsage = new JTextField[poolRows];
		poolPeakUsage = new JTextField[poolRows];
				
		JLabel ln = new JLabel("Collector Name"); 
		JLabel lc = new JLabel("Collection Count");
		JLabel lt = new JLabel("Collection Time");
		JLabel la = new JLabel("Average GC Time");
		ln.setForeground(lblColor);
		ln.setPreferredSize(headingSize);
		ln.setHorizontalAlignment(JLabel.CENTER);
		lc.setForeground(lblColor);
		lc.setPreferredSize(headingSize);
		lc.setHorizontalAlignment(JLabel.CENTER);
		lt.setForeground(lblColor);
		lt.setPreferredSize(headingSize);
		lt.setHorizontalAlignment(JLabel.CENTER);
		la.setForeground(lblColor);
		la.setPreferredSize(headingSize);
		la.setHorizontalAlignment(JLabel.CENTER);
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx = VALUE_WEIGHT;
 		c2.gridx=0;
		c2.gridy=0;
		p2.add(ln,c2);
		c2.gridx=1;
		p2.add(lc,c2);
		c2.gridx=2;
		p2.add(lt,c2);
		c2.gridx=3;
		p2.add(la,c2);
		
		for (int i = 0; i < gcrows; i++ ) {
			GarbageCollectorData gcd = res.getGCData(i);
			
			collectorName[i] = new JTextField();
			c2.weightx=VALUE_WEIGHT;
			c2.gridx=0;
			c2.gridy=i+1; 
			c2.gridheight=1;
			collectorName[i].setEditable(false);
			collectorName[i].setPreferredSize(largeTextSize);
			collectorName[i].setFont(fontTF);
			collectorName[i].setText(gcd.getName());
			collectorName[i].setToolTipText(getGCDescription(gcd.getName()));
			p2.add(collectorName[i],c2);
			
			collectorCount[i] = new JTextField();
			c2.weightx=VALUE_WEIGHT;
			c2.gridx=1;
			c2.gridy=i+1; 
			c2.gridheight=1;
			collectorCount[i].setEditable(false);
			collectorCount[i].setPreferredSize(largeTextSize);
			collectorCount[i].setFont(fontTF);
			collectorCount[i].setText(Utilities.format(gcd.getCount()));
			p2.add(collectorCount[i],c2);
			
			collectorTime[i] = new JTextField();
			c2.weightx=VALUE_WEIGHT;
			c2.gridx=2;
			c2.gridy=i+1; 
			c2.gridheight=1;
			collectorTime[i].setEditable(false);
			collectorTime[i].setPreferredSize(largeTextSize);
			collectorTime[i].setFont(fontTF);
			collectorTime[i].setText(Utilities.format(gcd.getTime()) + "ms");
			p2.add(collectorTime[i],c2);
			
			collectorAverage[i] = new JTextField();
			c2.weightx=VALUE_WEIGHT;
			c2.gridx=3;
			c2.gridy=i+1; 
			c2.gridheight=1;
			collectorAverage[i].setEditable(false);
			collectorAverage[i].setPreferredSize(largeTextSize);
			collectorAverage[i].setFont(fontTF);
			if (gcd.getCount() > 0) {
				collectorAverage[i].setText(
						Utilities.format(gcd.getTime() / gcd.getCount()) + "ms");
			} else {
				collectorAverage[i].setText("0ms");
			}
			p2.add(collectorAverage[i],c2);
			
		}
		
		//Column Labels
		JLabel pn = new JLabel("Pool Name");
		JLabel pt = new JLabel("Type");
		JLabel pu = new JLabel("Current Usage");
		JLabel pp = new JLabel("Peak Usage");
		pn.setForeground(lblColor);
		pn.setPreferredSize(headingSize);
		pn.setHorizontalAlignment(JLabel.CENTER);
		pt.setForeground(lblColor);
		pt.setPreferredSize(headingSize);
		pt.setHorizontalAlignment(JLabel.CENTER);
		pu.setForeground(lblColor);
		pu.setPreferredSize(headingSize);
		pu.setHorizontalAlignment(JLabel.CENTER);
		pp.setForeground(lblColor);
		pp.setPreferredSize(headingSize);
		pp.setHorizontalAlignment(JLabel.CENTER);
		c3.gridwidth=1;
		c3.gridheight=1;
		c3.weightx = VALUE_WEIGHT;
 		c3.gridx=0;
		c3.gridy=0;
		p3.add(pn,c3);
		c3.gridx=1;
		p3.add(pt,c3);
		c3.gridx=2;
		p3.add(pu,c3);
		c3.gridx=3;
		p3.add(pp,c3);
		
		for (int i = 0; i < poolRows; i++ ) {
			Color color;
			color = (i < seriesColor.length ?  seriesColor[i]: Color.BLACK);
			MemoryPoolData mpd = res.getPoolData(i);
			c3.gridwidth=1;
			c3.gridheight=1;
			poolName[i] = new JTextField();
			c3.weightx=VALUE_WEIGHT;
			c3.gridx=0;
			c3.gridy=i+1;
			c3.gridheight=1;
			poolName[i].setEditable(false);
			poolName[i].setPreferredSize(largeTextSize);
			poolName[i].setFont(fontTF);
			poolName[i].setBackground(color);
			poolName[i].setForeground(Utilities.invertColor(color));
			poolName[i].setText(mpd.getName());
			poolName[i].setToolTipText(getPoolDescription(mpd.getName()));
			p3.add(poolName[i],c3);
			
			poolType[i] = new JTextField();
			c3.weightx=VALUE_WEIGHT;
			c3.gridx=1;
			c3.gridy=i+1; 
			c3.gridheight=1;
			poolType[i].setEditable(false);
			poolType[i].setPreferredSize(largeTextSize);
			poolType[i].setFont(fontTF);
			poolType[i].setText(mpd.getType());
			poolType[i].setToolTipText(mpd.getType());
			p3.add(poolType[i],c3);
			
			poolCurrentUsage[i] = new JTextField();
			c3.weightx=VALUE_WEIGHT;
			c3.gridx=2;
			c3.gridy=i+1; 
			c3.gridheight=1;
			poolCurrentUsage[i].setEditable(false);
			poolCurrentUsage[i].setPreferredSize(largeTextSize);
			poolCurrentUsage[i].setFont(fontTF);
			poolCurrentUsage[i].setText(Utilities.format(mpd.getCurrentUsage()));
			p3.add(poolCurrentUsage[i],c3);
			
			poolPeakUsage[i] = new JTextField();
			c3.weightx=VALUE_WEIGHT;
			c3.gridx=3;
			c3.gridy=i+1; 
			c3.gridheight=1;
			poolPeakUsage[i].setEditable(false);
			poolPeakUsage[i].setPreferredSize(largeTextSize);
			poolPeakUsage[i].setFont(fontTF);
			poolPeakUsage[i].setText(Utilities.format(mpd.getPeakUsage()));
			p3.add(poolPeakUsage[i],c3);
			
		}
		
		osTotalPMemSize = new JTextField();
		osFreePMemSize = new JTextField();
		osTotalSwapSize = new JTextField();
		osFreeSwapSize = new JTextField();
		osCommitedMemSize = new JTextField();
		JTextField osName = new JTextField();
		
		osTotalPMemSize.setEditable(false);
		osTotalPMemSize.setPreferredSize(largeTextSize);
		osTotalPMemSize.setFont(fontTF);
		osTotalPMemSize.setText(Utilities.format(res.getOsTotalPhysMemSize()));
		
		osFreePMemSize.setEditable(false);
		osFreePMemSize.setPreferredSize(largeTextSize);
		osFreePMemSize.setFont(fontTF);
		osFreePMemSize.setText(Utilities.format(res.getOsFreePhysMemSize()));
		
		osTotalSwapSize.setEditable(false);
		osTotalSwapSize.setPreferredSize(largeTextSize);
		osTotalSwapSize.setFont(fontTF);
		osTotalSwapSize.setText(Utilities.format(res.getOsTotoalSwapSpaceSize()));
		
		osFreeSwapSize.setEditable(false);
		osFreeSwapSize.setPreferredSize(largeTextSize);
		osFreeSwapSize.setFont(fontTF);
		osFreeSwapSize.setText(Utilities.format(res.getOsFreeSwapSpaceSize()));
		
		osCommitedMemSize.setEditable(false);
		osCommitedMemSize.setPreferredSize(largeTextSize);
		osCommitedMemSize.setFont(fontTF);
		osCommitedMemSize.setText(Utilities.format(res.getOsCommittedVirtMemSize()));
		
		osName.setEditable(false);
		osName.setPreferredSize(largeTextSize);
		osName.setFont(fontTF);
		osName.setText(res.getOsName());
		freePhysical = jpbh.newProgressBar("Physical Memory in Use");
		freeSwap = jpbh.newProgressBar("Swap File Space in Use");
		JLabel osnam = new JLabel("Operating System");
		JLabel ostms = new JLabel("Total Physical Memory");
		JLabel osfms = new JLabel("Free Physical Memory");
		JLabel ostss = new JLabel("Total Swap Space");
		JLabel osfss = new JLabel("Free Swap Space");
		JLabel oscms = new JLabel("Commited Virtual Memory");
		
		JLabel osfph = new JLabel("Physical Memory Used");
		JLabel osfsw = new JLabel("Swap File Space Used");
		
		osnam.setForeground(lblColor);
		osnam.setPreferredSize(headingSize);
		osnam.setHorizontalAlignment(JLabel.RIGHT);
		ostms.setForeground(lblColor);
		ostms.setPreferredSize(headingSize);
		ostms.setHorizontalAlignment(JLabel.RIGHT);
		osfms.setForeground(lblColor);
		osfms.setPreferredSize(headingSize);
		osfms.setHorizontalAlignment(JLabel.RIGHT);
		ostss.setForeground(lblColor);
		ostss.setPreferredSize(headingSize);
		ostss.setHorizontalAlignment(JLabel.RIGHT);
		osfss.setForeground(lblColor);
		osfss.setPreferredSize(headingSize);
		osfss.setHorizontalAlignment(JLabel.RIGHT);
		oscms.setForeground(lblColor);
		oscms.setPreferredSize(headingSize);
		oscms.setHorizontalAlignment(JLabel.RIGHT);
		osfph.setForeground(lblColor);
		osfph.setPreferredSize(headingSize);
		osfph.setHorizontalAlignment(JLabel.RIGHT);
		osfsw.setForeground(lblColor);
        osfsw.setPreferredSize(headingSize);
        osfsw.setHorizontalAlignment(JLabel.RIGHT);
		
		JPanel p4 = new JPanel();
 		p4.setLayout(new  GridBagLayout());
 		p4.setBorder(new TitledBorder( new EtchedBorder(), "OS System Memory"));
 		GridBagConstraints c4 = new GridBagConstraints();
		c4.insets= new Insets(1,1,1,1);
		c4.fill = GridBagConstraints.BOTH;
		int gridy = 0;
		
		c4.gridwidth=1;
		c4.gridheight=1;
		c4.weightx = LABEL_WEIGHT;
 		c4.gridx=0;
		c4.gridy=gridy++;
		p4.add(ostms,c4);
		c4.weightx = VALUE_WEIGHT;
 		c4.gridx=1;
		p4.add(osTotalPMemSize,c4);
		c4.weightx = LABEL_WEIGHT;
 		c4.gridx=2;
		p4.add(osfms,c4);
		c4.weightx = VALUE_WEIGHT;
 		c4.gridx=3;
		p4.add(osFreePMemSize,c4);
		
		c4.gridwidth=1;
        c4.gridheight=1;
        c4.weightx = LABEL_WEIGHT;
        c4.gridx=0;
        c4.gridy=gridy++;
        p4.add(osfph,c4);
        c4.weightx = VALUE_WEIGHT;
        c4.gridx=1;
        c4.gridwidth=3;
        p4.add(freePhysical,c4);
  
        
		c4.gridwidth=1;
		c4.gridheight=1;
		c4.weightx = LABEL_WEIGHT;
 		c4.gridx=0;
		c4.gridy=gridy++;
		p4.add(ostss,c4);
		c4.weightx = VALUE_WEIGHT;
 		c4.gridx=1;
		p4.add(osTotalSwapSize,c4);
		c4.weightx = LABEL_WEIGHT;
 		c4.gridx=2;
		p4.add(osfss,c4);
		c4.weightx = VALUE_WEIGHT;
 		c4.gridx=3;
		p4.add(osFreeSwapSize,c4);
		
	      
        c4.gridwidth=1;
        c4.gridheight=1;
        c4.weightx = LABEL_WEIGHT;
        c4.gridx=0;
        c4.gridy=gridy++;
        p4.add(osfsw,c4);
        c4.weightx = VALUE_WEIGHT;
        c4.gridx=1;
        c4.gridwidth=3;
        p4.add(freeSwap,c4);
		
		c4.gridwidth=1;
		c4.gridheight=1;
		c4.weightx = LABEL_WEIGHT;
 		c4.gridx=0;
		c4.gridy=gridy++;;
		p4.add(oscms,c4);
		c4.weightx = VALUE_WEIGHT;
 		c4.gridx=1;
		p4.add(osCommitedMemSize,c4);
		c4.weightx = LABEL_WEIGHT;
 		c4.gridx=2;
		p4.add(osnam,c4);
		c4.weightx = VALUE_WEIGHT;
 		c4.gridx=3;
		p4.add(osName,c4);
		
		//***
		memPageFaults = new JTextField();
		memWSSize = new JTextField();
		memPeakWSSize = new JTextField();
		memPageFileUsage = new JTextField();
		memPeakPageFileUsage = new JTextField();
		memPrivateBytes = new JTextField();
		processID = new JTextField();
		memPageFaultsDelta = new JProgressBar();
		
		memPageFaults.setEditable(false);
		memPageFaults.setPreferredSize(largeTextSize);
		memPageFaults.setFont(fontTF);
		memPageFaults.setText(Utilities.format(res.getPageFaults()));
		
		memWSSize .setEditable(false);
		memWSSize .setPreferredSize(largeTextSize);
		memWSSize .setFont(fontTF);
		memWSSize .setText(Utilities.format(res.getWorkingSetSize()));
		
		memPeakWSSize.setEditable(false);
		memPeakWSSize.setPreferredSize(largeTextSize);
		memPeakWSSize.setFont(fontTF);
		memPeakWSSize.setText(Utilities.format(res.getPeakWorkingSetSize()));
		
		memPageFileUsage.setEditable(false);
		memPageFileUsage.setPreferredSize(largeTextSize);
		memPageFileUsage.setFont(fontTF);
		memPageFileUsage.setText(Utilities.format(res.getPagefileUsage()));
		
		memPeakPageFileUsage.setEditable(false);
		memPeakPageFileUsage.setPreferredSize(largeTextSize);
		memPeakPageFileUsage.setFont(fontTF);
		memPeakPageFileUsage.setText(Utilities.format(res.getPeakPagefileUsage()));
		
		memPrivateBytes.setEditable(false);
		memPrivateBytes.setPreferredSize(largeTextSize);
		memPrivateBytes.setFont(fontTF);
		memPrivateBytes.setText(Utilities.format(res.getPrivateBytes()));
		processID.setEditable(false);
		processID.setPreferredSize(largeTextSize);
		processID.setFont(fontTF);
		processID.setText(Utilities.format(res.getPrivateBytes()));
		memPageFaultsDelta.setStringPainted(true);
		memPageFaultsDelta.setPreferredSize(largeTextSize);
		memPageFaultsDelta.setValue(0);
		memPageFaultsDelta.setMinimum(0);
		memPageFaultsDelta.setMaximum(maxProgressValue);
		Font boldFont = memPageFaultsDelta.getFont();
		boldFont = boldFont.deriveFont(Font.BOLD);
		memPageFaultsDelta.setFont(boldFont);
		memPageFaultsDelta.setForeground(new Color(128,128,128));
		memPageFaultsDelta.setString("0");
		
		JLabel mempf = new JLabel("Page Faults");
		JLabel memwss = new JLabel("Working Set Size");
		JLabel mempwss = new JLabel("Peak Working Set Size");
		JLabel mempfu = new JLabel("Page File Use");
		JLabel memppfu = new JLabel("Peak Page File Use");
		JLabel mempfd = new JLabel("Page Faults Delta");
		JLabel mempb = new JLabel("Private Bytes");
		JLabel procid = new JLabel("Process ID");
		
		mempf.setForeground(lblColor);
		mempf.setPreferredSize(headingSize);
		mempf.setHorizontalAlignment(JLabel.RIGHT);
		memwss.setForeground(lblColor);
		memwss.setPreferredSize(headingSize);
		memwss.setHorizontalAlignment(JLabel.RIGHT);
		mempwss.setForeground(lblColor);
		mempwss.setPreferredSize(headingSize);
		mempwss.setHorizontalAlignment(JLabel.RIGHT);
		mempfu.setForeground(lblColor);
		mempfu.setPreferredSize(headingSize);
		mempfu.setHorizontalAlignment(JLabel.RIGHT);
		memppfu.setForeground(lblColor);
		memppfu.setPreferredSize(headingSize);
		memppfu.setHorizontalAlignment(JLabel.RIGHT);
		mempfd.setForeground(lblColor);
		mempfd.setPreferredSize(headingSize);
		mempfd.setHorizontalAlignment(JLabel.RIGHT);
		mempb.setForeground(lblColor);
		mempb.setPreferredSize(headingSize);
		mempb.setHorizontalAlignment(JLabel.RIGHT);
		procid.setForeground(lblColor);
		procid.setPreferredSize(headingSize);
		procid.setHorizontalAlignment(JLabel.RIGHT);
		
		JPanel p5 = new JPanel();
 		p5.setLayout(new  GridBagLayout());
 		p5.setBorder(new TitledBorder( new EtchedBorder(), "Process Memory"));
 		GridBagConstraints c5 = new GridBagConstraints();
		c5.insets= new Insets(1,1,1,1);
		c5.fill = GridBagConstraints.BOTH;
		
		c5.gridwidth=1;
		c5.gridheight=1;
		c5.weightx = LABEL_WEIGHT;
 		c5.gridx=0;
		c5.gridy=0;
		p5.add(mempf,c5);
		c5.weightx = VALUE_WEIGHT;
 		c5.gridx=1;
		c5.gridy=0;
		p5.add(memPageFaults,c5);
		c5.weightx = LABEL_WEIGHT;
 		c5.gridx=2;
		c5.gridy=0;
		p5.add(mempfd,c5);
		c5.weightx = VALUE_WEIGHT;
 		c5.gridx=3;
		c5.gridy=0;
		//p5.add(memPrivateBytes,c5);
		p5.add(memPageFaultsDelta,c5);
		
		c5.gridwidth=1;
		c5.gridheight=1;
		c5.weightx = LABEL_WEIGHT;
 		c5.gridx=0;
		c5.gridy=1;
		p5.add(memwss,c5);
		c5.weightx = VALUE_WEIGHT;
 		c5.gridx=1;
		c5.gridy=1;
		p5.add(memWSSize,c5);
		c5.weightx = LABEL_WEIGHT;
 		c5.gridx=2;
		c5.gridy=1;
		p5.add(mempwss,c5);
		c5.weightx = VALUE_WEIGHT;
 		c5.gridx=3;
		c5.gridy=1;
		p5.add(memPeakWSSize,c5);
		
		c5.gridwidth=1;
		c5.gridheight=1;
		c5.weightx = LABEL_WEIGHT;
 		c5.gridx=0;
		c5.gridy=2;
		p5.add(mempfu,c5);
		c5.weightx = VALUE_WEIGHT;
 		c5.gridx=1;
		c5.gridy=2;
		p5.add(memPageFileUsage,c5);
		c5.weightx = LABEL_WEIGHT;
 		c5.gridx=2;
		c5.gridy=2;
		p5.add(memppfu,c5);
		c5.weightx = VALUE_WEIGHT;
 		c5.gridx=3;
		c5.gridy=2;
		p5.add(memPeakPageFileUsage,c5);
		
		c5.gridwidth=1;
		c5.gridheight=1;
		c5.weightx = LABEL_WEIGHT;
 		c5.gridx=0;
		c5.gridy=3;
		p5.add(mempb,c5);
		c5.weightx = VALUE_WEIGHT;
 		c5.gridx=1;
		c5.gridy=3;
		p5.add(memPrivateBytes,c5);
		c5.weightx = LABEL_WEIGHT;
 		c5.gridx=2;
		c5.gridy=3;
		p5.add(procid,c5);
		c5.weightx = VALUE_WEIGHT;
 		c5.gridx=3;
		c5.gridy=3;
		p5.add(processID,c5);
		//***
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets= new Insets(1,1,1,1);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridwidth=1;
		gbc.gridheight=1;
		gbc.weightx=1;
		gbc.weighty=.5;
		add(p2, gbc);
		gbc.gridx=0;
		gbc.gridy=1;
		gbc.gridwidth=1;
		gbc.gridheight=1;
		gbc.weightx=1;
		gbc.weighty=.7;
		add(p3, gbc);
		gbc.gridx=0;
		gbc.gridy=2;
		gbc.gridwidth=1;
		gbc.gridheight=1;
		gbc.weightx=1;
		gbc.weighty=.5;
		add(p4, gbc);
		gbc.gridx=0;
		gbc.gridy=3;
		gbc.gridwidth=1;
		gbc.gridheight=1;
		gbc.weightx=1;
		gbc.weighty=.5;
		if (res.isIoCountersAvailable()) add(p5, gbc);
		repaint(10);
		panelBuilt = true;
	}
	public void update(ProbeResponse res) {
		
		for (int i = 0; i < res.getPoolDataSize(); i++) {
			MemoryPoolData mpd = res.getPoolData(i);
			poolName[i].setText(mpd.getName());
			poolType[i].setText(mpd.getType());
			poolCurrentUsage[i].setText(Utilities.format(mpd.getCurrentUsage(), true));
			poolPeakUsage[i].setText(Utilities.format(mpd.getPeakUsage(), true));
	   	}
	    
	    for (int i = 0; i < res.getGCDataSize(); i++) {
	    	GarbageCollectorData gcd = res.getGCData(i);
	    	collectorName[i].setText(gcd.getName());
			collectorCount[i].setText(Utilities.format(gcd.getCount()));
			collectorTime[i].setText(Utilities.format(gcd.getTime()) + "ms");
			if (gcd.getCount() > 0) {
				collectorAverage[i].setText(
						Utilities.format((double)gcd.getTime() / (double) gcd.getCount(), 2) + "ms");
			} else {
				collectorAverage[i].setText("0ms");
			}
	    }
	    long usedPhysical = res.getOsTotalPhysMemSize() - res.getOsFreePhysMemSize();
	    long usedSwap = res.getOsTotoalSwapSpaceSize() - res.getOsFreeSwapSpaceSize();
	    int freePhysicalSize = (int) ( ((double)usedPhysical / (double) res.getOsTotalPhysMemSize()) * 100d);
	    
	    int freeSwapSize = (int) ( ((double)usedSwap / (double) res.getOsTotoalSwapSpaceSize()) * 100d);
	    
	    freePhysical.setValue(freePhysicalSize);
	    freeSwap.setValue(freeSwapSize);
	    
	    if (freePhysicalSize < options.getHeapThresholdOk()) {
	        freePhysical.setForeground(options.getHeapColorOk());
        }else if (freePhysicalSize < options.getHeapThresholdBad()) {
            freePhysical.setForeground(options.getHeapColorWarn());
        }else{
            freePhysical.setForeground(options.getHeapColorBad());
        }
	    
	    if (freeSwapSize < options.getHeapThresholdOk()) {
	        freeSwap.setForeground(options.getHeapColorOk());
        }else if (freeSwapSize < options.getHeapThresholdBad()) {
            freeSwap.setForeground(options.getHeapColorWarn());
        }else{
            freeSwap.setForeground(options.getHeapColorBad());
        }
	    
	    osTotalPMemSize.setText(Utilities.format(res.getOsTotalPhysMemSize(), true));
		osFreePMemSize.setText(Utilities.format(res.getOsFreePhysMemSize(), true));
		osTotalSwapSize.setText(Utilities.format(res.getOsTotoalSwapSpaceSize(), true));
		osFreeSwapSize.setText(Utilities.format(res.getOsFreeSwapSpaceSize(), true));
		osCommitedMemSize.setText(Utilities.format(res.getOsCommittedVirtMemSize(), true));
		
		if (!res.isIoCountersAvailable()) return;
		
		memPageFaults.setText(Utilities.format(res.getPageFaults()));
		memWSSize.setText(Utilities.format(res.getWorkingSetSize(), true));
		memPeakWSSize.setText(Utilities.format(res.getPeakWorkingSetSize(), true));
		memPageFileUsage.setText(Utilities.format(res.getPagefileUsage(), true));
		memPeakPageFileUsage.setText(Utilities.format(res.getPeakPagefileUsage(), true));
		memPrivateBytes.setText(Utilities.format(res.getPrivateBytes(), true));
		processID.setText(res.getProcessID() +"");
		
		long thisDelta = res.getPageFaults() - lastPageFaultValue;
		if (lastPageFaultValue == 0) thisDelta = 0;
		accumulatedPageFaults += thisDelta;
		observations++;
		lastPageFaultValue = res.getPageFaults();
		
		long ave = accumulatedPageFaults / observations;
		memPageFaultsDelta.setMaximum((int) (ave * 2));
		memPageFaultsDelta.setValue((int)thisDelta);
		memPageFaultsDelta.setString(new Long(thisDelta).toString());
		//System.out.println("Last=" + thisDelta + " Ave=" + ave + " Obs=" + observations);
	}
	
	protected String getPoolDescription(String name) {
		
		for (int i = 0; i < poolDescription.length; i++) {
			
			if (poolDescription[i][0].equalsIgnoreCase(name)) {
				return poolDescription[i][1];
			}
		}
		return "No Description";
	}
	protected String getGCDescription(String name) {
		
		for (int i = 0; i < gcDescription.length; i++) {
			
			if (gcDescription[i][0].equalsIgnoreCase(name)) {
				return gcDescription[i][1];
			}
		}
		return "No Description";
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
