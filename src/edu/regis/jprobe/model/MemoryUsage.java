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

import javax.management.openmbean.CompositeData;

/**
 * This class houses memory usage data.
 * @author Jim Di Vincenzo
 *
 */
public class MemoryUsage implements Externalizable {
    
    private long committed;
    private long init;
    private long max;
    private long used;
    
    /**
     * CTOR:
     * Required by Externalizable
     */
    public MemoryUsage() {
        
    }
    /**
     * CTOR:
     * @param cd Composite Data
     */
    public MemoryUsage(CompositeData cd) {
        
        committed = (long) cd.get("committed");
        init      = (long) cd.get("init");
        max       = (long) cd.get("max");
        used      = (long) cd.get("used");
    }

    /**
     * Accessor for committed
     * @return the committed
     */
    public long getCommitted() {
        return committed;
    }

    /**
     * Accessor for init
     * @return the init
     */
    public long getInit() {
        return init;
    }

    /**
     * Accessor for max
     * @return the max
     */
    public long getMax() {
        return max;
    }

    /**
     * Accessor for used
     * @return the used
     */
    public long getUsed() {
        return used;
    }
    
    @Override
    public String toString() {
        return "Committed(" + committed + ") Init(" + init + ") Max(" + max + ") Used(" + used + ")";
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
