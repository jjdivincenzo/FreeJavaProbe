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
//    Lesser General License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
/////////////////////////////////////////////////////////////////////////////////// 
package edu.regis.jprobe.agent.mbean;

import java.util.List;


public interface ProbeStatusMBean {
	
	String getStatus();
	
	int getSendCount();
	
	long getSendBytes();
	
	long getSendUncompressedBytes();
	
	int getReceiveCount();
	
	long getReceiveBytes();
	
	long getReceiveUncompressedBytes();
	
	String getReceiveCompressionRatio();
	
	String getSendCompressionRatio();
	
	long getProbeCPUTime();
	
	String getProbeVersion();
	
	String getLastRequest();
	
	int getBroadcastCount();
	
	String getProbeOverhead();
	
	long getXformTime();
	
	String getConnectTime();
	
	String getServiceURL();
	
	void disconnect();
	
	void stopRecordingThread();
	
	void startRecordingThread();
	
	void clearDebugMessages();
	
	List<String> getExceptions();
	
	List<String> getDebugMessages();
	
	double getAverageResponseTime();
	
	long getMaxResponseTime();
	
	long getClassInfoOverheadBytes();

}
