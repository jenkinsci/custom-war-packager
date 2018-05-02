package io.jenkins.tools.warpackager.lib.model.bom;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Oleg Nenashev
 * @since TODO
 */
public class Metadata {

    @CheckForNull
    @JsonProperty
    Map<String, String> labels;

    @CheckForNull
    @JsonProperty
    Map<String, String> annotations;

    public void setAnnotations(@CheckForNull Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public void setLabels(@CheckForNull Map<String, String> labels) {
        this.labels = labels;
    }

    @Nonnull
    public Map<String, String> getAnnotations() {
        return annotations != null ? annotations : Collections.emptyMap();
    }

    @Nonnull
    public Map<String, String> getLabels() {
        return labels != null ? labels : Collections.emptyMap();
    }
}
