import jenkins.plugins.logstash.LogstashInstallation
import jenkins.plugins.logstash.persistence.LogstashIndexerDao;

import java.lang.System;

String logstashPort = System.getProperty("elasticsearch.port");

def descriptor = LogstashInstallation.logstashDescriptor
descriptor.@type = LogstashIndexerDao.IndexerType.ELASTICSEARCH
descriptor.@host = System.getProperty("elasticsearch.host", "http://localhost")
descriptor.@port = logstashPort != null ? Integer.parseInt(logstashPort) : 9200
descriptor.@username = System.getProperty("elasticsearch.username")
descriptor.@password = System.getProperty("elasticsearch.password")
descriptor.@key = System.getProperty("logstash.key", "/logstash/logs")
descriptor.save()



