////////////////////////////////////////////////////////////////////////
//
// BEGIN_COPYRIGHT
// Confidential property of Sybase, Inc.
//
// Copyright 1987-2010.
//
// Sybase, Inc. All rights reserved.
//
// Unpublished rights reserved under U.S. copyright laws.
// END_COPYRIGHT
//
// BEGIN_DISCLAIMER
// This software contains confidential and trade secret information of 
// Sybase, Inc. Use, duplication or disclosure of the software and 
// documentation by the U.S. Government is subject to restrictions set 
// forth in a license agreement between the Government and Sybase, Inc. 
// or other written agreement specifying the Government's rights to use 
// the software and any applicable FAR provisions, for example, 
// FAR 52.227-19.
//
// Sybase, Inc. One Sybase Drive, Dublin, CA 94568, USA
// END_DISCLAIMER
//
////////////////////////////////////////////////////////////////////////
package edu.regis.jprobe.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/**
 * This class provides static methods for creating the Open MBean data 
 * objects CompositeData and TabularData. 
 * 
 * To truly provide open JMX Mbeans, exposing non Java objects to JMX clients
 * requires that they have access to the classes that are used by those objects.
 * Composite and Tabular Open MBean classes provide a means of de-coupling this.
 * 
 * Creating these objects by hand is very tedious, this class will automate that
 * chore by creating them using Java Reflection. For this to be effective, the 
 * objects supplied should be standard POJO style with getters and setters.
 * 
 * <p>This class is intended to handle a large number of Composite and Tabular
 * data type from the supplied objects, <b> However,</b> it does not provide 
 * 100% coverage. Some supplied objects may not be able to be dynamically built.
 * To provide additional debug info, this class can provide runtime diagnostics: 
 * set the java property: <i><b>com.sybase.rs.container.util.JMXUtil.debug</b></i>
 * to <b>true</b> for runtime diagnostics.
 * 
 * 
 * @author jdivince
 *
 */
public final class JMXUtils {
	
	private static final Class<?>[] RETURNCLASS = {
		BigDecimal.class,
		BigInteger.class,
		Boolean.class,
		Byte.class,
		Character.class,
		Date.class,
		Double.class,
		Float.class,
		Integer.class,
		Long.class,
		ObjectName.class,
		Short.class,
		String.class,
		Void.class
	};
	
	private static final SimpleType<?>[] RETURNTYPE = {
		SimpleType.BIGDECIMAL,
		SimpleType.BIGINTEGER,
		SimpleType.BOOLEAN,
		SimpleType.BYTE,
		SimpleType.CHARACTER,
		SimpleType.DATE,
		SimpleType.DOUBLE,
		SimpleType.FLOAT,
		SimpleType.INTEGER,
		SimpleType.LONG,
		SimpleType.OBJECTNAME,
		SimpleType.SHORT,
		SimpleType.STRING,
		SimpleType.VOID,
	};
	
	private static final Class<?>[] BOXEDCLASS = {
		Boolean.class,
		Byte.class,
		Character.class,
		Double.class,
		Float.class,
		Integer.class,
		Long.class,
		Short.class
	};
	private static final String[] BOXEDNAME = {
		"boolean",
		"byte",
		"char",
		"double",
		"float",
		"int",
		"long",
		"short"
		
	};
	private static final boolean DEBUG = (
			"true".equalsIgnoreCase(System.getProperty(
			"com.sybase.rs.container.util.JMXUtil.debug") ));
	
	static {
		if (RETURNCLASS.length != RETURNTYPE.length) {
			throw new RuntimeException("Invalid Class Definition," + 
					" RETURNCLASS length is not equal to RETURNTYPE length");
		}
		if (BOXEDCLASS.length != BOXEDNAME.length) {
			throw new RuntimeException("Invalid Class Definition," + 
					" BOXEDCLASS length is not equal to BOXEDNAME length");
		}
		
		
	}
	
	/**
	 * To Prevent Instantiation.
	 */
	private JMXUtils() {
		
	}
	
