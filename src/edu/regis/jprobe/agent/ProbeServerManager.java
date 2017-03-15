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
package edu.regis.jprobe.agent;

import static edu.regis.jprobe.model.Utilities.VERSION_HEADING;
import static edu.regis.jprobe.model.Utilities.debugMsg;
import static edu.regis.jprobe.model.Utilities.formatException;
import static edu.regis.jprobe.model.Utilities.sleep;
import static edu.regis.jprobe.model.Utilities.getMulitcastAddress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import edu.regis.jprobe.model.BroadcastMessage;
import edu.regis.jprobe.model.IdentityFileManager;
import edu.regis.jprobe.model.ProbeInputStream;
import edu.regis.jprobe.model.ProbeOutputStream;
import edu.regis.jprobe.model.ProbeRequest;
import edu.regis.jprobe.model.ProbeResponse;
import edu.regis.jprobe.model.Utilities;


public class ProbeServerManager {
	
	private ProbeInputStream in;
	private ProbeOutputStream out;
	private Socket client;
	private ServerSocket server;
	private List<DatagramSocket> sockets = new ArrayList<DatagramSocket>();
	private ByteArrayOutputStream b_out;
	private ObjectOutputStream o_out;
	private BroadcastMessage bm;
	private DatagramPacket dgram;
	private DatagramPacket dgramIPV6;
	private JMXConnectorServer jmxConnServer;
	private int port  = BroadcastMessage.TCP_PORT;
	private int sendResetCount = 0;
	private String interfaceName;
	private Registry localRegistry = null;
	private static final int RESET_COUNT = 50;
	private static final int ACCEPT_TIMEOUT = 3000;
	private static final int INIT_ACCEPT_TIMEOUT = 10;
	public static final long NANOS_PER_MILLI = 1000000;
	public static final String PREFERRED_NETWORK_INTERFACE = "JPROBE_PREFERRED_NETWORK";
	public static final String PREFERRED_NETWORK_IP = "JPROBE_PREFERRED_IP";
	public static final String SERVICE_PREFIX = "service:jmx:rmi:///jndi/rmi://";
	//Preferred 
	private String probeName;
	private String clientIP;
	private int sendCount=0;
	private int receiveCount=0;
	private int broadcastCount = 0;
	private ProbeState status;
	private boolean once = true;
	private boolean compress = true;
	private String preferredNetwork = null;
	private String preferredIP = null;
	private String serviceURL = null;
	private IdentityFileManager ifm;
		
