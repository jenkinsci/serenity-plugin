package com.ikokoon.serenity.hudson;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.hudson.modeller.HighchartsModeller;
import com.ikokoon.serenity.hudson.modeller.IModeller;
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
	/** Owner is necessary to render the sidepanel.jelly */
	private AbstractBuild<?, ?> abstractBuild;
	/** The project for the result. */
	private Project<?, ?> project;
	/** The currently selected composite. */
	private Composite<?, ?> composite;

	/**
	 * Constructor takes the real action that generated the build for the project.
	 * 
	 * @param abstractBuild the build action that generated the build for the project
	 */
	public SerenityResult(AbstractBuild<?, ?> abstractBuild) {
		this.abstractBuild = abstractBuild;
	}

	/**
	 * This method is called from the front end. The result from the call will result in some piece of data being extracted from the database. For example if
	 * the user clicks on a package the name of the package will be used to get that package from the database and will be made available to the UI.
	 * 
	 * @param token the token from the front end
	 * @param req the Stapler request from the ui
	 * @param rsp the Stapler response for the ui
	 * @return the result which is this class
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getDynamic(final String token, final StaplerRequest req, final StaplerResponse rsp) throws Exception {

		String klass = req.getParameter("class");
		String id = req.getParameter("id");

		IDataBase dataBase = null;
		try {
			if (klass != null && id != null) {
				long _id = Long.parseLong(id);
				if (composite != null && composite.getId().equals(_id)) {
					return this;
				}
				java.lang.Class _klass = java.lang.Class.forName(klass);
				dataBase = getDataBase(abstractBuild);
				composite = dataBase.find(_klass, _id);
			}
		} catch (Exception e) {
			logger.error("Exception initialising the model and the source for : " + klass + ", " + id, e);
		} finally {
			closeDataBase(dataBase);
		}

		return this;
	}

	public Object getOwner() {
		return this.abstractBuild;
	}

	public String getName() {
		if (abstractBuild != null) {
			AbstractProject<?, ?> abstractProject = abstractBuild.getProject();
			if (abstractProject != null) {
				return abstractProject.getName();
			}
		}
		return "No name Project...";
	}

	public boolean hasReport() {
		IDataBase dataBase = null;
		try {
			dataBase = getDataBase(abstractBuild);
			return dataBase.find(Project.class, Toolkit.hash(Project.class.getName())) != null;
		} finally {
			closeDataBase(dataBase);
		}
	}

	public Project<?, ?> getProject() {
		if (project == null) {
			IDataBase dataBase = null;
			try {
				dataBase = getDataBase(abstractBuild);
				project = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));
			} finally {
				closeDataBase(dataBase);
			}
		}
		return project;
	}

	@SuppressWarnings("rawtypes")
	public List<Package> getPackages() {
		IDataBase dataBase = null;
		try {
			dataBase = getDataBase(abstractBuild);
			List<Package> packages = dataBase.find(Package.class);
			if (packages != null) {
				Collections.sort(packages, new Comparator<Package>() {
					public int compare(Package o1, Package o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
				// Sort the classes in the packages
				for (Package<?, ?> pakkage : packages) {
					Collections.sort(pakkage.getChildren(), new Comparator<Class<?, ?>>() {
						public int compare(Class<?, ?> o1, Class<?, ?> o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
					// Sort the methods in the classes
					for (Class<?, ?> klass : pakkage.getChildren()) {
						Collections.sort(klass.getChildren(), new Comparator<Method<?, ?>>() {
							public int compare(Method<?, ?> o1, Method<?, ?> o2) {
								return o1.getName().compareTo(o2.getName());
							}
						});
					}
				}
			}
			// Remove the inner classes from the packages
			for (Package<?, ?> pakkage : packages) {
				Iterator<Class<?, ?>> iterator = pakkage.getChildren().iterator();
				while (iterator.hasNext()) {
					Class<?, ?> klass = iterator.next();
					if (klass.getName().indexOf("$") > -1) {
						iterator.remove();
					}
				}
			}
			return packages;
		} finally {
			closeDataBase(dataBase);
		}
	}

	public String getModel() {
		if (composite == null) {
			return "";
		}
		return getModel(null, composite);
	}

	public String getProjectModel() {
		// Move the build forward to the last build because Hudson will go to the last stable build
		// which we don't want, we want the last build
		AbstractBuild<?, ?> abstractBuild = getLastBuild(this.abstractBuild);

		IDataBase dataBase = getDataBase(abstractBuild);
		Project<?, ?> project = dataBase.find(Project.class, Toolkit.hash(Project.class.getName()));

		Object object = abstractBuild.getProject();
		if (object instanceof hudson.model.Project<?, ?>) {
			hudson.model.Project<?, ?> hudsonProject = (hudson.model.Project<?, ?>) object;
			String projectName = hudsonProject.getName();
			project.setName(projectName);
		}

		return getModel("ProjectSmall", project);
	}

	private AbstractBuild<?, ?> getLastBuild(final AbstractBuild<?, ?> abstractBuild) {
		if (abstractBuild.getNextBuild() == null) {
			return abstractBuild;
		}
		AbstractBuild<?, ?> nextAbstractBuild = getLastBuild(abstractBuild.getNextBuild());
		if (nextAbstractBuild.isBuilding()) {
			return abstractBuild;
		}
		return getLastBuild(abstractBuild.getNextBuild());
	}

	public String getSource() {
		return getSource(composite);
	}

	public String getFile(final String name) {
		return Toolkit.getContents(this.getClass().getResourceAsStream(name)).toString();
	}

	private String getSource(final Composite<?, ?> composite) {
		if (composite != null && Class.class.isAssignableFrom(composite.getClass())) {
			String className = ((Class<?, ?>) composite).getName();
			
			StringBuilder sourceFilePath = new StringBuilder(abstractBuild.getRootDir().getAbsolutePath());
			sourceFilePath.append(File.separator);
			sourceFilePath.append(IConstants.SERENITY_SOURCE);
			sourceFilePath.append(File.separator);
			sourceFilePath.append(className);
			sourceFilePath.append(".html");
			
			File sourceFile = new File(sourceFilePath.toString());
			if (sourceFile.exists()) {
				return Toolkit.getContents(sourceFile).toString();
			}
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public String getModel(final String modelName, final Composite<?, ?> composite) {
		if (composite == null) {
			return "";
		}
		LinkedList<Composite<?, ?>> composites = new LinkedList<Composite<?, ?>>();
		composites.addFirst(composite);
		LinkedList<Integer> buildNumbers = new LinkedList<Integer>();
		buildNumbers.addFirst(abstractBuild.number);

		composites = getPreviousComposites((java.lang.Class<Composite<?, ?>>) composite.getClass(), abstractBuild, composites, buildNumbers, composite.getId(),
				1);

		IModeller modeller = new HighchartsModeller(modelName, buildNumbers.toArray(new Integer[buildNumbers.size()]));
		modeller.visit(composite.getClass(), composites.toArray(new Composite[composites.size()]));
		String model = modeller.getModel();
		return model;
	}

	@SuppressWarnings("unchecked")
	LinkedList<Composite<?, ?>> getPreviousComposites(final java.lang.Class<Composite<?, ?>> klass, final AbstractBuild<?, ?> abstractBuild,
			final LinkedList<Composite<?, ?>> composites, final LinkedList<Integer> buildNumbers, final Long id, final int history) {
		if (history >= HISTORY) {
			return composites;
		}
		AbstractBuild<?, ?> previousBuild = abstractBuild.getPreviousBuild();
		if (previousBuild == null) {
			return composites;
		}
		IDataBase dataBase = getDataBase(previousBuild);
		if (dataBase == null) {
			return composites;
		}
		Composite<?, ?> composite = dataBase.find(klass, id);
		closeDataBase(dataBase);
		if (composite == null) {
			return composites;
		}
		composites.addFirst(composite);
		buildNumbers.addFirst(previousBuild.number);
		return getPreviousComposites((java.lang.Class<Composite<?, ?>>) composite.getClass(), previousBuild, composites, buildNumbers, id, history + 1);
	}

	@SuppressWarnings("unchecked")
	protected void printParameters(final StaplerRequest req) {
		Enumeration<String> parameterNames = req.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			logger.debug("Parameter : " + parameterName + ", value : " + req.getParameter(parameterName));
		}
	}

	private IDataBase getDataBase(final AbstractBuild<?, ?> abstractBuild) {
		String dataBaseFile = abstractBuild.getRootDir().getAbsolutePath() + File.separator + IConstants.DATABASE_FILE_ODB;
		return IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, dataBaseFile, null);
	}

	private void closeDataBase(final IDataBase dataBase) {
		try {
			if (dataBase != null) {
				dataBase.close();
			}
		} catch (Exception e) {
			logger.error("Exception closing database : " + dataBase, e);
		}
	}

}