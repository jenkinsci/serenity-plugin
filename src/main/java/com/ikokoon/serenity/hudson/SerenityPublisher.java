package com.ikokoon.serenity.hudson;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class runs at the end of the build, called by Hudson. The purpose is to copy the database file from the output directory for the reports
 * directory where the build action can present the data to the front end.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
@SuppressWarnings("unchecked")
public class SerenityPublisher extends Recorder {

	static {
		LoggingConfigurator.configure();
	}
	protected static Logger logger = Logger.getLogger(SerenityPublisher.class);
	/** The description for Hudson. */
	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	@DataBoundConstructor
	public SerenityPublisher() {
	}

	/** The pattern for the object database file. */
	// private String serenityDatabase = "**/serenity/serenity.odb";
	// @DataBoundConstructor
	// public SerenityPublisher(String serenityDatabase) {
	// logger.debug("SerenityPublisher:" + serenityDatabase);
	// if (serenityDatabase != null) {
	// // this.serenityDatabase = serenityDatabase;
	// }
	// }
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
		logger.debug("perform");
		// PrintStream consolePrintStream = buildListener.getLogger();
		// consolePrintStream.println("Looking for database file : " + serenityDatabase);

		// if (serenityDatabase == null) {
		// consolePrintStream.println("Skipping coverage reports as serenityDatabase is null");
		// return false;
		// }

		if (!Result.SUCCESS.equals(build.getResult())) {
			buildListener.getLogger().println("Build was not successful... but will still try to publish the report");
		}

		buildListener.getLogger().println("Publishing Serenity reports...");

		copyDataBaseToBuildDirectory(build, buildListener);
		copySourceToBuildDirectory(build, buildListener);

		buildListener.getLogger().println("Accessing Serenity results...");
		ISerenityResult result = new SerenityResult(build);
		SerenityBuildAction buildAction = new SerenityBuildAction(build, result);
		build.getActions().add(buildAction);

		return true;
	}

	private boolean copySourceToBuildDirectory(AbstractBuild<?, ?> build, final BuildListener buildListener) throws InterruptedException, IOException {
		FilePath moduleRoot = build.getWorkspace();
		buildListener.getLogger().println("Module root : " + moduleRoot.toURI().getRawPath());

		List<File> sourceDirectories = new ArrayList<File>();
		Toolkit.findFiles(new File(moduleRoot.toURI().getRawPath()), new Toolkit.IFileFilter() {
			public boolean matches(File file) {
				if (file == null) {
					return false;
				}
				if (!file.isDirectory()) {
					return false;
				}
				String filePath = file.getAbsolutePath();
				filePath = Toolkit.replaceAll(filePath, "\\", "/");
				if (filePath.indexOf("serenity/source") == -1) {
					return false;
				}
				return true;
			}
		}, sourceDirectories);

		FilePath buildDirectory = new FilePath(build.getRootDir());
		FilePath buildSourceDirectory = new FilePath(buildDirectory, IConstants.SERENITY_SOURCE);
		File targetSourceDirectory = new File(buildSourceDirectory.toURI().getRawPath());

		try {
			for (File sourceDirectory : sourceDirectories) {
				buildListener.getLogger().println(
						"Publishing serenity source from : " + sourceDirectory.getAbsolutePath() + ", to : "
								+ buildSourceDirectory.toURI().getRawPath());
				Toolkit.copyFile(sourceDirectory, targetSourceDirectory);
			}
		} catch (IOException e) {
			Util.displayIOException(e, buildListener);
			e.printStackTrace(buildListener.fatalError("Unable to copy Serenity database file from : " + sourceDirectories + ", to : "
					+ buildDirectory));
		}
		return true;
	}

	private boolean copyDataBaseToBuildDirectory(AbstractBuild<?, ?> build, BuildListener buildListener) throws InterruptedException, IOException {
		String serenityDatabase = "**/serenity/serenity.odb";
		FilePath[] reports = new FilePath[0];
		final FilePath moduleRoot = build.getWorkspace();
		buildListener.getLogger().println("Module root : " + moduleRoot.toURI().getRawPath());
		try {
			reports = moduleRoot.list(serenityDatabase);
		} catch (IOException e) {
			Util.displayIOException(e, buildListener);
			e.printStackTrace(buildListener.fatalError("Unable to find Serenity results"));
			build.setResult(Result.FAILURE);
			return false;
		}

		if (reports.length == 0) {
			buildListener.getLogger().println("No coverage results were found using the pattern '" + serenityDatabase + "'.");
			build.setResult(Result.FAILURE);
			return true;
		}

		if (reports.length > 1) {
			buildListener.getLogger().println("Serenity publisher found more than one report that match the pattern. Impossible, but true.");
			build.setResult(Result.FAILURE);
			return true;
		}

		FilePath buildTarget = new FilePath(build.getRootDir());
		FilePath singleReport = reports[0];
		FilePath targetPath = new FilePath(buildTarget, IConstants.DATABASE_FILE_ODB);

		try {
			buildListener.getLogger().println(
					"Publishing serenity db from : " + singleReport.toURI().getRawPath() + ", to : " + targetPath.toURI().getRawPath());
			singleReport.copyTo(targetPath);
		} catch (IOException e) {
			Util.displayIOException(e, buildListener);
			e.printStackTrace(buildListener.fatalError("Unable to copy Serenity database file from : " + singleReport + ", to : " + buildTarget));
			build.setResult(Result.FAILURE);
		}
		return true;
	}

	@Override
	public Action getProjectAction(AbstractProject abstractProject) {
		logger.debug("getProjectAction(AbstractProject)");
		return new SerenityProjectAction(abstractProject);
	}

	// public void setSerenityDatabase(String serenityDatabase) {
	// logger.debug("setSerenityDatabase");
	// this.serenityDatabase = serenityDatabase;
	// }
	//
	// public String getSerenityDatabase() {
	// logger.debug("getSerenityDatabase");
	// return serenityDatabase;
	// }

	/**
	 * Descriptor for {@link SerenityPublisher}. Used as a singleton. The class is marked as public so that it can be accessed from views.
	 * <p/>
	 * <p/>
	 * See <tt>views/hudson/plugins/coverage/CoveragePublisher/*.jelly</tt> for the actual HTML fragment for the configuration screen.
	 */
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		/**
		 * Constructs a new DescriptorImpl.
		 */
		DescriptorImpl() {
			super(SerenityPublisher.class);
			logger.debug("DescriptorImpl");
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			logger.debug("getDisplayName");
			return "Publish Serenity Report";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			logger.debug("isApplicable");
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			logger.debug("configure");
			req.bindParameters(this, "serenity.");
			save();
			return super.configure(req, json);
		}

		/**
		 * Creates a new instance of {@link SerenityPublisher} from a submitted form.
		 */
		@Override
		public SerenityPublisher newInstance(StaplerRequest req, JSONObject json) throws FormException {
			logger.debug("newInstance");
			SerenityPublisher instance = req.bindParameters(SerenityPublisher.class, "serenity.");
			return instance;
		}
	}

	public BuildStepMonitor getRequiredMonitorService() {
		logger.debug("getRequiredMonitorService");
		return BuildStepMonitor.STEP;
	}
}