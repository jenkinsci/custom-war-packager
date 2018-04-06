package io.jenkins.tools.warpackager.mavenplugin;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.impl.Builder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import javax.annotation.CheckForNull;

import java.io.File;
import java.io.IOException;

import static org.apache.maven.plugins.annotations.LifecyclePhase.*;
import static org.apache.maven.plugins.annotations.ResolutionScope.*;

//TODO: There is a serious correlation with Maven HPI's Plugin "custom-war" step. This step is actually used inside via Maven-in-Maven. May be reworked later.
/**
 * Mojo wrapper for {@link io.jenkins.tools.warpackager.lib.impl.Builder}.
 * @author Oleg Nenashev
 * @since TODO
 */
@Mojo(name="custom-war", defaultPhase = PACKAGE, requiresDependencyResolution = RUNTIME)
public class PackageMojo extends AbstractMojo {

    @Parameter
    public @CheckForNull String configFilePath;

    @Parameter
    public @CheckForNull String warVersion;

    @Parameter
    public @CheckForNull String tmpDir;

    @Parameter
    public @CheckForNull String mvnSettingsFile;

    @Parameter
    public boolean batchMode;

    @Component
    protected MavenProject project;

    @Component
    protected MavenProjectHelper projectHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (configFilePath == null) {
            throw new MojoExecutionException("Config file is not defined");
        }

        final Config cfg;
        try {
            cfg = Config.loadConfig(new File(configFilePath));
        } catch (IOException ex) {
            throw new MojoExecutionException("Cannot load configuration from " + configFilePath, ex);
        }

        cfg.buildSettings.setVersion(warVersion);
        if (mvnSettingsFile != null) {
            cfg.buildSettings.setMvnSettingsFile(new File(mvnSettingsFile));
        }
        if (tmpDir != null) {
            cfg.buildSettings.setTmpDir(new File(tmpDir));
        } else { // Use a Maven temporary dir
            //TODO: use step ID
            cfg.buildSettings.setTmpDir(new File(project.getBuild().getDirectory(), "custom-war-packager-maven-plugin"));
        }

        if (batchMode) {
            cfg.buildSettings.addMavenOption("--batch-mode");
        }

        final Builder bldr = new Builder(cfg);
        try {
            bldr.build();
        } catch (Exception ex) {
            throw new MojoExecutionException("Failed to build the custom WAR", ex);
        }

        projectHelper.attachArtifact(project, "war", cfg.getOutputWar());
    }

}
