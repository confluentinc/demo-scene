#!/usr/bin/env python

from asyncio import get_event_loop, Future
import asyncio
from functools import partial
import signal
from uuid import uuid4
import threading

import simplejson
import websockets

from confluent_kafka import DeserializingConsumer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroDeserializer

def run_consumer(shutdown_flag, clients, lock):
    print("Start consumer")
    schema_registry = SchemaRegistryClient({'url': 'http://localhost:8081'})

    deserializer = AvroDeserializer(schema_registry)

    config = {
        'bootstrap.servers': 'localhost:9092',
        'group.id': 'dashboard-demo',
        'value.deserializer': deserializer
    }

    consumer = DeserializingConsumer(config)
    consumer.subscribe(['DASHBOARD'])

    while True:
        msg = consumer.poll(0.5)

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

    print("Stop consumer")
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

    asyncio.get_event_loop().run_in_executor(None, run_consumer, shutdown_flag,
                                             clients, lock)

    try:
        print("Starting webserver")
        async with websockets.serve(partial(handle_connection, clients, lock),
                                    'localhost', 8080):
            await Future()
    finally:
        shutdown_flag.set_result(True)


asyncio.run(main())
