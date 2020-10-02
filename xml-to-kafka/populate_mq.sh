#!/bin/bash
# Ref: https://www.ibm.com/support/knowledgecenter/SSFKSJ_9.1.0/com.ibm.mq.ref.dev.doc/q130750_.htm

curl --insecure --request POST 'https://localhost:9443/ibmmq/rest/v2/messaging/qmgr/QM1/queue/DEV.QUEUE.1/message' \
     --header 'ibm-mq-rest-csrf-token: rmoff' \
     --header 'Content-Type: application/xml;charset=utf-8' \
     --user app:Admin123 \
     --data-raw '<note> <to>Tove</to> <from>Jani</from> <heading>Reminder 01</heading> <body>Don'\''t forget me this weekend!</body> </note>'

curl --insecure --request POST 'https://localhost:9443/ibmmq/rest/v2/messaging/qmgr/QM1/queue/DEV.QUEUE.1/message' \
     --header 'ibm-mq-rest-csrf-token: rmoff' \
     --header 'Content-Type: application/xml;charset=utf-8' \
     --user app:Admin123 \
     --data-raw '<note> <to>Jani</to> <from>Tove</from> <heading>Reminder 02</heading> <body>Of course I won'\''t!</body> </note>'

curl --insecure --request POST 'https://localhost:9443/ibmmq/rest/v2/messaging/qmgr/QM1/queue/DEV.QUEUE.1/message' \
     --header 'ibm-mq-rest-csrf-token: rmoff' \
     --header 'Content-Type: application/xml;charset=utf-8' \
     --user app:Admin123 \
     --data-raw '<note> <to>Tove</to> <from>Jani</from> <heading>Reminder 03</heading> <body>Where are you?</body> </note>'

curl --insecure --request POST 'https://localhost:9443/ibmmq/rest/v2/messaging/qmgr/QM1/queue/DEV.QUEUE.1/message' \
     --header 'ibm-mq-rest-csrf-token: rmoff' \
     --header 'Content-Type: application/xml;charset=utf-8' \
     --user app:Admin123 \
     --data-raw '<note> <to>Jani</to> <from>Tove</from> <heading>Reminder 04</heading> <body>I forgot 🤷‍♂️</body> </note>'

