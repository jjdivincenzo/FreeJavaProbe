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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.WriteAbortedException;
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularDataSupport;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import edu.regis.jprobe.model.ClassPathUpdater;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.ProbeCommunicationsException;
import edu.regis.jprobe.model.ProbeCommunicationsManager;
import edu.regis.jprobe.model.ProbeRequest;
import edu.regis.jprobe.model.ProbeResponse;


/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JMXBeanPanel extends PerformancePanel implements IPerformancePanel {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private boolean panelBuilt = false;
	private boolean panelRefresh = false;
	private JTree jmxTree;
	private JPanel beanView;
	private JTabbedPane beanInfo;
	private JSplitPane sp;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode top;
	private ProbeCommunicationsManager pcm;
	private JMXBeanPanel instance;
	private TreePath selectedBean;
	private Logger logger;
	private File path = new File(".");
	private MBeanServerConnection connection;
	private UIOptions options;
	private Map<ObjectName, NotificationSubscriberPanel> subs;
	private JProbeClientFrame client;
	private ProbeResponse lastResponse;
		
	private static final String[][] PRIMATIVE_CLASSES = {
	    { "boolean", "Z"},
	    { "byte", "B"},
	    { "char", "C"},
	    { "double", "D"},
	    { "float", "F"},
	    { "int", "I"},
	    { "long","J"},
	    { "short", "S"}
	};
	
	private TreePath lastPath;
	
	public JMXBeanPanel(ProbeCommunicationsManager pcm, UIOptions options, JProbeClientFrame client) {
		
	    this.options = options;
	    this.client = client;
	    subs = new HashMap<ObjectName, NotificationSubscriberPanel>();
		setBorder(new TitledBorder( new EtchedBorder(), "JMX Beans"));
		//setLayout(new  GridBagLayout());
		setLayout(new GridLayout(1,1,1,1));
		GridBagConstraints cc = new GridBagConstraints();
        cc.insets= new Insets(1,1,1,1);
        cc.fill = GridBagConstraints.BOTH;
        cc.gridx=0;
        cc.gridy=0;
        cc.gridwidth=1;
        cc.gridheight=1;
        cc.weightx = .3;
        cc.weighty = 1;
		
        beanView = new JPanel();
        beanView.setLayout(new GridLayout(1,1,1,1));
        beanView.setBorder(new EtchedBorder());
        
        //add(beanView, cc);
        beanInfo = new JTabbedPane();
        
            
        cc.gridx=1;
        cc.gridwidth=2;
        cc.weightx = .7;

        sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, beanView, beanInfo);
        sp.setOneTouchExpandable(true);
        add(sp);
        this.pcm = pcm;
       
		instance = this;
		this.logger = Logger.getLogger();
		
		
		
	}
	
	
	public void update(ProbeResponse res) {
		
		if (panelBuilt) return;
		 
		if (pcm != null && pcm.isConnected()) {
		    connection = getJMXConnection();
		}
		
		JMXTreeItem rootItem = 
		        new JMXTreeItem(JMXTreeItem.ROOT,res.getProbeName(), res.getProbeName(), null);
		//	MetaData Tree
		top = new DefaultMutableTreeNode(rootItem);
				
		treeModel = new DefaultTreeModel(top);
		DefaultTreeSelectionModel selModel = new DefaultTreeSelectionModel();
		
		selModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		jmxTree = new JTree(treeModel);
		jmxTree.setSelectionModel(selModel);
		jmxTree.setExpandsSelectedPaths(true);
		jmxTree.setCellRenderer(new JMXTreeRenderer());
		Font newfont = jmxTree.getFont();
		newfont = newfont.deriveFont(Font.BOLD);
		jmxTree.setFont(newfont);
		jmxTree.setEditable(false);
				
		JScrollPane scroll = new JScrollPane(jmxTree);
		
		jmxTree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				
			    TreePath path = jmxTree.getSelectionPath();
			    expand(path);
			    lastPath = path;
				

				
			}
			
		});	
		
		jmxTree.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				
				
				if (e.getButton() == 3)  {
					
					promptToRefresh();
					
				}
				
				
			}
			
		});
		
		beanView.add(scroll);
		String domains[] = res.getJmxDomains();
		DefaultMutableTreeNode defaultNodes[] = new DefaultMutableTreeNode[domains.length + 1];
		defaultNodes[0] = top;
		for (int i =0; i < domains.length; i++) {
			JMXTreeItem ti = new JMXTreeItem(JMXTreeItem.DOMAIN,domains[i], domains[i], null);
			DefaultMutableTreeNode domain = new DefaultMutableTreeNode(ti);
					
			top.add(domain);
			defaultNodes[i + 1] = domain;
			try {
			    expandDomain(domain, ti);
			    
			} catch (ProbeCommunicationsException e1) {
	            logger.logException(e1, instance);
	        }
			
		}
		if (selectedBean == null) {
			jmxTree.setSelectionPath(new TreePath(defaultNodes));
		} else {
			jmxTree.setSelectionPath(selectedBean);
		}
		
		jmxTree.setShowsRootHandles(true);
		
		panelBuilt = true;
		try {
		    if (panelRefresh) {
		        panelRefresh = false;
		        expand(lastPath);
		    } else {
		        expandDomain(top, rootItem);
		    }
        } catch (ProbeCommunicationsException e1) {
            logger.logException(e1, instance);
        }
		scroll.repaint();
		beanView.repaint();
		this.repaint();
		beanView.validate();
		sp.setDividerLocation(0.33);
		lastResponse = res;
		
	}
	public void resetPanel() {
		
		if (!panelBuilt) return;
		panelBuilt = false;
		beanView.removeAll();
		beanView.repaint();
		beanView.validate();
		//panelRefresh = true;

	}
	public boolean isPanelBuild() {
		return panelBuilt;
	}
	
	private void expand(TreePath selpath) {
	    
	    if (selpath == null) return;
	    selectedBean = null;
        
        if (pcm == null || !pcm.isConnected()) {
            beanInfo.removeAll();
            JPanel pnl = new JPanel();
            JLabel lbl = new JLabel("Not Connected");
            Font f = lbl.getFont().deriveFont(24f);
            lbl.setFont(f);
            pnl.add(lbl);
            beanInfo.add("Not Connected", pnl);
            
            return;
        }
       
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selpath.getLastPathComponent();
        Object o = ((DefaultMutableTreeNode) selpath.getLastPathComponent()).getUserObject();
        JMXTreeItem ti = (JMXTreeItem) o;
        logger.debug( "Expanding " + ((JMXTreeItem) o).format());
        
        
        if (!ti.isItemExpanded() && ti.getType() == JMXTreeItem.DOMAIN) {
            try {
                
                expandDomain(node, ti);
                jmxTree.validate();
            } catch (Exception e1) {
                logger.logException(e1, instance);
            } finally {
                
            }
        }
        
        if (!ti.isItemExpanded() && ti.getType() == JMXTreeItem.BEAN) {
            try {
                
                expandBean(node, ti);
                jmxTree.validate();
            } catch (ProbeCommunicationsException e1) {
                logger.logException(e1, instance);
                if (e1.getCause() instanceof ClassNotFoundException) {
                    String msg = "This MBean has one or more Objects that " +
                            " do not have Class Definitions in this JVM." +
                            "\nThis MBean Cannot Be Expanded. \nMissing Class is " +
                            e1.getCause().getMessage() + 
                            "\nDo You Want To Locate And Add the Missing Library?";
                    
                    if ( JOptionPane.showConfirmDialog(this, 
                            msg,
                            "MBean Unmarshalling Error",
                            JOptionPane.YES_NO_OPTION & 
                            JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) 
                        {
                            loadJarFile(e1.getCause().getMessage());
                        }
                } else {
                    String msg = "An Error Occurred Fetching MBean \n" +
                            ((JMXTreeItem) o).getName() +
                            "\nError is " +
                            e1.getCause().getMessage();
                    
                    JOptionPane.showMessageDialog(instance, 
                            msg,
                             "MBean Error", JOptionPane.ERROR_MESSAGE, null);
                }
                return;
            } catch (Exception e1) {
                String msg = "An Error Occurred Fetching MBean \n" +
                        //((JMXTreeItem) o).getName() +
                        "\nError is " +
                        e1.getMessage();
                logger.logException(e1, instance);
                JOptionPane.showMessageDialog(instance, 
                        msg,
                         "MBean Error", JOptionPane.ERROR_MESSAGE, null);
            } finally {
                
            }
        }
        
        if (ti.getType() == JMXTreeItem.BEAN) {
            
            selectedBean = selpath;
            
        } 
        
    }
	

	private void expandDomain(DefaultMutableTreeNode node, JMXTreeItem item) throws ProbeCommunicationsException {
			
		ProbeRequest pr = new ProbeRequest();
		pr.setRequestType(ProbeRequest.REQ_GET_MBEANS);
		pr.setMbeanDomain(item.getName());
		
		ProbeResponse res = pcm.sendCommand(pr);
		
		List<ObjectName> beans = new ArrayList<ObjectName>();
		beans.addAll(res.getMbeans());
		Collections.sort(beans, new ObjectNameComparator());
		
		Map<String, DefaultMutableTreeNode> baseNode = new HashMap<String, DefaultMutableTreeNode>();
		
		for (Object obeans : beans) {
			ObjectName on = (ObjectName) obeans; 
			logger.debug("ObjectName: CannonicalName=" + on.getCanonicalName() + 
			            ", Domain=" + on.getDomain() + ", KeyProperties=" + on.getCanonicalKeyPropertyListString() );
			           
			String name = getName(on); 
			
			Hashtable<String, String> props = on.getKeyPropertyList();
			Set<String> keys = props.keySet();
			Iterator<String> iter = keys.iterator();
			String childName = "Unknown";
			while (iter.hasNext()) {
			    String key = iter.next();
			    if (!key.equals("type")) {
			        childName = name + "," + key + "=" + props.get(key);
			    }
			}
			
			DefaultMutableTreeNode parent = baseNode.get(name);
			
			if (parent == null) {
			        
			    DefaultMutableTreeNode dom = new DefaultMutableTreeNode(
			            new JMXTreeItem(JMXTreeItem.BEAN,name, item.getName(), on));
			
			    node.add(dom);
			    baseNode.put(name, dom);
			} else {
			    DefaultMutableTreeNode dom = new DefaultMutableTreeNode(
                        new JMXTreeItem(JMXTreeItem.BEAN,childName, item.getName(), on));
			    parent.add(dom);
			}
			
			
			
		}
	
		

		item.setItemExpanded(true);
		treeModel.reload(node);

	}
	private void expandBean(DefaultMutableTreeNode node, JMXTreeItem item) throws ProbeCommunicationsException {
		
	    MBeanInfo info = null;
        AttributeList attrl = null; 
        MBeanOperationInfo oper[] =null;
        MBeanNotificationInfo[] notifications = null;
        MBeanAttributeInfo[] attrInfo = null;
	    
        if (connection == null || options.isUseProbeForMBean()) {
    		ProbeRequest pr = new ProbeRequest();
    		pr.setRequestType(ProbeRequest.REQ_GET_MBEAN_INFO);
    		pr.setObjectName(item.getObjectName());
     		ProbeResponse res = pcm.sendCommand(pr);
     		
    		info = res.getMbeanInfo();
    		attrl = res.getAttributeList(); 
    		
        } else {
            try {
                info = connection.getMBeanInfo(item.getObjectName());
                attrInfo = info.getAttributes();
                attrl = new AttributeList();
                for (int i = 0; i < attrInfo.length; i++) {
                    String name = attrInfo[i].getName();
                    Attribute attr = null;
                    
                    try {
                        if (attrInfo[i].isReadable()) {
                            Object obj = connection.getAttribute(item.getObjectName(), name);
                            attr = new Attribute(name, obj);
                        } else {
                            attr = new Attribute(name, "");
                        }
                    
                    } catch (UnmarshalException e) {
                        Throwable cause = e.getCause();
                        if (cause != null) {
                            if (cause instanceof ClassNotFoundException) {
                                throw new ProbeCommunicationsException("ClassNotFoundException", cause);
                            }
                            if (cause instanceof WriteAbortedException) {
                                attr = new Attribute(name, "<Not Serializable>");
                            }
                        }
                    } catch (Exception e) {
                        logger.logException(e, this);
                        attr = new Attribute(name, "<Unavailable>");
                    }
                    attrl.add(attr);
                    
                }
            } catch (IOException e) {
                throw new ProbeCommunicationsException("Error Getting MBean", e);
            } catch (InstanceNotFoundException e) {
                throw new ProbeCommunicationsException("Error Getting MBean", e);
            } catch (IntrospectionException e) {
                throw new ProbeCommunicationsException("Error Getting MBean", e);
            } catch (ReflectionException e) {
                throw new ProbeCommunicationsException("Error Getting MBean", e);
            }
            
        }
        oper = info.getOperations();
        notifications = info.getNotifications();
        attrInfo = info.getAttributes();
        
		beanInfo.removeAll();
		JPanel attPanel = buildBeanAttributePanel(attrl, attrInfo, item.getObjectName());
		JPanel operPanel = buildBeanOperationsPanel(oper, item.getObjectName());
		JPanel notifPanel = buildBeanNotificationPanel(notifications, item.getObjectName());
		
		
		
		if (attPanel != null) {
		    beanInfo.add("Attributes", attPanel);
		    //beanInfo.setSelectedComponent(attPanel);
		}
		if (operPanel != null) {
		    beanInfo.add("Operations", operPanel);
		}
		if (notifPanel != null) {
            beanInfo.add("Notifications", notifPanel);
        }
		if (info != null) {
            beanInfo.add("Bean Info", buildBeanInfoPanel(info, item.getObjectName()));
        }
		
		//beanInfo.setSelectedIndex(tabIndex);
		
		treeModel.reload(node);

	}
	protected void addComposite(CompositeDataSupport composite, DefaultMutableTreeNode node) {
		
		Collection<?> comVals = composite.values();
		CompositeType type = composite.getCompositeType();
		
		//String typeName = type.getTypeName();
		Set<String> keys = type.keySet();
		Object key[] = keys.toArray();
		int max = key.length;
		int idx = 0;
		Iterator<?> iter = comVals.iterator();
		while (iter.hasNext()) {
			Object oa = iter.next();
			Object myname = (idx < max? key[idx++] : new String("xxx"));
			if (oa instanceof CompositeDataSupport) {
				addComposite((CompositeDataSupport)oa,node);
			} else if (oa instanceof TabularDataSupport) {
				addTabular((TabularDataSupport)oa, node);
			} else {
				
				DefaultMutableTreeNode value = new DefaultMutableTreeNode(
					new JMXTreeItem(JMXTreeItem.COMPOSITE,
							myname + "=" + oa.toString(), null, null));
				node.add(value);
			}
		}
			
		
		
	}
	protected void addTabular(TabularDataSupport tab, DefaultMutableTreeNode node) {
		
		
		Set<Object> keys = tab.keySet();
		Collection<Object> values = tab.values();
		Object name[] = keys.toArray();
		Object value[] = values.toArray();
		
		int max = (name.length > value.length? value.length :name.length) ;
		int idx = 0;
		
		
		while (idx < max) {
			
			if (value[idx] instanceof CompositeDataSupport) {
				DefaultMutableTreeNode valueItem = new DefaultMutableTreeNode(
						new JMXTreeItem(JMXTreeItem.TABULAR,
								name[idx].toString(), null, null));
						node.add(valueItem);
				addComposite((CompositeDataSupport)value[idx],valueItem);
			
				
			} else {
				
				DefaultMutableTreeNode valueItem = new DefaultMutableTreeNode(
					new JMXTreeItem(JMXTreeItem.COMPOSITE,
							name[idx].toString() + "=" + value[idx].toString(), null, null));
				node.add(valueItem);
			}
			idx++;
		}
			
		
	}

	protected void promptToRefresh() {
		
		final JPopupMenu queryPopup = new JPopupMenu("Refresh");
		
		JMenuItem popupSelect = new JMenuItem("Refresh JMXBeans");
		popupSelect.setMnemonic('S');
		popupSelect.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		popupSelect.setEnabled(true);
		queryPopup.add(popupSelect);
		
		popupSelect.addActionListener(new ActionListener() {
			
			public void actionPerformed (ActionEvent e)
			{
				logger.info("Refreshing MBean Tree");
			    
				try {
				    resetPanel();
				    selectedBean = null;
				    update(lastResponse);
				} catch(Exception ex) {
				    logger.logException(ex, null);
				    JOptionPane.showMessageDialog(instance, 
		                    "Error Refreshing MBeans, error is " +
		                     ex.getMessage(),
		                     "MBean Refresh Error", JOptionPane.ERROR_MESSAGE, null);
				}
			}
			
		});
		
		Point p = this.getMousePosition();
		queryPopup.setBorderPainted(true);
		queryPopup.show(instance, p.x, p.y);
	}
	
	private JPanel buildBeanInfoPanel(MBeanInfo info, ObjectName objectName) {
	    
	    String[] colNames = {"Name", "Value"};
	    String[][] data = new String[3] [2];
	    
	    data[0][0] = "Object Name";
	    data[0][1] = objectName.toString();
	    
	    data[1][0] = "Class Name";
        data[1][1] = info.getClassName();
        
        data[2][0] = "Description";
        data[2][1] = info.getDescription();
	    
	    DefaultTableModel model = new DefaultTableModel(data, colNames);
	    JPanel jp = new JPanel();
	    jp.setBorder(new TitledBorder( new EtchedBorder(), "MBean Info(" + objectName.toString() +")"));
	    jp.setLayout(new GridLayout(2,1,1,1));
	    JTable infoTable = new JTable(model);
	    infoTable.getTableHeader().setReorderingAllowed(false);
	    
	    JScrollPane sp = new JScrollPane(infoTable);
	    jp.add(sp);
	    
	    
	    
	    Descriptor descriptor = info.getDescriptor();
	    String[] fieldNames = descriptor.getFieldNames();
	    
	   
        String[][] fieldData = new String[fieldNames.length] [2];
        
        for (int i = 0; i < fieldNames.length; i++) {
            fieldData[i][0] = fieldNames[i];
            fieldData[i][1] = descriptor.getFieldValue(fieldNames[i]).toString();
        }
        
        DefaultTableModel fieldmodel = new DefaultTableModel(fieldData, colNames);
        JTable descTable = new JTable(fieldmodel);
        //descTable.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(Color.BLUE));
        JScrollPane sp2 = new JScrollPane(descTable);
        jp.add(sp2);
	    
	    return jp;
	    
	}
	private JPanel buildBeanAttributePanel(AttributeList attrl, 
	        MBeanAttributeInfo[] attrInfo, 
	        final ObjectName objectName) {
	   
	    Map<String, MBeanAttributeInfo> attrInfoMap =
	            new HashMap<String, MBeanAttributeInfo>();
	    
	    for (MBeanAttributeInfo info : attrInfo){
	      attrInfoMap.put(info.getName(), info);
	    }
	    
	    Map<String, Attribute> attrMap =
                new HashMap<String, Attribute>();
	    
	    for (int k=0; k < attrl.size(); k++) {
            
            Attribute val = (Attribute)attrl.get(k);
            if (val != null) {
                attrMap.put(val.getName(), val);
            }
           
        }
	    
	    int rows = attrl.size();
	    
	    if (rows == 0) {
	        return null;
	    }
	    final JPanel jp = new JPanel();
        jp.setBorder(new TitledBorder( new EtchedBorder(), "Attributes(" + objectName.toString() +")"));
        jp.setLayout(new GridBagLayout());
	    GridBagConstraints cc = new GridBagConstraints();
        cc.insets= new Insets(1,1,1,1);
        cc.fill = GridBagConstraints.BOTH;
        cc.gridx=0;
        cc.gridy=0;
        cc.gridwidth=5;
        cc.gridheight=9;
        cc.weightx = 1;
        cc.weighty = 1;
        
	    
	    final String[] colNames = {"Name", "Value"};
        
        Object[][] data = getAttributes(attrl); 
        
        final DefaultTableModel model = new DefaultTableModel(data, colNames);
        
        final AttributeTable infoTable = new AttributeTable(model, this, 
                objectName, attrInfoMap, attrMap);
        
        infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        infoTable.setColumnSelectionAllowed(true);
        infoTable.setCellSelectionEnabled(true);
        
        final JScrollPane sp = new JScrollPane(infoTable);
        
        jp.add(sp,cc);
        
            
        
        JButton refresh = new JButton("Refresh");
        cc.gridx=0;
        cc.gridy=9;
        cc.gridwidth=2;
        cc.gridheight=1;
        cc.weighty = 0;
        cc.weightx = .5;
        jp.add(new JLabel(" "), cc);
        cc.gridx=2;
        cc.gridwidth=1;
        cc.weightx = 0;
        jp.add(refresh, cc);
        cc.gridx=3;
        cc.gridwidth=2;
        cc.weightx = .5;
        jp.add(new JLabel(" "), cc);
        
        
        refresh.addActionListener( new ActionListener() {

           
            public void actionPerformed(ActionEvent e) {
                ProbeRequest pr = new ProbeRequest();
                pr.setRequestType(ProbeRequest.REQ_GET_MBEAN_INFO);
                pr.setObjectName(objectName);
                final JScrollBar vertical = sp.getVerticalScrollBar();
                final int pos = vertical.getValue();
                
                ProbeResponse res = null; 
                
                try {
                    res = pcm.sendCommand(pr);
                } catch (ProbeCommunicationsException e1) {
                    logger.logException(e1, instance);
                    JOptionPane.showMessageDialog(instance, 
                            "Cannot Refresh this MBean, Error is " +
                                    e1.getMessage() +
                             "\nTry Refreshing The MBean Tree",
                             "Refresh Error", JOptionPane.ERROR_MESSAGE, null);
                    return;
                }
                
                AttributeList attrl = res.getAttributeList();
                Object[][] data = getAttributes(attrl);
                DefaultTableModel model = new DefaultTableModel(data, colNames);
                infoTable.setModel(model);
                infoTable.refresh();
                
                SwingWorker<Object,Object> thd = new SwingWorker<Object,Object>() {
                    public Object doInBackground() {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                          //dont care  
                        }
                        int max = vertical.getMaximum();
                        if (pos <= max) {
                            vertical.setValue(pos);
                        } else {
                            vertical.setValue(max);
                       }
                        return null;
                    }
                    
                };
                thd.execute();
                
            }
            
        });
        
        return jp;
	}
	private JPanel buildBeanOperationsPanel(final MBeanOperationInfo ops[], final ObjectName objectName) {
	    
	    if (ops == null || ops.length == 0) {
	        return null;
	    }
	    int maxParms = 0;
	    List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>(); 
	    for (MBeanOperationInfo op : ops) {
	        operations.add(op);
	        int len = op.getSignature().length;
	        if (len > maxParms) {
	            maxParms = len;
	        }
	    }
	    
	    Collections.sort(operations, new OperationComparator());
	    int maxrows = 50;
        JPanel jp = new JPanel();
        int numOps = operations.size();
        jp.setBorder(new TitledBorder( new EtchedBorder(), "Operations(" + objectName.toString() +")"));
        jp.setLayout(new GridLayout(1,1,1,1));
        JPanel operPanel = new JPanel();
        //operPanel.setLayout(new GridBagLayout());
        operPanel.setLayout(new GridLayout(maxrows,maxParms,2,2));
        GridBagConstraints cc = new GridBagConstraints();
        int gridy = 0;
        cc.insets= new Insets(1,1,1,1);
        cc.fill = GridBagConstraints.BOTH;
        cc.gridx=0;
        cc.gridy=gridy;
        cc.gridwidth=1;
        cc.gridheight=1;
        cc.weightx = 1;
        cc.weighty = 0;
        JScrollPane sp = new JScrollPane(operPanel);
        jp.add(sp);
        Dimension min = new Dimension(0,0);
        Dimension pref = new Dimension(80,25);
        	        
        JPanel[] opers = new JPanel[numOps];
        for (int i = 0; i < numOps; i++) {
            MBeanOperationInfo oper = operations.get(i);
            final MBeanParameterInfo parms[] = oper.getSignature();
            opers[i] = new JPanel();
            int parmCount = parms.length + 2;
            if (parmCount < 10) {
                parmCount = 10;
            }
            opers[i].setLayout(new GridLayout(1,(parmCount * 2) + 2,1,1));
            opers[i].setBorder(new LineBorder(Color.BLACK,1));
            final String returnType = oper.getReturnType();
            JLabel l = new JLabel(formatParm(oper.getReturnType()));
            l.setHorizontalAlignment(JLabel.RIGHT);
            opers[i].add(l);
            
            final String methodName = oper.getName();
            JButton op = new JButton(methodName + "()");
            op.setMinimumSize(min);
            op.setPreferredSize(pref);
            if (oper.getDescription() != null && !oper.getDescription().isEmpty()) {
                op.setToolTipText("Invoke(" + oper.getDescription() +")");
            } else {
                op.setToolTipText("Invoke method " + methodName);
            }
            opers[i].add(op);
            
            int col = 10;
            boolean canCall = true;
            if (connection == null) {
                canCall = false;
            }
            final JTextField fld[] = new JTextField[parms.length];
            final String[] parmSig = new String[parms.length];
            for (int k = 0; k < parms.length; k++) {
                fld[k] = new JTextField();
                fld[k].setMinimumSize(min);
                fld[k].setPreferredSize(pref);
                fld[k].setText(formatParm(parms[k].getType()));
                parmSig[k] = parms[k].getType();
                if (!canCallParm(parms[k].getType())) {
                    canCall = false;
                }
                if (parms[k].getName() != null && !parms[k].getName().isEmpty()) {
                    JLabel lbl = new JLabel("{" + parms[k].getName() + "}");
                    lbl.setHorizontalAlignment(JLabel.RIGHT);
                    lbl.setMinimumSize(min);
                    opers[i].add(lbl);
                    //helper.addColumn(lbl, 1);
                    if (parms[k].getDescription() != null && !parms[k].getDescription().isEmpty())  {
                        fld[k].setToolTipText(parms[k].getDescription());
                    }
                } else {
                    opers[i].add(new JLabel(""));
                }
                opers[i].add(fld[k]);
                col--;
            }
            if (!canCall) {
                for (JTextField jtf : fld) {
                    jtf.setEditable(false);
                }
                op.setEnabled(false);
            }
            
            
            op.addActionListener( new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    invokeOperation(objectName, methodName, returnType, parmSig, fld);
                    
                }

                
                
            });
            while (col-- > 0) {
                opers[i].add(new JLabel("")); 
            }
            operPanel.add(opers[i]);
            gridy++;
        }
        while (gridy < maxrows) {
            JPanel p = new JPanel();
            p.add(new JLabel(""));
            operPanel.add(p);
            gridy++;
        }
        return jp;
	}
	   private JPanel buildBeanNotificationPanel(MBeanNotificationInfo[] info, final ObjectName objectName) {
	        
	       if (info == null || info.length == 0) {
	           return null;
	       }
	       
	        int maxrows = (info.length > 5 ? info.length : 5);
	        JPanel jp = new JPanel();
	        Dimension buttonSize = new Dimension(jp.getSize());
	        buttonSize.height = 30;
	       
	        jp.setBorder(new TitledBorder( new EtchedBorder(), "Notifications(" + objectName.toString() +")"));
	        jp.setLayout(new GridLayout(1,1,1,1));
	        JPanel operPanel = new JPanel();
	       
	        operPanel.setLayout(new GridLayout(maxrows,1,1,1));

	        jp.add(operPanel);
	        
	                    
	        JPanel[] opers = new JPanel[info.length];
	        for (int i = 0; i < info.length; i++) {
	            maxrows--;
	            opers[i] = new JPanel();
	            opers[i].setLayout(new GridBagLayout());
	            opers[i].setBorder(new EtchedBorder());
	            GridBagConstraints cc = new GridBagConstraints();
	            cc.insets= new Insets(1,1,1,1);
	            cc.fill = GridBagConstraints.BOTH;
	            cc.gridx=0;
	            cc.gridy=0;
	            cc.gridwidth=1;
	            cc.gridheight=4;
	            cc.weightx = 1;
	            cc.weighty = 1;
	            
	            String[] types = info[i].getNotifTypes();
	            String[] colNames = {"Name", "Value"};
	            String[][] data = new String [3] [2];
	            	            
	            data[0][0] = "Name";
	            data[0][1] = info[i].getName();
	            
	            data[1][0] = "Description";
	            data[1][1] = info[i].getDescription();
	            
	            data[2][0] = "Type";
	            StringBuilder sb = new StringBuilder();
	            sb.append("{");
	            for (int k = 0; k < types.length ; k++) {
	                sb.append(types[k]);
	                if (k < types.length -1) {
	                    sb.append(", ");
	                }
	            }
	            sb.append("}");
	            data[2][1] = sb.toString();
	            
	            DefaultTableModel model = new DefaultTableModel(data, colNames);
	            
	            JTable infoTable = new JTable(model);
	            JScrollPane sp2 = new JScrollPane(infoTable);
	            opers[i].add(sp2, cc);
	            JPanel buttons = new JPanel();
	            buttons.setBorder(new EtchedBorder());
	            buttons.setLayout(new GridLayout(1,7,1,1));
	            buttons.setSize(buttonSize);
	            
	            buttons.add(new JLabel());
	            buttons.add(new JLabel());
	            buttons.add(new JLabel());
	            JButton subscribe =  new JButton("Subscribe");
	            buttons.add(subscribe);
	            if (connection == null) {
	                subscribe.setEnabled(false);
	            }
	            buttons.add(new JLabel());
                buttons.add(new JLabel());
                buttons.add(new JLabel());
                cc.gridx=0;
                cc.gridy=4;
                cc.gridwidth=1;
                cc.gridheight=1;
                cc.weightx = 1;
                cc.weighty = 0;
                opers[i].add(buttons,cc);
                
                operPanel.add(opers[i]);
                
                subscribe.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JPanel p = buildSubscriberPanel(objectName);
                        beanInfo.add("Subscription", p);
                        beanInfo.setSelectedComponent(p);
                    }
                    
                });
	           
	        }
	       
	        while (maxrows-- > 0) {
	            operPanel.add(new JLabel(" "));
	        }
	        
            if (subs.containsKey(objectName)) {
                logger.info("Restoring Subcriber for " + objectName.toString());
                JPanel p = buildSubscriberPanel(objectName);
                beanInfo.add("Subscription", p);
                //beanInfo.setSelectedComponent(p);
            }
	        return jp;
	    }
    private JPanel buildSubscriberPanel(ObjectName objectName) {
        
        logger.info("Building Subscriber for " + objectName.toString());        
        final JPanel jp = new JPanel();
        jp.setBorder(new TitledBorder( new EtchedBorder(), "Received Notifications"));
        jp.setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();
        cc.insets= new Insets(1,1,1,1);
        cc.fill = GridBagConstraints.BOTH;
        cc.gridx=0;
        cc.gridy=0;
        cc.gridwidth=7;
        cc.gridheight=9;
        cc.weightx = 1;
        cc.weighty = 1;
        
        NotificationSubscriberPanel nsp = subs.get(objectName);
        
        if (nsp == null) {
                logger.info("Subscriber not found for " + objectName.toString() + 
                        ", building new panel");
                nsp = new NotificationSubscriberPanel(objectName, connection);
                subs.put(objectName, nsp);
        }
        final NotificationSubscriberPanel panel = nsp;
        
 
        jp.add(nsp, cc);
        JButton unsubscribe = new JButton("Unsubscribe");
        JButton close = new JButton("Close");
        JButton clear = new JButton("Clear");
        cc.gridx=0;
        cc.gridy=9;
        cc.gridwidth=2;
        cc.gridheight=1;
        cc.weighty = 0;
        cc.weightx = .35;
        jp.add(new JLabel(" "), cc);
        cc.gridx=2;
        cc.gridwidth=1;
        cc.weightx = .1;
        jp.add(unsubscribe, cc);
        cc.gridx=3;
        cc.gridwidth=1;
        jp.add(clear, cc);
        cc.gridx=4;
        cc.gridwidth=1;
        jp.add(close, cc);
        cc.gridx=5;
        cc.gridwidth=2;
        cc.weightx = .35;
        jp.add(new JLabel(" "), cc);
        
        clear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                panel.clear();
                
            }
            
        });
        close.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                panel.unsubscribe();
                subs.remove(panel.getObjectName());
                beanInfo.remove(jp);
                logger.info("Removing Subscriber for " + panel.getObjectName().toString());
            }
            
        });
        unsubscribe.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                panel.unsubscribe();
                
            }
            
        });
        return jp;
    }


	private Object[][] getAttributes(AttributeList attrl ) {
	       
	       AttributeList sorted = new AttributeList(attrl);
	       Collections.sort(sorted, new AttributeComparator());
	        
	        int rows = sorted.size();
	        Object[][] data = new Object[rows] [2];
	        
	        for (int k=0; k < sorted.size(); k++) {
	            
	            Attribute val = (Attribute)sorted.get(k);
	            
	            if (val == null) {
	                data[k] [0] = "<null>";
	                data[k] [1] = "<null>";
	            } else {
	                data[k] [0] = val.getName();
	                data[k] [1] =  val.getValue() ;
	            }
	                        
	        }
	        return data;
	    }
	
	private void loadJarFile(String missingClass) {
	    
	    JFileChooser chooser = new JFileChooser();
        
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle("Locate Jar File For " + missingClass);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        String ext[] = {"jar","zip", "ear", "war", "class"}; 
        JarFileFilter filter = new JarFileFilter(ext, "Java Archive Files (*.jar)");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(path);
        
                 
        int returnVal = chooser.showSaveDialog(instance);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
           File[] jars = chooser.getSelectedFiles();
           path = chooser.getCurrentDirectory();
           for (File jar : jars) {
                try {
                    //ClassPathUpdater.addFile(jar);
                    if (options.getLibraryManager().addLibrary(jar.getAbsolutePath()) ) {
                        ClassPathUpdater.addFile(options.getLibraryManager().cacheLibrary(jar.getAbsolutePath()));
                    }
                } catch (IOException e) {
                    logger.logException(e, this);
                }  
           }
        } 
	}
	private MBeanServerConnection getJMXConnection() {
	    
	    
	    MBeanServerConnection mbsc = null;
        
        try{
            mbsc = pcm.getJMXConnection();
        } catch (ProbeCommunicationsException e) {
            e.printStackTrace();
            logger.logException(e, this);
            JOptionPane.showMessageDialog(instance, 
                    "Cannot Connect to a Remote JMX, some functions will be\n" +
                     " Unavailable",
                     "Remote Connect Error", JOptionPane.INFORMATION_MESSAGE, null);
        } 
        
        return mbsc;
	}
	public String getConnectionName() {
	    return pcm.getConnectionName();
	}
	public boolean isConnectedToJMX() {
	    return (connection != null);
	}
	private void invokeOperation(ObjectName objectName, String methodName, String returnType,
            String[] parmSignatures, JTextField[] fld) {
	    
	    
	    Object[] parameters = new Object[fld.length];
        
	    String errors = mapParms(fld, parameters, parmSignatures);
        
	    if (!errors.isEmpty()) {
	        JOptionPane.showMessageDialog(instance, 
	                "One or More Fields are Invalid: \n" +
	                 errors,
	                 "Parameter Error", JOptionPane.ERROR_MESSAGE, null);
	        return;
	    }
        //Execute the method
	    Object ret = null;
        try {
            ret = invoke(objectName, methodName, parameters, parmSignatures);
        } catch (Exception e) {
            String error = "Exception " + e.getClass().getName() + 
                    "\nocurred , error is " + e.getMessage() + 
                    "\nCalling Method " + methodName;
            logger.logException(e, this);
            JOptionPane.showMessageDialog(instance, 
                    error,
                     "Invokation Error", JOptionPane.ERROR_MESSAGE, null);
            return;
                    
        } 
        //String returnMsg = (returnType.equals("void") ? "void" : "<null>");
        if (returnType.equals("void")) {
            JOptionPane.showMessageDialog(instance, 
                    "void " + methodName + "() Executed Successfully",
                     "Invokation Complete", JOptionPane.INFORMATION_MESSAGE, null);
            return;
        } 
        JMXReturnValueDialog dlg = new JMXReturnValueDialog(JProbeUI.getStaticFrame(), ret, "Returned Value");
        
        if (dlg.isWatchable()) {
            new WatchFrame(JProbeUI.getStaticFrame(), this, objectName, methodName, parmSignatures, parameters);
        }
        
    }
	protected Object invoke(ObjectName objectName, String methodName, 
	        Object[] parameters, String[] parmSignatures) 
	                throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
	    
	    if (!pcm.isConnected())  {
	          return null;
	    }
	    
	    return connection.invoke(objectName, methodName, parameters, parmSignatures);
	}
   protected void setAttribute(ObjectName objectName, Attribute attr) throws InstanceNotFoundException, 
       AttributeNotFoundException, InvalidAttributeValueException, MBeanException, 
       ReflectionException, IOException {
        
        
          connection.setAttribute(objectName, attr);
        
   }
   protected Object getAttribute(ObjectName objectName, String attrName) throws AttributeNotFoundException, 
       InstanceNotFoundException, MBeanException, ReflectionException, IOException {
       
      if (!pcm.isConnected())  {
          return null;
      }
      return connection.getAttribute(objectName, attrName);
       
   }
   protected AttributeList getAttributes(ObjectName objectName) throws Exception {
       
       MBeanInfo info = connection.getMBeanInfo(objectName);
       MBeanAttributeInfo[] attrInfo = info.getAttributes();
       String[] attrNames = new String[attrInfo.length];
       
       for (int i = 0; i < attrNames.length; i++) {
           attrNames[i] = attrInfo[i].getName();
       }
       return connection.getAttributes(objectName, attrNames);
   }
	
	private boolean canCallParm(String type) {
	    
	    String[] validTypes = {"java.lang.String", "boolean", "int", 
	                           "short", "long", "float", "double" };
		
	    for (String valid : validTypes) {
	       if (type.equals(valid)) {
	           return true;
	       }
	            
	    }
	    
	    return false;
	}
	private String mapParms(JTextField[] fields, Object[] parms, String[] signatures) {
	    
	    String errors = "";
	            
	    for (int i = 0; i < fields.length; i++) {
	        String sig = signatures[i];
	        
	        if (sig.equals("java.lang.String")) {
	            parms[i] = fields[i].getText();
	        }
	        
	        if (sig.equals("boolean")) {
                parms[i] = Boolean.getBoolean(fields[i].getText());
            }
	        
	        if (sig.equals("int")) {
                try {
                    parms[i] = Integer.valueOf(fields[i].getText());
                } catch (NumberFormatException e) {
                    errors += "Invalid Value(" + fields[i].getText() + "), ";
                }
	        }
	        if (sig.equals("short")) {
                try {
                    parms[i] = Short.valueOf(fields[i].getText());
                } catch (NumberFormatException e) {
                    errors += "Invalid Value(" + fields[i].getText() + "), ";
                }
            }
	        if (sig.equals("long")) {
                try {
                    parms[i] = Long.valueOf(fields[i].getText());
                } catch (NumberFormatException e) {
                    errors += "Invalid Value(" + fields[i].getText() + "), ";
                }
            }
	        if (sig.equals("float")) {
                try {
                    parms[i] = Float.valueOf(fields[i].getText());
                } catch (NumberFormatException e) {
                    errors += "Invalid Value(" + fields[i].getText() + "), ";
                }
            }
	        if (sig.equals("double")) {
                try {
                    parms[i] = Double.valueOf(fields[i].getText());
                } catch (NumberFormatException e) {
                    errors += "Invalid Value(" + fields[i].getText() + "), ";
                }
            }
	    }
	    
	    return errors;
	}
	private String formatParm(String parm) {
	    
	    boolean isArray = false;
	    boolean isPrimative = false;
	    int levels = 0;
	    
	    String clazz = parm;
	    String occurs = "";
	    if (parm.startsWith("[")) {
	        isArray = true;
	        
	        char[] chars = parm.toCharArray();
	        
	        for (char c : chars) {
	            if (c == '[') {
	                levels++;
	                occurs += "[]";
	            }
	        }
	        clazz = parm.substring(levels, parm.length());
	    }
	    for (int i = 0; i < PRIMATIVE_CLASSES.length; i++) {
	        if (clazz.equals(PRIMATIVE_CLASSES[i][1])) {
	            clazz = PRIMATIVE_CLASSES[i][0];
	            isPrimative = true;
	        }
	    }
	    if (isArray) {
	        if (isPrimative) {
	            return clazz + occurs;
	        }
            String name = clazz.substring(1, clazz.length() - 1);
            return name + occurs;
	    }
	    
	    return clazz.trim();
	    
	}

	public JProbeClientFrame getClient() {
	    return client;
	}
    private String getName(ObjectName on) {
        
        String name = on.getKeyProperty("type");
        Hashtable<String, String> props = on.getKeyPropertyList();
        if (name != null) {
            return name;
        }
        
        Set<String> keys = props.keySet();
        Iterator<String> iter = keys.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            return props.get(key);
        }
        
        return "null";
        
    }
	
}
class DataRowHelper {
    
