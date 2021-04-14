## Order Service

### Description

This service is a standard Micronaut app with the Kafka feature. It contains an HTTP server to receive data from clients, and a Kafka Producer to send that data to a Kafka topic. It is the first in a three service workflow.

### Steps to run this service

Before running this service, you will need to update the `application.yml` file. Set the `bootstrap.servers` property to the Confluent Cloud endpoint, which you can get from Confluent Cloud, on the Clients page, or the Cluster settings. Also set the API Key and Secret in the `sasl.jaas.config` property. If you haven't already, you can create API Key and Secret on the API Access tab. 

You will also need to create a topic called `new-orders`.  You can do this on the Topics page of Confluent Cloud.

Once that is all set, you can run the service from the command line with `./gradlew run`.  Once it is up and running, you can use any HTTP client to post new orders in the following structure:

`{"customerNumber":"customer123", "item":"item1", "amount":19.95}`

Before posting data, open the Messages tab of the Confluent Cloud Topics page, and leave it open.  Then when you post data, you should see it there.  Alternatively, you can run a command line consumer to see the data written to your `new-order` topic.