import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.model.Jenkins
import hudson.model.*

boolean createAdmin = Boolean.getBoolean("io.jenkins.dev.security.createAdmin")

println("=== Installing the Security Realm")
def securityRealm = new HudsonPrivateSecurityRealm(false)
User user = securityRealm.createAccount("user", "user")
user.setFullName("User")
User admin = securityRealm.createAccount("admin", "admin")
admin.setFullName("Admin")
Jenkins.instance.setSecurityRealm(securityRealm)

println("=== Installing the demo Authorization strategy")
Jenkins.instance.authorizationStrategy = new FullControlOnceLoggedInAuthorizationStrategy()

