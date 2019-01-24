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