package com.ikokoon.persistence;

import java.util.List;

import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.model.IComposite;

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
 */
public interface IDataBase {

	/**
	 * Static class for central access from the interface.
	 * 
	 * @author Michael Couck
	 */
	public static class DataBase {

		private static IDataBase dataBase;

		public static synchronized IDataBase getDataBase(boolean fresh) {
			if (dataBase == null || dataBase.isClosed()) {
				dataBase = getDataBase(IConstants.DATABASE_FILE, fresh);
			}
			return dataBase;
		}

		public static IDataBase getDataBase(String dataBaseFile, boolean fresh) {
			return new DataBaseXml(dataBaseFile, fresh);
		}

	}

	/**
	 * @param composite
	 * @return
	 */
	public IComposite persist(IComposite composite);

	/**
	 * @param id
	 * @return
	 */
	public IComposite find(Long id);

	/**
	 * @param parameters
	 * @return
	 */
	public IComposite find(List<Object> parameters);

	/**
	 * @param id
	 * @return
	 */
	public IComposite remove(Long id);

	/**
	 * @return
	 */
	public boolean isClosed();

	/**
	 * 
	 */
	public void close();

}