	public ProbeServerManager(String probeName, ProbeState status) throws ProbeServerManagerException {
		
	    preferredNetwork = System.getenv(PREFERRED_NETWORK_INTERFACE);
	    preferredIP = System.getenv(PREFERRED_NETWORK_IP);
	        
		this.probeName = probeName;
		this.status = status;
		
		if (Utilities.isWindows()) {
		    this.ifm = new IdentityFileManager(null);
		}
		
		int tries = 0;
		
		List<InetAddress> addresses = new ArrayList<InetAddress>();
        try {
            addresses = getMulitcastAddress();
        } catch (IOException e1) {
            debugMsg("Unable to Enumerate Internet Addresses, error is " + e1.getMessage());
            try {
                sockets.add(new DatagramSocket());
            } catch (SocketException e) {
                throw new ProbeServerManagerException("Error Creating Broadcast Socket Stub Disabled");
            }
        }
		
		for (InetAddress addr : addresses) {
    		try {
    		    DatagramSocket socket = new DatagramSocket(0,addr);
    			sockets.add(socket);
    			debugMsg("Adding Multicast for " + addr.toString());
    		} catch (SocketException e2) {
    			debugMsg("Unable to Add Multicast for " + addr.toString());
    			
    		}
		}
		
		while (true) {
	        try
		      {
	        	++tries;
	        	
	        	InetAddress ia = getNetworkAddress();
	        	server = new ServerSocket(port, 10, ia);
		      	server.setSoTimeout(INIT_ACCEPT_TIMEOUT);
		      	server.setReuseAddress(true);
		      	createJMXConnector(ia);
		      	break;
		      } catch (IOException e ) {
		      	if (port++ > BroadcastMessage.MAX_PORT) {
		      		throw new ProbeServerManagerException("Unable to Obtain an Available Port, Stub Disabled");
		      		
		      	}
		      }
	     }
		 createBroadcast();
		 
		 debugMsg(tries + " Tries needed to create TCP Socket");
		 Thread.currentThread().setName("Probe Thread:" + port);
		 System.out.println(VERSION_HEADING + 
		         " - Monitor Thread Started, Bound on Port " + port + ", On Interface " + 
		         interfaceName);
	}
	public void waitForConnect() throws ProbeServerManagerException {
		
	    if (Utilities.isWindows()) {
            if (!ifm.saveBroadcast(bm)) {
                debugMsg("Unable to write BroadcastMessage");
                if (ifm.hasErrors()) {
                    for (String error : ifm.getErrors()) {
                        debugMsg(error);
                    }
                }
            } else {
                debugMsg("Broadcast Message Saved to " + ifm.getFileName());
            }
        }
	    		
		while (client == null) {
			sendBroadcast();
			client = acceptConnect();
			if (once) {
				Utilities.debugMsg("ProbeThread Initialization is Complete");
				status.setInitialized(true);
				//At this point, we are operational...
				//Tell the main thread...
			
				synchronized (status) {
					status.notifyAll();
				}
				once = false;
				try {
					server.setSoTimeout(ACCEPT_TIMEOUT);
				} catch (SocketException e) {
					Utilities.debugMsg("Error: " + e.getMessage() + 
							" while setting timeout");
				}
			}
        }
		
        //	Set up the streams
        try {
            in = new ProbeInputStream(client.getInputStream());
            out = new ProbeOutputStream(client.getOutputStream());
			
		} catch (IOException e) {
			String host = client.getInetAddress().getHostAddress();
			disconnect();
			throw new ProbeServerManagerException("IOException trying to connect with " +
					host);
		}
        try {
            if (!jmxConnServer.isActive()) {
                debugMsg("Starting JMX Connector");
                jmxConnServer.start();
                debugMsg("JMX Connector Successfully Started");
                if (localRegistry != null) {
                    String[] stubs = localRegistry.list();
                    for (String stub : stubs) {
                        debugMsg("Stub = " + stub);
                    }
                }
            }
        } catch (IOException e) {
            debugMsg("Error " + e.getMessage() + " while starting JMX Connector for client " +
                    client.getInetAddress().getHostName());
        }
        
     	InetAddress i = client.getInetAddress();
     	clientIP = i.getHostAddress();
     	debugMsg("Probe Connected to " + 
     			i.getHostName() + 
				":" + "(" + 
				i.getHostAddress() +
				"):" + port);
     	
     	Thread.currentThread().setName("Probe Thread:(" +
     			i.getHostAddress() +
				":" +  port + ")");
     	
     	sendResetCount = 0;
     	
     	if (Utilities.isWindows()) {
            if (!ifm.removeBroadcast(bm)) {
                debugMsg("Unable to remove BroadcastMessage");
                if (ifm.hasErrors()) {
                    for (String error : ifm.getErrors()) {
                        debugMsg(error);
                    }
                }
            } else {
                debugMsg("Broadcast Message removed");
            }
        }
		
	}
	public void disconnect() {
		
		if (client.isConnected()){
			debugMsg("Probe Disconnecting From " + 
	     			client.getInetAddress().getHostName() + 
					":" + "(" + 
					client.getInetAddress().getHostAddress() +
					"):" +  port);
			
			try {
				in.close();
				out.close();
				client.close();
				
			} catch (IOException e) {
				debugMsg("Error " + e.getMessage() + " while disconnecting from " +
						client.getInetAddress().getHostName());
			}
		}
		client = null;
		Thread.currentThread().setName("Probe Thread:" + port);
	}
	
