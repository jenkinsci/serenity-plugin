package com.ikokoon.serenity;

import org.objectweb.asm.Type;

import java.io.File;

/**
 * This is a constants class for database names and system property names.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19.07.09
 */
public interface IConstants {

    String SERENITY = "serenity";
    String SOURCE = "source";
    /**
     * The Serenity directory for work and output data, i.e. the database. './serenity'.
     */
    String SERENITY_DIRECTORY = "." + File.separatorChar + SERENITY;
    /**
     * The Serenity directory for the source to HTML.
     */
    String SERENITY_SOURCE = SERENITY_DIRECTORY + File.separatorChar + SOURCE;

    /**
     * Value : 'serenity/source/' or 'serenity\source\'
     */
    String SERENITY_SOURCE_DIRECTORY = SERENITY + File.separatorChar + SOURCE + File.separatorChar;

    String SERENITY_ODB = "serenity.odb";
    String SERENITY_RAM = "serenity.ram";

    /**
     * The database file, 'serenity.ram', 'serenity.odb', 'serenity.jpa'.
     */
    String DATABASE_FILE_RAM = SERENITY_DIRECTORY + File.separatorChar + SERENITY_RAM;
    String DATABASE_FILE_ODB = SERENITY_DIRECTORY + File.separatorChar + SERENITY_ODB;
    /**
     * The logging configuration file, '/META-INF/log4j.properties'.
     */
    String LOG_4_J_PROPERTIES = "/META-INF/log4j.properties";
    /**
     * The style sheet location for the reports.
     */
    String REPORT_STYLE_SHEET = "/META-INF/profiler-report-style.css";

    /**
     * The system property key for the packages to enhance, 'included.packages'.
     */
    String INCLUDED_PACKAGES_PROPERTY = "included.packages";
    /**
     * The system property to exclude patterns from the data collection, 'excluded.packages'.
     */
    String EXCLUDED_PACKAGES_PROPERTY = "excluded.packages";
    /**
     * The included jars property, 'included.jars'.
     */
    String INCLUDED_JARS_PROPERTY = "included.jars";
    /**
     * The system property key for the class adapter classes to exclude, 'included.adapters'.
     */
    String INCLUDED_ADAPTERS_PROPERTY = "included.adapters";
    /**
     * The system property for the Java class path, 'java.class.path'.
     */
    String JAVA_CLASS_PATH = "java.class.path";
    /**
     * The Surefire classpath, 'surefire.test.class.path'
     */
    String SUREFIRE_TEST_CLASS_PATH = "surefire.test.class.path";
    /**
     * Whether to write the enhanced classes to the ./serenity directory for visual checking, 'write.classes'.
     */
    String WRITE_CLASSES = "write.classes";
    /**
     * Whether to delete the old class files before writing the new enhanced class files, 'clean.classes'.
     */
    String CLEAN_CLASSES = "clean.classes";
    /**
     * The interval between snapshots for the profiler.
     */
    String SNAPSHOT_INTERVAL = "snapshotInterval";
    /**
     * The interval between report dumps for the profiler.
     */
    String REPORT_INTERVAL = "reportInterval";
    /**
     * The time unit to use, default is nano seconds.
     */
    String TIME_UNIT = "timeUnit";

    String COVERAGE = "coverage";
    String COMPLEXITY = "complexity";
    String DEPENDENCY = "dependency";
    String PROFILING = "profiling";
    String DUMP = "dump";

    /**
     * The type of parameters that the {@link Collector} takes in the profiling collection method.
     */
    Type STRING_TYPE = Type.getType(String.class);
    /**
     * The name of the class ({@link Collector}) that will be the collector for the method adapter.
     */
    String COLLECTOR_CLASS_NAME = Type.getInternalName(Collector.class);

    /**
     * The profiling methods that are called on the {@link Collector} by the added instructions.
     */
    String COLLECT_ALLOCATION = "collectAllocation";
    String COLLECT_START = "collectStart";
    String COLLECT_END = "collectEnd";
    String COLLECT_START_WAIT = "collectStartWait";
    String COLLECT_END_WAIT = "collectEndWait";
    /**
     * The byte code signature of the profiling methods in the {@link Collector}.
     */
    String PROFILING_METHOD_DESCRIPTION = Type.getMethodDescriptor(Type.VOID_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE);

    /**
     * The sleep(long) method description in byte code.
     */
    String sleepLongMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE);
    /**
     * The sleep(long, int) method description in byte code.
     */
    String sleepLongIntMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.INT_TYPE);
    /**
     * The yield() method description in byte code.
     */
    String yieldMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE);
    /**
     * The wait() method description in byte code.
     */
    String waitMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE);
    /**
     * The wait(long) method description in byte code.
     */
    String waitLongMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE);
    /**
     * The wait(long, int) method description in byte code.
     */
    String waitLongIntMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.INT_TYPE);
    /**
     * The join() method description in byte code.
     */
    String joinMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE);
    /**
     * The join(long) method description in byte code.
     */
    String joinLongMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE);
    /**
     * The join(long, int) method description in byte code.
     */
    String joinLongIntMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE, Type.INT_TYPE);

    String REPORT = "report";
    String LISTENING = "listening";

    int PORT = 50005;

    String STYLE_SHEET = "profiler-report-style.css";
    String METHOD_SERIES = "greatestAverageTimePerMethod.html";
    String METHOD_CHANGE_SERIES = "methodChangeSeries.html";

    String STYLE_SHEET_FILE = SERENITY_DIRECTORY + File.separatorChar + STYLE_SHEET;
    String METHOD_SERIES_FILE = SERENITY_DIRECTORY + File.separatorChar + METHOD_SERIES;

    String CHARTS = "charts";
    File chartDirectory = new File(SERENITY_DIRECTORY + File.separatorChar + CHARTS);

    String ENCODING = "UTF8";
}