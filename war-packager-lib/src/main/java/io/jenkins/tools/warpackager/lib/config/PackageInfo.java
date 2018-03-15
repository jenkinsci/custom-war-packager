package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.CheckForNull;

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
}
