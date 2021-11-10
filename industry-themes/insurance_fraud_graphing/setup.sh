#this demo uses Confluent Cloud and self-managed connectors using cp all in one cloud
#full instructions can be found here
#https://docs.confluent.io/platform/current/tutorials/build-your-own-demos.html#cp-all-in-one-cloud

#get your client configs fron Confluent Cloud and put them here
vi $HOME/.confluent/java.config

#download the ccloud library shell script
curl -sS -o ccloud_library.sh https://raw.githubusercontent.com/confluentinc/examples/latest/utils/ccloud_library.sh

#source the functions
source ./ccloud_library.sh

#generate the configs from your java config
ccloud::generate_configs $HOME/.confluent/java.config

#source the delta configs to environments variables
source delta_configs/env.delta

#start up the docker images
docker-compose up -d 

#bash into the connect container
docker exec -it connect bash

#install the Neo4j connector
confluent-hub install neo4j/kafka-connect-neo4j:1.0.9
#answer a bunch of questions

#restart connect
docker restart connect

#create topic auto_insurance_claims
ccloud kafka topic create auto_insurance_claims

#you'll need a ksqlDB app to run the ksql code
ccloud ksql app create claim_formatter

#run ksql in claim-formatter.ksql

#upload the connector configuration
curl -X POST http://localhost:8083/connectors \
  -H 'Content-Type:application/json' \
  -H 'Accept:application/json' \
  -d @neo4j-sink.json

#wait a bit
curl -X GET http://localhost:8083/connectors/Neo4jSinkConnector/status | jq

#run the producer to Confluent Cloud
bash < producers.sh

#open the Neo4j browser
http://localhost:7474/browser/

#cypher queries
MATCH (a:Adjuster)-[r:ADJUSTED]->(c:Claim)<-[PAID_BY]-(p:Payee)
RETURN a.name, count(r) as count, p.name
ORDER BY count DESC

MATCH (n:Adjuster)-[r:ADJUSTED]->(:Claim)
RETURN n.name, count(r) as count
ORDER BY count DESC

MATCH (a:Adjuster{name:'Nebula'})-[r:ADJUSTED]->(c:Claim)<-[PAID_BY]-(p:Payee)
RETURN p.name, count(r) as count
ORDER BY count DESC
