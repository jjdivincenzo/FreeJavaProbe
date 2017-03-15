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
public class LockStackTrace {
    
    private StackTraceElement[] trace;
    private int hashCode = 0;
    
    public LockStackTrace(StackTraceElement[] trace) {
        
        if (trace == null) {
            throw new NullPointerException();
        }
       
        for (StackTraceElement el : trace) {
            hashCode += el.toString().hashCode();
        }
        this.trace = trace;
    }

    /**
     * @return the trace
     */
    public final StackTraceElement[] getTrace() {
        return trace;
    }


    public boolean equals(Object other) {
        
        if (other == null) {
            return false;
        }
        
        if (other instanceof LockStackTrace) {
            LockStackTrace otherTrace = (LockStackTrace) other;
            if (otherTrace.trace.length == this.trace.length) {
                
                for (int i = 0; i < this.trace.length; i++) {
                    
                    if (!this.trace[i].toString().equals(otherTrace.trace[i].toString())) {
                        return false;
                    }
                    
                }
                return true;
            } 
            return false;
        } 
            
        return false;

    }
    public int hashCode() {
        return hashCode;
    }
    
    public String toString() {
        return Utilities.formatStackTrace(trace);
    }

}
