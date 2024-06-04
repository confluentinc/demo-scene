var express = require("express");
var bodyParser = require("body-parser");
var app = express();
app.use(express.static(__dirname + "/public"));
const Kafka = require("@confluentinc/kafka-javascript");
require("dotenv").config();

const producer = new Kafka.Producer({
  "bootstrap.servers": ["pkc-921jm.us-east-2.aws.confluent.cloud:9092"],
  "security.protocol": "SASL_SSL",
  "client.id": "kafka_votes",
  "sasl.mechanisms": "PLAIN",
  "sasl.username": process.env.CONFLUENT_API_KEY,
  "sasl.password": process.env.CONFLUENT_API_SECRET,
  "broker.version.fallback": "0.10.0", // still needed with librdkafka 0.11.6 to avoid fallback to 0.9.0
  "log.connection.close": false,
  dr_cb: true,
});

const consumer = new Kafka.KafkaConsumer(
  {
    "bootstrap.servers": ["pkc-921jm.us-east-2.aws.confluent.cloud:9092"],
    "security.protocol": "SASL_SSL",
    "group.id": "kafka1",
    "sasl.mechanisms": "PLAIN",
    "sasl.username": process.env.CONFLUENT_API_KEY,
    "sasl.password": process.env.CONFLUENT_API_SECRET,
    "enable.auto.commit": false,
  },
  {},
);

producer.connect();

const path = require("path");
const port = 3000;

app.get("/", async (req, res) => {});

app.listen(port, async () => {
  console.log(`app listening on port ${port}`);
});

app.use(bodyParser.urlencoded({ extended: true }));

app.post("/send-to-kafka-topic", async function (req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
  res.header("Access-Control-Allow-Headers", "Content-Type");

  var data = await req.body;

  res.json(data);

  producer.poll(100);
  sendMessage(data).catch(console.error);
  next;
});

producer.on("delivery-report", function (err, report) {
  console.log("delivery-report: " + JSON.stringify(report));
});

//logging all errors
consumer.on("event.error", function (err) {
  console.error("Error from consumer");
  console.error(err);
});

consumer.connect();

consumer.on("ready", function (arg) {
  console.log("Consumer ready. Fetching committed offsets.");

  // Assuming partition 0, adjust as necessary
  const topicPartitions = [{ topic: "total_count", partition: 0 }];

  consumer.committed(topicPartitions, 5000, (err, topicPartitions) => {
    if (err) {
      console.error("Error fetching committed offsets:", err);
      return;
    }

    const { offset } = topicPartitions[0];

    if (offset === Kafka.CODES.RD_KAFKA_OFFSET_INVALID) {
      consumer.assign([{ topic: "total_count", partition: 0, offset: 1 }]);
      consumer.consume();
    } else {
      // Adjust the offset if needed. Here we start from the committed offset directly
      console.log(`Starting from committed offset: ${offset}`);
      consumer.assign([{ topic: "total_count", partition: 0, offset }]);
      consumer.consume();
    }
  });
});

consumer.on("data", function (message) {
  // Output the actual message contents
  decoded = JSON.parse(message.value.toString());
  console.log("FROM CONSUMER", decoded);
  // Manually commit offset after processing
  // Here, you might adjust to commit the current message's offset - 1 if you want to re-consume the last message upon restart
  const commitOffset = message.offset;
  consumer.commit({
    topic: message.topic,
    partition: message.partition,
    offset: commitOffset,
  });
});
consumer.on("offset.commit", function () {
  console.log("commited");
});

async function sendMessage(data) {
  let question_id_string = `${data.data.question_id}`;
  let vote = data.data.vote;

  decoded[question_id_string][vote] = decoded[question_id_string][vote] + 1;
  Object.entries(decoded).forEach(
    (voteObj) => (voteObj[1].lastClicked = false),
  );

  decoded[question_id_string]["lastClicked"] = true;

  console.log("decoded from producer", decoded);
  producer.produce("total_count", 0, Buffer.from(JSON.stringify(decoded)));

  producer.on("event.error", (err) => {
    console.error("Error from producer");
    console.error(err);
  });

  producer.on("disconnected", function (arg) {
    console.log("producer disconnected. " + JSON.stringify(arg));
  });
}