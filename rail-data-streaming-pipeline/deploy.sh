#!/bin/bash
source ./data/set_credentials_env.sh
source ./data/deploy_ksql.sh


echo "Checking stuff is running…"

docker exec -it kafka-connect bash -c 'echo -e "\n\n  Waiting for Kafka Connect to be available\n"; while : ; do curl_status=$(curl -s -o /dev/null -w %{http_code} http://localhost:8083/connectors) ; echo -e $(date) " Kafka Connect HTTP state: " $curl_status " (waiting for 200)" ; if [ $curl_status -eq 200 ] ; then  break ; fi ; sleep 5 ; done '
docker exec -it ksqldb bash -c 'echo -e "\n\n  Waiting for ksqlDB to be available\n"; while : ; do curl_status=$(curl -s -o /dev/null -w %{http_code} http://ksqldb:8088/info) ; echo -e $(date) " ksqlDB server listener HTTP state: " $curl_status " (waiting for 200)" ; if [ $curl_status -eq 200 ] ; then  break ; fi ; sleep 5 ; done '

echo -e "\n** Check output and press enter to continue"
read

echo "Creating CSV source connector to load location data"
./data/ingest/locations/00_ingest.sh

# Wait for a message to be present on the topic
sleep 5
echo ""
docker exec kafkacat kafkacat -b broker:29092 -t ukrail-locations -C -c1

echo -e "\n** Check output and press enter to continue"
read

echo "Loading reference data"
./data/ingest/movements/00_load_canx_reason_code.sh
./data/ingest/cif_schedule/00_ingest_schedule.sh

echo -e "\n** Check output and press enter to continue"
read

echo "Creating ksqlDB tables for reference data"
deploy_ksql ./data/ksql/01_location/00_location.ksql
deploy_ksql ./data/ksql/03_movements/01_canx_reason.ksql
deploy_ksql ./data/ksql/02_cif_schedule/01_schedule_raw.ksql
deploy_ksql ./data/ksql/02_cif_schedule/04_schedule.ksql
deploy_ksql ./data/ksql/02_cif_schedule/05_schedule_table.ksql

echo -e "\n** Check output and press enter to continue"
read

echo "Creating ActiveMQ source connectors"
./data/ingest/movements/00_ingest.sh

# Wait for a message to be present on the topic
sleep 10
echo ""
docker exec kafkacat kafkacat -b broker:29092 -t networkrail_train_mvt -C -c1

echo -e "\n** Check output and press enter to continue"
read

echo "Creating ksqlDB streams"
deploy_ksql ./data/ksql/03_movements/01_movement_raw.ksql
deploy_ksql ./data/ksql/03_movements/02_activations.ksql
query_ksql ./data/ksql/03_movements/03_activations_query.ksql
deploy_ksql ./data/ksql/03_movements/04_movements_nway.ksql
deploy_ksql ./data/ksql/03_movements/04_cancellations_nway.ksql
deploy_ksql ./data/ksql/03_movements/05_movement_stats.ksql
query_ksql ./data/ksql/03_movements/06_movement_stats_query.ksql

echo -e "\n** Check output and press enter to continue"
read

echo "Create Elasticsearch template config & sinks"
./data/egress/elasticsearch/00_create_template.sh
./data/egress/elasticsearch/00_set_kibana_config.sh
./data/egress/elasticsearch/01_create_sinks.sh


echo "------"
echo "DONE"
echo ""
echo "Now head over to http://localhost:5601/app/management/kibana/objects and import the pre-built objects"