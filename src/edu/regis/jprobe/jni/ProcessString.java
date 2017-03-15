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
package edu.regis.jprobe.jni;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jdivinc
 *
 */
public class ProcessString {
    
    private List<String> list = new ArrayList<String>();
    
    public ProcessString() {
        
    }
    public void add(String val) {
        list.add(val);
    }
    
    public List<OSProcessInfo> getProcessList() {
        
        List<OSProcessInfo> ret = new ArrayList<OSProcessInfo>();
        
        for (String proc : list) {
            
            OSProcessInfo pi = new OSProcessInfo(proc);
            ret.add(pi);
        }
        
        return ret;
    }
    
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        
        for (String val : list) {
            sb.append(val);
            sb.append("\n");
        }
        
        return sb.toString();
    }

}
