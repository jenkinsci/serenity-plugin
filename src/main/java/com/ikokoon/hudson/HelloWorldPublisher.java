package com.ikokoon.hudson;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.tasks.BuildStepMonitor;

import java.io.IOException;
import java.io.PrintStream;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class HelloWorldPublisher extends hudson.tasks.Publisher {

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
	private String coverageReportPattern;

	@DataBoundConstructor
	public HelloWorldPublisher(String coverageReportPattern) {
		this.coverageReportPattern = coverageReportPattern;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
		PrintStream consolePrintStream = buildListener.getLogger();
		consolePrintStream.println("Looking for database file : " + coverageReportPattern);
		if (coverageReportPattern == null) {
			consolePrintStream.println("Skipping coverage reports as coverageReportPattern is null");
			return false;
		}
		if (!Result.SUCCESS.equals(build.getResult())) {
			buildListener.getLogger().println("Build was not successful... but will still publish the report");
			// return true;
		}

		buildListener.getLogger().println("Publishing Serenity reports...");

		FilePath[] reports = new FilePath[0];
		final FilePath moduleRoot = build.getParent().getWorkspace();
		try {
			reports = moduleRoot.list(coverageReportPattern);
		} catch (IOException e) {
			Util.displayIOException(e, buildListener);
			e.printStackTrace(buildListener.fatalError("Unable to find Serenity results"));
			build.setResult(Result.FAILURE);
		}

		if (reports.length == 0) {
			buildListener.getLogger().println("No coverage results were found using the pattern '" + coverageReportPattern + "'.");
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
		FilePath targetPath = new FilePath(buildTarget, "./serenity/serenity.db");
		try {
			buildListener.getLogger().println(
					"Publishing serenity db from : " + singleReport.toURI().getRawPath() + ", to : " + targetPath.toURI().getRawPath());
			singleReport.copyTo(targetPath);
		} catch (IOException e) {
			Util.displayIOException(e, buildListener);
			e.printStackTrace(buildListener.fatalError("Unable to copy Serenity database file from : " + singleReport + ", to : " + buildTarget));
			build.setResult(Result.FAILURE);
		}

		buildListener.getLogger().println("Accessing Serenity results...");
		//		ISerenityResult coverageResult = new com.ikokoon.hudson.SerenityResult(build);
		//		SerenityBuildAction buildAction = new SerenityBuildAction(build, coverageResult);
		//		build.getActions().add(buildAction);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Action getProjectAction(Project project) {
		return new HelloWorldProjectAction(project);
	}

	/**
	 * {@inheritDoc}
	 */
	public Descriptor<hudson.tasks.Publisher> getDescriptor() {
		// see Descriptor javadoc for more about what a descriptor is.
		return DESCRIPTOR;
	}

	public void setCoverageReportPattern(String coverageReportPattern) {
		this.coverageReportPattern = coverageReportPattern;
	}

	public String getCoverageReportPattern() {
		return coverageReportPattern;
	}

	/**
	 * Descriptor for {@link SerenityPublisher}. Used as a singleton. The class is marked as public so that it can be accessed from views.
	 * <p/>
	 * <p/>
	 * See <tt>views/hudson/plugins/coverage/CoveragePublisher/*.jelly</tt> for the actual HTML fragment for the configuration screen.
	 */
	public static final class DescriptorImpl extends Descriptor<hudson.tasks.Publisher> {
		/**
		 * Constructs a new DescriptorImpl.
		 */
		DescriptorImpl() {
			super(HelloWorldPublisher.class);
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Publish Serenity Report";
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			return configure(req);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean configure(StaplerRequest req) throws FormException {
			req.bindParameters(this, "serenity.");
			save();
			return super.configure(req);
		}

		/**
		 * Creates a new instance of {@link SerenityPublisher} from a submitted form.
		 */
		public HelloWorldPublisher newInstance(StaplerRequest req) throws FormException {
			HelloWorldPublisher instance = req.bindParameters(HelloWorldPublisher.class, "serenity.");
			return instance;
		}
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.STEP;
	}
}