package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

/**
 * A war definition can contain only dependency info or also libraries
 */

@SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", justification = "JSON Deserialization")
public class WarInfo extends DependencyInfo {

    public List<LibraryInfo> libraries;

    @Override
    public boolean isNeedsBuild() {
        if (libraries != null && libraries.size() > 0 && source != null && source.getType().equals(SourceInfo.Type.MAVEN_REPO)){
            throw new IllegalArgumentException("war libraries are not supported for released versions of jenkins war, use a git source instead");
        }
        return super.isNeedsBuild() || isLibrariesNeeded();
    }

    public boolean isLibrariesNeeded() {
        if (libraries == null || libraries.size() == 0) {
            return false;
        } else {
            return true;
        }
    }
}