	/**
	 * <b>This method will create an OpenMBean Composite Data Structure
	 * from an object. This method does provide support for deeply
	 * nested objects.</b> 
	 * <p>
	 * For this method to be useful, the specified objects should have
	 * get.. is.. methods (Standard MBean format). If the specified object
	 * does not have any get/is methods, it will return null. If any nested
	 * objects do not have get/is methods, they will be represented by calling
	 * the toString() method.
	 * 
	 * @param obj Object to build structure from
	 * @param typeName Name of the Type
	 * @param description of the data
	 * @param excludedMethods - if not null, method names to be excluded from the
	 * 		construction of the object.
	 * @return CompositeData or null if there are no eligible methods
	 * 		   in the specified object.
	 * @throws OpenDataException - If the constructed object is not valid.
	 * @throws InvocationTargetException  - Reflection error
	 * @throws IllegalAccessException - Reflection error
	 */
	public static CompositeData buildCompositeData(Object obj, 
		String typeName, String description, String[] excludedMethods) 
		throws OpenDataException, 
			IllegalAccessException, 
			InvocationTargetException {
		
		
		Class<?> objClass = obj.getClass();
		debug("Building Composite Data for " + objClass.getName());
		
		List<Method> attributes = new ArrayList<Method>();
		List<SimpleType<?>> types = new ArrayList<SimpleType<?>>();
		List<String> names = new ArrayList<String>();
		
		Method[] methods = objClass.getDeclaredMethods();
		/*
		 * Get all methods that start with get or is that take
		 * no parameters (getters)
		 */
		for (Method method : methods) {
			
			if ((method.getName().startsWith("get") || 
						method.getName().startsWith("is")  || 
						method.getName().equals("toString")) 
					&& 	isMethodIncluded(method, excludedMethods)) {
				if (Modifier.isPublic(method.getModifiers()) &&
					method.getParameterTypes().length == 0) {
					
					Class<?> returnType = method.getReturnType();
					//Skip tabular or composite
					if (isSupportedClass(returnType)) {
						SimpleType<?> type = getSimpleType(returnType, false);
						
						attributes.add(method);
						debug("Adding method: " + method.getName());
						
						
						
						//Check for methods that return primitives.
						if (type == null) {
							Class<?> primativeClass = getBoxedClass(
									returnType);
							if (primativeClass != null) {		
								type = getSimpleType(primativeClass, true);
							}
						}
						debug("Method is using " + (type == null ? 
								"Unknown " : type.toString()) + 
								method.getReturnType());
								
						types.add(type);
						if (method.getName().startsWith("get")) {
							names.add(method.getName().substring(3));
						} else {
							names.add(method.getName().substring(2));
						}
					}
					
					
				}
			}
		}
		
		/*
		 * No methods eligible for Composite
		 */
		if (attributes.size() == 0) {
				debug("Object does not have any eligable methods");
				return null;
		}
		
		Object[] data = new Object[attributes.size()];
		String[] itemNames = new String[attributes.size()];
		OpenType<?>[] oType = new OpenType<?>[attributes.size()];

		/*
		 * Call the methods and save the result
		 */
		for (int i = 0; i < attributes.size(); i++) {
			Method m = attributes.get(i);
			oType[i] = types.get(i);
			itemNames [i] = names.get(i);
			debug("Invoking " + m.getName());
			Object o = null;
			try {
				o = m.invoke(obj, (Object[])null);
			} catch (Exception e) {
				o = "Exception Invoking Method " + m.getName() + 
					", Exception: " + e.getClass().getClass().getName() +
					", Error: " + e.getMessage();
			}
			if (o == null) {
				debug("Invocation returned null");
			} else {
				debug("Invocation returned Class " + o.getClass().getName() + 
						", Value=" + o.toString());
			}
			
			//If null, just set to null
			if (o == null) {
				debug("Assigned Data Type is null/Void");
				data[i] = null;
				oType[i] = SimpleType.VOID;
			} else if (o instanceof Map) {
				// This is a Map, build a Tabular Data for it
				debug("Assigned Data Type is MAP");
				data[i] = buildTabularData((Map<?, ?>)o, itemNames [i], itemNames [i] ,excludedMethods);
				oType[i] = ((TabularData) data[i]).getTabularType();
			} else if (o.getClass().isArray()) {
				debug("Assigned Data Type is Array");
				// This is an Array, build an Array Type 
				//(this also will indirectly recurse...) 
				data[i] = o;
				oType[i] = getArrayType(o ,excludedMethods);
				
				//If we can't build it (Unknown type) just set to null
				if (oType[i] == null) {
					data[i] = null;
					oType[i] = SimpleType.VOID;
				}
			} else if (o instanceof List) {
				debug("Assigned Data Type is List");
				data[i] = buildStringArray(((List<?>)o).toArray());
				oType[i] = getArrayType(data[i] ,excludedMethods);
				
			} else if (oType[i] == SimpleType.STRING) {
				debug("Assigned Data Type is String");
				//These are simple Stings
				data[i] = o.toString();
			} else if (oType[i] == null) { 
				//This means it is a User/Java class so we build a
				// Composite for it (recurse)
				data[i] = buildCompositeData(o, o.getClass().getName(), 
						o.getClass().getName() ,excludedMethods);
				if (data[i] != null) {
					
					oType[i] = ((CompositeData)data[i]).getCompositeType();
					debug("Assigned Data Type is Composite");
				} else {
					// This means that the Object has no "get/is" methods
					// So we simply call toString();
					debug("Assigned Data Type is Default String");
					data[i] = null;
					oType[i] = SimpleType.VOID;
				}
			} else {
				debug("Assigned Data Type is " + o.getClass().getName());
				data[i] = o;
			}
			
		}
		debug("Building Composite Started");
		CompositeType ctype = new CompositeType(typeName, description ,
				itemNames, itemNames, oType);
		debug("Building Composite Complete");
		return new CompositeDataSupport(ctype, itemNames, data);
	}
	

