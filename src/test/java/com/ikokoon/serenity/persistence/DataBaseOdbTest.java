package com.ikokoon.serenity.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseOdbTest extends ATest {

	private IDataBase dataBase;
	private File dataBaseFile = new File("./src/test/resources/DataBaseOdbTest.odb");

	@Before
	public void clear() {
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile.getAbsolutePath(), mockInternalDataBase);
		DataBaseToolkit.clear(dataBase);
	}

	@After
	public void close() {
		dataBase.close();
		Toolkit.deleteFile(dataBaseFile, 3);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void persist() {
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		pakkage = (Package) dataBase.find(Package.class, pakkage.getId());
		assertNotNull(pakkage);

		Long classId = ((Class) pakkage.getChildren().get(0)).getId();
		Class klass = (Class) dataBase.find(Class.class, classId);
		assertNotNull(klass);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void findId() {
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
	@SuppressWarnings("rawtypes")
	public void findParameters() {
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
		parameters.add(methodDescription);
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
	@SuppressWarnings("rawtypes")
	public void removeId() throws Exception {
		// java.lang.Class<T> klass, Long id
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		Class klass = (Class) pakkage.getChildren().iterator().next();
		klass = (Class) dataBase.find(Class.class, klass.getId());
		assertNotNull(klass);
		dataBase.remove(Class.class, klass.getId());
		klass = (Class) dataBase.find(Class.class, klass.getId());
		assertNull(klass);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void find() {
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("name", pakkage.getName());
		List<Class> classes = dataBase.find(Class.class, parameters);
		assertEquals(1, classes.size());
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void findStartEnd() {
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		List<Class> classes = dataBase.find(Class.class, 0, Integer.MAX_VALUE);
		assertEquals(1, classes.size());

		pakkage = getPackage();
		dataBase.persist(pakkage);

		classes = dataBase.find(Class.class, 0, 1);
		assertEquals(1, classes.size());
	}

	// @Test
	@SuppressWarnings("rawtypes")
	public void memoryUsage() {
		long million = 1000000;
		long freeMemoryStart = Runtime.getRuntime().freeMemory() / million;
		LOGGER.info("Free memory start : " + freeMemoryStart);

		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, "./serenity/findbugs.serenity.odb", null);

		long freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		LOGGER.info("Free memory difference after initialise : " + (freeMemoryEnd - freeMemoryStart));

		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash("edu.umd.cs.findbugs"));
		assertNotNull(pakkage);

		freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		LOGGER.info("Free memory difference after select one package : " + (freeMemoryEnd - freeMemoryStart));

		pakkage = null;
		Runtime.getRuntime().gc();

		freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		LOGGER.info("Free memory difference after null the package : " + (freeMemoryEnd - freeMemoryStart));

		List<Package> packages = dataBase.find(Package.class);
		assertTrue(packages.size() > 0);

		freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		LOGGER.info("Free memory difference after select all packages : " + (freeMemoryEnd - freeMemoryStart));

		packages = null;
		Runtime.getRuntime().gc();

		freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		LOGGER.info("Free memory difference after null all the packges : " + (freeMemoryEnd - freeMemoryStart));
		LOGGER.info("Free memory end : " + freeMemoryEnd);
	}

}