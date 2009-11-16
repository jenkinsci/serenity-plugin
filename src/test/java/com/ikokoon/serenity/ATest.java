package com.ikokoon.serenity;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;

import com.ikokoon.IConstants;
import com.ikokoon.serenity.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.serenity.model.Afferent;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Efferent;
import com.ikokoon.serenity.model.Line;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.target.Target;

/**
 * Base class for the tests.
 * 
 * @author Michael Couck
 * @since 30.07.09
 * @version 01.00
 */
public abstract class ATest implements IConstants {

	protected static Logger logger;

	protected IDataBase dataBase;

	protected String packageName = Target.class.getPackage().getName();
	protected String className = Target.class.getName();
	protected String methodName = "complexMethod";
	protected String methodDescription = "methodDescription";
	protected double lineNumber = 70;
	protected double complexity = 10d;
	protected int access = 1537;

	protected String efferentName = "efferentName";
	protected String afferentName = "afferentName";

	@BeforeClass
	public static void setup() {
		LoggingConfigurator.configure();
		logger = Logger.getLogger(ATest.class);
		StringBuilder builder = new StringBuilder(CoverageClassAdapter.class.getName());
		builder.append(";");
		builder.append(DependencyClassAdapter.class.getName());
		builder.append(";");
		builder.append(ComplexityClassAdapter.class.getName());
		System.setProperty(IConstants.INCLUDED_ADAPTERS_PROPERTY, builder.toString());
		System.setProperty(IConstants.INCLUDED_PACKAGES_PROPERTY, Target.class.getPackage().getName());
		Configuration.getConfiguration().includedPackages.add(Transformer.class.getPackage().getName());
		// Configuration.getConfiguration().excludedPackages.add(Target.class.getPackage().getName());
		Configuration.getConfiguration().excludedPackages.add(Project.class.getPackage().getName());
	}

	@Before
	public void initilize() {
		// OdbConfiguration.setDebugEnabled(true);
		// OdbConfiguration.setAutomaticCloseFileOnExit(true);
		// OdbConfiguration.setDisplayWarnings(true);
		if (dataBase == null) {
			dataBase = IDataBase.DataBaseManager.getDataBase(IConstants.DATABASE_FILE, false);
		}
		// Project<?, ?> project = (Project<?, ?>) dataBase.find(Toolkit.hash(Project.class.getName()));
		// project.getChildren().clear();
		// project.getIndex().clear();
		// project.getIndex().add(project);
	}

	@SuppressWarnings("unchecked")
	protected Package<?, ?> getPackage() {
		Package pakkage = new Package();
		pakkage.setAbstractness(1d);
		pakkage.setAfferent(1d);
		pakkage.setChildren(new ArrayList<Class>());
		pakkage.setComplexity(1d);
		pakkage.setCoverage(1d);
		pakkage.setDistance(1d);
		pakkage.setEfferent(1d);
		pakkage.setImplement(1d);
		pakkage.setInterfaces(1d);
		pakkage.setName(packageName);
		pakkage.setStability(1d);
		getClass(pakkage);
		return pakkage;
	}

	@SuppressWarnings("unchecked")
	protected Class<?, ?> getClass(Package<?, ?> pakkage) {
		Class klass = new Class();
		klass.setParent(pakkage);
		pakkage.getChildren().add(klass);
		klass.setAfferent(1d);

		klass.setComplexity(1d);
		klass.setCoverage(1d);
		klass.setEfferent(1d);

		Efferent efferent = new Efferent();
		efferent.setName(efferentName);
		klass.getEfferentPackages().add(efferent);

		Afferent afferent = new Afferent();
		afferent.setName(afferentName);
		klass.getAfferentPackages().add(afferent);

		klass.setInterfaze(true);
		klass.setName(className);
		klass.setStability(1d);
		getMethod(klass);
		return klass;
	}

	@SuppressWarnings("unchecked")
	protected Method<?, ?> getMethod(Class<?, ?> klass) {
		Method method = new Method();
		method.setParent(klass);
		method.setClassName(klass.getName());
		klass.getChildren().add(method);
		method.setComplexity(1d);
		method.setCoverage(1d);
		method.setDescription(methodDescription);
		method.setLines(1d);
		method.setName(methodName);
		getLine(method);
		return method;
	}

	@SuppressWarnings("unchecked")
	protected Line<?, ?> getLine(Method<?, ?> method) {
		Line line = new Line();
		line.setCounter(1d);
		line.setNumber(lineNumber);
		line.setParent(method);
		line.setMethodName(method.getName());
		line.setClassName(method.getClassName());
		method.getChildren().add(line);
		return line;
	}

}
