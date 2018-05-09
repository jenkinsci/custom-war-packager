package io.jenkins.tools.warpackager.mavenplugin;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.impl.Builder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;
import static org.apache.maven.plugins.annotations.ResolutionScope.RUNTIME;

//TODO: There is a serious correlation with Maven HPI's Plugin "custom-war" step. This step is actually used inside via Maven-in-Maven. May be reworked later.

/**
 * Mojo wrapper for {@link Builder}.
 * @author Oleg Nenashev
 * @since TODO
 */
@Mojo(name="build", defaultPhase = PACKAGE, requiresProject = false)
public class BuildMojo extends AbstractMojo {

    @Parameter(property = "configFile", required = true)
    public @CheckForNull String configFilePath;

    @Parameter(property = "version")
    public @CheckForNull String warVersion;

    @Parameter(property = "tmpDir")
    public @CheckForNull String tmpDir;

    @Parameter(property = "mvnSettingsFile")
    public @CheckForNull String mvnSettingsFile;

    @Parameter(property = "bomFile")
    public @CheckForNull String bom;

    @Parameter(property = "environment")
    public @CheckForNull String environment;

    @Parameter(property = "batchMode")
    public boolean batchMode;

    @Parameter(property = "installArtifacts")
    public boolean installArtifacts;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Config cfg = getConfigOrFail();
        build(cfg, new File("tmp"));
    }

    protected void build(@Nonnull Config cfg, @Nonnull File buildDir) throws MojoExecutionException {
        cfg.buildSettings.setVersion(warVersion);
        cfg.buildSettings.setInstallArtifacts(installArtifacts);
        if (mvnSettingsFile != null) {
            cfg.buildSettings.setMvnSettingsFile(new File(mvnSettingsFile));
        }
        if (tmpDir != null) {
            cfg.buildSettings.setTmpDir(new File(tmpDir));
        } else { // Use a Maven temporary dir
            //TODO: use step ID
            cfg.buildSettings.setTmpDir(buildDir);
        }

        if (batchMode) {
            cfg.buildSettings.addMavenOption("--batch-mode");
            cfg.buildSettings.addMavenOption("-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn");
        }

        // BOM
        cfg.buildSettings.setEnvironmentName(environment);
        if (bom != null) {
            cfg.buildSettings.setBOM(new File(bom));
        }

        final Builder bldr = new Builder(cfg);
        try {
            bldr.build();
        } catch (Exception ex) {
            throw new MojoExecutionException("Failed to build the custom WAR", ex);
        }
    }

    protected Config getConfigOrFail() throws MojoExecutionException {
        if (configFilePath == null) {
            throw new MojoExecutionException("Config file is not defined");
        }

        final Config cfg;
        try {
            cfg = Config.loadConfig(new File(configFilePath));
        } catch (IOException ex) {
            throw new MojoExecutionException("Cannot load configuration from " + configFilePath, ex);
        }
        return cfg;
    }

}
