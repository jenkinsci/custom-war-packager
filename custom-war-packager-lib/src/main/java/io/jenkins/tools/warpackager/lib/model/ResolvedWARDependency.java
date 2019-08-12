package io.jenkins.tools.warpackager.lib.model;

import hudson.util.VersionNumber;
import io.jenkins.tools.warpackager.lib.config.DependencyInfo;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class ResolvedWARDependency extends ResolvedDependency {

    public ResolvedWARDependency(@Nonnull ResolvedDependency base) {
        super(base.getGroupId(), base.getVersion(), base.getOriginalDependency());
    }

    public ResolvedWARDependency(@Nonnull String groupId, @Nonnull VersionNumber version, @Nonnull DependencyInfo originalDependency) {
        super(groupId, version, originalDependency);
    }
}
