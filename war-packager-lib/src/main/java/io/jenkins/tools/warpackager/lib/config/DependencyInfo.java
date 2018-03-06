package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.maven.model.Dependency;

import java.io.IOException;
import java.util.Map;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Deserialization")
public class DependencyInfo {
    public String groupId;
    public String artifactId;
    public SourceInfo source;

    public boolean isNeedsBuild() {
        return source != null && !source.isReleasedVersion();
    }

    public Dependency toDependency(Map<String,String> versionOverrides) throws IOException {

        Dependency dep = new Dependency();
        dep.setGroupId(groupId);
        dep.setArtifactId(artifactId);

        String version = versionOverrides.get(artifactId);
        if (version == null) {
            if (source == null || source.version == null) {
                throw new IOException("Source version has not been resolved: " + source);
            }
            version = source.version;
        }

        dep.setVersion(version);
        return dep;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, source);
    }
}
