package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.GroovyHookInfo;
import io.jenkins.tools.warpackager.lib.config.SourceInfo;
import io.jenkins.tools.warpackager.lib.config.WARResourceInfo;
import io.jenkins.tools.warpackager.lib.model.bom.BOM;
import io.jenkins.tools.warpackager.lib.model.bom.ComponentReference;
import io.jenkins.tools.warpackager.lib.model.bom.Metadata;
import io.jenkins.tools.warpackager.lib.model.bom.Specification;
import io.jenkins.tools.warpackager.lib.util.SimpleManifest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds {@link BOM}.
 * @author Oleg Nenashev
 * @since TODO
 */
public class BOMBuilder {

    private final Config config;

    private static final Logger LOGGER = Logger.getLogger(BOMBuilder.class.getName());

    @CheckForNull
    private Map<String, String> versionOverrides;

    @CheckForNull
    private Map<String, ComponentReference> bundledPlugins;

    public BOMBuilder(@Nonnull Config config) {
        this.config = config;
    }

    public BOMBuilder withPluginsDir(File pluginsDir) throws IOException, InterruptedException {

        // Process dependencies
        if (pluginsDir != null) {
            LOGGER.log(Level.INFO, "Reading bundled files from {0}", pluginsDir);
            File[] plugins = pluginsDir.listFiles();
            if (plugins != null) {
                bundledPlugins = new HashMap<>();
                for (File plugin : plugins) {
                    if (plugin.getAbsolutePath().endsWith(".hpi")) {
                        final ComponentReference ref = SimpleManifest.readPluginManifest(plugin);
                        bundledPlugins.put(ref.getArtifactId(), ref);
                    }
                }
            }
        }

        return this;
    }

    public BOMBuilder withStatus(Map<String, String> versionOverrides) {
        this.versionOverrides = versionOverrides;
        return this;
    }

    public BOM build() throws IOException, InterruptedException {
        BOM bom = new BOM();

        LOGGER.log(Level.INFO, "Building BOM");
        bom.setMetadata(buildMetadata());
        bom.setSpec(buildSpec(false));
        if (versionOverrides != null) { // We can produce status
            LOGGER.log(Level.INFO, "Generating 'status' section of the BOM");
            bom.setStatus(buildSpec(true));
        }
        return bom;
    }

    private Metadata buildMetadata() {
        Map<String, String> labels = new HashMap<>();
        config.bundle.toKeyValueMap(labels);
        labels.put("version", config.buildSettings.getVersion());

        // TODO(oleg_nenashev): I have no idea what annotations are about, just trying non-production info
        // Metadata from WAR can be also added
        Map<String, String> annotations = new HashMap<>();
        annotations.put("builtBy", "Custom WAR Packager");

        Metadata metadata = new Metadata();
        metadata.setLabels(labels);
        metadata.setAnnotations(annotations);
        return metadata;
    }

    private Specification buildSpec(boolean overrideVersions) {
        Specification spec = new Specification();
        //TODO(oleg_nenashev): it will produce artifactId and groupId in BOM's [status/core]
        spec.setCore(ComponentReference.resolveFrom(config.war, overrideVersions, versionOverrides));

        // Plugins
        List<ComponentReference> plugins = new ArrayList<>();
        if (overrideVersions && bundledPlugins != null && !bundledPlugins.isEmpty()){
            // Writing 'status', put explicit versions for all bundled plugins
            for (Map.Entry<String, ComponentReference> bundled : bundledPlugins.entrySet()) {
                ComponentReference ref = bundled.getValue();

                DependencyInfo requiredPlugin = config.findPlugin(ref.getArtifactId());
                SourceInfo requiredVersionSource = requiredPlugin != null ? requiredPlugin.source : null;
                String requiredVersion = requiredVersionSource != null ? requiredVersionSource.version : null;

                // TODO: due to whatever reason timestamped Snapshots do not have full version in the manifest
                // Plugin-Version points to SNAPSHOT. So here we override it
                String bundledHPIVersion = ref.getVersion();
                if (requiredVersion != null && bundledHPIVersion != null &&
                        !requiredVersion.equals(bundledHPIVersion) && bundledHPIVersion.contains("-SNAPSHOT")) {
                    LOGGER.log(Level.WARNING, "Plugin {0}: Required version {1} differ from what is in the bundled HPI: {2}. " +
                            "Assuming that it is a timestamped snapshot, using specification value",
                            new Object[] {ref.getArtifactId(), requiredVersion, bundledHPIVersion});
                    ComponentReference override = new ComponentReference();
                    override.setGroupId(ref.getGroupId());
                    override.setArtifactId(ref.getArtifactId());
                    override.setVersion(requiredVersion);
                    ref = override;
                }
                plugins.add(ref);
            }
        } else if (config.plugins != null) {
            // We use default resolution
            for (DependencyInfo plugin : config.plugins) {
                plugins.add(ComponentReference.resolveFrom(plugin, overrideVersions, versionOverrides));
            }
        }
        spec.setPlugins(plugins);

        // Components - everything else
        //TODO(oleg_nenashev): BOM should support whatever type definition
        List<ComponentReference> components = new ArrayList<>();
        if (config.libPatches != null) {
            for (DependencyInfo dep : config.libPatches) {
                components.add(ComponentReference.resolveFrom(dep, overrideVersions, versionOverrides));
            }
        }
        if (config.groovyHooks != null) {
            for (WARResourceInfo extraResource : config.getAllExtraResources()) {
                components.add(toComponentReference(extraResource, overrideVersions));
            }
        }
        spec.setComponents(components);

        return spec;
    }

    private ComponentReference toComponentReference(WARResourceInfo hook, boolean overrideVersions) {
        //TODO(oleg_nenashev): no artifact IDs, some hacks here. Maybe groovy hooks should require standard fields
        DependencyInfo mockDependency = new DependencyInfo();
        mockDependency.groupId = "io.jenkins.tools.warpackager." + hook.getResourceType() + "." + hook.id;
        mockDependency.artifactId = hook.id;
        mockDependency.source = hook.source;

        ComponentReference ref = ComponentReference.resolveFrom(mockDependency, overrideVersions, versionOverrides);
        //TODO(oleg_nenashev): we cannot produce version
        if (overrideVersions && ref.getVersion() == null) {
            ref.setVersion("unknown");
        }
        return ref;
    }
}
