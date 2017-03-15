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

import javax.swing.ImageIcon;

import edu.regis.jprobe.model.Logger;


/**
 * This class manages all of the icons used by the application. It loads the Icons
 * from the resources in the jar file.
 * @author jdivince
 *
 */
public class IconManager {
	
	private ImageIcon upIcon;
	private ImageIcon downIcon;
	private ImageIcon mainIcon;
	private ImageIcon frameIcon;
	private ImageIcon threadIcon;
	private ImageIcon rootIcon;
	private ImageIcon domainIcon;
	private ImageIcon beanIcon;
	private ImageIcon attributeIcon;
	private ImageIcon operationIcon;
	private ImageIcon valueIcon;
	private ImageIcon tabularIcon;
	private ImageIcon defaultIcon;
	private ImageIcon splashIcon;
	
	
	private final String upGif = "resources/SortUp.gif";
	private final String downGif = "resources/SortDown.gif";
	private final String frameGif = "resources/frame.gif";
	private final String threadGif = "resources/thread.gif";
	private final String mainGif = "resources/main.gif";
	private final String rootGif = "resources/root.gif";
	private final String domainGif = "resources/domain.gif";
	private final String beanGif = "resources/bean.gif";
	private final String attributeGif = "resources/attribute.gif";
	private final String operationGif = "resources/operation.gif";
	private final String defaultGif = "resources/default.gif";
	private final String valueGif = "resources/value.gif";
	private final String tabularGif = "resources/tabular.gif";
	private final String splashGif = "resources/splash.gif";

	
	
	private static IconManager instance;
	
	private IconManager() {
		
	    Logger.getLogger().debug("Initializing Icon Manager");
		
		upIcon = loadIcon(upGif);
		downIcon = loadIcon(downGif);
		threadIcon = loadIcon(threadGif);
		mainIcon = loadIcon(mainGif);
		frameIcon = loadIcon(frameGif);
		rootIcon = loadIcon(rootGif);
		domainIcon = loadIcon(domainGif);
		beanIcon = loadIcon(beanGif);
		attributeIcon = loadIcon(attributeGif);
		operationIcon = loadIcon(operationGif);
		defaultIcon = loadIcon(defaultGif);
		valueIcon = loadIcon(valueGif);
		tabularIcon = loadIcon(tabularGif);
		splashIcon = loadIcon(splashGif);

		
	}
	
	public static synchronized IconManager getIconManager() {
		
		if (instance == null) {
			
			instance = new IconManager();
		}
		
		return instance;
		
	}
	
	private ImageIcon loadIcon(String name) {
		
		Logger.getLogger().info("Loading Icon " + name);
		
		java.net.URL imgURL = IconManager.class.getResource(name);
		
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, name);
	    }
	    
        Logger.getLogger().warning("Couldn't load Icon find file: " + name);
        return null;
		
	}

	/**
	 * @return the downIcon
	 */
	public ImageIcon getDownIcon() {
		return downIcon;
	}

	/**
	 * @return the frameIcon
	 */
	public ImageIcon getFrameIcon() {
		return frameIcon;
	}

	/**
	 * @return the mainIcon
	 */
	public ImageIcon getMainIcon() {
		return mainIcon;
	}

	/**
	 * @return the threadIcon
	 */
	public ImageIcon getThreadIcon() {
		return threadIcon;
	}

	/**
	 * @return the upIcon
	 */
	public ImageIcon getUpIcon() {
		return upIcon;
	}

	/**
	 * @return the attributeIcon
	 */
	public ImageIcon getAttributeIcon() {
		return attributeIcon;
	}

	/**
	 * @return the beanIcon
	 */
	public ImageIcon getBeanIcon() {
		return beanIcon;
	}

	/**
	 * @return the defaultIcon
	 */
	public ImageIcon getDefaultIcon() {
		return defaultIcon;
	}

	/**
	 * @return the domainIcon
	 */
	public ImageIcon getDomainIcon() {
		return domainIcon;
	}

	/**
	 * @return the operationIcon
	 */
	public ImageIcon getOperationIcon() {
		return operationIcon;
	}

	/**
	 * @return the rootIcon
	 */
	public ImageIcon getRootIcon() {
		return rootIcon;
	}

	/**
	 * @return the valueIcon
	 */
	public ImageIcon getValueIcon() {
		return valueIcon;
	}

	/**
	 * @return the tabularIcon
	 */
	public ImageIcon getTabularIcon() {
		return tabularIcon;
	}

	public ImageIcon getSplashScreen() {
	    return splashIcon;
	}
	
	

}
