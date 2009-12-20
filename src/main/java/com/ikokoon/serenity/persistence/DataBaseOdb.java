package com.ikokoon.serenity.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;

import com.ikokoon.serenity.model.Composite;
import com.ikokoon.toolkit.Toolkit;

/**
 * This is the database class using Neodatis as the persistence tool. This class is for the Hudson side of things as there is a memory issue with
 * loading all the data for the builds into memory.
 * 
 * @author Michael Couck
 * @since 01.12.09
 * @version 01.00
 */
public class DataBaseOdb extends DataBase {

	private Logger logger = Logger.getLogger(this.getClass());
	/** The Neodatis object database for persistence. */
	private ODB odb = null;
	/** The listener that catches events. */
	private IDataBaseListener dataBaseListener;
	/** The database file for Neodatis. */
	private String dataBaseFile;
	/** The closed flag. */
	private boolean closed = true;

	/**
	 * Constructor initialises a {@link DataBaseOdb} object.
	 * 
	 * @param dataBaseFile
	 *            the file to open the database with
	 * @param create
	 *            whether to create a new database essentially deleting the database file and creating a new one or to use the data in the existing
	 *            database file
	 */
	DataBaseOdb(IDataBaseListener dataBaseListener, String dataBaseFile, Boolean create) {
		this.dataBaseListener = dataBaseListener;
		this.dataBaseFile = dataBaseFile;
		logger.info("Opening ODB database on file : " + dataBaseFile);
		try {
			if (create) {
				File file = new File(this.dataBaseFile);
				if (!file.delete()) {
					logger.warn("Couldn't delete old database file");
				}
				logger.info("Database file : " + file.getAbsolutePath());
			}
			odb = ODBFactory.open(this.dataBaseFile);
		} catch (Exception e) {
			logger.error("Exception initialising the database", e);
		}
		closed = false;
		commit();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized <E extends Composite<?, ?>> E find(Class<E> klass, Long id) {
		// ICriterion criterion = new EqualCriterion("id", id.intValue());
		// IQuery query = new CriteriaQuery(Object.class, criterion);
		IQuery query = new CriteriaQuery(klass, Where.equal("id", id));
		return (E) find(query);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized <E extends Composite<?, ?>> E find(Class<E> klass, List<Object> parameters) {
		Long id = Toolkit.hash(parameters.toArray());
		return find(klass, id);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized <E extends Composite<?, ?>> List<E> find(Class<E> klass) {
		List<E> list = new ArrayList<E>();
		try {
			Objects objects = odb.getObjects(klass);
			while (objects.hasNext()) {
				list.add((E) objects.next());
			}
		} catch (Exception e) {
			logger.error("Exception selecting objects with class : " + klass, e);
		}
		return list;
	}

	private synchronized void commit() {
		try {
			odb.commit();
		} catch (IOException e) {
			logger.error("Exception comitting the ODB database", e);
		} catch (Exception e) {
			logger.error("Exception comitting the ODB database", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public synchronized <E extends Composite<?, ?>> E persist(E composite) {
		try {
			setIds(composite);
			E duplicate = (E) find(composite.getClass(), composite.getId());
			if (duplicate != null) {
				logger.warn("Attempted to persist a duplicate composite : " + composite);
				return composite;
			}
			logger.debug("Persisting composite : " + composite);
			odb.store(composite);
		} catch (Exception e) {
			logger.error("Exception persisting object : " + composite, e);
		}
		commit();
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized <E extends Composite<?, ?>> E remove(Class<E> klass, Long id) {
		E composite = find(klass, id);
		try {
			if (composite != null) {
				odb.delete(composite);
			}
		} catch (Exception e) {
			logger.error("Exception deleting object : " + id, e);
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
			if (closed) {
				logger.warn("Attempted to close the database again");
				return;
			}
			commit();
			odb.close();
			closed = true;

			final IDataBase dataBase = this;
			IDataBaseEvent dataBaseEvent = new IDataBaseEvent() {
				public IDataBase getDataBase() {
					return dataBase;
				}

				public Type getEventType() {
					return IDataBaseEvent.Type.DATABASE_CLOSE;
				}
			};
			dataBaseListener.fireDataBaseEvent(dataBaseEvent);
		} catch (Exception e) {
			logger.error("Exception closing the ODB database", e);
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized <E extends Composite<?, ?>> E find(IQuery query) {
		E e = null;
		try {
			Objects objects = odb.getObjects(query);
			if (objects.size() == 1) {
				e = (E) objects.getFirst();
			} else if (objects.size() > 1) {
				logger.warn("Id for object must be unique : " + query);
			}
		} catch (Exception ex) {
			logger.error("Exception selecting object on ODB database : " + query, ex);
		}
		return e;
	}

	/**
	 * This method sets the ids in a graph of objects.
	 * 
	 * @param object
	 *            the object to set the ids for
	 */
	@SuppressWarnings("unchecked")
	synchronized final void setIds(Composite<?, ?> composite) {
		if (composite == null) {
			return;
		}
		super.setId(composite);
		logger.debug("Persisted object : " + composite);
		List<Composite<?, ?>> children = (List<Composite<?, ?>>) composite.getChildren();
		for (Composite<?, ?> child : children) {
			setIds(child);
		}
	}

}
