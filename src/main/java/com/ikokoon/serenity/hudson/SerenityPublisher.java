package com.ikokoon.serenity.hudson;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.File;
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
import com.ikokoon.serenity.hudson.source.CoverageSourceCode;
import com.ikokoon.serenity.hudson.source.ISourceCode;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.Aggregator;
import com.ikokoon.serenity.process.Pruner;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class runs at the end of the build, called by Hudson. The purpose is to copy the database files from the output directories for each module in the case
 * of Maven and Ant builds to the output directory for the build for display in the Hudson front end plugin. As well as this the source that was found for the
 * project is copied to the source directory where the front end can access it.
 * 
 * Once all the database files are copied to a location on the local machine then they are merged together and pruned.
 * 
 * @author Michael Couck
 * @since 12.07.09
 * @version 01.00
 */
@SuppressWarnings("unchecked")
public class SerenityPublisher extends Recorder implements Serializable {

	/** Initialise the logging. */
	static {
		LoggingConfigurator.configure();
	}

	/** The pattern to exclude from the file filter. */
	static final String SERENITY_ODB_REGEX = ".*(serenity.odb)";
	static final String SERENITY_SOURCE_REGEX = ".*serenity.*.source.*";
	/** The LOGGER. */
	protected static Logger LOGGER = Logger.getLogger(SerenityPublisher.class);
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
	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener buildListener) throws InterruptedException,
			IOException {
		PrintStream printStream = buildListener.getLogger();
		try {
			printStream.println("Publishing Serenity reports...");
			if (!Result.SUCCESS.equals(build.getResult())) {
				printStream.println("Build was not successful... but will still try to publish the report");
			}

			// Copy the source for the Java files to the build directory
			writeCoveredSourceForClasses(build, buildListener);

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
		} catch (Exception e) {
			printStream.println(e.getMessage());
			LOGGER.error(null, e);
		}

		return true;
	}

	/**
	 * Scans the output directory for the project and locates all the database files. These files are then copied to a local file in the user temp directory.
	 * The databases are merged into the main database.
	 * 
	 * @param build the build for this project
	 * @param buildListener the listener of the build
	 * @return the primary database file for the final result
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private IDataBase copyDataBasesToBuildDirectory(final AbstractBuild<?, ?> build, final BuildListener buildListener) throws InterruptedException,
			IOException {
		final PrintStream printStream = buildListener.getLogger();
		IDataBase targetDataBase = null;

		try {
			File buildDirectory = build.getRootDir();
			printStream.println("Build directory...  " + buildDirectory);
			File targetDataBaseFile = new File(buildDirectory, IConstants.DATABASE_FILE_ODB);
			printStream.println("Target database... " + targetDataBaseFile);

			// Create the final output database file for the build
			String targetPath = targetDataBaseFile.getAbsolutePath();
			IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, targetPath, null);
			targetDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM + "." + Math.random(), odbDataBase);

			// Scan the build output roots for database files to merge
			FilePath[] moduleRoots = build.getModuleRoots();
			// The list of Serenity database files found in the module roots
			List<FilePath> databaseFiles = new ArrayList<FilePath>();
			Pattern pattern = Pattern.compile(SERENITY_ODB_REGEX);
			for (final FilePath moduleRoot : moduleRoots) {
				// printStream.println("Module root : " + moduleRoot.toURI());
				try {
					findFilesAndDirectories(moduleRoot, databaseFiles, pattern, printStream);
				} catch (Exception e) {
					printStream.println("Exception searching for database files : " + moduleRoot);
					LOGGER.error(null, e);
				}
			}

			// Iterate over the database files that were found and merge them to the final database
			for (final FilePath databaseFile : databaseFiles) {
				if (databaseFile.isDirectory()) {
					continue;
				}
				String sourcePath = null;
				IDataBase sourceDataBase = null;
				File sourceFile = null;
				try {
					// We copy the file to a 'local' file because the database could be on a remote machine
					sourceFile = File.createTempFile("serenity", ".odb", new File(IConstants.SERENITY_DIRECTORY));
					FilePath sourceFilePath = new FilePath(sourceFile);
					databaseFile.copyTo(sourceFilePath);
					sourcePath = sourceFile.getAbsolutePath();
					sourceDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, sourcePath, null);
					// Copy the data from the source into the target, then close the source
					printStream.println("Copying database from... " + sourcePath + " to... " + targetPath);
					DataBaseToolkit.copyDataBase(sourceDataBase, targetDataBase);
					List<Class> classes = targetDataBase.find(Class.class);
					for (final Class clazz : classes) {
						File coverageSourceCodeFile = new File(buildDirectory, IConstants.SERENITY_SOURCE + File.separatorChar + clazz.getName() + ".html");
						ISourceCode sourceCode = new CoverageSourceCode(clazz, clazz.getSource());
						String htmlSource = sourceCode.getSource();
						// printStream.println("Creating coverage source : " + coverageSourceCodeFile.getAbsolutePath());
						Toolkit.setContents(coverageSourceCodeFile, htmlSource.getBytes());
					}
				} catch (Exception e) {
					printStream.println("Unable to copy Serenity database file from : " + sourcePath + ", to : " + targetPath);
					LOGGER.error(null, e);
				} finally {
					if (sourceDataBase != null) {
						try {
							sourceDataBase.close();
						} catch (Exception e) {
							printStream.println("Exception closing the source database : " + sourcePath + ", target : " + targetPath);
						}
					}
					if (sourceFile != null) {
						try {
							boolean deleted = sourceFile.delete();
							if (!deleted) {
								printStream.println("Couldn't delete temp database file... " + sourcePath);
								Toolkit.deleteFile(sourceFile, 3);
								sourceFile.deleteOnExit();
							}
						} catch (Exception e) {
							printStream.println("Exception closing the source database : " + sourcePath + ", target : " + targetPath);
						}
					}
				}
			}
		} catch (Exception e) {
			printStream.println(e.getMessage());
			LOGGER.error(null, e);
		}
		return targetDataBase;
	}

	/**
	 * Runs the aggregator on the final database to generate the statistics etc.
	 * 
	 * @param build the build for the project
	 * @param buildListener the build listener that has the LOGGER in it
	 * @param targetDataBase the target database to aggregate
	 */
	private void aggregate(final AbstractBuild<?, ?> build, final BuildListener buildListener, final IDataBase targetDataBase) {
		try {
			buildListener.getLogger().println("Aggregating data... ");
			new Aggregator(null, targetDataBase).execute();
		} catch (Exception e) {
			buildListener.getLogger().println(e.getMessage());
			LOGGER.error(null, e);
		}
	}

	/**
	 * Prunes the database removing all the objects that are no longer needed, like the lines and afferent/efferent objects.
	 * 
	 * @param build the build for the project
	 * @param buildListener the build listener that has the LOGGER in it
	 * @param targetDataBase the target database to aggregate
	 */
	private void prune(final AbstractBuild<?, ?> build, final BuildListener buildListener, final IDataBase targetDataBase) {
		try {
			buildListener.getLogger().println("Pruning data...");
			new Pruner(null, targetDataBase).execute();
		} catch (Exception e) {
			buildListener.getLogger().println(e.getMessage());
			LOGGER.error(null, e);
		}
	}

	@SuppressWarnings("rawtypes")
	boolean writeCoveredSourceForClasses(final AbstractBuild<?, ?> build, final BuildListener buildListener) throws InterruptedException, IOException {
		try {
			PrintStream printStream = buildListener.getLogger();
			File targetDataBaseFile = new File(build.getRootDir(), IConstants.DATABASE_FILE_ODB);
			printStream.println("Target database... " + targetDataBaseFile);

			// Create the final output database file for the build
			String targetPath = targetDataBaseFile.getAbsolutePath();
			IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, targetPath, null);
			List<Class> classes = odbDataBase.find(Class.class);
			for (final Class clazz : classes) {
				String source = clazz.getSource();
				if (source == null) {
					continue;
				}
				try {
					File file = new File(IConstants.SERENITY_SOURCE, clazz.getName() + ".html");
					if (!file.getParentFile().exists()) {
						boolean madeDirectories = file.getParentFile().mkdirs();
						if (!madeDirectories) {
							LOGGER.warn("Couldn't make directories : " + file.getAbsolutePath());
						}
					}
					// LOGGER.error("Collecting source : " + className + ", " + file.getAbsolutePath());
					// We try to delete the old file first
					boolean deleted = file.delete();
					if (!deleted) {
						LOGGER.debug("Didn't delete source coverage file : " + file);
					}
					if (!file.exists()) {
						if (!Toolkit.createFile(file)) {
							LOGGER.warn("Couldn't create new source file : " + file);
						}
					}
					if (file.exists()) {
						LOGGER.debug("Writing source to : " + file);
						ISourceCode sourceCode = new CoverageSourceCode(clazz, source);
						String htmlSource = sourceCode.getSource();
						Toolkit.setContents(file, htmlSource.getBytes());
					} else {
						LOGGER.warn("Source file does not exist : " + file);
					}
				} catch (Exception e) {
					LOGGER.error(null, e);
				}
			}
		} catch (Exception e) {
			buildListener.getLogger().println(e.getMessage());
			LOGGER.error(null, e);
		}
		return true;
	}

	/**
	 * Scans recursively the {@link FilePath}(s) for the database files.
	 * 
	 * @param filePath the starting file path to start scanning from
	 * @param filePaths the list of file paths that were found
	 * @param fileFilter the file filter that will return all files in the path
	 * @param printStream the LOGGER to the front end
	 * @throws Exception
	 */
	void findFilesAndDirectories(final FilePath filePath, final List<FilePath> filePaths, final Pattern pattern, final PrintStream printStream)
			throws Exception {
		try {
			List<FilePath> subFilePaths = filePath.list();
			if (subFilePaths != null) {
				for (final FilePath subFilePath : subFilePaths) {
					findFilesAndDirectories(subFilePath, filePaths, pattern, printStream);
				}
			}
			String stringFilePath = Toolkit.cleanFilePath(filePath.toURI().toString());
			Matcher matcher = pattern.matcher(stringFilePath);
			if (matcher.matches()) {
				filePaths.add(filePath);
			}
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Action getProjectAction(final AbstractProject abstractProject) {
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
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Publish Serenity Report";
		}

		@Override
		@SuppressWarnings("rawtypes")
		public boolean isApplicable(final java.lang.Class<? extends AbstractProject> jobType) {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
			req.bindParameters(this, "serenity.");
			save();
			return super.configure(req, json);
		}

		/**
		 * Creates a new instance of {@link SerenityPublisher} from a submitted form.
		 */
		@Override
		public SerenityPublisher newInstance(final StaplerRequest req, final JSONObject json) throws FormException {
			SerenityPublisher instance = req.bindParameters(SerenityPublisher.class, "serenity.");
			return instance;
		}
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.STEP;
	}
}