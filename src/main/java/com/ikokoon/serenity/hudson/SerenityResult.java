package com.ikokoon.serenity.hudson;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.hudson.modeller.IModeller;
import com.ikokoon.serenity.hudson.modeller.Modeller;
import com.ikokoon.serenity.hudson.source.CoverageSourceCode;
import com.ikokoon.serenity.hudson.source.ISourceCode;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.model.Project;
import com.ikokoon.serenity.persistence.DataBaseOdb;
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
	/** The package that the user specified from the front end. */
	private Long pakkageId;
	/** The class that the user specified from the front end. */
	private Long klassId;
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
		String dataBaseFile = owner.getRootDir().getAbsolutePath() + File.separator + IConstants.DATABASE_FILE_ODB;
		logger.debug("Opening database on file : " + dataBaseFile);
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, false, null);
		Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		logger.debug("Project : " + project);
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
		logger.debug("getDynamic:" + token);

		List<Object> parameters = new ArrayList<Object>();

		String packageName = req.getParameter(PACKAGE_NAME);
		String className = req.getParameter(CLASS_NAME);
		String methodName = req.getParameter(METHOD_NAME);
		String methodDescription = req.getParameter(METHOD_DESCRIPTION);

		logger.debug("Parameter map : " + req.getParameterMap());
		logger.debug("Package name : " + packageName + ", class name : " + className + ", method name : " + methodName + ", method description : "
				+ methodDescription);

		if (className != null) {
			parameters.clear();
			parameters.add(className);
			Class<?, ?> klass = (Class<?, ?>) dataBase.find(Class.class, parameters);
			if (klass != null) {
				this.klassId = klass.getId();
				this.model = getModel(klass);
				Collections.sort(klass.getChildren(), new Comparator<Method<?, ?>>() {
					public int compare(Method<?, ?> o1, Method<?, ?> o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
			}
		} else if (packageName != null) {
			parameters.clear();
			parameters.add(packageName);
			Package<?, ?> pakkage = (Package<?, ?>) dataBase.find(Package.class, parameters);
			if (pakkage != null) {
				this.pakkageId = pakkage.getId();
				this.model = getModel(pakkage);
				Collections.sort(pakkage.getChildren(), new Comparator<Class<?, ?>>() {
					public int compare(Class<?, ?> o1, Class<?, ?> o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
			}
		}

		logger.debug("Package : " + pakkageId + ", class : " + klassId);

		url = req.getOriginalRequestURI();
		int endIndex = url.indexOf(this.getClass().getSimpleName());
		if (endIndex > -1) {
			url = url.substring(0, endIndex);
		}

		logger.debug("Url : " + url);
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
		return dataBase.find(Project.class, Toolkit.hash(Project.class.getName())) != null;
	}

	public Project<?, ?> getProject() {
		return dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
	}

	@SuppressWarnings("unchecked")
	public List<Package> getPackages() {
		List<Package> packages = dataBase.find(Package.class);
		if (packages != null) {
			Collections.sort(packages, new Comparator<Package>() {
				public int compare(Package o1, Package o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		return packages;
	}

	public Package<?, ?> getPackage() {
		logger.debug("getPackage");
		if (pakkageId != null) {
			return dataBase.find(Package.class, pakkageId);
		}
		return null;
	}

	public Class<?, ?> getKlass() {
		logger.debug("getKlass");
		if (klassId != null) {
			return dataBase.find(Class.class, klassId);
		}
		return null;
		// return klass;
	}

	public String getProjectModel() {
		return this.projectModel;
	}

	public String getModel() {
		return this.model;
	}

	/**
	 * This method can be called from a Jelly script and the model can be fed to the applet on the client. Trouble if that generating models for all
	 * the classes in a package takes too long. So this is not used currently.
	 * 
	 * @param klass
	 *            the class of the composite
	 * @param id
	 *            the id of the class or package, or in fact method
	 * @return the base 64 serialised model
	 */
	@SuppressWarnings("unchecked")
	public String getModel(java.lang.Class klass, Long id) {
		logger.debug("getModel(className, id) : " + klass + ", " + id);
		Composite<?, ?> composite = null;
		composite = (Composite<?, ?>) dataBase.find(klass, id);
		return getModel(composite);
		// return this.model;
	}

	public String getSource() {
		if (klassId != null) {
			ISourceCode sourceCode = new CoverageSourceCode(dataBase.find(Class.class, klassId));
			return sourceCode.getSource();
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public String getModel(Composite<?, ?> composite) {
		logger.debug("Composite : " + composite);
		ArrayList<Composite<?, ?>> composites = new ArrayList<Composite<?, ?>>();
		composites.add(composite);
		Object[] uniqueValues = Toolkit.getUniqueValues(composite);
		Long id = Toolkit.hash(uniqueValues);
		composites = getPreviousComposites((java.lang.Class<Composite<?, ?>>) composite.getClass(), owner, id, composites, 1);
		IModeller modeller = new Modeller();
		modeller.visit(composite.getClass(), composites.toArray(new Composite[composites.size()]));
		return modeller.getModel();
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Composite<?, ?>> getPreviousComposites(java.lang.Class<Composite<?, ?>> klass, AbstractBuild<?, ?> abstractBuild, Long id,
			ArrayList<Composite<?, ?>> composites, int history) {
		if (history >= HISTORY) {
			return composites;
		}
		logger.debug("Abstract build : " + abstractBuild);
		AbstractBuild<?, ?> previousBuild = abstractBuild.getPreviousBuild();
		if (previousBuild == null) {
			return composites;
		}
		String dataBaseFile = previousBuild.getRootDir().getAbsolutePath() + File.separator + IConstants.DATABASE_FILE_ODB;
		logger.debug("Previous database file : " + dataBaseFile);
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, false, null);
		Composite<?, ?> composite = dataBase.find(klass, id);
		logger.debug("Looking for composite : " + id + ", " + composite);
		composites.add(composite);
		return getPreviousComposites((java.lang.Class<Composite<?, ?>>) composite.getClass(), previousBuild, id, composites, ++history);
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