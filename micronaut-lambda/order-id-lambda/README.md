## Order ID Lambda

### Description

This is a Micronaut Serverless Function project. It has a handler class that will assign an orderId to NewOrder objects. 

### Steps to build and deploy this project

This project is ready to build.  Just run `./gradlew shadowJar`.  This will create the jar file `build/libs/order-id-lambda-0.1-all.jar`. Next in AWS Lambda create a new function, and choose Java 11 as the runtime. Set the function name to `orderIdLambda`. Then set the `handler` to `io.confluent.OrderRequestHandler`. Now, in the Code source page, click the upload button to upload the jar file. Once that is complete, you can hook the function to our applications using Confluent's AWS Lambda Sink connector.

In Confluent Cloud, go to the Connectors page, and select the AWS Lambda Sink connector. Enter the following values in the form:

topics: new-orders
Name: Leave default, or any name you'd like
Input messages: JSON
Kafka Cluster Credentials: Your Confluent Cloud API Key and Secret
AWS Credentials: The API Key and Secret for an IAM user that has access to the Lambda function
Function Name: orderIdLambda
AWS Lambda Invocation Type: Sync
Batch Size: 1
Tasks: 1

Click Next, and then Launch. The Connector will take a few minutes to provision.  Once it is done, go to the Topics page in Confluent Cloud, and you will see three new topics. One of them will start with the word `success`.  Copy that topic name to use in the `order-fulfillment-service`, which we'll build next.


## Handler

[AWS Lambda Handler](https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html)

Handler: io.confluent.BookRequestHandler

## Feature aws-lambda documentation

- [Micronaut AWS Lambda Function documentation](https://micronaut-projects.github.io/micronaut-aws/latest/guide/index.html#lambda)

