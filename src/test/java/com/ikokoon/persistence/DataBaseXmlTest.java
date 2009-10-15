package com.ikokoon.persistence;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.ikokoon.ATest;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.Line;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;

public class DataBaseXmlTest extends ATest {

	@Test
	public void persist() {
		// T object
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		dumpData(Package.class);
		pakkage = dataBase.find(Package.class, pakkage.getId());
		assertNotNull(pakkage);
	}

	@Test
	public void findClassId() {
		// Class<T> klass, Long id
		Package pakkage = getPackage();
		Class klass = getClass(pakkage);
		Method method = getMethod(klass);
		Line line = getLine(method);
		dataBase.persist(line);
		dumpData(Package.class);
		line = dataBase.find(Line.class, line.getId());
		assertNotNull(line);
	}

	@Test
	public void findClassParameters() {
		Package pakkage = getPackage();
		dataBase.persist(pakkage);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(NAME, packageName);
		pakkage = dataBase.find(Package.class, parameters);
		assertNotNull(pakkage);

		parameters.put(NAME, className);
		Class klass = dataBase.find(Class.class, parameters);
		assertNotNull(klass);

		parameters.put(PARENT, klass);
		parameters.put(NAME, methodName);
		parameters.put(DESCRIPTION, methodDescription);
		Method method = dataBase.find(Method.class, parameters);
		assertNotNull(method);

		parameters.clear();
		parameters.put(PARENT, method);
		parameters.put(NUMBER, lineNumber);
		Line line = dataBase.find(Line.class, parameters);
		assertNotNull(line);
	}

	@Test
	public void merge() {
		// T object
		Package pakkage = getPackage();
		Class klass = pakkage.getChildren().iterator().next();
		Method method = klass.getChildren().iterator().next();
		dataBase.persist(pakkage);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(PARENT, klass);
		parameters.put(NAME, methodName);
		parameters.put(DESCRIPTION, methodDescription);
		method = dataBase.find(Method.class, parameters);
		assertNotNull(method);

		String anotherName = "another name";
		method.setName(anotherName);
		method = dataBase.merge(method);

		parameters.put(METHOD_NAME, anotherName);
		method = dataBase.find(Method.class, method.getId());
		assertNotNull(method);
		assertEquals(anotherName, method.getName());
	}

	@Test
	public void findClassParametersFirstMaxResults() {
		// <T> List<T> - Class<T> klass, String queryName, Map<String, Object> parameters, int firstResult, int maxResults
		Class klass = new Class();
		klass.setName(CLASS_NAME);
		Method method = getMethod(klass);
		dataBase.persist(method);

		klass = new Class();
		klass.setName(CLASS_NAME + System.currentTimeMillis());
		method = getMethod(klass);
		dataBase.persist(method);

		klass = new Class();
		klass.setName(CLASS_NAME + System.currentTimeMillis());
		method = getMethod(klass);
		dataBase.persist(method);

		klass = new Class();
		klass.setName(CLASS_NAME + System.currentTimeMillis());
		method = getMethod(klass);
		dataBase.persist(method);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(NAME, methodName);
		List<Method> methods = dataBase.find(Method.class, parameters, 0, 2);
		assertEquals(2, methods.size());

		parameters.put(PARENT, klass);
		parameters.put(DESCRIPTION, methodDescription);
		methods = dataBase.find(Method.class, parameters, 0, Integer.MAX_VALUE);
		assertEquals(1, methods.size());
	}

	@Test
	public void findClassFirstMaxResults() {
		// <T> List<T> - Class<T> klass, int firstResult, int maxResults
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		pakkage = getPackage();
		dataBase.persist(pakkage);
		pakkage = getPackage();
		dataBase.persist(pakkage);
		pakkage = getPackage();
		dataBase.persist(pakkage);

		List<Method> methods = dataBase.find(Method.class, 0, 3);
		assertEquals(3, methods.size());
	}

	@Test
	public void setId() {
		Package pakkage = getPackage();
		Long id = new Long(System.currentTimeMillis());
		dataBase.setId(pakkage, Package.class, id, true);
		assertEquals(id, pakkage.getId());
	}

	@Test
	public void removeClassId() throws Exception {
		// java.lang.Class<T> klass, Long id
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		Class klass = (Class) pakkage.getChildren().iterator().next();
		klass = dataBase.find(Class.class, klass.getId());
		assertNotNull(klass);
		dumpData(Package.class);
		dataBase.remove(Class.class, klass.getId());
		dumpData(Package.class);
		klass = dataBase.find(Class.class, klass.getId());
		assertNull(klass);
	}

