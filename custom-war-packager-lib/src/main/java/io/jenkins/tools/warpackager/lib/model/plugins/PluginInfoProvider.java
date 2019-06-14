package io.jenkins.tools.warpackager.lib.model.plugins;

import io.jenkins.tools.warpackager.lib.config.DependencyInfo;
import io.jenkins.tools.warpackager.lib.impl.plugins.MavenPluginInfoProvider;
import io.jenkins.tools.warpackager.lib.impl.plugins.UpdateCenterPluginInfoProvider;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Plugin Information provider.
 * The implementations can be used to query various data about plugins.
 * @see MavenPluginInfoProvider
 * @see UpdateCenterPluginInfoProvider
 * @since TODO
 */
public interface PluginInfoProvider {

    public static final PluginInfoProvider DEFAULT = new UpdateCenterPluginInfoProvider(UpdateCenterPluginInfoProvider.DEFAULT_JENKINS_UC_URL);

    /**
     * Initializes the plugin info source
     * @throws IOException Execution failure
     * @throws InterruptedException Execution was interrupted
     */
    default void init() throws IOException, InterruptedException {
        //NOOP
    }

    /**
     * Check whether the dependency is an existing plugin available from the specified source.
     * @param dependency Dependency to check
     * @return {@code true} if the provider can confirm that the extension is a plugin.
     *         {@code false} otherwise, even if there is no conclusive response
     * @throws IOException Execution failure
     * @throws InterruptedException Execution was interrupted
     */
    public boolean isPlugin(@Nonnull DependencyInfo dependency) throws IOException, InterruptedException;
}
