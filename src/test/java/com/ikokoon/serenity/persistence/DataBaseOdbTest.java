package com.ikokoon.serenity.persistence;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseOdbTest extends ATest {

	// @Test
	@SuppressWarnings("unchecked")
	public void memoryUsage() {
		DataBaseToolkit.clear(dataBase);
		dataBase.close();

		long million = 1000000;
		long freeMemoryStart = Runtime.getRuntime().freeMemory() / million;
		logger.info("Free memory start : " + freeMemoryStart);

		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, "./serenity/findbugs.serenity.odb", false, null);

		long freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		logger.info("Free memory difference after initialise : " + (freeMemoryEnd - freeMemoryStart));

		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash("edu.umd.cs.findbugs"));
		assertNotNull(pakkage);

		freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		logger.info("Free memory difference after select one package : " + (freeMemoryEnd - freeMemoryStart));

		pakkage = null;
		Runtime.getRuntime().gc();

		freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		logger.info("Free memory difference after null the package : " + (freeMemoryEnd - freeMemoryStart));

		List<Package> packages = dataBase.find(Package.class);
		assertTrue(packages.size() > 0);

		freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		logger.info("Free memory difference after select all packages : " + (freeMemoryEnd - freeMemoryStart));

		packages = null;
		Runtime.getRuntime().gc();

		freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		logger.info("Free memory difference after null all the packges : " + (freeMemoryEnd - freeMemoryStart));
		logger.info("Free memory end : " + freeMemoryEnd);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void persist() {
		DataBaseToolkit.clear(dataBase);
		dataBase.close();
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, true, null);
		DataBaseToolkit.clear(dataBase);

		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		pakkage = (Package) dataBase.find(Package.class, pakkage.getId());
		assertNotNull(pakkage);

		Long classId = ((Class) pakkage.getChildren().get(0)).getId();
		Class klass = (Class) dataBase.find(Class.class, classId);
		assertNotNull(klass);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findId() {
		DataBaseToolkit.clear(dataBase);
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, true, null);
		DataBaseToolkit.clear(dataBase);
		DataBaseToolkit.dump(dataBase, new DataBaseToolkit.ICriteria() {
			public boolean satisfied(Composite<?, ?> composite) {
				return true;
			}
		}, this.getClass().getSimpleName() + " database dump : ");
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		// 7873017250689681547, 437917821655607927
		Line line = (Line) dataBase.find(Line.class, 7873017250689681547l);
		assertNotNull(line);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findParameters() {
		DataBaseToolkit.clear(dataBase);
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, true, null);
		DataBaseToolkit.clear(dataBase);

		Package pakkage = getPackage();
		dataBase.persist(pakkage);

		List<Object> parameters = new ArrayList<Object>();
		parameters.add(packageName);
		pakkage = (Package) dataBase.find(Package.class, parameters);
		assertNotNull(pakkage);

		parameters.clear();
		parameters.add(className);
		Class klass = (Class) dataBase.find(Class.class, parameters);
		assertNotNull(klass);

		parameters.clear();
		parameters.add(klass.getName());
		parameters.add(methodName);
		parameters.add(methodSignature);
		Method method = (Method) dataBase.find(Method.class, parameters);
		assertNotNull(method);

		parameters.clear();
		parameters.add(klass.getName());
		parameters.add(method.getName());
		parameters.add(lineNumber);
		Line line = (Line) dataBase.find(Line.class, parameters);
		assertNotNull(line);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void removeId() throws Exception {
		DataBaseToolkit.clear(dataBase);
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, true, null);
		DataBaseToolkit.clear(dataBase);

		// java.lang.Class<T> klass, Long id
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		Class klass = (Class) pakkage.getChildren().iterator().next();
		klass = (Class) dataBase.find(Class.class, klass.getId());
		assertNotNull(klass);
		dataBase.remove(Class.class, klass.getId());
		klass = (Class) dataBase.find(Class.class, klass.getId());
		assertNull(klass);
		dataBase.close();
		dataBase = null;
	}

	@Test
	@SuppressWarnings("unchecked")
	public void find() {
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, IConstants.DATABASE_FILE_ODB, true, null);
		DataBaseToolkit.clear(dataBase);

		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("name", pakkage.getName());
		List<Class> classes = dataBase.find(Class.class, parameters);
		assertEquals(1, classes.size());
		dataBase.close();
		dataBase = null;
	}

}