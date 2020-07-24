package io.jenkins.tools.warpackager.lib.impl.jenkinsfileRunner;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DockerBuildSettings;
import io.jenkins.tools.warpackager.lib.util.DockerfileBuilder;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Builds Docker image of Jenkinsfile Runner
 * @author Oleg Nenashev
 * @since TODO
 */
public class JenkinsfileRunnerDockerBuilder extends DockerfileBuilder {

    @CheckForNull
    private Map<String, String> versionOverrides;

    @CheckForNull
    private File pluginsDir;

    @CheckForNull
    private String runWorkspace;

    private boolean noSandbox;

    public JenkinsfileRunnerDockerBuilder(@Nonnull Config config,
                                          @Nonnull DockerBuildSettings dockerBuildSettings,
                                          @Nonnull File targetDir) throws IOException {
        super(config, dockerBuildSettings, targetDir);
    }

    public JenkinsfileRunnerDockerBuilder withPlugins(@Nonnull File pluginsDir) {
        this.pluginsDir = pluginsDir;
        return this;
    }

    public JenkinsfileRunnerDockerBuilder withVersionOverrides(Map<String, String> versionOverrides) {
        this.versionOverrides = versionOverrides;
        return this;
    }

    public JenkinsfileRunnerDockerBuilder withRunWorkspace(@Nonnull String runWorkspace) {
        this.runWorkspace = runWorkspace;
        return this;
    }

    public JenkinsfileRunnerDockerBuilder withNoSandbox(boolean noSandbox) {
        this.noSandbox = noSandbox;
        return this;
    }

    @Override
    public void build() throws IOException, InterruptedException {
        if (pluginsDir != null) {
            org.apache.commons.io.FileUtils.copyDirectory(
                    pluginsDir,
                    new File(outputDir, "plugins"));
        }

        super.build();
    }

    @Override
    protected String generateDockerfile() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps;
        try {
            ps = new PrintStream(baos, true, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        
        ps.println("FROM " + dockerSettings.getBase());
        // TODO(oleg_nenashev): probably this is the most crappy Dockerfile Generation logic you have ever seen
        ps.println("USER root");
        ps.println("ADD target/" + config.getOutputWar().getName() + " /usr/share/jenkins/jenkins.war");
        ps.println("RUN mkdir /app && unzip /usr/share/jenkins/jenkins.war -d /app/jenkins");
        ps.println("COPY jenkinsfileRunner /app");
        //TODO(oleg_nenashev): Plugins dir is empty to prevent the logic from crashing, plugins are actually bundled inside WAR
        ps.println("RUN chmod +x /app/bin/jenkinsfile-runner && mkdir -p /usr/share/jenkins/ref/plugins");

        //TODO: we can copy it from exploded WAR instead
        if (pluginsDir != null) {
            ps.println("COPY plugins /usr/share/jenkins/ref/plugins");
        }

        if (config.groovyHooks != null) {
            ps.println("RUN cp -R /app/jenkins/WEB-INF/*.groovy.d /usr/share/jenkins/ref/");
        }

        ps.println();

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ENTRYPOINT [\"/app/bin/jenkinsfile-runner\",\\\n" +
                "            \"-w\", \"/app/jenkins\",\\\n" +
                "            \"-p\", \"/usr/share/jenkins/ref/plugins\",\\\n" +
                //TODO(oleg_nenashev): There is a glitch in the stock Dockerfile in the repo,
                // it should point to Jenkinsfile to prevent using / as a Filesystem SCM root
                "            \"-f\", \"/workspace/Jenkinsfile\"");

        if (runWorkspace != null) {
            stringBuilder.append(",\\\n            \"--runWorkspace\", \"");
            stringBuilder.append(runWorkspace);
            stringBuilder.append("\"");
        }

        if (noSandbox) {
            stringBuilder.append(",\\\n            \"--no-sandbox\"");
        }

        stringBuilder.append("]");

        ps.println(stringBuilder.toString());

        String dockerfile = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        return dockerfile;
    }


}
