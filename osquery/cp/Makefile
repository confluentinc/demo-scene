TOPICS=open_sockets open_files logged_in_users shell_history listening_ports arp_cache processes syslog users

build:
	@echo "------------"
	docker build ../docker/osquery -t demo-scene/osquery
	@echo "------------"
	docker build ../docker/connect -t demo-scene/connect
	$(MAKE) java 
	@echo "------------"	
	docker-compose build

java:
	@echo "------------"	
	mvn -f ../java/demo/pom.xml clean package 

cluster: up

up:
	docker-compose up -d

pipeline:
	docker exec -it ksql-cli ksql  http://ksql-server:8088 <<EOF
		> RUN SCRIPT '/project/cp/ksql-commands.ksql';
		> exit
		> EOF
	
topics:
	# create your topics manually in case auto.create.topics.enable is set to true
	@echo 'creating topics'
	@for t in $(TOPICS); do \
		docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic $$t --partitions 1 --replication-factor 1 ; \
	done

ksql:
	docker exec -it ksql-cli ksql http://ksql-server:8088

down:
	docker-compose down

ps:
	docker-compose ps

login:
	docker exec -it --user jay host1 bash