const { Kafka } = require('@confluentinc/kafka-javascript').KafkaJS;
// the consumerStart function is from the Confluent documentation: https://github.com/confluentinc/confluent-kafka-javascript/blob/dev_early_access_development_branch/QUICKSTART.md
require("dotenv").config();

const kafka = new Kafka({
    kafkaJS: {
        brokers: [process.env.BOOTSTRAP_SERVERS],
        ssl: true,
        sasl: {
            mechanism: 'plain',
            username: process.env.USERNAME,
            password: process.env.PASSWORD,
        },
    }
  });
  
const consumer = kafka.consumer({'group.id': 'db_metadata', 'auto.offset.reset': 'earliest'});

async function consumerStart() {

  let stopped = false;
  const topic = "db_metadata"

  await consumer.connect();
  await consumer.subscribe({ topics: [topic] });

  consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      console.log({
        topic,
        partition,
        offset: message.offset,
        value: message.value.toString(),
      });
    }
  });

  // Update stopped whenever we're done consuming.
  // The update can be in another async function or scheduled with setTimeout etc.
  while(!stopped) {
    await new Promise(resolve => setTimeout(resolve, 1000));
  }

  await consumer.disconnect();
}

consumerStart();