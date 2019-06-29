package io.jenkins.tools.warpackager.lib.model;

import hudson.util.VersionNumber;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Fully resolved dependency.
 * This class uses {@link io.jenkins.tools.warpackager.lib.config.DependencyInfo} as a base and also
 * contains resolution results.
 * All resolved dependencies have a fully qualified g:a:v.
 * @since TODO
 * @see ResolvedWARDependency
 * @see ResolvedPluginDependency
 * @see ResolvedLibraryDependency
 * @see ResolvedResourceDependency
 */
public class ResolvedDependency {

    private final @Nonnull String groupId;
    private final @Nonnull String artifactId;
    //TODO: Use version info?
    private final @Nonnull VersionNumber version;
    private final @CheckForNull String type;

    private final @Nonnull DependencyInfo originalDependency;

    public ResolvedDependency(@Nonnull String groupId, @Nonnull VersionNumber version,
                              @Nonnull DependencyInfo originalDependency) {
        this.groupId = groupId;
        this.artifactId = originalDependency.artifactId;
        this.version = version;
        this.type = originalDependency.type;
        this.originalDependency = originalDependency;
    }

    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    @Nonnull
    public String getArtifactId() {
        return artifactId;
    }

    @Nonnull
    public VersionNumber getVersion() {
        return version;
    }

    @CheckForNull
    public String getType() {
        return type;
    }

    @Nonnull
    public DependencyInfo getOriginalDependency() {
        return originalDependency;
    }

    /**
     * Converts the relaxed Custom WAR packager definition to a strict Maven definition.
     * @return Maven dependency with resolved g:a:v
     * @throws IOException Cannot resolve the dependency g:a:v
     */
    public Dependency toMavenDependency() throws IOException {

        Dependency dep = new Dependency();
        dep.setGroupId(groupId);
        dep.setArtifactId(artifactId);
        if (StringUtils.isNotEmpty(type)) {
            dep.setType(type);
        }

        dep.setVersion(version.toString());
        return dep;
    }
}
