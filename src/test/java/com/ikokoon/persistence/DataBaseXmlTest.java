package com.ikokoon.persistence;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.ikokoon.ATest;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.IComposite;
import com.ikokoon.instrumentation.model.Line;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseXmlTest extends ATest {

	@Test
	public void insert() {
		DataBaseXml dataBase = (DataBaseXml) this.dataBase;

		LinkedList<IComposite> list = getList();

		// Check the performance
		list.clear();
		double inserts = 1000;
		double start = System.currentTimeMillis();
		for (long i = 0; i < inserts; i++) {
			Class klass = new Class();
			klass.setName("" + i);
			klass.setId(i);
			dataBase.insert(list, klass, i);
		}
		double end = System.currentTimeMillis();
		double duration = end - start;
		double insertsPerSecond = inserts / (duration / 60);
		logger.info("Duration : " + duration + ", inserts per second : " + insertsPerSecond);
	}

	private LinkedList<IComposite> getList() {
		DataBaseXml dataBase = (DataBaseXml) this.dataBase;

		LinkedList<IComposite> list = new LinkedList<IComposite>();
		Class klass = new Class();
		klass.setName("a");
		Long id = Toolkit.hash(klass.getName());
		klass.setId(id);
		dataBase.insert(list, klass, id);

		klass = new Class();
		klass.setName("c");
		id = Toolkit.hash(klass.getName());
		klass.setId(id);
		dataBase.insert(list, klass, id);

		klass = new Class();
		klass.setName("b");
		id = Toolkit.hash(klass.getName());
		klass.setId(id);
		dataBase.insert(list, klass, id);

		return list;
	}

	@Test
	public void search() {
		DataBaseXml dataBase = (DataBaseXml) this.dataBase;
		LinkedList<IComposite> list = getList();
		Long id = Toolkit.hash("b");
		Object object = dataBase.search(list, id);
		assertNotNull(object);
	}

	@Test
	public void persist() {
		// T object
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		pakkage = (Package) dataBase.find(pakkage.getId());
		assertNotNull(pakkage);

		Long classId = pakkage.getChildren().get(0).getId();
		Class klass = (Class) dataBase.find(classId);
		assertNotNull(klass);
	}

	@Test
	public void findId() {
		// Class<T> klass, Long id
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		Line line = (Line) dataBase.find(5286208520220707252l);
		assertNotNull(line);
	}

	@Test
	public void findParameters() {
		Package pakkage = getPackage();
		dataBase.persist(pakkage);

		List<Object> parameters = new ArrayList<Object>();
		parameters.add(packageName);
		pakkage = (Package) dataBase.find(parameters);
		assertNotNull(pakkage);

		parameters.clear();
		parameters.add(className);
		Class klass = (Class) dataBase.find(parameters);
		assertNotNull(klass);

		parameters.clear();
		parameters.add(klass.getName());
		parameters.add(methodName);
		parameters.add(methodDescription);
		Method method = (Method) dataBase.find(parameters);
		assertNotNull(method);

		parameters.clear();
		parameters.add(klass.getName());
		parameters.add(method.getName());
		parameters.add(lineNumber);
		Line line = (Line) dataBase.find(parameters);
		assertNotNull(line);
	}

	@Test
	public void removeId() throws Exception {
		// java.lang.Class<T> klass, Long id
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		Class klass = (Class) pakkage.getChildren().iterator().next();
		klass = (Class) dataBase.find(klass.getId());
		assertNotNull(klass);
		dataBase.remove(klass.getId());
		klass = (Class) dataBase.find(klass.getId());
		assertNull(klass);
	}

	@Test
	public void persistPerformance() throws Exception {
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
	public void findPerformance() throws Exception {
		long size = 10000;
		for (int i = 0; i < size; i++) {
			Package pakkage = getPackage();
			pakkage.setName(packageName + "." + i);
			Class klass = getClass(pakkage);
			klass.setName(className + "." + i);
			Method method = getMethod(klass);
			method.setName(method.getName() + "." + i);
			method.setDescription(method.getDescription() + "." + i);
			method.setClassName(klass.getName());
			Line line = getLine(method);
			line.setNumber(i);
			line.setClassName(klass.getName());
			line.setMethodName(method.getName());
			dataBase.persist(pakkage);
		}

		// Test the select performance
		double selects = 10000;
		double start = System.currentTimeMillis();
		List<Object> packageParameters = new ArrayList<Object>();
		packageParameters.add(packageName + "." + 13);

		List<Object> lineParameters = new ArrayList<Object>();
		lineParameters.add(className + "." + 26);
		lineParameters.add(methodName + "." + 26);
		lineParameters.add(26d);

		Long packageId = Toolkit.hash(packageName + "." + 233);
		Long classId = Toolkit.hash(className + "." + 871);
		Long methodId = Toolkit.hash(className + "." + 441 + methodName + "." + 441 + methodDescription + "." + 441);
		Long lineId = Toolkit.hash(className + "." + 359 + methodName + "." + 359 + "" + 359d);

		for (int i = 0; i < selects; i++) {
			dataBase.find(packageId);
			// assertNotNull(pakkage);
			dataBase.find(classId);
			// assertNotNull(klass);
			dataBase.find(methodId);
			// assertNotNull(method);
			dataBase.find(lineId);
			// assertNotNull(line);

			dataBase.find(packageParameters);
			// assertNotNull(object);
			dataBase.find(lineParameters);
			// assertNotNull(object);
			dataBase.find(lineParameters);
			// assertNotNull(object);
		}
		double end = System.currentTimeMillis();
		double duration = (end - start) / 1000d;
		double selectsPerSecond = (selects * 7 / duration);
		logger.info("Duration : " + duration + ", selects per second : " + selectsPerSecond);
		double minimumSelectsPerSecond = 1000;
		assertTrue(selectsPerSecond > minimumSelectsPerSecond);

		start = System.currentTimeMillis();
		for (int i = 0; i < selects; i++) {
			dataBase.find(lineParameters);
		}
		end = System.currentTimeMillis();
		duration = (end - start) / 1000d;
		selectsPerSecond = (selects * 7 / duration);
		logger.info("Only line : Duration : " + duration + ", selects per second : " + selectsPerSecond);
		assertTrue(selectsPerSecond > minimumSelectsPerSecond);
	}

}