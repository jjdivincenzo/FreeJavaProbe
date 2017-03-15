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


/**
 * @author Jim Di Vincenzo
 *
 */
public class GarbageCollectorProperties implements Externalizable {
    
    private String name;
    private long collectionCount;
    private long collectionTime;
    private String[] poolNames;
    private GCInfo lastGCInfo;
    
    /**
     * No Arg CTOR: required by Externalizable
     */
    public GarbageCollectorProperties() {
        
    }
    /**
     * CTOR:
     * @param name Collector Name
     * @param collectionCount Count
     * @param collectionTime Time
     * @param poolNames Pools Managed
     * @param lastGCInfo Last GC info
     */
    public GarbageCollectorProperties(String name, long collectionCount, long collectionTime, 
            String[] poolNames, GCInfo lastGCInfo ) {
        
        this.name = name;
        this.collectionCount = collectionCount;
        this.collectionTime = collectionTime;
        this.poolNames = poolNames;
        this.lastGCInfo = lastGCInfo;
        
    }

    /**
     * Accessor for name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Accessor for collectionCount
     * @return the collectionCount
     */
    public long getCollectionCount() {
        return collectionCount;
    }

    /**
     * Accessor for collectionTime
     * @return the collectionTime
     */
    public long getCollectionTime() {
        return collectionTime;
    }

    /**
     * Accessor for poolNames
     * @return the poolNames
     */
    public String[] getPoolNames() {
        return poolNames;
    }

    /**
     * Accessor for lastGCInfo
     * @return the lastGCInfo
     */
    public GCInfo getLastGCInfo() {
        return lastGCInfo;
    }
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("Name(" + name + 
                ") Count(" + collectionCount +
                ") Time(" + collectionTime +
                ") Pools(");
        for (String pool : poolNames) {
            sb.append(pool).append(" ");
        }
        
        sb.append(")\n\tLastGC: ").append(lastGCInfo);
        return sb.toString();
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
