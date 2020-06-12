#!/bin/bash

echo -e $(date) "Firing up Docker Compose"
docker-compose up -d

# ---- Wait for Oracle DB to be up (takes several minutes to instantiate) ---
echo -e "\n--\n\n$(date) Waiting for Oracle to be available … ⏳"
grep -q "DATABASE IS READY TO USE!" <(docker-compose logs -f oracle)
echo -e "$(date) Installing rlwrap on Oracle container"
docker exec --interactive --tty --user root --workdir / $(docker ps --filter "name=oracle" --quiet) bash -c 'rpm -Uvh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm'
docker exec --interactive --tty --user root --workdir / $(docker ps --filter "name=oracle" --quiet) bash -c 'yum install -y rlwrap'

# ---- Set up Debezium source connector ---
export CONNECT_HOST=connect-debezium
echo -e "\n--\n\n$(date) Waiting for Kafka Connect to start on $CONNECT_HOST … ⏳"
grep -q "Kafka Connect started" <(docker-compose logs -f $CONNECT_HOST)
echo -e "\n--\n$(date) +> Creating Kafka Connect Oracle source (Debezium/XStream)"
docker-compose exec connect-debezium bash -c '/scripts/create-ora-source-debezium-xstream.sh'

# ---- Set up JDBC source connector ---
export CONNECT_HOST=kafka-connect
echo -e "\n--\n\n$(date) Waiting for Kafka Connect to start on $CONNECT_HOST … ⏳"
grep -q "Kafka Connect started" <(docker-compose logs -f $CONNECT_HOST)

echo -e "\n--\n$(date) +> Creating Kafka Connect Oracle source (JDBC)"
docker-compose exec kafka-connect bash -c '/scripts/create-ora-source-jdbc.sh'
