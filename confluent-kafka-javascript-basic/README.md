# Use confluent-kafka-javascript to produce and consume records

This is a basic example of using the [confluent-kafka-javascript](https://github.com/confluentinc/confluent-kafka-javascript) client (early access as of when this was written, 4/29/24). 

In this example, you'll produce database metadata records (made in the example with faker.js) to a Kafka topic and consume the records from the same topic. 

![CC UI image](/graph.png)

## Step 1: Clone and install

`git clone https://github.com/Cerchie/basic-confluent-kafka-javascript-example.git` 

`npm install` 

## Step 2: Get set up in Confluent Cloud

Sign up for [Confluent Cloud](https://www.confluent.io/confluent-cloud). 

### To add an environment:

- Open the Confluent Cloud Console and go to the Environments page at https://confluent.cloud/environments.
- Click 'Add cloud environment'.
- Enter a name for the environment: `db_metadata`
- Click 'Create'.

Skip any prompting or options for stream governance.

### Then, create a cluster inside your environment.

- Click 'Add cluster'.
- On the 'Create cluster' page, for the 'Basic' cluster, select 'Begin configuration'.
- When you're prompted to select a provider and location, choose AWS's `us-east-2`.
- Select 'Continue'.
- Specify a cluster name, review the configuration and cost information, and then select 'Launch cluster'.
- Depending on the chosen cloud provider and other settings, it may take a few minutes to provision your cluster, but after the cluster has provisioned, the 'Cluster Overview' page displays.
  

### Create a Confluent Cloud API key and save it. 

Note: if you have more than one environment, follow the secondary set of instructions below. 

- From the 'Administration' menu, click 'Cloud API keys' or go to https://confluent.cloud/settings/api-keys.

- Click 'Add key'.

- Choose to create the key associated with your user account.

- The API key and secret are generated and displayed.

- Click 'Copy' to copy the key and secret to a secure location.

*Important*

_The secret for the key is only exposed initially in the Create API key dialog and cannot be viewed or retrieved later from the web interface. Store the secret and its corresponding key in a secure location. Do not share the secret for your API key._

- (Optional, but recommended) Enter a description of the API key that describes how you intend to use it, so you can distinguish it from other API keys.

- Select the confirmation check box that you have saved your key and secret.

- Click 'Save'. The key is added to the keys table.

_Instructions for if you have more than one environment:_

- go to the Environments page at https://confluent.cloud/environments and select the environment.
- Select the Cluster.
- Select 'API Keys' then under 'Cluster Overview'.
- Click '+ Add key'. The API key and secret are generated and displayed.
- Click 'Copy' to copy the key and secret to a secure location.

The secret for the key is only displayed in the 'Create API key' dialog and cannot be viewed or retrieved later. Store the API key and secret in a secure location. Do not share the secret for your API key.

- (Optional, but recommended) Enter a description of the API key that describes how you intend to use it, so you can distinguish it from other API keys. Confirm that you have saved your key and secret.
- Click 'Continue'. The key is added to the API keys table.

### Create 1 topic with 1 partition each named `db_metadata`. 

- From the navigation menu, click 'Topics', and then click 'Create topic'.
- In the Topic name field, type “db_metadata”. Change the 'Partitions' field from 6 to 1. Then select 'Create with defaults'.

## Step 3: Set up your .env file

Add a `.env` file to the top lever of the app directory. 

The contents should read:

```
BOOTSTRAP_SERVERS=${bootstrap_server_address_here}
USERNAME=${confluent_cloud_api_key_here}
PASSWORD=${confluent_cloud_api_secret_here}
```

## Step 4: Run the app

In one terminal window, run 

`node producer.js`

and in the other

`node consumer.js` 

https://github.com/Cerchie/basic-confluent-kafka-javascript-example/assets/54046179/62a8c715-9856-4842-848d-e97a175a6ec8

you can doubly verify that this is working by visiting your Confluent Cloud topic in the CC UI:

![CC UI image](/db_records.png)