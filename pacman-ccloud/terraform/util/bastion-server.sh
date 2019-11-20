#!/bin/bash

cat > /home/${user}/cert.pem <<- "EOF"
${private_key_pem}
EOF

chmod 600 /home/${user}/cert.pem
chown ${user}:${user} /home/${user}/cert.pem

cat > /etc/hosts <<- "EOF"
${rest_proxy_addresses} rest-proxy
${ksql_server_addresses} ksql-server
EOF

