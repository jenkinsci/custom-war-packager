//TODO: Migrate to JCasC once it supports disabling via system property
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

if (!Boolean.getBoolean("io.jenkins.demo.external-task-logging-elk.enabled")) {
    // Production mode, we do not configure the system
    return
}

println("-- Creating Jobs")
//TODO: Classes do not work here, so some copy-paste for now
/*
WorkflowJob project1 = Jenkins.instance.createProject(WorkflowJob.class, "Demo_master")
project1.definition = new CpsFlowDefinition(
    "echo \"ping -c 20 google.com\"",
    true // Sandbox
)
project1.save()

WorkflowJob project2 = Jenkins.instance.createProject(WorkflowJob.class, "Demo_agent")
project2.definition = new CpsFlowDefinition(
    "node('agent') {" +
    "  echo \"Hello, world!\"" +
    "  sh \"ping -c 20 google.com\"" +
    "}",
    true // Sandbox
)
project2.save()
*/