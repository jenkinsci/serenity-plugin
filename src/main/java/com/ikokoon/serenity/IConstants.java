package com.ikokoon.serenity;

import java.io.File;

import org.objectweb.asm.Type;

/**
 * This is a constants class for database names and system property names.
 *
 * @author Michael Couck
 * @since 19.07.09
 * @version 01.00
 */
public interface IConstants {

	public String SERENITY = "serenity";
	public String SOURCE = "source";
	/** The Serenity directory for work and output data, i.e. the database. './serenity'. */
	public String SERENITY_DIRECTORY = "." + File.separatorChar + SERENITY;
	/** The Serenity directory for the source to HTML. */
	public String SERENITY_SOURCE = SERENITY_DIRECTORY + File.separatorChar + SOURCE;

	/** Value : 'serenity/source/' or 'serenity\source\' */
	public String SERENITY_SOURCE_DIRECTORY = SERENITY + File.separatorChar + SOURCE + File.separatorChar;

	public String SERENITY_ODB = "serenity.odb";
	public String SERENITY_RAM = "serenity.ram";
	public String SERENITY_JPA = "serenity.jpa";

	/** The database file, 'serenity.ram', 'serenity.odb', 'serenity.jpa'. */
	public String DATABASE_FILE_RAM = SERENITY_DIRECTORY + File.separatorChar + SERENITY_RAM;
	public String DATABASE_FILE_ODB = SERENITY_DIRECTORY + File.separatorChar + SERENITY_ODB;
	public String DATABASE_FILE_JPA = SERENITY_DIRECTORY + File.separatorChar + SERENITY_JPA;
	/** The JPA persistence unit name, 'SerenityPersistenceUnit'. */
	public String SERENITY_PERSISTENCE_UNIT = "SerenityPersistenceUnit";
	/** The logging configuration file, '/META-INF/log4j.properties'. */
	public String LOG_4_J_PROPERTIES = "/META-INF/log4j.properties";

	/** The system property key for the packages to enhance, 'included.packages'. */
	public String INCLUDED_PACKAGES_PROPERTY = "included.packages";
	/** The system property to exclude patterns from the data collection, 'excluded.packages'. */
	public String EXCLUDED_PACKAGES_PROPERTY = "excluded.packages";
	/** The included jars property, 'included.jars'. */
	public String INCLUDED_JARS_PROPERTY = "included.jars";
	/** The system property key for the class adapter classes to exclude, 'included.adapters'. */
	public String INCLUDED_ADAPTERS_PROPERTY = "included.adapters";
	/** The system property for the Java class path, 'java.class.path'. */
	public String JAVA_CLASS_PATH = "java.class.path";
	/** The Surefire classpath, 'surefire.test.class.path' */
	public String SUREFIRE_TEST_CLASS_PATH = "surefire.test.class.path";
	/** Whether to write the enhanced classes to the ./serenity directory for visual checking, 'write.classes'. */
	public String WRITE_CLASSES = "write.classes";
	/** Whether to delete the old class files before writing the new enhanced class files, 'clean.classes'. */
	public String CLEAN_CLASSES = "clean.classes";
	/** The interval between snapshots for the profiler. */
	public String SNAPSHOT_INTERVAL = "snapshotInterval";

	public String COVERAGE = "coverage";
	public String COMPLEXITY = "complexity";
	public String DEPENDENCY = "dependency";
	public String PROFILING = "profiling";

	/** The type of parameters that the {@link Collector} takes in the profiling collection method. */
	public Type STRING_TYPE = Type.getType(String.class);
	/** The name of the class ({@link Collector}) that will be the collector for the method adapter. */
	public String COLLECTOR_CLASS_NAME = Type.getInternalName(Collector.class);
	/** The array of type parameters for the {@link Collector} for the profiling method. */
	public Type[] PROFILING_TYPES = new Type[] { STRING_TYPE, STRING_TYPE, STRING_TYPE };

	/** The profiling methods that are called on the {@link Collector} by the added instructions. */
	public String COLLECT_ALLOCATION = "collectAllocation";
	public String COLLECT_START = "collectStart";
	public String COLLECT_END = "collectEnd";
	public String COLLECT_START_WAIT = "collectStartWait";
	public String COLLECT_END_WAIT = "collectEndWait";
	/** The byte code signature of the profiling methods in the {@link Collector}. */
	public String PROFILING_METHOD_DESCRIPTION = Type.getMethodDescriptor(Type.VOID_TYPE, PROFILING_TYPES);

	public Type[] noTypes = new Type[] {};
	public Type[] longTypes = new Type[] { Type.LONG_TYPE };
	public Type[] longIntTypes = new Type[] { Type.LONG_TYPE, Type.INT_TYPE };

	/** The sleep(long) method description in byte code. */
	public String sleepLongMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, IConstants.longTypes);
	/** The sleep(long, int) method description in byte code. */
	public String sleepLongIntMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, IConstants.longIntTypes);
	/** The yield() method description in byte code. */
	public String yieldMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, IConstants.noTypes);
	/** The wait() method description in byte code. */
	public String waitMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, IConstants.noTypes);
	/** The wait(long) method description in byte code. */
	public String waitLongMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, IConstants.longTypes);
	/** The wait(long, int) method description in byte code. */
	public String waitLongIntMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, IConstants.longIntTypes);
	/** The join() method description in byte code. */
	public String joinMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, IConstants.noTypes);
	/** The join(long) method description in byte code. */
	public String joinLongMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, IConstants.longTypes);
	/** The join(long, int) method description in byte code. */
	public String joinLongIntMethodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, IConstants.longIntTypes);

}