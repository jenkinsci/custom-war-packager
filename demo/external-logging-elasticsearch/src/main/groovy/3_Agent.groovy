import hudson.slaves.DumbSlave;
import hudson.slaves.JNLPLauncher;
import jenkins.model.Jenkins;
import jenkins.slaves.JnlpSlaveAgentProtocol;

import javax.crypto.spec.SecretKeySpec;

println("-- Configuring the agent")

// Hardcode secret so that Docker Compose can connect agents
JnlpSlaveAgentProtocol.SLAVE_SECRET.@key = new SecretKeySpec(new byte[10], "HmacSHA256");

// Register the agent
def node = new DumbSlave("agent", "/home/jenkins", new JNLPLauncher(true));
Jenkins.instance.addNode(node);
