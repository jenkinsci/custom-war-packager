package io.jenkins.tools.warpackager.lib.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches all dependencies which are being included into the target WAR file.
 */
public class ResolvedDependencies {

    private @Nonnull ResolvedWARDependency war;
    private Map<String, ResolvedPluginDependency> plugins = new HashMap<>();
    private Map<String, ResolvedLibraryDependency> libraries = new HashMap<>();
    private Map<String, ResolvedResourceDependency> resources = new HashMap<>();

    public ResolvedDependencies(@Nonnull ResolvedWARDependency war) {
        this.war = war;
    }

    @Nonnull
    public ResolvedWARDependency getWar() {
        return war;
    }

    @Nonnull
    public Collection<ResolvedLibraryDependency> getLibraries() {
        return libraries.values();
    }

    @CheckForNull
    public ResolvedLibraryDependency getLibrary(@Nonnull String artifactId) {
        return libraries.get(artifactId);
    }

    public void addLibrary(@Nonnull ResolvedDependency lib) {
        libraries.put(lib.getArtifactId(), new ResolvedLibraryDependency(lib));
    }

    @CheckForNull
    public ResolvedPluginDependency getPlugin(@Nonnull String artifactId) {
        return plugins.get(artifactId);
    }

    @Nonnull
    public Collection<ResolvedPluginDependency> getPlugins() {
        return plugins.values();
    }

    public void addPlugin(@Nonnull ResolvedDependency plugin) {
        plugins.put(plugin.getArtifactId(), new ResolvedPluginDependency(plugin));
    }

    @Nonnull
    public Collection<ResolvedResourceDependency> getResources() {
        return resources.values();
    }

    public void addResource(@Nonnull ResolvedResourceDependency resource) {
        resources.put(resource.originalDependency.id, resource);
    }
}
