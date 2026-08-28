# Applying Different Throughput Priorities to Mirror Topics with a Custom Script

This demo has a custom Node.JS script which uses the Cluster Linking lag REST API to detect for bandwidth issues and then pauses mirror topics, again via REST API, to prioritize traffic from certain topics over others.

# Video Demo

Watch a video demo and in-depth explanation here: https://drive.google.com/file/d/1hyy5Wazg8chzRkrfw88NBvpFDuUZ8rjV/view?usp=sharing

# Setup

This demo assumes:

## Source Cluster and Destination Cluster

You're running a source cluster and a destination cluster where the network bandwidth can be limited between the two of them. These clusters can be any Confluent clusters that support Cluster Linking.

In the video demo, the source cluster was a single broker CP 7.5 cluster running on the demoer's laptop. It was launched using these simple commands:

* `zookeeper-server-start simple-zookeeper.properties`
* `kafka-server-start simple-server.properties`

In the video demo, the destination cluster was a Confluent Cloud cluster, Dedicated, with Internet Networking. This can be provisioned at confluent.cloud

## Cluster Link

There should be a cluster link between the two clusters. It can be a regular cluster link, a bidirectional cluster link, or a source-initiated cluster link. It can have a mirror topic prefix--as the one did in the demo--but it doesn't have to.

In the demo, the cluster link was a source-initiated link. It was created by:

1. Creating a cluster link called `laptop_outpost_1` (name of your choosing) in the Confluent Cloud Console at confluent.cloud, and selecting "Confluent Platform Cluster" + "source-initiated link" as the source cluster. The command `./kafka-cluster cluster-id --bootstrap-server localhost:9092` was used to get the source cluster's cluster ID. If creating via the CLI--for example, on a Confluent Platform cluster--a sample `dest-link.config` is provided which can be passed in to the `kafka-cluster-links --create` or `confluent kafka link create` command.

1. Creating the source-side of the link with the command `./kafka-cluster-links --create --link laptop_outpost_1 --bootstrap-server localhost:9092 --config-file source-link.config --cluster-id <dest-cluster-id>`

## Topics & Mirror Topics

In the demo, topics were created on the source cluster by hand using this command: `./kafka-topics --create --bootstrap-server localhost:9092 --topic critical1 --partitions 1` (repeat for the other topic names, like `critical2`, `lowX` and `mediumX`). The topics can have multiple partitions if you'd like; a single partition topic was used in the demo since the demo was only running on one broker.

In the demo, mirror topics were created by hand in the destination cluster. This can be done in the Confluent Cloud Console, or via REST API, Terraform, or CLI. For example, the command `./kafka-mirrors --create --source-topic <source-topic-name> --mirror-topic <mirror-topic-name> --link laptop_outpost_1 --bootstrap-server <dest-bootstrap-server>`

The mirror topics' names--including the prefixes, if given--need to be hardcoded into the `apply-priorities.js` script. Alternatively, you can source the topic priority list from a database or GUI.

# Running the Script

The `apply-priorities.js` script requires:

* NodeJS installed on the machine it will run on
* The machine it will run on needs connectivity (access) to the Destination cluster (not necessarily to the source cluster)
* The hardcoded variables in the script--bootstrap server, cluster ID, mirror topic names, and base-64 encoded API Key: API Secret--need to be replaced with ones you have access to.

There are two ways to run the prioritizing script:

* `node apply-priorities.js` will do one run of the priorities
* `./run-apply-priorities.sh` will run 