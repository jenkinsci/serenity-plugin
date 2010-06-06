package com.ikokoon.serenity.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ikokoon.serenity.model.Composite;
import com.ikokoon.toolkit.ObjectFactory;

/**
 * This is the persistence interface for the Serenity code coverage and dependency functionality.
 *
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 *
 *          -------------------------------------------------------------<br>
 * @version 01.1 <br>
 *          Removed the JPA oriented methods in favour of in memory as JPA was hopelessly too slow.
 *          -------------------------------------------------------------<br>
 * @version 01.2 <br>
 *          Added the listeners to the databases so the manager can release them from the map. Changed the IDataBase interface to be generic so
 *          Neodatis can be integrated easier and modified several methods to include the class that is being selected as most persistence libraries
 *          will need this for their selection.
 * @version 01.3 <br>
 *          Added a find method that can be implemented as a fuzzy search method by the implementation with multiple search criteria. For example
 *          'where x like n and y in m'.
 */
public interface IDataBase {

	/**
	 * This class manages the databases if there are more than one. It also listens to events that the databases might throw like a close and so on.
	 *
	 * @author Michael Couck
	 * @since 12.08.09
	 * @version 01.00
	 */
	public static class DataBaseManager {

		private static Logger logger = Logger.getLogger(DataBaseManager.class);
		/** The map of open databases keyed on the database file name. */
		private static Map<String, IDataBase> dataBases = new HashMap<String, IDataBase>();
		/** The map of database listeners keyed on the database file name. */
		private static Map<String, List<IDataBaseListener>> dataBaseListeners = new HashMap<String, List<IDataBaseListener>>();

		/**
		 * Access to all the databases in the current VM.
		 *
		 * @return all the databases
		 */
		public static synchronized Map<String, IDataBase> getDataBases() {
			return dataBases;
		}

		/**
		 * Accesses a database. In the case the database is not open one will be instantiated on the database file specified. In the case the database
		 * is open but not the right type of database, the old one will be closed and the new one will be opened on the database file, otherwise the
		 * database is returned.
		 *
		 * @param <E>
		 *            the database type
		 * @param klass
		 *            the class of database to initialise
		 * @param dataBaseFile
		 *            the database file to open the database on
		 * @param create
		 *            whether to create a new database, i.e. deleting the old database file and creating a new one
		 * @return the database
		 */
		public static synchronized final <E extends IDataBase> IDataBase getDataBase(Class<E> klass, String dataBaseFile, IDataBase internalDataBase) {
			IDataBase dataBase = dataBases.get(dataBaseFile);
			if (dataBase == null || dataBase.isClosed()) {
				getDataBaseListener(dataBaseFile);
				dataBase = ObjectFactory.getObject(klass, dataBaseFile, internalDataBase);
				logger.info("Adding database : " + dataBase);
				dataBases.put(dataBaseFile, dataBase);
			}
			logger.info("Returned database : " + klass + ", data base : " + dataBase + ", file : " + dataBaseFile);
			return dataBase;
		}

		public static synchronized void addDataBaseListener(String dataBaseFile, IDataBaseListener dataBaseListener) {
			List<IDataBaseListener> dataBaseListeners = DataBaseManager.dataBaseListeners.get(dataBaseFile);
			if (dataBaseListeners == null) {
				dataBaseListeners = new ArrayList<IDataBaseListener>();
				DataBaseManager.dataBaseListeners.put(dataBaseFile, dataBaseListeners);
			}
			dataBaseListeners.add(dataBaseListener);
		}

		public static synchronized void removeDataBaseListener(String dataBaseFile, IDataBaseListener dataBaseListener) {
			List<IDataBaseListener> dataBaseListeners = DataBaseManager.dataBaseListeners.get(dataBaseFile);
			if (dataBaseListeners != null) {
				dataBaseListeners.remove(dataBaseListener);
			}
		}

