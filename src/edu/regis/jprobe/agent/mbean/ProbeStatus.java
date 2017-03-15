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
package edu.regis.jprobe.agent.mbean;


import java.util.ArrayList;
import java.util.List;

import edu.regis.jprobe.agent.ProbeThread;
import edu.regis.jprobe.model.Utilities;


public class ProbeStatus implements ProbeStatusMBean {

	private String status;
	private String lastRequest;
	private String probeVersion;
	private String connectTime;
	private String serviceURL;
	private long probeCPUTime;
	private int receiveCount;
	private int sendCount;
	private int broadcastCount;
	private double probeOverhead;
	private long xformTime;
	private long receiveBytes;
	private long receiveUncompressedBytes;
	private long sendBytes;
    private long sendUncompressedBytes;
    private long totalResponseTime;
    private long maxResponseTime = 0;
    private List<Throwable> probeExceptions;
   
	
	private ProbeThread probe;
	
	public ProbeStatus(ProbeThread probe) {
	    this.probe = probe;
	    probeExceptions = new ArrayList<Throwable>();
	}
	
	/**
	 * @return the lastRequest
	 */
	public String getLastRequest() {
		return lastRequest;
	}
	/**
	 * @param lastRequest the lastRequest to set
	 */
	public void setLastRequest(String lastRequest) {
		this.lastRequest = lastRequest;
	}
	/**
	 * @return the probeCPUTime
	 */
	public long getProbeCPUTime() {
		return probeCPUTime;
	}
	/**
	 * @param probeCPUTime the probeCPUTime to set
	 */
	public void setProbeCPUTime(long probeCPUTime) {
		this.probeCPUTime = probeCPUTime; // Utilities.NANOS_PER_MILLI;
	}
	/**
	 * @return the receiveCount
	 */
	public int getReceiveCount() {
		return receiveCount;
	}
	/**
	 * @param receiveCount the receiveCount to set
	 */
	public void setReceiveCount(int receiveCount) {
		this.receiveCount = receiveCount;
	}
	/**
	 * @return the sendCount
	 */
	public int getSendCount() {
		return sendCount;
	}
	/**
	 * @param sendCount the sendCount to set
	 */
	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the probeVersion
	 */
	public String getProbeVersion() {
		return probeVersion;
	}
	/**
	 * @param probeVersion the probeVersion to set
	 */
	public void setProbeVersion(String probeVersion) {
		this.probeVersion = probeVersion;
	}
	/**
	 * @return the broadcastCount
	 */
	public int getBroadcastCount() {
		return broadcastCount;
	}
	/**
	 * @param broadcastCount the broadcastCount to set
	 */
	public void setBroadcastCount(int broadcastCount) {
		this.broadcastCount = broadcastCount;
	}
	
	/**
	 * @return the probeOverhead
	 */
	public String getProbeOverhead() {
		return Utilities.format(probeOverhead, 2) + "%";
	}
	/**
	 * @param probeOverhead the probeOverhead to set
	 */
	public void setProbeOverhead(double probeOverhead) {
		this.probeOverhead = probeOverhead;
	}
	public void disconnect() {
		probe.shutdown();
		
	}
    @Override
    public void clearDebugMessages() {
        Utilities.clearDebugMessages();
        
    }
	public long getXformTime() {
		
		return xformTime;
	}
	public void setXformTime(long t) {
		xformTime = t;
		
	}
	public String getConnectTime() {
		
		return connectTime;
	}
	public void setConnectTime() {
		connectTime = Utilities.getDateTime("MM-dd-yyyy HH:mm:ss.SSS");
		
	}
    public String getServiceURL() {
        return serviceURL;
    }
    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }
   
    @Override
    public void stopRecordingThread() {
        probe.stopProbeRecorder();
        
    }
    
    @Override
    public void startRecordingThread() {
        probe.startProbeRecorder();
        
    }

    /**
     * @return the receiveBytes
     */
    public long getReceiveBytes() {
        return receiveBytes;
    }

    /**
     * @param receiveBytes the receiveBytes to set
     */
    public void setReceiveBytes(long receiveBytes) {
        this.receiveBytes = receiveBytes;
    }

    /**
     * @return the receiveUncompressedBytes
     */
    public long getReceiveUncompressedBytes() {
        return receiveUncompressedBytes;
    }

    /**
     * @param receiveUncompressedBytes the receiveUncompressedBytes to set
     */
    public void setReceiveUncompressedBytes(long receiveUncompressedBytes) {
        this.receiveUncompressedBytes = receiveUncompressedBytes;
    }

    /**
     * @return the sendBytes
     */
    public long getSendBytes() {
        return sendBytes;
    }

    /**
     * @param sendBytes the sendBytes to set
     */
    public void setSendBytes(long sendBytes) {
        this.sendBytes = sendBytes;
    }

    /**
     * @return the sendUncompressedBytes
     */
    public long getSendUncompressedBytes() {
        return sendUncompressedBytes;
    }

    /**
     * @param sendUncompressedBytes the sendUncompressedBytes to set
     */
    public void setSendUncompressedBytes(long sendUncompressedBytes) {
        this.sendUncompressedBytes = sendUncompressedBytes;
    }

    /**
     * @return the receiveCompressionRatio
     */
    public String getReceiveCompressionRatio() {
        
        double ratio = (receiveBytes == 0 ? 0.0 : ((double)receiveUncompressedBytes / (double)receiveBytes));
        return Utilities.format(ratio, 2) + " : 1";
    }


    /**
     * @return the sendCompressionRatio
     */
    public String getSendCompressionRatio() {
        
        double ratio =  (sendBytes == 0 ? 0.0 : ((double)sendUncompressedBytes / (double)sendBytes));
        return Utilities.format(ratio, 2) + " : 1";
    }

   
    /**
     * Add an encountered exception
     * @param t the Throwable
     */
    public void addException(Throwable t) {
        probeExceptions.add(t);
    }
    
    public void addToResponseTime(long ms) {
        
        if (ms > maxResponseTime) {
            maxResponseTime = ms;
        }
        totalResponseTime += ms;
    }
    @Override
    public List<String> getExceptions() {
        
        List<String> exc = new ArrayList<String>();
        
        for (Throwable t : probeExceptions) {
            exc.add(Utilities.formatException(t, null));
        }

        return exc;

    }


    @Override
    public List<String> getDebugMessages() {
        
        return Utilities.getDebugMessages();
    }


    @Override
    public double getAverageResponseTime() {

        if (sendCount == 0) {
            return 0;
        }
        
        return (double) totalResponseTime / (double) sendCount;
    }

    @Override
    public long getMaxResponseTime() {
        
        return maxResponseTime;
    }


    @Override
    public long getClassInfoOverheadBytes() {
        // TODO Auto-generated method stub
        return probe.getClassTransformer().getClassInfoOverheadBytes();
    }



}
