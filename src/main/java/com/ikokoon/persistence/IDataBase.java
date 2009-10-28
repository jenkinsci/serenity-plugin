package com.ikokoon.persistence;

import java.util.List;

import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.model.IComposite;

/**
 * This is the persistence interface for the Serenity code coverage and dependency functionality. This database implementations are implemented as
 * decorated classes. The can be chained to add functionality. Jpa is not fast enough so a database object for the cache needs to be added to the
 * front end to use as much as possible the in memory access to the data model.
 * 
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 * 
 *          -------------------------------------------------------------<br>
 * @version 01.1 <br>
 *          Removed the JPA oriented methods in favour of in memory as JPA was hopelessly too slow.
 */
public interface IDataBase {

	/**
	 * Static class for central access from the interface.
	 * 
	 * @author Michael Couck
	 */
	public static class DataBase {

		private static IDataBase dataBase;

		public static synchronized IDataBase getDataBase() {
			if (dataBase == null || dataBase.isClosed()) {
				dataBase = getDataBase(IConstants.DATABASE_FILE);
			}
			return dataBase;
		}

		public static IDataBase getDataBase(String dataBaseFile) {
			return new DataBaseXml(dataBaseFile);
		}

	}

	/**
	 * @param <T>
	 * @param t
	 * @return
	 */
	public IComposite persist(IComposite composite);

	/**
	 * @param <T>
	 * @param id
	 * @return
	 */
	public IComposite find(Long id);

	/**
	 * @param <T>
	 * @param parameters
	 * @return
	 */
	public IComposite find(List<Object> parameters);

	/**
	 * @param <T>
	 * @param id
	 * @return
	 */
	public IComposite remove(Long id);

	/**
	 * @return
	 */
	public boolean isClosed();

	/**
	 * Closes the database flushing any resources and releasing resources where necessary.
	 */
	public void close();

}