## Order Fulfillment Service

### Description
This service is an example of a Micronaut messaging application. It does not have an HTTP server, and only communicates to the rest of our system through events in a Kafka topic.

It is the third, in three-part workflow. You should have the `order-service`, and `order-id-lambda` projects running before attempting to run this one.
  
### Steps to run this service
In `OrderConsumer.java`, on line 10, you will need to enter the name of the topic that `Order` events will be in.  This topic will be the `success` topic of the Lambda Sink connector that was setup in an earlier step.

In `application.yml` you will need to set the `bootstrap.servers` property to the Confluent Cloud endpoint, which you can get from Confluent Cloud, on the Clients page, or the Cluster settings. Also set the API Key and Secret in the `sasl.jaas.config` property. If you haven't already, you can create API Key and Secret on the API Access tab. 

Once these changes are made, you can run this project, from the command line, with `./gradlew run`

You should see any `Order` events in the `success` topic showing in the terminal.

