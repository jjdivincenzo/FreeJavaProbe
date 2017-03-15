package edu.regis.jprobe.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class StackExcludeFilters {
    
    public static final String DEFAULT_FILE_NAME = "StackExcludes.xml";
    private String fileName = DEFAULT_FILE_NAME;
    private Logger logger;
    private boolean enabled = true;
    
    private List<String> excludes;
    public StackExcludeFilters() {
        
        excludes = new ArrayList<String>();
        logger = Logger.getLogger();
        
    }
    
    public void restore(String fileName) {
        
        if (fileName == null) {
            this.fileName = DEFAULT_FILE_NAME;
        } else {
            this.fileName = fileName;
        }
        
        File excludeFile = new File(this.fileName);
        
        if (!excludeFile.exists()) {
            logger.info("Creating Stack Exclude File");
            loadDefaults();
            save();
            return;
        }
        
        FileInputStream fis = null;
        
        try {
            fis = new FileInputStream(excludeFile);
            Properties props = new Properties();
            props.loadFromXML(fis);
            loadFromProperties(props);
        } catch (IOException e) {
            logger.error("Error Loading Exclude File " + excludeFile.getAbsolutePath());
            logger.logException(e, this);
            loadDefaults();
        }
        
        
        
    }
    
    public void save() {
        
        FileOutputStream fos = null;
        Properties props = new Properties();
        File excludeFile = new File(fileName);
        for (int i = 0; i < excludes.size(); i++) {
            String filter = excludes.get(i);
            props.put("Filter" + i, filter);
        }
        
        try {
            fos = new FileOutputStream(excludeFile);
            props.storeToXML(fos, "JProbe Stack Exclude Filters");
        } catch (IOException e) {
            logger.error("Error Saving Exclude File " + excludeFile.getAbsolutePath());
            logger.logException(e, this);
        }
        
    }
    
    private void loadFromProperties(Properties props) {
        
        Set<Object> keys = props.keySet();
        Iterator<Object> iter = keys.iterator();
        excludes.clear();
        int count = 0;
        
        while (iter.hasNext()) {
            String key = iter.next().toString();
            String value = props.getProperty(key);
            excludes.add(value);
            logger.debug("Loading Filter " + value);
            count++;
        }
        logger.info(count + " excludes loaded from " + fileName);
    }
    public String getCurrentStackTrace(StackTraceElement[] stes) {
        
        if (!enabled) {
            if (stes.length > 0) {
                return stes[0].toString();
            }
            return "N/A";
        }
        for (StackTraceElement ste : stes) {
            if (!exclude(ste)) {
                return ste.toString();
            }
        }
        return "*Filtered*";
    }
    public boolean exclude(StackTraceElement ste) {
        
        String stackEntry = ste.toString();
        
        for (String exclude : excludes) {
            
            if (stackEntry.startsWith(exclude)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean addFilter(String filter) {
        
        if (excludes.contains(filter.trim())) {
            return false;
        }
        excludes.add(filter.trim());
        return true;

    }
    public boolean removeFilter(String filter) {
        
        if (!excludes.contains(filter.trim())) {
            return false;
        }
        excludes.remove(filter.trim());
        return true;

    }
    public List<String> getAllFilters() {
        
        List<String> ret = new ArrayList<String>();
        
        for (String filter : excludes) {
            ret.add(filter);
        }
        
        Collections.sort(ret);
        return ret;
    }
    
    public void setAllFilters(List<String> newFilters) {
        excludes.clear();
        
        for (String filter : newFilters) {
            excludes.add(filter);
        }
    }
    public void clearAllFilters() {
        excludes.clear();
    }
    public int getSize() {
        return excludes.size();
    }
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("*** Filters ***\n");
        
        for (String filter : excludes) {
            sb.append("\t").append(filter).append("\n");
        }
        
        return sb.toString();
    }
    private void loadDefaults() {
        
        excludes.add("java.");
        excludes.add("javax.");
        excludes.add("com.sun.");
        excludes.add("sun.");
        excludes.add("org.apache.");
        excludes.add("org.xml.");
        excludes.add("junit.");
        excludes.add("org.testng.");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            logger.info("Enabling Stack Filters");
        } else {
            logger.info("Disabling Stack Filters");
        }
    }

    public static void main(String[] args) throws Exception {
        
        StackExcludeFilters sef = new StackExcludeFilters();
        
        sef.restore(null);
        
        sef.addFilter("com.sybase.");
        sef.addFilter("com.oracle.");
        sef.addFilter("com.ibm.");
        
        System.out.println(sef.toString());
        
        sef.removeFilter("com.ibm.");
        
        System.out.println(sef.toString());
        
        sef.save();
        
    }

}
