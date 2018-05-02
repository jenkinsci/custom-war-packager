package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.GroovyHookInfo;
import io.jenkins.tools.warpackager.lib.config.SourceInfo;
import io.jenkins.tools.warpackager.lib.model.bom.BOM;
import io.jenkins.tools.warpackager.lib.model.bom.ComponentReference;
import io.jenkins.tools.warpackager.lib.model.bom.Metadata;
import io.jenkins.tools.warpackager.lib.model.bom.Specification;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds {@link BOM}.
 * @author Oleg Nenashev
 * @since TODO
 */
public class BOMBuilder {

    private final Config config;

    @CheckForNull
    private Map<String, String> versionOverrides;

    public BOMBuilder(@Nonnull Config config) {
        this.config = config;
    }

    public BOMBuilder withStatus(Map<String, String> versionOverrides) {
        this.versionOverrides = versionOverrides;
        return this;
    }

    public BOM build() throws IOException {
        BOM bom = new BOM();

        bom.setMetadata(buildMetadata());
        bom.setSpec(buildSpec(false));
        if (versionOverrides != null) { // We can produce status
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
        spec.setCore(toComponentReference(config.war, overrideVersions));

        List<ComponentReference> plugins = new ArrayList<>();
        if (config.plugins != null) {
            for (DependencyInfo plugin : config.plugins) {
                plugins.add(toComponentReference(plugin, overrideVersions));
            }
        }
        spec.setPlugins(plugins);

        //TODO(oleg_nenashev): BOM should support whatever type definition
        List<ComponentReference> components = new ArrayList<>();
        if (config.libPatches != null) {
            for (DependencyInfo dep : config.libPatches) {
                components.add(toComponentReference(dep, overrideVersions));
            }
        }
        if (config.groovyHooks != null) {
            for (GroovyHookInfo hookInfo : config.groovyHooks) {
                components.add(toComponentReference(hookInfo, overrideVersions));
            }
        }
        spec.setComponents(components);

        return spec;
    }

    private ComponentReference toComponentReference(DependencyInfo dep, boolean overrideVersions) {
        ComponentReference ref = new ComponentReference();
        ref.setGroupId(dep.groupId);
        ref.setArtifactId(dep.artifactId);
        //TODO(oleg_nenashev): BOM says "the realized BoM after refs are resolved" when versions are resolved
        if (dep.source == null) {
            throw new IllegalStateException("Source is not defined for dependency " + dep);
        }

        // Not putting ref then
        ref.setRef(dep.source.getCheckoutId());
        String effectiveVersion = dep.source.version;
        if (overrideVersions && versionOverrides != null && versionOverrides.containsKey(dep.artifactId)) {
            effectiveVersion = versionOverrides.get(dep.artifactId);
        }
        ref.setVersion(effectiveVersion);

        return ref;
    }

    private ComponentReference toComponentReference(GroovyHookInfo hook, boolean overrideVersions) {
        //TODO(oleg_nenashev): no artifact IDs, some hacks here. Maybe groovy hooks should require standard fields
        DependencyInfo mockDependency = new DependencyInfo();
        mockDependency.groupId = "io.jenkins.tools.warpackager.hooks." + hook.type;
        mockDependency.artifactId = hook.id;
        mockDependency.source = hook.source;

        ComponentReference ref = toComponentReference(mockDependency, overrideVersions);
        //TODO(oleg_nenashev): we cannot produce version
        if (overrideVersions && ref.getVersion() == null) {
            ref.setVersion("unknown");
        }
        return ref;
    }
}
