#!/bin/bash
#
# Copyright 2020 Confluent Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


###############################################################################
# Overview:
#
# This code reads a local Confluent Cloud configuration file
# and writes delta configuration files into ./delta_configs for
# Confluent Platform components and clients connecting to Confluent Cloud.
#
# Confluent Platform Components:
# - Confluent Schema Registry
# - KSQL Data Generator
# - ksqlDB server
# - Confluent Replicator (executable)
# - Confluent Control Center
# - Confluent Metrics Reporter
# - Confluent REST Proxy
# - Kafka Connect
# - Kafka connector
# - Kafka command line tools
#
# Kafka Clients:
# - Java (Producer/Consumer)
# - Java (Streams)
# - Python
# - .NET
# - Go
# - Node.js (https://github.com/Blizzard/node-rdkafka)
# - C++
#
# Documentation for using this script:
#
#   https://docs.confluent.io/current/cloud/connect/auto-generate-configs.html
#
# Arguments:
#
#   1 (optional) - CONFIG_FILE, defaults to ~/.ccloud/config, (required if specifying SR_CONFIG_FILE)
#   2 (optional) - SR_CONFIG_FILE, defaults to CONFIG_FILE
#
# Example CONFIG_FILE at ~/.ccloud/config
#
#   $ cat $HOME/.ccloud/config
#
#   bootstrap.servers=<BROKER ENDPOINT>
#   ssl.endpoint.identification.algorithm=https
#   security.protocol=SASL_SSL
#   sasl.mechanism=PLAIN
#   sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username\="<API KEY>" password\="<API SECRET>";
#
# If you are using Confluent Cloud Schema Registry, add the following configuration parameters
# either to file above (arg 1 CONFIG_FILE) or to a separate file (arg 2 SR_CONFIG_FILE)
#
#   basic.auth.credentials.source=USER_INFO
#   schema.registry.basic.auth.user.info=<SR API KEY>:<SR API SECRET>
#   schema.registry.url=https://<SR ENDPOINT>
#
# If you are using Confluent Cloud ksqlDB, add the following configuration parameters
# to file above (arg 1 CONFIG_FILE)
#
#   ksql.endpoint=<ksqlDB ENDPOINT>
#   ksql.basic.auth.user.info=<ksqlDB API KEY>:<ksqlDB API SECRET>
#
################################################################################
CONFIG_FILE=$1
if [[ -z "$CONFIG_FILE" ]]; then
  CONFIG_FILE=~/.ccloud/config
fi
if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "File $CONFIG_FILE is not found.  Please create this properties file to connect to your Confluent Cloud cluster and then try again"
  echo "See https://docs.confluent.io/current/cloud/connect/auto-generate-configs.html for more information"
  exit 1
fi

SR_CONFIG_FILE=$2
if [[ -z "$SR_CONFIG_FILE" ]]; then
  SR_CONFIG_FILE=$CONFIG_FILE
fi
if [[ ! -f "$SR_CONFIG_FILE" ]]; then
  echo "File $SR_CONFIG_FILE is not found.  Please create this properties file to connect to your Schema Registry and then try again"
  echo "See https://docs.confluent.io/current/cloud/connect/auto-generate-configs.html for more information"
  exit 1
fi

echo -e "\nGenerating component configurations from $CONFIG_FILE and Schema Registry configurations from $SR_CONFIG_FILE" 
echo -e "\n(If you want to run any of these components to talk to Confluent Cloud, these are the configurations to add to the properties file for each component)"

# Set permissions
PERM=600
if ls --version 2>/dev/null | grep -q 'coreutils' ; then
  # GNU binutils
  PERM=$(stat -c "%a" $CONFIG_FILE)
else
  # BSD
  PERM=$(stat -f "%OLp" $CONFIG_FILE)
fi

# Make destination
DEST="delta_configs"
mkdir -p $DEST

################################################################################
# Glean parameters from the Confluent Cloud configuration file
################################################################################

