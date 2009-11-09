package com.ikokoon.serenity.hudson;

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
import com.ikokoon.serenity.model.IComposite;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;

/**
 * This is the result that will be used to render the results on the front end.
 * 
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
public class SerenityResult implements ISerenityResult {

	private static final String TAB = "tab";
	private static final String PACKAGE_NAME = "packageName";
	private static final String CLASS_NAME = "className";
	private static final String METHOD_NAME = "methodName";
	private static final String METHOD_DESCRIPTION = "methodDescription";

	private Logger logger = Logger.getLogger(SerenityResult.class);
	/** Owner is necessary to render the sidepanel jelly */
	private AbstractBuild<?, ?> owner;
	/** The object database with the results from the coverage execution. */
	private IDataBase dataBase;
	/** The base url for Stapler. */
	private String url = "";
	/** The project for the build. */
	private Project<?, ?> project;
	/** The package that the user specified from the front end. */
	private Package<?, ?> pakkage;
	/** The class that the user specified from the front end. */
	private Class<?, ?> klass;
	/** The method that the user specified from the front end. */
	private Method<?, ?> method;
	/** The active tab in the ui. */
	private String tab;

	/**
	 * Constructor takes the real action that generated the build for the project.
	 * 
	 * @param abstractBuild
	 *            the build action that generated the build for the project
	 */
	public SerenityResult(AbstractBuild<?, ?> abstractBuild) {
		logger.info("SerenityResult:serenityResult");
		this.owner = abstractBuild;
		String dataBaseFile = owner.getRootDir().getAbsolutePath() + File.separator + IConstants.DATABASE_FILE;
		dataBase = IDataBase.DataBase.getDataBase(dataBaseFile, false);
		project = (Project<?, ?>) dataBase.find(Toolkit.hash(Project.class.getName()));
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
		logger.error("SerenityResult:getDynamic:" + token);

		List<Object> parameters = new ArrayList<Object>();

		String packageName = req.getParameter(PACKAGE_NAME);
		String className = req.getParameter(CLASS_NAME);
		String methodName = req.getParameter(METHOD_NAME);
		String methodDescription = req.getParameter(METHOD_DESCRIPTION);
		this.tab = req.getParameter(TAB);

		logger.error("Package name : " + packageName + ", class name : " + className + ", method name : " + methodName + ", method description : "
				+ methodDescription);

		if (className != null && methodName != null && methodDescription != null) {
			parameters.clear();
			parameters.add(className);
			parameters.add(methodName);
			parameters.add(methodDescription);
			this.method = (Method<?, ?>) dataBase.find(parameters);
		} else if (className != null) {
			parameters.clear();
			parameters.add(className);
			this.klass = (Class<?, ?>) dataBase.find(parameters);
		} else if (packageName != null) {
			parameters.clear();
			parameters.add(packageName);
			this.pakkage = (Package<?, ?>) dataBase.find(parameters);
		}

		logger.error("Package : " + pakkage + ", class : " + klass + ", method : " + method);
		logger.error("Url : 1 : " + url);

		url = req.getOriginalRequestURI();
		int endIndex = url.indexOf(this.getClass().getSimpleName());
		if (endIndex > -1) {
			url = url.substring(0, endIndex);
		}

		logger.error("Url : 2 : " + url);
		return this;
	}

	public String getUrl() {
		return url;
	}

	public String getTab() {
		logger.error("SerenityResult:getTab : " + tab);
		return this.tab;
	}

	public Object getOwner() {
		logger.info("SerenityResult:getOwner");
		return this.owner;
	}

	public boolean hasReport() {
		logger.info("SerenityResult:hasReport");
		return dataBase != null;
	}

	public Project<?, ?> getProject() {
		return this.project;
	}

	@SuppressWarnings("unchecked")
	public List<Package> getPackages() {
		Project project = getProject().getClass().cast(getProject());
		List<Package> packages = project.getChildren().getClass().cast(project.getChildren());
		return packages;
	}

	public Package<?, ?> getPackage() {
		logger.info("SerenityResult:getPackage");
		return pakkage;
	}

	public Class<?, ?> getKlass() {
		logger.info("SerenityResult:getKlass");
		return klass;
	}

	public Method<?, ?> getMethod() {
		logger.info("SerenityResult:getMethod");
		return method;
	}

	public String getModel(IComposite<?, ?> composite) {
		return null;
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