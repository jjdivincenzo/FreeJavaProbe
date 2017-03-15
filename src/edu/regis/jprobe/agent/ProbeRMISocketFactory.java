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
package edu.regis.jprobe.agent;

import static edu.regis.jprobe.model.Utilities.debugMsg;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

/**
 * This class will create client and server sockets used by the RMI connector in the
 * probe thread.
 * 
 * @author jdivince
 *
 */
public class ProbeRMISocketFactory implements RMIClientSocketFactory, RMIServerSocketFactory {

    public static final int DEFAULT_BACKLOG = 10;
    public static final int DEFAULT_TIMEOUT = 0;
   
    private InetAddress ia;
    private int backlog = DEFAULT_BACKLOG;
    private int timeout = DEFAULT_TIMEOUT;
    
   
    public ProbeRMISocketFactory(InetAddress ia) {
        this(ia, DEFAULT_TIMEOUT, DEFAULT_BACKLOG);
    }
    public ProbeRMISocketFactory(InetAddress ia, int timeout, int backlog) {
        this.ia = ia;
        this.backlog = backlog;
        this.timeout = timeout;
        
    }
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket server = new ServerSocket(port, backlog, ia);
        server.setSoTimeout(timeout);
        server.setReuseAddress(true);
        debugMsg("RMI Server Socket Created on " + ia.toString() + ", using port " + port);
        return server;
    }

    
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket server = new Socket(host, port);
        server.setSoTimeout(timeout);
        server.setReuseAddress(true);
        debugMsg("RMI Client Socket Created on " + ia.toString() + 
                " to Host(" + host + ") port(" + port + ")");
        return server;
    }

}
