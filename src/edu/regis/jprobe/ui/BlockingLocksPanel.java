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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import edu.regis.jprobe.model.LockData;
import edu.regis.jprobe.model.LockStack;
import edu.regis.jprobe.model.LockStackTrace;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.ResponseMonitorInfo;
import edu.regis.jprobe.model.ResponseThreadData;
import edu.regis.jprobe.model.Utilities;


/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BlockingLocksPanel extends PerformancePanel implements IPerformancePanel {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JPanel detailPanel;
    private JPanel historyPanel;
    private JTabbedPane tabPane; 
    private JTextArea locksArea;
    private JTextArea stacksArea;
    private JTextField stackNumber;
    private JButton nextBtn;
    private JButton prevBtn;
    private LockTable lockTable;
    private LockDataModel lockModel;
    private JPanel tablePanel;
    private JPanel tracePanel;
	private StringBuilder sb = new StringBuilder();
	private Map<String, LockData> lockMap = new HashMap<String, LockData>();
	private Vector<LockData> locks = new Vector<LockData>();
	private LockData currentLockData;
	private int currentLockIndex = 0;
	private boolean debug = false;
	private TitledBorder stackBorder;
	private JScrollPane resultPane4;
	/**
	 * 	
	 * @param u
	 */
	public BlockingLocksPanel(JProbeClientFrame u) {
		
		Dimension hugeTextSize = new Dimension(250,80);
		setLayout(new  GridBagLayout());
		tabPane = new JTabbedPane(JTabbedPane.TOP);
		historyPanel = new JPanel();
		historyPanel.setLayout(new  GridBagLayout());
		historyPanel.setBorder(new EtchedBorder());
 		GridBagConstraints cc = new GridBagConstraints();
		cc.insets= new Insets(1,1,1,1);
		cc.fill = GridBagConstraints.BOTH;
		
	
		cc.gridx=0;
		cc.gridy=0;
		cc.gridwidth=1;
		cc.gridheight=1;
		cc.weightx = 1;
		cc.weighty = 1;

		locksArea = new JTextArea();
		locksArea.setEditable(false);
		locksArea.setCaretPosition(0);
		JScrollPane resultPane3 = new JScrollPane( locksArea , 
	      		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPane3.setPreferredSize(hugeTextSize);
		historyPanel.add(resultPane3, cc);
		

		
		lockModel = new LockDataModel();
        lockTable = new LockTable(lockModel);
        lockTable.setAutoCreateColumnsFromModel(false);
        lockTable.setColumnModel(new DefaultTableColumnModel());
        JScrollPane sp = new JScrollPane();
       
        sp.setWheelScrollingEnabled(true);
        for (int k = 0; k < lockModel.getColumnCount(); k++) {
            
            DefaultTableCellRenderer renderer = new ThreadTableCellRenderer();
            renderer.setHorizontalAlignment(lockModel.cdata[k].alignment);
            TableColumn column = new TableColumn(k, lockModel.cdata[k].length, renderer, null);
            column.setHeaderRenderer(createDefaultRenderer());
            lockTable.addColumn(column);
        }
        ;
        JTableHeader header = lockTable.getTableHeader();
        header.setUpdateTableInRealTime(true);
        header.setReorderingAllowed(false);
        sp.getViewport().add(lockTable);
        
        sp.setBorder(new EtchedBorder());
        
        tracePanel = new JPanel();
        stackBorder = new TitledBorder("Stack Traces");
        tracePanel.setBorder(stackBorder);
        tracePanel.setLayout(new GridBagLayout());
        
        GridBagConstraints cc2 = new GridBagConstraints();
        cc2.insets= new Insets(1,1,1,1);
        cc2.fill = GridBagConstraints.BOTH;
        
    
        cc2.gridx=0;
        cc2.gridy=0;
        cc2.gridwidth=1;
        cc2.gridheight=1;
        cc2.weightx = 1;
        cc2.weighty = 0;
        JPanel controls = new JPanel();
        controls.setBorder(new EtchedBorder());
        controls.setLayout(new GridLayout(1,8,1,1));
        JLabel l1 = new JLabel("Stack Trace Instance");
        l1.setHorizontalAlignment(JLabel.RIGHT);
        controls.add(l1);
        
        stackNumber = new JTextField();
        stackNumber.setHorizontalAlignment(JTextField.LEADING);
        Font bold = stackNumber.getFont().deriveFont(Font.BOLD);
        stackNumber.setFont(bold);
        stackNumber.setEditable(false);
        controls.add(stackNumber);
        controls.add(new JLabel(" "));
        nextBtn = new JButton("Next >>");
        nextBtn.setToolTipText("Next Stack Trace for This Lock");
        nextBtn.setEnabled(false);
        controls.add(nextBtn);
        controls.add(new JLabel(" "));
        prevBtn = new JButton("<< Prev");
        prevBtn.setEnabled(false);
        prevBtn.setToolTipText("Previous Stack Trace for This Lock");
        controls.add(prevBtn);
        controls.add(new JLabel(" "));
        tracePanel.add(controls, cc2);
        cc2.weighty = 1;
        cc2.gridy=1;
        cc2.gridheight=5;
        stacksArea = new JTextArea();
        stacksArea.setEditable(false);
        resultPane4 = new JScrollPane( stacksArea , 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resultPane4.setPreferredSize(hugeTextSize);
        
        resultPane4.setBorder(new TitledBorder("Stack Trace"));
        
        tracePanel.add(resultPane4, cc2);
       
        tablePanel = new JPanel();
        tablePanel.setBorder(new TitledBorder("Locks"));
        tablePanel.setLayout(new GridLayout(1,1,1,1));
        tablePanel.add(sp);
        detailPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets= new Insets(1,1,1,1);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.gridwidth=1;
        gbc.gridheight=1;
        gbc.weightx = 1;
        gbc.weighty = 0.3;


        detailPanel.setLayout(layout);
        detailPanel.add(tablePanel, gbc);
        gbc.gridx=0;
        gbc.gridy=1;
        gbc.gridwidth=1;
        gbc.gridheight=3;
        gbc.weightx = 1;
        gbc.weighty = 0.7;
        detailPanel.add(tracePanel, gbc);
        
        /*detailPanel.setLayout(new GridLayout(2,1,1,1));
        detailPanel.add(tablePanel);
        detailPanel.add(tracePanel);*/
		tabPane.addTab("Detail Summary", detailPanel);
		tabPane.addTab("History", historyPanel);
		
		MouseListener popupListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int button = e.getButton();
                if (button == MouseEvent.BUTTON3) {
                    showPopupMenu();
                    
                }
            }
        };
        tabPane.addMouseListener(popupListener);
        locksArea.addMouseListener(popupListener);
        detailPanel.addMouseListener(popupListener);
		MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedRow = lockTable.getSelectedRow();
                
                    if (selectedRow >= 0 && locks.size() > 0 ) {
                        
                         currentLockData = locks.get(selectedRow);
                         if (currentLockData != null) {
                             currentLockIndex = 0;
                             updateLockStack(currentLockData);
                             
                         }
                    }
                    
                 
            }
        };
        ActionListener buttonListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (currentLockData == null) {
                    return;
                }
                if (source == nextBtn) {
                    currentLockIndex++;
                    updateLockStack(currentLockData);
                } else {
                    currentLockIndex--;
                    updateLockStack(currentLockData);
                }
                
            }
            
        };
        nextBtn.addActionListener(buttonListener);
        prevBtn.addActionListener(buttonListener);
       lockTable.addMouseListener(mouseListener);
       stacksArea.setFont(stacksArea.getFont().deriveFont(Font.BOLD));
       
       this.addComponentListener(new ComponentAdapter() {
           public void componentResized(ComponentEvent e) {
               Dimension d = tabPane.getSize();
               tablePanel.setPreferredSize(new Dimension(d.width, (int)(d.height * .3)));
               tracePanel.setPreferredSize(new Dimension(d.width, (int)(d.height * .7)));
           }
       });
       cc.gridy=0;
       add(tabPane, cc);
		
	}
	public void update(ProbeResponse res) {
		
	    if (sb.length() == 0) {
	        locksArea.setText("*** No Monitor Locks have been Observed ***");
	    }
		if (res.getNumberOfBlockedThreads() == 0) {
		    return;
		}
		
		
		sb.append("Blocking Threads from Observation at ");
		sb.append(Utilities.getDateTime());
		sb.append("\n");
		Vector<ResponseThreadData> allThreads = res.getAllThreadData();
		Map<Long, ResponseThreadData> threadMap = new HashMap<Long, ResponseThreadData>();
		
		for (ResponseThreadData thd : allThreads) {
		    threadMap.put(thd.getThreadId(), thd);
		}
		
		int threads = res.getNumberOfThreadInfo();
		
		for (int i = 0; i < threads; i++) {
		    ResponseThreadData td = res.getThreadInfo(i);
		    if (td.isThreadBlocked()) {
		        StackTraceElement[] ste = td.getCurrentStackFrame();
		        String frame = "";
		        if (ste != null && ste.length > 0) {
		            sb.append("\tCurrent Frame: ");
		            frame = ste[0].toString();
		            sb.append(frame);
		            sb.append("\n");
		        }
		        sb.append("\t\tBlocked Thread: ");
		        sb.append(td.getThreadName());
		        sb.append(":[");
		        sb.append(td.getThreadId());
		        sb.append("]\n");
		        sb.append("\t\tLock Name: ");
                sb.append(td.getLockName());
		        sb.append("\n\t\tLock Owner: ");
		        sb.append(td.getLockOwner());
		        sb.append("\n\t\tOwning Thread: ");
		        sb.append(td.getLockOwningThread());
		        sb.append("\n\n\t\tBlocking Thread Stack Trace: \n");
		        sb.append(formatStackTrace(td.getCurrentStackFrame(), 3));
		        if (td.getMonitorInfo() != null) {

                    sb.append("\n\t\tBlocking Thread Held Monitors: \n");
                    for (ResponseMonitorInfo rmi : td.getMonitorInfo()) {
                        String lockName = rmi.getClassName() + "@" + Integer.toHexString(rmi.getIdentityHashCode());
                        sb.append("\t\t\tLock: ").append(lockName)
                        .append("\n");
                        sb.append("\t\t\tLocking Frame: ").append(rmi.getLockingStackFrame()).append("\n");
                        
                    }
                }
                if (td.getLockInfo() != null) {
                    sb.append("\n\t\tBlocking Thread Held Locks: \n");
                    for (ResponseMonitorInfo rmi : td.getLockInfo()) {
                        String lockName = rmi.getClassName() + "@" + Integer.toHexString(rmi.getIdentityHashCode());
                        sb.append("\t\t\tLock: ").append(lockName)
                        .append("\n");
                    }
                }
		        
		        
		        
		        ResponseThreadData owning = threadMap.get(td.getLockOwningThread());
		        if (owning != null) {
		            sb.append("\n\t\tOwning Thread Stack Trace: \n");
	                sb.append(formatStackTrace(owning.getCurrentStackFrame(), 3));
	                if (owning.getMonitorInfo() != null) {
	                    sb.append("\n\t\tOwning Thread Held Monitors: \n");
	                    for (ResponseMonitorInfo rmi : owning.getMonitorInfo()) {
	                        sb.append("\t\t\tLock: ").append(rmi.getClassName())
	                        .append("@").append(Integer.toHexString(rmi.getIdentityHashCode())).append("\n");
	                        sb.append("\t\t\tLocking Frame: ").append(rmi.getLockingStackFrame()).append("\n");
	                        
	                    }
	                }
	                if (owning.getLockInfo() != null) {
                        sb.append("\n\t\tOwning Thread Held Locks: \n");
                        for (ResponseMonitorInfo rmi : owning.getLockInfo()) {
                            sb.append("\t\t\tLock: ").append(rmi.getClassName())
                            .append("@").append(Integer.toHexString(rmi.getIdentityHashCode())).append("\n");
                        }
                    }
		        }
		        sb.append("\n\n");
		        LockData ld = lockMap.get(td.getLockName());
		        
		        if (ld != null) {
		            ld.incrementLockCount();
		            ld.addStack(new LockStackTrace(ste));
		            
		        } else {
		            ld = new LockData(td.getLockName());
		            ld.addStack(new LockStackTrace(ste));
		            if (debug) {
		                Exception e1 = new Exception();
		                Exception e2 = new Exception();
		                ld.addStack(new LockStackTrace(e1.getStackTrace()));
		                ld.addStack(new LockStackTrace(e2.getStackTrace()));
		            }
		            lockMap.put(td.getLockName(), ld);
		        }
		    }
		}
		
		locksArea.setText(sb.toString());
		
		Set<String> keys = lockMap.keySet();             
        Iterator<String> iter = keys.iterator();        
        locks.clear();
        while (iter.hasNext()) {
            String key = iter.next();
            locks.add(lockMap.get(key));
    
        }
        
        lockModel.update(locks);
        lockTable.clearRenderer();
	}
	
	public void resetPanel() {
		locksArea.setText("");
	}
	public boolean isPanelBuilt() {
	    return true;
	}
	private void updateLockStack(LockData ld) {
	    stackBorder.setTitle("Stack Traces for (" + currentLockData.getLockName() +")");
	    prevBtn.setEnabled(true);
        nextBtn.setEnabled(true);
	    List<LockStack> stacks = ld.getStacks();
	    int size = stacks.size();
	    if (currentLockIndex >= size || currentLockIndex < 0) {
	        currentLockIndex = 0;
	    }
        LockStack stk = stacks.get(currentLockIndex);
        if (stk == null) {
            return;
        }
        stackNumber.setText((currentLockIndex + 1) + " of " + size);
        LockStackTrace frame = stk.getStackFrame();
        stacksArea.setText(frame.toString());
        
        stacksArea.setCaretPosition(0);
        
        if (size == 1) {
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
        } else {
            if (currentLockIndex + 1 < size) {
                nextBtn.setEnabled(true);
            } else {
                nextBtn.setEnabled(false);
            }
            if (currentLockIndex < 1) {
                prevBtn.setEnabled(false);
            } else {
                prevBtn.setEnabled(true);
            }
        }
        this.repaint();
	}
	private String formatStackTrace(StackTraceElement[] ste, int tabs) {
	    
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < tabs; i++) {
	        sb.append("\t");
	    }
	    
	    String tbs = sb.toString();
	    sb.setLength(0);
	    
	    for (int i = 0; i < ste.length; i++) {
	        sb.append(tbs);
	        sb.append("(");
	        sb.append(i);
	        sb.append(") - ");
	        sb.append(ste[i]);
	        sb.append("\n");
	    }
	    
	    
	    return sb.toString();
	}
	   protected void showPopupMenu() {
	        
	        final JPopupMenu popupMenu = new JPopupMenu("Options");
	        
	        JMenuItem popupClear = new JCheckBoxMenuItem("Clear History");
	        popupClear.setMnemonic('K');
	        popupClear.setAccelerator(KeyStroke.getKeyStroke(
	                KeyEvent.VK_K, ActionEvent.CTRL_MASK));
	        popupClear.setEnabled(true);
	        popupMenu.add(popupClear);
	        popupClear.addActionListener(new ActionListener() {
	            
	            public void actionPerformed (ActionEvent e)
	            {
	                sb.setLength(0);
	                locksArea.setText("");
	            }
	        });
	        
	        
	        
	        JMenuItem popupClearSummary = new JCheckBoxMenuItem("Clear Summary");
	        popupClearSummary.setMnemonic('S');
	        popupClearSummary.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	        popupClearSummary.setEnabled(true);
	        popupMenu.add(popupClearSummary);
	        popupClearSummary.addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent e) {
                    
                    lockMap.clear();
                    locks.clear();
                    currentLockData = null;
                    currentLockIndex = 0;
                    stacksArea.setText("");
                    stackNumber.setText("");
                    nextBtn.setEnabled(false);
                    prevBtn.setEnabled(false);
                    
                    
                }
                
            });
	        JMenuItem popupCopy = new JCheckBoxMenuItem("Copy History To Clipboard");
            popupCopy.setMnemonic('C');
            popupCopy.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_C, ActionEvent.CTRL_MASK));
            popupCopy.setEnabled(true);
            popupMenu.add(popupCopy);
            popupCopy.addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent e) {
                    
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection data = new StringSelection(sb.toString());
                    clipboard.setContents(data, data);
                    
                }
                
            });
	        

	        
	        Point p = this.getMousePosition();
	        popupMenu.setBorderPainted(true);
	        popupMenu.show(this, p.x, p.y);
	        
	    }
       protected TableCellRenderer createDefaultRenderer() {
           DefaultTableCellRenderer label = new DefaultTableCellRenderer() {
               
               private static final long serialVersionUID = 1L;

               public Component getTableCellRendererComponent(JTable table, Object value,
                                boolean isSelected, boolean hasFocus, int row, int column) {
                   if (table != null) {
                       
                       JTableHeader header = table.getTableHeader();
                       if (header != null) {
                           setForeground(header.getForeground());
                           setBackground(header.getBackground());
                           setFont(header.getFont());
                       }
                   }

                   setText((value == null) ? "" : value.toString());
                   setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                   return this;
               }
           };
           label.setHorizontalAlignment(JLabel.CENTER);
           return label;
       }

}

