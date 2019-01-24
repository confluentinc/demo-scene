#!/bin/bash

########### Update and Install ###########

yum update -y
yum install wget -y
yum install unzip -y
yum install java-1.8.0-openjdk-devel.x86_64 -y

########### Initial Bootstrap ###########

cd /tmp
curl -O ${confluent_platform_location}
unzip confluent-5.1.0-2.11.zip
mkdir /etc/confluent
mv confluent-5.1.0 /etc/confluent

########### Generating Props File ###########

cd /etc/confluent/confluent-5.1.0/etc/schema-registry

cat > schema-registry-ccloud.properties <<- "EOF"
${schema_registry_properties}
EOF

########### Creating the Service ############

cat > /lib/systemd/system/schema-registry.service <<- "EOF"
[Unit]
Description=Confluent Schema Registry
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=1
ExecStart=/etc/confluent/confluent-5.1.0/bin/schema-registry-start /etc/confluent/confluent-5.1.0/etc/schema-registry/schema-registry-ccloud.properties
ExecStop=/etc/confluent/confluent-5.1.0/bin/schema-registry-stop /etc/confluent/confluent-5.1.0/etc/schema-registry/schema-registry-ccloud.properties

[Install]
WantedBy=multi-user.target
EOF

########### Enable and Start ###########

systemctl enable schema-registry
systemctl start schema-registry

########### Register Schema ############


bash -c 'while netstat -lnt | awk '$4 ~ /:8081$/ {exit 1}'; do sleep 10; done'

curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" -d '{ "schema": "[{ \"type\": \"record\", \"name\": \"event\", \"fields\": [ { \"name\": \"name\", \"type\": \"string\" }, { \"name\": \"motion\", \"type\": { \"name\": \"motion\", \"type\": \"record\", \"fields\":[ { \"name\":\"x\", \"type\":\"int\" }, { \"name\":\"y\", \"type\":\"int\" }, { \"name\":\"z\", \"type\":\"int\" } ] } }, { \"name\": \"speed\", \"type\": { \"name\": \"speed\", \"type\": \"record\", \"fields\":[ { \"name\":\"x\", \"type\":\"int\" }, { \"name\":\"y\", \"type\":\"int\" }, { \"name\":\"z\", \"type\":\"int\" } ] } } ] } ]"}' http://localhost:8081/subjects/_EVENTS-value/versions