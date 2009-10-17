package com.ikokoon.hudson;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.Method;
import com.ikokoon.instrumentation.model.Package;
import com.ikokoon.persistence.IDataBase;

/**
 * This is the result that will be used to render the results on the front end.
 * 
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
public class SerenityResult implements ISerenityResult {

	private Logger logger = Logger.getLogger(SerenityResult.class);
	/** Owner is necessary to render the sidepanel jelly */
	private AbstractBuild owner;
	/** The object database with the results from the coverage execution. */
	private IDataBase dataBase;
	/** The base url for Stapler. */
	private String url = "";
	/** The package that the user specified from the front end. */
	private Package pakkage;
	/** The class that the user specified from the front end. */
	private Class klass;
	/** The method that the user specified from the front end. */
	private Method method;
	/** The string containing the aggregated metrics for the build. */
	private String metrics;

	/**
	 * Constructor takes the real action that generated the build for the project.
	 * 
	 * @param abstractBuild
	 *            the build action that generated the build for the project
	 */
	public SerenityResult(AbstractBuild abstractBuild) {
		logger.info("SerenityResult:serenityResult");
		this.owner = abstractBuild;
		File file = new File(abstractBuild.getRootDir(), IConstants.DATABASE_FILE);
		dataBase = IDataBase.DataBase.getDataBase(file);
		metrics = buildMetrics();
	}

	/**
	 * This method is called from the front end. The result from the call will result in some piece of data being extracted from the database. For
	 * example if the user clicks on a package the name of the package will be used to get that padkage from the database and will be made available
	 * to the ui.
	 * 
	 * @param token
	 *            the token from the front end, could be a package name or a class name
	 * @param req
	 *            the Stapler request from the ui
	 * @param rsp
	 *            the Stapler response for the ui
	 * @return the result which is this class
	 * @throws IOException
	 */
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) throws IOException {
		logger.info("SerenityResult:getDynamic");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IConstants.NAME, token);
		Package pakkage = dataBase.find(Package.class, parameters);
		if (pakkage != null) {
			this.pakkage = pakkage;
			this.klass = null;
			this.method = null;
		}
		parameters.clear();
		parameters.put(IConstants.NAME, token);
		Class klass = dataBase.find(Class.class, parameters);
		if (klass != null) {
			this.klass = klass;
			this.method = null;
		}
		if (this.klass != null) {
			parameters.clear();
			parameters.put(IConstants.CLASS_NAME, this.klass.getName());
			parameters.put(IConstants.NAME, token);
			// TODO - how to get the method? It is only unique with the description added to the query
			parameters.put(IConstants.DESCRIPTION, token);
			Method method = dataBase.find(Method.class, parameters);
			if (method != null) {
				this.method = method;
			}
		}
		url = req.getOriginalRequestURI().replaceAll(token, "");
		return this;
	}

	public String getUrl() {
		logger.info("SerenityResult:getUrl");
		return url;
	}

	public Object getOwner() {
		logger.info("SerenityResult:getOwner");
		return this.owner;
	}

	public boolean hasReport() {
		logger.info("SerenityResult:hasReport");
		return dataBase != null;
	}

	public String getMetrics() {
		logger.info("SerenityResult:metrics");
		return metrics;
	}

	public List<Package> getPackages() {
		return dataBase.find(Package.class, 0, Integer.MAX_VALUE);
	}

	public Package getPackage() {
		logger.info("SerenityResult:getPackage");
		return pakkage;
	}

	public Class getKlass() {
		logger.info("SerenityResult:getKlass");
		return klass;
	}

	public Method getMethod() {
		logger.info("SerenityResult:getMethod");
		return method;
	}

	public String getName() {
		logger.info("SerenityResult:getName");
		if (owner != null) {
			AbstractProject abstractProject = owner.getProject();
			if (abstractProject != null) {
				return abstractProject.getName();
			}
		}
		return "No name Project...";
	}

	/**
	 * Builds the aggregated data for the metrics for the project.
	 * 
	 * @return the aggregated metrics for the project
	 */
	private String buildMetrics() {
		logger.info("SerenityResult:buildMetrics");
		// Build the averages for the project metrics
		List<Package> packages = dataBase.find(Package.class, 0, Integer.MAX_VALUE);
		double coverage = 0;
		double complexity = 0;
		double interfaces = 0;
		double implementations = 0;
		double stability = 0;
		double abstractness = 0;
		for (Package pakkage : packages) {
			abstractness += pakkage.getAbstractness() > 0 ? pakkage.getAbstractness() : 0;
			complexity += pakkage.getComplexity() > 0 ? pakkage.getComplexity() : 0;
			coverage += pakkage.getCoverage() > 0 ? pakkage.getCoverage() : 0;
			implementations += pakkage.getImplementations() > 0 ? pakkage.getImplementations() : 0;
			interfaces += pakkage.getInterfaces() > 0 ? pakkage.getInterfaces() : 0;
			stability += pakkage.getStability() > 0 ? pakkage.getStability() : 0;
		}
		StringBuilder builder = new StringBuilder("Dummy metrics");
		// int precision = 2;
		// double packageSize = packages.size() > 0 ? packages.size() : 1;
		// builder.append("Coverage : ");
		// builder.append(pakkage.format((coverage / packageSize), precision));
		// builder.append(", complexity : ");
		// builder.append(pakkage.format((complexity / packageSize), precision));
		// builder.append(", interfaces : ");
		// builder.append(pakkage.format(interfaces, precision));
		// builder.append(", implementations : ");
		// builder.append(pakkage.format(implementations, precision));
		// builder.append(", stability : ");
		// builder.append(pakkage.format(stability / packageSize, precision));
		// builder.append(", abstractness : ");
		// builder.append(pakkage.format(abstractness / packageSize, precision));
		return builder.toString();
	}

}