	/**
	 * This method will create an OpenMBean Tabular Data Structure
	 * from a Map. 
	 * 
	 * <b> This implementation ONLY supports Maps that are either 
	 * implicitly or explicitly typed. What this means is that the keys
	 * and values MUST all be of the same type:</b>
	 * <p>For Example:
	 * <p> If the key is a String, all of the Keys MUST be a String
	 * <p> If all of the Values are Long, all Must Be Long.
	 * 
	 * @param map Map to build structure from
	 * @param typeName Name of the Type
	 * @param description of the data
	 * @param excludedMethods - if not null, method names to be excluded from the
	 * 		construction of the object.
	 * @return TabularData
	 * @throws OpenDataException - If the constructed object is not valid.
	 * @throws InvocationTargetException  - Reflection error
	 * @throws IllegalAccessException - Reflection error
	 */
	public static TabularData buildTabularData(Map<?,?> map, 
		String typeName, String description, String[] excludedMethods) 
		throws OpenDataException, 
			IllegalAccessException, 
			InvocationTargetException {
		
		debug("Building Tabular Data for " + map.getClass().getName());
		

		OpenType<?> keyType = null;
		OpenType<?> valueType = null;
		
		List<Object> keys = new ArrayList<Object>();
		String[] itemNames = {"Key" , "Value"};
		OpenType<?>[] oTypes = new OpenType[2];
		
		Set<?> entry = map.entrySet();
		Iterator<?> iter = entry.iterator();
		 	
		while (iter.hasNext()) {
			Map.Entry<?, ?>  me = (Map.Entry<?, ?>)iter.next();
			keys.add(me.getKey());
		}
		
		if (keys.isEmpty()) {
			debug("Map is Empty, returning Null");
			return null;
		}
		debug("Map contains " + map.size() + " entries");
		keyType = getSimpleType(keys.get(0).getClass(), true); //false);
		if (keyType == null) {
			
			CompositeData cd = buildCompositeData(keys.get(0), keys.get(0).getClass().getName(),
					keys.get(0).getClass().getName() ,excludedMethods);
			if (cd != null) {
				keyType = cd.getCompositeType();
			} else {
				keyType = SimpleType.STRING;
			}
			
		}
		debug("Map Key type is " + keyType.getClass().getName());
		Object value = map.get(keys.get(0));
		
		if (value == null) {
			valueType = SimpleType.STRING;
			debug("Value is null, assigning SimpleType.STRING");
		} else {
			valueType = getSimpleType(value.getClass(), false);
			debug("Value class is " + value.getClass().getName());
		}
		
		if (valueType == null && value != null) {
			debug("ValueType is null, assigning CompositeType");
			CompositeData cd = buildCompositeData(value, value.getClass().getName(),
					value.getClass().getName() ,excludedMethods);
			if (cd != null) {
				valueType = cd.getCompositeType();
				debug("Value is using a composite type");
			} else {
				debug("Composite is null, assigning SimpleType.STRING");
				valueType = SimpleType.STRING;
			}
			
		}
		debug("Assigning type " + keyType.getClass().getName() + " to the Key");
		debug("Assigning type " + 
				(valueType == null ? "null" : valueType.getClass().getName())
				+ " to the Value");
		oTypes[0] = keyType;
		oTypes[1] = valueType;
		
		CompositeType ctype = new CompositeType(typeName, description ,
				itemNames, itemNames, oTypes);
		
		TabularType tt = new TabularType(typeName, description, ctype, itemNames);
		
		TabularDataSupport tds = new TabularDataSupport(tt);
		int idx = 0;
		
		for (Object key : keys) {
			
			Object[] data = new Object[2];
			Object val = map.get(key);
			debug("Fetching Key: " + key.getClass().getName() + 
					" : " + key.toString());
			
			if (val != null) {
				debug("Fetching Value: " + val.getClass().getName() +
						" : " + val.toString()); 
			} else {
				debug("Fetching Value: null");
			}
 
			if (oTypes[0] == SimpleType.STRING) {
				data[0] = key.toString();
			} else {
				data[0] = key;
			}
			
			if (oTypes[1] == SimpleType.STRING && val != null) {
				data[1] = val.toString();
			} else {
				data[1] = val;
			}
			
			if (oTypes[1] instanceof CompositeType && val != null) {
				CompositeData cd = buildCompositeData(val, val.getClass().getName(),
						val.getClass().getName() ,excludedMethods);
				if (cd != null) {
					//oTypes[1] = cd.getCompositeType();
					data[1] = cd;
				} else {
					//oTypes[1] = SimpleType.STRING;
					data[1] = val.toString();
				}
				
			}
			debug("Putting Entity " + idx);
			debug("Assigning Entity Value as " + oTypes[1].getClass().getName() );
			CompositeDataSupport cds = new CompositeDataSupport(ctype, itemNames, data);
			
			tds.put(cds);
			debug("Putting Complete for " + idx++);
			
		}
		return tds;
		
	}
	/**
	 * This method is used to construct a JMException 
	 * (Standard JMX Open Exception) from any thowable. 
	 * This allows developers to "wrap" the original exception
	 * in a Generic form so that developers do not need to 
	 * ensure that the Exception is in the clients classpath.
	 * 
	 * @param t Throwable to create exception from.
	 * @return JMException The new generic exception.
	 */
	public static JMException buildGenericException(Throwable t) {
		
		String msg = "Generic Exception: Original Message(" + 
			t.getMessage() + ") Original Class(" + 
			t.getClass().getName() + ")";
		JMException exc = new JMException(msg);
		exc.setStackTrace(t.getStackTrace());
		
		if (t.getCause() != null) {
			exc.initCause(buildGenericException(t.getCause()));
		}
		
		
		
		return exc;
		
	}
	/**
	 * This method is used to construct a RemoteException 
	 * (Standard Exception) from any thowable. 
	 * This allows developers to "wrap" the original exception
	 * in a Generic form so that developers do not need to 
	 * ensure that the Exception is in the clients classpath.
	 * 
	 * @param t Throwable to create exception from.
	 * @return RemoteException The new generic exception.
	 */
	public static RemoteException buildRemoteException(Throwable t) {
		
		String msg = "Generic Exception: Original Message(" + 
			t.getMessage() + ") Original Class(" + 
			t.getClass().getName() + ")";
		RemoteException exc = null;
		if (t.getCause() != null) {
			exc = new RemoteException(msg, buildRemoteException(t.getCause()));
		} else {
			exc = new RemoteException(msg);
		}
		exc.setStackTrace(t.getStackTrace());
		
		
		return exc;
		
	}
	/**
	 * This method will return the OpenType (SimpleType) of the supplied class
	 * @param returnType - Class to evaluate
	 * @param useDefault - if true, it will return a SimpleType.STRING if it cannot
	 * 	find the appropriate type. If false, it will return null if it cannot 
	 *  find the appropriate type.
	 * @return SimpleType
	 */
	private static SimpleType<?> getSimpleType(Class<?> returnType, boolean useDefault) {
		
		for (int i = 0; i < RETURNCLASS.length; i++) {
			if (returnType == RETURNCLASS[i]) {
				return RETURNTYPE[i];
			}
		}
		
		if (useDefault) {
			return SimpleType.STRING;
		} 
		
		return null;
		
	}
	
