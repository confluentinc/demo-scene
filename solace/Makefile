build:
	docker-compose build

cluster:
	docker-compose up -d

ps:
	docker-compose ps

topics:
	docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic from-solace-messages --partitions 1 --replication-factor 1
	docker exec -it broker kafka-topics --bootstrap-server broker:9092 --create --topic to-solace-messages --partitions 1 --replication-factor 1

queue:
	curl -X POST -u admin:admin http://localhost:8080/SEMP/v2/config/msgVpns/default/queues \
		-H "content-type: application/json" \
		-d '{"queueName":"connector-quickstart","accessType":"non-exclusive","egressEnabled":true,"ingressEnabled":true,"permission":"delete"}'

	curl -X POST -u admin:admin http://localhost:8080/SEMP/v2/config/msgVpns/default/queues \
		-H "content-type: application/json" \
		-d '{"queueName":"connector-quickstart-sink","accessType":"non-exclusive","egressEnabled":true,"ingressEnabled":true,"permission":"delete"}'

send:
	curl -X POST -d "m1" http://localhost:9000/Queue/connector-quickstart -H "Content-Type: text/plain" -H "Solace-Message-ID: $$RANDOM"

connect:
	docker exec -it connect curl -d "@/project/solace-source.json" \
		-X PUT \
		-H "Content-Type: application/json" \
		http://connect:8083/connectors/SolaceSourceConnector/config 

	docker exec -it connect curl -d "@/project/solace-sink.json" \
		-X PUT \
		-H "Content-Type: application/json" \
		http://connect:8083/connectors/SolaceSinkConnector/config 


consumer:
	#docker exec -it connect kafka-console-consumer --bootstrap-server broker:29092 --topic from-solace-messages --from-beginning
	docker exec -it connect kafka-avro-console-consumer --bootstrap-server broker:29092 --topic from-solace-messages --from-beginning \
		--property schema.registry.url=http://schema-registry:8081 \
		--property consumer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor

producer:
	docker exec -it connect kafka-console-producer --broker-list broker:29092 --topic to-solace-messages \
		--property consumer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor


down:
	docker-compose down
