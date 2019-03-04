const express = require("express");
const app = express();
const server = require("http").createServer(app);
const io = require("socket.io")(server);
const KafkaAvro = require("kafka-avro");
const Kafka = require("node-rdkafka");
const config = require("../../config.json");
const request = require("request");
const appUrl =
  "http://" + config.APPLICATION_HOSTNAME + ":" + config.APPLICATION_PORT;

app.get("/topics", function(req, res, next) {
  request({
    uri: config.REST_PROXY_URL + "/topics"
  }).pipe(res);
});

app.get("/subjects", function(req, res, next) {
  request({
    uri: config.SCHEMA_REGISTRY_URL + "/subjects"
  }).pipe(res);
});

app.use(express.static("dist"));

server.listen(config.APPLICATION_PORT, () => {
  console.log("");
  console.log("*".repeat(50));
  console.log("Server Listening on: " + appUrl);
  console.log("*".repeat(50));
});

let subscriptions = {};

io.sockets.on("connection", function(socket) {
  socket.on("topic", function(topic) {
    socket.join(topic);
  });

  socket.on("subscribe", function(topic) {
    if (subscriptions[topic] === undefined) {
      newConsumer(topic);
    }
    socket.join(topic);
  });

  socket.on("unsubscribe", function(topic) {
    socket.leave(topic);

    let clientsInRoom = [];
    io.in(topic).clients((err, clients) => {
      if (clients.length === 0) {
        try {
          //HERE
          subscriptions[topic].consumer.disconnect();
        } catch (e) {
          console.log(e);
        } finally {
          delete subscriptions[topic];
        }
      }
    });
  });
});

const newConsumer = topic => {
  let kafkaAvro = new KafkaAvro({
    kafkaBroker: config.KAFKA_BROKER,
    schemaRegistry: config.SCHEMA_REGISTRY_URL,
    fetchAllVersions: true,
    fetchRefreshRate: 20,
    topics: [topic]
  });

  kafkaAvro.init().then(function() {
    kafkaAvro
      .getConsumerStream(
        {
          "group.id": "TopicTailer-d" + topic,
          "socket.keepalive.enable": true,
          "enable.auto.commit": false,
          "session.timeout.ms": 10000
        },
        { "auto.offset.reset": "latest" },
        {
          topics: topic,
          waitInterval: 0
        }
      )
      .then(function(stream) {
        subscriptions[stream.topics[0]] = stream;

        stream.on("data", function(data) {
          let payload = {
            topic: data.topic,
            ts: data.timestamp,
            partition: data.partition,
            offset: data.offset,
            key: data.key ? data.key.toString() : ""
          };

          if (data.parsed) {
            Object.keys(data.parsed).forEach(p => {
              if (typeof data.parsed[p] == "object") {
                if (Array.isArray(data.parsed[p])) {
                  payload[p] = JSON.stringify(data.parsed[p], null, 2);
                } else if (data.parsed[p] === null) {
                  payload[p] = "null";
                } else {
                  Object.keys(data.parsed[p]).forEach(np => {
                    if (typeof data.parsed[p][np] == "object") {
                      payload[p] = JSON.stringify(data.parsed[p][np], null, 2);
                    } else {
                      payload[p] = data.parsed[p][np];
                    }
                  });
                }
              } else {
                payload[p] = data.parsed[p];
              }
            });

            io.sockets.in(topic).emit("message", payload);
          }
        });
      });
  });
};
