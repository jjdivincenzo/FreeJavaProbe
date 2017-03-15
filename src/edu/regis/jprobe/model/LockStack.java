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

/**
 * @author jdivince
 *
 */
public class LockStack {

    private LockStackTrace stackFrame;
    private long count;
    
    public LockStack(LockStackTrace stackFrame) {
        this.stackFrame = stackFrame;
        this.count = 1;
    }
    /**
     * @return the stackFrame
     */
    public final LockStackTrace getStackFrame() {
        return stackFrame;
    }
    /**
     * @param stackFrame the stackFrame to set
     */
    public final void setStackFrame(LockStackTrace stackFrame) {
        this.stackFrame = stackFrame;
    }
    /**
     * @return the count
     */
    public final long getCount() {
        return count;
    }
    /**
     * @param count the count to set
     */
    public final void setCount(long count) {
        this.count = count;
    }
    /**
     * increment the count
     */
    public final void incrementCount() {
        this.count++;
    }
    
    public String toString() {
        
        return "Waiting Frame[" + stackFrame + "] Count[" + count + "]";
    }
}
