package com.ikokoon.persistence;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.toolkit.Toolkit;

/**
 * This database implementation is in memory and serializes the data model to XML when it closes.
 * 
 * @author Michael Couck
 * @since 11.10.09
 * @version 01.00
 */
public class DataBaseXml extends ADataBase implements IDataBase {

	/** The cache of objects keyed on the class of the entity. */
	private Map<Class, Map<Long, Object>> cache = new HashMap<Class, Map<Long, Object>>();
	/** Indexes of unique combinations for the entities that point to the key for the corresponding entity in the cache map. */
	private Map<Class, Map<Long, Long>> indexes = new HashMap<Class, Map<Long, Long>>();

	/**
	 * Constructor tries to open the XML data model and load the existing data into memory from the previous run. This assists in the speed of
	 * execution as the insertion into the data model takes far longer than selection due to the reindexing of the indexes etc.
	 */
	public DataBaseXml(File file) {
		logger.error("Initilizing the database data model in memory");
		try {
			file = getFile(file);
			ByteArrayOutputStream byteArrayOutputStream = Toolkit.getContents(file);
			InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			// ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			// cache = (Map<Class, Map<Long, Object>>) objectInputStream.readObject();
			ClassLoader loader = XMLDecoder.class.getClassLoader();
			logger.info("Decoder classloader : " + loader);

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(classLoader);
			classLoader.loadClass(XMLDecoder.class.getName());
			this.getClass().getClassLoader().loadClass(XMLDecoder.class.getName());
			Class.forName(XMLDecoder.class.getName());

			loader = XMLDecoder.class.getClassLoader();
			logger.info("Decoder classloader : " + loader);

			XMLDecoder decoder = new XMLDecoder(inputStream, null, null, this.getClass().getClassLoader());
			cache = (Map<Class, Map<Long, Object>>) decoder.readObject();
		} catch (Exception e) {
			logger.error("Exception reading the data from the serialized file", e);
		}
		logger.error("Finished initilizing the database data model in memory");
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
	public synchronized final <T> T find(Class<T> klass, Long id) {
		T t = null;
		Map map = cache.get(klass);
		if (map != null) {
			t = (T) map.get(id);
		}
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final <T> T find(final Class<T> klass, final Map<String, Object> parameters) {
		Long hash = Toolkit.hash(parameters.values());
		Map<Long, Long> index = indexes.get(klass);
		if (index == null) {
			index = new HashMap<Long, Long>();
			indexes.put(klass, index);
		}
		Map objects = cache.get(klass);
		if (objects == null) {
			objects = new HashMap<Long, Object>();
			cache.put(klass, objects);
		}
		Long id = index.get(hash);
		if (id != null) {
			T t = (T) objects.get(id);
			// logger.info("Index hit : " + hash + ", " + id + ", " + t);
			if (t != null) {
				return t;
			}
		}
		for (Object object : objects.values()) {
			if (isEqual(object, parameters)) {
				id = getId(klass, object);
				// logger.info("Parameter search : " + id + ", " + parameters + ", " + object);
				index.put(hash, id);
				return (T) object;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized final <T> List<T> find(Class<T> klass, Map<String, Object> parameters, int firstResult, int maxResults) {
		List results = new ArrayList();
		Map objects = cache.get(klass);
		if (objects != null) {
			int index = 0;
			for (Object object : objects.values()) {
				if (isEqual(object, parameters)) {
					if (index++ < firstResult) {
						continue;
					}
					if (index > maxResults) {
						break;
					}
					results.add(object);
				}
			}
		}
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
	public synchronized final <T> List<T> find(Class<T> klass, int firstResult, int maxResults) {
		List results = new ArrayList();
		Map objects = cache.get(klass);
		if (objects != null) {
			int index = 0;
			for (Object object : objects.values()) {
				if (index++ < firstResult) {
					continue;
				}
				if (index > maxResults) {
					break;
				}
				results.add(object);
			}
		}
		return results;
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
		Map index = indexes.get(klass);
		if (index != null && t != null) {
			Long hash = getKeyFromValue(index, t);
			if (hash != null) {
				index.remove(hash);
			}
		}
		Map objects = cache.get(klass);
		if (objects != null) {
			objects.remove(id);
		}
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
		logger.warn("Comitting and closing the database");
		write();
	}

	private synchronized final void write() {
		FileOutputStream fileOutputStream = null;
		try {
			File file = getFile(null);
			fileOutputStream = new FileOutputStream(file);
			OutputStream outputStream = new BufferedOutputStream(fileOutputStream);
			// ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			// objectOutputStream.writeObject(cache);
			ClassLoader loader = XMLEncoder.class.getClassLoader();
			logger.info("XMLEncoder class loader : " + loader);

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(classLoader);
			classLoader.loadClass(XMLDecoder.class.getName());
			this.getClass().getClassLoader().loadClass(XMLDecoder.class.getName());
			Class.forName(XMLDecoder.class.getName());

			loader = XMLEncoder.class.getClassLoader();
			logger.info("XMLEncoder class loader : " + loader);

			XMLEncoder encoder = new XMLEncoder(outputStream);
			encoder.writeObject(cache);
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
	 *            the klass of object
	 * @param list
	 *            a list of already set id fields
	 */
	protected synchronized final <T> void setIds(Object object, Class<? extends Object> klass, List<Object> list) {
		if (object == null) {
			return;
		}
		if (!object.getClass().getPackage().getName().equals(Package.class.getPackage().getName())) {
			return;
		}
		if (list.contains(object)) {
			return;
		}
		if (getId(klass, object) != null) {
			return;
		}
		list.add(object);

		Object[] uniqueValues = getUniqueValues(object);
		Long id = Toolkit.hash(uniqueValues);
		setId(object, object.getClass(), id, true);
		Map objects = cache.get(klass);
		if (objects == null) {
			objects = new HashMap<Long, Object>();
			cache.put(klass, objects);
		}
		objects.put(id, object);
		Map<Long, Long> index = indexes.get(klass);
		if (index == null) {
			index = new HashMap<Long, Long>();
		}
		index.put(id, id);

		Field[] fields = klass.getDeclaredFields();
		for (Field field : fields) {
			Object fieldValue = Toolkit.getValue(object, field.getName());
			if (fieldValue == null) {
				continue;
			}
			if (fieldValue instanceof Collection) {
				for (Object collectionObject : (Collection) fieldValue) {
					setIds(collectionObject, collectionObject.getClass(), list);
				}
				continue;
			}
			setIds(fieldValue, fieldValue.getClass(), list);
		}
	}

}