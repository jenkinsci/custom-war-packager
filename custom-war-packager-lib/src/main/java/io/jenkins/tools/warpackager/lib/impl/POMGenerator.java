package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;
import io.jenkins.tools.warpackager.lib.config.JenkinsRepositorySettings;

import java.nio.charset.StandardCharsets;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;

/**
 * @author Oleg Nenashev
 */
public class POMGenerator {

    protected final Config config;

    public POMGenerator(Config config) {
        this.config = config;
    }

    protected void addRepositories(Model target) {
        JenkinsRepositorySettings repositorySettings = config.buildSettings.getJenkinsRepository();
        if (repositorySettings == null) {
            repositorySettings = new JenkinsRepositorySettings();
        }

        // Common repo
        Repository jenkinsRepository = new Repository();
        jenkinsRepository.setId(repositorySettings.getId());
        jenkinsRepository.setUrl(repositorySettings.getUrl());
        target.addPluginRepository(jenkinsRepository);
        target.addRepository(jenkinsRepository);

        // Incrementals repo
        Repository incrementals = new Repository();
        incrementals.setId(repositorySettings.getIncrementalsId());
        incrementals.setUrl(repositorySettings.getIncrementalsUrl());
        target.addPluginRepository(incrementals);
        target.addRepository(incrementals);
    }
    
    protected void addUTF8SourceEncodingProperty(Model target) {
        target.addProperty("project.build.sourceEncoding", StandardCharsets.UTF_8.toString());
    }
}