	public ProbeRequest getRequest() throws ProbeServerManagerException {
		
		if (!client.isConnected()) return null;
		
		try {
			receiveCount++;
			return (ProbeRequest)in.readObject(compress);
			
		} catch (IOException e) {
			throw new ProbeServerManagerException("IOException Getting Request, Error is " + 
					e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			throw new ProbeServerManagerException("ClassNotFoundException Getting Request");
		}
		
		
		
	}
	public void sendResponse(ProbeResponse response) throws ProbeServerManagerException {
		
	    response.setServiceURL(serviceURL);
		try {
			long start = System.nanoTime();
			out.writeObject(response, compress);
			out.flush();
	    	double end = (double) (System.nanoTime() - start) / (double) NANOS_PER_MILLI;
	    	Utilities.debugMsg("Response output took " + Utilities.format(end,3) + " ms");
			sendCount++;
		} catch (IOException e) {
		    //e.printStackTrace();
			throw new ProbeServerManagerException("IOException Sending Response", e);
		}
		
		//Java will maintain soft references to objects serialized/deserialized using
	    // an ObjectOutputStream until the stream is closed. 
	    // Since these objects will not be eligible for finalization/GC, we will run out of heap
	    // space. To avoid this, we will periodicaly reset the stream to allow
	    // the objects to be finalized.
		
		if (++sendResetCount >= RESET_COUNT) {
				try {
					debugMsg("Resetting Server Output Stream(" +
							client.getInetAddress().getHostAddress() +
							":" +  port + 
							") after " +
							sendResetCount + " sends");
					out.reset();
				} catch (IOException e) {
					debugMsg(formatException(e, this));
				}
				sendResetCount = 0;
		}
	}
	protected Socket acceptConnect() {
       
        
        try {
            return server.accept( );
        
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
        	debugMsg(formatException(e, this));
        }
        
        return null;
        
    }
	/**
     * This method will send out a UDP multi-cast message specifying our
     * host name and port address that we are listening on for a TCP connection.
     * @param socket
     */
    private void sendBroadcast() {
    	
        List<DatagramSocket> remove = new ArrayList<DatagramSocket>();
        for (DatagramSocket socket : sockets) {
            
            InetAddress ia = socket.getLocalAddress();
    		try {
    			
    		  //debugMsg("Sending Broadcast Message on " + ia.toString() +
    		  //        ". Data: " + bm );
    		  
    		  if (ia instanceof Inet4Address) {
    		      socket.send(dgram);
    		  } else {
    		      socket.send(dgramIPV6);
    		  }
    		  broadcastCount++;
    		 
    		} catch (IOException e1) {
    			debugMsg("Error Sending Broadcast Message on " + ia.toString() + 
    			        ": Error is " +
    					e1.getMessage() + 
    					". Interface removed form Broadcast List");
    			remove.add(socket);
    		}
        }
		
        if (!remove.isEmpty()) {
            sockets.removeAll(remove);
        }
    }
	/**
     * This method will create a UDP multi-cast message specifying our
     * host name and port address that we are listening on for a TCP connection.
     * @param socket
     */
    private void createBroadcast() {
    	
    	
		
		String msg = null;
		String ipAddress = "127.0.0.1";
		
	    msg = server.getInetAddress().getHostName();
	    ipAddress = server.getInetAddress().getHostAddress();
		
		try {
			b_out = new ByteArrayOutputStream();
			o_out = new ObjectOutputStream(b_out);
			
			bm = new BroadcastMessage(probeName, msg, ipAddress, port);
			o_out.writeObject(bm);
			byte[] b =  b_out.toByteArray();
			
			dgram = new DatagramPacket(b, b.length,
			  InetAddress.getByName(BroadcastMessage.MCAST_ADDR), BroadcastMessage.MCAST_PORT);
			dgramIPV6 = new DatagramPacket(b, b.length,
		              InetAddress.getByName(BroadcastMessage.IPV6_MCAST_ADDR), BroadcastMessage.MCAST_PORT);
				
		} catch (IOException e1) {
			debugMsg("Error Creating Broadcast Message:\n " +
					e1.getMessage());
		}
		
		
		
		debugMsg("Broadcast Message Created: " + bm );
    }
	/**
	 * @return the clientIP
	 */
	public String getClientIP() {
		return clientIP;
	}
    
	public void shutdown() {
		
		debugMsg("ProbeServerManager Shutting Down");
		if (client != null && client.isConnected()){
			try {
				ProbeResponse res = new ProbeResponse();
				res.setResponseCode(ProbeResponse.RES_SHUTDOWN);
				res.setResponseErrorMessage("JVM is Terminating");
				sendResponse(res);
			} catch (Exception e) {
				debugMsg(formatException(e, this));
			}
		}
		if (Utilities.isWindows()) {
            if (!ifm.removeBroadcast(bm)) {
                debugMsg("Unable to remove BroadcastMessage");
                if (ifm.hasErrors()) {
                    for (String error : ifm.getErrors()) {
                        debugMsg(error);
                    }
                }
            } else {
                debugMsg("Broadcast Message removed");
            }
        }
		
		sleep(100);
		debugMsg("ProbeServerManager Terminated");
	}
	/**
	 * @return the receiveCount
	 */
	public int getReceiveCount() {
		return receiveCount;
	}
	/**
	 * @return the sendCount
	 */
	public int getSendCount() {
		return sendCount;
	}
	public long getSendBytes() {
	    return out.getBytesWritten();
	}
	public long getSendUncompressedBytes() {
        return out.getBytesWrittenUncompressed();
    }
	public long getReceiveBytes() {
	    return in.getBytesRead();
	}
	public long getReceiveUncompressedBytes() {
        return in.getBytesReadUncompressed();
    }
	public int getBroadcastCount() {
		return broadcastCount;
	}
	public String getServiceURL() {
	    return serviceURL;
	}
	private InetAddress getNetworkAddress() throws IOException {
	    
	    //Override the selected Network Interface
	    if (preferredNetwork != null) {
	        debugMsg("Looking for Interface by Name");
	        NetworkInterface ni = NetworkInterface.getByName(preferredNetwork);
	        if (ni != null) {
	            Enumeration<InetAddress> addr = ni.getInetAddresses();
	            while(addr.hasMoreElements()) {
	                InetAddress ia = addr.nextElement();
	                if (ia instanceof Inet4Address) {
	                    debugMsg("Returning Interface " + 
	                            ia.toString());
	                    interfaceName = ni.getName() + "(" + ia.toString() + ")" ;
	                    return ia;
	                }
	            }
	        }
	        
	    }
	    
	    if (preferredIP != null) {
	        debugMsg("Looking for Interface by IPAddress");
	        InetAddress addr = InetAddress.getByName(preferredIP);
	        if (addr != null) {
	            NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
	            if (ni != null) {
	                Enumeration<InetAddress> adEnum = ni.getInetAddresses();
	                while(adEnum.hasMoreElements()) {
	                    InetAddress ia = adEnum.nextElement();
	                    if (ia instanceof Inet4Address) {
	                        debugMsg("Returning Interface " + 
	                                ia.toString());
	                        interfaceName = ni.getName() + "(" + ia.toString() + ")" ;
	                        return ia;
	                    }
	                }
	            }
	        }
            
        }
	    debugMsg("Looking for First Available Interface");
	    Enumeration<NetworkInterface> inter = NetworkInterface.getNetworkInterfaces();

        while (inter.hasMoreElements()) {
            NetworkInterface ni = inter.nextElement();
            
            if (ni.isLoopback()) {
                continue;
            }
            if (!ni.supportsMulticast()) {
                continue;
            }
            
            if (!ni.isUp()) {
                continue;
            }
                        
            Enumeration<InetAddress> addr = ni.getInetAddresses();
            while(addr.hasMoreElements()) {
                InetAddress ia = addr.nextElement();
                
                if (ia instanceof Inet4Address) {
                    if (ia.isReachable(100)) {
                        debugMsg("Returning Interface " + 
                                ia.toString());
                        interfaceName = ni.getName() + "(" + ia.toString() + ")" ;
                        return ia;
                    }
                }
                
                
            }
            
        }
        debugMsg("Returning Default Localhost Interface of " + 
                InetAddress.getLocalHost().toString());
        interfaceName =  InetAddress.getLocalHost().getHostAddress();
        return InetAddress.getLocalHost();
	}
    private boolean createJMXConnector(InetAddress ia) {
        
        
        int port = BroadcastMessage.MAX_PORT;
        while (true) {
            
            ProbeRMISocketFactory sf = new ProbeRMISocketFactory(ia);
            try {
                localRegistry = LocateRegistry.createRegistry(port, sf, sf);
                break;
            } catch (RemoteException e) {
                if (port++ > BroadcastMessage.MAX_PORT + 100) {
                    port = -1;
                    e.printStackTrace();
                    break;
                }
            }
        }
        
        if (port == -1) {
            debugMsg("Max Retries attempting to start RMI Registry");
            serviceURL = "N/A";
            return false;
        }
        serviceURL = SERVICE_PREFIX +
                ia.getHostAddress() + ":" + 
                port + "/jprobe";
        
        MBeanServer mbServer = ManagementFactory.getPlatformMBeanServer();
        
        try {
           JMXServiceURL url = new JMXServiceURL(serviceURL);
           jmxConnServer =
                JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbServer);
            
            debugMsg("Creating MBean Server at " + serviceURL);
            
        } catch (IOException e) {
            debugMsg("Unable to start JMX Server");
            e.printStackTrace();
            return false;
        }
        debugMsg("JMX Connector URL = " + serviceURL);
        return true;
    }
    

    
}
