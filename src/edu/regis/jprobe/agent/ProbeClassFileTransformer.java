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

import static edu.regis.jprobe.model.Utilities.debugMsg;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import edu.regis.jprobe.model.Utilities;

public class ProbeClassFileTransformer implements ClassFileTransformer {
	
	private Map<String, ClassInfo> loadedClasses; 
	private Map<String, ClassLoaderInfo> classLoaders;
	
	private static final String JVMOPT_LOG_OPTION = "edu.regis.jprobe.log.loader";
	private static final int STRING_BUFFER_SIZE = 4096;
	public static final long WAIT_TIMEOUT = 5000;
	private boolean logClassLoads = false;
	private long totalClassSize = 0;
	private long transformTime = 0;
	private long numberOfPackages = 0;
	private long overheadBytes = 0;
	private ClassLoader bootstrapLoader;
	private ProbeState status;
	private Instrumentation inst;
	
	protected ProbeClassFileTransformer(ProbeState status, Instrumentation inst) {
		
		this.status = status;
		this.inst = inst;
		debugMsg("Initializing Probe Class File Transformer");
		loadedClasses = new ConcurrentHashMap<String, ClassInfo>(); 
        classLoaders = new ConcurrentHashMap<String, ClassLoaderInfo>();
		
		
		String logLoads = System.getProperty(JVMOPT_LOG_OPTION);
		if ("true".equalsIgnoreCase(logLoads)) {
			logClassLoads = true;
			Utilities.debug = true;
		}
		
		bootstrapLoader = ClassLoader.getSystemClassLoader();
	}
	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		
		if (className == null) return null;
		
		//Wait till all initialization is complete...
		if (!status.isInitialized())  {
			
			Utilities.debugMsg("Waiting for Initialization to complete."); 
			return null;
		}
		
		long start = System.nanoTime();
		
		try {

			String classLoaderName = "";
			if (loader != null ) {
				classLoaderName = loader.getClass().getCanonicalName();
			} else {
				classLoaderName = bootstrapLoader.getClass().getName();
				loader = bootstrapLoader;
			}
					
			
			
			
			String name = normalizeName(className);
			
			if (logClassLoads) {
				long tid = Thread.currentThread().getId();
				Utilities.debugMsg("[" + tid + "] - ClassLoader(" + classLoaderName + ") is Loading Class " +
						className + " size is " + classfileBuffer.length);
			}
			ClassInfo ci = new ClassInfo(name, classfileBuffer.length, loader);
			loadedClasses.put(name, ci);
			overheadBytes += inst.getObjectSize(ci);
					
			
			totalClassSize += classfileBuffer.length;
			
			ClassLoaderInfo loads = classLoaders.get(classLoaderName);
			
			
			if (loads == null) {
				classLoaders.put(classLoaderName, new ClassLoaderInfo(loader));
			} else {
				loads.incrementLoadCount();
				classLoaders.put(classLoaderName,loads);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
						
			transformTime += System.nanoTime() - start;
		}
		
		return null;
	}


	
	protected Package[] getPackages(ClassLoader loader) {
		
		Class<?> sysclass = loader.getClass();
		Object parm[] = new Object[0];
		Object pkg = null;
		
		while(sysclass != null) {
		
						
			try {
				Method method = sysclass.getDeclaredMethod("getPackages", (Class[]) null); 
				method.setAccessible(true);
				pkg = method.invoke(loader, parm);
				break;
			} catch (NoSuchMethodException e) {
				sysclass = sysclass.getSuperclass();
			} catch (Exception e) {
				Utilities.debugMsg("Error Getting Packages, Error is " + e.getMessage());
				break;
			}
		}
	
		if (pkg != null && pkg instanceof Package[]) {
				return (Package[]) pkg;
		}
		
		return null;
		
	}
	public String getClassPathURLs() {
		
		
		StringBuilder clsb = new StringBuilder(STRING_BUFFER_SIZE);
		Set<String> urlSet = null;
		try {
			
	    	
	    	
	    	//synchronized(classLoaders) {
		    	Set<String>	classLoaderkeys = classLoaders.keySet();
		    	urlSet = new HashSet<String>();
		    	Iterator<String> classLoaderIter = classLoaderkeys.iterator();	
			   		    
			    while (classLoaderIter.hasNext()) {
			    	String classLoaderName = classLoaderIter.next();
			    	ClassLoaderInfo loads = classLoaders.get(classLoaderName);
			    	ClassLoader ldr = loads.getClassLoader();
			    	
			    	if (ldr instanceof URLClassLoader) {
			    		URL urls[] =((URLClassLoader) ldr).getURLs();
					
						for (int i = 0; i < urls.length; i++) {
							urlSet.add(urls[i].toString());
							
						}
			    	}
			    	
			    	
				}
	    	//}
	    	
	    	Iterator<String> urlIter = urlSet.iterator();
		   	List<String> urlList = new ArrayList<String>();
		   	
		    while (urlIter.hasNext()) {
		    	urlList.add(urlIter.next());
		    }    
			Collections.sort(urlList);
			
			
			for (int i = 0; i < urlList.size(); i ++ ) {
				
				clsb.append(urlList.get(i));
				clsb.append("\n");
				
			}
	     }catch (ConcurrentModificationException e) { 
	            return "Not Available at This Time, retry This Operation Later";
		} catch (Exception e) {
			clsb.append("Exception " + e.getMessage() + 
					" Occured Obtaining Class Loader URL List\n" + 
					Utilities.formatException(e, this));
		}
		
		return clsb.toString();
		
	}

