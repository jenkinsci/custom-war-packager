package io.jenkins.tools.warpackager.lib.util;

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
 * Builds Docker images for WAR.
 * This method implies that the base Docker image is compatible with the standard {@code jenkins/jenkins} image.
 * @author Oleg Nenashev
 */
public abstract class DockerfileBuilder {

    protected final Config config;
    protected final DockerBuildSettings dockerSettings;
    protected final File outputDir;

    private static final Logger LOGGER = Logger.getLogger(DockerfileBuilder.class.getName());

    public DockerfileBuilder(@Nonnull Config config,
                             @Nonnull DockerBuildSettings dockerSettings,
                             @Nonnull File outputDir) throws IOException {
        this.config = config;
        this.dockerSettings = dockerSettings;
        if (dockerSettings == null) {
            throw new IOException("Docker settings are not defined");
        }
        this.outputDir = outputDir;
    }

    /**
     * Builds Dockerfile and prepares all resources
     */
    public void build() throws IOException, InterruptedException {
        LOGGER.log(Level.INFO, "Generating Dockerfile");
        String dockerfile = generateDockerfile();
        try(FileOutputStream ostream = new FileOutputStream(new File(outputDir, "Dockerfile"))) {
            IOUtils.write(dockerfile, ostream, "UTF-8");
        }

        if (!dockerSettings.isBuild()) {
            return;
        }
        String tag = dockerSettings.getTag();
        if (tag == null) {
            throw new IOException("Cannot build Docker image, tag is not defined");
        }
        LOGGER.log(Level.INFO, "Building Docker image {0}", tag);
        if (dockerSettings.isBuildx()) {
            String output;
            switch (dockerSettings.getOutput()) {
                case "push":
                    // the image push into remote registry directly
                    output = "--push";
                    break;
                case "load":
                    // store the docker image into local docker daemon, it can be listed via docker images
                    output = "--load";
                    break;
                default:
                    // the image exists as a local cache, it cannot be listed via docker images
                    output = "";
                    break;
            }
            SystemCommandHelper.processFor(outputDir, "docker", "buildx", "build", "--platform",
                    dockerSettings.getPlatform(), output, "-t", tag, ".");
        } else {
            SystemCommandHelper.processFor(outputDir, "docker", "build", "-t", tag, ".");
        }
    }

    protected abstract String generateDockerfile() throws IOException, InterruptedException;
}
