package com.ikokoon.hudson;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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
import com.ikokoon.instrumentation.model.Project;
import com.ikokoon.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

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
	private AbstractBuild<?, ?> owner;
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
	public SerenityResult(AbstractBuild<?, ?> abstractBuild) {
		logger.info("SerenityResult:serenityResult");
		this.owner = abstractBuild;
		initilize();
	}

	private void initilize() {
		File file = new File(owner.getRootDir(), IConstants.DATABASE_FILE);
		dataBase = IDataBase.DataBase.getDataBase(file);

		Project project = getProject();
		StringBuilder builder = new StringBuilder();
		builder.append("Total lines : ");
		builder.append(project.getTotalLines());
		builder.append(", total methods : ");
		builder.append(project.getTotalMethods());
		builder.append(", total lines executed : ");
		builder.append(project.getTotalLinesExecuted());
		builder.append(", total methods executed : ");
		builder.append(project.getTotalMethodsExecuted());
		metrics = builder.toString();

		logger.debug("Metrics : " + metrics);
	}

	/**
	 * This method is called from the front end. The result from the call will result in some piece of data being extracted from the database. For
	 * example if the user clicks on a package the name of the package will be used to get that package from the database and will be made available
	 * to the UI.
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
		logger.error("SerenityResult:getDynamic");

		// Null everything
		this.pakkage = null;
		this.klass = null;
		this.method = null;

		Map<String, Object> parameters = new HashMap<String, Object>();

		String packageName = req.getParameter("packageName");
		String className = req.getParameter("className");
		String methodName = req.getParameter("methodName");
		String methodDescription = req.getParameter("methodDescription");

		// StringBuilder builder = new StringBuilder();
		// builder.append("Parameters : ");
		// builder.append(req.getParameterMap());
		// builder.append(", package name : ");
		// builder.append(packageName);
		// builder.append(", class name : ");
		// builder.append(className);
		// builder.append(", method name : ");
		// builder.append(methodName);
		// builder.append(", method description : ");
		// builder.append(methodDescription);
		// metrics = builder.toString();

		if (className != null && methodName != null && methodDescription != null) {
			parameters.clear();
			parameters.put(IConstants.CLASS_NAME, className);
			parameters.put(IConstants.NAME, methodName);
			parameters.put(IConstants.DESCRIPTION, methodDescription);
			this.method = dataBase.find(Method.class, parameters);
		} else if (className != null) {
			parameters.clear();
			parameters.put(IConstants.NAME, className);
			this.klass = dataBase.find(Class.class, parameters);
		} else if (packageName != null) {
			parameters.clear();
			parameters.put(IConstants.NAME, packageName);
			this.pakkage = dataBase.find(Package.class, parameters);
		}

		url = req.getOriginalRequestURI().replaceAll(token, "");
		url = url.replaceAll("///", "/");
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
		String name = Project.class.getName();
		Long id = Toolkit.hash(name);
		Project project = dataBase.find(Project.class, id);
		return project.getChildren();
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
			AbstractProject<?, ?> abstractProject = owner.getProject();
			if (abstractProject != null) {
				return abstractProject.getName();
			}
		}
		return "No name Project...";
	}

	private Project getProject() {
		Project project = new Project();

		double totalLines = 0d;
		double totalMethods = 0d;
		double totalLinesExecuted = 0d;
		double totalMethodsExecuted = 0d;

		List<Package> packages = getPackages();
		for (Package pakkage : packages) {
			for (Class klass : pakkage.getChildren()) {
				for (Method method : klass.getChildren()) {
					totalLines += method.getLines();
					totalMethods++;
					totalLinesExecuted += method.getTotalLinesExecuted();
					if (method.getTotalLinesExecuted() > 0) {
						totalMethodsExecuted++;
					}
				}
			}
		}

		project.setTimestamp(new Date());
		project.setTotalLines(totalLines);
		project.setTotalLinesExecuted(totalLinesExecuted);
		project.setTotalMethods(totalMethods);
		project.setTotalMethodsExecuted(totalMethodsExecuted);

		return project;
	}

}