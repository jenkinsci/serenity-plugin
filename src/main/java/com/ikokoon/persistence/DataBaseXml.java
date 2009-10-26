package com.ikokoon.persistence;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.ikokoon.instrumentation.model.Project;
import com.ikokoon.toolkit.Toolkit;

/**
 * This database implementation is in memory and serializes the data model to XML when it closes.
 * 
 * @author Michael Couck
 * @since 11.10.09
 * @version 01.00
 */
public class DataBaseXml extends ADataBase implements IDataBase {

	private Project project;

	/**
	 * Constructor tries to open the XML data model and load the existing data into memory from the previous run. This assists in the speed of
	 * execution as the insertion into the data model takes far longer than selection due to the re-indexing of the indexes etc.
	 */
	public DataBaseXml(File file) {
		this(file, null);
	}

	public DataBaseXml(File file, ClassLoader classLoader) {
		logger.info("Initilizing the database data model in memory");
		InputStream inputStream = null;
		try {
			file = getFile(file);
			inputStream = new FileInputStream(file);
			XMLDecoder decoder = new XMLDecoder(inputStream);
			project = (Project) decoder.readObject();
		} catch (NoSuchElementException e) {
			logger.info("No data generated for the project yet? First run.");
		} catch (Exception e) {
			logger.error("Exception reading the data from the serialized file", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		if (project == null) {
			project = new Project();
		}
		logger.info("Finished initilizing the database data model in memory");
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final <T> T persist(T t) {
		List<Object> list = new ArrayList<Object>();
		setIds(t, t.getClass(), list);
		logger.debug("Persisted object : " + t);
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized final <T> T find(Class<T> klass, Long id) {
		List<Object> index = project.getIndex();
		return (T) binarySearch(index, id);
	}

	@SuppressWarnings("unchecked")
	protected Object binarySearch(List<Object> index, long key) {
		int low = 0;
		int high = index.size() - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			Object object = index.get(mid);
			long midVal = getId(object.getClass(), object);
			// long midVal = a[mid];
			int cmp;
			if (midVal < key) {
				cmp = -1; // Neither val is NaN, thisVal is smaller
			} else if (midVal > key) {
				cmp = 1; // Neither val is NaN, thisVal is larger
			} else {
				long midBits = Double.doubleToLongBits(midVal);
				long keyBits = Double.doubleToLongBits(key);
				cmp = (midBits == keyBits ? 0 : // Values are equal
						(midBits < keyBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
								1)); // (0.0, -0.0) or (NaN, !NaN)
			}
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else {
				// return mid; // key found
				return object;
			}
		}
		return null;
		// return -(low + 1); // key not found.
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized final <T> T find(final Class<T> klass, final Map<String, Object> parameters) {
		Long id = Toolkit.hash(parameters.values());
		List<Object> index = project.getIndex();
		return (T) binarySearch(index, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized final <T> List<T> find(Class<T> klass, Map<String, Object> parameters, int firstResult, int maxResults) {
		List<T> results = new ArrayList<T>();
		// Map<Long, Object> objects = cache.get(klass);
		// if (objects != null) {
		// int index = 0;
		// for (Object object : objects.values()) {
		// if (isEqual(object, parameters)) {
		// if (index++ < firstResult) {
		// continue;
		// }
		// if (index > maxResults) {
		// break;
		// }
		// results.add((T) object);
		// }
		// }
		// }
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	private synchronized final boolean isEqual(Object object, final Map<String, Object> parameters) {
		for (String key : parameters.keySet()) {
			Object value = parameters.get(key);
			Object fieldValue = Toolkit.getValue(object, key);
			if (value != null && !value.equals(fieldValue)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final <T> T merge(T t) {
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final <T> T remove(Class<T> klass, Long id) {
		T t = find(klass, id);
		// Map<Long, Long> index = indexes.get(klass);
		// if (index != null && t != null) {
		// Long hash = getKeyFromValue(index, t);
		// if (hash != null) {
		// index.remove(hash);
		// }
		// }
		// Map<Long, Object> objects = cache.get(klass);
		// if (objects != null) {
		// objects.remove(id);
		// }
		return t;
	}

	/**
	 * Returns the key from the map based on the value.
	 * 
	 * @param index
	 *            the map to get the key from
	 * @param value
	 *            the value for which we are looking for the key
	 * @return the key that corresponds to the value in the map, or null if no such value exists in the map
	 */
	private synchronized final Long getKeyFromValue(Map<Long, Long> index, Object value) {
		for (Long o : index.keySet()) {
			Object indexObject = index.get(o);
			if (indexObject == null) {
				continue;
			}
			// logger.debug("O : " + o + ", index object : " + indexObject + ", value : " + value);
			if (indexObject.equals(value)) {
				return o;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final boolean isClosed() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final void close() {
		logger.info("Comitting and closing the database");
		write();
	}

	private synchronized final void write() {
		FileOutputStream fileOutputStream = null;
		try {
			File file = getFile(null);
			fileOutputStream = new FileOutputStream(file);
			XMLEncoder encoder = new XMLEncoder(fileOutputStream);
			encoder.writeObject(project);
			encoder.close();
		} catch (Exception e) {
			logger.error("Couldn't find the database file? Permissioning on the OS perhaps?", e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Exception e) {
					logger.error("Exception closing the output stream to the database file", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final <T> T find(String queryName, Map<String, Object> parameters) {
		throw new RuntimeException("This method is for JPA, not OODB");
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final <T> List<T> find(Class<T> klass, String queryName, Map<String, Object> parameters, int firstResult, int maxResults) {
		throw new RuntimeException("This method is for JPA, not OODB");
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final int execute(String query) {
		throw new RuntimeException("This method is for JPA, not OODB");
	}

	/**
	 * This method sets the ids in a graph of objects. The objects need to be stored, perhaps using the top level object in the heirachy, then the
	 * database is consulted for it's uid for the object. The uid is set in the field that has the Identifier annotation on the setter method for the
	 * field.
	 * 
	 * @param <T>
	 *            the type of object
	 * @param object
	 *            the object to set the id for
	 * @param klass
	 *            the class of object
	 * @param list
	 *            a list of already set id fields
	 */
	protected synchronized final <T> void setIds(Object object, Class<? extends Object> klass, List<Object> list) {
		if (object == null) {
			return;
		}
		if (getId(klass, object) != null) {
			return;
		}
		list.add(object);

		Object[] uniqueValues = getUniqueValues(object);
		Long id = Toolkit.hash(uniqueValues);
		setId(object, object.getClass(), id, true);

		// Insert the object into the index

		Field[] fields = klass.getDeclaredFields();
		for (Field field : fields) {
			Object fieldValue = Toolkit.getValue(object, field.getName());
			if (fieldValue == null) {
				continue;
			}
			if (fieldValue instanceof Collection) {
				for (Object collectionObject : (Collection<?>) fieldValue) {
					setIds(collectionObject, collectionObject.getClass(), list);
				}
				continue;
			}
			setIds(fieldValue, fieldValue.getClass(), list);
		}
	}

	protected void insert(LinkedList<Object> index, Object toInsert, long key) {
		if (index.size() == 0) {
			index.addFirst(toInsert);
			return;
		}
		int low = 0;
		int high = index.size();
		while (low <= high) {
			int mid = (low + high) >>> 1;
			if (mid >= index.size()) {
				index.add(mid, toInsert);
				break;
			}
			Object object = index.get(mid);
			long midVal = getId(object.getClass(), object);
			// logger.info("Low : " + low + ", high : " + high + ", mid : " + mid + ", mid val : " + midVal);
			if (midVal < key) {
				int next = mid + 1;
				if (index.size() > next) {
					Object nextObject = index.get(next);
					long nextVal = getId(nextObject.getClass(), nextObject);
					if (nextVal > key) {
						index.add(next, toInsert);
						break;
					}
				}
				low = mid + 1;
			} else if (midVal > key) {
				int previous = mid - 1;
				if (previous > 0) {
					Object previousObject = index.get(previous);
					long previousVal = getId(previousObject.getClass(), previousObject);
					if (previousVal < key) {
						index.add(mid, toInsert);
					}
				}
				high = mid - 1;
			} else {
				// Found key? Duplicate?
				throw new RuntimeException("Duplicate key found : " + toInsert + ", " + key);
			}
		}
	}

}