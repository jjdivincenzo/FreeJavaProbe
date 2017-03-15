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

public class OSProcessMemoryInfo {
	
	public long pageFaults = 0;
	public long workingSetSize = 0;
	public long peakworkingSetSize = 0;
	public long pageFileUsage = 0;
	public long peakPageFileUsage = 0;
	public long privateBytes= 0;
	
	
	public OSProcessMemoryInfo(long[] vals) {
		
		int numElements = vals.length;
		
		if (numElements > 0) pageFaults = vals[0];
		if (numElements > 1) workingSetSize = vals[1];
		if (numElements > 2) peakworkingSetSize = vals[2];
		if (numElements > 3) pageFileUsage = vals[3];
		if (numElements > 4) peakPageFileUsage = vals[4];
		if (numElements > 5) privateBytes = vals[5];
	}
}
