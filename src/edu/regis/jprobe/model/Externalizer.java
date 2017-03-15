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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This Utility class provides functionality for performing
 * serialization/deserialization in a version independent manner. Any field
 * present in the object stream but not in the target object will be ignored.
 * Any field that is not present in the target class but in the object stream
 * will also be ignored.
 * 
 * This Utility class methods are designed to be called by classes that implement the
 * Externalizable interface and as such MUST have a public no argument contructor.
 * 
 * @author Jim Di Vincenzo
 * @see java.io.Externalizable
 * 
 */
public class Externalizer {

    private Map<String, VersionizedClass> classMap; 
   
    
    private static Externalizer instance;
    
    /**
     * Singleton
     */
    private Externalizer() {

        classMap = new ConcurrentHashMap<String, VersionizedClass>();
                       
    }
    
    public synchronized static Externalizer getInstance() {
        
        if (instance == null) {
            instance = new Externalizer();
        }
        return instance;
    }

    /**
     * This method will serialize an object to the specified output stream
     * @param obj Object to be serialized.
     * @param out Output stream to write object to
     * @return the number of fields serialized.
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public int writeObject(Object obj, ObjectOutput out) throws IOException, IllegalArgumentException {

        
        VersionizedClass vc = getVersionedClass(obj);
        long start = System.nanoTime();
        
        vc.incrementNumberOfWrites();

        List<String> fieldsWritten = vc.getFieldNames();
        
        out.writeObject(fieldsWritten);

        for (Field field : vc.getFields()) {
 
            Object fld;
            try {
                fld = field.get(obj);
            } catch (IllegalAccessException e) {
                throw new IOException("Field " + field.getName() + " is not accessable");
            }
            out.writeObject(fld);
        }
        vc.addToWriteTime(System.nanoTime() - start);
        return vc.getNumberOfEligableFields();

    }
    /**
     * This method will deserialize an input stream back to the java object.
     * @param obj Object to deserialize back into
     * @param in Input Stream
     * @return Number of fields deserialized
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked") //Can't do instanceof for a List<String>
    public  int readObject(Object obj, ObjectInput in) throws IOException, ClassNotFoundException {

        
        VersionizedClass vc = getVersionedClass(obj);
        long start = System.nanoTime();
        vc.incrementNumberOfReads();
        
        Object objList = in.readObject();

        List<String> fieldsWritten = null;

        if (objList instanceof List<?>) {
            fieldsWritten = (List<String>) objList;
        } else {
            throw new IOException("Missing Field List in InputStream");
        }

        int fieldsDeserialized = 0;
        for (String name : fieldsWritten) {

            Field f = vc.getField(name);

            if (f != null) {
                Object data = in.readObject();
                try {
                    f.set(obj, data);
                    fieldsDeserialized++;
                } catch (Exception e) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Name[");
                    sb.append(name);
                    sb.append("] TypeClass[");
                    sb.append(f.getType().getCanonicalName());
                    sb.append("] FieldClass[");
                    sb.append(f.getDeclaringClass().getCanonicalName());
                    sb.append("] Read Value Type[");
                    sb.append((data != null ?data.getClass().getCanonicalName() : "<null>"));
                    sb.append("]");
                    throw new IOException("Cannot Set Field. Info(" + sb.toString() + 
                            ") from the object stream, Exception " + 
                            e.getClass().getName() + " Occurred. Error is " + e.getMessage(), e);
                } 
            } else {
                vc.incrementNumberOfReadMissess();
            }

        }
        vc.addToReadTime(System.nanoTime() - start);
        return fieldsDeserialized;
    }
    
    private VersionizedClass getVersionedClass(Object obj) {
        
        Class<? extends Object> objClass = obj.getClass();
              
        
        VersionizedClass ret = classMap.get(objClass.getCanonicalName());
        
        if (ret != null) {
            return ret;
        }
        
        return  getNewVersionizedClass(objClass); 
        
    }
    /*
     * OK, What is going on here...
     * This method allows us to minimize locks required when modifying the
     * ClassMap Map. This way, we do not need to lock for reads (ConcurrentHashMap Does that for us)
     * but when we add a new entry, we need to lock it. Additionally, in the case were 2 threads attempt
     * to create the same object, the second one will wait for the first to complete, then once it is complete,
     * the object will be in the map so we can return the new created object without a clash.
     */
    private synchronized VersionizedClass getNewVersionizedClass(Class<?> objClass) {
        
        VersionizedClass ret = classMap.get(objClass.getCanonicalName());
        
        if (ret == null) {
            ret = new VersionizedClass(objClass);
            classMap.put(ret.getClassName(), ret);
        }
        
        return ret;
    }
    @Override
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        
        sb.append("Externalizer Statatistics for ").append(this.getClass().getCanonicalName()).append("\n");
        sb.append("\tNumber of Classes in Cache: ").append(Utilities.format(classMap.size())).append("\n");
        
