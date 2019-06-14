package io.jenkins.tools.warpackager.lib.impl.plugins;

import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.model.plugins.PluginInfoProvider;
import io.jenkins.tools.warpackager.lib.util.MavenHelper;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * Retrieves information about plugins from a Maven repository.
 * The implementation uses local caches when needed.
 */
public class MavenPluginInfoProvider implements PluginInfoProvider {

    private MavenHelper helper;
    private File tmpDir;

    public MavenPluginInfoProvider(@Nonnull MavenHelper helper, @Nonnull File tmpDir) {
        this.helper = helper;
        this.tmpDir = tmpDir;
    }

    @Override
    public boolean isPlugin(@Nonnull DependencyInfo dep) throws IOException, InterruptedException {
        return helper.artifactExistsInLocalCache(dep, dep.getSource().version, "hpi") ||
                helper.artifactExists(tmpDir, dep, dep.getSource().version, "hpi");
    }
}
