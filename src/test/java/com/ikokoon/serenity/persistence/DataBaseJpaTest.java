package com.ikokoon.serenity.persistence;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseJpaTest extends ATest {

	@SuppressWarnings("unchecked")
	public void memoryUsage() {
		dataBase.close();

		long million = 1000000;
		long freeMemoryStart = Runtime.getRuntime().freeMemory() / million;
		logger.info("Free memory start : " + freeMemoryStart);

		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseJpa.class, "./serenity/serenity.db", false, null);

		long freeMemoryEnd = Runtime.getRuntime().freeMemory() / million;
		logger.info("Free memory difference after initialise : " + (freeMemoryEnd - freeMemoryStart));

		Package<?, ?> pakkage = dataBase.find(Package.class, Toolkit.hash("edu.umd.cs.findbugs"));
		logger.info(pakkage);
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
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseJpa.class, IConstants.DATABASE_FILE_JPA, true, null);

		Package<?, ?> pakkage = getPackage();

		// dataBase.persist(klass);
		dataBase.persist(pakkage);
		pakkage = (Package) dataBase.find(Package.class, pakkage.getId());
		assertNotNull(pakkage);

		Class<?, ?> klass = pakkage.getChildren().get(0);
		Long classId = klass.getId();
		klass = (Class) dataBase.find(Class.class, classId);
		assertNotNull(klass);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findId() {
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseJpa.class, IConstants.DATABASE_FILE_JPA, true, null);
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		// 7873017250689681547, 437917821655607927
		Line line = (Line) dataBase.find(Line.class, 7873017250689681547l);
		assertNotNull(line);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void findParameters() {
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseJpa.class, IConstants.DATABASE_FILE_JPA, true, null);
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
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseJpa.class, IConstants.DATABASE_FILE_JPA, true, null);
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

}