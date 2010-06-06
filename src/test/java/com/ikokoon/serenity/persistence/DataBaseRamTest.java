package com.ikokoon.serenity.persistence;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.toolkit.Executer;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseRamTest extends ATest {

	private IDataBase dataBase;

	@Before
	public void open() {
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM, mockInternalDataBase);
	}

	@After
	public void close() {
		dataBase.close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void insert() {
		final DataBaseRam dataBase = (DataBaseRam) this.dataBase;
		final List<Composite<?, ?>> list = getList();
		// Check the performance
		list.clear();
		double inserts = 100000;
		double insertsPerSecond = Executer.execute(new Executer.IPerform() {
			public void execute() {
				long currentTime = System.currentTimeMillis();
				Class<?, ?> klass = new Class();
				klass.setName("Name : " + currentTime);
				klass.setId(currentTime);
				dataBase.insert(list, klass);
			}
		}, "inserts of class", inserts);
		assertTrue(insertsPerSecond > 1000);
	}

	@SuppressWarnings("unchecked")
	private LinkedList<Composite<?, ?>> getList() {
		DataBaseRam dataBase = (DataBaseRam) this.dataBase;

		LinkedList<Composite<?, ?>> list = new LinkedList<Composite<?, ?>>();
		Class<?, ?> klass = new Class<Package<?, ?>, Method<?, ?>>();
		klass.setName("a");
		Long id = Toolkit.hash(klass.getName());
		klass.setId(id);
		dataBase.insert(list, klass);

		klass = new Class<Package, Method>();
		klass.setName("c");
		id = Toolkit.hash(klass.getName());
		klass.setId(id);
		dataBase.insert(list, klass);

		klass = new Class();
		klass.setName("b");
		id = Toolkit.hash(klass.getName());
		klass.setId(id);
		dataBase.insert(list, klass);

		return list;
	}

	@Test
	public void search() {
		DataBaseRam dataBase = (DataBaseRam) this.dataBase;
		LinkedList<Composite<?, ?>> list = getList();
		Long id = Toolkit.hash("b");
		Object object = dataBase.search(Class.class, list, id);
		assertNotNull(object);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void persist() {
		// T object
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
		Package pakkage = getPackage();
		dataBase.persist(pakkage);
		// 7873017250689681547, 437917821655607927
		Line line = (Line) dataBase.find(Line.class, 7873017250689681547l);
		assertNotNull(line);
	}

	@Test
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
	public void persistPerformance() throws Exception {
		// Test the insert performance
		double inserts = 10000;
		double insertsPerSecond = Executer.execute(new Executer.IPerform() {
			public void execute() {
				Package pakkage = getPackage();
				pakkage.setName(pakkage.getName() + System.currentTimeMillis());
				Class klass = (Class) pakkage.getChildren().iterator().next();
				klass.setName(klass.getName() + System.currentTimeMillis());
				dataBase.persist(pakkage);
			}
		}, "inserts of package", inserts);
		double minimumInsertsPerSecond = 1000d;
		assertTrue(insertsPerSecond > minimumInsertsPerSecond);
	}

	@Test
	@SuppressWarnings("unchecked")
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

		final List<Object> packageParameters = new ArrayList<Object>();
		packageParameters.add(packageName + "." + 13);

		final List<Object> lineParameters = new ArrayList<Object>();
		lineParameters.add(className + "." + 26);
		lineParameters.add(methodName + "." + 26);
		lineParameters.add(26d);

		final Long packageId = Toolkit.hash(packageName + "." + 233);
		final Long classId = Toolkit.hash(className + "." + 871);
		final Long methodId = Toolkit.hash(className + "." + 441 + methodName + "." + 441 + methodDescription + "." + 441);
		final Long lineId = Toolkit.hash(className + "." + 359 + methodName + "." + 359 + "" + 359d);

		double selectsPerSecond = Executer.execute(new Executer.IPerform() {
			public void execute() {

				dataBase.find(Package.class, packageId);
				dataBase.find(Class.class, classId);
				dataBase.find(Method.class, methodId);
				dataBase.find(Line.class, lineId);

				dataBase.find(Package.class, packageParameters);
				// assertNotNull(object);
				dataBase.find(Line.class, lineParameters);
				// assertNotNull(object);
				dataBase.find(Line.class, lineParameters);
				// assertNotNull(object);
			}
		}, "select different, by id and by parameters, package and line", selects) * 7;
		double minimumSelectsPerSecond = 10000;
		assertTrue(selectsPerSecond > minimumSelectsPerSecond);

		selectsPerSecond = Executer.execute(new Executer.IPerform() {
			public void execute() {
				dataBase.find(Line.class, lineParameters);
			}
		}, "select line with parameters", selects);
		assertTrue(selectsPerSecond > minimumSelectsPerSecond);
	}

}