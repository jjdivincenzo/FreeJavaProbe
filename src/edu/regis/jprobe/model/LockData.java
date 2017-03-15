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

import java.util.ArrayList;
import java.util.List;

/**
 * @author jdivince
 *
 */
public class LockData {
    
    private String lockName;
    private String type;
    private int lockCount;
    private List<LockStack> stackList = new ArrayList<LockStack>();
    
    public LockData(String name) {
        this.lockName = name;
        this.lockCount = 1;
    }
    /**
     * @return the lockName
     */
    public final String getLockName() {
        return lockName;
    }
    /**
     * @param lockName the lockName to set
     */
    public final void setLockName(String lockName) {
        this.lockName = lockName;
    }
    /**
     * @return the lockCount
     */
    public final int getLockCount() {
        return lockCount;
    }
    /**
     * @param lockCount the lockCount to set
     */
    public final void setLockCount(int lockCount) {
        this.lockCount = lockCount;
    }
    /**
     * increment the count
     */
    public final void incrementLockCount() {
        this.lockCount++;
    }
    
    /**
     * @return the type
     */
    public final String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public final void setType(String type) {
        this.type = type;
    }
    public void addStack(LockStackTrace stack) {
        
        for (LockStack stk : stackList) {
            if (stk.getStackFrame().equals(stack)) {
                stk.incrementCount();
                return;
            }
        }
        
        LockStack ls = new LockStack(stack);
            stackList.add(ls);
        
    
    }
    public int getStackSize() {
        return stackList.size();
    }
    public List<LockStack> getStacks() {
        return stackList;
        /*List<LockStack> ret = new ArrayList<LockStack>();
        
       
        Set keys = stackMap.keySet();             
        Iterator iter = keys.iterator();        
        
        while (iter.hasNext()) {
            String key = (String) iter.next();
            ret.add(stackMap.get(key));
    
        }
        
        return ret;*/
        
    }
    
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("Lock Name[")
        .append(lockName)
        .append("] Type[")
        .append(type)
        .append("] Count[")
        .append(lockCount)
        .append("]\n")
        .append("\tFrames\n");
        
        List<LockStack> stacks = getStacks();
        for (LockStack stack : stacks) {
            sb.append("\t\t")
            .append(stack.toString())
            .append("\n");
        }
        
        return sb.toString();
        
    }

}
