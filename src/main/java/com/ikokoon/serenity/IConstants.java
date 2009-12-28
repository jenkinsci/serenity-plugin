package com.ikokoon.serenity;

/**
 * This is a constants class for database names and system property names.
 * 
 * @author Michael Couck
 * @since 19.07.09
 * @version 01.00
 */
public interface IConstants {

	/** The Serenity directory for work and output data, i.e. the database.. */
	public String SERENITY_DIRECTORY = "./serenity/";
	/** The database file. */
	public String DATABASE_FILE_RAM = SERENITY_DIRECTORY + "serenity.ram";
	public String DATABASE_FILE_ODB = SERENITY_DIRECTORY + "serenity.odb";
	public String DATABASE_FILE_JPA = SERENITY_DIRECTORY + "serenity.jpa";
	/** The JPA persistence unit name. */
	public String SERENITY_PERSISTENCE_UNIT = "SerenityPersistenceUnit";
	/** The logging configuration file. */
	public String LOG_4_J_PROPERTIES = "/META-INF/log4j.properties";

	/** The system property key for the packages to enhance. */
	public String INCLUDED_PACKAGES_PROPERTY = "included.packages";
	/** The system property to exclude patterns from the data collection. */
	public String EXCLUDED_PACKAGES_PROPERTY = "excluded.packages";
	/** The system property key for the class adapter classes to exclude. */
	public String INCLUDED_ADAPTERS_PROPERTY = "included.adapters";
	/** The system property for the Java class path. */
	public String JAVA_CLASS_PATH = "java.class.path";
	/** Whether to write the enhanced classes to the ./serenity directory for visual checking. */
	public String WRITE_CLASSES = "write.classes";
	/** Whether to delete the old class files before writing the new enhanced class files. */
	public String CLEAN_CLASSES = "clean.classes";

}