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
	public String SERENITY_PERSISTENCE_UNIT = "SerenityPersistenceUnit";

	/** The system property key for the packages to enhance. */
	public String INCLUDED_PACKAGES_PROPERTY = "included.packages";
	/** The system property to exclude patterns from the data collection. */
	public String EXCLUDED_PACKAGES_PROPERTY = "excluded.packages";
	/** The system property key for the class adapter classes to exclude. */
	public String INCLUDED_ADAPTERS_PROPERTY = "included.adapters";

	public String NAME = "name";
	public String TYPE = "type";
	public String PARENT = "parent";
	public String DESCRIPTION = "description";
	public String CLASS_NAME = "className";
	public String PACKAGE_NAME = "packageName";
	public String METHOD_NAME = "methodName";
	public String METHOD_DESCRIPTION = "methodDescription";
	public String EFFERENT_NAME = "efferentName";
	public String AFFERENT_NAME = "afferentName";
	public String NUMBER = "number";
	public String IMPLEMENTATIONS = "implementations";
	public String ABSTRACTNESS = "abstractness";
	public String INDEX = "index";

	public String ID = "id";

}