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


import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.swing.ImageIcon;



class JMXTreeItem {
	
	public static final int ROOT = 0;
	public static final int DOMAIN = 1;
	public static final int BEAN = 2;
	public static final int ATTRIBUTE = 3;
	public static final int OPERATION = 4;
	public static final int VALUE = 5;
	public static final int COMPOSITE = 6;
	public static final int TABULAR = 7;

	
	
	private int type;
	private String name;
	private String domain;
	private ObjectName objectName;
	private ImageIcon icon;
	//private final String CAT_SEP = ".";
	private boolean itemExpanded = false;
	private String operName;
	private String operReturnType;
	private MBeanParameterInfo operParms[];

	
	public JMXTreeItem(int type, String name, String domain, ObjectName objectName) {
		
		this.name = name;
		this.type = type;
		this.itemExpanded = false;
		this.icon = findIcon();
		this.domain = domain;
		
		if (type == BEAN) this.objectName = objectName;
		
	}
	
	private ImageIcon findIcon() {
		
		ImageIcon icon = null;
		
		switch (type) {
			case ROOT:
				icon = IconManager.getIconManager().getRootIcon();
				break;
			case DOMAIN:
				icon = IconManager.getIconManager().getDomainIcon();
				break;
			case BEAN:
				icon = IconManager.getIconManager().getBeanIcon();
				break;
			case ATTRIBUTE:
				icon = IconManager.getIconManager().getAttributeIcon();
				break;
			case OPERATION:
				icon = IconManager.getIconManager().getOperationIcon();
				break;
			case VALUE:
				icon = IconManager.getIconManager().getValueIcon();
				break;
			case COMPOSITE:
				icon = IconManager.getIconManager().getValueIcon();
				break;
			case TABULAR:
				icon = IconManager.getIconManager().getTabularIcon();
				break;
			default:
				icon = IconManager.getIconManager().getDefaultIcon();
					
		}
		return icon;
	}

	public String toString() {
		
		if (name == null) return "<null>";
		return name;
	}
	public int getType() {
		return type;
	}
	public String format() {
		return "Name=" + name +
			    " Type=" + type + " Expanded: " + itemExpanded;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * @return the icon
	 */
	public ImageIcon getIcon() {
		return icon;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the itemExpanded
	 */
	public boolean isItemExpanded() {
		return itemExpanded;
	}
	/**
	 * @param itemExpanded the itemExpanded to set
	 */
	public void setItemExpanded(boolean itemExpanded) {
		this.itemExpanded = itemExpanded;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @return the objectName
	 */
	public ObjectName getObjectName() {
		return objectName;
	}

	/**
	 * @return the operName
	 */
	public String getOperName() {
		return operName;
	}

	/**
	 * @param operName the operName to set
	 */
	public void setOperName(String operName) {
		this.operName = operName;
	}

	/**
	 * @return the operParms
	 */
	public MBeanParameterInfo[] getOperParms() {
		return operParms;
	}

	/**
	 * @param operParms the operParms to set
	 */
	public void setOperParms(MBeanParameterInfo[] operParms) {
		this.operParms = operParms;
	}

	/**
	 * @return the operReturnType
	 */
	public String getOperReturnType() {
		return operReturnType;
	}

	/**
	 * @param operReturnType the operReturnType to set
	 */
	public void setOperReturnType(String operReturnType) {
		this.operReturnType = operReturnType;
	}
	
}