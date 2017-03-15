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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import edu.regis.jprobe.model.BroadcastMessage;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.Utilities;


/**
 * @author jdivincenzo
 *
 * This Class is the GUI Dialog for displaying vehicle data so that the user can
 * add, update or delete vehicles.
 * 
 */
public class ConnectDialog extends JDialog {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int frameWidth = 600;
	private int frameHeight = 300;
	private int buttonWidth=80;
	private int buttonHeight=40;
	private static final double LABEL_WEIGHT = 0.5;
	private static final double VALUE_WEIGHT = 0.5;
	/** Time in milliseconds that a broadcast message is removed */
	private static long BROADCAST_AGE_OUT = 10000;
	
	// Swing components
	private JTextField statusMsg;
	private HostTable availableHosts;
	private HostDataModel hostModel;
	private JList<Object> recordedSessions;
	private JScrollPane sp;
	private JTabbedPane tabPane;
	private JTextField hostIP;
	private JTextField hostPort;
	private JTextField userID;
	private JPasswordField password;
	private JButton connectBtn;
	private Timer timer;
	private JLabel lblScan;
	private JLabel lr1;
	private JLabel lblFileProperties;
	private String host;
	private String ipAddress;
	private String name;
	private String clientUserId;
	private String clientPassword;
	private String sessionFile;
	private String sessionPath;
	private int port;
	private ConnectDialog instance;
	private boolean dialogCanceled = false;
	private boolean remoteConnection = false;
	private boolean recordedSession = false;
	private Vector<BroadcastMessage> hostVector;
	private BroadCastScanner scanner;
	//private Map<String, BroadcastMessage> map;
	private Map<String, BroadcastMessage> lastMap;
	private Logger logger;
	private UIOptions options;
	private int selectedRow = -1;
	
	private static final String SCAN_LABEL = "Probes Broadcasting on this Subnet";
	private static final String REC_LABEL = "Recorded Sessions in ";
	
	/**
	 * Connect Dialog
	 * @param owner Owning Frame
	 * @param host	Host address
	 * @param port  Host Port
	 * @param options UI OPtions
	 */
	@SuppressWarnings("unchecked")
    public ConnectDialog( JFrame owner, String host, int port, UIOptions options) {
		
		super(owner,true);
		this.host = host;
		this.port = port;
		this.options = options;
		this.setTitle("Connect to a Probe");
		instance = this;
		logger = Logger.getLogger();
		
		
		Color lblColor = Color.DARK_GRAY;
		hostVector = new Vector<BroadcastMessage>();
		//map = new HashMap<String, BroadcastMessage>();
		lastMap = new HashMap<String, BroadcastMessage>();
		scanner = BroadCastScanner.getInstance();
		//scanner.start();
		setSize(frameWidth,frameHeight);
				
		Point p = owner.getLocation();
		Dimension pd = owner.getSize();
		int x = p.x + ((pd.width - frameWidth) / 2);
		int y = p.y + ((pd.height - frameHeight) / 2);
		setLocation (x,  y);
			
		//Our field dimensions
		Dimension headingSize = new Dimension(50,20);
		Dimension largeTextSize = new Dimension(250,20);
		
		tabPane = new JTabbedPane();
		tabPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                Object source = e.getSource();
                if (source instanceof JTabbedPane) {
                    int idx = ((JTabbedPane) source).getSelectedIndex();
                    //System.out.println("Selected pane is " + idx);
                    if (timer == null) {
                        return;
                    }
                    if (idx == 0) {
                        if (!timer.isRunning()) {
                            statusMsg.setText("Refreshing Host List...");
                            connectBtn.setEnabled(false);
                            //selectedRow = -1;
                            selectedRow = availableHosts.getSelectedRow();
                            scanner.clear();
                            hostVector.clear();
                            availableHosts.reset();
                            timer.start();
                            lblScan.setText(SCAN_LABEL);
                        }
                    } else {
                        connectBtn.setEnabled(true);
                        timer.stop();
                    }
                }
                
            }
		    
		});
				
		//Options Data Panel
		JPanel p1 = new JPanel();
		p1.setLayout(new GridBagLayout());
		p1.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
		GridBagConstraints c = new GridBagConstraints();
		c.insets= new Insets(2,2,2,2);
		c.fill = GridBagConstraints.BOTH;
		
		

		
