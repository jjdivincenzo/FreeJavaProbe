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
package edu.regis.jprobe.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;

/**
 * @author jdivince
 *
 */
public class ResponseMonitorInfo implements Externalizable {
    
    private String className;
    private int identityHashCode;
    private int stackDepth;
    private StackTraceElement lockingStackFrame;
    private Type type;
    
    
    public ResponseMonitorInfo() {
        
    }
    public ResponseMonitorInfo(MonitorInfo mi) {
        
        this.className = mi.getClassName();
        this.identityHashCode = mi.getIdentityHashCode();
        this.stackDepth = mi.getLockedStackDepth();
        this.lockingStackFrame = mi.getLockedStackFrame();
        this.type = Type.MONITOR;
    }
    public ResponseMonitorInfo(LockInfo mi) {
        
        this.className = mi.getClassName();
        this.identityHashCode = mi.getIdentityHashCode();
        this.type = Type.LOCK;
    }
    
    /**
     * @return the className
     */
    public final String getClassName() {
        return className;
    }

    /**
     * @return the identityHashCode
     */
    public final int getIdentityHashCode() {
        return identityHashCode;
    }

    /**
     * @return the stackDepth
     */
    public final int getStackDepth() {
        return stackDepth;
    }

    /**
     * @return the lockingStackFrame
     */
    public final StackTraceElement getLockingStackFrame() {
        return lockingStackFrame;
    }

    /**
     * @return the type
     */
    public final Type getType() {
        return type;
    }
    @Override 
    public String toString() {
        
        if (type == Type.MONITOR) {
            return "MonitorClass(" + className + ") IdentityHashCode(" + identityHashCode +
                    ") StackDepth(" + stackDepth + ") Locking Frame(" +
                    lockingStackFrame + ")";
        }
        
        return "LockClass(" + className + ") IdentityHashCode(" + identityHashCode +
                ")";

    }
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Externalizer.getInstance().writeObject(this, out);
        
    }
 
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Externalizer.getInstance().readObject(this, in);
        
    }

}
enum Type {
    MONITOR,
    LOCK
}