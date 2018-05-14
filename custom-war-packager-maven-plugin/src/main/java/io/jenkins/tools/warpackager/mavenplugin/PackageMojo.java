package io.jenkins.tools.warpackager.mavenplugin;

import io.jenkins.tools.warpackager.lib.config.Config;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;

import static org.apache.maven.plugins.annotations.LifecyclePhase.*;
import static org.apache.maven.plugins.annotations.ResolutionScope.*;

//TODO: There is a serious correlation with Maven HPI's Plugin "custom-war" step. This step is actually used inside via Maven-in-Maven. May be reworked later.
/**
 * Mojo wrapper for {@link io.jenkins.tools.warpackager.lib.impl.Builder}.
 * @author Oleg Nenashev
 * @since TODO
 */
@Mojo(name="custom-war", defaultPhase = PACKAGE, requiresDependencyResolution = RUNTIME)
public class PackageMojo extends BuildMojo {

    @Component
    protected MavenProject project;

    @Component
    protected MavenProjectHelper projectHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File buildDir = new File(project.getBuild().getDirectory(), "custom-war-packager-maven-plugin");
        Config cfg = getConfigOrFail();

        build(cfg, buildDir);

        projectHelper.attachArtifact(project, "war", cfg.getOutputWar());
        projectHelper.attachArtifact(project, "yml", "bom", cfg.getOutputBOM());
    }
}