        Collection<VersionizedClass> classValues = classMap.values();
        for (VersionizedClass vc : classValues) {
            sb.append(vc.toString()).append("\n");
        }
              
        
        return sb.toString();
        
    }

}
class VersionizedClass {
    
    
    private String className;
    private List<Field> fields;
    private List<String> fieldNames;
    private Map<String, Field> fieldMap;
    
    private long numberOfWrites = 0;
    private long numberOfReads = 0;
    private long numberOfReadMissess = 0;
    private long writeTime = 0;
    private long readTime = 0;
    
    private static final double NANOS_PER_MILLI = 1000000; 
     
    public VersionizedClass(Class<?> clazz) {
        
        fields = getAllFields(clazz);
        fieldNames = new ArrayList<String>();
        fieldMap = new HashMap<String, Field>();
        className = clazz.getCanonicalName();
        
        for (Field fld : fields) {
            
            fieldNames.add(fld.getName());
            fieldMap.put(fld.getName(), fld);
        }
        
    }
    
    
    private  List<Field> getAllFields(Class<?> clazz) {
        
        List<Field> ret = new ArrayList<Field>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();

            if ((Modifier.isStatic(mod) && Modifier.isFinal(mod)) || Modifier.isTransient(mod)) {
                continue;
            }
            field.setAccessible(true);
            ret.add(field);
        }
        
        Class<?> parent = clazz.getSuperclass();
        if (parent != null) {
            List<Field> parents = getAllFields(parent);
            ret.addAll(parents);
        }
        
        return ret;
    }


    /**
     * Accessor for className
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    public int getNumberOfEligableFields() {
        return fields.size();
    }
    /**
     * Accessor for fields
     * @return the fields
     */
    public List<Field> getFields() {
        return fields;
    }


    /**
     * Accessor for fieldNames
     * @return the fieldNames
     */
    public List<String> getFieldNames() {
        return fieldNames;
    }


    /**
     * Accessor for fieldMap
     * @return the fieldMap
     */
    public Field getField(String name) {
        return fieldMap.get(name);
    }


    /**
     * Accessor for numberOfWrites
     * @return the numberOfWrites
     */
    public void incrementNumberOfWrites() {
        numberOfWrites++;
    }


    /**
     * Accessor for numberOfReads
     * @return the numberOfReads
     */
    public void incrementNumberOfReads() {
        numberOfReads++;
    }


    /**
     * Accessor for numberOfReadMissess
     * @return the numberOfReadMissess
     */
    public void incrementNumberOfReadMissess() {
        numberOfReadMissess++;
    }


    /**
     * Accessor for writeTime
     * @return the writeTime
     */
    public void addToWriteTime(long time) {
        writeTime += time;
    }


    /**
     * Accessor for readTime
     * @return the readTime
     */
    public void addToReadTime(long time) {
        readTime += time;
    }


    
    /**
     * Accessor for numberOfWrites
     * @return the numberOfWrites
     */
    public long getNumberOfWrites() {
        return numberOfWrites;
    }


    /**
     * Accessor for numberOfReads
     * @return the numberOfReads
     */
    public long getNumberOfReads() {
        return numberOfReads;
    }




    /**
     * Accessor for numberOfReadMissess
     * @return the numberOfReadMissess
     */
    public long getNumberOfReadMissess() {
        return numberOfReadMissess;
    }


    /**
     * Accessor for writeTime
     * @return the writeTime
     */
    public long getWriteTime() {
        return writeTime;
    }


    /**
     * Accessor for readTime
     * @return the readTime
     */
    public long getReadTime() {
        return readTime;
    }
    @Override
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        
        sb.append("\tExternalizer Stats for Class ").append(className).append("\n");
        sb.append("\t\tFields: ");
        
        for (String name : fieldNames) {
            sb.append("[").append(name).append("] ");
        }
        sb.append("\n");
        
        double readMS = readTime / NANOS_PER_MILLI;
        double writeMS = writeTime / NANOS_PER_MILLI;
        double aveRead = (numberOfReads == 0 ? 0.0 : readMS / numberOfReads);
        double aveWrite = (numberOfWrites == 0 ? 0.0 : writeMS / numberOfWrites);
        
        sb.append("\t\tReads: .................... ");
        sb.append(Utilities.format(numberOfReads)).append("\n");
        sb.append("\t\tWrites: ................... ");
        sb.append(Utilities.format(numberOfWrites)).append("\n");
        sb.append("\t\tMissing Fields on Reads: .. ");
        sb.append(Utilities.format(numberOfReadMissess)).append("\n");
        sb.append("\t\tRead Time: ................ ");
        sb.append(Utilities.format(readMS, 3)).append("ms\n");
        sb.append("\t\tWrite Time: ............... ");
        sb.append(Utilities.format((writeMS), 3)).append("ms\n");
        sb.append("\t\tAverage Read Time: ........ ");
        sb.append(Utilities.format(aveRead, 5)).append("ms\n");
        sb.append("\t\tAverage Write Time: ....... ");
        sb.append(Utilities.format((aveWrite), 5)).append("ms\n");
        
        
        return sb.toString();
    }
    
}