package io.jenkins.tools.warpackager.lib.impl;

import io.jenkins.tools.warpackager.lib.config.Config;

import java.nio.charset.StandardCharsets;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;

/**
 * @author Oleg Nenashev
 * @since TODO
 */
public class POMGenerator {

    protected final Config config;

    public POMGenerator(Config config) {
        this.config = config;
    }

    //TODO: support customization via build settings
    protected void addRepositories(Model target) {
        // Common repo
        Repository jenkinsRepository = new Repository();
        jenkinsRepository.setId("repo.jenkins-ci.org");
        jenkinsRepository.setUrl("https://repo.jenkins-ci.org/public/");
        target.addPluginRepository(jenkinsRepository);
        target.addRepository(jenkinsRepository);

        // Incrementals repo
        Repository incrementals = new Repository();
        incrementals.setId("incrementals");
        incrementals.setUrl("https://repo.jenkins-ci.org/incrementals/");
        target.addPluginRepository(incrementals);
        target.addRepository(incrementals);
    }
    
    protected void addUTF8SourceEncodingProperty(Model target) {
        target.addProperty("project.build.sourceEncoding", StandardCharsets.UTF_8.toString());
    }
}
