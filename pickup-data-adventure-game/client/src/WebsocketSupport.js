
"use strict";
/*global exports*/

exports.registerPorts = function(app, socket) {
  console.log("PORTS", app.ports);
  if (app.ports.sendToServer) {
    app.ports.sendToServer.subscribe(function(data) {
      socket.send(data);
    });
  };

  socket.onopen = function(event) {
    if (app.ports.onOpen) {
      console.log("Got open", event.data, typeof(event.data));
      app.ports.onOpen.send(null);
    };
  };

  if (app.ports.onMessage) {
    socket.onmessage = function(event) {
      console.log("Got message", event.data, typeof(event.data));
      app.ports.onMessage.send(event.data);
    };
  };

  if (app.ports.onError) {
    socket.onerror = function(event) {
      console.log("Got error", event.data, typeof(event.data));
      app.ports.onError.send(event.data);
    };
  };

  socket.onclose = function(event) {
    if (app.ports.onClose) {
      console.log("Got close");
      app.ports.onClose.send(null);
    };
  };
};
