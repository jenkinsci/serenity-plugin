package com.ikokoon.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
