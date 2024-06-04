const Kafka = require("@confluentinc/kafka-javascript");
require("dotenv").config();

const producer = new Kafka.Producer({
  "bootstrap.servers": ["pkc-921jm.us-east-2.aws.confluent.cloud:9092"],
  "security.protocol": "SASL_SSL",
  'client.id': 'kafka_votes',
  "sasl.mechanisms": "PLAIN",
  "sasl.username": process.env.CONFLUENT_API_KEY,
  "sasl.password": process.env.CONFLUENT_API_SECRET,
  'broker.version.fallback': '0.10.0',  // still needed with librdkafka 0.11.6 to avoid fallback to 0.9.0
  'log.connection.close' : false,
  'dr_cb': true
});


producer.connect()

producer.on('delivery-report', function(err, report) {
  console.log('delivery-report: ' + JSON.stringify(report));

});

let message = {
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
    },
    "question-8": {
      "bun": 0,
      "node": 0,
      "lastClicked": false
    }
  }
  producer.on('ready', function(arg) {
producer.produce("voting_state", 0, Buffer.from(JSON.stringify(message)))
  })

producer.on('event.error', (err) => {
  console.error('Error from producer');
  console.error(err);
})

producer.on('disconnected', function(arg) {
  console.log('producer disconnected. ' + JSON.stringify(arg));
});
