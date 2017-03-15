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
package edu.regis.jprobe.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jdivince
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProbeClassLoader extends ClassLoader {
	
	 private long loadedPackages = 0; 
	
	 public String getLoadedPackageNames() {
	 	
	 	//String packages = new String("");
	 	
	 	Package pks[] = getPackages();
	 	List<String> pkList = new ArrayList<String>();
	 	
	 	for(int a = 0; a < pks.length; a++ ) {
	 		pkList.add(pks[a].getName());
	 		
	 	}
	 	loadedPackages = pkList.size();
	 	StringBuilder sb = new StringBuilder(4096);
	 	
	 	Collections.sort(pkList);
	 	
	 	for (int i = 0; i < pkList.size(); i ++ ) {
	 		//packages += pkList.get(i) + "\n";
	 		sb.append(pkList.get(i));
	 		sb.append("\n");
	 	}
	 	
	 	//return packages;
	 	return sb.toString();
	 }

	/**
	 * @return the loadedPackages
	 */
	public long getLoadedPackages() {
		return loadedPackages;
	}
	 
}