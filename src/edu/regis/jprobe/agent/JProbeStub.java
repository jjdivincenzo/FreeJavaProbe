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


import java.lang.instrument.Instrumentation;

import edu.regis.jprobe.model.Utilities;


/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JProbeStub {
	
	private ProbeState status;
	public static final long WAIT_TIMEOUT = 30000;
	/**
	 * ctor 
	 * @param probeName - name of the targeted JVM
	 * @param inst - Instrumentation object
	 */
	public JProbeStub(String probeName, Instrumentation inst) {
		
		status = new ProbeState();
		ProbeClassFileTransformer xform = new ProbeClassFileTransformer(status, inst);
				
		ProbeThread pt = new ProbeThread(inst, probeName, xform, status);
		startProbeThread(pt);
		Utilities.formatStackTrace();
		Utilities.debugMsg("Waiting for ProbeThread to Initialize...");
		
		//wait for the probethread to notify us to continue...
		synchronized (status) {
			try {
				status.wait(WAIT_TIMEOUT);
			} catch (InterruptedException e) {
				Utilities.debugMsg("wait interrupted...");
			}
		}
		
		if (inst != null) inst.addTransformer(xform);
		
		Utilities.debugMsg("ProbeThread Initialized, Class Transformer Active...");
	}
	private void startProbeThread(ProbeThread pt) {
	    
        pt.setDaemon(true);
        pt.setPriority(Thread.MAX_PRIORITY);
        pt.start();
	}

	/**
	 * premain required for -javaagent: parm
	 * @param agentArgs - options passed via -javaagent:JProbe.jar=parm
	 * @param inst
	 */
	public static void premain(String agentArgs, Instrumentation inst) {
		
		new JProbeStub((agentArgs != null ? agentArgs : ""), inst); 
		
		Utilities.debugMsg("premain() ended...");
		
			
	}
	public static void probeStart(String agentArgs) {
		
		new JProbeStub((agentArgs != null ? agentArgs : ""), null); 
		
		
	}
}

