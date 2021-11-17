# KPI Dashboard

A fast, simple, streaming dashboard for KPIs.

For this codebase we imagine we have a business running, selling
things, and recording those sales in a `purchases` topic. A dummy process
runs in the background simulating those sales.

From there, we build out a ksqlDB summary table, a websocket server,
and a React frontend. As new purchases come in, ksqlDB updates the
summary table, which is streamed to the webserver, which sends the new
record out to all connected clients for display.

## Presentation

You can watch a complete walkthrough of this code being built up from scratch here:

<a href="http://www.youtube.com/watch?feature=player_embedded&v=wirjC_Zp2ZY" target="_blank">
 <img src="http://img.youtube.com/vi/wirjC_Zp2ZY/mqdefault.jpg" alt="Watch the video"  border="10" />
</a>

## Code Layout

(This code assumes you are running Kafka & ksqlDB locally. You will
need to modify configuration files if that's not the case.)

### In brief

The most interesting files in here are:

* The table: [dashboard_table.sql](dashboard_table.sql)
* The webserver: [pythonserver/server.py](pythonserver/server.py)
* The frontend: [client/src/App.js](client/src/App.js)

### The "Business" Simulator

`setup.sql` creates the purchases table. Run it with `ksql -f setup.sql`.

Then run the process that generates dummy purchases:

```sh
cd javaserver
gradle runGenerator
```

### ksqlDB

`dashboard_table.sql` is the code to create the summary table. Run it
with `ksql -f dashboard_table.sql`, or type it by hand - it's short
and informative.

### Websocket Server

The server is written in Python. Run it with:

``` sh
cd pythonserver
virtualenv env
source env/bin/activate

pip install confluent_kafka websockets simplejson
./server.py
```

If you have `ws` installed you can easily test the server with `ws ws://localhost:8080`.

### React frontend

The frontend is a simple React app. Run it with:


```sh
cd client
yarn start
```
