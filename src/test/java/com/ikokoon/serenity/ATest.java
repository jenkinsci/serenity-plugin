package com.ikokoon.serenity;

import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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

	protected IDataBase dataBase;

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
		initLog4j();
		StringBuilder builder = new StringBuilder(CoverageClassAdapter.class.getName());
		builder.append(";");
		builder.append(DependencyClassAdapter.class.getName());
		builder.append(";");
		builder.append(ComplexityClassAdapter.class.getName());
		System.setProperty(IConstants.INCLUDED_ADAPTERS_PROPERTY, builder.toString());
		Configuration.getConfiguration().includedPackages.add(Target.class.getPackage().getName());
	}

	private static void initLog4j() {
		URL url = ATest.class.getResource(LOG_4_J_PROPERTIES);
		if (url != null) {
			PropertyConfigurator.configure(url);
		} else {
			Properties properties = getProperties();
			PropertyConfigurator.configure(properties);
		}
		logger = Logger.getLogger(ATest.class);
		logger.info("Loaded logging properties from : " + url);
	}

	private static Properties getProperties() {
		Properties properties = new Properties();
		// Root Logger
		properties.put("log4j.rootLogger", "INFO, ikokoon, file");
		properties.put("log4j.rootCategory", "INFO, ikokoon");

		// Serenity application logging file output
		properties.put("log4j.appender.file", "org.apache.log4j.DailyRollingFileAppender");
		properties.put("log4j.appender.file.Threshold", "DEBUG");
		properties.put("log4j.appender.file.File", "./serenity/serenity.log");
		properties.put("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
		properties.put("log4j.appender.file.layout.ConversionPattern", "%d{HH:mm:ss,SSS} %-5p %C:%L - %m%n");
		properties.put("log4j.appender.file.Append", "false");

		// Serenity application logging console output
		properties.put("log4j.appender.ikokoon", "org.apache.log4j.ConsoleAppender");
		properties.put("log4j.appender.ikokoon.Threshold", "DEBUG");
		properties.put("log4j.appender.ikokoon.ImmediateFlush", "true");
		properties.put("log4j.appender.ikokoon.layout", "org.apache.log4j.PatternLayout");
		properties.put("log4j.appender.ikokoon.layout.ConversionPattern", "%d{HH:mm:ss,SSS} %-5p %C:%L - %m%n");

		// Set the Serenity categories and thresholds
		properties.put("log4j.category.net", "WARN");
		properties.put("log4j.category.com", "WARN");
		properties.put("log4j.category.org", "WARN");

		// Specific thresholds
		properties.put("log4j.category.com.ikokoon", "DEBUG");
		properties.put("log4j.category.com.ikokoon.toolkit", "DEBUG");
		properties.put("log4j.category.com.ikokoon.persistence", "DEBUG");
		properties.put("log4j.category.com.ikokoon.instrumentation.process", "DEBUG");
		properties.put("log4j.category.com.ikokoon.instrumentation.coverage", "DEBUG");
		properties.put("log4j.category.com.ikokoon.instrumentation.complexity", "DEBUG");
		properties.put("log4j.category.com.ikokoon.instrumentation.dependency", "DEBUG");
		properties.put("log4j.category.com.ikokoon.instrumentation.profiling", "DEBUG	");
		return properties;
	}

	@Before
	public void initilize() {
		// OdbConfiguration.setDebugEnabled(true);
		// OdbConfiguration.setAutomaticCloseFileOnExit(true);
		// OdbConfiguration.setDisplayWarnings(true);
		dataBase = IDataBase.DataBaseManager.getDataBase(IConstants.DATABASE_FILE, true);
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
