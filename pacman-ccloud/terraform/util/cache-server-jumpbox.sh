#!/bin/bash

yum update -y
amazon-linux-extras install redis4.0 -y

cat > /etc/hosts <<- "EOF"
${cache_server} cache-server
EOF
