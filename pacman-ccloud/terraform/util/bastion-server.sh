#!/bin/bash

cat > /home/ec2-user/cert.pem <<- "EOF"
${private_key_pem}
EOF

chmod 600 /home/ec2-user/cert.pem
chown ec2-user:ec2-user /home/ec2-user/cert.pem

cat > /etc/hosts <<- "EOF"
${rest_proxy_addresses} rest-proxy
${ksql_server_addresses} ksql-server
EOF