//		Properties File label& Field
		lblScan = new JLabel(SCAN_LABEL);
		lblScan.setHorizontalAlignment(JLabel.CENTER);
		lblScan.setPreferredSize(headingSize);
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		p1.add(lblScan,c);
		
		hostModel = new HostDataModel();
		availableHosts = new HostTable(hostModel);
		availableHosts.setAutoCreateColumnsFromModel(false);
		availableHosts.setColumnModel(new DefaultTableColumnModel());
		availableHosts.setRowSelectionAllowed(true);
		availableHosts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sp = new JScrollPane();		
		sp.setWheelScrollingEnabled(true);
		for (int k = 0; k < hostModel.getColumnCount(); k++) {
            
            DefaultTableCellRenderer renderer = new ThreadTableCellRenderer();
            renderer.setHorizontalAlignment(hostModel.cdata[k].alignment);
            TableColumn column = new TableColumn(k, hostModel.cdata[k].length, renderer, null);
            column.setHeaderRenderer(createDefaultRenderer());
            availableHosts.addColumn(column);
        }
        JTableHeader header = availableHosts.getTableHeader();
                
        header.setUpdateTableInRealTime(true);
        header.setReorderingAllowed(false);
		c.gridx=0;
		c.gridy=1;
		c.gridwidth=1;
		c.gridheight=3;
		c.weightx = 1.0;
		c.weighty = 1.0;
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    sp.getViewport().add(availableHosts);
                
		sp.setBorder(new EtchedBorder());
		p1.add(sp,c);
		
		statusMsg = new JTextField();
		statusMsg.setEditable(false);
		c.gridx=0;
		c.gridy=4;
		c.gridwidth=1;
		c.gridheight=1;
		c.weightx=0.0;
		c.weighty = 0.0;
		statusMsg.setText("Refreshing Host List...");
		p1.add(statusMsg,c);
		
		MouseListener mouseListener = new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		         selectedRow = availableHosts.getSelectedRow();
		         //System.out.println("Selected Row = " + selectedRow);
		         
		         if (e.getClickCount() == 2) {
		             processSelection(null);
		          } else {
		        	 //populateRemotePane();
		             if (selectedRow >= 0) {
		                 if (selectedRow < hostVector.size()) { 
		                     BroadcastMessage bm = hostVector.get(selectedRow);
		                     if (bm != null) {
		                         statusMsg.setText("Click Connect to Monitor " + bm.getKey());
		                         timer.stop();
		                         connectBtn.setEnabled(true);
		                     }
		                 }
		             } else {
		                 //availableHosts.requestFocusInWindow();
		             }
		             
		          }
		         lastMap.clear();
		     }
		 };
		availableHosts.addMouseListener(mouseListener);

		tabPane.addTab("Local Subnet", p1);
		 
		JPanel p2 = new JPanel();
		p2.setLayout(new GridBagLayout());
		p2.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets= new Insets(2,2,2,2);
		c2.fill = GridBagConstraints.BOTH;
		
		JLabel l1 = new JLabel("Host Name/IP Address");
		l1.setHorizontalAlignment(JLabel.RIGHT);
		l1.setPreferredSize(headingSize);
		l1.setForeground(lblColor);
		c2.gridx=0;
		c2.gridy=0;
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx = LABEL_WEIGHT;
		c2.weighty = .25;
		p2.add(l1,c2);
		hostIP = new JTextField();
		c2.gridx=1;
		c2.gridy=0;
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx=VALUE_WEIGHT;
		hostIP.setPreferredSize(largeTextSize);
		Font fontTF = hostIP.getFont();
		fontTF = fontTF.deriveFont(Font.BOLD);
		hostIP.setFont(fontTF);
		p2.add(hostIP,c2);
		
		JLabel l2 = new JLabel("Host Port");
		l2.setHorizontalAlignment(JLabel.RIGHT);
		l2.setPreferredSize(headingSize);
		l2.setForeground(lblColor);
		c2.gridx=0;
		c2.gridy=1;
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx = LABEL_WEIGHT;
		p2.add(l2,c2);
		hostPort = new JTextField();
		c2.gridx=1;
		c2.gridy=1;
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx=VALUE_WEIGHT;
		hostPort.setPreferredSize(largeTextSize);
		hostPort.setFont(fontTF);
		hostPort.setText(options.getLastRemotePort().trim());
		p2.add(hostPort,c2);
		
		JLabel l3 = new JLabel("User Id");
		l3.setHorizontalAlignment(JLabel.RIGHT);
		l3.setPreferredSize(headingSize);
		l3.setForeground(lblColor);
		c2.gridx=0;
		c2.gridy=2;
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx = LABEL_WEIGHT;
		p2.add(l3,c2);
		userID = new JTextField();
		c2.gridx=1;
		c2.gridy=2;
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx=VALUE_WEIGHT;
		userID.setPreferredSize(largeTextSize);
		userID.setFont(fontTF);
		userID.setText(System.getProperty("user.name"));
		p2.add(userID,c2);
		
		JLabel l4 = new JLabel("Password");
		l4.setHorizontalAlignment(JLabel.RIGHT);
		l4.setPreferredSize(headingSize);
		l4.setForeground(lblColor);
		c2.gridx=0;
		c2.gridy=3;
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx = LABEL_WEIGHT;
		p2.add(l4,c2);
		password = new JPasswordField();
		c2.gridx=1;
		c2.gridy=3;
		c2.gridwidth=1;
		c2.gridheight=1;
		c2.weightx=VALUE_WEIGHT;
		password.setPreferredSize(largeTextSize);
		password.setFont(fontTF);
		p2.add(password,c2);
		
		tabPane.addTab("Remote System", p2);
		
		//Options Data Panel
        JPanel p4 = new JPanel();
        p4.setLayout(new GridBagLayout());
        p4.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
        GridBagConstraints c4 = new GridBagConstraints();
        c4.insets= new Insets(2,2,2,2);
        c4.fill = GridBagConstraints.BOTH;
        
        

        