    private List<String> column = new ArrayList<String>();
    private List<List<String>> rows = new ArrayList<List<String>>();
    private int numCols = 0;
    
    public DataRowHelper(int numCols) {
        this.numCols = numCols;
    }
    
    public void addColumn(String data) {
        if (column.size() > numCols) {
            addRow();
        }
        column.add(data);
    }
    
    public void addRow() {
        
        if (column.size() < numCols) {
            return;
        }
        rows.add(column);
        column = new ArrayList<String>();
    }
    
    public String[][] getArray() {
        
        String[][] ret = new String[rows.size()] [numCols] ;
        
        for (int i = 0; i < rows.size(); i++) {
            
            List<String> row = rows.get(i);
            for (int k = 0; k < numCols; k++) {
                ret[i][k] = row.get(k);
            }
        }
        
        return ret;
    }
}
class JarFileFilter extends FileFilter {

    private String extention[];
    private String description;
    
    
    /**
     * This constructor requires the extention name and description to be
     * passed in as strings. The extention is what will determine what the
     * JFileChooser dialog box will display. The JFileChooser dialog calls
     * the accept method and will display the file if we return true.
     * 
     * @param ext String, the file extention to display i.e txt (no dot)
     * @param desc String, the description i.e "Text Files (*.txt)"
     */
    public JarFileFilter (String ext[], String desc ) {
        
        extention = ext; //new String(ext.toLowerCase());
        description = new String(desc);
        
    }
    
    
    /**
     * This method will determine if we will display the file.
     * @param f File, file object of the requested file.
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {
        
        String extension = new String();
        
        // we will return true if the file extention part of the
        // filename matches the extention we are looking for.
        // We will also return true if the file is a directory so that
        // the user can select a directory to traverse.
        
        if(f != null) {
            
            if(f.isDirectory()) {
            return true;
            }
            
            String fileName = f.getName();
            int idx = fileName.lastIndexOf('.');
            
            if (idx > 0 && idx < fileName.length() -1) {
                extension = fileName.substring(idx+1);
            }
            
            for (int i = 0; i < extention.length; i++) {
                if (extension.toLowerCase().equals(extention[i])) return true;
            }
                    
        }
        
        return false;
    }
    /**
     * This method will simply return the file description string that was 
     * passed to us in the class constructor.
     * @return description - String, the file description text
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription() { 
        
        return description;
    }
    
}
class ObjectNameComparator implements Comparator<Object> {


    @Override
    public int compare(Object o1, Object o2) {
        
        if (!(o1 instanceof ObjectName) || !(o2 instanceof ObjectName )) {
            return 0;
        }
        ObjectName on1 = (ObjectName) o1;
        ObjectName on2 = (ObjectName) o2;
        
        return getName(on1).compareTo(getName(on2));
        /*String type1 = on1.getKeyProperty("type");
        String type2 = on2.getKeyProperty("type");
        
        if (type1 == null) type1 = "null";
        if (type2 == null) type2 = "null";
        type1 += on1.getKeyPropertyList().size();
        type2 += on2.getKeyPropertyList().size();
        return type1.compareTo(type2);*/
         
        
        
    }
    private String getName(ObjectName on) {
        
        String name = on.getKeyProperty("type");
        Hashtable<String, String> props = on.getKeyPropertyList();
        if (name != null) {
            return name + props.size();
        }
        
        Set<String> keys = props.keySet();
        Iterator<String> iter = keys.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            return props.get(key);
        }
        
        return "null";
        
    }
    
}
class AttributeComparator implements Comparator<Object> {

 
    @Override
    public int compare(Object o1, Object o2) {

        Attribute attr1 = (Attribute) o1;
        Attribute attr2 = (Attribute) o2;
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return attr1.getName().compareTo(attr2.getName());
        
    }
    
}
class OperationComparator implements Comparator<MBeanOperationInfo> {

    
    @Override
    public int compare(MBeanOperationInfo o1, MBeanOperationInfo o2) {

        MBeanOperationInfo oper1 = o1;
        MBeanOperationInfo oper2 = o2;
        return oper1.getName().compareTo(oper2.getName());
        
    }
    
}