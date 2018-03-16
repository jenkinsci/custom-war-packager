import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration
import jenkins.CLI
import jenkins.security.s2m.AdminWhitelistRule
import org.kohsuke.stapler.StaplerProxy
//import hudson.tasks.Mailer

println("-- System configuration")

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

