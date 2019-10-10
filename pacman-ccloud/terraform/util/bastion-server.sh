#!/bin/bash

cat > /etc/hosts <<- "EOF"
${rest_proxy_addresses} rest-proxy
${ksql_server_addresses} ksql-server
EOF