//      Properties File label& Field
        lr1 = new JLabel(REC_LABEL);
        lr1.setHorizontalAlignment(JLabel.CENTER);
        lr1.setPreferredSize(headingSize);
        c4.gridx=0;
        c4.gridy=0;
        c4.gridwidth=1;
        c4.gridheight=1;
        c4.weightx = 0.0;
        c4.weighty = 0.0;
        p4.add(lr1,c4);
        
        
        recordedSessions = new JList<Object>();
        JScrollPane sp2 = new JScrollPane();     
        sp2.setWheelScrollingEnabled(true);
        c4.gridx=0;
        c4.gridy=1;
        c4.gridwidth=1;
        c4.gridheight=3;
        c4.weightx = 1.0;
        c4.weighty = 1.0;
        recordedSessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordedSessions.setSelectedIndex(0);
        recordedSessions.setCellRenderer(new HostListCellRenderer(options.getRowHeightAdjustment()));
        sp2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        sp2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp2.getViewport().add(recordedSessions);
        
        Vector<String> fileList = getSessionFiles();
        recordedSessions.setListData(fileList);
                
        sp2.setBorder(new EtchedBorder());
        p4.add(sp2,c4);
        lblFileProperties = new JLabel(" ");
        lblFileProperties.setBorder(new EtchedBorder());
        Font bold = lblFileProperties.getFont().deriveFont(Font.BOLD);
        lblFileProperties.setFont(bold);
        c4.gridx=0;
        c4.gridy=4;
        c4.gridwidth=1;
        c4.gridheight=1;
        c4.weightx = 1.0;
        c4.weighty = 0;
        p4.add(lblFileProperties,c4);
        
        JPanel p4b = new JPanel();

        p4b.setLayout(new GridLayout(1,5,1,1));
        p4b.setBorder(new EtchedBorder( EtchedBorder.LOWERED));
        p4b.add(new JLabel());
        p4b.add(new JLabel());
        JButton browse = new JButton("Browse");
        p4b.add(browse);
        p4b.add(new JLabel());
        p4b.add(new JLabel());
        c4.gridx=0;
        c4.gridy=5;
        c4.gridwidth=1;
        c4.gridheight=1;
        c4.weightx = 1.0;
        c4.weighty = 0;
        p4.add(p4b, c4);
        
        MouseListener sessionmouseListener = new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 
                 if (e.getClickCount() == 2) {
                     
                    processSelection(null);
                  } else {
                      String sel = (String) recordedSessions.getSelectedValue();
                      File file = new File(sessionPath + File.separator + sel);
                      lblFileProperties.setText("Length(" + Utilities.format(file.length()) +
                              ") Date(" + 
                              Utilities.formatTimeStamp(file.lastModified(),
                                      "MM/DD/YYYY HH:mm:ss") + ")");
                      
                  }
             }
         };
         browse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String file = getBrowsedFile();
                
                if (file != null) {
                    processSelection(file);
                }
                
            }
             
         });
         recordedSessions.addMouseListener(sessionmouseListener);
        
        tabPane.addTab("Recorded Sessions", p4);
		
		// Panel for our action buttons
		JPanel p3 = new JPanel();
		p3.setLayout(new GridLayout(1,2,5,5));
		p3.setBorder(new EtchedBorder( EtchedBorder.LOWERED));

		// Save button and event handler
		connectBtn = new JButton();
		connectBtn.setText(" Connect ");
		connectBtn.setToolTipText("To Connect to the Selected Probe...");
		connectBtn.setSize(buttonWidth, buttonHeight);
		connectBtn.setDefaultCapable(true);
		connectBtn.setEnabled(false);
		connectBtn.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						
						processSelection(null);
						
					 }
				});

		p3.add(connectBtn);
		this.getRootPane().setDefaultButton(connectBtn);
		// Cancel button and event handler
		JButton b3 = new JButton();
		b3.setText("Cancel");
		b3.setToolTipText("To Cancel The Connect Dialog...");
		b3.setSize(buttonWidth, buttonHeight);
		b3.addActionListener(
				new ActionListener()
				{
					public void actionPerformed (ActionEvent e)
					{
						 cancelData();
					 }
				});
		
		p3.add(b3);
		
		// Add the panels to the frame
		setLayout(new  GridBagLayout());
 		GridBagConstraints c3 = new GridBagConstraints();
		c3.insets= new Insets(1,1,1,1);
		c3.fill = GridBagConstraints.BOTH;
		
		// Customer info panel
		c3.gridx=0;
		c3.gridy=0;
		c3.gridwidth=1;
		c3.gridheight=1;
		c3.weightx=1;
		c3.weighty=.8;
		//add(p1,c3);
		add(tabPane,c3);
				
		// Buttons
		c3.gridx=0;
		c3.gridy=2;
		c3.weightx=.3;
		c3.weighty=.1;
		c3.gridheight=1;
		add(p3,c3);

		// We want to catch this
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
				
		// Window close handler
		addWindowListener( new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
								
				cancelData();
			}
		});
		ActionListener act = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        getAvailableHosts();
		        
		    }
		};
		if (options.getLastRemoteHost().isEmpty()) {
    		try {
    			hostIP.setText(InetAddress.getLocalHost().getHostAddress());
    		} catch (UnknownHostException e) {
    			logger.logException(e, this);
    			hostIP.setText("localhost");
    		}
		} else {
		    hostIP.setText(options.getLastRemoteHost());
		}
		
				
		timer = new Timer(1000, act);
		timer.start();
		getAvailableHosts();
		
		setVisible(true);
		
		
	}
	
	
	/**
	 * This method is used to process the selection 
	 */
	public void processSelection(String fileName) {
		
				
		if (tabPane.getSelectedIndex() == 0) {
		    if (selectedRow < 0) {
		        return;
		    }
		    
		    
		    BroadcastMessage bm = hostVector.get(selectedRow);
		    
		    if (bm == null) {
		        return;
		    }
			
			name = bm.idName;
			host = bm.hostName;
			ipAddress = bm.hostIP;
			port = bm.portNumber;
			
			remoteConnection = false;
			recordedSession = false;
			clientUserId = System.getProperty("user.name");
			clientPassword = null;
			
		} else if (tabPane.getSelectedIndex() == 1) {
			name = "Remote";
			host = hostIP.getText();
			ipAddress = host;
			remoteConnection = true;
			recordedSession = false;
			clientUserId = userID.getText();
			clientPassword = new String(password.getPassword()).trim();
			try {
				port = Integer.parseInt(hostPort.getText());
			} catch (NumberFormatException e) {
				
				JOptionPane.showMessageDialog(this,
						"Port Number is Invalid or Not Numeric", 
						"Invalid Port",JOptionPane.ERROR_MESSAGE);
				return;
			}
			options.setLastRemoteHost(host.trim());
			options.setLastRemotePort(hostPort.getText().trim());
			options.save();
		} else {
		    if (fileName == null) {
		        sessionFile = (String) recordedSessions.getSelectedValue();
		    } else {
		        sessionFile = fileName;
		    }
		    sessionFile = sessionPath + 
		            File.separator +
		            sessionFile;
		    remoteConnection = false;
		    recordedSession = true;
		    name = "Playback";
            host = sessionFile;
            ipAddress = sessionFile;
		}
		timer.stop();
		//timer.shutdown();
		//scanner.shutdown();
		this.dispose(); 
	}
	
	
	protected void populateRemotePane() {
		
		int index = availableHosts.getSelectedRow();
        
        BroadcastMessage bm = hostVector.get(index);
        
        if (bm == null) {
            return;
        }
        
        name = bm.idName;
        host = bm.hostName;
        ipAddress = bm.hostIP;
        port = bm.portNumber;
	}
	/**
	 * This method is used to handle the window close event, from the cancel button 
	 * or the window close button.
	 */
	public void cancelData()
	   {
	    
		dialogCanceled = true;
		timer.stop();
		//scanner.shutdown();
	    this.dispose();

	   
	     	
     }
	protected void getAvailableHosts() {
		
	    Map<String, BroadcastMessage> map = scanner.getHostMap();
		logger.debug("Connect Dialog Scanning for Hosts");
		
		//Set keys = new HashSet<BroadcastMessage>();	//create a collection of the stat map keys
	    Set<String> keys = map.keySet();				        //Get the collection of keys
	    Iterator<String> iter = keys.iterator();	        //create an iterator for them
	    
	    if (!compareMaps(map, lastMap)) {
	        lastMap.clear();
	        lastMap.putAll(map);
	    } else {
	        return;
	    }
	    //Loop thru the array
	    hostVector.clear();
	    while (iter.hasNext()) {
	    	
	    	Object obj = iter.next();
	    	BroadcastMessage bm = map.get(obj);
	    	logger.debug("Found Probe: " + bm.toString());
	    	if (System.currentTimeMillis() - bm.receiveTime < BROADCAST_AGE_OUT ) {
	    	    hostVector.add(bm);
	    	} 
	    	
		}
	     
	    hostModel.update(hostVector);
	    
		if (hostVector.size() > 0) {
		    
		    if (selectedRow == -1) {
		        statusMsg.setText("Select an Available Host");
		        availableHosts.requestFocusInWindow();
		    } else {
		        if (hostVector.size() > selectedRow ) {
		            BroadcastMessage bm = hostVector.get(selectedRow);
		            if (bm != null) {
		                statusMsg.setText("Click Connect to " + bm.getKey()); 
		            }
		        }
		    }
			logger.debug(hostVector.size() + " Probes Found");
		} else {
			statusMsg.setText("No Available Host Found, Scanning...");
		}
		if (tabPane.getSelectedIndex() == 0 &&
            selectedRow < 0) {
                connectBtn.setEnabled(false);
 		} else {
 		   connectBtn.setEnabled(true); 
 		}
		availableHosts.reset();
		lblScan.setText(SCAN_LABEL + " (" + hostVector.size() + " Probes Found)");
	}
	private Vector<String> getSessionFiles() {
	    
	    Vector<String> fileList = new Vector<String>();
	    File file = new File(options.getRecordingDirectory());
	    lr1.setText(REC_LABEL + options.getRecordingDirectory());
	    sessionPath = options.getRecordingDirectory();
	    if (file.isDirectory()) {
	        File[] files = file.listFiles();
	        for (File session : files) {
	            if (session.getName().endsWith(UIOptions.RECORDING_SUFFIX)) {
	                fileList.add(session.getName());
	            }
	        }
	    }
	    
	    
	   return fileList; 
	}
	private boolean compareMaps(Map<String, BroadcastMessage> map1, 
	        Map<String, BroadcastMessage> map2) {
	    
	    if (map1.size() != map2.size()) {
	        return false;
	    }
	    
	    Set<String> keys = map1.keySet();    //create a collection of the stat map keys
        Iterator<String> iter = keys.iterator();  //create an iterator for them
        
        
        //Loop thru the array
        hostVector.clear();
        while (iter.hasNext()) {
            String key = iter.next();
            BroadcastMessage bm1 = map1.get(key);
            BroadcastMessage bm2 = map2.get(key);
            if (!bm1.equals(bm2)) {
                return false;
            }
            if (System.currentTimeMillis() - bm1.receiveTime > BROADCAST_AGE_OUT ) {
                return false;
            }
        }
	    return true;
	    
	}
	private String getBrowsedFile() {
	    
	    File path = new File(options.getRecordingDirectory());
	    JFileChooser chooser = new JFileChooser();
        
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle("Specify the Recording to Open");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilter() {
           public boolean accept(File f) {
               if (f.isDirectory()) {
                   return true;
               }
                return f.getAbsolutePath().endsWith(UIOptions.RECORDING_SUFFIX);
            }

            @Override
            public String getDescription() {
                return "Recorded Probe Files (*" + UIOptions.RECORDING_SUFFIX + ")";
            }
            
        });
        chooser.setCurrentDirectory(path);
        
                 
        int returnVal = chooser.showOpenDialog(instance);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           File file = chooser.getSelectedFile();
           sessionPath = file.getParent();
           return file.getName();
        } 
        
        return null;
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
	/**
	 * @return Returns the host.
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host The host to set.
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return Returns the port.
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port The port to set.
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the dialogCanceled.
	 */
	public boolean isDialogCanceled() {
		return dialogCanceled;
	}


	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}


	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	/**
	 * @return the remoteConnection
	 */
	public boolean isRemoteConnection() {
		return remoteConnection;
	}


	/**
	 * @param remoteConnection the remoteConnection to set
	 */
	public void setRemoteConnection(boolean remoteConnection) {
		this.remoteConnection = remoteConnection;
	}


	/**
	 * @return the clientPassword
	 */
	public String getClientPassword() {
		return clientPassword;
	}


	/**
	 * @return the clientUserId
	 */
	public String getClientUserId() {
		return clientUserId;
	}


    public String getSessionFile() {
        return sessionFile;
    }


    public void setSessionFile(String sessionFile) {
        this.sessionFile = sessionFile;
    }


    public boolean isRecordedSession() {
        return recordedSession;
    }


    public void setRecordedSession(boolean recordedSession) {
        this.recordedSession = recordedSession;
    }
	
	
}

@SuppressWarnings("rawtypes")
class HostListCellRenderer extends JLabel implements ListCellRenderer {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int height = 14;
	public HostListCellRenderer(int heightAdjustment) {
		setOpaque(true);
		this.setForeground(Color.black);
		this.setBorder(new LineBorder(Color.black, 1, true));
		Dimension dim = this.getPreferredSize();
		dim.height = height + heightAdjustment;
		this.setPreferredSize(dim);
	}
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		if (isSelected) {
			setBackground(Color.lightGray);
		}else {	
			setBackground(Color.white);
		}
		
		if (value != null) {
		    setText(value.toString());
		}
		return this;
	}
	
}