	public String getLoadedClasses() {
		
		StringBuilder lcsb = new StringBuilder(STRING_BUFFER_SIZE);
		List<String> classList = null;
		try {
			
	    	
	    	//synchronized (loadedClasses) {
		    	Set<String> keys = loadedClasses.keySet();			 
		    	classList = new ArrayList<String>();
			    Iterator<String> iter = keys.iterator();	
			    
			    while (iter.hasNext()) {
			    	
			    	String className = iter.next();
			    	Integer size = loadedClasses.get(className).getClassSize();
			    	classList.add(className + " Size(" + size.toString() + ")");
			    	
				}
	    	//}
		    	    	
			Collections.sort(classList);
			
			
			for (int i = 0; i < classList.size(); i ++ ) {
				
				lcsb.append(classList.get(i));
				lcsb.append("\n");
				
			}
		}catch (ConcurrentModificationException e) { 
		    return "Not Available at This Time, retry This Operation Later";
		}catch (Exception e) {
			lcsb.append("Exception " + e.getMessage() + 
					" Occured Obtaining Loaded Class List\n" + 
					Utilities.formatException(e, this));
		}
		return lcsb.toString();
	}
	
	public String getClassLoaders() {
		
		StringBuilder clsb = new StringBuilder(STRING_BUFFER_SIZE);
		List<String> classLoaderList = null;
		
		try {
			
	    	
	    	//synchronized(classLoaders) {
	        	Set<String>	classLoaderkeys = classLoaders.keySet();
		    	classLoaderList = new ArrayList<String>();
		    	Iterator<String> classLoaderIter = classLoaderkeys.iterator();	
			   		    
			    while (classLoaderIter.hasNext()) {
			    	String classLoaderName = classLoaderIter.next();
			    	ClassLoaderInfo loads = classLoaders.get(classLoaderName);
			    	classLoaderList.add(classLoaderName + " Loads(" + loads.getLoadCount() + ")");
			    	
				}
	    	//}	
			Collections.sort(classLoaderList);
			
			
			for (int i = 0; i < classLoaderList.size(); i ++ ) {
				
				clsb.append(classLoaderList.get(i));
				clsb.append("\n");
				
			}
        }catch (ConcurrentModificationException e) { 
	            return "Not Available at This Time, retry This Operation Later";

		} catch (Exception e) {
			clsb.append("Exception " + e.getMessage() + 
					" Occured Obtaining Class Loader List\n" + 
					Utilities.formatException(e, this));
		}
		
		return clsb.toString();

		
	}
	public String getPackages() {
		
		StringBuilder clsb = new StringBuilder(STRING_BUFFER_SIZE);
		
		try {
			
			Set<String> classLoaderList = null;
	    	ClassLoaderInfo loaders[] = null;
	    	
	    	//synchronized(classLoaders) {
		    	Set<String> classLoaderkeys = classLoaders.keySet();
		    	classLoaderList = new HashSet<String>();
	    		loaders = new ClassLoaderInfo[classLoaders.size()];
			    Iterator<String> classLoaderIter = classLoaderkeys.iterator();
			    int idx = 0;
			    while (classLoaderIter.hasNext()) {
			    	String classLoaderName = classLoaderIter.next();
			    	loaders[idx++] = classLoaders.get(classLoaderName);
			    }
	    	//}
	    	
	    	for (ClassLoaderInfo inf : loaders) {
		    	Package pkg[] = getPackages(inf.getClassLoader());
		    	if (pkg != null) {
		    		for (int i =0; i < pkg.length; i++) {
		    			classLoaderList.add(pkg[i].getName());
		    		}
		    	}
			}
	    	
		    
		   	Iterator<String> packageIter = classLoaderList.iterator();
		   	List<String> packageList = new ArrayList<String>();
		   	
		    while (packageIter.hasNext()) {
		    	packageList.add(packageIter.next());
		    }
		    
			Collections.sort(packageList);
			
			numberOfPackages = packageList.size();
			for (int i = 0; i < classLoaderList.size(); i ++ ) {
				
				clsb.append(packageList.get(i));
				clsb.append("\n");
				
			}
	     }catch (ConcurrentModificationException e) { 
	            return "Not Available at This Time, retry This Operation Later";

		} catch (Exception e) {
			clsb.append("Exception " + e.getMessage() + 
					" Occured Obtaining Class Loader List.\n" +
			Utilities.formatException(e, this)); 
		}
		
		return clsb.toString();

		
	}
	//we do this so that the regex class is not loaded by the classloader
	//possibly deadlocking us...
	protected String normalizeName(String name) {
		char namebytes[] = name.toCharArray();
		char normalName[] = new char[namebytes.length];
		
		for (int i=0; i < namebytes.length; i++) {
			
			if (namebytes[i] == '/' ) {
				normalName[i] = '.';
			} else {
				normalName[i] = namebytes[i];
			}
		}
		
		return new String(normalName);
	}

	public ClassLoader getClassLoaderForClass(String name) {
		
		ClassInfo inf = loadedClasses.get(name);
		
		if (inf == null) return null;
		
		return inf.getClassLoader();
	}
	/**
	 * @return the totalClassSize
	 */
	public long getTotalClassSize() {
		return totalClassSize;
	}
		
	/**
	 * @return the transformTime
	 */
	public long getTransformTime() {
		return transformTime;
	}
	
	public long getNumberOfClassLoaders() {
		return classLoaders.size();
	}
	public long getNumberOfLoadedClasses() {
		return loadedClasses.size();
	}
	public long getNumberOfPackages() {
		return numberOfPackages;
	}
	public long getClassInfoOverheadBytes() {
	    return overheadBytes;
	}

	
}
