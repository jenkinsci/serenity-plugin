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
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.Aggregator;
import com.ikokoon.serenity.process.Pruner;
import com.ikokoon.toolkit.LoggingConfigurator;

/**
 * This class runs at the end of the build, called by Hudson. The purpose is to copy the database files from the output directories for each module in
 * the case of Maven and Ant builds to the output directory for the build for display in the Hudson front end plugin. As well as this the source that
 * was found for the project is copied to the source directory where the front end can access it.
 *
 * Once all the database files are copied to a location on the local machine then they are merged together and pruned.
 *
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
@SuppressWarnings("unchecked")
public class SerenityPublisher extends Recorder implements Serializable {

	/**
	 * The file filter that matches all the files available. We'll filter then later.
	 *
	 * @author Michael Couck
	 * @since 12.05.10
	 * @version 01.00
	 */
	class FileFilterImpl implements FileFilter, Serializable {
		public boolean accept(File pathname) {
			return true;
		}
	}

	/** Initialise the logging. */
	static {
		LoggingConfigurator.configure();
	}

	/** The pattern to exclude from the file filter. */
	private static final String SERENITY_ODB_REGEX = ".*serenity.odb";
	private static final String SERENITY_SOURCE_REGEX = ".*serenity.*.source.*";
	/** The logger. */
	protected static Logger logger = Logger.getLogger(SerenityPublisher.class);
	/** The description for Hudson. */
	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	@DataBoundConstructor
	public SerenityPublisher() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
		PrintStream printStream = buildListener.getLogger();
		printStream.println("Publishing Serenity reports...");
		if (!Result.SUCCESS.equals(build.getResult())) {
			printStream.println("Build was not successful... but will still try to publish the report");
		}

		// Copy the source for the Java files to the build directory
		copySourceToBuildDirectory(build, buildListener);

		// Copy the database files from the output directories to the build directory. and
		// merge them and then aggregate all the data, then prune the data
		IDataBase targetDataBase = copyDataBasesToBuildDirectory(build, buildListener);
		aggregate(build, buildListener, targetDataBase);
		prune(build, buildListener, targetDataBase);
		targetDataBase.close();

		printStream.println("Publishing the Serenity results...");
		ISerenityResult result = new SerenityResult(build);
		SerenityBuildAction buildAction = new SerenityBuildAction(build, result);
		build.getActions().add(buildAction);

		return true;
	}

	/**
	 * Scans the output directory for the project and locates all the database files. These files are then copied to a local file in the user temp
	 * directory. The databases are merged into the main database.
	 *
	 * @param build
	 *            the build for this project
	 * @param buildListener
	 *            the listener of the build
	 * @return the primary database file for the final result
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private IDataBase copyDataBasesToBuildDirectory(AbstractBuild<?, ?> build, BuildListener buildListener) throws InterruptedException, IOException {
		final PrintStream printStream = buildListener.getLogger();

		File buildDirectory = build.getRootDir();
		printStream.println("Build directory...  " + buildDirectory);
		File targetDataBaseFile = new File(buildDirectory, IConstants.DATABASE_FILE_ODB);
		printStream.println("Target database... " + targetDataBaseFile);

		// Create the final output database file for the build
		String targetPath = targetDataBaseFile.getAbsolutePath();
		IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, targetPath, null);
		IDataBase targetDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM + "." + Math.random(),
				odbDataBase);

		// Scan the build output roots for database files to merge
		FilePath[] moduleRoots = build.getModuleRoots();
		FileFilter fileFilter = new FileFilterImpl();
		// The list of Serenity database files found in the module roots
		List<FilePath> serenityOdbs = new ArrayList<FilePath>();
		Pattern pattern = Pattern.compile(SERENITY_ODB_REGEX);
		for (FilePath moduleRoot : moduleRoots) {
			// printStream.println("Module root : " + moduleRoot.toURI());
			try {
				findFilesAndDirectories(moduleRoot, serenityOdbs, fileFilter, pattern, printStream);
			} catch (Exception e) {
				printStream.println("Exception searching for database files : " + moduleRoot);
				e.printStackTrace(buildListener.fatalError("Exception searching for Serenity database files : " + moduleRoot));
			}
		}

		// Iterate over the database files that were found and merge them to the final database
		for (FilePath serenityOdb : serenityOdbs) {
			File sourceFile = File.createTempFile("serenity", ".odb");
			FilePath sourceFilePath = new FilePath(sourceFile);
			serenityOdb.copyTo(sourceFilePath);
			String sourcePath = sourceFile.getAbsolutePath();
			IDataBase sourceDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, sourcePath, null);
			try {
				// Copy the data from the source into the target, then close the source
				printStream.println("Copying database from... " + sourcePath + " to... " + targetPath);
				DataBaseToolkit.copyDataBase(sourceDataBase, targetDataBase);
				boolean deleted = sourceFile.delete();
				if (!deleted) {
					printStream.println("Couldn't delete temp database file... " + sourcePath);
					sourceFile.deleteOnExit();
				}
			} catch (Exception e) {
				e.printStackTrace(buildListener.fatalError("Unable to copy Serenity database file from : " + sourcePath + ", to : " + targetPath));
				build.setResult(Result.UNSTABLE);
			} finally {
				sourceDataBase.close();
			}
		}
		return targetDataBase;
	}

	/**
	 * Scans recursively the {@link FilePath}(s) for the database files.
	 *
	 * @param filePath
	 *            the starting file path to start scanning from
	 * @param filePaths
	 *            the list of file paths that were found
	 * @param fileFilter
	 *            the file filter that will return all files in the path
	 * @param printStream
	 *            the logger to the front end
	 * @throws Exception
	 */
	private void findFilesAndDirectories(FilePath filePath, List<FilePath> filePaths, FileFilter fileFilter, Pattern pattern, PrintStream printStream)
			throws Exception {
		// printStream.println("File path : " + filePath.toURI());
		List<FilePath> list = filePath.list(fileFilter);
		if (list != null) {
			for (FilePath childFilePath : list) {
				findFilesAndDirectories(childFilePath, filePaths, fileFilter, pattern, printStream);
			}
		}
		Matcher matcher = pattern.matcher(filePath.toURI().toString());
		if (matcher.find()) {
			filePaths.add(filePath);
		}
	}

	/**
	 * Runs the aggregator on the final database to generate the statistics etc.
	 *
	 * @param build
	 *            the build for the project
	 * @param buildListener
	 *            the build listener that has the logger in it
	 * @param targetDataBase
	 *            the target database to aggregate
	 */
	private void aggregate(AbstractBuild<?, ?> build, BuildListener buildListener, IDataBase targetDataBase) {
		buildListener.getLogger().println("Aggregating data... ");
		new Aggregator(null, targetDataBase).execute();
	}

	/**
	 * Prunes the database removing all the objects that are no longer needed, like the lines and afferent/efferent objects.
	 *
	 * @param build
	 *            the build for the project
	 * @param buildListener
	 *            the build listener that has the logger in it
	 * @param targetDataBase
	 *            the target database to aggregate
	 */
	private void prune(AbstractBuild<?, ?> build, BuildListener buildListener, IDataBase targetDataBase) {
		buildListener.getLogger().println("Pruning data...");
		new Pruner(null, targetDataBase).execute();
	}

	private boolean copySourceToBuildDirectory(AbstractBuild<?, ?> build, final BuildListener buildListener) throws InterruptedException, IOException {
		FilePath workSpace = build.getWorkspace();
		PrintStream printStream = buildListener.getLogger();
		printStream.println("Workspace root... " + workSpace.toURI().getRawPath());

		FilePath[] moduleRoots = build.getModuleRoots();
		FileFilter fileFilter = new FileFilterImpl();
		// The list of Serenity source directories found in the module roots
		List<FilePath> sourceDirectories = new ArrayList<FilePath>();
		Pattern pattern = Pattern.compile(SERENITY_SOURCE_REGEX);
		for (FilePath moduleRoot : moduleRoots) {
			// printStream.println("Module root : " + moduleRoot.toURI());
			try {
				findFilesAndDirectories(moduleRoot, sourceDirectories, fileFilter, pattern, printStream);
			} catch (Exception e) {
				printStream.println("Exception searching for source directories : " + moduleRoot);
				e.printStackTrace(buildListener.fatalError("Exception searching for Serenity source files : " + moduleRoot));
			}
		}

		FilePath buildDirectory = new FilePath(build.getRootDir());
		FilePath buildSourceDirectory = new FilePath(buildDirectory, IConstants.SERENITY_SOURCE);

		try {
			buildSourceDirectory.deleteContents();
			for (FilePath sourceDirectory : sourceDirectories) {
				String sourcePath = sourceDirectory.toURI().toString();
				// This is a hack. The pattern for the source directories (.*serenity.*.source) doesn't work in a slave for some reason. So we
				// have to use .*serenity.*.source.*, which returns all the directories and files with the pattern in it, and after that we have to
				// check that this is not a sub directory of the source folder and that it is not a file either. Pity that, try to get the bloody
				// pattern working in the future
				boolean isDirectoryAndSerenitySource = !sourceDirectory.isDirectory() && !sourcePath.endsWith("serenity/source/")
						&& !sourcePath.endsWith("serenity\\source\\");
				if (isDirectoryAndSerenitySource) {
					continue;
				}
				printStream.println("Copying source from... " + sourceDirectory.toURI().toString() + " to... "
						+ buildSourceDirectory.toURI().getRawPath());
				sourceDirectory.copyRecursiveTo(buildSourceDirectory);
			}
		} catch (IOException e) {
			Util.displayIOException(e, buildListener);
			e.printStackTrace(buildListener.fatalError("Unable to copy Serenity source directories from : " + sourceDirectories + ", to : "
					+ buildDirectory));
		}
		return true;
	}

	@Override
	public Action getProjectAction(AbstractProject abstractProject) {
		logger.debug("getProjectAction(AbstractProject)");
		return new SerenityProjectAction(abstractProject);
	}

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
		public boolean isApplicable(java.lang.Class<? extends AbstractProject> jobType) {
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