#!/usr/bin/env python

from asyncio.events import get_event_loop
from asyncio.futures import Future
from functools import partial
import asyncio
import simplejson
import threading
import websockets

from confluent_kafka import DeserializingConsumer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroDeserializer

def run_consumer(shutdown_flag, clients, lock):
    print("Starting Kafka Consumer.")
    schema_registry_client = SchemaRegistryClient({
        "url": "<Schema Registry -> API Endpoint>",
        "basic.auth.user.info": "<Schema Registry -> API Credentials Key>:<Schema Registry -> API Credentials Secret>"
    })
    deserializer = AvroDeserializer(schema_registry_client)
    config = {
        "bootstrap.servers": "<Cluster Settings -> Bootstrap server>",
        "security.protocol": "SASL_SSL",
        "sasl.mechanism": "PLAIN",
        "sasl.username": ""<Data Integration -> API Keys -> Key>",
        "sasl.password": "<Data Integration -> API Keys -> Secret>",
        "group.id": "dashboard-demo",
        "value.deserializer": deserializer
    }

    consumer = DeserializingConsumer(config)
    consumer.subscribe(["dashboard"])

    while not shutdown_flag.done():
        msg = consumer.poll(0.2)

        if msg is None:
            print("Waiting...")
        elif msg.error():
            print(f"ERROR: {msg.error()}")
        else:
            value = msg.value()
            formatted = simplejson.dumps(value)
            print(f"Sending {formatted} to {clients}")

            with lock:
                websockets.broadcast(clients, formatted)

    print("Closing Kafka Consumer")
    consumer.close()

async def handle_connection(clients, lock, connection, path):
    with lock:
        clients.add(connection)

    await connection.wait_closed()

    with lock:
        clients.remove(connection)


async def main():
    shutdown_flag = Future()
    clients = set()
    lock = threading.Lock()

    get_event_loop().run_in_executor(None, run_consumer, shutdown_flag,
                                     clients, lock)

    print("Starting WebSocket Server.")
    try:
        async with websockets.serve(partial(handle_connection, clients, lock),
                                    "localhost", 8080):
            await Future()
    finally:
        shutdown_flag.set_result(True)


asyncio.run(main())
