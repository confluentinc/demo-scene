
"use strict";
/*global exports*/

exports.registerPorts = function(app, socket) {
  console.log("PORTS", app.ports);
  if (app.ports.sendToServer) {
    app.ports.sendToServer.subscribe(function(data) {
      socket.send(data);
    });
  };

  if (app.ports.onMessage) {
    socket.onmessage = function(event) {
      console.log("Got message", event.data, typeof(event.data));
      app.ports.onMessage.send(event.data);
    };
  };

  socket.onclose = function(event) {
    if (app.ports.onClose) {
      console.log("Got close", event.data, typeof(event.data));
      app.ports.onClose.send(null);
    };
  };
};
