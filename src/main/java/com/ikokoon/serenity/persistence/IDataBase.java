package com.ikokoon.serenity.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ikokoon.serenity.model.IComposite;

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
	public static class DataBaseManager {

		private static Map<String, IDataBase> dataBases = new HashMap<String, IDataBase>();

		public static synchronized final IDataBase getDataBase(String dataBaseFile, boolean create) {
			IDataBase dataBase = dataBases.get(dataBaseFile);
			if (dataBase == null) {
				dataBase = new DataBaseXml(dataBaseFile, create);
				dataBases.put(dataBaseFile, dataBase);
			}
			return dataBase;
		}

	}

	/**
	 * @param composite
	 * @return
	 */
	public IComposite<?, ?> persist(IComposite<?, ?> composite);

	/**
	 * @param id
	 * @return
	 */
	public IComposite<?, ?> find(Long id);

	/**
	 * @param parameters
	 * @return
	 */
	public IComposite<?, ?> find(List<Object> parameters);

	/**
	 * @param id
	 * @return
	 */
	public IComposite<?, ?> remove(Long id);

	/**
	 * @return
	 */
	public boolean isClosed();

	/**
	 * 
	 */
	public void close();

}