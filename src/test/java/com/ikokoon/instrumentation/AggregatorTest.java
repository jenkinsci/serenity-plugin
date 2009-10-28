package com.ikokoon.instrumentation;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import com.ikokoon.ATest;
import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.model.Afferent;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.Efferent;
import com.ikokoon.instrumentation.model.Line;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.instrumentation.model.Project;
import com.ikokoon.instrumentation.process.Aggregator;
import com.ikokoon.target.Target;
import com.ikokoon.target.one.One;
import com.ikokoon.toolkit.Toolkit;

/**
 * This is the test for the aggregator. The aggregator takes the collected data on the methods, classes and packages and calculates the metrics like
 * the abstractness the stability and so on.
 * 
 * @author Michael Couck
 * @since 02.08.09
 * @version 01.00
 */
public class AggregatorTest extends ATest implements IConstants {

	static {
		Configuration.getConfiguration().includedPackages.add(Target.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add(One.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add("edu.umd.cs.findbugs");
	}

	@Test
	public void onePackageClassMethodAndLine() {
		Project project = (Project) dataBase.find(Toolkit.hash(Project.class.getName()));
		Package pakkage = getPackage();
		Class klass = getClass(pakkage, Target.class.getName());
		Method method = getMethod(klass, "method name one", "method description one", 10, 10);
		Line line = getline(method, 1, 5);

		project.getChildren().add(pakkage);

		dataBase.persist(pakkage);

		Aggregator aggregator = new Aggregator(null, dataBase);
		aggregator.execute();

		printModel(pakkage);

		assertEquals(10, pakkage.getLines());
		assertEquals(10, pakkage.getComplexity());
		assertEquals(10, pakkage.getCoverage());
		assertEquals(0, pakkage.getAbstractness());
		assertEquals(0.5, pakkage.getStability());
		assertEquals(0.582910995399281, pakkage.getDistance());
		assertEquals(0, pakkage.getInterfaces());
		assertEquals(1, pakkage.getImplementations());
		assertEquals(1, pakkage.getEfferent());
		assertEquals(1, pakkage.getAfferent());
		assertEquals(1, pakkage.getChildren().size());

		assertEquals(10, klass.getLines());
		assertEquals(10, klass.getComplexity());
		assertEquals(10, klass.getCoverage());
		assertEquals(0.5, klass.getStability());
		assertEquals(1, klass.getEfferent());
		assertEquals(1, klass.getAfferent());
		assertEquals(false, klass.getInterfaze());
		assertEquals(1, klass.getChildren().size());

		assertEquals(10, method.getComplexity());
		assertEquals(10, method.getLines());
		assertEquals(5, method.getTotalLinesExecuted());
		assertEquals(10, method.getCoverage());
		assertEquals(1, method.getChildren().size());

		assertEquals(1, line.getNumber());
		assertEquals(5, line.getCounter());
	}

	@Test
	public void onePackageTwoClassesTwoMethodsAndTwoLines() {
		Project project = (Project) dataBase.find(Toolkit.hash(Project.class.getName()));
		Efferent efferent = new Efferent();
		efferent.setName(Logger.class.getPackage().getName());

		Package pakkage = getPackage();

		project.getChildren().add(pakkage);

		Class classTarget = getClass(pakkage, Target.class.getName());
		classTarget.setInterfaze(true);

		classTarget.getEfferentPackages().add(efferent);

		Method methodOne = getMethod(classTarget, "method name one", "method description one", 10, 10);
		getline(methodOne, 1, 5);
		getline(methodOne, 2, 10);

		Method methodTwo = getMethod(classTarget, "method name two", "method description two", 20, 5);
		getline(methodTwo, 1, 15);
		getline(methodTwo, 2, 20);

		Class classOne = getClass(pakkage, One.class.getName());
		Method methodThree = getMethod(classOne, "method name three", "method description three", 15, 30);
		getline(methodThree, 1, 25);
		getline(methodThree, 2, 30);

		Method methodFour = getMethod(classOne, "method name four", "method description four", 40, 20);
		getline(methodFour, 1, 35);
		getline(methodFour, 2, 40);

		dataBase.persist(pakkage);

		Aggregator aggregator = new Aggregator(null, dataBase);
		aggregator.execute();

		printModel(pakkage);

		assertEquals(65, pakkage.getLines());
		// Sigma : (class lines / package lines) * class complexity
		// ((15 / 65) * 13.333333333333332) + ((50 / 65) * 25) = 3.07692 + 19.2307 = 22.30692307692308
		assertEquals(22.30692307692308, pakkage.getComplexity());
		// ((15 / 65) * 26.666666666666664) + ((50 / 65) * 7.996) = 6.1538 + 6.5107 = 12.298461538461538
		assertEquals(12.298461538461538, pakkage.getCoverage());
		// i / (i + im) = 1 / 2 = 0.5
		assertEquals(0.5, pakkage.getAbstractness());
		// e / (e + a) = 3 / 5 = 0.6666666666666666
		// assertEquals(0.6666666666666666, pakkage.getStability());
		// d=|-stability + -abstractness + 1|/sqrt(-1²+-1²) = |-0.6666666666666666 + -0.5 + 1|sqrt(-1sq + -1sq) =
		// assertEquals(0.38860733026618743, pakkage.getDistance());
		assertEquals(1, pakkage.getInterfaces());
		assertEquals(1, pakkage.getImplementations());
		assertEquals(2, pakkage.getEfferent());
		assertEquals(1, pakkage.getAfferent());
		assertEquals(2, pakkage.getChildren().size());

		// Check the first class, the Target class
		assertEquals(15, classTarget.getLines()); // 10 + 15
		// Sigma n=1, n, (method lines / class lines) * method complexity
		// ((10 / 15) * 10) + ((5 / 15) * 20) = 6.666r + 6.6666r = 13.3333r
		assertEquals(13.333333333333332, classTarget.getComplexity());
		// ((10 / 15) * 20) + ((5 / 15) * 40) = 13.33r + 13.333r =
		assertEquals(26.666666666666664, classTarget.getCoverage());
		// e / e + a = 2 / 2 + 1 = 0.666r
		assertEquals(0.6666666666666666, classTarget.getStability());
		assertEquals(2, classTarget.getEfferent());
		assertEquals(1, classTarget.getAfferent());
		assertEquals(true, classTarget.getInterfaze());
		assertEquals(2, classTarget.getChildren().size());

		assertEquals(6.666666666666667, methodThree.getCoverage());
		assertEquals(55, methodThree.getTotalLinesExecuted());
		assertEquals(2, methodThree.getChildren().size());

		assertEquals(10, methodFour.getCoverage());
		assertEquals(75, methodFour.getTotalLinesExecuted());
		assertEquals(2, methodFour.getChildren().size());

		// Check the second class, the One class
		assertEquals(50, classOne.getLines()); // 30 + 20
		// Sigma n=1, n, (method lines / class lines) * method complexity
		// ((30 / 50) * 15) + ((20 / 50) * 40) = 9 + 16 = 25
		assertEquals(25, classOne.getComplexity());
		// ((30 / 50) * 6.666666666666667) + ((20 / 50) * 10.0) = 4.n2 + 4 = +-8
		assertEquals(7.996, classOne.getCoverage());
		// e / e + a = 1 / 1 + 1 = 0.5
		assertEquals(0.5, classOne.getStability());
		assertEquals(1, classOne.getEfferent());
		assertEquals(1, classOne.getAfferent());
		assertEquals(false, classOne.getInterfaze());
		assertEquals(2, classOne.getChildren().size());

		assertEquals(6.666666666666667, methodThree.getCoverage());
		assertEquals(55, methodThree.getTotalLinesExecuted());
		assertEquals(2, methodThree.getChildren().size());

		assertEquals(10, methodFour.getCoverage());
		assertEquals(75, methodFour.getTotalLinesExecuted());
		assertEquals(2, methodFour.getChildren().size());
	}

	private void printModel(Package pakkage) {
		logger.info("PRINTING MODEL");
		printEntity(pakkage);
		logger.info("Classes : " + pakkage.getChildren().size());
		for (Class klass : ((List<Class>) pakkage.getChildren())) {
			logger.info("");
			printEntity(klass);
			logger.info("Methods : " + klass.getChildren().size());
			for (Method method : ((List<Method>) klass.getChildren())) {
				logger.info("");
				printEntity(method);
				logger.info("Lines : " + method.getChildren().size());
				for (Line line : ((List<Line>) method.getChildren())) {
					logger.info("");
					printEntity(line);
				}
			}
		}
	}

	private void printEntity(Object object) {
		logger.info("Printing object : " + object + ", class : " + object.getClass());
		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			Object value = Toolkit.getValue(object, field.getName());
			logger.info(field.getName() + " : " + value);
		}
	}

	protected Package getPackage() {
		Package pakkage = new Package();
		pakkage.setName(Target.class.getPackage().getName());
		return pakkage;
	}

	protected Class getClass(Package pakkage, String className) {
		Afferent afferent = new Afferent();
		afferent.setName(org.apache.log4j.Logger.class.getPackage().getName());
		Efferent efferent = new Efferent();
		efferent.setName(JUnitCore.class.getPackage().getName());

		Class klass = new Class();

		klass.setName(className);
		klass.getAfferentPackages().add(afferent);
		klass.getEfferentPackages().add(efferent);
		klass.setInterfaze(false);
		klass.setParent(pakkage);

		pakkage.getChildren().add(klass);

		return klass;
	}

	protected Method getMethod(Class klass, String name, String description, double complexity, double lines) {
		Method method = new Method();
		method.setName(name);
		method.setClassName(klass.getName());
		method.setComplexity(complexity);
		method.setLines(lines);
		method.setDescription(description);
		method.setParent(klass);
		klass.getChildren().add(method);
		return method;
	}

	protected Line getline(Method method, double number, double counter) {
		Line line = new Line();
		line.setClassName(method.getClassName());
		line.setMethodName(method.getName());
		line.setNumber(number);
		line.setCounter(counter);
		line.setParent(method);
		method.getChildren().add(line);
		return line;
	}

}