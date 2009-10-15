package com.ikokoon.persistence;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.apache.log4j.Logger;

import com.ikokoon.IConstants;
import com.ikokoon.toolkit.Toolkit;

/**
 * This is the base database class for all the implementations of database. There are some utility methods like generating a unique id from a set of
 * fields in an object, setting the id in an object and getting the id of an object.
 * 
 * @author Michael Couck
 * @since 03.10.09
 * @version 01.00
 */
public abstract class ADataBase implements IDataBase {

	/** The logger for all database implementations. */
	protected Logger logger = Logger.getLogger(this.getClass());
	/** The setter methods for entities keyed on the class. */
	protected Map<Class, Method> idSetterMethods = new HashMap<Class, Method>();
	/** The getter methods for entities keyed on the class. */
	protected Map<Class, Method> idGetterMethods = new HashMap<Class, Method>();

	/**
	 * Gets the file for the database. If no such file exists then one is created.
	 * 
	 * @param file
	 *            the file for the database
	 * @return the file for the database
	 */
	protected File getFile(File file) {
		if (file == null) {
			file = new File(IConstants.DATABASE_FILE);
		}
		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				if (!file.getParentFile().mkdirs()) {
					logger.error("Didn't create parent directory for the database file " + file.getParent());
				} else {
					logger.warn("Created parent directory for the database file " + file.getParent());
				}
			}
			try {
				if (!file.createNewFile()) {
					logger.warn("Didn't create a new database file " + file.getAbsolutePath());
				} else {
					logger.warn("Created a new database file " + file.getAbsolutePath());
				}
			} catch (Exception e) {
				logger.error("Exception trying to create a new database file " + file.getAbsolutePath(), e);
			}
		}
		return file;
	}

	/**
	 * Accesses the id of an object. The id of an object is the method that has the Id annotation.
	 * 
	 * @param klass
	 *            the class of object
	 * @param object
	 *            the object to get the id field from
	 * @return the id from the object or null if no id field is available or the id is null
	 */
	public Long getId(Class klass, Object object) {
		Method idGetterMethod = idGetterMethods.get(klass);
		if (idGetterMethod == null) {
			Method[] methods = klass.getDeclaredMethods();
			for (Method method : methods) {
				Id idAnnotation = method.getAnnotation(Id.class);
				if (idAnnotation != null) {
					idGetterMethod = method;
					idGetterMethods.put(klass, idGetterMethod);
					break;
				}
			}
		}
		if (idGetterMethod != null) {
			try {
				Object value = idGetterMethod.invoke(object, (Object[]) null);
				if (value != null) {
					return (Long) value;
				}
			} catch (Exception e) {
				logger.info(e);
			}
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param <T>
	 * @param t
	 * @param klass
	 * @param id
	 * @param addToPersistables
	 */
	protected <T> void setId(T t, Class klass, Long id, boolean addToPersistables) {
		Method idSetterMethod = idSetterMethods.get(klass);
		if (idSetterMethod == null) {
			Method[] methods = klass.getDeclaredMethods();
			for (Method method : methods) {
				Identifier identifier = method.getAnnotation(Identifier.class);
				if (identifier != null) {
					idSetterMethod = method;
					this.idSetterMethods.put(klass, idSetterMethod);
					break;
				}
			}
		}
		if (idSetterMethod != null) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Setting id : " + klass + ", " + id + ", " + t);
				}
				idSetterMethod.invoke(t, new Object[] { id });
			} catch (Exception e) {
				logger.error("Exception setting the identifier" + id + " on the object " + t + " with method " + idSetterMethod, e);
			}
		}
	}

	/**
	 * Creates an id based on the field combination that is unique for the object. Unique fields are declared with the Unique annotation for the
	 * class.
	 * 
	 * @param <T>
	 *            the type of object to generate the id for
	 * @param t
	 *            the object to generate the id for
	 * @return the hash id generated for the object
	 */
	protected <T> Long generateId(T t) {
		T[] uniqueValues = getUniqueValues(t);
		StringBuilder builder = new StringBuilder();
		for (T uniqueValue : uniqueValues) {
			builder.append(uniqueValue);
		}
		return Toolkit.hash(builder.toString());
	}

	/**
	 * Returns an array of values that are defined as being a unique combination for the entity by using the Unique annotation for the class.
	 * 
	 * @param <T>
	 *            the type of object to be inspected for unique fields
	 * @param t
	 *            the object t inspect for unique field combinations
	 * @return the array of unique field values for the entity
	 */
	protected <T> T[] getUniqueValues(T t) {
		Unique unique = t.getClass().getAnnotation(Unique.class);
		if (unique == null) {
			return (T[]) new Object[] { t };
		}
		String[] fields = unique.fields();
		List<T> values = new ArrayList<T>();
		for (String field : fields) {
			Object value = Toolkit.getValue(t, field);
			T[] uniqueValues = (T[]) getUniqueValues(value);
			for (T uniqueValue : uniqueValues) {
				values.add(uniqueValue);
			}
		}
		return (T[]) values.toArray(new Object[values.size()]);
	}

}
