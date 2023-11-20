package io.jenkins.tools.warpackager.lib.model.bom;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.SourceInfo;
import io.jenkins.tools.warpackager.lib.config.WarInfo;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.Map;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "Fields come from JSON")
public class ComponentReference extends Reference {

    //TODO: properties are not required for the core

    @NonNull
  //  @JsonProperty(required = true)
    String groupId;

    @NonNull
  //  @JsonProperty(required = true)
    String artifactId;

    @NonNull
    public String getArtifactId() {
        return artifactId;
    }

    @NonNull
    public String getGroupId() {
        return groupId;
    }

    public void setArtifactId(@NonNull String artifactId) {
        this.artifactId = artifactId;
    }

    public void setGroupId(@NonNull String groupId) {
        this.groupId = groupId;
    }

    public WarInfo toWARDependencyInfo() {
        WarInfo dep = new WarInfo();
        dep.groupId = "org.jenkins-ci.main";
        dep.artifactId = "jenkins-war";

        dep.source = new SourceInfo();
        if (version != null) {
            dep.source.version = version;
        } else {
            //TODO(oleg_nenashev): we have to interpolate Git repo now, not defined in BOM
            dep.source.git = "https://github.com/jenkinsci/" + dep.artifactId + "-plugin.git";
            dep.source.branch = ref; // It may be actually commit as well, but the CWP's logic will work
        }

        if (dir != null) {
            dep.source.dir = dir;
        }
        return dep;
    }

    public DependencyInfo toDependencyInfo() {
        DependencyInfo dep = new DependencyInfo();
        dep.groupId = groupId;
        dep.artifactId = artifactId;

        dep.source = new SourceInfo();
        if (version != null) {
            dep.source.version = version;
        } else {
            //TODO(oleg_nenashev): we have to interpolate Git repo now, not defined in BOM
            dep.source.git = "https://github.com/jenkinsci/" + dep.artifactId + "-plugin.git";
            dep.source.branch = ref; // It may be actually commit as well, but the CWP's logic will work
        }

        if (dir != null) {
            dep.source.dir = dir;
        }
        return dep;
    }

    @NonNull
    public static ComponentReference resolveFrom(@NonNull DependencyInfo dep) {
        return resolveFrom(dep, false, null);
    }

    @NonNull
    public static ComponentReference resolveFrom(@NonNull DependencyInfo dep, boolean overrideVersions,
                                                  @CheckForNull Map<String, String> versionOverrides) {
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
}
