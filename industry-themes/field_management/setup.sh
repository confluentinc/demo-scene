#create confluent cloud cluster

#start confluent local on docker
docker-compose up

#create topics
#cloud
#local
#create topic called UTILITY_WORK_ORDERS_TRUCK_JOINED.REPLICA
#use replication factor 1 and min isr 1

#start producers
cd producer
#local first
bash < local-produer.sh
#cloud
bash < cloud-producer.sh

#start cloud to local Replicator
cd replicator
bash < replicator.sh

#setup ksqlDB in CC
ccloud ksql app list

#get service account
ccloud ksql app configure-acls <ksqlDB-id> --dry-run

#set acls for service account on all topics
ccloud kafka acl create --allow --service-account <service-account-id> --operation READ --topic '*'
ccloud kafka acl create --allow --service-account <service-account-id> --operation WRITE --topic '*'
ccloud kafka acl create --allow --service-account <service-account-id> --operation CREATE --topic '*'

#start ksqlDB
cd ksqlDB
#cloud
ksql -u <API-key> -p <API-secret> <ksqlDB-endpoint>
run script cloud-field-management.ksql
#local
ksql
run script local-field-management.ksql

#run pull query against local ksqlDB
SELECT * FROM UTILITY_WORK_ORDERS_LOOKUP WHERE WORK_ORDER_ID = 5;