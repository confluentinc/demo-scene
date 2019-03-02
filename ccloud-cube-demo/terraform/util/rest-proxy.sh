#!/bin/bash

########### Update and Install ###########

yum update -y
yum install wget -y
yum install unzip -y
yum install java-1.8.0-openjdk-devel.x86_64 -y
yum install git -y
yum install maven -y

########### Initial Bootstrap ###########

cd /tmp
curl -O ${confluent_platform_location}
unzip confluent-5.1.0-2.11.zip
mkdir /etc/confluent
mv confluent-5.1.0 /etc/confluent

############ Jaeger Tracing #############

cd /tmp
git clone https://github.com/riferrei/jaeger-tracing-support.git
cd jaeger-tracing-support
mvn clean
mvn compile
mvn install
cd target
cp jaeger-tracing-support-1.0.jar /etc/confluent/confluent-5.1.0/share/java/monitoring-interceptors

cd /etc/confluent/confluent-5.1.0/share/java/monitoring-interceptors
curl -O https://riferrei.net/wp-content/uploads/2019/03/dependencies.zip
unzip dependencies.zip

cd /tmp
wget ${jaeger_tracing_location}
tar -xvzf jaeger-1.10.0-linux-amd64.tar.gz
mkdir /etc/jaeger
mv jaeger-1.10.0-linux-amd64 /etc/jaeger

cat > /etc/jaeger/jaeger-1.10.0-linux-amd64/jaeger-agent.yaml <<- "EOF"
reporter:
  type: tchannel
  tchannel:
    host-port: ${jaeger_collector}
EOF

########### Generating Props File ###########

cd /etc/confluent/confluent-5.1.0/etc/kafka-rest

cat > kafka-rest-ccloud.properties <<- "EOF"
${rest_proxy_properties}
EOF

cat > interceptorsConfig.json <<- "EOF"
{
   "services":[
      {
         "service":"REST Proxy",
         "config":{
            "sampler":{
               "type":"const",
               "param":1
            },
            "reporter":{
               "logSpans":true
            }
         },
         "topics":[
            "_EVENTS"
         ]
      }
   ]
}
EOF

########### Creating the Service ############

cat > /lib/systemd/system/jaeger-agent.service <<- "EOF"
[Unit]
Description=Jaeger Agent
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=1
ExecStart=/etc/jaeger/jaeger-1.10.0-linux-amd64/jaeger-agent --config-file=/etc/jaeger/jaeger-1.10.0-linux-amd64/jaeger-agent.yaml

[Install]
WantedBy=multi-user.target
EOF

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

systemctl enable jaeger-agent
systemctl start jaeger-agent

systemctl enable kafka-rest
systemctl start kafka-rest