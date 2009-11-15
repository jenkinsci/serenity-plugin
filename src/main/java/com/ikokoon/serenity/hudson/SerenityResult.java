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
import com.ikokoon.serenity.hudson.modeller.IModeller;
import com.ikokoon.serenity.hudson.modeller.Modeller;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.IComposite;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
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
	/** The model for the currently selected composite. */
	private String model;
	/** The project model string in base 64. */
	private String projectModel;

	/**
	 * Constructor takes the real action that generated the build for the project.
	 * 
	 * @param abstractBuild
	 *            the build action that generated the build for the project
	 */
	public SerenityResult(AbstractBuild<?, ?> abstractBuild) {
		logger.debug("SerenityResult");
		this.owner = abstractBuild;
		String dataBaseFile = owner.getRootDir().getAbsolutePath() + File.separator + IConstants.DATABASE_FILE;
		logger.debug("Opening database on file : " + dataBaseFile);
		dataBase = IDataBase.DataBaseManager.getDataBase(dataBaseFile, false);
		project = (Project<?, ?>) dataBase.find(Toolkit.hash(Project.class.getName()));
		this.projectModel = getModel(project);
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
		logger.error("getDynamic:" + token);

		List<Object> parameters = new ArrayList<Object>();

		String packageName = req.getParameter(PACKAGE_NAME);
		String className = req.getParameter(CLASS_NAME);
		String methodName = req.getParameter(METHOD_NAME);
		String methodDescription = req.getParameter(METHOD_DESCRIPTION);

		logger.info("Parameter map : " + req.getParameterMap());

		logger.debug("Package name : " + packageName + ", class name : " + className + ", method name : " + methodName + ", method description : "
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
			this.model = getModel(this.klass);
		} else if (packageName != null) {
			parameters.clear();
			parameters.add(packageName);
			this.pakkage = (Package<?, ?>) dataBase.find(parameters);
			this.model = getModel(this.pakkage);
		}

		logger.debug("Package : " + pakkage + ", class : " + klass + ", method : " + method);

		url = req.getOriginalRequestURI();
		int endIndex = url.indexOf(this.getClass().getSimpleName());
		if (endIndex > -1) {
			url = url.substring(0, endIndex);
		}

		logger.debug("Url : 2 : " + url);
		return this;
	}

	public String getUrl() {
		return url;
	}

	public Object getOwner() {
		logger.debug("getOwner");
		return this.owner;
	}

	public boolean hasReport() {
		logger.debug("hasReport");
		return project != null;
	}

	public Project<?, ?> getProject() {
		return this.project;
	}

	@SuppressWarnings("unchecked")
	public List<Package> getPackages() {
		Project project = this.project.getClass().cast(this.project);
		List<Package> packages = project.getChildren().getClass().cast(project.getChildren());
		return packages;
	}

	public Package<?, ?> getPackage() {
		logger.debug("getPackage");
		return pakkage;
	}

	public Class<?, ?> getKlass() {
		logger.debug("getKlass");
		return klass;
	}

	public Method<?, ?> getMethod() {
		logger.debug("getMethod");
		return method;
	}

	public String getProjectModel() {
		return this.projectModel;
	}

	public String getModel() {
		return this.model;
	}

	public String getModel(String parameter) {
		logger.info("getModel(parameter):" + parameter);
		return this.model;
	}

	public String getModel(IComposite<?, ?> composite) {
		logger.debug("Composite : " + composite);
		ArrayList<IComposite<?, ?>> composites = new ArrayList<IComposite<?, ?>>();
		composites.add(composite);
		Object[] uniqueValues = Toolkit.getUniqueValues(composite);
		Long id = Toolkit.hash(uniqueValues);
		composites = getPreviousComposites(owner, id, composites, 1);
		IModeller modeller = new Modeller();
		modeller.visit(composite.getClass(), composites.toArray(new IComposite[composites.size()]));
		return modeller.getModel();
	}

	private ArrayList<IComposite<?, ?>> getPreviousComposites(AbstractBuild<?, ?> abstractBuild, Long id, ArrayList<IComposite<?, ?>> composites,
			int history) {
		if (history >= HISTORY) {
			return composites;
		}
		logger.debug("Abstract build : " + abstractBuild);
		AbstractBuild<?, ?> previousBuild = abstractBuild.getPreviousBuild();
		if (previousBuild == null) {
			return composites;
		}
		String dataBaseFile = previousBuild.getRootDir().getAbsolutePath() + File.separator + IConstants.DATABASE_FILE;
		logger.debug("Previous database file : " + dataBaseFile);
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(dataBaseFile, false);
		IComposite<?, ?> composite = dataBase.find(id);
		logger.debug("Looking for composite : " + id + ", " + composite);
		composites.add(composite);
		return getPreviousComposites(previousBuild, id, composites, ++history);
	}

	public String getName() {
		logger.debug("getName");
		if (owner != null) {
			AbstractProject<?, ?> abstractProject = owner.getProject();
			if (abstractProject != null) {
				return abstractProject.getName();
			}
		}
		return "No name Project...";
	}

}