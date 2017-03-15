
package edu.regis.jprobe.model;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;

/**
 * The ClassPathHacker class is a utility class that dynamically adds 
 * resources to the classpath by using the system class loader. The 
 * entries are added from a search path of URLs referring to both JAR 
 * files and directories. Any URL that ends with a '/' is assumed to 
 * refer to a directory. Otherwise, the URL is assumed to refer to a JAR 
 * file which will be opened as needed. 
 * 
 * 
 */
public class ClassPathUpdater {
		 
	// class varibales
	private static final Class<?>[] parameters = new Class[]{URL.class};
	private static HashSet<URL> urlList = new HashSet<URL>();	 
	/**
	 * Adds a file to the classpath.
	 * 
	 * @param string
	 * @throws IOException
	 */
	public static void addFile(String string) throws IOException {
		File file = new File(string);
		addFile(file);
	}
	
	public static void addDirectory(File dir, boolean recurse) throws IOException {
		
		if (!dir.exists()) throw new IOException("Directory " + dir.getName() + " Does not Exist"); 
		if (!dir.isDirectory()) throw new IOException("File is not a Directory"); 
		
		File files[] = dir.listFiles();
		
		for (int i=0; i < files.length; i++) {
			
			if (files[i].isDirectory()) {
				if (recurse) {
					addDirectory(files[i], true);
					continue;
				}
			} else {
				if (isJava(files[i].getName())) {
					addFile(files[i]);
				}
			}
			
		}
	}
	/**
	 * Adds a file to the classpath.
	 * 
	 * @param file
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
    public static void addFile(File file) throws IOException {
		addURL(file.toURL());
	}
		  
	/**
	 * Adds a resource represented by a URL to the classpath.
	 * 
	 * @param url
	 * @throws IOException
	 */
	public static synchronized void addURL(URL url) throws IOException {
		
		if (!urlList.add(url)) { 	//Only add if it is not already there.
			Logger.getLogger().info(url.toString() + " Not Added to Classpath, It Is Already In The Classpath");
			return; 
		}
		
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		
		try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[]{ url });
		} 
		catch (Throwable t) {
		    Logger.getLogger().error(Utilities.formatException(t,null));
			throw new IOException("Could not add URL to system classloader");
		}
		
		Logger.getLogger().info(url.toString() + " Has Been Added to Classpath");
		

	}
	
	/**
	 * Adds an array of resources represented by URLs to the classpath.
	 * 
	 * @param urls
	 * @throws IOException
	 */
	public static void addURL(URL[] urls) throws IOException {
				
		for(int i = 0; i < urls.length; i++) {
			addURL(urls[i]);
		}
	}	
	
	public static boolean isJava(String name) {
		int loc = name.lastIndexOf(".");
		if (loc < 0) return false;
		String ext = name.substring(loc);
		
		if (ext.equalsIgnoreCase(".class") || 
			ext.equalsIgnoreCase(".jar")   ||
			ext.equalsIgnoreCase(".ear")   ||
			ext.equalsIgnoreCase(".war")   ||
			ext.equalsIgnoreCase(".zip")) 
			return true;
		
		return false;
	}
	

}
