
import jenkins.model.Jenkins
import hudson.model.JDK
import hudson.tasks.Maven.MavenInstallation;
import hudson.tasks.Maven
import hudson.tools.InstallSourceProperty

println("--- Setup tool installations")
// Java 8 should be a default Java, because we require it for Jenkins 2.60.1+
JDK jdk8 = new JDK("jdk8", "")
Jenkins.instance.getDescriptorByType(JDK.DescriptorImpl.class).setInstallations(jdk8)

InstallSourceProperty p = new InstallSourceProperty([new Maven.MavenInstaller("3.5.0")])
MavenInstallation mvn = new MavenInstallation("mvn", null, [p])
Jenkins.instance.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(mvn)
