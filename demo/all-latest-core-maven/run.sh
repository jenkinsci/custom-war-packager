JENKINS_HOME=$(pwd)/work java -jar target/custom-war-packager-maven-plugin/output/target/jenkins-all-latest-1.1-SNAPSHOT.war --httpPort=8080 --prefix=/jenkins

