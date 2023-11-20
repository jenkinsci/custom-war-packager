package io.jenkins.tools.warpackager.lib.model.bom;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.tools.warpackager.lib.config.PackageInfo;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static io.jenkins.tools.warpackager.lib.util.CollectionsHelper.getOrFail;

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

    @NonNull
    public Map<String, String> getAnnotations() {
        return annotations != null ? annotations : Collections.emptyMap();
    }

    @NonNull
    public Map<String, String> getLabels() {
        return labels != null ? labels : Collections.emptyMap();
    }

    @CheckForNull
    public PackageInfo toPackageInfo() throws IOException {
        if (labels == null) {
            return null;
        }

        PackageInfo pi = new PackageInfo();
        pi.groupId = getOrFail(labels, "groupId", "BOM Metadata labels");
        pi.artifactId = getOrFail(labels, "artifactId", "BOM Metadata labels");
        pi.vendor = labels.get("vendor");
        pi.description = labels.getOrDefault("description", labels.get("name"));
        pi.title = labels.getOrDefault("title", labels.get("name"));
        return pi;
    }
}
