// require('kafkajs') is replaced with require('@confluentinc/kafka-javascript').KafkaJS.
const { Kafka } = require("@confluentinc/kafka-javascript").KafkaJS;
const { faker } = require('@faker-js/faker');
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

const producer = kafka.producer();

// producer should always be connected at app initialization, separately from producing message 
const connectProducer = async () => {
  await producer.connect();
  console.log("Connected successfully");
}

const run = async () => {

  let messageValue = {
    collation: faker.database.collation(),
    column: faker.database.column(),
    engine: faker.database.engine(), 
    mongodbObjectId: faker.database.engine(),
    type: faker.database.type()

  }
  console.log(messageValue)
   producer.send({
            topic: 'db_metadata',
            messages: [
                { value: JSON.stringify(messageValue) }
            ]
        });

    await producer.disconnect();

    console.log("Disconnected successfully");
}

(async() => {
  await connectProducer();
  await run();
})();