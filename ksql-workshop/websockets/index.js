// initialize kafka avro client
var KafkaAvro = require('kafka-avro');
var kafkaAvro = new KafkaAvro({
    kafkaBroker: 'kafka:29092',
    schemaRegistry: 'http://schema-registry:8081',
});
kafkaAvro.init()
    .then(function() {
        console.log('Ready to use');
    });

// Setup basic express server
var express = require('express');
var app = express();
var path = require('path');
var server = require('http').createServer(app);
var io = require ('socket.io') (server);
var port = process.env.PORT || 3000;

server.listen(port, () => {
  console.log('Server listening at port %d', port);
});

// Routing
app.use(express.static(path.join(__dirname, 'public')));

// log when we get a websocket connection
io.on('connection', (socket) => {
  console.log('new connection, socket.id: ' + socket.id);
});

// single avro consumer
kafkaAvro.getConsumer({
  'group.id': "server1",
  'socket.keepalive.enable': true,
  'enable.auto.commit': false,
})
  .then(function(consumer) {
    return new Promise(function (resolve, reject) {
      consumer.on('ready', function() {
        resolve(consumer);
      });
      consumer.connect({}, function(err) {
        if (err) {
          reject(err);
          return;
        }
        resolve(consumer); 
      });
    });
  })
  .then(function(consumer) {
    // Subscribe to POOR_RATINGS
    var topicName = 'POOR_RATINGS';
    consumer.subscribe([topicName]);
    consumer.consume();
    consumer.on('data', function(rawData) {
      // send the message to all the listeners
      console.log(".");
      io.sockets.emit('new message', rawData.parsed);
    });
  });