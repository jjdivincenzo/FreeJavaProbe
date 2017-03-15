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

public class ClassInfo {
	
	private String className;
	private int classSize;
	private ClassLoader classLoader;
	
	public ClassInfo(String name, int classSize, ClassLoader loader) {
		
		this.className = name;
		this.classSize = classSize;
		this.classLoader = loader;
	}
	/**
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	/**
	 * @param classLoader the classLoader to set
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	/**
	 * @return the classSize
	 */
	public int getClassSize() {
		return classSize;
	}
	/**
	 * @param classSize the classSize to set
	 */
	public void setClassSize(int classSize) {
		this.classSize = classSize;
	}
    /*public byte[] getClassBytes() {
        return classBytes;
    }
    public void setClassBytes(byte[] classBytes) {
        this.classBytes = classBytes;
    }*/
	
	

}
