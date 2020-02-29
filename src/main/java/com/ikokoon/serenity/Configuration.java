package com.ikokoon.serenity;

import com.ikokoon.serenity.instrumentation.complexity.ComplexityClassAdapter;
import com.ikokoon.serenity.instrumentation.coverage.CoverageClassAdapter;
import com.ikokoon.serenity.instrumentation.dependency.DependencyClassAdapter;
import com.ikokoon.serenity.instrumentation.profiling.ProfilingClassAdviceAdapter;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;
import org.objectweb.asm.ClassVisitor;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * The configuration object holds the parameters for the processing, some from the system parameters that
 * can be set by the user and some internal like packages to be excluded always for example java.lang.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05.10.09
 */
public class Configuration {

    /**
     * The instance of the configuration.
     */
    private static Configuration configuration = new Configuration();
    /**
     * The separator for the package names.
     */
    private static final String SEPARATOR = ";:|, ";

    /**
     * The LOGGER for the class.
     */
    public Logger logger;
    /**
     * Packages that are included in the enhancement.
     */
    public Set<String> includedPackages = new TreeSet<>();
    /**
     * Patterns in class names that are excluded from enhancement.
     */
    public Set<String> excludedPackages = new TreeSet<>();
    /**
     * The class adapters that the system will chain.
     */
    public List<Class<ClassVisitor>> classAdapters = new ArrayList<>();

    /**
     * System wide access to the configuration.
     *
     * @return the configuration for the system
     */
    public static synchronized Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Private access insures singularity.
     */
    private Configuration() {
        LoggingConfigurator.configure();
        logger = Logger.getLogger(this.getClass().getName());
        addIncludedPackages();
        addExcludedPackages();
        addIncludedClassAdapters();
        addDefaultExcludedPackages();
    }

    /**
     * Checks to see that the class name is included in the packages that are to be included.
     *
     * @param string the string to check for pattern inclusion
     * @return whether the string is included in the pattern list
     */
    public boolean included(final String string) {
        if (string == null) {
            return false;
        }
        String strippedString = Toolkit.slashToDot(string);
        for (final String pattern : includedPackages) {
            if (strippedString.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the class is included in the classes that can be enhanced, so for example java.lang is excluded.
     *
     * @param string the string that is to be checked for exclusion
     * @return whether the class is excluded and should not be used
     */
    public boolean excluded(final String string) {
        if (string == null) {
            return true;
        }
        String strippedString = Toolkit.slashToDot(string);
        for (final String pattern : excludedPackages) {
            if (strippedString.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Access to the system properties. This method can be extended to include other properties like in files etc.
     *
     * @param name the name of the property
     * @return the system property with the specified name
     */
    public String getProperty(final String name) {
        return System.getProperty(name);
    }

    /**
     * Access to the classpath of the system. Included in the classpath are the jars that were manually added by the user.
     *
     * @return the classpath of the system including the Surefire classpath
     */
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
        if (includedJars != null) {
            StringTokenizer tokenizer = new StringTokenizer(includedJars, SEPARATOR, false);
            while (tokenizer.hasMoreTokens()) {
                String jarFile = tokenizer.nextToken();
                builder.append(File.pathSeparator);
                builder.append(jarFile);
            }
        }
        return builder.toString();
    }

    public long getSnapshotInterval() {
        String snapshotInterval = System.getProperty(IConstants.SNAPSHOT_INTERVAL);
        if (snapshotInterval != null && Toolkit.isDigits(snapshotInterval)) {
            return Long.parseLong(snapshotInterval);
        }
        return -1;
    }

    public long getReportInterval() {
        String reportInterval = System.getProperty(IConstants.REPORT_INTERVAL);
        if (reportInterval != null && Toolkit.isDigits(reportInterval)) {
            return Long.parseLong(reportInterval);
        }
        return -1;
    }

    public double getTimeUnitDenominator() {
        String timeUnit = System.getProperty(IConstants.TIME_UNIT);
        if (timeUnit != null && Toolkit.isDigits(timeUnit)) {
            return Long.parseLong(timeUnit);
        }
        return 1000;
    }

    private void addIncludedPackages() {
        String packageNames = System.getProperty(IConstants.INCLUDED_PACKAGES_PROPERTY);
        if (packageNames != null) {
            StringTokenizer tokenizer = new StringTokenizer(packageNames, SEPARATOR, false);
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
            StringTokenizer tokenizer = new StringTokenizer(excludedPatterns, SEPARATOR, false);
            while (tokenizer.hasMoreTokens()) {
                String excludedPattern = tokenizer.nextToken();
                excludedPackages.add(excludedPattern);
                logger.info("Excluded pattern : " + excludedPattern);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addIncludedClassAdapters() {
        String adapterNames = System.getProperty(IConstants.INCLUDED_ADAPTERS_PROPERTY);
        if (adapterNames != null) {
            StringTokenizer tokenizer = new StringTokenizer(adapterNames, SEPARATOR, false);
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
                        classAdapters.add((Class<ClassVisitor>) Class.forName(ProfilingClassAdviceAdapter.class.getName()));
                    }
                } catch (ClassNotFoundException e) {
                    logger.severe("Class : " + adapterName + " not found" + e);
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
