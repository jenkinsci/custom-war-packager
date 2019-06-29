package io.jenkins.tools.warpackager.lib.model.bom;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.SourceInfo;
import io.jenkins.tools.warpackager.lib.config.WarInfo;
import io.jenkins.tools.warpackager.lib.model.ResolvedDependency;

import javax.annotation.Nonnull;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "Fields come from JSON")
public class ComponentReference extends Reference {

    //TODO: properties are not required for the core

    @Nonnull
  //  @JsonProperty(required = true)
    String groupId;

    @Nonnull
  //  @JsonProperty(required = true)
    String artifactId;

    @Nonnull
    public String getArtifactId() {
        return artifactId;
    }

    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    public void setArtifactId(@Nonnull String artifactId) {
        this.artifactId = artifactId;
    }

    public void setGroupId(@Nonnull String groupId) {
        this.groupId = groupId;
    }

    public WarInfo toWARDependencyInfo() {
        WarInfo dep = new WarInfo();
        dep.setGroupId("org.jenkins-ci.main");
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
        dep.setGroupId(groupId);
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

    @Nonnull
    public static ComponentReference fromResolvedDependency(@Nonnull ResolvedDependency dep, boolean useResolvedVersion) {
        ComponentReference ref = new ComponentReference();
        ref.setGroupId(dep.getGroupId());
        ref.setArtifactId(dep.getArtifactId());

        DependencyInfo original = dep.getOriginalDependency();
        if (original.source != null) {
            ref.setRef(original.source.getCheckoutId());
            String versionToReference = useResolvedVersion ? dep.getVersion().toString() : original.source.version;
            ref.setVersion(versionToReference);
        }

        return ref;
    }
}
