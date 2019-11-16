TOPICS=open_sockets open_files logged_in_users shell_history listening_ports arp_cache processes syslog users

build:
	@echo "------------"
	docker build ../docker/osquery -t demo-scene/osquery
	@echo "------------"
	docker build ../docker/connect -t demo-scene/connect
	@echo "------------"	
	docker-compose build

cluster: up

up:
	docker-compose up -d
	
topics:
	@echo 'creating topics'
	@for t in $(TOPICS); do \
		docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic $$t --partitions 1 --replication-factor 1 ; \
	done

connect:
	# docker exec -it connect /project/certs/gen-certs.sh
	docker exec -it connect curl -d "@/project/docker/connect/elasticsearch-sink-processes.json" \
		-X PUT \
		-H "Content-Type: application/json" \
		http://connect:8083/connectors/elasticsearch-sink-processes/config 

ksql:
	docker exec -it ksql-cli ksql http://ksql-server:8088

down:
	docker-compose down

ps:
	docker-compose ps
