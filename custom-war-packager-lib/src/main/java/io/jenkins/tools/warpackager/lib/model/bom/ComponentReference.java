package io.jenkins.tools.warpackager.lib.model.bom;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "Fields come from JSON")
public class ComponentReference extends Reference {

    //TODO: propoperties are not required for the core

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
}
