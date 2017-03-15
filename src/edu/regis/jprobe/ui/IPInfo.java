///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2006  James Di Vincenzo
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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Utility Class to Enumerate and Display Network Interfaces and their IP
 * addresses
 * 
 * @author jdivince
 * 
 */
public final class IPInfo {

    private static final int LENGTH = 25; 

    public static String getInfo(boolean showDisabled) {

        StringBuilder sb = new StringBuilder();
        
        
        try {
            /*
             * Enumerate thru the interfaces
             */
            Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces();
    
            while (inter.hasMoreElements()) {
                NetworkInterface ni = inter.nextElement();
    
                if (!showDisabled) {
                    if (!ni.isUp()) {
                        continue;
                    }
                    
                }
    
                
                byte[] mac = ni.getHardwareAddress();
    
                String macadr = "Unkown mac address";
                if (mac != null) {
                    macadr = encodeStr(mac, "-");
                }
                sb.append("Interface [" + ni.getName());
                sb.append("]\n");
                sb.append(format("Name:", ni.getDisplayName()));
                
                String status = (ni.isUp() ? "Enabled" : "Disabled");
                boolean isLoop = ni.isLoopback();
                String netType = "Virtual";
                if (ni.getParent() == null) {
                    netType = "Physical";
                }
                if (mac != null) {
                    sb.append(format(netType + " Address:", macadr));
                }
                sb.append(format("Status:", status));
                sb.append(format("Max MTU Size:", ni.getMTU()));
                sb.append(format("Supports Multicast:", ni.supportsMulticast()));
                if (isLoop) {
                    sb.append("\tLoopback Interface").append("\n");
                }
                Enumeration<InetAddress> addr = ni.getInetAddresses();
                while (addr.hasMoreElements()) {
                    InetAddress ia = addr.nextElement();
    
                    String type = null;
                    if (ia instanceof Inet6Address) {
                        type = "IPV6";
                        
                    } else {
                        type = "IPV4";
                        
                    }
                    sb.append(format(type + " Address:", ia.getHostAddress()));
    
                }
            }
            sb.append("\n");
        } catch (SocketException e) {
            return "Error Obtaining Network Information, Error is " + e.getMessage();
        }
        
        return sb.toString();
    }

    /**
     * Utility method to format and decode an MAC address
     * 
     * @param bytes
     *            Raw MAC Address
     * @param delim
     *            Delimiter (Format is 00-00-00-00)
     * @return format String
     */
    private static String encodeStr(byte[] bytes, String delim) {

        StringBuffer encodedStr = new StringBuffer();
        
        if (bytes.length == 0) {
            return "N/A";
        }

        for (int i = 0; i < bytes.length; i++) {

            int newInt = (bytes[i] & 0xf0) + (bytes[i] & 0x0f);

            String currByteStr = Integer.toHexString(newInt);
            if (currByteStr.length() == 1) {
                encodedStr.append("0");
            }

            encodedStr.append(currByteStr);
            if (i < bytes.length - 1) {
                encodedStr.append(delim);
            }
        }
        return encodedStr.toString();
    }

    private static String format(String name, Object value) {
        
        int len = LENGTH - name.length();
        String fill = "";
        if (len > 0) {
            char[] dots = new char[len];
            Arrays.fill(dots, '.');
            fill = new String(dots);
        }
        
        return "\t" + name + fill + " " + value.toString() + "\n";
    }

}
