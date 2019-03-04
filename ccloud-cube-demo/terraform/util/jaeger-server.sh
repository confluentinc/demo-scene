#!/bin/bash

########### Update and Install ###########

yum update -y
yum install wget -y

########### Initial Bootstrap ###########

cd /tmp
wget ${jaeger_tracing_location}
tar -xvzf jaeger-1.10.0-linux-amd64.tar.gz
mkdir /etc/jaeger
mv jaeger-1.10.0-linux-amd64 /etc/jaeger

########### Creating the Service ############

cat > /lib/systemd/system/jaeger-server.service <<- "EOF"
[Unit]
Description=Jaeger Server
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=1
ExecStart=/etc/jaeger/jaeger-1.10.0-linux-amd64/jaeger-all-in-one

[Install]
WantedBy=multi-user.target
EOF

########### Enable and Start ###########

systemctl enable jaeger-server
systemctl start jaeger-server