	@Test
	public void insertPerformance() throws Exception {
		// Test the insert performance
		double inserts = 100;
		double start = System.currentTimeMillis();
		for (int i = 0; i < inserts; i++) {
			Package pakkage = getPackage();
			pakkage.setName(pakkage.getName() + System.currentTimeMillis());
			Class klass = (Class) pakkage.getChildren().iterator().next();
			klass.setName(klass.getName() + System.currentTimeMillis());
			dataBase.persist(pakkage);
		}
		double end = System.currentTimeMillis();
		double duration = (end - start) / 1000d;
		double insertsPerSecond = ((inserts * 6d) / duration);
		logger.error("Duration : " + duration + ", inserts per second : " + insertsPerSecond);
		double minimumInsertsPerSecond = 100d;
		assertTrue(insertsPerSecond > minimumInsertsPerSecond);
	}

	@Test
	public void selectPerformance() throws Exception {
		try {
			Package pakkage = getPackage();
			dataBase.persist(pakkage);
			Class klass = pakkage.getChildren().iterator().next();
			Method method = klass.getChildren().iterator().next();
			Line line = method.getChildren().iterator().next();
			for (int i = 0; i < 100; i++) {
				pakkage = getPackage();
				klass = pakkage.getChildren().iterator().next();
				method = klass.getChildren().iterator().next();
				line = method.getChildren().iterator().next();

				pakkage.setName(pakkage.getName() + "." + i);

				klass.setName(klass.getName() + "." + i);

				method.setName(method.getName() + "." + i);
				method.setDescription(method.getDescription() + "." + i);
				method.setClassName(klass.getName());

				line.setNumber(line.getNumber() + i);
				line.setClassName(klass.getName());
				line.setMethodName(method.getName());
				dataBase.persist(pakkage);
			}

			checkDuplicates(Package.class);
			checkDuplicates(Class.class);
			checkDuplicates(Method.class);
			checkDuplicates(Line.class);

			// Test the select performance
			double selects = 1000;
			double start = System.currentTimeMillis();
			Map<String, Object> packageParameters = new HashMap<String, Object>();
			packageParameters.put(NAME, packageName);

			Map<String, Object> lineParameters = new HashMap<String, Object>();
			lineParameters.put(CLASS_NAME, className);
			lineParameters.put(METHOD_NAME, methodName);
			lineParameters.put(NUMBER, lineNumber);

			for (int i = 0; i < selects; i++) {
				pakkage = dataBase.find(Package.class, pakkage.getId());
				assertNotNull(pakkage);
				klass = dataBase.find(Class.class, klass.getId());
				assertNotNull(klass);
				method = dataBase.find(Method.class, method.getId());
				assertNotNull(method);
				line = dataBase.find(Line.class, line.getId());
				assertNotNull(line);

				assertEquals(2, dataBase.find(Package.class, 0, 2).size());
				assertEquals(1, dataBase.find(Package.class, packageParameters, 0, Integer.MAX_VALUE).size());
				assertEquals(1, dataBase.find(Line.class, lineParameters, 0, Integer.MAX_VALUE).size());
				assertNotNull(dataBase.find(Line.class, lineParameters));
			}
			double end = System.currentTimeMillis();
			double duration = (end - start) / 1000d;
			double selectsPerSecond = (selects * 8 / duration);
			logger.error("Duration : " + duration + ", selects per second : " + selectsPerSecond);
			double minimumSelectsPerSecond = 1000;
			assertTrue(selectsPerSecond > minimumSelectsPerSecond);

			start = System.currentTimeMillis();
			for (int i = 0; i < selects; i++) {
				assertNotNull(dataBase.find(Line.class, lineParameters));
			}
			end = System.currentTimeMillis();
			duration = (end - start) / 1000d;
			selectsPerSecond = (selects * 7 / duration);
			logger.error("Only line : Duration : " + duration + ", selects per second : " + selectsPerSecond);
			assertTrue(selectsPerSecond > minimumSelectsPerSecond);
		} finally {
			// dumpData(Package.class);
			// dumpData(Class.class);
			// dumpData(Method.class);
			// dumpData(Line.class);
			// dumpData(Efferent.class);
			// dumpData(Afferent.class);
		}
	}

	private void checkDuplicates(java.lang.Class klass) {
		Set set = new TreeSet();
		List objects = dataBase.find(klass, 0, Integer.MAX_VALUE);
		for (Object object : objects) {
			Long id = dataBase.getId(object.getClass(), object);
			if (set.contains(id)) {
				logger.warn("Duplicate id : " + id);
			}
			set.add(id);
		}
	}

}