#!/bin/bash

########### Update and Install ###########

yum install wget -y
yum install unzip -y
yum install java-1.8.0-openjdk-devel.x86_64 -y

########### Initial Bootstrap ###########

cd /tmp
wget ${confluent_platform_location}
unzip confluent-5.3.1-2.12.zip
mkdir /etc/confluent
mv confluent-5.3.1 /etc/confluent

########### Generating Props File ###########

cat > ${confluent_home_value}/etc/kafka-rest/rest-proxy-ccloud.properties <<- "EOF"
${rest_proxy_properties}
EOF

############ Custom Start Script ############

cat > ${confluent_home_value}/bin/startRestProxy.sh <<- "EOF"
#!/bin/bash

cat > /tmp/config.properties <<- "IEND"

ssl.endpoint.identification.algorithm=https
sasl.mechanism=PLAIN
request.timeout.ms=20000
bootstrap.servers=${bootstrap_server}
retry.backoff.ms=500
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="${cluster_api_key}" password="${cluster_api_secret}";
security.protocol=SASL_SSL

IEND

${confluent_home_value}/bin/kafka-topics --bootstrap-server ${bootstrap_server} \
    --command-config /tmp/config.properties --create --topic USER_GAME \
    --partitions 6 --replication-factor 3 || true

${confluent_home_value}/bin/kafka-topics --bootstrap-server ${bootstrap_server} \
    --command-config /tmp/config.properties --create --topic USER_LOSSES \
    --partitions 6 --replication-factor 3 || true

export KAFKAREST_HEAP_OPTS="-Xms8g -Xmx8g"
${confluent_home_value}/bin/kafka-rest-start ${confluent_home_value}/etc/kafka-rest/rest-proxy-ccloud.properties

EOF

chmod 775 ${confluent_home_value}/bin/startRestProxy.sh

########### Creating the Service ############

cat > /lib/systemd/system/rest-proxy.service <<- "EOF"
[Unit]
Description=Confluent REST Proxy
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=1
ExecStart=${confluent_home_value}/bin/startRestProxy.sh
ExecStop=${confluent_home_value}/bin/kafka-rest-stop ${confluent_home_value}/etc/kafka-rest/rest-proxy-ccloud.properties

[Install]
WantedBy=multi-user.target
EOF

############# Enable and Start ############

systemctl enable rest-proxy
systemctl start rest-proxy
