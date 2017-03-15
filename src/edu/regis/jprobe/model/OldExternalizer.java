///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2006  James Di Vincenzo
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides functionality for performing serialization/deserialization
 * in a version independent manner. Any field present in the object stream but
 * not in the target object will be ignored. Any field that is not present in the
 * target class but in the object stream will also be ignored.
 * 
 * @author jdivince
 *
 */
public final class OldExternalizer {
    
    /**
     * Prevent instantiation
     */
    private OldExternalizer() {
        
    }
    
    public static int writeObject(Object obj, ObjectOutput out) throws IOException, IllegalArgumentException {
        
        Class<? extends Object> objClass = obj.getClass();
        //System.out.println(Thread.currentThread().getId() + " - Serializing " + objClass.getSimpleName());
        
        Field[] fields = objClass.getDeclaredFields();
        
        List<String> fieldsWritten = new ArrayList<String>();
        List<Field> fieldList = new ArrayList<Field>();
        
        for (Field field : fields) {
            
            int mod = field.getModifiers();
            
            if ((Modifier.isStatic(mod) && Modifier.isFinal(mod)) ||
                    Modifier.isTransient(mod)) {
                continue;
            }
            
            fieldsWritten.add(field.getName());
            fieldList.add(field);
        }
        
        out.writeObject(fieldsWritten);
        
        for (Field field : fieldList) { 
            //System.out.println("Serializing " + field.getName());
            field.setAccessible(true);
            Object fld;
            try {
                fld = field.get(obj);
            } catch (IllegalAccessException e) {
                throw new IOException("Field " + field.getName() + " is not accessabe");
            }
            out.writeObject(fld); 
        }
        
        return fieldList.size();
        
    }
   @SuppressWarnings("unchecked")
public static int readObject(Object obj, ObjectInput in) throws IOException, ClassNotFoundException {
        
        Class<? extends Object> objClass = obj.getClass();
        //System.out.println(Thread.currentThread().getId() + " - Deserializing " + objClass.getSimpleName());
        Field[] fields = objClass.getDeclaredFields();
        
        
        Map<String, Field> fieldMap = new HashMap<String, Field>();
        
        for (Field field : fields) {
            
            int mod = field.getModifiers();
            
            if ((Modifier.isStatic(mod) && Modifier.isFinal(mod)) ||
                    Modifier.isTransient(mod)) {
                continue;
            }
          
            fieldMap.put(field.getName(), field);
        }
       
        Object objList = in.readObject();

        List<String> fieldsWritten = null;
        
        if (objList instanceof List<?>) {
            fieldsWritten = (List<String>) objList;
        } else {
            throw new IOException("Missing Field List in InputStream");
        }
        
        int errors = 0;
        for (String name : fieldsWritten) {
            
            Field f = fieldMap.get(name);
            
            if (f != null) {
                f.setAccessible(true);
                Object data = in.readObject();
                try {
                    f.setAccessible(true);
                    f.set(obj, data);
                } catch (IllegalAccessException e) {
                    errors++;
                    Logger.getLogger().logException(e, obj);
                } catch (IllegalArgumentException e) {
                    Logger.getLogger().error("Cant set " + f.getName() + ", error is " + e.getMessage());
                }
            }
            
        }
        return errors;
   }
   public static void main(String[] args) throws Exception {
       
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       ObjectOutputStream oos = new ObjectOutputStream(baos);
       ProbeResponse pr1 = new ProbeResponse();
       pr1.setClassInfo("Hello There");
       pr1.setCurrentHeapSize(9999);
       pr1.setCpuAffinity(777666);
       pr1.setCurrentCPUPercent(54.987);
       
       
       System.out.println(Utilities.toStringFormatter(pr1));
       
       oos.writeObject(pr1);
       byte[] serClass = baos.toByteArray();
       
       ByteArrayInputStream bais = new ByteArrayInputStream(serClass);
       ObjectInputStream ois = new ObjectInputStream(bais);
       ProbeResponse pr2 = (ProbeResponse) ois.readObject(); 
       System.out.println(Utilities.toStringFormatter(pr2));
   }

}
