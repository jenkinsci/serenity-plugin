package com.ikokoon.serenity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;

import com.ikokoon.serenity.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdapter;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.toolkit.Toolkit;

/**
 * The configuration object holds the parameters for the processing, some from the system parameters that can be set by the user and some internal
 * like packages to be excluded always for example java.lang.
 * 
 * @author Michael Couck
 * @since 05.10.09
 * @version 01.00
 */
public class Configuration {

	/** The instance of the configuration. */
	private static Configuration configuration = new Configuration();

	/** The logger for the class. */
	public Logger logger;
	/** Packages that are included in the enhancement. */
	public Set<String> includedPackages = new TreeSet<String>();
	/** Patterns in class names that are excluded from enhancement. */
	public Set<String> excludedPackages = new TreeSet<String>();
	/** The class adapters that the system will chain. */
	public List<Class<ClassVisitor>> classAdapters = new ArrayList<Class<ClassVisitor>>();

	public static synchronized Configuration getConfiguration() {
		return configuration;
	}

	private Configuration() {
		LoggingConfigurator.configure();
		logger = Logger.getLogger(this.getClass());
		addIncludedPackages();
		addExcludedPackages();
		addIncludedClassAdapters();
		addDefaultExcludedPackages();
	}

	/**
	 * Checks to see that the class name is included in the packages that are to be included.
	 * 
	 * @param string
	 *            the string to check for pattern inclusion
	 * @return whether the string is included in the pattern list
	 */
	public boolean included(String string) {
		if (string == null) {
			return false;
		}
		string = Toolkit.slashToDot(string);
		for (String pattern : includedPackages) {
			// logger.info("String : " + string + ", pattern : " + pattern + ", index of : " + (string.indexOf(pattern) > -1));	
			if (string.indexOf(pattern) > -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks to see if the class is included in the classes that can be enhanced, so for example java.lang is excluded.
	 * 
	 * @param className
	 *            the name of the class to check
	 * @return whether the class is excluded and should not be used
	 */
	public boolean excluded(String string) {
		if (string == null) {
			return true;
		}
		string = Toolkit.slashToDot(string);
		for (String pattern : excludedPackages) {
			if (string.indexOf(pattern) > -1) {
				return true;
			}
		}
		return false;
	}

	public String getProperty(String name) {
		return System.getProperty(name);
	}

	public String getClassPath() {
		StringBuilder builder = new StringBuilder();
		String classpath = System.getProperty(IConstants.JAVA_CLASS_PATH);
		builder.append(classpath);

		String surefireClasspath = System.getProperty(IConstants.SUREFIRE_TEST_CLASS_PATH);
		if (surefireClasspath != null) {
			builder.append(File.pathSeparator);
			builder.append(surefireClasspath);
		}

		String includedJars = System.getProperty(IConstants.INCLUDED_JARS_PROPERTY);
		logger.info("Included jars : " + includedJars);
		if (includedJars != null) {
			builder.append(File.pathSeparator);
			builder.append(includedJars);
		}
		return builder.toString();
	}

	private void addIncludedPackages() {
		String packageNames = System.getProperty(IConstants.INCLUDED_PACKAGES_PROPERTY);
		logger.info("Package names : " + packageNames);
		if (packageNames != null) {
			StringTokenizer tokenizer = new StringTokenizer(packageNames, ";: ", false);
			while (tokenizer.hasMoreTokens()) {
				String packageName = tokenizer.nextToken();
				packageName = Toolkit.stripWhitespace(packageName);
				includedPackages.add(packageName);
				logger.info("Added package to enhance : " + packageName);
			}
		}
	}

	private void addExcludedPackages() {
		String excludedPatterns = System.getProperty(IConstants.EXCLUDED_PACKAGES_PROPERTY);
		if (excludedPatterns != null) {
			StringTokenizer tokenizer = new StringTokenizer(excludedPatterns, ";: ", false);
			while (tokenizer.hasMoreTokens()) {
				String excludedPattern = tokenizer.nextToken();
				excludedPackages.add(excludedPattern);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addIncludedClassAdapters() {
		String adapterNames = System.getProperty(IConstants.INCLUDED_ADAPTERS_PROPERTY);
		if (adapterNames != null) {
			StringTokenizer tokenizer = new StringTokenizer(adapterNames, ";: ", false);
			while (tokenizer.hasMoreTokens()) {
				String adapterName = tokenizer.nextToken();
				adapterName = Toolkit.stripWhitespace(adapterName);
				try {
					if (adapterName.equals(IConstants.COVERAGE)) {
						classAdapters.add((Class<ClassVisitor>) Class.forName(CoverageClassAdapter.class.getName()));
					}
					if (adapterName.equals(IConstants.COMPLEXITY)) {
						classAdapters.add((Class<ClassVisitor>) Class.forName(ComplexityClassAdapter.class.getName()));
					}
					if (adapterName.equals(IConstants.DEPENDENCY)) {
						classAdapters.add((Class<ClassVisitor>) Class.forName(DependencyClassAdapter.class.getName()));
					}
					if (adapterName.equals(IConstants.PROFILING)) {
						classAdapters.add((Class<ClassVisitor>) Class.forName(ProfilingClassAdapter.class.getName()));
					}
				} catch (ClassNotFoundException e) {
					logger.error("Class : " + adapterName + " not found", e);
				}
			}
		}
	}

	private void addDefaultExcludedPackages() {
		excludedPackages.add("java.lang");
		excludedPackages.add("sun");
		excludedPackages.add("sunw");
		excludedPackages.add("com.sun");
		excludedPackages.add("Test");
		excludedPackages.add(Project.class.getPackage().getName());
	}

}