# Kafka cluster
BOOTSTRAP_SERVERS=$( grep "^bootstrap.server" $CONFIG_FILE | awk -F'=' '{print $2;}' )
BOOTSTRAP_SERVERS=${BOOTSTRAP_SERVERS/\\/}
SASL_JAAS_CONFIG=$( grep "^sasl.jaas.config" $CONFIG_FILE | cut -d'=' -f2- )
SASL_JAAS_CONFIG_PROPERTY_FORMAT=${SASL_JAAS_CONFIG/username\\=/username=}
SASL_JAAS_CONFIG_PROPERTY_FORMAT=${SASL_JAAS_CONFIG_PROPERTY_FORMAT/password\\=/password=}
CLOUD_KEY=$( echo $SASL_JAAS_CONFIG | awk '{print $3}' | awk -F'"' '$0=$2' )
CLOUD_SECRET=$( echo $SASL_JAAS_CONFIG | awk '{print $4}' | awk -F'"' '$0=$2' )

# Schema Registry
BASIC_AUTH_CREDENTIALS_SOURCE=$( grep "^basic.auth.credentials.source" $SR_CONFIG_FILE | awk -F'=' '{print $2;}' )
SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO=$( grep "^schema.registry.basic.auth.user.info" $SR_CONFIG_FILE | awk -F'=' '{print $2;}' )
SCHEMA_REGISTRY_URL=$( grep "^schema.registry.url" $SR_CONFIG_FILE | awk -F'=' '{print $2;}' )

# ksqlDB
KSQLDB_ENDPOINT=$( grep "^ksql.endpoint" $CONFIG_FILE | awk -F'=' '{print $2;}' )
KSQLDB_BASIC_AUTH_USER_INFO=$( grep "^ksql.basic.auth.user.info" $CONFIG_FILE | awk -F'=' '{print $2;}' )

################################################################################
# Build configuration file with CCloud connection parameters and
# Confluent Monitoring Interceptors for Streams Monitoring in Confluent Control Center
################################################################################
INTERCEPTORS_CONFIG_FILE=$DEST/interceptors-ccloud.config
rm -f $INTERCEPTORS_CONFIG_FILE
echo "# Configuration derived from $CONFIG_FILE" > $INTERCEPTORS_CONFIG_FILE
while read -r line
do
  # Skip lines that are commented out
  if [[ ! -z $line && ${line:0:1} == '#' ]]; then
    continue
  fi
  # Skip lines that contain just whitespace
  if [[ -z "${line// }" ]]; then
    continue
  fi
  if [[ ${line:0:9} == 'bootstrap' ]]; then
    line=${line/\\/}
  fi
  echo $line >> $INTERCEPTORS_CONFIG_FILE
done < "$CONFIG_FILE"
echo -e "\n# Confluent Monitoring Interceptor specific configuration" >> $INTERCEPTORS_CONFIG_FILE
while read -r line
do
  # Skip lines that are commented out
  if [[ ! -z $line && ${line:0:1} == '#' ]]; then
    continue
  fi
  # Skip lines that contain just whitespace
  if [[ -z "${line// }" ]]; then
    continue
  fi
  if [[ ${line:0:9} == 'bootstrap' ]]; then
    line=${line/\\/}
  fi
  if [[ ${line:0:4} == 'sasl' ||
        ${line:0:3} == 'ssl' ||
        ${line:0:8} == 'security' ||
        ${line:0:9} == 'bootstrap' ]]; then
    echo "confluent.monitoring.interceptor.$line" >> $INTERCEPTORS_CONFIG_FILE
  fi
done < "$CONFIG_FILE"
chmod $PERM $INTERCEPTORS_CONFIG_FILE

echo -e "\nConfluent Platform Components:"

################################################################################
# Confluent Schema Registry instance (local) for Confluent Cloud
################################################################################
SR_CONFIG_DELTA=$DEST/schema-registry-ccloud.delta
echo "$SR_CONFIG_DELTA"
rm -f $SR_CONFIG_DELTA
while read -r line
do
  if [[ ! -z $line && ${line:0:1} != '#' ]]; then
    if [[ ${line:0:29} != 'basic.auth.credentials.source' && ${line:0:15} != 'schema.registry' ]]; then
      echo "kafkastore.$line" >> $SR_CONFIG_DELTA
    fi
  fi
done < "$CONFIG_FILE"
chmod $PERM $SR_CONFIG_DELTA

