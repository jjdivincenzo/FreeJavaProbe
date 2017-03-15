////////////////////////////////////////////////////////////////////////
//
// BEGIN_COPYRIGHT
// Confidential property of Sybase, Inc.
//
// Copyright 1987-2010.
//
// Sybase, Inc. All rights reserved.
//
// Unpublished rights reserved under U.S. copyright laws.
// END_COPYRIGHT
//
// BEGIN_DISCLAIMER
// This software contains confidential and trade secret information of 
// Sybase, Inc. Use, duplication or disclosure of the software and 
// documentation by the U.S. Government is subject to restrictions set 
// forth in a license agreement between the Government and Sybase, Inc. 
// or other written agreement specifying the Government's rights to use 
// the software and any applicable FAR provisions, for example, 
// FAR 52.227-19.
//
// Sybase, Inc. One Sybase Drive, Dublin, CA 94568, USA
// END_DISCLAIMER
//
////////////////////////////////////////////////////////////////////////

package edu.regis.jprobe.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

/**
 * @author Jim Di Vincenzo
 *
 */
public class ServerGCProperties extends AbstractProperties {

    private List<GarbageCollectorProperties> collectors = new ArrayList<GarbageCollectorProperties>();
    
    /**
     * @param conn JMX Connection
     * @throws MalformedObjectNameException if Object Name is bad
     * @throws IOException  If connection is lost
     */
    public ServerGCProperties(MBeanServerConnection conn) throws MalformedObjectNameException, IOException {
        super(conn);
        
        ObjectName on = new ObjectName(GC_MBEAN_NAME);
        
        Set<ObjectName> gcNames = null;
        
        
        try {
            gcNames = conn.queryNames(on, null);
        } catch (IOException e) {
            errors.add("Error Querying GC MBeans, Error is " + e.getMessage());
        }
        
        if (gcNames != null) {
            
            Iterator<ObjectName> iter = gcNames.iterator();
            
            while (iter.hasNext()) {
                ObjectName poolON = iter.next();
                collectors.add(getCollector(poolON));        
            }
        }
        
        logErrors(this.getClass());
    }
    
    private GarbageCollectorProperties getCollector(ObjectName on) throws IOException {
        
        String name = (String) getData(on, "Name");
        long collectionCount = (long) getData(on, "CollectionCount");
        long collectionTime = (long) getData(on, "CollectionTime");
        String[] poolNames = (String[]) getData(on, "MemoryPoolNames");
        GCInfo lastGCInfo = new GCInfo((CompositeData) getData(on, "LastGcInfo"));
        
        return new GarbageCollectorProperties(name, collectionCount, collectionTime, poolNames, lastGCInfo);
    }

    /**
     * Accessor for collectors
     * @return the collectors
     */
    public List<GarbageCollectorProperties> getCollectors() {
        return collectors;
    }

}
