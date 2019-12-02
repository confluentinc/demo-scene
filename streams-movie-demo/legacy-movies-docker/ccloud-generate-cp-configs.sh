#!/bin/bash
#
# Copyright 2016 Confluent Inc.
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
# - KSQL server
# - Confluent Replicator (executable)
# - Confluent Control Center
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
echo "CONFIG_FILE: $CONFIG_FILE"

SR_CONFIG_FILE=$2
if [[ -z "$SR_CONFIG_FILE" ]]; then
  SR_CONFIG_FILE=$CONFIG_FILE
fi
if [[ ! -f "$SR_CONFIG_FILE" ]]; then
  echo "File $SR_CONFIG_FILE is not found.  Please create this properties file to connect to your Schema Registry and then try again"
  echo "See https://docs.confluent.io/current/cloud/connect/auto-generate-configs.html for more information"
  exit 1
fi
echo "SR_CONFIG_FILE: $SR_CONFIG_FILE"

# Set permissions
PERM=600
if ls --version 2>/dev/null | grep -q 'coreutils'; then
  # GNU binutils
  PERM=$(stat -c "%a" $CONFIG_FILE)
else
  # BSD
  PERM=$(stat -f "%OLp" $CONFIG_FILE)
fi
#echo "INFO: setting file permission to $PERM"

# Make destination
DEST="delta_configs"
mkdir -p $DEST

################################################################################
# Clean parameters from the Confluent Cloud configuration file
################################################################################
BOOTSTRAP_SERVERS=$(grep "^bootstrap.server" $CONFIG_FILE | awk -F'=' '{print $2;}')
BOOTSTRAP_SERVERS=${BOOTSTRAP_SERVERS/\\/}
SASL_JAAS_CONFIG=$(grep "^sasl.jaas.config" $CONFIG_FILE | cut -d'=' -f2-)
SASL_JAAS_CONFIG_PROPERTY_FORMAT=${SASL_JAAS_CONFIG/username\\=/username=}
SASL_JAAS_CONFIG_PROPERTY_FORMAT=${SASL_JAAS_CONFIG_PROPERTY_FORMAT/password\\=/password=}
CLOUD_KEY=$(echo $SASL_JAAS_CONFIG | awk '{print $3}' | awk -F'"' '$0=$2')
CLOUD_SECRET=$(echo $SASL_JAAS_CONFIG | awk '{print $4}' | awk -F'"' '$0=$2')

#echo "bootstrap.servers: $BOOTSTRAP_SERVERS"
#echo "sasl.jaas.config: $SASL_JAAS_CONFIG"
#echo "key: $CLOUD_KEY"
#echo "secret: $CLOUD_SECRET"

BASIC_AUTH_CREDENTIALS_SOURCE=$(grep "^basic.auth.credentials.source" $SR_CONFIG_FILE | awk -F'=' '{print $2;}')
SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO=$(grep "^schema.registry.basic.auth.user.info" $SR_CONFIG_FILE | awk -F'=' '{print $2;}')
SCHEMA_REGISTRY_URL=$(grep "^schema.registry.url" $SR_CONFIG_FILE | awk -F'=' '{print $2;}')

#echo "basic.auth.credentials.source: $BASIC_AUTH_CREDENTIALS_SOURCE"
#echo "schema.registry.basic.auth.user.info: $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO"
#echo "schema.registry.url: $SCHEMA_REGISTRY_URL"

################################################################################
# Build configuration file with CCloud connection parameters and
# Confluent Monitoring Interceptors for Streams Monitoring in Confluent Control Center
################################################################################
INTERCEPTORS_CONFIG_FILE=$DEST/interceptors-ccloud.config
rm -f $INTERCEPTORS_CONFIG_FILE
echo "# Configuration derived from $CONFIG_FILE" >$INTERCEPTORS_CONFIG_FILE
while read -r line; do
  # Skip lines that are commented out
  if [[ ! -z $line && ${line:0:1} == '#' ]]; then
    continue
  fi
  # Skip lines that contain just whitespace
  if [[ -z "${line// /}" ]]; then
    continue
  fi
  if [[ ${line:0:9} == 'bootstrap' ]]; then
    line=${line/\\/}
  fi
  echo $line >>$INTERCEPTORS_CONFIG_FILE
