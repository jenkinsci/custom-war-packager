package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.CheckForNull;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Deserialization")
public class SourceInfo {
    public String version;
    public String git;
    public String branch;
    public String commit;
    @CheckForNull
    public String dir;

    public boolean isReleasedVersion() {
        return version != null;
    }

    public Type getType() {
        if (version != null) {
            return Type.MAVEN_REPO;
        }

        if (git != null) {
            return Type.GIT;
        }

        if (dir != null) {
            return Type.FILESYSTEM;
        }

        return Type.UNKNOWN;
    }

    //TODO(oleg_nenashev): rename to getRef()
    @CheckForNull
    public String getCheckoutId() {
        return commit != null ? commit : branch;
    }

    @Override
    public String toString() {
        switch (getType()) {
            case MAVEN_REPO:
                return version;
            case GIT:
                return String.format("git: %s, checkout: %s", git, getCheckoutId());
            case FILESYSTEM:
                return String.format("filesystem: %s", dir);
            default:
                return "unknown source";
        }
    }

    public enum Type {
        MAVEN_REPO,
        GIT,
        FILESYSTEM,
        UNKNOWN
    }
}
