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
package edu.regis.jprobe.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MemoryPoolData implements Externalizable { 

	private String name;
	private String type;
	private long currentUsage;
	private long peakUsage;
	private long collectionUsage;
	
	
	public MemoryPoolData(MemoryPoolMXBean bean) {
		
		this.name = bean.getName();
		this.type = bean.getType().toString();
		this.currentUsage = getUsed(bean.getUsage());
		this.peakUsage = getUsed(bean.getPeakUsage());
		this.collectionUsage = getUsed(bean.getCollectionUsage());
		
		
	}

	public MemoryPoolData() {
		this.name = "";
		this.type = "";
		this.currentUsage = 0;
		this.peakUsage = 0;
		this.collectionUsage = 0;
		
	}
	protected long getUsed(MemoryUsage u) {
		
		 if (u != null) return u.getUsed();
		 return -1;
	}
	/**
	 * @return Returns the currentUsage.
	 */
	public long getCurrentUsage() {
		return currentUsage;
	}
	/**
	 * @param currentUsage The currentUsage to set.
	 */
	public void setCurrentUsage(long currentUsage) {
		this.currentUsage = currentUsage;
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
	 * @return Returns the peakUsage.
	 */
	public long getPeakUsage() {
		return peakUsage;
	}
	/**
	 * @param peakUsage The peakUsage to set.
	 */
	public void setPeakUsage(long peakUsage) {
		this.peakUsage = peakUsage;
	}
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return Returns the collectionUsage.
	 */
	public long getCollectionUsage() {
		return collectionUsage;
	}
	/**
	 * @param collectionUsage The collectionUsage to set.
	 */
	public void setCollectionUsage(long collectionUsage) {
		this.collectionUsage = collectionUsage;
	}
    public String toString() {
        return Utilities.toStringFormatter(this);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Externalizer.getInstance().writeObject(this, out);
        
    }
 
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Externalizer.getInstance().readObject(this, in);
        
    }
}
