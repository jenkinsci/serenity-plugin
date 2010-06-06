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
	public Type stringType = Type.getType(String.class);
	/** The name of the class ({@link Collector}) that will be the collector for the method adapter. */
	public String collectorClassName = Type.getInternalName(Collector.class);
	/** The array of type parameters for the {@link Collector} for the profiling method. */
	public Type[] profilingTypes = new Type[] { stringType, stringType, stringType };

	/** The profiling methods that are called on the {@link Collector} by the added instructions. */
	public String collectAllocation = "collectAllocation";
	public String collectStart = "collectStart";
	public String collectEnd = "collectEnd";
	public String collectStartWait = "collectStartWait";
	public String collectEndWait = "collectEndWait";
	/** The byte code signature of the profiling methods in the {@link Collector}. */
	public String profilingMethodDescription = Type.getMethodDescriptor(Type.VOID_TYPE, profilingTypes);

}