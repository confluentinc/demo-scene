from configparser import ConfigParser
from confluent_kafka import Producer, Consumer
import json
import random

config_parser = ConfigParser(interpolation=None)
config_file = open('config.properties', 'r')
config_parser.read_file(config_file)
client_config = dict(config_parser['kafka_client'])

veggies_producer = Producer(client_config)

meats_consumer = Consumer(client_config)
meats_consumer.subscribe(['pizza-with-meats'])


def start_service():
    while True:
        msg = meats_consumer.poll(0.1)
        if msg is None:
            pass
        elif msg.error():
            pass
        else:
            pizza = json.loads(msg.value())
            add_veggies(msg.key(), pizza)


def add_veggies(order_id, pizza):
    pizza['veggies'] = calc_veggies()
    veggies_producer.produce('pizza-with-veggies', key=order_id, value=json.dumps(pizza))


def calc_veggies():
    i = random.randint(0,4)
    veggies = ['tomato', 'olives', 'onions', 'peppers', 'pineapple', 'mushrooms', 'tomato', 'olives', 'onions', 'peppers', 'pineapple', 'mushrooms']
    selection = []
    if i == 0:
        return 'none'
    else:
        for n in range(i):
            selection.append(veggies[random.randint(0, 11)])
    return ' & '.join(set(selection))


if __name__ == '__main__':
    start_service()