	/**
	 * This method creates a Open MBean ArrayType for
	 * the specified Object array.
	 * 
	 * @param array Source Array
	 * @return ArrayType
	 * @throws OpenDataException - if the construction fails
	 * @throws IllegalAccessException - Reflection Error
	 * @throws InvocationTargetException - Reflection Error
	 */
	private static ArrayType<?> getArrayType(Object array, String[] excludedMethods) 
		throws OpenDataException, 
			   IllegalAccessException, 
			   InvocationTargetException {
		
		Class<?> arrayClass = array.getClass();
		debug("Building Array Data for " + arrayClass.getName());

		char[] nameChars = arrayClass.getName().toCharArray();
		
		int dim = 0;
		
		for (char c : nameChars) {
			if (c == '[') {
				dim++;
			}
		}
		
		debug("Array Consists of " + dim + " dimensions");

		Class<?> parent = null;
		Class<?> cl = arrayClass;
		
		while (parent == null) {
			if(cl.isArray()) {
				cl = cl.getComponentType();
			} else {
				parent = cl;
			}
			
		}
		if (parent.isPrimitive()) {
			
			parent = getBoxedClass(parent);
			debug("Array is a primative Boxed to " + parent.getName());
			SimpleType<?> simpleType = getSimpleType(parent, false);
			return new ArrayType<Object>(simpleType, true);
		}
		

		OpenType<?> type = getSimpleType(parent, false);
		
		if (type == null) {
			CompositeData cd = buildCompositeData(array, 
					arrayClass.getName(), parent.getName() ,excludedMethods);
			
			if (cd == null) {
				debug("Array type unknown, returning null");
				//type = SimpleType.STRING;
				return null;
				//return new ArrayType(dim, type);
			} 
			debug("Array is a Composite");
			return new ArrayType<Object>(dim, cd.getCompositeType());
		} 
		debug("Array is " + type.toString());
		return new ArrayType<Object>(dim, type);

	}
	/**
	 * This method returns the boxed class for a given primative
	 * @param clazz primative class
	 * @return Boxed class
	 */
	private static Class<?> getBoxedClass(Class<?> clazz) {
		
		String primativeName = clazz.getName();
		for (int i = 0; i < BOXEDNAME.length; i++) {
			if (primativeName.equals(BOXEDNAME[i])) {
				return BOXEDCLASS[i];
			}
		}
		//throw new IllegalArgumentException(primativeName + " is not primative");
		return null;
		
	}
	/**
	 * This will build a string array from a supplied object array 
	 * by calling the toString() for each element.
	 * @param array Array to create String array from
	 * @return String array
	 */
	private static String[] buildStringArray(Object[] array) {
		
		String[] ret = new String[array.length]; 
		
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				ret[i] = array[i].toString();
			} else {
				ret[i] = "NULL";
			}
			
		}
		
		return ret;
	}
	
	/**
	 * Check to see if the return type is composite or tabular.
	 * @param fc Class to check
	 * @return true, if the class is valid
	 */
	private static boolean isSupportedClass(Class<?> fc) {
		
		if (fc == TabularDataSupport.class) {
			System.out.println("Excluding TabularDataSupport");
			return false;
		}
		if (fc == CompositeDataSupport.class) {
			System.out.println("Excluding CompositeDataSupport");
			return false;
		}
		return true;
	}
	/**
	 * Check to see if the specified Method has been filtered out.
	 * @param method Method to check
	 * @param excludedNames = array of names to exclude
	 * @return true, if the method is to be included, false if it was filtered out.
	 */
	private static boolean isMethodIncluded(Method method, String[] excludedNames) {
		
		if (excludedNames == null || excludedNames.length ==0) {
			return true;
		}
		for(String name : excludedNames) {
			
			if (method.getName().equals(name)) {
				debug("Excluding method " + name + " via Exclusion List");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Debug message routine
	 * @param msg Debug message
	 */
	private static void debug(String msg) {
		
		if (DEBUG) {
			long tid = Thread.currentThread().getId();
			System.out.println("[" + tid + "] - " + msg);
		}
	}
}
