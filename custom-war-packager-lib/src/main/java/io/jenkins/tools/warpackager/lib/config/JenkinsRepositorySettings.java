package io.jenkins.tools.warpackager.lib.config;

import edu.umd.cs.findbugs.annotations.NonNull;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Settings for Jenkins repository
 */
public class JenkinsRepositorySettings {

    public static final String DEFAULT_ID = "repo.jenkins-ci.org";
    public static final String DEFAULT_URL = "https://repo.jenkins-ci.org/public/";
    public static final String DEFAULT_INCREMENTALS_ID = "incrementals";
    public static final String DEFAULT_INCREMENTALS_URL = "https://repo.jenkins-ci.org/incrementals/";

    @CheckForNull
    private String id;

    @CheckForNull
    private String url;

    @CheckForNull
    private String incrementalsId;

    @CheckForNull
    private String incrementalsUrl;

    public void setId(@CheckForNull String id) {
        this.id = id;
    }

    public void setUrl(@CheckForNull String url) {
        this.url = url;
    }

    public void setIncrementalsId(@CheckForNull String incrementalsId) {
        this.incrementalsId = incrementalsId;
    }

    public void setIncrementalsUrl(@CheckForNull String incrementalsUrl) {
        this.incrementalsUrl = incrementalsUrl;
    }

    @NonNull
    public String getId() {
        return id != null ? id : DEFAULT_ID;
    }


    @NonNull
    public String getUrl() {
        return url != null ? url : DEFAULT_URL;
    }

    @NonNull
    public String getIncrementalsId() {
        return incrementalsId != null ? incrementalsId : DEFAULT_INCREMENTALS_ID;
    }


    @NonNull
    public String getIncrementalsUrl() {
        return incrementalsUrl != null ? incrementalsUrl : DEFAULT_INCREMENTALS_URL;
    }
}
