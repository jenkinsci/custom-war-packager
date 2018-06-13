External Task Logging to Elasticsearch Demo
===

This demo packages Jenkins WAR for External Task Logging to Elasticsearch with help of Logstash plugin.

This demo includes [Logstash Plugin PR#18](https://github.com/jenkinsci/logstash-plugin/pull/18) and
all its upstream dependencies. 
It also bundles auto-configuration System Groovy scripts, so that the WAR file starts
up with pre-configured Logstash plugin settings and some other configs.

Features of the demo:

* Pipeline jobs logging goes to Elasticsearch
* When tasks are executed on agents, the logs get posted to Elasticsearch directly
  without passing though the master and causing scalability issues
* Pipeline jobs override standard Log actions in the Jenkins core, so the
  underlying implementation is transparent to users
* Secrets are escaped in stored/displayed logs when running on master and agents.
* Console annotations work as they work for common Jenkins instances
* Log blocks are collapsible in the _Console_ screen
* Origin container ID of every message is visible in Kibana (if you have set that up) via sender field

The demo can be run in Docker Compose,
ELK stack is provided by the [sebp/elk](https://hub.docker.com/r/sebp/elk/)  image in this case.

## Prerequisites

* Docker and Docker Compose are installed

## Building demo

To build the demo...

1. Go to the repository root, run `mvn clean package` to build Jenkins Custom WAR Packager
2. Change directory to the demo root
3. Run `make build`

First build may take a while, because the packager will need to checkout and build 
many repositories.

## Running demo

1. Run `make run`. It will spin up the demo with predefined environment.
   Jenkins will be available on the port 8080, credentials: `admin/admin`
2. If you want to run demo jobs on the agent, 
also run `docker-compose up agent` in a separate terminal window
3. In order to access the instance, use the "admin/admin" credentials.
4. Run one of the demo jobs.   
5. Browse logs
  * Classic Log action queries data from Elasticsearch
  * There is a _Log (Kibana)_ action in runs, which shows Kibana. 
  * In order to see Kibana logs, you will need to configure the default index in the 
    embedded page once Jenkins starts up. Use `logstash/` as a default index and 
    `@timestamp` as data source

## Manual run

This guideline allows to run the demo locally.
Only Logstash will be preconfigured.

1. Run `docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -it --name elk sebp/elk:es241_l240_k461` 
to start the Docker container to to expose ports
2. Run Jenkins using `JENKINS_HOME=$(pwd)/work java -jar tmp/output/target/external-task-logging-elk-2.107.3-elk-SNAPSHOT.war --httpPort=8080 --prefix=/jenkins` 
(or just `run run.sh`).
  * If needed, the demo can be configured by setting system properties
  * `elasticsearch.host` - host, defaults to `http://elk`
  * `elasticsearch.port` - Elasticsearch port, defaults to `9200`
  * `logstash.key` - Path to the root index/key for logging, defaults to `/logstash/logs`
  * `elasticsearch.username` and `elasticsearch.password` - 
3. Pass through the installation Wizard
4. Create a Pipeline job with some logging (e.g. `echo` commands), run it
5. Browse logs (see above)
