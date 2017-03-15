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

package edu.regis.jprobe.model;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;


/**
 * @author Jim Di Vincenzo
 *
 */
public abstract class AbstractProperties {

    protected static final String OS_MBEAN_NAME = "java.lang:type=OperatingSystem";
    protected static final String MEMORY_MBEAN_NAME = "java.lang:type=Memory";
    protected static final String RUNTIME_MBEAN_NAME = "java.lang:type=Runtime";
    protected static final String GC_MBEAN_NAME = "java.lang:type=GarbageCollector,name=*";
    protected static final String MP_MBEAN_NAME = "java.lang:type=MemoryPool,name=*";
    protected static final String THREADING_MBEAN_NAME = "java.lang:type=Threading";
    
    protected MBeanServerConnection conn;
    protected List<String> errors = new ArrayList<String>();
    
    protected AbstractProperties(MBeanServerConnection conn) throws MalformedObjectNameException {
        this.conn = conn;
    }
    protected void buildSimpleAttributes(ObjectName on) throws IOException {
        
        List<Field> attributes = new ArrayList<Field>();
        Field[] fields = this.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            
            int mods = field.getModifiers();
            
            if (Modifier.isPrivate(mods) && !Modifier.isStatic(mods)) {
                field.setAccessible(true);
                attributes.add(field);
            }
        }
        
        for (Field attribute: attributes) {
            
            Object val = null;
            
            String name = camelCase(attribute.getName());
            
            try {
                val = conn.getAttribute(on, name);
            } catch (AttributeNotFoundException e) {
                errors.add(name + " Not Found");
                val = getDefaultValue(attribute.getType());
            } catch (InstanceNotFoundException e) {
                errors.add(name + " Object Not Found");
                val = getDefaultValue(attribute.getType());
            } catch (MBeanException e) {
                errors.add(name + " MBean Error, " + e.getMessage());
                val = getDefaultValue(attribute.getType());
            } catch (ReflectionException e) {
                errors.add(name + " Reflection Error, " + e.getMessage());
                val = getDefaultValue(attribute.getType());
            } 
            
            if (val != null) {
                try {
                    attribute.set(this, val);
                } catch (Exception e) {
                    errors.add("Exception " + e.getClass().getSimpleName() + 
                            ", Error: " + e.getMessage() + 
                            ", setting field " + name);
                }
            }
        }
    }

    private Object getDefaultValue(Class<?> type) {
        
        if (type.equals(Integer.class)) {
            return new Integer(-1);
        }
        
        if (type.equals(Long.class)) {
            return new Long(-1);
        }
        
        if (type.equals(Double.class)) {
            return new Double(-1.0);
        }
        
        if (type.equals(Float.class)) {
            return new Float(-1.0f);
        }
        
        return "N/A";
    }
    protected CompositeData getCompositeData(ObjectName on, String name) throws IOException {
        
        Object val = null;
        
        try {
            val = conn.getAttribute(on, name);
        } catch (AttributeNotFoundException e) {
            errors.add(name + " Not Found");
        } catch (InstanceNotFoundException e) {
            errors.add(name + " Object Not Found");
        } catch (MBeanException e) {
            errors.add(name + " MBean Error, " + e.getMessage());
        } catch (ReflectionException e) {
            errors.add(name + " Reflection Error, " + e.getMessage());
        } 
        
        if (val != null && val instanceof CompositeData) {
            return (CompositeData) val;
        }
        
        errors.add(name + " is not an instance of CompositeData");
        
        return null;
    }
    protected Object getData(ObjectName on, String name) throws IOException {
        
        Object val = null;
        
        try {
            val = conn.getAttribute(on, name);
        } catch (AttributeNotFoundException e) {
            errors.add(name + " Not Found");
        } catch (InstanceNotFoundException e) {
            errors.add(name + " Object Not Found");
        } catch (MBeanException e) {
            errors.add(name + " MBean Error, " + e.getMessage());
        } catch (ReflectionException e) {
            errors.add(name + " Reflection Error, " + e.getMessage());
        } 
        
                
        return val;
    }
    protected void logErrors(Class<?> clazz) {
        
        if (errors.size() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        
        sb.append("Error Building Data Object " + clazz.getSimpleName() + ", Errors are:");
        
        for (String msg : errors) {
            sb.append("\t" + msg); 
        }
        
        Utilities.debugMsg(sb.toString());
        
    }
    private String camelCase(String name) {
        
        String first = name.substring(0, 1).toUpperCase();
        String next = name.substring(1, name.length());
        
        return first + next;
    }
}
