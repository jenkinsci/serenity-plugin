package com.ikokoon.serenity.hudson;

import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An object in the chain of proxy objects that serve the front end in Hudson.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09.12.09
 */
@SuppressWarnings("rawtypes")
public class SerenityProjectAction extends Actionable implements ProminentProjectAction {

    private Logger logger = LoggerFactory.getLogger(SerenityProjectAction.class);
    /**
     * The real owner that generated the build.
     */
    private AbstractProject owner;

    /**
     * Constructor takes the real build from Hudson.
     *
     * @param owner the build that generated the actual build
     */
    public SerenityProjectAction(AbstractProject owner) {
        logger.debug("SerenityProjectAction:");
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        logger.debug("getDisplayName");
        return "Serenity report";
    }

    /**
     * {@inheritDoc}
     */
    public String getIconFileName() {
        logger.debug("getIconFileName");
        return "graph.gif";
    }

    /**
     * {@inheritDoc}
     */
    public String getUrlName() {
        logger.debug("getUrlName");
        return "serenity";
    }

    /**
     * {@inheritDoc}
     */
    public String getSearchUrl() {
        logger.debug("getSearchUrl");
        return getUrlName();
    }

    @JavaScriptMethod
    public ISerenityResult getLastResult() {
        logger.debug("getLastResult");
        Run build = owner.getLastStableBuild();
        if (build == null) {
            build = owner.getLastBuild();
        }
        if (build != null) {
            SerenityBuildAction action = build.getAction(SerenityBuildAction.class);
            if (action != null) {
                return action.getResult();
            }
        }
        return null;
    }

    @JavaScriptMethod
    public String getLastBuildProjectId() {
        return getLastResult().getLastBuildProjectId();
    }

    @JavaScriptMethod
    public String getProjectName() {
        return getLastResult().getName();
    }

    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
        logger.debug("doIndex");
        if (hasResult()) {
            rsp.sendRedirect2("../lastBuild/serenity");
        } else {
            rsp.sendRedirect2("nocoverage");
        }
    }

    public boolean hasResult() {
        logger.debug("hasResult");
        return getLastResult() != null;
    }

}