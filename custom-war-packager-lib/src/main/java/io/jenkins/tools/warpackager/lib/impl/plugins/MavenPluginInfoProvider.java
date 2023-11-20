package io.jenkins.tools.warpackager.lib.impl.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.config.SourceInfo;
import io.jenkins.tools.warpackager.lib.model.plugins.PluginInfoProvider;
import io.jenkins.tools.warpackager.lib.util.MavenHelper;

import java.io.File;
import java.io.IOException;

/**
 * Retrieves information about plugins from a Maven repository.
 * The implementation uses local caches when needed.
 */
public class MavenPluginInfoProvider implements PluginInfoProvider {

    private MavenHelper helper;
    private File tmpDir;

    public MavenPluginInfoProvider(@NonNull MavenHelper helper, @NonNull File tmpDir) {
        this.helper = helper;
        this.tmpDir = tmpDir;
    }

    @Override
    public boolean isPlugin(@NonNull DependencyInfo dep) throws IOException, InterruptedException {
        SourceInfo sourceInfo = dep.getSource();
        if (sourceInfo == null) {
            throw new IOException("No Source info provided for " + dep);
        }
        return helper.artifactExistsInLocalCache(dep, sourceInfo.version, "hpi") ||
                helper.artifactExists(tmpDir, dep, sourceInfo.version, "hpi");
    }
}
