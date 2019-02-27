package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
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
    public String type;
    @CheckForNull
    public SourceInfo source;
    @CheckForNull
    public DependencyBuildSettings build;

    public boolean isNeedsBuild() {
        return source != null && !source.isReleasedVersion();
    }

    public Dependency toDependency(Map<String,String> versionOverrides) throws IOException {

        Dependency dep = new Dependency();
        dep.setGroupId(groupId);
        dep.setArtifactId(artifactId);
        if (StringUtils.isNotEmpty(type)) {
            dep.setType(type);
        }

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

    @CheckForNull
    public SourceInfo getSource() {
        return source;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, source);
    }

    @Nonnull
    public DependencyBuildSettings getBuildSettings() {
        return build != null ? build : DependencyBuildSettings.DEFAULT;
    }
}
