package com.ikokoon;

/**
 * This class contains mainly the tags for the xml that is generated for the metrics.
 * 
 * @author Michael Couck
 * @since 19.07.09
 * @version 01.00
 */
public interface IConstants {

	public String DATABASE_FILE = "./serenity/serenity.db";
	public String LOG_4_J_PROPERTIES = "/META-INF/log4j.properties";

	/** The system property key for the packages to enhance. */
	public String INCLUDED_PACKAGES_PROPERTY = "included.packages";
	/** The system property to exclude patterns from the data collection. */
	public String EXCLUDED_PACKAGES_PROPERTY = "excluded.packages";
	/** The system property key for the class adapter classes to exclude. */
	public String INCLUDED_ADAPTERS_PROPERTY = "included.adapters";


}