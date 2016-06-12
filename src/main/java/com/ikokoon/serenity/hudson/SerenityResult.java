package com.ikokoon.serenity.hudson;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.hudson.modeller.GoogleChartModeller;
import com.ikokoon.serenity.hudson.modeller.IModeller;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.*;
import com.ikokoon.serenity.model.Package;
import com.ikokoon.serenity.persistence.DataBaseOdb;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.Toolkit;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.remoting.Base64;
import org.apache.log4j.Logger;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Exported;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * This is the result that will be used to render the results on the front end.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12.08.09
 */
public class SerenityResult implements ISerenityResult {

    private Logger logger = Logger.getLogger(SerenityResult.class);
    /**
     * Owner is necessary to render the sidepanel.jelly
     */
    private AbstractBuild<?, ?> abstractBuild;
    /**
     * The project for the result.
     */
    private Project<?, ?> project;
    /**
     * The currently selected composite.
     */
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
     * This method is called from the front end. The result from the call will result in some piece of data being extracted
     * from the database. For example if the user clicks on a package the name of the package will be used to get that package
     * from the database and will be made available to the UI.
     *
     * @param token the token from the front end
     * @param req   the Stapler request from the ui
     * @param rsp   the Stapler response for the ui
     * @return the result which is this class
     * @throws Exception anything untoward
     */
    @SuppressWarnings({"unchecked", "rawtypes", "unused"})
    public Object getDynamic(final String token, final StaplerRequest req, final StaplerResponse rsp) throws Exception {
        String klass = req.getParameter("class");
        String id = req.getParameter("id");
        IDataBase dataBase = null;
        try {
            if (klass != null && id != null) {
                long _id = Long.parseLong(id);
                if (composite == null || !composite.getId().equals(_id)) {
                    java.lang.Class _klass = java.lang.Class.forName(klass);
                    dataBase = getDataBase(abstractBuild);
                    composite = dataBase.find(_klass, _id);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("Exception initialising the model and the source for : " + klass + ", " + id, e);
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings({"rawtypes", "Convert2Lambda"})
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
            return packages;
        } finally {
            closeDataBase(dataBase);
        }
    }

    @SuppressWarnings("unused")
    public String getFile(final String name) {
        try {
            return Toolkit.getContents(this.getClass().getResourceAsStream(name)).toString(IConstants.ENCODING);
        } catch (final UnsupportedEncodingException e) {
            logger.error(IConstants.ENCODING + " not supported on this platform : ", e);
        }
        return null;
    }

    @Exported
    public String getSource() {
        return getSource(composite);
    }

    @Exported
    @JavaScriptMethod
    public String getSource(final String klass, final String identifier) {
        return getSource(getComposite(klass, identifier));
    }

    @Exported
    public String getSource(final Composite<?, ?> composite) {
        if (composite != null && Class.class.isAssignableFrom(composite.getClass())) {
            String className = ((Class<?, ?>) composite).getName();

            @SuppressWarnings("StringBufferReplaceableByString")
            StringBuilder sourceFilePath = new StringBuilder(abstractBuild.getRootDir().getAbsolutePath());
            sourceFilePath.append(File.separator);
            sourceFilePath.append(IConstants.SERENITY_SOURCE);
            sourceFilePath.append(File.separator);
            sourceFilePath.append(className);
            sourceFilePath.append(".html");

            File sourceFile = new File(sourceFilePath.toString());
            try {
                String source = Toolkit.getContents(sourceFile).toString(IConstants.ENCODING);
                source = source.replace("\r", "").replace("\n", "").replace("\\", "");
                return Base64.encode(source.getBytes(IConstants.ENCODING));
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return "";
    }

    @JavaScriptMethod
    public String getModel(final String klass, final String identifier) {
        return getModel(getComposite(klass, identifier));
    }

    @SuppressWarnings("unchecked")
    public String getModel(final Composite<?, ?> composite) {
        if (composite == null) {
            return "";
        }
        LinkedList<Composite<?, ?>> composites = new LinkedList<>();
        composites.addFirst(composite);
        List<Integer> buildNumbers = new ArrayList<>();
        buildNumbers.add(abstractBuild.number);

        composites = getPreviousComposites((java.lang.Class<Composite<?, ?>>) composite.getClass(), abstractBuild, composites, buildNumbers, composite.getId(), 1);

        IModeller modeller = new GoogleChartModeller();
        modeller.setBuildNumbers(buildNumbers.toArray(new Integer[buildNumbers.size()]));
        modeller.visit(composite.getClass(), composites.toArray(new Composite[composites.size()]));
        return modeller.getModel();
    }

    private Composite<?, ?> getComposite(final String klass, final String identifier) {
        IDataBase dataBase = getDataBase(abstractBuild);
        long _id = Long.parseLong(identifier);
        java.lang.Class _klass;
        try {
            _klass = java.lang.Class.forName(klass);
            return dataBase.find(_klass, _id);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    LinkedList<Composite<?, ?>> getPreviousComposites(final java.lang.Class<Composite<?, ?>> klass, final AbstractBuild<?, ?> abstractBuild,
                                                      final LinkedList<Composite<?, ?>> composites, final List<Integer> buildNumbers, final Long id, final int history) {
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
        buildNumbers.add(previousBuild.number);
        return getPreviousComposites((java.lang.Class<Composite<?, ?>>) composite.getClass(), previousBuild, composites, buildNumbers, id, history + 1);
    }

    @SuppressWarnings({"unchecked", "unused"})
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
        } catch (final Exception e) {
            logger.error("Exception closing database : " + dataBase, e);
        }
    }

    /*@Deprecated
    @JavaScriptMethod
    @SuppressWarnings("unused")
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

        return getModel(project);
    }*/

    /*private AbstractBuild<?, ?> getLastBuild(final AbstractBuild<?, ?> abstractBuild) {
        if (abstractBuild.getNextBuild() == null) {
            return abstractBuild;
        }
        AbstractBuild<?, ?> nextAbstractBuild = getLastBuild(abstractBuild.getNextBuild());
        if (nextAbstractBuild.isBuilding()) {
            return abstractBuild;
        }
        return getLastBuild(abstractBuild.getNextBuild());
    }*/

}