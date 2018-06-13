import hudson.security.csrf.DefaultCrumbIssuer
import hudson.model.*
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import hudson.util.Secret
import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration
import jenkins.CLI
import jenkins.security.s2m.AdminWhitelistRule
import org.kohsuke.stapler.StaplerProxy

import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

//TODO: Migrate to JCasC once it supports disabling via system property

if (!Boolean.getBoolean("io.jenkins.demo.external-task-logging-elk.enabled")) {
    // Production mode, we do not configure the system
    return
}

println("-- System configuration")

println("--- Installing the Security Realm")
def securityRealm = new HudsonPrivateSecurityRealm(false)
User user = securityRealm.createAccount("user", "user")
user.setFullName("User")
User admin = securityRealm.createAccount("admin", "admin")
admin.setFullName("Admin")
Jenkins.instance.setSecurityRealm(securityRealm)

println("---Installing the demo Authorization strategy")
Jenkins.instance.authorizationStrategy = new FullControlOnceLoggedInAuthorizationStrategy()

println("--- Configuring Remoting (JNLP4 only, no Remoting CLI)")
CLI.get().enabled = false
Jenkins.instance.agentProtocols = new HashSet<String>(["JNLP4-connect"])
Jenkins.instance.getExtensionList(StaplerProxy.class)
    .get(AdminWhitelistRule.class)
    .masterKillSwitch = false

println("--- Checking the CSRF protection")
if (Jenkins.instance.crumbIssuer == null) {
    println "CSRF protection is disabled, Enabling the default Crumb Issuer"
    Jenkins.instance.crumbIssuer = new DefaultCrumbIssuer(true)
}

println("--- Configuring Quiet Period")
// We do not wait for anything, demo should be fast
Jenkins.instance.quietPeriod = 0

println("--- Configuring Email global settings")
JenkinsLocationConfiguration.get().adminAddress = "admin@non.existent.email"
// Mailer.descriptor().defaultSuffix = "@non.existent.email"

println("--- Adding test credentials")
def c = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    "token",
    "Test token",
    Secret.fromString("SECRET_TOKEN_WHICH_SHOULD_NOD_BE_DISPLAYED")
)

CredentialsProvider.lookupStores(Jenkins.instance).each { it ->
    it.addCredentials(Domain.global(), c)
}
