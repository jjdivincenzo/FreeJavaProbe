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
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import edu.regis.jprobe.ui.UIOptions;



/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProbeCommunicationsManager implements ICommunicationsManager {
	
	
	private int hostPort;
	private int sendCount = 0;
	private int socketConnectTimeout = 5000;
	private int socketReadTimeout = 60000;
	private static final int RESET_INTERVAL = 50;
	private String hostIP;
	private String clientIP;
	private String clientHostName;
	private String userid;
	private String password;
	private String serviceURL;
	private Socket server;
	private ProbeOutputStream out; 
	private ProbeInputStream in;
	private boolean isConnected = false;
	private volatile boolean requestInProgress = false;
	private String currentRequest;
	private boolean compress = true;
	private Logger logger;
	private MBeanServerConnection mbsc;
	private JMXConnector jmxc;
	
	public ProbeCommunicationsManager(String hostIP, int hostPort, String userid, String password) {
		
		this.hostIP = hostIP;
		this.hostPort = hostPort;
		this.userid = userid;
		this.password = password;
		this.logger = Logger.getLogger();
		
		try {
			this.clientIP = InetAddress.getLocalHost().getHostAddress();
			this.clientHostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		    logger.logException(e, this);
			this.clientIP = "127.0.0.1";
			this.clientHostName = "Unknown";
		}
		
		socketConnectTimeout = UIOptions.getOptions().getSocketConnectTimeout();
		socketReadTimeout = UIOptions.getOptions().getSocketReadTimeout();
		
				
	}
	public void connect() throws ProbeCommunicationsException {
			
       logger.info("ProbeCommunicationsManager connecting to host " + hostIP + " on Port " + hostPort);
       long start = System.currentTimeMillis();
		try {
			server = new Socket(hostIP, hostPort);
			server.setSoTimeout(socketConnectTimeout);
			server.setReuseAddress(true);
			out = new ProbeOutputStream(server.getOutputStream());
            in = new ProbeInputStream(server.getInputStream());
		} catch (UnknownHostException e) {
			throw new ProbeCommunicationsException(
					"Unknown Host Exception: " + e.getMessage() + 
					" While connecting to " + hostIP + ":" + hostPort, e);
		} catch (IOException e) {
			throw new ProbeCommunicationsException(
					"IOException: " + e.getMessage()+ 
					" While connecting to " + hostIP + ":" + hostPort, e);
		}
	      
		isConnected = true;
		sendCount = 0;
		long connectTime = System.currentTimeMillis() - start;
		
		try {
			server.setSoTimeout(socketReadTimeout);
		} catch (SocketException e) {
			logger.error("Error Setting Socket Read Timeout");
			logger.logException(e,this);
		}
		
		logger.info("Connected to " + hostIP + ":" + hostPort + 
		        ", Connect took " + connectTime + " milliseconds");
		
		
	}
	public void disconnect() throws ProbeCommunicationsException {
		
		if (!isConnected) return;
		
		if (requestInProgress) {
			throw new ProbeCommunicationsException(
					"Request [" + currentRequest + "] In Progress While Requesting Disconnect");
		}
		
		try {
			if (server.isConnected()) {
				ProbeRequest pr = new ProbeRequest();
				pr.setRequestType(ProbeRequest.REQ_DISCONNECT);
				out.writeObject(pr, compress);
			}
			
			if (mbsc != null && jmxc != null) {
			    jmxc.close();
			}
    		            
	    } catch (IOException e) {
	    	logger.logException(e, this);
	    	throw new ProbeCommunicationsException(
					"IOException: " + e.getMessage() + 
					" While disconnecting From " + hostIP + ":" + hostPort, e);
	    } finally {
	    	close();
	    	serviceURL = null;
    	
		}
	}
	
	public void close() {
		
		
		try {
			if (server != null) server.close();
		} catch (IOException e) {
			logger.logException(e, this);
			
		}
					
		server = null;
		out = null;
		in = null;
		isConnected = false; 
		logger.info("Disconnected from " + hostIP + ":" + hostPort);
		
	}
		
	public synchronized ProbeResponse sendCommand(IRequest request) 
						 throws ProbeCommunicationsException {
		
		if (!isConnected) {
			throw new ProbeCommunicationsException(
				"Not Connected Error processing sendCommand()");
		}
		if (requestInProgress) {
		    //return null;
		    throw new ProbeCommunicationsException(
	                "Request  [" + currentRequest + "] is in progress");
		}
		
		
		request.setClientIP(clientIP);
		request.setClientHostName(clientHostName);
		request.setUserId(userid);
		
		if (password != null) request.setPassword(Utilities.encrypt(password.getBytes()));
		
		ProbeResponse response = null;
		
		
	    try {
	    	long start = System.nanoTime();
	    	requestInProgress = true;
	    	currentRequest = request.getRequestName();
	    	out.writeObject(request, compress);
	    	out.flush();
		   	response = (ProbeResponse) in.readObject(compress);
	    	double end = (double) (System.nanoTime() - start) / (double) Utilities.NANOS_PER_MILLI;
	    	logger.debug("Request [" + 
	    		request.getRequestName() +  "] took " + Utilities.format(end,3) + " ms");
	    	if (end > socketReadTimeout / 2) {
	    	    logger.info("Excessive Response Time of " + Utilities.format(end,3) + " ms for a " +
	    	            currentRequest + " request");
	    	}
		   		   	
	    } catch (IOException e) {
	    	String msg = "Connection Lost due to IOException";
	    	if (e.getMessage() != null) {
	    		msg += " - Error is: " + e.getMessage();
	    	}
	    	throw new ProbeCommunicationsException(msg, e);
			    	
	    
	    } catch (ClassNotFoundException e) {
	    	throw new ProbeCommunicationsException(
					"ClassNotFoundException: " + e.getMessage() + 
					" While sending stream to " + hostIP + ":" + hostPort, e);
	    } finally {
	    	requestInProgress = false;
	    	currentRequest = "";
	    }
	    
  	    if (response.getResponseCode() == ProbeResponse.RES_INVALID_AUTHORIZATION) {
	    	disconnect();
	    	throw new ProbeCommunicationsException("Not Authorized: " + 
	    			response.getResponseErrorMessage());
	    }
	    
	    if (response.getResponseCode() == ProbeResponse.RES_SHUTDOWN) {
	    	throw new ProbeCommunicationsException("JVM Being Monitored has Terminated");
	    }
	    
	    if (response.getResponseCode() != ProbeResponse.RES_OK) {
	        Throwable thw = response.getProbeError();
            String error = response.getResponseErrorMessage();
            if (error == null) {
                error = "Request Failed";
            }
            if (thw != null) {
                logger.error("Probe Error:" + error);
                logger.logException(thw, this);
                
            }
            
	    	throw new ProbeCommunicationsException("Request Error: " +	error);
	    }
	    //Java will maintain soft references to objects serialized using
	    // an ObjectOutputStream until the stream is closed. 
	    // Since these objects will not be eligible for finalization/GC, we will run out of heap
	    // space. To avoid this, we will periodicaly reset the stream to allow
	    // the objects to be finalized.
	   	   	    
	    if (++sendCount >= RESET_INTERVAL) { 
	    	logger.debug("Resetting Client Output Stream(" +
	    			hostIP + ":" + hostPort + ") after " + sendCount + 
	    			" Sends");
	    	
	    	try {
				out.reset();
			} catch (IOException e) {
				logger.logException(e, this);
				response = null;
				throw new ProbeCommunicationsException("IOException resetting Stream", e);
			}
	    	sendCount = 0;
	    }
	    
	    if (serviceURL == null) {
	        serviceURL = response.getServiceURL();
	        logger.info("Probe Service URL is " + serviceURL);
	        
	    }
	    return response;
	}
	/**
	 * @return the isConnected
	 */
	public boolean isConnected() {
		return isConnected;
	}
	
	public String getConnectionName() {
	    return hostIP + ":" + hostPort; 
	}
	public String getServiceURL() {
	    return serviceURL;
	}
   public MBeanServerConnection getJMXConnection() throws ProbeCommunicationsException {
        
        
        if (mbsc != null) {
            return mbsc;
        }
        
        try{
            JMXServiceURL url = new JMXServiceURL(serviceURL);
            logger.info("Connecting to JMX Server at " + serviceURL);
            jmxc = JMXConnectorFactory.newJMXConnector(url, null);
            jmxc.connect();
            mbsc = jmxc.getMBeanServerConnection();
        
        } catch (IOException e) {
            throw new ProbeCommunicationsException(
                    "Cannot Connect to a Remote JMX", e);
        } 
        
        return mbsc;
    }

}
