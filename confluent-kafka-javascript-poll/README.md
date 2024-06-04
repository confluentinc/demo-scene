# Tabs or Spaces? A Poll Powered by [confluent-kafka-javascript](https://github.com/confluentinc/confluent-kafka-javascript)



https://github.com/Cerchie/js-client-poll/assets/54046179/92370f88-6b9f-44c3-a30a-c9fd65768f0d



This is the code behind https://www.lets-settle-this.com/

The realtime data flow of votes is powered by [confluent-kafka-javascript](https://github.com/confluentinc/confluent-kafka-javascript). 

To get it running locally on your own:

## Get set up in Confluent Cloud

Sign up for [Confluent Cloud](https://www.confluent.io/confluent-cloud). 

### To add an environment:

- Open the Confluent Cloud Console and go to the Environments page at https://confluent.cloud/environments.
- Click 'Add cloud environment'.
- Enter a name for the environment: `js_poll_environment`
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

### Create 1 topic named `vote_state` with 1 partition. 

- From the navigation menu, click 'Topics', and then click 'Create topic'.
- In the Topic name field, type “voting_state”. Change the 'Partitions' field from 6 to 1. Enable infinite retention on the topic. Then select 'Create with defaults'.

## Get the code set up

### Clone this repo

```
git clone https://github.com/Cerchie/js-client-poll.git
```

### Set up your environment variables 

You'll need a file in the root directory named `.env`. 

In it, put your Confluent API key and secret:

```
CONFLUENT_API_SECRET={put_secret_here}
CONFLUENT_API_KEY={put_key_here}
```

### Seed your topic

Now you'll need to seed your topic with some data: 

```json
{
  "question-1": {
    "Tabs": 0,
    "Spaces": 0,
    "lastClicked": false
  },
  "question-2": {
    "merge": 0,
    "rebase": 0,
    "lastClicked": false
  },
  "question-3": {
    "debugger": 0,
    "print": 0,
    "lastClicked": false
  },
  "question-4": {
    "NoSQL": 0,
    "SQL": 0,
    "lastClicked": false
  },
  "question-5": {
    "Protobuf": 0,
    "Avro": 0,
    "lastClicked": false
  },
  "question-6": {
    "Vim": 0,
    "Emacs": 0,
    "lastClicked": false
  },
  "question-7": {
    "KStreams": 0,
    "Flink": 0,
    "lastClicked": false
  }
}
```

The code to seed your topic is set up for you in `seedproducer.js`. Run `node seedproducer.js` and check your topic in Confluent Cloud to confirm that the seed message arrived:

<img width="1340" alt="interface showing the JSON message above" src="https://github.com/Cerchie/js-client-poll/assets/54046179/6fbbf11c-b27c-4c80-b1e9-5038f43158eb">


### Run your code locally

Now you can run this command in the root directory:

```
node producer.js
```

and in a second terminal window in the same directory:

```
node consumer.js
```

Open up the browser interface with [Live Server](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer) (or however you view browser frontends) to see your app running locally!


https://github.com/Cerchie/js-client-poll/assets/54046179/1a5f0e80-ed34-46f9-98f9-389e8f4d1fb2