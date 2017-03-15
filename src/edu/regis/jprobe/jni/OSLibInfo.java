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
package edu.regis.jprobe.jni;

import java.io.File;
import java.io.Serializable;

import edu.regis.jprobe.model.Utilities;

public class OSLibInfo implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String name;
	private String path;
	private long size;
	private long loadAddress;
	
	public OSLibInfo(String name, String path, long size, long loadAddress) {
		this.name = name;
		this.path = path;
		this.size = size;
		this.loadAddress = loadAddress;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}
	
	public String getLoadAddress() {
	    return "0x" + Long.toHexString(loadAddress).toUpperCase();
	}
	
	public String getFileDate() {
	    File f = new File(path);
	    
	    if (f.exists()) {
	        return Utilities.formatTimeStamp(f.lastModified(), "MM/dd/yyyy HH:mm:ss");
	    } 
	    
	    return "N/A";
	}
	
	public String toString() {
		
		return name + 
				" (" + path +
				") Size(" + Utilities.format(size) + ") Addr(" +
				getLoadAddress() + ") Last Modified(" + getFileDate() + ")"; 
	}
}
