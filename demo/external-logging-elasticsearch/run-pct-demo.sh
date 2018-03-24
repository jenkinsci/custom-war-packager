# Runs PCT for a custom WAR with External Task Logging Enabled
docker run --rm -v $HOME/.m2:/root/.m2 -v $(pwd)/pct:/pct/out -v $(pwd)/tmp/output/target/custom-war-1.0-SNAPSHOT.war:/pct/jenkins.war:ro -e ARTIFACT_ID=workflow-job jenkins/pct
