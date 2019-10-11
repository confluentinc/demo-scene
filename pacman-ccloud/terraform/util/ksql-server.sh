#!/bin/bash

########### Update and Install ###########

yum install wget -y
yum install unzip -y
yum install java-1.8.0-openjdk-devel.x86_64 -y

########### Initial Bootstrap ###########

cd /tmp
wget ${confluent_platform_location}
unzip confluent-5.3.0-2.12.zip
mkdir /etc/confluent
mv confluent-5.3.0 /etc/confluent
mkdir ${confluent_home_value}/data

########### Generating Props File ###########

cat > ${confluent_home_value}/etc/ksql/ksql-server-ccloud.properties <<- "EOF"
${ksql_server_properties}
EOF

############ Custom Start Script ############

cat > ${confluent_home_value}/bin/startKSQL.sh <<- "EOF"
#!/bin/bash

export KSQL_HEAP_OPTS="-Xms8g -Xmx8g"
${confluent_home_value}/bin/ksql-server-start ${confluent_home_value}/etc/ksql/ksql-server-ccloud.properties

EOF

chmod 775 ${confluent_home_value}/bin/startKSQL.sh

########### Creating the Service ############

cat > /lib/systemd/system/ksql-server.service <<- "EOF"
[Unit]
Description=Confluent KSQL Server
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=1
ExecStart=${confluent_home_value}/bin/startKSQL.sh
ExecStop=${confluent_home_value}/bin/ksql-server-stop ${confluent_home_value}/etc/ksql/ksql-server-ccloud.properties

[Install]
WantedBy=multi-user.target
EOF

########### Enable and Start ###########

systemctl enable ksql-server
systemctl start ksql-server
