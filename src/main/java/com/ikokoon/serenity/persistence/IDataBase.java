package com.ikokoon.serenity.persistence;

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
		
		public static synchronized Map<String, IDataBase> getDataBases() {
			return dataBases;
		}

		/**
		 * Accesses a database. In the case the database is not open one will be instanciated on the database file specified. In the case the database
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
		public static synchronized final <E extends IDataBase> IDataBase getDataBase(Class<E> klass, String dataBaseFile, boolean create,
				IDataBase internalDataBase) {
			IDataBase dataBase = dataBases.get(dataBaseFile);
			IDataBaseListener dataBaseListener = getDataBaseListener();

			if (dataBase == null || dataBase.isClosed()) {
				dataBase = ObjectFactory.getObject(klass, internalDataBase, dataBaseListener, dataBaseFile, create);
				logger.info("Adding database : " + dataBase);
				// Thread.dumpStack();
				dataBases.put(dataBaseFile, dataBase);
			} else {
				if (!klass.isAssignableFrom(dataBase.getClass())) {
					dataBase.close();
					dataBase = ObjectFactory.getObject(klass, internalDataBase, dataBaseListener, dataBaseFile, create);
					// Thread.dumpStack();
					logger.info("Adding database : " + dataBase);
					dataBases.put(dataBaseFile, dataBase);
				}
			}
			return dataBase;
		}

		private static IDataBaseListener getDataBaseListener() {
			IDataBaseListener dataBaseListener = new IDataBaseListener() {
				public void fireDataBaseEvent(IDataBaseEvent dataBaseEvent) {
					if (dataBaseEvent.getEventType().equals(IDataBaseEvent.Type.DATABASE_CLOSE)) {
						IDataBase dataBase = dataBaseEvent.getDataBase();
						if (!dataBases.values().remove(dataBase)) {
							logger.warn("Database not removed from map : " + dataBase);
						}
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
	public <E extends Composite<?, ?>> E find(Class<E> klass, List<Object> parameters);

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