package edu.regis.jprobe.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * This class is a simple implementation of the FileFilter class
 * used by a JFileChooser class when determining which files to show in
 * the dialog box. We are only implementing the required methods of accept and
 * get description. 
 * 
 * @author jdivincenzo
 * @see javax.swing.filechooser.FileFilter
 * 
 */
public class UIFileFilter extends FileFilter{

	private String extention[];
	private String description;
	
	
	/**
	 * This constructor requires the extention name and description to be
	 * passed in as strings. The extention is what will determine what the
	 * JFileChooser dialog box will display. The JFileChooser dialog calls
	 * the accept method and will display the file if we return true.
	 * 
	 * @param ext String, the file extention to display i.e txt (no dot)
	 * @param desc String, the description i.e "Text Files (*.txt)"
	 */
	public UIFileFilter (String ext[], String desc ) {
		
		extention = ext; //new String(ext.toLowerCase());
		description = new String(desc);
		
	}
	
	
	/**
	 * This method will determine if we will display the file.
	 * @param f File, file object of the requested file.
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File f) {
		
		String extension = new String();
		
		// we will return true if the file extention part of the
		// filename matches the extention we are looking for.
		// We will also return true if the file is a directory so that
		// the user can select a directory to traverse.
		
		if(f != null) {
		    
			if(f.isDirectory()) {
			return true;
		    }
		    
		    String fileName = f.getName();
		    int idx = fileName.lastIndexOf('.');
		    
		    if (idx > 0 && idx < fileName.length() -1) {
		    	extension = fileName.substring(idx+1);
		    }
		    
		    for (int i = 0; i < extention.length; i++) {
		    	if (extension.toLowerCase().equals(extention[i])) return true;
		    }
		    		
		}
		
		return false;
	}
	/**
	 * This method will simply return the file description string that was 
	 * passed to us in the class constructor.
	 * @return description - String, the file description text
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() { 
		
		return description;
	}
	
}