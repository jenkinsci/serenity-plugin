package com.ikokoon.hudson;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.ikokoon.IConstants;
import com.ikokoon.instrumentation.model.Class;
import com.ikokoon.instrumentation.model.IComposite;
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
		Project project = (Project) dataBase.find(Toolkit.hash(Project.class.getName()));
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

		List<Object> parameters = new ArrayList<Object>();

		String packageName = req.getParameter("packageName");
		String className = req.getParameter("className");
		String methodName = req.getParameter("methodName");
		String methodDescription = req.getParameter("methodDescription");

		if (className != null && methodName != null && methodDescription != null) {
			parameters.clear();
			parameters.add(className);
			parameters.add(methodName);
			parameters.add(methodDescription);
			this.method = (Method) dataBase.find(parameters);
		} else if (className != null) {
			parameters.clear();
			parameters.add(className);
			this.klass = (Class) dataBase.find(parameters);
		} else if (packageName != null) {
			parameters.clear();
			parameters.add(packageName);
			this.pakkage = (Package) dataBase.find(parameters);
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

	public List<IComposite> getPackages() {
		Project project = (Project) dataBase.find(Toolkit.hash(Project.class.getName()));
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

}