		public static synchronized void fireDataBaseEvent(String dataBaseFile, IDataBaseEvent dataBaseEvent) {
			List<IDataBaseListener> dataBaseListeners = DataBaseManager.dataBaseListeners.get(dataBaseFile);
			if (dataBaseListeners != null) {
				IDataBaseListener[] array = dataBaseListeners.toArray(new IDataBaseListener[dataBaseListeners.size()]);
				for (IDataBaseListener dataBaseListener : array) {
					dataBaseListener.fireDataBaseEvent(dataBaseEvent);
				}
			}
		}

		private synchronized static IDataBaseListener getDataBaseListener(final String dataBaseFile) {
			IDataBaseListener dataBaseListener = new IDataBaseListener() {

				{
					DataBaseManager.addDataBaseListener(dataBaseFile, this);
				}

				public void fireDataBaseEvent(IDataBaseEvent dataBaseEvent) {
					if (dataBaseEvent.getEventType().equals(IDataBaseEvent.Type.DATABASE_CLOSE)) {
						IDataBase dataBase = dataBaseEvent.getDataBase();
						if (!dataBases.values().remove(dataBase)) {
							logger.info("Database not removed : " + dataBase);
						} else {
							logger.info("Removed database : " + dataBase);
						}
						DataBaseManager.removeDataBaseListener(dataBaseFile, this);
					}
				}
			};
			return dataBaseListener;
		}

	}

	/**
	 * Persists an object to a persistent store.
	 *
	 * @param composite
	 *            the composite to persist
	 * @return the composite that is persisted, typically the id will be set by the underlying implementation
	 */
	public <E extends Composite<?, ?>> E persist(E composite);

	/**
	 * Selects a composite based on the class type and the id of the class.
	 *
	 * @param <E>
	 *            the return type of the class
	 * @param klass
	 *            the type of class to select
	 * @param id
	 *            the unique id of the class
	 * @return the composite with the specified id
	 */
	public <E extends Composite<?, ?>> E find(Class<E> klass, Long id);

	/**
	 * Selects a class based on the combination of field values in the parameter list.
	 *
	 * @param <E>
	 *            the return type of the class
	 * @param klass
	 *            the type of class to select
	 * @param parameters
	 *            the unique combination of field values to select the class with
	 * @return the composite with the specified unique field combination
	 */
	public <E extends Composite<?, ?>> E find(Class<E> klass, List<?> parameters);

	/**
	 * Selects a list of objects based on the values in the objects and the class of the object. The implementations can implement multiple search
	 * criteria including fuzzy selection etc.
	 *
	 * @param <E>
	 *            the return type of the class
	 * @param klass
	 *            the type of class to select
	 * @param parameters
	 *            the parameters to do the selection with. These are the fields in the objects and the values
	 * @return the list of composites that match the selection criteria
	 */
	public <E extends Composite<?, ?>> List<E> find(Class<E> klass, Map<String, ?> parameters);

	/**
	 * Selects all the classes of a particular type. Note this could potentially return the whole database.
	 *
	 * @param <E>
	 *            the return type of the class
	 * @param klass
	 *            the type of class to select
	 * @return a list of all the objects in the database that have the specified class type
	 */
	public <E extends Composite<?, ?>> List<E> find(Class<E> klass);

	/**
	 * Selects all the classes of a particular type starting from an index and going to an index.
	 *
	 * @param <E>
	 *            the return type of the class
	 * @param klass
	 *            the type of class to select
	 * @param start
	 *            the beginning index to retrieve objects from
	 * @param end
	 *            the end index for the list of objects
	 * @return a list of all the objects in the database that have the specified class type
	 */
	public <E extends Composite<?, ?>> List<E> find(Class<E> klass, int start, int end);

	/**
	 * Removes an object from the database and returns the removed object as a convenience.
	 *
	 * @param <E>
	 *            the return type of the class
	 * @param klass
	 *            the type of class to select
	 * @param id
	 *            the unique id of the class
	 * @return the composite with the specified id
	 */
	public <E extends Composite<?, ?>> E remove(Class<E> klass, Long id);

	/**
	 * Checks the open status of the database.
	 *
	 * @return whether the database is open or closed
	 */
	public boolean isClosed();

	/**
	 * Closes the database.
	 */
	public void close();

}