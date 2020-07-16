#create kafka cluster in UI

#creata a kafka API key/secret in UI

#get bootstrap server

#update producer config with API key/secret

#create topics UI or script
#CLIENT_CURRENCY_UPDATES
#CURRENCY_PAIRS_UPDATES
create_topics.sh

#create ksqlDB cluster in UI

#login into ccloud cli
ccloud login

#check default environment 
ccloud environment list

#set environment
ccloud environment use <env-id>

#get kafka cluster list
ccloud kafka cluster list

#set default cluster
ccloud kafka cluster use <cluster-id>

#get ksqlDB resource id
ccloud ksql app list

#get service account
ccloud ksql app configure-acls <ksqlDB-id> --dry-run

#set acls for service account on all topics
ccloud kafka acl create --allow --service-account <service-id> --operation READ --topic '*'
ccloud kafka acl create --allow --service-account <service-id> --operation WRITE --topic '*'
ccloud kafka acl create --allow --service-account <service-id> --operation CREATE --topic '*'

#get api key for ksqlDB app
ccloud api-key create --resource <ksqlDB-id>

#start the ksql cli to ksqlDB in CC
ksql -u <api-key> -p <api-secret> <ksqlDB-endpoint>
