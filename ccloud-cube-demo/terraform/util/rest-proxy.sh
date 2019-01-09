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

cd /etc/confluent/confluent-5.1.0/etc/kafka-rest

cat > kafka-rest-ccloud.properties <<- "EOF"
${rest_proxy_properties}
EOF

########### Creating the Service ############

cat > /lib/systemd/system/kafka-rest.service <<- "EOF"
[Unit]
Description=Confluent Kafka REST
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=1
ExecStart=/etc/confluent/confluent-5.1.0/bin/kafka-rest-start /etc/confluent/confluent-5.1.0/etc/kafka-rest/kafka-rest-ccloud.properties
ExecStop=/etc/confluent/confluent-5.1.0/bin/kafka-rest-stop /etc/confluent/confluent-5.1.0/etc/kafka-rest/kafka-rest-ccloud.properties

[Install]
WantedBy=multi-user.target
EOF

############# Enable and Start ############

systemctl enable kafka-rest
systemctl start kafka-rest

############## Populate Data ##############

bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:8082)" != "200" ]]; do sleep 5; done'

curl -X POST -H "Content-Type: application/vnd.kafka.avro.v2+json" -d '{"value_schema_id" : 1, "records" : [{"value" : { "event" : { "name" : "ccloud-demo", "motion" : { "x" : 0, "y" : 0, "z" : 0 }, "speed" : { "x" : 0, "y" : 0, "z" : 0 }}}}]}' http://localhost:8082/topics/_EVENTS