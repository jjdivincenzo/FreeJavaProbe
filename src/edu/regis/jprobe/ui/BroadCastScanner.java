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

import static edu.regis.jprobe.model.Utilities.getMulitcastAddress;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.regis.jprobe.jni.OSSystemInfo;
import edu.regis.jprobe.model.BroadcastMessage;
import edu.regis.jprobe.model.IdentityFileManager;
import edu.regis.jprobe.model.Logger;
import edu.regis.jprobe.model.Utilities;


class BroadCastScanner extends Thread {
    
    private boolean shutdown = false;
    private Map<String, BroadcastMessage> map;
    private HashSet<Integer> localPids;
    private List<MulticastSocket> sockets = new ArrayList<MulticastSocket>();
    private static final int TTL = 1;
    private Logger logger;
    private static BroadCastScanner instance;
    private IdentityFileManager ifm;
    private String hostName;
    private UIOptions options;
    
    private BroadCastScanner() {
        
        options = JProbeUI.getOptions();
        map = new HashMap<String, BroadcastMessage>();
        localPids = new HashSet<Integer>();
        this.logger = Logger.getLogger();
        this.setName("MulticastScanner");
        this.setDaemon(true);
        
        if (Utilities.isWindows()) {
            this.ifm = new IdentityFileManager(logger);
        }
        
        this.hostName = Utilities.getHostName();
        
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        
        try {
            addresses = getMulitcastAddress();
        } catch (IOException e1) {
            logger.error("Unable to Enumerate Internet Addresses, error is " + e1.getMessage());
            try {
                MulticastSocket socket = new MulticastSocket(BroadcastMessage.MCAST_PORT);
                socket.joinGroup(InetAddress.getByName(BroadcastMessage.MCAST_ADDR));
                socket.setSoTimeout(options.getMulticastReadTimeout());
                socket.setTimeToLive(TTL);
                sockets.add(socket);
            } catch (Exception e) {
                logger.logException(e, this);
                logger.error("Unable to create Multicast Socket");
            }
        }
        
        for (InetAddress addr : addresses) {

            try {
                InetSocketAddress sa = new InetSocketAddress(addr, BroadcastMessage.MCAST_PORT);
                MulticastSocket socket = new MulticastSocket(sa); 
                if (addr instanceof Inet4Address) {
                    socket.joinGroup(InetAddress.getByName(BroadcastMessage.MCAST_ADDR));
                } else {
                    socket.joinGroup(InetAddress.getByName(BroadcastMessage.IPV6_MCAST_ADDR));
                }
                socket.setSoTimeout(options.getMulticastReadTimeout());
                socket.setTimeToLive(TTL);
                sockets.add(socket);
                logger.info("Adding Multicast for " + addr.toString());
            } catch (Exception e2) {
                logger.error("Unable to Add Multicast for " + addr.toString() + 
                        ", Error is " + e2.getMessage());
                logger.logException(e2, this);
                
            }
        }
        logger.info("Starting Multicast Scanner");
    }
    
    public synchronized static BroadCastScanner getInstance() {
        
        if (instance == null) {
            instance = new BroadCastScanner();
            instance.start();
        }
        
        return instance;
    }
    public void run() {
 
        
        while(!shutdown) {
            if (scan()) {
                Utilities.sleep(options.getMulticastScanInterval());
            } else {
                break;
            }
        }
        logger.info("Multicast Scanner Ending"); 
    }
    public void shutdown() {
        shutdown = true;
         
        try {
            this.join();
        } catch (InterruptedException e) {
            logger.logException(e, this);
        }
        instance = null;
         
    }
    public Map<String, BroadcastMessage> getHostMap() {
        
        return map;
        
    }
    private boolean scan() { 
        
        logger.debug("In Scan...");
        if (sockets.isEmpty()) {
            
            if (Utilities.isWindows()) {
                checkIdentityFile();
            } else {
                logger.error("No Eligible Multicast Sockets Available, Shutting Down");
                return false;
            }
        }
        
        //int received = 0;
        
        List<MulticastSocket> remove = new ArrayList<MulticastSocket>();
        
        for (MulticastSocket socket : sockets) {
            if (shutdown) {
                return false;
            }
            try {
                byte[] b = new byte[65535];
                ByteArrayInputStream b_in = new ByteArrayInputStream(b);
                DatagramPacket dgram = new DatagramPacket(b, b.length);
                socket.receive(dgram); // blocks until a datagram is received
                ObjectInputStream o_in = new ObjectInputStream(b_in);
                BroadcastMessage bm = (BroadcastMessage) o_in.readObject();
                
                if (bm.version == BroadcastMessage.CURRENT_VERSION) {
                    logger.debug("Received Broadcast on  " + socket.getLocalAddress().toString() +
                            ", Data:" +
                            bm.toString());
                    bm.receiveTime = System.currentTimeMillis();
                    map.put(bm.getKey(), bm);
                    logger.debug("Adding pid " + bm.processID );
                    if (bm.hostName.equalsIgnoreCase(hostName) || 
                            bm.hostName.equalsIgnoreCase("localhost")) {
                        localPids.add(bm.processID);
                    }
                    //received++;
                }
                 
            } catch (SocketTimeoutException e) {
                logger.debug("No Data From " + socket.getLocalAddress().toString());
                //Do nothing, this is an expected event
                
            } catch (InvalidClassException e) {
                //Do nothing, this may happen
                
            } catch (Exception e2) {
                logger.error("Removing " + socket.toString() + " From Scan List");
                logger.logException(e2, this);
                remove.add(socket);
            } 
        }
        
        //if (received == 0) {
            checkIdentityFile();
        //}
        return true;
        
    }
    public void clear() {
        
        map.clear();
        localPids.clear();
    }
    public boolean isConnectable(Long pid) {
        Integer val = new Integer(pid.intValue());
        boolean ret = localPids.contains(val);
        return ret;
    }
    private void checkIdentityFile() {
        logger.debug("Checking Identity File");
        if (Utilities.isWindows()) {
            List<BroadcastMessage> messages = ifm.getProbes();
            if (!messages.isEmpty()) {
                for (BroadcastMessage bm : messages) {
                    bm.receiveTime = System.currentTimeMillis();
                    if (OSSystemInfo.isOperational() ) {
                        if (OSSystemInfo.isProcessActive(bm.processID)) {
                            map.put(bm.getKey(), bm);
                            logger.debug("From Identity File, Adding pid " + bm.processID);
                            logger.debug(bm.toString());
                            localPids.add( bm.processID);
                        }
                    } else {
                        map.put(bm.getKey(), bm);
                        logger.debug("Adding pid " + bm.processID);
                        localPids.add(bm.processID);
                    }

                }
            } else {
                if (ifm.hasErrors()) {
                    for (String err : ifm.getErrors()) {
                        logger.error(err);
                    }
                    Exception e = ifm.getLastException();
                    if (e != null) {
                        logger.logException(e, this);
                    }
                }
            }
        }
    }
}