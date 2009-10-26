package com.ikokoon.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.model.Unique;
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
	@SuppressWarnings("unchecked")
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
