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
package edu.regis.jprobe.jni;

public class OSIOCounters {

	public long ioReadCount = 0;
	public long ioWriteCount = 0;
	public long ioOtherCount = 0;
	public long ioTotalCount = 0;
	public long ioReadBytes = 0;
	public long ioWriteBytes = 0;
	public long ioOtherBytes = 0;
	public long ioTotalBytes = 0;
	
	public OSIOCounters(long[] vals) {
		
		int numElements = vals.length;
		
		if (numElements > 0) ioReadCount  = vals[0];
		if (numElements > 1) ioWriteCount = vals[1];
		if (numElements > 2) ioOtherCount = vals[2];
		if (numElements > 3) ioReadBytes  = vals[3];
		if (numElements > 4) ioWriteBytes = vals[4];
		if (numElements > 5) ioOtherBytes = vals[5];
		
		ioTotalCount = ioReadCount + ioWriteCount + ioOtherCount;
		ioTotalBytes = ioReadBytes + ioWriteBytes + ioOtherBytes;
	}
}
