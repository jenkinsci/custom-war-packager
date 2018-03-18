package io.jenkins.tools.warpackager.lib.util;

import io.jenkins.tools.warpackager.lib.config.DependencyInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static io.jenkins.tools.warpackager.lib.util.SystemCommandHelper.processFor;
import static io.jenkins.tools.warpackager.lib.util.SystemCommandHelper.runFor;

/**
 * @author Oleg Nenashev
 */
public class MavenHelper {

    private MavenHelper() {}

    public static boolean artifactExists(File buildDir, DependencyInfo dep, String version) throws IOException, InterruptedException {
        String gai = dep.groupId + ":" + dep.artifactId + ":" + version;
        int res = runFor(buildDir, "mvn", "dependency:get", "-Dartifact=" + gai, "-Dtransitive=false", "-o");
        return res == 0;
    }

    public static void downloadArtifact(File buildDir, DependencyInfo dep, String version, File destination)
            throws IOException, InterruptedException {
        String gai = dep.groupId + ":" + dep.artifactId + ":" + version;
        processFor(buildDir, "mvn", "com.googlecode.maven-download-plugin:download-maven-plugin:1.4.0:artifact",
                "-DgroupId=" + dep.groupId,
                "-DartifactId=" + dep.artifactId,
                "-Dversion=" + version,
                "-DoutputDirectory=" + destination.getParentFile().getAbsolutePath(),
                "-DoutputFileName=" + destination.getName());
    }
}
