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
import java.io.Externalizable;
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
public final class ExternalizerHS {
    
    private Map<String, Field> fieldMap;
    private List<Field> fieldList;
    private List<String> fieldsWritten;
    private Class<?> clazz;
    private List<Field> fields;
    
    /**
     * CTor:
     * @param Class to externalize
     */
    public ExternalizerHS(Class<?> clazz) {
        
        this.clazz = clazz;
        fieldMap = new HashMap<String, Field>();
        fieldList = new ArrayList<Field>();
        fieldsWritten = new ArrayList<String>();
        fields = getAllFields(clazz);//clazz.getDeclaredFields();
      
              
        for (Field field : fields) {
            
            int mod = field.getModifiers();
            
            if ((Modifier.isStatic(mod) && Modifier.isFinal(mod)) ||
                    Modifier.isTransient(mod)) {
                continue;
            }
          
            fieldMap.put(field.getName(), field);
            fieldList.add(field);
            fieldsWritten.add(field.getName());
        }
    }
    /**
     * This method will write the specified object to the output stream.
     * @param obj - Object to Write
     * @param out - Output Stream
     * @return - Number of fields written
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public int writeObject(Object obj, ObjectOutput out) throws IOException, IllegalArgumentException {
       
        if (obj.getClass() != clazz) {
            throw new IllegalArgumentException("Specified Object is not an instance of the spacified class");
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
    /**
     * This will read the input stream and restore the specified object
     * @param obj - Object to restore
     * @param in - Input Stream
     * @return Number of fields restored
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
   @SuppressWarnings("unchecked")
   public int readObject(Object obj, ObjectInput in) throws IOException, ClassNotFoundException, IllegalAccessException {
       
       if (obj.getClass() != clazz) {
           throw new IllegalArgumentException("Specified Object is not an instance of the spacified class");
       }
               
        Object objList = in.readObject();

        List<String> fieldsWritten = null;
        
        if (objList instanceof List<?>) {
            fieldsWritten = (List<String>) objList;
        } else {
            throw new IOException("Missing Field List in InputStream");
        }
        
        int fields = 0;
        for (String name : fieldsWritten) {
            
            Field f = fieldMap.get(name);
            
            if (f != null) {
                f.setAccessible(true);
                Object data = in.readObject();
                f.setAccessible(true);
                f.set(obj, data);
                fields++;
            }
            
        }
        return fields;
   }
   private static List<Field> getAllFields(Class<?> clazz) {
       
       List<Field> ret = new ArrayList<Field>();
       Field[] fields = clazz.getDeclaredFields();
       for (Field field : fields) {
           ret.add(field);
       }
       Class<?> parent = clazz.getSuperclass();
       if (parent != null) {
           List<Field> parents = getAllFields(parent);
           ret.addAll(parents);
       }
       
       return ret;
   }
   public void skip(String fieldName) {
       fieldsWritten.remove(fieldName);
   }
   public static void main(String[] args) throws Exception {
       
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       ObjectOutputStream oos = new ObjectOutputStream(baos);
       TestObject pr1 = new TestObject();
       
       
       pr1.one = "Hello There";
       pr1.two = "Two for One";
       pr1.int3 = 33333333;
       pr1.long4 = 44444444444444l;
       pr1.setDouble5(54.987);
       
       System.out.println(Utilities.toStringFormatter(pr1));
       
       oos.writeObject(pr1);
       byte[] serClass = baos.toByteArray();
       
       ByteArrayInputStream bais = new ByteArrayInputStream(serClass);
       ObjectInputStream ois = new ObjectInputStream(bais);
       TestObject pr2 = (TestObject) ois.readObject(); 
       System.out.println(Utilities.toStringFormatter(pr2));
   }

}
class TestObject implements Externalizable {
    
    public String one;
    protected String two;
    int int3;
    long long4;
    private double double5;
    private transient ExternalizerHS ext;
    
    public TestObject() {
        ext = new ExternalizerHS(this.getClass());
        ext.skip("int3");
    }
    /**
     * @param double5 the double5 to set
     */
    public void setDouble5(double double5) {
        this.double5 = double5;
    }
    public double getDouble5() {
        return this.double5;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        ext.writeObject(this, out);
        
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        try {
            ext.readObject(this, in);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
}