################################################################################
# Confluent Replicator (executable) for Confluent Cloud
################################################################################
REPLICATOR_PRODUCER_DELTA=$DEST/replicator-to-ccloud-producer.delta
echo "$REPLICATOR_PRODUCER_DELTA"
rm -f $REPLICATOR_PRODUCER_DELTA
cp $INTERCEPTORS_CONFIG_FILE $REPLICATOR_PRODUCER_DELTA
echo -e "\n# Confluent Replicator (executable) specific configuration" >> $REPLICATOR_PRODUCER_DELTA
echo "interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor" >> $REPLICATOR_PRODUCER_DELTA
REPLICATOR_SASL_JAAS_CONFIG=$SASL_JAAS_CONFIG
REPLICATOR_SASL_JAAS_CONFIG=${REPLICATOR_SASL_JAAS_CONFIG//\\=/=}
REPLICATOR_SASL_JAAS_CONFIG=${REPLICATOR_SASL_JAAS_CONFIG//\"/\\\"}
chmod $PERM $REPLICATOR_PRODUCER_DELTA

################################################################################
# ksqlDB Server runs locally and connects to Confluent Cloud
################################################################################
KSQLDB_SERVER_DELTA=$DEST/ksqldb-server-ccloud.delta
echo "$KSQLDB_SERVER_DELTA"
cp $INTERCEPTORS_CONFIG_FILE $KSQLDB_SERVER_DELTA
echo -e "\n# ksqlDB Server specific configuration" >> $KSQLDB_SERVER_DELTA
echo "producer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor" >> $KSQLDB_SERVER_DELTA
echo "consumer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor" >> $KSQLDB_SERVER_DELTA
echo "ksql.streams.producer.retries=2147483647" >> $KSQLDB_SERVER_DELTA
echo "ksql.streams.producer.confluent.batch.expiry.ms=9223372036854775807" >> $KSQLDB_SERVER_DELTA
echo "ksql.streams.producer.request.timeout.ms=300000" >> $KSQLDB_SERVER_DELTA
echo "ksql.streams.producer.max.block.ms=9223372036854775807" >> $KSQLDB_SERVER_DELTA
echo "ksql.streams.replication.factor=3" >> $KSQLDB_SERVER_DELTA
echo "ksql.internal.topic.replicas=3" >> $KSQLDB_SERVER_DELTA
echo "ksql.sink.replicas=3" >> $KSQLDB_SERVER_DELTA
echo -e "\n# Confluent Schema Registry configuration for ksqlDB Server" >> $KSQLDB_SERVER_DELTA
while read -r line
do
  if [[ ${line:0:29} == 'basic.auth.credentials.source' ]]; then
    echo "ksql.schema.registry.$line" >> $KSQLDB_SERVER_DELTA
  elif [[ ${line:0:15} == 'schema.registry' ]]; then
    echo "ksql.$line" >> $KSQLDB_SERVER_DELTA
  fi
done < $SR_CONFIG_FILE
chmod $PERM $KSQLDB_SERVER_DELTA

################################################################################
# KSQL DataGen for Confluent Cloud
################################################################################
KSQL_DATAGEN_DELTA=$DEST/ksql-datagen.delta
echo "$KSQL_DATAGEN_DELTA"
rm -f $KSQL_DATAGEN_DELTA
cp $INTERCEPTORS_CONFIG_FILE $KSQL_DATAGEN_DELTA
echo -e "\n# KSQL DataGen specific configuration" >> $KSQL_DATAGEN_DELTA
echo "interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor" >> $KSQL_DATAGEN_DELTA
echo -e "\n# Confluent Schema Registry configuration for KSQL DataGen" >> $KSQL_DATAGEN_DELTA
while read -r line
do
  if [[ ${line:0:29} == 'basic.auth.credentials.source' ]]; then
    echo "ksql.schema.registry.$line" >> $KSQL_DATAGEN_DELTA
  elif [[ ${line:0:15} == 'schema.registry' ]]; then
    echo "ksql.$line" >> $KSQL_DATAGEN_DELTA
  fi
done < $SR_CONFIG_FILE
chmod $PERM $KSQL_DATAGEN_DELTA

################################################################################
# Confluent Control Center runs locally, monitors Confluent Cloud, and uses Confluent Cloud cluster as the backstore
################################################################################
C3_DELTA=$DEST/control-center-ccloud.delta
echo "$C3_DELTA"
rm -f $C3_DELTA
echo -e "\n# Confluent Control Center specific configuration" >> $C3_DELTA
while read -r line
  do
  if [[ ! -z $line && ${line:0:1} != '#' ]]; then
    if [[ ${line:0:9} == 'bootstrap' ]]; then
      line=${line/\\/}
      echo "$line" >> $C3_DELTA
    fi
    if [[ ${line:0:4} == 'sasl' || ${line:0:3} == 'ssl' || ${line:0:8} == 'security' ]]; then
      echo "confluent.controlcenter.streams.$line" >> $C3_DELTA
    fi
  fi
done < "$CONFIG_FILE"
# max.message.bytes is enforced to 8MB in Confluent Cloud
echo "confluent.metrics.topic.max.message.bytes=8388608" >> $C3_DELTA
echo -e "\n# Confluent Schema Registry configuration for Confluent Control Center" >> $C3_DELTA
while read -r line
do
  if [[ ${line:0:29} == 'basic.auth.credentials.source' ]]; then
    echo "confluent.controlcenter.schema.registry.$line" >> $C3_DELTA
  elif [[ ${line:0:15} == 'schema.registry' ]]; then
    echo "confluent.controlcenter.$line" >> $C3_DELTA
  fi
done < $SR_CONFIG_FILE
chmod $PERM $C3_DELTA

################################################################################
# Confluent Metrics Reporter to Confluent Cloud
################################################################################
METRICS_REPORTER_DELTA=$DEST/metrics-reporter.delta
echo "$METRICS_REPORTER_DELTA"
rm -f $METRICS_REPORTER_DELTA
echo "metric.reporters=io.confluent.metrics.reporter.ConfluentMetricsReporter" >> $METRICS_REPORTER_DELTA
echo "confluent.metrics.reporter.topic.replicas=3" >> $METRICS_REPORTER_DELTA
while read -r line
  do
  if [[ ! -z $line && ${line:0:1} != '#' ]]; then
    if [[ ${line:0:9} == 'bootstrap' || ${line:0:4} == 'sasl' || ${line:0:3} == 'ssl' || ${line:0:8} == 'security' ]]; then
      echo "confluent.metrics.reporter.$line" >> $METRICS_REPORTER_DELTA
    fi
  fi
done < "$CONFIG_FILE"
chmod $PERM $METRICS_REPORTER_DELTA

################################################################################
# Confluent REST Proxy to Confluent Cloud
################################################################################
REST_PROXY_DELTA=$DEST/rest-proxy.delta
echo "$REST_PROXY_DELTA"
rm -f $REST_PROXY_DELTA
while read -r line
  do
  if [[ ! -z $line && ${line:0:1} != '#' ]]; then
    if [[ ${line:0:9} == 'bootstrap' || ${line:0:4} == 'sasl' || ${line:0:3} == 'ssl' || ${line:0:8} == 'security' ]]; then
      echo "$line" >> $REST_PROXY_DELTA
      echo "client.$line" >> $REST_PROXY_DELTA
    fi
  fi
done < "$CONFIG_FILE"
echo -e "\n# Confluent Schema Registry configuration for REST Proxy" >> $REST_PROXY_DELTA
while read -r line
do
  if [[ ${line:0:29} == 'basic.auth.credentials.source' || ${line:0:36} == 'schema.registry.basic.auth.user.info' ]]; then
    echo "client.$line" >> $REST_PROXY_DELTA
  elif [[ ${line:0:19} == 'schema.registry.url' ]]; then
    echo "$line" >> $REST_PROXY_DELTA
  fi
done < $SR_CONFIG_FILE
chmod $PERM $REST_PROXY_DELTA

################################################################################
# Kafka Connect runs locally and connects to Confluent Cloud
################################################################################
CONNECT_DELTA=$DEST/connect-ccloud.delta
echo "$CONNECT_DELTA"
rm -f $CONNECT_DELTA
cat <<EOF > $CONNECT_DELTA
# Configuration for embedded admin client
replication.factor=3
config.storage.replication.factor=3
offset.storage.replication.factor=3
status.storage.replication.factor=3

EOF
while read -r line
  do
  if [[ ! -z $line && ${line:0:1} != '#' ]]; then
    if [[ ${line:0:9} == 'bootstrap' ]]; then
      line=${line/\\/}
      echo "$line" >> $CONNECT_DELTA
    fi
    if [[ ${line:0:4} == 'sasl' || ${line:0:3} == 'ssl' || ${line:0:8} == 'security' ]]; then
      echo "$line" >> $CONNECT_DELTA
    fi
  fi
done < "$CONFIG_FILE"

for prefix in "producer" "consumer" "producer.confluent.monitoring.interceptor" "consumer.confluent.monitoring.interceptor" ; do

echo -e "\n# Configuration for embedded $prefix" >> $CONNECT_DELTA
while read -r line
  do
  if [[ ! -z $line && ${line:0:1} != '#' ]]; then
    if [[ ${line:0:9} == 'bootstrap' ]]; then
      line=${line/\\/}
    fi
    if [[ ${line:0:4} == 'sasl' || ${line:0:3} == 'ssl' || ${line:0:8} == 'security' ]]; then
      echo "${prefix}.$line" >> $CONNECT_DELTA
    fi
  fi
done < "$CONFIG_FILE"

done


cat <<EOF >> $CONNECT_DELTA

# Confluent Schema Registry for Kafka Connect
value.converter=io.confluent.connect.avro.AvroConverter
value.converter.basic.auth.credentials.source=$BASIC_AUTH_CREDENTIALS_SOURCE
value.converter.schema.registry.basic.auth.user.info=$SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO
value.converter.schema.registry.url=$SCHEMA_REGISTRY_URL
EOF
chmod $PERM $CONNECT_DELTA

################################################################################
# Kafka connector
################################################################################
CONNECTOR_DELTA=$DEST/connector-ccloud.delta
echo "$CONNECTOR_DELTA"
rm -f $CONNECTOR_DELTA
cat <<EOF >> $CONNECTOR_DELTA
// Confluent Schema Registry for Kafka connectors
value.converter=io.confluent.connect.avro.AvroConverter
value.converter.basic.auth.credentials.source=$BASIC_AUTH_CREDENTIALS_SOURCE
value.converter.schema.registry.basic.auth.user.info=$SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO
value.converter.schema.registry.url=$SCHEMA_REGISTRY_URL
EOF
chmod $PERM $CONNECTOR_DELTA

################################################################################
# AK command line tools
################################################################################
AK_TOOLS_DELTA=$DEST/ak-tools-ccloud.delta
echo "$AK_TOOLS_DELTA"
rm -f $AK_TOOLS_DELTA
cp $CONFIG_FILE $AK_TOOLS_DELTA
chmod $PERM $AK_TOOLS_DELTA

################################################################################
# ENV
################################################################################
ENV_CONFIG=$DEST/env.delta
echo "$ENV_CONFIG"
rm -f $ENV_CONFIG

cat <<EOF >> $ENV_CONFIG
export BOOTSTRAP_SERVERS=$BOOTSTRAP_SERVERS
export SASL_JAAS_CONFIG='$SASL_JAAS_CONFIG_PROPERTY_FORMAT'
export SASL_JAAS_CONFIG_PROPERTY_FORMAT='$SASL_JAAS_CONFIG_PROPERTY_FORMAT'
export REPLICATOR_SASL_JAAS_CONFIG='$REPLICATOR_SASL_JAAS_CONFIG'
export BASIC_AUTH_CREDENTIALS_SOURCE=$BASIC_AUTH_CREDENTIALS_SOURCE
export SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO=$SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO
export SCHEMA_REGISTRY_URL=$SCHEMA_REGISTRY_URL
export CLOUD_KEY=$CLOUD_KEY
export CLOUD_SECRET=$CLOUD_SECRET
export KSQLDB_ENDPOINT=$KSQLDB_ENDPOINT
export KSQLDB_BASIC_AUTH_USER_INFO=$KSQLDB_BASIC_AUTH_USER_INFO
EOF
chmod $PERM $ENV_CONFIG