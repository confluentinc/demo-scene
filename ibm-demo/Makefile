export CONFLUENT_DOCKER_TAG = 6.1.1

build:
	docker-compose build

cluster: connectjars
	docker-compose up -d

ps:
	docker-compose ps

ibmjars:
	docker-compose up -d ibmmq
	#-docker exec -it -e LICENSE=accept -e MQ_QMGR_NAME=MQ1 ibmmq cp -Ra /opt/mqm/java/lib/  /project/mqlibs
	docker cp ibmmq:/opt/mqm/java/lib/ mqlibs
	docker-compose up -d ibmdb2
	docker cp ibmdb2:/opt/ibm/db2/V11.5/java/db2jcc4.jar connect-jars/db2jcc4.jar
	

topic:
	docker exec -it connect kafka-topics --bootstrap-server broker:29092 --create --topic ibmmq --partitions 1 --replication-factor 1
	docker exec -it connect kafka-topics --bootstrap-server broker:29092 --create --topic clickstream --partitions 1 --replication-factor 1
	
connectjars: ibmjars
	cp mqlibs/lib/jms.jar connect-jars/jms.jar
	cp mqlibs/lib/com.ibm.mq.allclient.jar connect-jars/com.ibm.mq.allclient.jar

connectsource: connectmqsource connectdatagen

	
connectmqsource: 
	docker exec -it connect curl -d "@/ibmmq/ibmmq-source.json" \
		-X PUT \
		-H "Content-Type: application/json" \
		http://connect:8083/connectors/ibmmq-source/config 

connectdb2source: 
	docker exec -it connect curl -d "@/ibmmq/ibmdb2-source.json" \
		-X PUT \
		-H "Content-Type: application/json" \
		http://connect:8083/connectors/ibmdb2-source/config 

connectdatagen: 
	docker exec -it connect curl -d "@/clickstream/clickstream-connector.json" \
		-X PUT \
		-H "Content-Type: application/json" \
		http://connect:8083/connectors/clickstream/config 

connectsink: 
	docker exec -it connect curl -d "@/ibmmq/ibmmq-sink.json" \
		-X PUT \
		-H "Content-Type: application/json" \
		http://connect:8083/connectors/ibmmq-sink/config 

down:
	docker-compose down
	-rm -rf mqlibs/*
	-rm -rf connect-jars/*

consumer:
	docker exec -it connect kafka-avro-console-consumer --bootstrap-server broker:29092 --topic ibmmq --from-beginning \
		--property schema.registry.url=http://schema-registry:8081 \
		--property consumer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor

