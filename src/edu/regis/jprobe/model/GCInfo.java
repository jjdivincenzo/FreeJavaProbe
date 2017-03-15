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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

/**
 * @author Jim Di Vincenzo
 *
 */
public class GCInfo implements Externalizable {
    
    private long gcThreadCount = 0;
    private long duration = 0;
    private long endTime = 0;
    private long startTime = 0;
    private long id = 0;
    
    private Map<String, MemoryUsage> usageBefore = new HashMap<String, MemoryUsage>();
    private Map<String, MemoryUsage> usageAfter = new HashMap<String, MemoryUsage>();
    
    /**
     * CTOR: 
     * Required by Externalizable
     */
    public GCInfo() {
        
    }
            
    /**
     * CTOR: 
     * @param cd Composite Data on GC
     */
    public GCInfo(CompositeData cd) {
        
        if (cd == null) {
            return;
        }
        
        this.gcThreadCount = (int) cd.get("GcThreadCount");
        this.duration = (long) cd.get("duration");
        this.startTime = (long) cd.get("startTime");
        this.endTime = (long) cd.get("endTime");
        this.id = (long) cd.get("id");
        
        TabularData before = (TabularData) cd.get("memoryUsageBeforeGc");
        TabularData after = (TabularData) cd.get("memoryUsageAfterGc");
        
        /*
         * Get Before Usage
         */
        Collection<?>  beforeValues = before.values();
        Iterator<?> beforeInter = beforeValues.iterator();
        
        while(beforeInter.hasNext()) {
            CompositeData pool = (CompositeData) beforeInter.next();
            String key = (String) pool.get("key");
            MemoryUsage usage = new MemoryUsage((CompositeData) pool.get("value"));
            usageBefore.put(key, usage);
        }
        
        /*
         * Get After Usage
         */
        Collection<?>  afterValues = after.values();
        Iterator<?> afterInter = afterValues.iterator();
        
        while(afterInter.hasNext()) {
            CompositeData pool = (CompositeData) afterInter.next();
            String key = (String) pool.get("key");
            MemoryUsage usage = new MemoryUsage((CompositeData) pool.get("value"));
            usageAfter.put(key, usage);
        }
    }

    /**
     * Accessor for gcThreadCount
     * @return the gcThreadCount
     */
    public long getGcThreadCount() {
        return gcThreadCount;
    }

    /**
     * Accessor for duration
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Accessor for endTime
     * @return the endTime
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Accessor for startTime
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Accessor for id
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Accessor for usageBefore
     * @return the usageBefore
     */
    public Map<String, MemoryUsage> getUsageBefore() {
        return usageBefore;
    }

    /**
     * Accessor for usageAfter
     * @return the usageAfter
     */
    public Map<String, MemoryUsage> getUsageAfter() {
        return usageAfter;
    }
    @Override
    public String toString() {
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("GCInfo ID(" + id + 
                ") Duration(" + duration +
                ") Threads(" + gcThreadCount +
                ") StartTime(" + Utilities.formatTimeStamp(startTime, "hh:mm:ss.SSS") + 
                ") EndTime(" + Utilities.formatTimeStamp(endTime, "hh:mm:ss.SSS") + ")");
        
        Map<String, MemoryUsage> beforeUsage = getUsageBefore();
        Set<String> beforeKeys = beforeUsage.keySet();
        Iterator<String> beforeIter = beforeKeys.iterator();
        sb.append("\n\tBefore GCUsage: \n");
        while (beforeIter.hasNext()) {
            String key = beforeIter.next();
            MemoryUsage usage = beforeUsage.get(key);
            sb.append("\t").append(key).append(" = ").append(usage.toString()).append("\n");
        }
        
        Map<String, MemoryUsage> afterUsage = getUsageAfter();
        Set<String> afterKeys = afterUsage.keySet();
        Iterator<String> afterIter = afterKeys.iterator();
        sb.append("\n\tAfter GCUsage: \n");
        while (afterIter.hasNext()) {
            String key = afterIter.next();
            MemoryUsage usage = afterUsage.get(key);
            sb.append("\t").append(key).append(" = ").append(usage.toString()).append("\n");
        }
        
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
