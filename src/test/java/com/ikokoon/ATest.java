package com.ikokoon;

import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;

import com.ikokoon.instrumentation.Configuration;
import com.ikokoon.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.instrumentation.coverage.CoverageClassAdapter;
import com.ikokoon.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.instrumentation.model.Afferent;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.Efferent;
import com.ikokoon.instrumentation.model.IComposite;
import com.ikokoon.instrumentation.model.Line;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.instrumentation.model.Project;
import com.ikokoon.persistence.ADataBase;
import com.ikokoon.persistence.IDataBase;
import com.ikokoon.target.Target;
import com.ikokoon.target.one.One;
import com.ikokoon.toolkit.Toolkit;

/**
 * Base class for the tests.
 * 
 * @author Michael Couck
 * @since 30.07.09
 * @version 01.00
 */
public abstract class ATest implements IConstants {

	protected static Logger logger;

	protected ADataBase dataBase;

	protected String packageName = One.class.getPackage().getName();
	protected String className = One.class.getName();
	protected String methodName = "complexMethod";
	protected String methodDescription = "methodDescription";
	protected double lineNumber = 70;
	protected double complexity = 10d;
	protected int access = 1537;

	protected String efferentName = "efferentName";
	protected String afferentName = "afferentName";

	@BeforeClass
	public static void setup() {
		URL url = ATest.class.getResource(LOG_4_J_PROPERTIES);
		if (url != null) {
			PropertyConfigurator.configure(url);
		}
		logger = Logger.getLogger(ATest.class);
		logger.info("Loaded logging properties from : " + url);
		StringBuilder builder = new StringBuilder(CoverageClassAdapter.class.getName());
		builder.append(";");
		builder.append(DependencyClassAdapter.class.getName());
		builder.append(";");
		builder.append(ComplexityClassAdapter.class.getName());
		System.setProperty(IConstants.INCLUDED_ADAPTERS_PROPERTY, builder.toString());
		Configuration.getConfiguration().includedPackages.add(Target.class.getPackage().getName());
	}

	@Before
	public void initilize() {
		// OdbConfiguration.setDebugEnabled(true);
		// OdbConfiguration.setAutomaticCloseFileOnExit(true);
		// OdbConfiguration.setDisplayWarnings(true);
		dataBase = (ADataBase) IDataBase.DataBase.getDataBase();
		Project project = (Project) dataBase.find(Toolkit.hash(Project.class.getName()));
		if (project != null) {
			project.getChildren().clear();
			project.getIndex().clear();
			project.getIndex().add(project);
		}
	}

	protected Package getPackage() {
		Package pakkage = new Package();
		pakkage.setAbstractness(1d);
		pakkage.setAfferent(1d);
		pakkage.setChildren(new ArrayList<IComposite>());
		pakkage.setComplexity(1d);
		pakkage.setCoverage(1d);
		pakkage.setDistance(1d);
		pakkage.setEfferent(1d);
		pakkage.setImplementations(1d);
		pakkage.setInterfaces(1d);
		pakkage.setName(packageName);
		pakkage.setStability(1d);
		getClass(pakkage);
		return pakkage;
	}

	protected Class getClass(Package pakkage) {
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

	protected Method getMethod(Class klass) {
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

	protected Line getLine(Method method) {
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
