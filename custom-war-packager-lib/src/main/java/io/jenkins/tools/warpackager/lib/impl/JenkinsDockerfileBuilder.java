package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DockerBuildSettings;
import io.jenkins.tools.warpackager.lib.util.DockerfileBuilder;
import io.jenkins.tools.warpackager.lib.util.SystemCommandHelper;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds Docker images for WAR.
 * This method implies that the base Docker image is compatible with the standard {@code jenkins/jenkins} image.
 * @author Oleg Nenashev
 */
public class JenkinsDockerfileBuilder extends DockerfileBuilder {

    private static final Logger LOGGER = Logger.getLogger(JenkinsDockerfileBuilder.class.getName());

    public JenkinsDockerfileBuilder(@Nonnull Config config,
                                    @Nonnull DockerBuildSettings docker,
                                    @Nonnull File outputDir) throws IOException {
        super(config,
              docker,
              outputDir);
    }

    public JenkinsDockerfileBuilder withPlugins(@Nonnull File pluginsDir) {
        if (!pluginsDir.exists()) {
            LOGGER.log(Level.INFO, "No plugins to include");
            return this;
        }

        LOGGER.log(Level.INFO, "Plugins are included into the WAR file, no need to copy them");
        return this;
    }

    public JenkinsDockerfileBuilder withInitScripts(@Nonnull File rootDir) {
        final File[] files = rootDir.listFiles();
        if (files == null || files.length == 0) {
            return this; // never happens anyway
        }

        for (File dir : files) {
            if (dir.getName().endsWith(".groovy.d")) {
                LOGGER.log(Level.INFO, "Discovered hooks: {0}", dir.getName());
                // Do nothing anyway, WAR file is packaged. They will be executed on startup
            }
        }

        return this;
    }

    protected String generateDockerfile() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps;
        try {
            ps = new PrintStream(baos, true, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        ps.println("FROM " + dockerSettings.getBase());

        // Labels
        StringBuilder labelString = new StringBuilder("LABEL Version=\"");
        labelString.append(config.buildSettings.getVersion());
        labelString.append("\"");
        if (config.bundle.description != null) {
            labelString.append("Description=\"");
            labelString.append(config.bundle.description);
            labelString.append("\"");
        }
        if (config.bundle.vendor != null) {
            labelString.append("Vendor=\"");
            labelString.append(config.bundle.vendor);
            labelString.append("\"");
        }
        ps.println(labelString);

        // Core
        ps.println("ADD target/" + config.getOutputWar().getName() + " /usr/share/jenkins/jenkins.war");

        ps.println("ENTRYPOINT [\"tini\", \"--\", \"/usr/local/bin/jenkins.sh\"]");
        String dockerfile = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        return dockerfile;
    }
}
