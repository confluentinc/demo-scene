# confluent-xml-demo

In this demo, you'll use live data from the British government on cycle hires and pipe it into a Kafka topic! Note: this is an updated version of the project Robin Moffat outlines in [this blog post](https://rmoff.net/2020/10/01/ingesting-xml-data-into-kafka-option-1-the-dirty-hack/). 

Let's get started.

## Prerequisites

1. A [Confluent Cloud](https://rebrand.ly/f0kvol5) account. 
2. Install [python-yq](https://pypi.org/project/yq/). This will allow you to use xq, which parses xml into JSON output. 
3. Install kcat using the [instructions](https://github.com/edenhill/kcat) from the repo README. 

## Step 1: Test the endpoint and  `python-yq`

Run this command in your terminal:

```bash
curl --show-error --silent https://tfl.gov.uk/tfl/syndication/feeds/cycle-hire/livecyclehireupdates.xml
```

You'll see the live results of your query in XML! 

Now, run this command. The `'.stations.station[] + {lastUpdate: .stations."@lastUpdate"}'` bit will create a JSON structure. 

```bash
curl --show-error --silent https://tfl.gov.uk/tfl/syndication/feeds/cycle-hire/livecyclehireupdates.xml | xq -c '.stations.station[] + {lastUpdate: .stations."@lastUpdate"}'
```

The output will be in the following format: 

```bash
{"id":"850","name":"Brandon Street, Walworth","terminalName":"300060","lat":"51.489102","long":"-0.0915489","installed":"true","locked":"false","installDate":null,"removalDate":null,"temporary":"false","nbBikes":"11","nbStandardBikes":"10","nbEBikes":"1","nbEmptyDocks":"11","nbDocks":"22","lastUpdate":"1677702601270"}
{"id":"851","name":"The Blue, Bermondsey","terminalName":"300059","lat":"51.492221","long":"-0.06251309999998966","installed":"true","locked":"false","installDate":"1666044000000","removalDate":null,"temporary":"false","nbBikes":"17","nbStandardBikes":"17","nbEBikes":"0","nbEmptyDocks":"5","nbDocks":"22","lastUpdate":"1677702601270"}
{"id":"852","name":"Coomer Place, West Kensington","terminalName":"300006","lat":"51.4835707","long":"-0.2020387000000028","installed":"true","locked":"false","installDate":null,"removalDate":null,"temporary":"false","nbBikes":"7","nbStandardBikes":"5","nbEBikes":"2","nbEmptyDocks":"20","nbDocks":"27","lastUpdate":"1677702601270"}
```

Let's move on to setup in the Confluent Cloud interface. 

## Step 2: Set up a topic and client in Confluent Cloud. 

 Log in to the Confluent Cloud interface. 
 
 Sign in to your Confluent Cloud account. Head over to the [confluent.cloud/environments](https://confluent.cloud/environments) page and click 'Add Cloud Environment' on the top right of your screen. 

<img width="1459" alt="click Add Cloud Environment on the top right" src="https://user-images.githubusercontent.com/54046179/220384774-b7518172-d674-4f92-ab80-6b4ac7aa6cf4.png">

Name your environment 'cycle_data' and click 'Create'. Note: If you're prompted to select a Stream Governance package, you can skip that option. 

On your cluster page, click 'Create cluster on my own' or 'Create cluster'. 

In the navbar on the left, select 'Topics' and then the 'Add topic' button over on the right. Follow the prompts to create a topic named 'livecyclehireupdates_01' with the defaults selected. 

Now, in that left-hand navbar, select 'Clients' and then the 'new client' button. Select 'Python' for now, and save the `bootstrap.servers` value in the copy-pasteable window that pops up. Create and download a Kafka cluster API key using the prompt on that page as well. 

After you have those values, you can configure your `kcat` utility! [Use the instructions written by Kris Jenkins](http://blog.jenkster.com/2022/10/setting-up-kcat-config.html), and the values you've just generated. 

## Step 3: Pipe your data into the topic! 

Run the command:

```bash
curl --show-error --silent https://tfl.gov.uk/tfl/syndication/feeds/cycle-hire/livecyclehireupdates.xml | \
    xq -c '.stations.station[] + {lastUpdate: .stations."@lastUpdate"}' | \
    kcat -t livecyclehireupdates_01 -P
```

Now, you can view the data from that query in your Kafka topic in the Confluent Cloud interface (Topics -> livecyclehireupdates_01):

<img width="1251" alt="cycle message" src="https://user-images.githubusercontent.com/54046179/222260001-5496e0ca-0dc6-408f-8782-285dbe140bed.png">
