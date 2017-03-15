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
import java.lang.management.GarbageCollectorMXBean;

/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GarbageCollectorData implements Externalizable { 
	
	/**
     * 
     */
    private String name;
	private long count;
	private long time;
	
	public GarbageCollectorData() {
	    
	}
	
	public GarbageCollectorData(String name, long count, long time) {
		
		this.name = name;
		this.count = count;
		this.time = time;
		
	}
	
	public GarbageCollectorData(GarbageCollectorMXBean gcBean) {
		
		this.name = gcBean.getName();
		this.count = gcBean.getCollectionCount();
		this.time = gcBean.getCollectionTime();
	}

	/**
	 * @return Returns the count.
	 */
	public long getCount() {
		return count;
	}
	/**
	 * @param count The count to set.
	 */
	public void setCount(long count) {
		this.count = count;
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
	 * @return Returns the time.
	 */
	public long getTime() {
		return time;
	}
	/**
	 * @param time The time to set.
	 */
	public void setTime(long time) {
		this.time = time;
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
