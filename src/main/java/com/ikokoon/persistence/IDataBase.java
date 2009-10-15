package com.ikokoon.persistence;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This is the persistence interface for the Serenity code coverage and dependency functionality. This database implementations are implemented as
 * decorated classes. The can be chained to add functionality. Jpa is not fast enough so a database object for the cache needs to be added to the
 * front end to use as much as possible the in memory access to the data model.
 * 
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
public interface IDataBase {

	/**
	 * Static class for central access from the interface.
	 * 
	 * @author Michael Couck
	 */
	public static class DataBase {

		private static IDataBase dataBase;

		public static IDataBase getDataBase() {
			if (dataBase == null || dataBase.isClosed()) {
				// dataBase = new DataBaseJpa(null);
				// dataBase = new DataBaseCache(new DataBaseJpa(null));
				// dataBase = new DataBaseDb4o();
				// dataBase = new DataBaseNeodatis();
				dataBase = new DataBaseXml(null);
			}
			return dataBase;
		}

		public static IDataBase getDataBase(File file) {
			return new DataBaseXml(file);
		}
	}

	/**
	 * Removes an object based on the id field of the object.
	 * 
	 * @param <T>
	 *            the type of the class
	 * @param klass
	 *            the klass of the object to delete
	 * @param id
	 *            the identifier field value of the object to be deleted
	 * @return the object that has been removed
	 */
	public <T> T remove(Class<T> klass, Long id);

	/**
	 * @param <T>
	 * @param klass
	 * @param parameters
	 * @return
	 */
	public <T> T find(Class<T> klass, Map<String, Object> parameters);

	/**
	 * Persists an object to the database.
	 * 
	 * @param <T>
	 *            the type of the object to persist
	 * @param object
	 *            the object to persist
	 * @return the object persisted and refreshed, so if the id gets set by the underlying persistence store then the returned object will heve the id
	 *         set
	 */
	public <T> T persist(T t);

	/**
	 * Merges any changes to an object to the persistent store underneath. The object must already have been persisted and the id of the object will
	 * need to have been set.
	 * 
	 * @param <T>
	 *            the type of object to merge
	 * @param object
	 *            the object to be merged
	 * @return the merged object refreshed from the persistent store
	 */
	public <T> T merge(T t);

	/**
	 * @param <T>
	 * @param klass
	 * @param id
	 * @return
	 */
	public <T> T find(Class<T> klass, Long id);

	/**
	 * @param <T>
	 * @param queryName
	 * @param parameters
	 * @return
	 */
	public <T> T find(String queryName, Map<String, Object> parameters);

	/**
	 * @param <T>
	 * @param klass
	 * @param queryName
	 * @param parameters
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public <T> List<T> find(Class<T> klass, String queryName, Map<String, Object> parameters, int firstResult, int maxResults);

	/**
	 * @param <T>
	 * @param klass
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public <T> List<T> find(Class<T> klass, int firstResult, int maxResults);

	/**
	 * @param <T>
	 * @param klass
	 * @param parameters
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public <T> List<T> find(Class<T> klass, Map<String, Object> parameters, int firstResult, int maxResults);

	/**
	 * @param query
	 * @return
	 */
	public int execute(String query);

	/**
	 * @return
	 */
	public boolean isClosed();

	/**
	 * Closes the database flushing any resources and releasing resources where necessary.
	 */
	public void close();

}