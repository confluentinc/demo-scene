# Cluster Linking & Schema Linking

The idea of this demo is to create a source cluster and a destination cluster, the product schema and data is created in the source cluster and it is replicated to the destination cluster using schema and Cluster Linking. We make use of schema contexts to isolate subjects and topic prefixing.

## Start the cluster

```shell
    docker-compose up -d
```

Two CP clusters are running:

*  Source/Hub Control Center available at [http://localhost:19021](http://localhost:19021/)
*  Destination Control Center available at [http://localhost:29021](http://localhost:29021/)
*  Source Schema Registry available at [http://localhost:8085](http://localhost:8085/)
*  Destination Schema Registry available at [http://localhost:8086](http://localhost:8086/)

## Create the topic `product` and the schema `product-value` in the source cluster

###  Create the schema `product-value` 

```shell
    curl -v -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data @data/product.avsc http://localhost:8085/subjects/product-value/versions
```

###  Create the topic `product`
```shell
    docker-compose exec srcKafka kafka-topics --bootstrap-server srcKafka:19092 --topic product --create --partitions 1 --replication-factor 1
```

### Open a consumer on src cluster

```shell
    docker-compose exec srcSchemaregistry \
        kafka-avro-console-consumer --bootstrap-server srcKafka:19092 \
        --property schema.registry.url=http://srcSchemaregistry:8085 \
        --group test-group \
        --from-beginning \
        --topic product
```

###  Produce some data (the last lines with the product data)
```shell
   docker-compose exec srcSchemaregistry kafka-avro-console-producer \
    --bootstrap-server srcKafka:19092 \
    --topic product \
    --property value.schema.id=1 \
    --property schema.registry.url=http://srcSchemaregistry:8085 \
    --property auto.register=false \
    --property use.latest.version=true

    { "product_id": 1, "product_name" : "rice"} 
    { "product_id": 2, "product_name" : "beans"} 
```

### Check current offset
```shell
    docker-compose exec srcKafka kafka-consumer-groups --bootstrap-server srcKafka:19092 --group test-group --describe

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                           HOST            CLIENT-ID
test-group    product         0          2               2               0               console-consumer-7ad3463a-282e-475e-b852-f0363abf66a9 /172.18.0.7     console-consumer
```

As you can see offset is 2 (two messages consumed).

## Create the Schema Linking

### Create a config file on Source Schema Registry host.

```shell
    docker-compose exec srcSchemaregistry bash -c 'echo "schema.registry.url=http://destSchemaregistry:8086" > /home/appuser/config.txt'
```

### As we will the schema with a new name, we need to set this new subject to import mode
```shell
    curl --silent -X PUT http://localhost:8086/mode/:.source:source.product-value -d "{  \"mode\": \"IMPORT\"}"    -H "Content-Type: application/json"
    # confirm change was applied
    curl --silent -X GET http://localhost:8086/mode/:.source:source.product-value
```

### Create the schema exporter 
```shell
    docker-compose exec srcSchemaregistry bash -c "\
    schema-exporter --create --name source-to-destination-sl --subjects \":*:\" \
    --config-file ~/config.txt \
    --schema.registry.url http://srcSchemaregistry:8085 \
    --subject-format 'source.\${subject}' \
    --context-type CUSTOM --context-name source"
```

### Validate exporter is working
```shell
    docker-compose exec srcSchemaregistry bash -c '\
    schema-exporter --list \
    --schema.registry.url http://srcSchemaregistry:8085'
````

### Check the exporter is running
```shell
    docker-compose exec srcSchemaregistry bash -c '\
    schema-exporter --get-status --name source-to-destination-sl --schema.registry.url http://srcSchemaregistry:8085' | jq
```

### Check the schema is the same in the source and destination

```shell
    curl http://localhost:8085/subjects/product-value/versions/1 | jq
    curl http://localhost:8086/subjects/:.source:source.product-value/versions/1 | jq
```

Check the name is prefixed with `:.source:`, it is the context. Also the subject is prefixed, as we forced it too, by `source.`.

## Create the Cluster Linking

### Create config file to configure the 
```shell
docker-compose exec destKafka bash -c 'echo "\
cluster.link.prefix=source.
bootstrap.servers=srcKafka:19092
consumer.offset.sync.enable=true 
consumer.group.prefix.enable=true
consumer.offset.group.filters="{\"groupFilters\": [{\"name\": \"*\",\"patternType\": \"LITERAL\",\"filterType\": \"INCLUDE\"}]}"
" > /home/appuser/cl.properties'
```
### Create the cluster link on the *destination* cluster. We are using some extra [configuration options](https://docs.confluent.io/platform/current/multi-dc-deployments/cluster-linking/configs.html#configuration-options).
```shell
    docker-compose exec destKafka \
    kafka-cluster-links --bootstrap-server destKafka:29092 \
    --create --link source-to-destination-cl \
    --config-file /home/appuser/cl.properties
``` 

### Create the mirroring
```shell
    docker-compose exec destKafka \
    kafka-mirrors --create \
    --source-topic product \
    --mirror-topic source.product \
    --link source-to-destination-cl \
    --bootstrap-server destKafka:29092        
``` 

### Verifying Cluster Linking is up

```shell
    docker-compose exec destKafka kafka-cluster-links --bootstrap-server destKafka:29092 --link source-to-destination-cl --list
 ````

Output is similar to `Link name: 'source-to-destination-cl', link ID: 'CdDrHuV5Q5Sqyq0TCXnLsw', remote cluster ID: 'nBu7YnBiRsmDR_WilKe6Og', local cluster ID: '1wnpnQRORZ-C2tdxEStVtA', remote cluster available: 'true'`

### Verifying consumer group offset is migrated

Note this propagation can take some time to be available due to the async behavior of Cluster Linking. In this demo, it should only take a few seconds.

```shell
docker-compose exec srcKafka kafka-consumer-groups --bootstrap-server destKafka:29092 --group source.test-group --describe

Consumer group 'source.test-group' has no active members.

GROUP             TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
source.test-group source.product  0          2               2               0               -               -               -
```

Same results from source cluster (note the consumer group is prefixed with `source.` as we configured the Cluster Linking to do)

### Verifying data is migrated

```shell
    docker-compose exec destSchemaregistry \
        kafka-avro-console-consumer --bootstrap-server destKafka:29092 \
        --property schema.registry.url=http://destSchemaregistry:8086 \
        --from-beginning \
        --topic source.product
```
messages are migrated as expected

## [Original repo](https://github.com/tomasalmeida/cluster-schema-linking-examples)