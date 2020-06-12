#!/bin/sh

# this is a hack but seems to work…

echo 'Restarting Capture process'

sqlplus sys/Admin123@//localhost:1521/ORCLCDB as sysdba <<- EOF

  call DBMS_CAPTURE_ADM.START_CAPTURE('CAP\$_DBZXOUT');

  exit;
EOF
