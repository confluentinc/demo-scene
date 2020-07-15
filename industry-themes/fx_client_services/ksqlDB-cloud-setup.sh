#create kafka cluster in UI

#creata a kafka API key/secret in UI

#get bootstrap server
pkc-ep9mm.us-east-2.aws.confluent.cloud:9092

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
ccloud environment use env-9wmxm

#get kafka cluster list
ccloud kafka cluster list

#set default cluster
ccloud kafka cluster use lkc-mz7g2

#get ksqlDB resource id
ccloud ksql app list

#get service account
ccloud ksql app configure-acls lksqlc-kjqo6 --dry-run

#set acls for service account on all topics
ccloud kafka acl create --allow --service-account 87874 --operation READ --topic '*'
ccloud kafka acl create --allow --service-account 87874 --operation WRITE --topic '*'
ccloud kafka acl create --allow --service-account 87874 --operation CREATE --topic '*'

#get api key for ksqlDB app
ccloud api-key create --resource lksqlc-kjqo6


#start the ksql cli to ksqlDB in CC
ksql -u <api-key> -p <api-secret> https://pksqlc-4vm1j.eastus.azure.confluent.cloud:443

