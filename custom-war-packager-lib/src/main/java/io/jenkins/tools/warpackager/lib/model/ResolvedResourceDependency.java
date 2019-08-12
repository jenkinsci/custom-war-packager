package io.jenkins.tools.warpackager.lib.model;

import hudson.util.VersionNumber;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.WARResourceInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * Fully resolved resource dependency.
 * The dependency also contains path to a local resource in the filesystem.
 * @since TODO
 */
public class ResolvedResourceDependency {

    @Nonnull
    final File resourcePath;
    @Nonnull
    final WARResourceInfo originalDependency;

    public ResolvedResourceDependency(@Nonnull File resourcePath,
                                      @Nonnull WARResourceInfo originalDependency) {
        this.resourcePath = resourcePath;
        this.originalDependency = originalDependency;
    }

    @Nonnull
    public File getResourcePath() {
        return resourcePath;
    }

    @Nonnull
    public WARResourceInfo getOriginalDependency() {
        return originalDependency;
    }
}
