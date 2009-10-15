package com.ikokoon.persistence;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ikokoon.ATest;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.Line;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;

public class DataBaseJpaTest extends ATest {

	@Test
	public void findClassId() {
		// Class<T> klass, Long id
		Package pakkage = getPackage();
		Class klass = getClass(pakkage);
		Method method = getMethod(klass);
		Line line = getLine(method);
		dataBase.persist(line);

		line = dataBase.find(Line.class, line.getId());
		assertNotNull(line);
	}

	@Test
	public void removeClassId() throws Exception {
		// java.lang.Class<T> klass, Long id
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		Class klass = (Class) pakkage.getChildren().iterator().next();

		klass = dataBase.find(Class.class, klass.getId());
		assertNotNull(klass);

		dataBase.remove(Class.class, klass.getId());

		klass = dataBase.find(Class.class, klass.getId());
		assertNull(klass);
	}

	@Test
	public void persistObject() {
		// T object
		Package pakkage = getPackage();
		dataBase.persist(pakkage);

		pakkage = dataBase.find(Package.class, pakkage.getId());
		assertNotNull(pakkage);
	}

	@Test
	public void mergeObject() {
		// T object
		Package pakkage = getPackage();
		Class klass = getClass(pakkage);
		Method method = getMethod(klass);
		dataBase.persist(pakkage);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(CLASS_NAME, className);
		parameters.put(METHOD_NAME, methodName);
		parameters.put(METHOD_DESCRIPTION, methodDescription);
		method = dataBase.find(Method.SELECT_METHOD_BY_CLASS_NAME_AND_METHOD_NAME_AND_METHOD_DESCRIPTION, parameters);
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
	public void findQueryNameParameters() {
		// String queryName, Map<String, Object> parameters
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(CLASS_NAME, className);
		parameters.put(METHOD_NAME, methodName);
		parameters.put(METHOD_DESCRIPTION, methodDescription);
		Method method = dataBase.find(Method.SELECT_METHOD_BY_CLASS_NAME_AND_METHOD_NAME_AND_METHOD_DESCRIPTION, parameters);
		assertNotNull(method);

		Line line = (Line) pakkage.getChildren().iterator().next().getChildren().iterator().next().getChildren().iterator().next();
		parameters.clear();
		parameters.put(CLASS_NAME, className);
		parameters.put(METHOD_NAME, methodName);
		parameters.put(METHOD_DESCRIPTION, methodDescription);
		parameters.put(NUMBER, lineNumber);
		line = dataBase.find(Line.SELECT_LINES_BY_CLASS_NAME_AND_METHOD_NAME_AND_DESCRIPTION_AND_NUMBER, parameters);
		assertNotNull(line);
	}

	@Test
	public void findClassQueryNameParametersFirstMaxResults() {
		// <T> List<T> - Class<T> klass, String queryName, Map<String, Object> parameters, int firstResult, int maxResults
		Class klass = new Class();
		klass.setName(CLASS_NAME + System.currentTimeMillis());
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
		List<Method> methods = dataBase.find(Method.class, Method.SELECT_METHODS_BY_NAME, parameters, 0, 2);
		assertEquals(2, methods.size());
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
	public void performance() {
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
		logger.error("Duration : " + duration + ", inserts per second : " + ((inserts * 6d) / duration));

		Package pakkage = getPackage();
		Class klass = (Class) pakkage.getChildren().iterator().next();
		Method method = (Method) klass.getChildren().iterator().next();
		Line line = (Line) method.getChildren().iterator().next();
		dataBase.persist(pakkage);

		double selects = 1000;
		start = System.currentTimeMillis();
		Map<String, Object> packageParameters = new HashMap<String, Object>();
		packageParameters.put(NAME, packageName);

		for (int i = 0; i < selects; i++) {
			assertNotNull(dataBase.find(Package.class, pakkage.getId()));
			assertNotNull(dataBase.find(Class.class, klass.getId()));
			assertNotNull(dataBase.find(Method.class, method.getId()));
			assertNotNull(dataBase.find(Line.class, line.getId()));

			assertEquals(2, dataBase.find(Package.class, 0, 2).size());
			assertNotNull(dataBase.find(Package.SELECT_PACKAGES_BY_NAME, packageParameters));
			assertEquals(1, dataBase.find(Package.class, Package.SELECT_PACKAGES_BY_NAME, packageParameters, 0, Integer.MAX_VALUE).size());
		}
		end = System.currentTimeMillis();
		duration = (end - start) / 1000d;
		double selectsPerSecond = (selects * 7 / duration);
		logger.error("Duration : " + duration + ", selects per second : " + selectsPerSecond);
		double minimumSelectsPerSecond = 100;
		assertTrue(selectsPerSecond > minimumSelectsPerSecond);
	}

	@Test
	public void setId() {
		Package pakkage = getPackage();
		Long id = new Long(System.currentTimeMillis());
		dataBase.setId(pakkage, Package.class, id, true);
		assertEquals(id, pakkage.getId());
	}

}