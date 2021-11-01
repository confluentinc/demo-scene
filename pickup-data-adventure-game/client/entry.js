"use strict";
/*global WebSocket*/

import "uikit/dist/css/uikit.min.css";
import "uikit/dist/js/uikit.min.js";
import "uikit/dist/js/uikit-icons.js";
import './static/main.less';

import { Elm } from './src/Main.elm';
import { v4 as uuidv4 } from 'uuid';
import * as WebsocketSupport from './src/WebsocketSupport';

const userId = uuidv4();

var app = Elm.Main.init({
  node: document.getElementById("app"),
  flags: {
    userId
  }
});

const protocol = window.location.protocol == "https:" ? "wss:" : "ws:";
const socket = new WebSocket(protocol + "//" + window.location.host + "/socket/" + userId, []);
WebsocketSupport.registerPorts(app, socket);
