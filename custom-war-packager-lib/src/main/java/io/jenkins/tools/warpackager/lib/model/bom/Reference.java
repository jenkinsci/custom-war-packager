package io.jenkins.tools.warpackager.lib.model.bom;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
public class Reference {

    @CheckForNull
    String ref;

    @CheckForNull
    String version;

    @CheckForNull
    String dir;

    public void setVersion(@CheckForNull String version) {
        this.version = version;
    }

    public void setRef(@CheckForNull String ref) {
        this.ref = ref;
    }

    public void setDir(@CheckForNull String dir) {
        this.dir = dir;
    }

    @CheckForNull
    public String getRef() {
        return ref;
    }

    @CheckForNull
    public String getVersion() {
        return version;
    }

    @CheckForNull
    public String getDir() {
        return dir;
    }
}
