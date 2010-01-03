package com.ikokoon.serenity.hudson;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.hudson.modeller.HighchartsModeller;
import com.ikokoon.serenity.hudson.modeller.IModeller;
import com.ikokoon.serenity.hudson.source.CoverageSourceCode;
import com.ikokoon.serenity.hudson.source.ISourceCode;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Composite;
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
	/** The database file/name. */
	private String dataBaseFile;
	/** The base url for Stapler. */
	private String url = "";
	/** The model for the currently selected composite. */
	private String model;
	/** The source for the currently selected item. */
	private String source;

	/**
	 * Constructor takes the real action that generated the build for the project.
	 * 
	 * @param abstractBuild
	 *            the build action that generated the build for the project
	 */
	public SerenityResult(AbstractBuild<?, ?> abstractBuild) {
		logger.debug("SerenityResult");
		this.owner = abstractBuild;
		dataBaseFile = owner.getRootDir().getAbsolutePath() + File.separator + IConstants.DATABASE_FILE_ODB;
		logger.debug("Opening database on file : " + dataBaseFile);
		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, false, null);
		Project<?, ?> project = (Project<?, ?>) dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		logger.debug("Project : " + project);
		dataBase.close();
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
	@SuppressWarnings("unchecked")
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) throws Exception {
		logger.debug("getDynamic:" + token);

		printParameters(req);

		String klass = req.getParameter("class");
		String id = req.getParameter("id");

		try {
			if (klass != null && id != null) {
				dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, false, null);
				Composite<?, ?> composite = this.dataBase.find((java.lang.Class<Composite<?, ?>>) java.lang.Class.forName(klass), Long.parseLong(id));
				logger.debug("Class : " + klass + ", id : " + id + ", " + composite);
				this.model = getModel(composite);
				// logger.warn("Model : " + model);
				this.source = getSource(composite);
				// logger.warn("Source : " + source);
			}
		} catch (Exception e) {
			logger.error("Exception initialising the model and the source for : " + klass + ", " + id, e);
		} finally {
			dataBase.close();
		}

		url = req.getOriginalRequestURI();
		int endIndex = url.indexOf(this.getClass().getSimpleName());
		if (endIndex > -1) {
			url = url.substring(0, endIndex);
		}

		logger.debug("Url : " + url);
		return this;
	}

	@SuppressWarnings("unchecked")
	protected void printParameters(StaplerRequest req) {
		Enumeration<String> parameterNames = req.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			logger.warn("Parameter : " + parameterName + ", value : " + req.getParameter(parameterName));
		}
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
		try {
			dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, false, null);
			return dataBase.find(Project.class, Toolkit.hash(Project.class.getName())) != null;
		} finally {
			dataBase.close();
		}
	}

	public Project<?, ?> getProject() {
		try {
			dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, false, null);
			return dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
		} finally {
			dataBase.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Package> getPackages() {
		try {
			dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, false, null);
			List<Package> packages = dataBase.find(Package.class);
			if (packages != null) {
				Collections.sort(packages, new Comparator<Package>() {
					public int compare(Package o1, Package o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
			}
			return packages;
		} finally {
			dataBase.close();
		}
	}

	public String getModel() {
		return model;
	}

	public String getSource() {
		return source;
	}

	private String getSource(Composite<?, ?> composite) {
		if (composite instanceof Class) {
			ISourceCode sourceCode = new CoverageSourceCode((Class<?, ?>) composite);
			return sourceCode.getSource();
		}
		return "No source";
	}

	@SuppressWarnings("unchecked")
	public String getModel(Composite<?, ?> composite) {
		ArrayList<Composite<?, ?>> composites = new ArrayList<Composite<?, ?>>();
		composites = getPreviousComposites((java.lang.Class<Composite<?, ?>>) composite.getClass(), owner, composites, composite.getId(), 1);
		composites.add(composite);
		IModeller modeller = new HighchartsModeller();
		modeller.visit(composite.getClass(), composites.toArray(new Composite[composites.size()]));
		String model = modeller.getModel();
		return model;
	}

	@SuppressWarnings("unchecked")
	ArrayList<Composite<?, ?>> getPreviousComposites(java.lang.Class<Composite<?, ?>> klass, AbstractBuild<?, ?> abstractBuild,
			ArrayList<Composite<?, ?>> composites, Long id, int history) {
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
		dataBase.close();
		return getPreviousComposites((java.lang.Class<Composite<?, ?>>) composite.getClass(), previousBuild, composites, id, ++history);
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