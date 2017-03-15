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
package edu.regis.jprobe.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.jfree.data.time.RegularTimePeriod;

class TimePeriod extends RegularTimePeriod {

    private long start;
    private long end;
    public TimePeriod(long start, long end) {
        this.start = start;
        this.end = end;
    }
    @Override
    public int compareTo(Object o) {
        
        if (o instanceof TimePeriod) {
            TimePeriod t = (TimePeriod) o;
            if (this.start > t.start) { 
                return 1;
            } else if (this.start < t.start) {
                return -1;
            }
            
        }
        return 0;
    }

    @Override
    public long getFirstMillisecond() {
        // TODO Auto-generated method stub
        return start;
    }

    @Override
    public long getFirstMillisecond(Calendar arg0) {
        // TODO Auto-generated method stub
        return start;
    }
    @Override
    public long getMiddleMillisecond(Calendar cal) { 
        return (end - start) + start;
    }
    @Override
    public long getMiddleMillisecond(TimeZone tz) { 
        return (end - start) + start;
    }
    @Override
    public long getMiddleMillisecond() {
        // TODO Auto-generated method stub
        return (end - start) + start;
    }
    @Override
    public Date getEnd() {
        return new Date(end);
    }
    @Override
    public Date getStart() {
        return new Date(start);
    }
    @Override
    public long getLastMillisecond() {
        // TODO Auto-generated method stub
        return end;
    }

    @Override
    public long getLastMillisecond(Calendar arg0) {
        // TODO Auto-generated method stub
        return end;
    }

    @Override
    public long getSerialIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RegularTimePeriod next() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void peg(Calendar arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public RegularTimePeriod previous() {
        // TODO Auto-generated method stub
        return null;
    }
    
}