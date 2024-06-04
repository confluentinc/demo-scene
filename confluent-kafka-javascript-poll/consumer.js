const { Kafka } = require("@confluentinc/kafka-javascript").KafkaJS;

require("dotenv").config();
var express = require("express");
var app = express();
var http = require("http");
socketIO = require("socket.io");

let server = http.Server(app);
server.listen(5000);
io = socketIO(
  server,
  {
    cors: {
      origin: "*",
    },
  },
  { transports: ["websocket"] },
);

const kafka = new Kafka({
  kafkaJS: {
    brokers: ["pkc-921jm.us-east-2.aws.confluent.cloud:9092"],
    ssl: true,
    sasl: {
      mechanism: "plain",
      username: process.env.CONFLUENT_API_KEY,
      password: process.env.CONFLUENT_API_SECRET,
    },
    groupId: "firstgroup2",
    fromBeginning: false,
  },
});
const consumer = kafka.consumer();

async function consumerStart() {
  console.log("GO");

  await consumer.connect();
  await consumer.subscribe({ topic: "total_count" });

  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      total_count = JSON.parse(message.value.toString());

      const messageFiltered = Object.entries(total_count).filter(
        (vote) => vote[1].lastClicked === true,
      );

      console.log({
        message: {
          question_id: messageFiltered[0][0],
          vote: messageFiltered[0][1],
        },
      });
      // consumer.commitOffsets()
      question_id = messageFiltered[0][0];
      count = messageFiltered[0][1];
      io.sockets.emit("event", {
        message: { question_id, count },
      });
    },
  });
}

// Set up signals for a graceful shutdown.
const disconnect = () => {
  process.off("SIGINT", disconnect);
  process.off("SIGTERM", disconnect);
  stopped = true;
  consumer
    .commitOffsets()
    .finally(() => consumer.disconnect())
    .finally(() => console.log("Disconnected successfully"));
};
process.on("SIGINT", disconnect);
process.on("SIGTERM", disconnect);

(async () => {
  await consumerStart().catch((e) =>
    console.error(`[example/consumer] ${e.message}`, e),
  );
})();