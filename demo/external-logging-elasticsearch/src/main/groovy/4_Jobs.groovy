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

if(Jenkins.instance.getItem("Demo_master") == null) {
    WorkflowJob project1 = Jenkins.instance.createProject(WorkflowJob.class, "Demo_master")
    project1.definition = new CpsFlowDefinition(
        "node('master') {\n" +
        "  sh \"ping -c 20 google.com\"\n" +
        "}",
        true // Sandbox
    )
    project1.save()
}

if(Jenkins.instance.getItem("Demo_agent") == null) {
    WorkflowJob project2 = Jenkins.instance.createProject(WorkflowJob.class, "Demo_agent")
    project2.definition = new CpsFlowDefinition(
        "node('agent') {\n" +
        "  sh \"echo Hello, world!\"\n" +
            // TODO Current demo image does not have ping, ORLY (alpine)
            //    "  sh \"ping -c 20 google.com\"\n" +
        "}",
        true // Sandbox
    )
    project2.save()
}

if(Jenkins.instance.getItem("Demo_parallel") == null) {
    WorkflowJob project3 = Jenkins.instance.createProject(WorkflowJob.class, "Demo_parallel")
    project3.definition = new CpsFlowDefinition(
        "parallel local: {\n" +
        "  node('master') {\n" +
        "    sh 'for x in 0 1 2 3 4 5 6 7 8 9; do echo \$x; sleep 1; done'\n" +
        "  }\n" +
        "}, remote: {\n" +
        "  node('agent') {\n" +
        "    withCredentials([string(credentialsId: 'token', variable: 'TOKEN')]) {\n" +
        "      sh 'echo receiving \$TOKEN'\n" +
        "      sh 'for x in 0 1 2 3 4 5 6 7 8 9; do echo \$x; sleep 1; done'\n" +
        "    }\n" +
        "  }\n" +
        "}",
        true // Sandbox
    )
    project3.save()
}
