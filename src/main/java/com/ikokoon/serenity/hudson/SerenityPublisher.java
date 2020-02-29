package com.ikokoon.serenity.hudson;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.hudson.source.CoverageSourceCode;
import com.ikokoon.serenity.hudson.source.ISourceCode;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.DataBaseRam;
import com.ikokoon.serenity.persistence.DataBaseToolkit;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.serenity.process.Aggregator;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Toolkit;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class runs at the end of the build, called by Hudson. The purpose is to copy the database files from the output
 * directories for each module in the case of Maven and Ant builds to the output directory for the build for display in the
 * Hudson front end plugin. As well as this the source that was found for the project is copied to the source directory where
 * the front end can access it.
 * <p>
 * Once all the database files are copied to a location on the local machine then they are merged together and pruned.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.07.09
 */
@SuppressWarnings("unchecked")
public class SerenityPublisher extends Recorder implements Serializable {

    /*
     * Initialise the logging.
     */
    static {
        LoggingConfigurator.configure();
    }

    /**
     * The pattern to exclude from the file filter.
     */
    static final String SERENITY_ODB_REGEX = ".*(serenity.odb)";
    static final String SERENITY_SOURCE_REGEX = ".*serenity.*.source.*";
    /**
     * The LOGGER.
     */
    protected static final Logger LOGGER = Logger.getLogger(SerenityPublisher.class.getName());
    /**
     * The description for Hudson.
     */
    @Extension
    @SuppressWarnings("unused")
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
            printStream.println("Start publishing Serenity reports : ");
            if (!Result.SUCCESS.equals(build.getResult())) {
                printStream.println("Build was not successful, but will still try to publish the report");
            }

            // Copy the source for the Java files to the build directory
            writeCoveredSourceForClasses(build, buildListener);

            // Copy the database files from the output directories to the build directory. and
            // merge them and then aggregate all the data, then prune the data
            IDataBase targetDataBase = copyDataBasesToBuildDirectory(build, buildListener);
            aggregate(buildListener, targetDataBase);
            // prune(buildListener, targetDataBase);
            targetDataBase.close();

