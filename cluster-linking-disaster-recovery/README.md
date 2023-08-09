# Cluster Linking & Schema Linking disaster recovery

The idea of this demo is to create a main cluster and a disaster recovery cluster, the `product` schema and data are created in the main cluster/schema registry and it is replicated to the disaster recovery cluster using Schema and Cluster Linking. We then, stop the main cluster, move consumers and producers to disaster recovery, restart the main cluster and move the data back

## Start the clusters

```shell
    docker-compose up -d
```

Two CP clusters are running:

*  Main Control Center available at [http://localhost:19021](http://localhost:19021/)
*  Disaster Recovery Control Center available at [http://localhost:29021](http://localhost:29021/)
*  Main Schema Registry available at [http://localhost:8085](http://localhost:8085/)
*  Disaster Recovery Schema Registry available at [http://localhost:8086](http://localhost:8086/)

## Create the topic `product` and the schema `product-value` in the main cluster

###  Create the schema `product-value`  and another one 

```shell
    curl -v -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data @data/product.avsc http://localhost:8085/subjects/product-value/versions
    curl -v -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data @data/product.avsc http://localhost:8085/subjects/other-topic-value/versions
```

###  Create the topic `product` (and another topic)
```shell
    docker-compose exec mainKafka kafka-topics --bootstrap-server mainKafka:19092 --topic product --create --partitions 1 --replication-factor 1
    docker-compose exec mainKafka kafka-topics --bootstrap-server mainKafka:19092 --topic other-topic --create --partitions 1 --replication-factor 1
```

### Open a consumer on main cluster

```shell
    docker-compose exec mainSchemaregistry \
        kafka-avro-console-consumer --bootstrap-server mainKafka:19092 \
        --property schema.registry.url=http://mainSchemaregistry:8085 \
        --group test-group \
        --from-beginning \
        --topic product
```

###  Produce some data (the last lines with the product data)
```shell
   docker-compose exec mainSchemaregistry kafka-avro-console-producer \
    --bootstrap-server mainKafka:19092 \
    --topic product \
    --property value.schema.id=1 \
    --property schema.registry.url=http://mainSchemaregistry:8085 \
    --property auto.register=false \
    --property use.latest.version=true

    { "product_id": 1, "product_name" : "rice"} 
    { "product_id": 2, "product_name" : "beans"} 
```

### Check current offset
```shell
docker-compose exec mainKafka kafka-consumer-groups --bootstrap-server mainKafka:19092 --group test-group --describe

Consumer group 'test-group' has no active members.

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
test-group      product         0          2               2               0               -               -               -
```

As you can see offset is 2 (two messages consumed).

## Create the Schema Linking (main to disaster cluster)

### Create a config file on main Schema Registry host.

```shell
    docker-compose exec mainSchemaregistry bash -c '\
    echo "schema.registry.url=http://disasterSchemaregistry:8086" > /home/appuser/config.txt'
```

### Create the schema exporter 
```shell
    docker-compose exec mainSchemaregistry bash -c '\
    schema-exporter --create --name main-to-disaster-sl --subjects "product-value" \
    --config-file ~/config.txt \
    --schema.registry.url http://mainSchemaregistry:8085 \
    --context-type NONE'
```

### Validate exporter is working
```shell
    docker-compose exec mainSchemaregistry bash -c '\
    schema-exporter --list \
    --schema.registry.url http://mainSchemaregistry:8085'
````

### Check the exporter is running
```shell
    docker-compose exec mainSchemaregistry bash -c '\
    schema-exporter --get-status --name main-to-disaster-sl --schema.registry.url http://mainSchemaregistry:8085' | jq
```

### Check the schema is the same in the main and disaster cluster

```shell
    curl http://localhost:8085/subjects/product-value/versions/1 | jq
    curl http://localhost:8086/subjects/product-value/versions/1 | jq
```

## Create the Cluster Linking (main to disaster cluster)

### Create config file to configure the Cluster Linking
```shell
docker-compose exec disasterKafka bash -c '\
echo "\
bootstrap.servers=mainKafka:19092
consumer.offset.sync.enable=true 
consumer.offset.group.filters="{\"groupFilters\": [{\"name\": \"*\",\"patternType\": \"LITERAL\",\"filterType\": \"INCLUDE\"}]}"
" > /home/appuser/cl.properties'
```
### Create the cluster link on the *destination* cluster. We are using some extra [configuration options](https://docs.confluent.io/platform/current/multi-dc-deployments/cluster-linking/configs.html#configuration-options).
```shell
    docker-compose exec disasterKafka \
    kafka-cluster-links --bootstrap-server disasterKafka:29092 \
    --create --link main-to-disaster-cl \
    --config-file /home/appuser/cl.properties
``` 

### Create the mirroring
```shell
    docker-compose exec disasterKafka \
    kafka-mirrors --create \
    --source-topic product \
    --mirror-topic product \
    --link main-to-disaster-cl \
    --bootstrap-server disasterKafka:29092        
``` 

### Verifying Cluster Linking is up

```shell
    docker-compose exec disasterKafka kafka-cluster-links --bootstrap-server disasterKafka:29092 --link main-to-disaster-cl --list
 ````

Output is similar to `Link name: 'main-to-disaster-cl', link ID: 'CdDrHuV5Q5Sqyq0TCXnLsw', remote cluster ID: 'nBu7YnBiRsmDR_WilKe6Og', local cluster ID: '1wnpnQRORZ-C2tdxEStVtA', remote cluster available: 'true'`

### Verifying consumer group offset is migrated

Note this propagation can take some time to be available due to the async behavior of Cluster Linking. In this demo, it should only take a few seconds.

```shell
docker-compose exec disasterKafka kafka-consumer-groups --bootstrap-server disasterKafka:29092 --group test-group --describe

Consumer group 'test-group' has no active members.

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
test-group      product         0          2               2               0               -               -               -
```

Same results from source cluster.

### Verifying data is migrated

```shell
    docker-compose exec disasterSchemaregistry \
        kafka-avro-console-consumer --bootstrap-server disasterKafka:29092 \
        --property schema.registry.url=http://disasterSchemaregistry:8086 \
        --from-beginning \
        --topic product
```
messages are migrated as expected

## Simulating a disaster

### Stop main cluster (and all consumer or producers you have created)

```shell
docker-compose stop mainKafka mainZookeeper mainSchemaregistry mainControlCenter
```

## FAILOVER: Promote disaster cluster to principal cluster

### Promote topic to writable

1. Stop mirroring

Note: we are using `--failover` because the main cluster is unavailable, if we want to sync before promoting the topic, we should use the option `--promote` instead.

```shell
    docker-compose exec disasterKafka \
        kafka-mirrors --bootstrap-server disasterKafka:29092 \
        --failover --topics product
```

2. Verify that the mirror topic is not a mirror anymore

```shell
    docker-compose exec disasterKafka \
        kafka-mirrors --bootstrap-server disasterKafka:29092 \
        --describe --topics product 
```

The result should have the `State: STOPPED` as part of it.

###  Produce some data (the last lines with the product data)
```shell
   docker-compose exec disasterSchemaregistry \
   kafka-avro-console-producer \
    --bootstrap-server disasterKafka:29092 \
    --topic product \
    --property value.schema.id=1 \
    --property schema.registry.url=http://disasterSchemaregistry:8086 \
    --property auto.register=false \
    --property use.latest.version=true


{ "product_id": 3, "product_name" : "tomato"} 
{ "product_id": 4, "product_name" : "lettuce"} 
```

### Promote schema to writable mode

1. Change the mode to WRITABLE

```shell
    curl --silent -X PUT http://localhost:8086/mode/product-value -d "{  \"mode\": \"READWRITE\"}"    -H "Content-Type: application/json"
    # confirm change was applied
    curl --silent -X GET http://localhost:8086/mode/product-value
```

2. Create a new version with optional field
   
```shell
    curl -v -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data @data/product2.avsc http://localhost:8086/subjects/product-value/versions
```

3. Produce data with new schema

Produce some data (the last lines with the product data)
Note: as describe in [avro documentation](https://avro.apache.org/docs/1.10.2/spec.html#json_encoding). We need to define the type of the optional field to avoid collisions.

```shell
   docker-compose exec disasterSchemaregistry \
   kafka-avro-console-producer \
    --bootstrap-server disasterKafka:29092 \
    --topic product \
    --property value.schema.id=2 \
    --property schema.registry.url=http://disasterSchemaregistry:8086 \
    --property auto.register=false \
    --property use.latest.version=true

{"product_id":5,"product_name":"meat","product_category":null}
{"product_id":6,"product_name":"pepper","product_category":{"string":"fresh"}}
```

4. Verify new data is correctly produced and the consumer consumes from the new offset (ids 3 to 6).

```shell
    docker-compose exec disasterSchemaregistry \
        kafka-avro-console-consumer --bootstrap-server disasterKafka:29092 \
        --property schema.registry.url=http://disasterSchemaregistry:8086 \
        --group test-group \
        --topic product
```
See the last messages as expected (id 3 to 6).

### FAILOVER complete

At this point, the topic `product` is a normal topic in the disaster cluster and could receive data. Subject `product-value` is a normal schema, that can be evolved as we did adding the version 2 with an optional field.

## FAILBACK Promote main cluster as main cluster again (failback)

### Start main cluster

Restart the main cluster

```shell
docker-compose start mainKafka mainZookeeper mainSchemaregistry mainControlCenter
```

### Remove the old Schema Linking (main to disaster)

```shell
# pause is needed before deleting
docker-compose exec mainSchemaregistry bash -c '\
    schema-exporter --pause --name main-to-disaster-sl --schema.registry.url http://mainSchemaregistry:8085'
# delete it
docker-compose exec mainSchemaregistry bash -c '\
    schema-exporter --delete --name main-to-disaster-sl --schema.registry.url http://mainSchemaregistry:8085'
```

You should see the message `Successfully deleted exporter main-to-disaster-sl`

### Delete content from main cluster (it will be migrated again)

Safest way is to trust the Cluster and Schema Linking and remigrate data from disaster to main as disaster could have more data (that is the case of this demo)

```shell
# delete topic
docker-compose exec mainKafka kafka-topics --bootstrap-server mainKafka:19092 --topic product --delete
# delete the subject
curl -v -X DELETE 'http://localhost:8085/subjects/product-value'
curl -v -X DELETE 'http://localhost:8085/subjects/product-value?permanent=true'
```

### Create the Schema Linking (disaster to main)

1. Create the config file

```shell
docker-compose exec disasterSchemaregistry bash -c '\
  echo "schema.registry.url=http://mainSchemaregistry:8085" > /home/appuser/config.txt'
```

2. Create the schema exporter 

```shell
    docker-compose exec disasterSchemaregistry bash -c '\
    schema-exporter --create --name disaster-to-main-sl --subjects "product-value" \
    --config-file ~/config.txt \
    --schema.registry.url http://disasterSchemaregistry:8086 \
    --context-type NONE'
```

3. Validate exporter is working
   
```shell
    docker-compose exec disasterSchemaregistry bash -c '\
    schema-exporter --list \
    --schema.registry.url http://disasterSchemaregistry:8086'
````

4. Check the exporter is running

```shell
    docker-compose exec disasterSchemaregistry bash -c '\
    schema-exporter --get-status --name disaster-to-main-sl --schema.registry.url http://disasterSchemaregistry:8086' | jq
```

5. Check the schemas are the same in the main and disaster

```shell
    curl http://localhost:8085/subjects/product-value/versions/2 | jq
    curl http://localhost:8086/subjects/product-value/versions/2 | jq
```

### Create Cluster Linking (disaster to main)

1. Create config file to configure the Cluster Linking

```shell
docker-compose exec mainKafka bash -c '\
echo "\
bootstrap.servers=disasterKafka:29092
consumer.offset.sync.enable=true 
consumer.offset.group.filters="{\"groupFilters\": [{\"name\": \"*\",\"patternType\": \"LITERAL\",\"filterType\": \"INCLUDE\"}]}"
" > /home/appuser/cl.properties'
```

2. Create the cluster link on the *destination* cluster. We are using some extra [configuration options](https://docs.confluent.io/platform/current/multi-dc-deployments/cluster-linking/configs.html#configuration-options).

```shell
    docker-compose exec mainKafka \
    kafka-cluster-links --bootstrap-server mainKafka:19092 \
    --create --link disaster-to-main-cl \
    --config-file /home/appuser/cl.properties
``` 

3. Create the mirroring
   
```shell
    docker-compose exec mainKafka \
    kafka-mirrors --create \
    --source-topic product \
    --mirror-topic product \
    --link disaster-to-main-cl \
    --bootstrap-server mainKafka:19092
``` 

4. Verifying Cluster Linking is up

```shell
    docker-compose exec mainKafka kafka-cluster-links --bootstrap-server mainKafka:19092 --link disaster-to-main-cl --list
 ````

Output is similar to `Link name: 'disaster-to-main-cl', link ID: '-FPTBi8JQnGskNQzbrLLmA', remote cluster ID: 'KxjPLtiZQaWPId1UORsRvg', local cluster ID: '59HpxdWkSLSlnqjXnX_ZIw', remote cluster available: 'true'`

5. Verifying consumer group offset is migrated

Note this propagation can take some time to be available due to the async behavior of Cluster Linking. In this demo, it should only take a few seconds.

```shell
docker-compose exec mainKafka kafka-consumer-groups --bootstrap-server mainKafka:19092 --group test-group --describe

Consumer group 'test-group' has no active members.

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
test-group      product         0          6               6               0               -               -               -
```

Same results from disaster cluster.

6. Verifying data is migrated

```shell
    docker-compose exec mainSchemaregistry \
        kafka-avro-console-consumer --bootstrap-server mainKafka:19092 \
        --property schema.registry.url=http://mainSchemaregistry:8085 \
        --from-beginning \
        --topic product
```
messages are migrated as expected

### Remove the cluster and Schema Linking (disaster to main) as all data is already migrated 

We need to promote the topic and schema in the main cluster as normal topic and schema. We need to run similar commands as we did for the failover steps

1. Stop topic mirroring

Note: as the disaster cluster is up, we use the option `--promote` instead that will confirm all data is migrated.

```shell
    docker-compose exec mainKafka \
        kafka-mirrors --bootstrap-server mainKafka:19092 \
        --promote --topics product

# output will be similar to
Calculating max offset and ms lag for mirror topics: [product]
Finished calculating max offset lag and max lag ms for mirror topics: [product]
Request for stopping topic product's mirror was successfully scheduled. Please use the describe command with the --pending-stopped-only option to monitor progress.
```

2. Verify that the mirror topic is not a mirror anymore

```shell
    docker-compose exec mainKafka \
        kafka-mirrors --bootstrap-server mainKafka:19092 \
        --describe --topics product 
```

The result should have the `State: STOPPED` as part of it.

3. Remove the Schema Linking (disaster to main)

```shell
# pause is needed before deleting
docker-compose exec disasterSchemaregistry bash -c '\
    schema-exporter --pause --name disaster-to-main-sl --schema.registry.url http://disasterSchemaregistry:8086'
# delete it
docker-compose exec disasterSchemaregistry bash -c '\
    schema-exporter --delete --name disaster-to-main-sl --schema.registry.url http://disasterSchemaregistry:8086'
```

4. Change the mode of the subject to WRITABLE 

```shell
    curl --silent -X PUT http://localhost:8085/mode/product-value -d "{  \"mode\": \"READWRITE\"}"    -H "Content-Type: application/json"
    # confirm change was applied
    curl --silent -X GET http://localhost:8085/mode/product-value
```

### work as normal in the main cluster

1. Create a new version with optional field
   
```shell
    curl -v -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data @data/product3.avsc http://localhost:8085/subjects/product-value/versions
```

2. Produce data with new schema

Produce some data (the last lines with the product data)
```shell
   docker-compose exec mainSchemaregistry \
   kafka-avro-console-producer \
    --bootstrap-server mainKafka:19092 \
    --topic product \
    --property value.schema.id=3 \
    --property schema.registry.url=http://mainSchemaregistry:8085 \
    --property auto.register=false \
    --property use.latest.version=true

{"product_id":7,"product_name":"cucumber","product_category":{"string":"fresh"}, "product_origin":{"string":"spain"}}
```

3. Verify new data is correctly produced and the consumer consumes from the new offset (ids 3 to 6).

```shell
    docker-compose exec mainSchemaregistry \
        kafka-avro-console-consumer --bootstrap-server mainKafka:19092 \
        --property schema.registry.url=http://mainSchemaregistry:8085 \
        --group test-group \
        --topic product
```
See the last message as expected (id 6).

### Recreate the disaster to main cluster

In order to keep the data safe, we need to recreate the cluster and Schema Linking from main to disaster, the steps are the same as above.

1. Delete the topic `product` on disaster cluster
2. Delete the schema `product-value` on disaster schema registry

```shell
# delete topic
docker-compose exec disasterKafka kafka-topics --bootstrap-server disasterKafka:29092 --topic product --delete
# delete the subject
curl -v -X DELETE 'http://localhost:8086/subjects/product-value'
curl -v -X DELETE 'http://localhost:8086/subjects/product-value?permanent=true'
```

3. Repeat the steps to create 
   1. the Schema Linking (all steps)
   2. the cluster mirror linking on disaster cluster. Note: the Cluster Linking `main-to-destination-cl` does not need to be recreated, as it was never deleted.



## [Original repo](https://github.com/tomasalmeida/cluster-schema-linking-examples)