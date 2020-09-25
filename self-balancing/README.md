# Overview

## Product

### Definition

Self-balancing simplifies the management of Kafka clusters in the following ways:
* When the load on the cluster is unevenly distributed, Self-balancing will automatically rebalance partitions to optimize performance
* When a new broker is added to the cluster, Self-balancing will automatically fill it with partitions
* When an operator wants to remove a broker, she can call a Self-balancing API to shut down the broker and drain the partitions from it
* When a broker has been down for a certain amount of time, Self-balancing will automatically reassign the partitions to other brokers

### Deployment

Self-balancing runs on the Confluent Server brokers and does not introduce any new dependencies.

## Demo

### About

This demo showcases the main features of Self-balancing Clusters, which debuts with the release of Confluent Platform CP 6.0.0

### Requirements

In order to run this demo, you will need:
* Docker
* AWS CLI [configured to pull from ECS](https://confluentinc.atlassian.net/wiki/spaces/TOOLS/pages/107872257/Finding+CI+Artifacts+Where+s+My+Stuff)

### What's included

This demo will pull the following images:
* ZooKeeper
* Kafka
* Confluent Control Center (optional to also try out the GUI)


# Test scenarios

## Uneven load

We will create uneven load in the cluster and watch Self-balancing address this condition.

1. Run `docker-compose`

  `docker-compose -f kafka-0-1-2.yml up` (we will be looking at the logs to see some interesting information so it's recommended to run it in the foreground).

  This will start ZooKeeper, 3 Confluent Server brokers and Confluent Control Center.

2. Create a topic

  ```
  kafka-topics \
    --bootstrap-server localhost:9092 \
    --create \
    --topic sbk \
    --replica-assignment 0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1,0:1
  ```

  We are forcing the topic to not create replicas in broker 2 to create an uneven load.

3. Produce data

  ```
  kafka-producer-perf-test \
    --producer-props bootstrap.servers=localhost:9092 \
    --topic sbk \
    --record-size 1000 \
    --throughput 1000 \
    --num-records 3600000
  ```

  This will produce data at about 1MB/s during 1h.

4. Start watching changes in the topic

  ```
    watch kafka-topics \
      --bootstrap-server localhost:9092 \
      --describe \
      --topic sbk 
  ```

  This will show changes in replica assignments, in-sync replicas, as well as
  relevant information such as replication throttling details. Optionally,
  Confluent Control Center can be used to watch the same changes.

5. Wait for Self-balancing to start the rebalance

   Self-balancing samples data about disk use, leader count, partition count,
   network throughput and other variables. It then aggregates this data to make
   informed decisions about how to reassign partitions. It needs between 10 and 20
   minutes from startup to collect enough samples to generate a rebalacing plan (if
   one is needed). Self-balancing also invalidates previous samples when the number
   of partitions in the cluster changes materially since they may not accurately
   reflect the current state of the cluster.

While Self-balancing is still sampling, the following message will appear on the logs periodically:
`INFO Skipping proposal precomputing because load monitor does not have enough snapshots.`

6. Watch Self-balancing rebalance the cluster

   Once Self-balacing is ready to compute reassignment plans, the following message will appear:
   `INFO Finished the precomputation proposal candidates in * ms (com.linkedin.kafka.cruisecontrol.analyzer.GoalOptimizer)`

   Self-balancing should then start the rebalancing process. Monitor the logs and
   the `watch kafka-topics` command to observe the changes as they happen.

## Expansion

We will now add 2 more brokers to the cluster and watch Self-balancing fill them with partitions.

1. Run `docker-compose`

  `docker-compose -f kafka-0-1-2.yml -f kafka-3-4.yml up --no-recreate` will add 2 brokers to the setup that was
  running previously (do not stop any of the  processes from the first part of the tutorial).

2. Watch Self-balancing rebalance the cluster

   Self-balancing should be able to use the data it has already sampled, and the
   rebalance should kick off almost immediately.

   Note in the logs how Self-balancing is also detecting the fact that we are
   rebalancing to a new broker, and not just the fact that the cluster is out of
   balance. This is important because Self-balancing offers a config called
   `confluent.balancer.heal.uneven.load.trigger` which can be set to either
   `ANY_UNEVEN_LOAD` or `EMPTY_BROKER`. `EMPTY_BROKER` will only rebalance when
   Self-balancing finds an empty broker (this expansion scenario), but
   `ANY_UNEVEN_LOAD` will rebalance when the load is uneven regardless of the cause
   (the previous uneven load scenario).

## Shrinkage

We will now remove a broker from the cluster and watch Self-balancing shut it down and drain the partitions.

1. Trigger a broker removal

   For this example, we will be using the Confluent Server REST API.

   First, `curl localhost:8090/kafka/v3/clusters` will return a collection with all
   the clusters the REST API can address. In this case, there is only one cluster.
   Note the value of `data[0].cluster_id` as the id of our cluster.

   Then, `curl -X DELETE localhost:8090/kafka/v3/clusters/:cluster_id/brokers/3`
   (replace `:cluster_id` with the cluster id from the previous step) will trigger
   the removal of broker 3. This should return `202 Accepted`.

   Alternatively, you can perform this step with the new CLI tool, `kafka-remove-brokers`, that ships with Confluent Platform 6.0.0.
   The equivalent CLI command is:

   ```
   kafka-remove-brokers \
     --bootstrap-server localhost:9092 \
     --delete \
     --broker-id 3
   ```

  Finally, this operation can also be executed from Confluent Control Center.

2. Watch Self-balancing remove the broker

   Self-balancing should be able to use the data it has already sampled, and the
   removal should kick off almost immediately. The removal, however, is a
   long-running process (because it involves data movement).

   `curl localhost:8090/kafka/v3/clusters/:cluster_id/remove-broker-tasks/3` will
   return a `partition_reassignment_status` and a `broker_shutdown_status` for the
   broker removal task.

   The alternative for CLI users in this case is:

   ```
   kafka-remove-brokers \
     --bootstrap-server localhost:9092 \
     --describe \
     --broker-id 3
   ```

## Broker failure

We will now simulate a broker failure and watch Self-balancing address this condition.

1. Stop a container

   `docker container stop $(docker container ls -q --filter name=kafka4)` will stop the container running broker 4.

2. Watch Self-balancing create new replicas

   Self-balancing should be able to use the data it has already sampled, and the creation of new replicas should kick off almost immediately.

   Note that we have also set the config `confluent.balancer.heal.broker.failure.threshold.ms` to `5000`, meaning that
   Self-balancing will consider the broker dead after only 5s. This is not suitable for production environments, where
   typically timeouts should be set to between 30min and 1h (or whatever is most reasonable for the customer), but it is
   helpful here so that the demo can run faster.
