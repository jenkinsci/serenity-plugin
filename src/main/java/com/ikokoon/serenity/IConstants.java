package com.ikokoon.serenity;

/**
 * This is a constants class for database names and system property names.
 *
 * @author Michael Couck
 * @since 19.07.09
 * @version 01.00
 */
public interface IConstants {

	/** The file separator. */
	public String SEPARATOR = "/";
	/** The Serenity directory for work and output data, i.e. the database. './serenity'. */
	public String SERENITY_DIRECTORY = "." + SEPARATOR + "serenity";
	/** The Serenity directory for the source to HTML. */
	public String SERENITY_SOURCE = SERENITY_DIRECTORY + SEPARATOR + "source";
	/** The database file, 'serenity.ram', 'serenity.odb', 'serenity.jpa'. */
	public String DATABASE_FILE_RAM = SERENITY_DIRECTORY + SEPARATOR + "serenity.ram";
	public String DATABASE_FILE_ODB = SERENITY_DIRECTORY + SEPARATOR + "serenity.odb";
	public String DATABASE_FILE_JPA = SERENITY_DIRECTORY + SEPARATOR + "serenity.jpa";
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

	public String COVERAGE = "coverage";
	public String COMPLEXITY = "complexity";
	public String DEPENDENCY = "dependency";
	public String PROFILING = "profiling";

}