done <"$CONFIG_FILE"
echo -e "\n# Confluent Monitoring Interceptor specific configuration" >>$INTERCEPTORS_CONFIG_FILE
while read -r line; do
  # Skip lines that are commented out
  if [[ ! -z $line && ${line:0:1} == '#' ]]; then
    continue
  fi
  # Skip lines that contain just whitespace
  if [[ -z "${line// /}" ]]; then
    continue
  fi
  if [[ ${line:0:9} == 'bootstrap' ]]; then
    line=${line/\\/}
  fi
  if [[ ${line:0:4} == 'sasl' || ${line:0:3} == 'ssl' || ${line:0:8} == 'security' || ${line:0:9} == 'bootstrap' ]]; then
    echo "confluent.monitoring.interceptor.$line" >>$INTERCEPTORS_CONFIG_FILE
  fi
done <"$CONFIG_FILE"
chmod $PERM $INTERCEPTORS_CONFIG_FILE

echo -e "\nConfluent Platform Components:"

################################################################################
# Confluent Replicator (executable) for Confluent Cloud
################################################################################
REPLICATOR_PRODUCER_DELTA=$DEST/replicator-to-ccloud-producer.delta
echo "$REPLICATOR_PRODUCER_DELTA"
rm -f $REPLICATOR_PRODUCER_DELTA
cp $INTERCEPTORS_CONFIG_FILE $REPLICATOR_PRODUCER_DELTA
echo -e "\n# Confluent Replicator (executable) specific configuration" >>$REPLICATOR_PRODUCER_DELTA
echo "interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor" >>$REPLICATOR_PRODUCER_DELTA
REPLICATOR_SASL_JAAS_CONFIG=$SASL_JAAS_CONFIG
REPLICATOR_SASL_JAAS_CONFIG=${REPLICATOR_SASL_JAAS_CONFIG//\\=/=}
REPLICATOR_SASL_JAAS_CONFIG=${REPLICATOR_SASL_JAAS_CONFIG//\"/\\\"}
chmod $PERM $REPLICATOR_PRODUCER_DELTA

################################################################################
# Kafka Connect runs locally and connects to Confluent Cloud
################################################################################
CONNECT_DELTA=$DEST/connect-ccloud.delta
echo "$CONNECT_DELTA"
rm -f $CONNECT_DELTA
cat <<EOF >$CONNECT_DELTA
replication.factor=3
config.storage.replication.factor=3
offset.storage.replication.factor=3
status.storage.replication.factor=3
EOF
while read -r line; do
  if [[ ! -z $line && ${line:0:1} != '#' ]]; then
    if [[ ${line:0:9} == 'bootstrap' ]]; then
      line=${line/\\/}
      echo "$line" >>$CONNECT_DELTA
    fi
    if [[ ${line:0:4} == 'sasl' || ${line:0:3} == 'ssl' || ${line:0:8} == 'security' ]]; then
      echo "$line" >>$CONNECT_DELTA
    fi
  fi
done <"$CONFIG_FILE"
echo -e "\n# Connect producer and consumer specific configuration" >>$CONNECT_DELTA
while read -r line; do
  if [[ ! -z $line && ${line:0:1} != '#' ]]; then
    if [[ ${line:0:9} == 'bootstrap' ]]; then
      line=${line/\\/}
    fi
    if [[ ${line:0:4} == 'sasl' || ${line:0:3} == 'ssl' || ${line:0:8} == 'security' ]]; then
      echo "producer.$line" >>$CONNECT_DELTA
      echo "producer.confluent.monitoring.interceptor.$line" >>$CONNECT_DELTA
      echo "consumer.$line" >>$CONNECT_DELTA
      echo "consumer.confluent.monitoring.interceptor.$line" >>$CONNECT_DELTA
    fi
  fi
done <"$CONFIG_FILE"
cat <<EOF >>$CONNECT_DELTA

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
cat <<EOF >>$CONNECTOR_DELTA
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

cat <<EOF >>$ENV_CONFIG
export BOOTSTRAP_SERVERS='$BOOTSTRAP_SERVERS'
export SASL_JAAS_CONFIG='$SASL_JAAS_CONFIG'
export SASL_JAAS_CONFIG_PROPERTY_FORMAT='$SASL_JAAS_CONFIG_PROPERTY_FORMAT'
export REPLICATOR_SASL_JAAS_CONFIG='$REPLICATOR_SASL_JAAS_CONFIG'
export BASIC_AUTH_CREDENTIALS_SOURCE=$BASIC_AUTH_CREDENTIALS_SOURCE
export SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO=$SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO
export SCHEMA_REGISTRY_URL=$SCHEMA_REGISTRY_URL
EOF
chmod $PERM $ENV_CONFIG
