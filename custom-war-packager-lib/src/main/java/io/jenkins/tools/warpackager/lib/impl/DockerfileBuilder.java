package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DockerBuildSettings;
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
 * @author Oleg Nenashev
 * @since TODO
 */
public class DockerfileBuilder {

    private final Config config;
    private final DockerBuildSettings dockerSettings;
    private final File outputDir;

    private static final Logger LOGGER = Logger.getLogger(DockerfileBuilder.class.getName());

    public DockerfileBuilder(@Nonnull Config config) throws IOException {
        this.config = config;
        this.dockerSettings = config.buildSettings.getDocker();
        if (dockerSettings == null) {
            throw new IOException("Docker settings are not defined");
        }
        this.outputDir = config.buildSettings.getOutputDir();
    }

    public DockerfileBuilder withPlugins(@Nonnull File pluginsDir) {
        if (!pluginsDir.exists()) {
            LOGGER.log(Level.INFO, "No plugins to include");
            return this;
        }

        LOGGER.log(Level.INFO, "Plugins are included into the WAR file, no need to copy them");
        return this;
    }

    public DockerfileBuilder withInitScripts(@Nonnull File rootDir) {
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

    /**
     * Builds Dockerfile and prepares all resources
     */
    public void build() throws IOException {
        String dockerfile = generateDockerfile();
        try(FileOutputStream ostream = new FileOutputStream(new File(outputDir, "Dockerfile"))) {
            IOUtils.write(dockerfile, ostream, "UTF-8");
        }
    }

    private String generateDockerfile() {
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
