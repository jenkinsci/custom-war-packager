package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

import static io.jenkins.tools.warpackager.lib.util.CollectionsHelper.putIfNotNull;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Deserialization")
public class PackageInfo {
    public String groupId;
    public String artifactId;

    @CheckForNull
    public String vendor;
    @CheckForNull
    public String title;
    @CheckForNull
    public String description;

    public void toKeyValueMap(@Nonnull Map<String, String> dest) {
        // TODO(oleg_nenashev): add support of Name from BOM? how is it different from artifactID, what are the requirements?
        dest.put("name", artifactId);
        dest.put("artifactId", artifactId);
        dest.put("groupId", groupId);
        putIfNotNull(dest, "vendor", vendor);
        putIfNotNull(dest, "title", title);
        putIfNotNull(dest, "description", description);
    }
}
