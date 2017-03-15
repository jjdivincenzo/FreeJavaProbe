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

import java.io.Serializable;

/**
 * This class describes a Broadcast Message
 * @author jdivince
 *
 * 
 */
public class BroadcastMessage implements Serializable {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final String MCAST_ADDR = "225.100.200.69";
    public static final String IPV6_MCAST_ADDR = "FF7E:230::69";
	public static final int MCAST_PORT = 32310;
	public static final int TCP_PORT = 32311;
	public static final int MAX_PORT = 32400;
	public static final String DELIM = ":";
	public static final int CURRENT_VERSION = 1003;
	public String idName;
	public String hostName;
	public String hostIP;
	public int portNumber;
	public int version;
	public int processID;
	public long receiveTime;

	public BroadcastMessage(String id, String name, String ip, int port) {
		idName = id;
		hostName = name;
		portNumber = port;
		hostIP = ip;
		version = CURRENT_VERSION;
		processID = Utilities.getProcessID();
		
	}
	public String getKey() {
		return idName + DELIM +
			   hostName + DELIM + 
			   hostIP + DELIM +
			   portNumber;
	}
	public String toString() {
		return  Utilities.toStringFormatter(this);
	}
	public boolean equals(Object otherOne) {
	    
	    if (otherOne == null) {
	        return false;
	    }
	    
	    if (otherOne instanceof BroadcastMessage) {
	        BroadcastMessage other = (BroadcastMessage) otherOne;
	        if (this.idName.equals(other.idName) && 
	            this.hostName.equals(other.hostName) &&
	            this.hostIP.equals(other.hostIP) && 
	            this.portNumber == other.portNumber &&
	            this.processID == other.processID) {
	            return true;
	            
	        }
	                 
	        
	    }
	    
	    return false;
	}
	
}
