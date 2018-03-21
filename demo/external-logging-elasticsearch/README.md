External Task Logging to Elasticsearch Demo
===

This demo packages Jenkins WAR for External Task Logging to Elasticsearch with help of Logstash plugin.

This demo includes [Logstash Plugin PR#18](https://github.com/jenkinsci/logstash-plugin/pull/18) and
all its upstream dependencies. 
It also bundles auto-configuration System Groovy scripts, so that the WAR file starts
up with pre-configured Logstash plugin settings and some other configs.

## Building demo

To build the demo...

1. Go to the repository root, run `mvn clean package` to build Jenkins Custom WAR Packager
2. Change directory to the demo root
3. Run `sh run.sh`

First build may take a while, because the packager will need to checkout and build 
many repositories.
Finally you will get an output WAR here:`tmp/output/target/external-task-logging-logstash-1.0-SNAPSHOT.war`.
This is a modified Jenkins WAR, which can be launched from CLI.

## Running demo

The demo requires Elasticsearch 2.4 and Kibana 4.4.
The most simple way to get them running is to use the 
[sebp/elk](https://hub.docker.com/r/sebp/elk/) Docker image (e.g. the `es241_l240_k461` tag).

1. Run `docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -it --name elk sebp/elk:es241_l240_k461` 
to start the Docker container to to expose ports
2. Run Jenkins using `JENKINS_HOME=$(pwd)/work java -jar tmp/output/target/external-task-logging-logstash-1.0-SNAPSHOT.war --httpPort=8080 --prefix=/jenkins` 
(or just `run run.sh`).
  * If needed, the demo can be configured by setting system properties
  * `elasticsearch.host` - host, defaults to `http://localhost`
  * `elasticsearch.port` - Elasticsearch port, defaults to `9200`
  * `logstash.key` - Path to the root index/key for logging, defaults to `/logstash/logs`
  * `elasticsearch.username` and `elasticsearch.password` - 
3. Pass through the installation Wizard
4. Create a Pipeline job with some logging (e.g. `echo` commands), run it
5. Browse logs using the default "Log" action or additional Kibana Log action
