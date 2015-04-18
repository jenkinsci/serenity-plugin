package com.ikokoon.serenity.persistence;

import com.ikokoon.serenity.model.Composite;
import com.ikokoon.toolkit.Toolkit;
import org.apache.log4j.Logger;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * This is the database class using Neodatis as the persistence tool.
 *
 * @author Michael Couck
 * @since 01-12-2009
 * @version 01.00
 */
public class DataBaseOdb extends DataBase {

	private Logger logger = Logger.getLogger(this.getClass());
	/** The Neodatis object database for persistence. */
	private ODB odb = null;
	/** The database file for Neodatis. */
	private String dataBaseFile;
	/** The closed flag. */
	private boolean closed = true;

	/**
	 * Constructor initialises a {@link DataBaseOdb} object.
	 *
	 * @param dataBaseFile
	 *            the file to open the database with
	 */
	public DataBaseOdb(final String dataBaseFile) {
		synchronized (DataBaseOdb.class) {
			this.dataBaseFile = dataBaseFile;
			logger.info("Opening ODB database on file : " + new File(dataBaseFile).getAbsolutePath());
			try {
				odb = ODBFactory.open(this.dataBaseFile);
				closed = false;
			} catch (final Exception e) {
				logger.error("Exception initialising the database : " + dataBaseFile + ", " + this, e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized <E extends Composite<?, ?>> E find(final Class<E> klass, final Long id) {
		IQuery query = new CriteriaQuery(klass, Where.equal("id", id));
		return (E) find(query);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized <E extends Composite<?, ?>> E find(final Class<E> klass, final List<?> parameters) {
		Long id = Toolkit.hash(parameters.toArray());
		return find(klass, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized <E extends Composite<?, ?>> List<E> find(final Class<E> klass) {
		return find(klass, 0, Integer.MAX_VALUE);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public <E extends Composite<?, ?>> List<E> find(final Class<E> klass, final int start, final int end) {
        if (isClosed()) {
            return Collections.EMPTY_LIST;
        }
		List<E> list = new ArrayList<E>();
		try {
			Objects objects = odb.getObjects(klass, false, start, end);
			while (objects.hasNext()) {
				Object object = objects.next();
				list.add((E) object);
			}
		} catch (final Exception e) {
			logger.error("Exception selecting objects with class : " + klass + ", " + this, e);
		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public <E extends Composite<?, ?>> List<E> find(final Class<E> klass, final Map<String, ?> parameters) {
        if (isClosed()) {
            return Collections.EMPTY_LIST;
        }
		Set<E> set = new TreeSet<E>();
		for (final String field : parameters.keySet()) {
			Object value = parameters.get(field);
			logger.debug("Field : " + field + ", " + value);
			IQuery query = new CriteriaQuery(klass, Where.like(field, "%" + value.toString() + "%"));
			try {
				Objects objects = odb.getObjects(query);
				logger.debug("Objects : " + objects);
				if (set.size() == 0) {
					set.addAll(objects);
				}
				set.retainAll(objects);
				logger.debug("Set : " + set);
			} catch (Exception e) {
				logger.error("Exception selecting objects with class : " + klass + ", parameters : " + parameters + ", " + this, e);
			}
		}
		List<E> list = new ArrayList<E>();
		list.addAll(set);
		logger.debug("List : " + list);
		return list;
	}

	private synchronized void commit() {
		try {
			if (!isClosed()) {
				odb.commit();
			}
		} catch (final IOException e) {
			logger.error("Exception comitting the ODB database : " + this, e);
		} catch (final Exception e) {
			logger.error("Exception comitting the ODB database : " + this, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized <E extends Composite<?, ?>> E persist(final E composite) {
        if (isClosed()) {
            return null;
        }
		try {
			setIds(composite);
			E duplicate = (E) find(composite.getClass(), composite.getId());
			if (duplicate != null) {
				if (duplicate != composite) {
					logger.warn("Attempted to persist a duplicate composite : " + composite + ", " + this);
					return composite;
				}
			}
			logger.debug("Persisting composite : " + composite);
			odb.store(composite);
		} catch (final Exception e) {
			logger.error("Exception persisting object : " + composite + ", " + this, e);
		}
		commit();
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized <E extends Composite<?, ?>> E remove(final Class<E> klass, final Long id) {
		E composite = find(klass, id);
		try {
			if (composite != null) {
				odb.delete(composite);
			}
		} catch (final Exception e) {
			logger.error("Exception deleting object : " + id + ", " + this, e);
		}
		commit();
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean isClosed() {
		return closed;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void close() {
		try {
			if (isClosed()) {
				logger.warn("Attempted to close the database again : " + this);
				return;
			}

			if (logger.isInfoEnabled()) {
				logger.info("Closing database : " + dataBaseFile + ", " + this);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				new Exception().printStackTrace(new PrintStream(bos));
				logger.info("Stack dump        : " + bos.toString());
			}

			commit();
			odb.close();

			IDataBaseEvent dataBaseEvent = new DataBaseEvent(this, IDataBaseEvent.Type.DATABASE_CLOSE);
			IDataBase.DataBaseManager.fireDataBaseEvent(dataBaseFile, dataBaseEvent);
		} catch (final Exception e) {
			logger.error("Exception closing the ODB database : " + this, e);
		}
		closed = true;
	}

	@SuppressWarnings("unchecked")
	private synchronized <E extends Composite<?, ?>> E find(final IQuery query) {
        if (isClosed()) {
            return null;
        }
        E e = null;
        try {
            Objects objects = odb.getObjects(query);
			if (objects.size() == 1) {
				e = (E) objects.getFirst();
			} else if (objects.size() > 1) {
				logger.warn("Id for object must be unique : " + query);
			}
		} catch (final Exception ex) {
			logger.error("Exception selecting object on ODB database : " + query + ", " + this.dataBaseFile + ", " + this, ex);
		}
		return e;
	}

	/**
	 * This method sets the ids in a graph of objects.
	 *
	 * @param composite
	 *            the object to set the ids for
	 */
	@SuppressWarnings("unchecked")
	synchronized final void setIds(final Composite<?, ?> composite) {
        if (isClosed()) {
            return;
        }
		if (composite == null) {
			return;
		}
		super.setId(composite);
		logger.debug("Persisted object : " + composite);
		List<Composite<?, ?>> children = (List<Composite<?, ?>>) composite.getChildren();
		if (children != null) {
			for (final Composite<?, ?> child : children) {
				setIds(child);
			}
		}
	}

}
