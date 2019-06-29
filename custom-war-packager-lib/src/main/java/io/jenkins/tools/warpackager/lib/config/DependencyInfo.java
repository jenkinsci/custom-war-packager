package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

/**
 * Provides information about dependencies in Custom WAR Packager.
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Deserialization")
public class DependencyInfo {

    /**
     * GroupId of the dependency.
     * May be {@code null} when not resolved.
     * Many components like {@link io.jenkins.tools.warpackager.lib.impl.BOMBuilder} and
     * {@link io.jenkins.tools.warpackager.lib.impl.MavenHPICustomWARPOMGenerator} require fully qualified dependencies,
     * and such methods may crash when groupId cannot be resolved.
     */
    @CheckForNull
    private String groupId;
    public String artifactId;
    public String type;
    @CheckForNull
    public SourceInfo source;
    @CheckForNull
    public DependencyBuildSettings build;

    /**
     * Sets a new groupId for the dependency.
     * @param groupId Group ID to set
     * @since 2.0.0
     */
    public void setGroupId(@Nonnull String groupId) {
        this.groupId = groupId;
    }

    /**
     * Retrieves groupId of the dependency
     * @throws ConfigException groupId cannot be resolved
     * @since 2.0.0
     */
    @Nonnull
    public String getGroupId() throws ConfigException {
        if (groupId == null) {
            throw new ConfigException("Group ID is not defined for the dependency: " + this);
        }
        return groupId;
    }

    public boolean isNeedsBuild() {
        return source != null && !source.isReleasedVersion();
    }

    /**
     * Converts the relaxed Custom WAR packager definition to a strict Maven definition.
     * @param versionOverrides Version overrides registry
     * @return Maven dependency with resolved g:a:v
     * @throws IOException Cannot resolve the dependency g:a:v
     */
    public Dependency toDependency(@Nonnull Map<String,String> versionOverrides) throws IOException {

        Dependency dep = new Dependency();
        dep.setGroupId(getGroupId());
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
        return String.format("%s:%s:%s", groupId != null ? groupId : "unknown", artifactId, source);
    }

    @Nonnull
    public DependencyBuildSettings getBuildSettings() {
        return build != null ? build : DependencyBuildSettings.DEFAULT;
    }
}
