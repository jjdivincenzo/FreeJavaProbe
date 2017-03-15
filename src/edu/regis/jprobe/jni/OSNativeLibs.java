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

import java.util.ArrayList;
import java.util.List;

public class OSNativeLibs {

	private List<OSLibInfo> libList;
	
	public OSNativeLibs() {
		libList = new ArrayList<OSLibInfo>();
	}
	
	public void addLib(String name, String path, long size, long addr) {
		libList.add(new OSLibInfo(name, path, size, addr));
	}

	public OSLibInfo[] getLibs() {
		
		OSLibInfo ret[] = new OSLibInfo[libList.size()];
		
		for (int i=0; i < libList.size(); i++) {
			ret[i] = libList.get(i);
		}
		
		return ret;
	}
	public List<OSLibInfo> getLibList() {
		return libList;
	}
}
