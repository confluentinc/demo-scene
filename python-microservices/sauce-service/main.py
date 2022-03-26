from configparser import ConfigParser
from confluent_kafka import Producer, Consumer
import json
import random

config_parser = ConfigParser(interpolation=None)
config_file = open('config.properties', 'r')
config_parser.read_file(config_file)
client_config = dict(config_parser['kafka_client'])

sauce_producer = Producer(client_config)

pizza_consumer = Consumer(client_config)
pizza_consumer.subscribe(['pizza'])


def start_service():
    while True:
        msg = pizza_consumer.poll(0.1)
        if msg is None:
            pass
        elif msg.error():
            pass
        else:
            pizza = json.loads(msg.value())
            add_sauce(msg.key(), pizza)


def add_sauce(order_id, pizza):
    pizza['sauce'] = calc_sauce()
    sauce_producer.produce('pizza-with-sauce', key=order_id, value=json.dumps(pizza))


def calc_sauce():
    i = random.randint(0, 8)
    sauces = ['regular', 'light', 'extra', 'none', 'alfredo', 'regular', 'light', 'extra', 'alfredo']
    return sauces[i]


if __name__ == '__main__':
    start_service()
