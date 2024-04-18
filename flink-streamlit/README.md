# How to use FlinkSQL with Kafka, Streamlit, and the Alpaca API

Pssst. A simulation of this app is currently deployed at https://st-flink-kafka-simulation.streamlit.app/

Learn how to use these 4 technologies together by running this demo yourself! 

In this project you'll produce stock trade events from the [Alpaca API markets](https://app.alpaca.markets) websocket to an [Apache Kafka](https://kafka.apache.org/) topic located in [Confluent Cloud](https://www.confluent.io/lp/confluent-cloud). From there, use [FlinkSQL](https://nightlies.apache.org/flink/flink-docs-master/docs/dev/table/sql/overview/) in Confluent Cloud to generate 5 sec averages of stock prices over a tumbling window. Then, you'll consume the averages from a backup Kafka topic and display them using [Streamlit](https://streamlit.io/). 

<img width="718" alt="graph of the 4 technologies" src="https://github.com/Cerchie/alpaca-kafka-flink-streamlit/assets/54046179/7600d717-69bc-46c5-8679-d8d65b9ce810">


## Step 1: Get set up in Confluent Cloud

Sign up for [Confluent Cloud](https://www.confluent.io/confluent-cloud). 

### To add an environment:

- Open the Confluent Cloud Console and go to the Environments page at https://confluent.cloud/environments.
- Click 'Add cloud environment'.
- Enter a name for the environment: `stocks_environment`
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

### Create 1 topic with 1 partition each named `SPY`. 

- From the navigation menu, click 'Topics', and then click 'Create topic'.
- In the Topic name field, type “SPY”. Change the 'Partitions' field from 6 to 1. Then select 'Create with defaults'.

### Create an API key for Schema Registry. 

- In the environment for which you want to set up Schema Registry, find 'Credentials' on the right side panel and click <someNumber> keys to bring up the API credentials dialog. (If you are just getting started, click 0 keys.)
- Click 'Add key' to create a new Schema Registry API key.
- When the API Key and API Secret are saved, click the checkbox next to 'I have saved my API key and secret and am ready to continue', and click 'Continue'. Your new Schema Registry key is shown on the Schema Registry API access key list.

### For the topic, set a JSON schema:

- From the navigation menu, click 'Topics', then click a topic to select it (or create a new one).
- Click the 'Schema' tab.
- Click 'Set a schema'. The Schema editor appears.
- Select a schema type: JSON.
- The basic structure of a schema appears prepopulated in the editor as a starting point. Erase and enter the following schema in the editor:

```json
{
  "$id": "http://example.com/myURI.schema.json",
  "$schema": "http://json-schema.org/draft-07/schema#",
  "additionalProperties": false,
  "description": "Sample schema to help you get started.",
  "properties": {
    "bid_timestamp": {
      "description": "The string type is used for strings of text.",
      "type": "string"
    },
    "price": {
      "description": "JSON number type.",
      "type": "number"
    },
    "symbol": {
      "description": "The string type is used for strings of text.",
      "type": "string"
    }
  },
  "title": "SampleRecord",
  "type": "object"
}
```
- Click Create.

_There are two urls you'll need later. Your bootstrap server url can be found under Cluster Overview -> Cluster Settings, and your Schema Registry URL is under Environments -> stocks_environments_

### Create a Flink compute pool. 

- In the navigation menu, click 'Environments' and click the tile for the environment where you want to use Flink SQL.

- In the environment details page, click 'Flink'.

- In the Flink page, click 'Compute pools', if it’s not selected already.

- Click 'Create compute pool' to open the 'Create compute pool' page.

- In the 'Region' dropdown, select the region that hosts the data you want to process with SQL. Click 'Continue'.

*Important*

_This region must the same region as the one in which you created your cluster, that is, AWS's `us-east-2`_

- In the 'Pool' name textbox, enter “stocks-compute-pool”.

- In the 'Max CFUs' dropdown, select 10. 

- Click 'Continue', and on the 'Review and create' page, click 'Finish'.

A tile for your compute pool appears on the Flink page. It shows the pool in the Provisioning state. 

_It may take a few minutes for the pool to enter the Running state._

### Run FlinkSQL

In the cell of the new workspace, you can start running SQL statements. Copy and paste this statement into the workspace:


```sql
CREATE TABLE tumble_interval_SPY
(`symbol` STRING, `window_start` STRING,`window_end` STRING,`price` DOUBLE, PRIMARY KEY (`symbol`) NOT ENFORCED)
    WITH ('value.format' = 'json-registry');
```
- Click 'Run'.

The above statement creates a Flink table with fields indicating the end and start of a window, and a price for each end and start.

You'll need to run this statement as well. Click the '+' symbol to add a statement in the workspace.


```sql
INSERT INTO tumble_interval_SPY
SELECT symbol, DATE_FORMAT(window_start,'yyyy-MM-dd hh:mm:ss.SSS'), DATE_FORMAT(window_end,'yyyy-MM-dd hh:mm:ss.SSS'), AVG(price)
FROM TABLE(
        TUMBLE(TABLE SPY, DESCRIPTOR($rowtime), INTERVAL '5' SECONDS))
GROUP BY
    symbol,
    window_start,
    window_end;

```

The above statement inserts data into the table you just created. Every five seconds, it takes the average for the SPY stock price and adds it to the `tumble_interval_SPY` table. 

Note: no data will show up in these Kafka topics or Flink tables until you run the app.

## Step 2: Your Alpaca credentials

You'll need to sign up for [sign up for alpaca](https://alpaca.markets/) to get a key. 

Navigate to https://app.alpaca.markets.

_You do not have to give a tax ID or fill out the other account info to get your paper trading key._

Generate a key using the widget you'll find on the right of the screen on the home page. This is the key/secret you'll be adding to the app. 

## Step 3: Get started running the app

`git clone https://github.com/Cerchie/finnhub.git && cd finnhub`

then

`pip install -r requirements.txt` 

Now, create a file in the root directory named `.streamlit/secrets.toml` (that initial `.` is part of the convention.)

In it, you'll need:

```
ALPACA_KEY = "your_alpaca_key"
ALPACA_SECRET = "your_alpaca_secret"
SASL_USERNAME = "your_confluent_cloud_api_key"
SASL_PASSWORD = "your_confluent_cloud_api_secret"
SR_URL =  "your_confluent_cloud
BASIC_AUTH_USER_INFO = "your_confluent_cloud_schema_registry_key:your_confluent_cloud_schema_registry_secret"
BOOTSTRAP_URL = "your_confluent_cloud_bootstrap_url"
```
Note that the `:` is necessary for `BASIC_AUTH_USER_INFO`. 

You'll need a [Streamlit account](https://streamlit.io/) as well for the [secrets to be in the environment](https://docs.streamlit.io/streamlit-community-cloud/deploy-your-app/secrets-management). 

Now, run `streamlit run alpacaviz.py` in your root dir in order to run the app. 

To deploy on Streamlit yourself, follow the [instructions here](https://docs.streamlit.io/streamlit-community-cloud/deploy-your-app) and make sure to [include the secrets](https://docs.streamlit.io/streamlit-community-cloud/deploy-your-app/secrets-management) in your settings. 