            printStream.println("Finished publishing the Serenity results : ");
            ISerenityResult result = new SerenityResult(build);
            SerenityBuildAction buildAction = new SerenityBuildAction(build, result);
            //noinspection deprecation
            build.getActions().add(buildAction);
        } catch (final Exception e) {
            printStream.println(e.getMessage());
            LOGGER.log(Level.SEVERE, null, e);
        }

        return true;
    }

    /**
     * Scans the output directory for the project and locates all the database files. These files
     * are then copied to a local file in the user temp directory. The databases are merged into the
     * main database.
     *
     * @param build         the build for this project
     * @param buildListener the listener of the build
     * @return the primary database file for the final result
     */
    @SuppressWarnings("rawtypes")
    private IDataBase copyDataBasesToBuildDirectory(final AbstractBuild<?, ?> build, final BuildListener buildListener) throws InterruptedException,
            IOException {
        final PrintStream printStream = buildListener.getLogger();
        IDataBase targetDataBase = null;

        try {
            File buildDirectory = build.getRootDir();
            printStream.println("Build directory : " + buildDirectory);
            File targetDataBaseFile = new File(buildDirectory, IConstants.DATABASE_FILE_ODB);
            printStream.println("Target database : " + targetDataBaseFile);

            // Create the final output database file for the build
            String targetPath = targetDataBaseFile.getAbsolutePath();
            IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, targetPath, null);
            targetDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, IConstants.DATABASE_FILE_RAM + "." + Math.random(), odbDataBase);

            // Scan the build output roots for database files to merge
            FilePath[] moduleRoots = build.getModuleRoots();
            // The list of Serenity database files found in the module roots
            List<FilePath> databaseFiles = new ArrayList<>();
            Pattern pattern = Pattern.compile(SERENITY_ODB_REGEX);
            for (final FilePath moduleRoot : moduleRoots) {
                // printStream.println("Module root : " + moduleRoot.toURI());
                try {
                    findFilesAndDirectories(moduleRoot, databaseFiles, pattern);
                } catch (final Exception e) {
                    printStream.println("Exception searching for database files : " + moduleRoot);
                    LOGGER.log(Level.SEVERE, null, e);
                }
            }

            // Iterate over the database files that were found and merge them to the final database
            for (final FilePath databaseFile : databaseFiles) {
                if (databaseFile.isDirectory()) {
                    continue;
                }
                printStream.println("Database for merging : " + databaseFile.getRemote());
                String sourcePath = null;
                IDataBase sourceDataBase = null;
                File sourceFile = null;
                try {
                    // We copy the file to a 'local' file because the database could be on a remote machine
                    // sourceFile = File.createTempFile("serenity", ".odb", new File(IConstants.SERENITY_DIRECTORY));
                    String name = "serenity-" + RandomStringUtils.random(8) + ".odb";
                    Files.createDirectories(new File(IConstants.SERENITY_DIRECTORY).toPath());
                    sourceFile = Files.createFile(new File(IConstants.SERENITY_DIRECTORY, name).toPath()).toFile();

                    FilePath sourceFilePath = new FilePath(sourceFile);
                    databaseFile.copyTo(sourceFilePath);
                    sourcePath = sourceFile.getAbsolutePath();

                    sourceDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, sourcePath, null);
                    // Copy the data from the source into the target, then close the source
                    printStream.println("Merging database from : " + sourcePath + " to : " + targetPath);
                    DataBaseToolkit.copyDataBase(sourceDataBase, targetDataBase);
                    List<Class> classes = targetDataBase.find(Class.class);
                    for (final Class clazz : classes) {
                        File coverageSourceCodeFile = new File(buildDirectory, IConstants.SERENITY_SOURCE + File.separatorChar + clazz.getName() + ".html");
                        ISourceCode sourceCode = new CoverageSourceCode(clazz, clazz.getSource());
                        String htmlSource = sourceCode.getSource();
                        // printStream.println("Creating coverage source : " + coverageSourceCodeFile.getAbsolutePath());
                        Toolkit.setContents(coverageSourceCodeFile, htmlSource.getBytes());
                    }
                } catch (final Exception e) {
                    printStream.println("Unable to copy Serenity database file from : " + sourcePath + ", to : " + targetPath);
                    LOGGER.log(Level.SEVERE, null, e);
                } finally {
                    if (sourceDataBase != null) {
                        try {
                            sourceDataBase.close();
                        } catch (final Exception e) {
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
                        } catch (final Exception e) {
                            printStream.println("Exception closing the source database : " + sourcePath + ", target : " + targetPath);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            printStream.println(e.getMessage());
            LOGGER.log(Level.SEVERE, null, e);
        }
        return targetDataBase;
    }

    /**
     * Runs the aggregator on the final database to generate the statistics etc.
     *
     * @param buildListener  the build listener that has the LOGGER in it
     * @param targetDataBase the target database to aggregate
     */
    private void aggregate(final BuildListener buildListener, final IDataBase targetDataBase) {
        try {
            buildListener.getLogger().println("Aggregating data : ");
            new Aggregator(null, targetDataBase).execute();
        } catch (final Exception e) {
            buildListener.getLogger().println(e.getMessage());
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    /*
     * Prunes the database removing all the objects that are no longer needed, like the lines and afferent/efferent objects.
     *
     * @param buildListener  the build listener that has the LOGGER in it
     * @param targetDataBase the target database to aggregate
     */
    /*private void prune(final BuildListener buildListener, final IDataBase targetDataBase) {
        try {
            buildListener.getLogger().println("Pruning data : ");
            new Pruner(null, targetDataBase).execute();
        } catch (final Exception e) {
            buildListener.getLogger().println(e.getMessage());
            LOGGER.log(Level.SEVERE, null, e);
        }
    }*/

    @SuppressWarnings("rawtypes")
    boolean writeCoveredSourceForClasses(final AbstractBuild<?, ?> build, final BuildListener buildListener) throws InterruptedException, IOException {
        try {
            PrintStream printStream = buildListener.getLogger();
            File targetDataBaseFile = new File(build.getRootDir(), IConstants.DATABASE_FILE_ODB);
            printStream.println("Target database : " + targetDataBaseFile);

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
                            LOGGER.warning("Couldn't make directories : " + file.getAbsolutePath());
                        }
                    }
                    // We try to delete the old file first
                    boolean deleted = file.delete();
                    if (!deleted) {
                        LOGGER.fine("Didn't delete source coverage file : " + file);
                    }
                    if (!file.exists()) {
                        if (!Toolkit.createFile(file)) {
                            LOGGER.warning("Couldn't create new source file : " + file);
                        }
                    }
                    if (file.exists()) {
                        LOGGER.fine("Writing source to : " + file);
                        ISourceCode sourceCode = new CoverageSourceCode(clazz, source);
                        String htmlSource = sourceCode.getSource();
                        Toolkit.setContents(file, htmlSource.getBytes());
                    } else {
                        LOGGER.warning("Source file does not exist : " + file);
                    }
                } catch (final Exception e) {
                    LOGGER.log(Level.SEVERE, null, e);
                }
            }
        } catch (final Exception e) {
            buildListener.getLogger().println(e.getMessage());
            LOGGER.log(Level.SEVERE, null, e);
        }
        return true;
    }

    /**
     * Scans recursively the {@link FilePath}(s) for the database files.
     *
     * @param filePath  the starting file path to start scanning from
     * @param filePaths the list of file paths that were found
     * @param pattern   the pattern to find the files and directories
     */
    void findFilesAndDirectories(final FilePath filePath, final List<FilePath> filePaths, final Pattern pattern)
            throws Exception {
        try {
            List<FilePath> subFilePaths = filePath.list();
            if (subFilePaths != null) {
                for (final FilePath subFilePath : subFilePaths) {
                    findFilesAndDirectories(subFilePath, filePaths, pattern);
                }
            }
            String stringFilePath = Toolkit.cleanFilePath(filePath.toURI().toString());
            Matcher matcher = pattern.matcher(stringFilePath);
            if (matcher.matches()) {
                filePaths.add(filePath);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Action getProjectAction(final AbstractProject abstractProject) {
        return new SerenityProjectAction(abstractProject);
    }

    /**
     * Descriptor for {@link SerenityPublisher}. Used as a singleton. The class is marked as public so that it can be accessed from views.
     * See <tt>views/hudson/plugins/coverage/CoveragePublisher/*.jelly</tt> for the actual HTML fragment for the configuration screen.
     */
    @SuppressWarnings("WeakerAccess")
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
        @SuppressWarnings("deprecation")
        public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
            req.bindParameters(this, "serenity.");
            save();
            return super.configure(req, json);
        }

        /**
         * Creates a new instance of {@link SerenityPublisher} from a submitted form.
         */
        @Override
        @SuppressWarnings("deprecation")
        public SerenityPublisher newInstance(final StaplerRequest req, final JSONObject json) throws FormException {
            return req.bindParameters(SerenityPublisher.class, "serenity.");
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }
}