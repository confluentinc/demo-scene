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
mkdir /etc/confluent/confluent-5.1.0/data

########### Generating Props File ###########

cd /etc/confluent/confluent-5.1.0/etc/ksql

cat > ksql-server-ccloud.properties <<- "EOF"
${ksql_server_properties}
EOF

########### Creating the Service ############

cat > /lib/systemd/system/ksql-server.service <<- "EOF"
[Unit]
Description=Confluent KSQL Server
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=1
ExecStart=/etc/confluent/confluent-5.1.0/bin/ksql-server-start /etc/confluent/confluent-5.1.0/etc/ksql/ksql-server-ccloud.properties
ExecStop=/etc/confluent/confluent-5.1.0/bin/ksql-server-stop /etc/confluent/confluent-5.1.0/etc/ksql/ksql-server-ccloud.properties

[Install]
WantedBy=multi-user.target
EOF

########### Enable and Start ###########

systemctl enable ksql-server
systemctl start ksql-server

############# Populate Data ############

bash -c 'while netstat -lnt | awk '$4 ~ /:808$/ {exit 1}'; do sleep 10; done'

bash -c '/etc/confluent/confluent-5.1.0/bin/kafka-console-producer --broker-list ${broker_list} --producer.config /etc/confluent/confluent-5.1.0/etc/ksql/ksql-server-ccloud.properties --topic _NUMBERS --property "parse.key=true" --property "key.separator=:" <<EOF
1:{"NUMBER" : 1, "X": 1, "Y" : 0, "Z" : 0}
2:{"NUMBER" : 2, "X": 1, "Y" : -90, "Z" : 1}
3:{"NUMBER" : 3, "X": -180, "Y" : 0, "Z" : 180}
4:{"NUMBER" : 4, "X": 1, "Y" : 90, "Z" : -1}
EOF'