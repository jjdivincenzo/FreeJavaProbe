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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;



/**
 * This class contains the properties for a single server form the JVM MBeans.
 * @author Jim Di Vincenzo
 *
 */
public class ServerOSProperties extends AbstractProperties {
    
    
    
    /**
     * java.lang:type=OperatingSystem (OS MBean Data)
     */
    private int availableProcessors;
    private long committedVirtualMemorySize;
    private long freePhysicalMemorySize;
    private long freeSwapSpaceSize;
    private long totalPhysicalMemorySize;
    private long totalSwapSpaceSize;
    private double processCpuLoad;
    private long processCpuTime;
    private double systemCpuLoad;
    private double systemLoadAverage;
    
    protected List<String> errors = new ArrayList<String>();
    
    /**
     * CTOR:
     * @param conn MBean Connection
     * @throws MalformedObjectNameException on error
     * @throws IOException 
     */
    public ServerOSProperties(MBeanServerConnection conn) throws MalformedObjectNameException, IOException {
        
        super(conn);
        ObjectName on = new ObjectName(OS_MBEAN_NAME);
         
        buildSimpleAttributes(on);
        logErrors(this.getClass());
        
    }


    /**
     * Accessor for availableProcessors
     * @return the availableProcessors
     */
    public int getAvailableProcessors() {
        return availableProcessors;
    }


    /**
     * Accessor for committedVirtualMemorySize
     * @return the committedVirtualMemorySize
     */
    public long getCommittedVirtualMemorySize() {
        return committedVirtualMemorySize;
    }


    /**
     * Accessor for freePhysicalMemorySize
     * @return the freePhysicalMemorySize
     */
    public long getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }


    /**
     * Accessor for freeSwapSpaceSize
     * @return the freeSwapSpaceSize
     */
    public long getFreeSwapSpaceSize() {
        return freeSwapSpaceSize;
    }


    /**
     * Accessor for totalPhysicalMemorySize
     * @return the totalPhysicalMemorySize
     */
    public long getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }


    /**
     * Accessor for totalSwapSpaceSize
     * @return the totalSwapSpaceSize
     */
    public long getTotalSwapSpaceSize() {
        return totalSwapSpaceSize;
    }


    /**
     * Accessor for processCpuLoad
     * @return the processCpuLoad
     */
    public double getProcessCpuLoad() {
        return processCpuLoad;
    }


    /**
     * Accessor for processCpuTime
     * @return the processCpuTime
     */
    public double getProcessCpuTime() {
        return processCpuTime;
    }


    /**
     * Accessor for systemCpuLoad
     * @return the systemCpuLoad
     */
    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }


    /**
     * Accessor for systemLoadAverage
     * @return the systemLoadAverage
     */
    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    
    
}
