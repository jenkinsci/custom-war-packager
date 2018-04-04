package io.jenkins.tools.warpackager.lib.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.tools.warpackager.lib.config.BuildSettings;
import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import static io.jenkins.tools.warpackager.lib.util.SystemCommandHelper.processFor;
import static io.jenkins.tools.warpackager.lib.util.SystemCommandHelper.runFor;

/**
 * @author Oleg Nenashev
 */
public class MavenHelper {

    private Config cfg;

    public MavenHelper(Config cfg) {
        this.cfg = cfg;
    }

    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Passed parameter is true")
    public void run(File buildDir, String ... args) throws IOException, InterruptedException {
        run(buildDir, true, args);
    }

    @CheckReturnValue
    public int run(File buildDir, boolean failOnError, String ... args) throws IOException, InterruptedException {
        ArrayList<String> callArgs = new ArrayList<>();
        callArgs.add("mvn");
        File settingsFile = cfg.buildSettings != null ? cfg.buildSettings.getMvnSettingsFile() : null;
        if (settingsFile != null) {
            callArgs.add("-s");
            callArgs.add(settingsFile.getAbsolutePath());
        }
        Collections.addAll(callArgs, args);

        if (failOnError) {
            processFor(buildDir, callArgs.toArray(args));
            return 0;
        }
        return runFor(buildDir, callArgs.toArray(args));
    }

    public boolean artifactExists(File buildDir, DependencyInfo dep, String version, String packaging) throws IOException, InterruptedException {
        String gai = dep.groupId + ":" + dep.artifactId + ":" + version;
        int res = run(buildDir, false,"dependency:get",
                "-Dartifact=" + gai,
                "-Dpackaging=" + packaging,
                "-Dtransitive=false", "-o");
        return res == 0;
    }

    public void downloadArtifact(File buildDir, DependencyInfo dep, String version, File destination)
            throws IOException, InterruptedException {
        run(buildDir, "com.googlecode.maven-download-plugin:download-maven-plugin:1.4.0:artifact",
                "-DgroupId=" + dep.groupId,
                "-DartifactId=" + dep.artifactId,
                "-Dversion=" + version,
                "-DoutputDirectory=" + destination.getParentFile().getAbsolutePath(),
                "-DoutputFileName=" + destination.getName());
    }
}
