import json

from pizza import Pizza, PizzaOrder
from configparser import ConfigParser
from confluent_kafka import Consumer

config_parser = ConfigParser(interpolation=None)
config_file = open('config.properties', 'r')
config_parser.read_file(config_file)
producer_config = dict(config_parser['kafka_client'])
consumer_config = dict(config_parser['kafka_client'])
consumer_config.update(config_parser['consumer'])

cheeses = {}
cheese_topic = 'pizza-with-cheese'

def start_consumer():
    cheese_consumer = Consumer(consumer_config)
    cheese_consumer.subscribe([cheese_topic])
    while True:
        event = cheese_consumer.poll(1.0)
        if event is None:
            pass
        elif event.error():
            print(f'Bummer - {event.error()}')
        else:
            pizza = json.loads(event.value())
            add_cheese_count(pizza['cheese'])
            
def add_cheese_count(cheese):
    if cheese in cheeses:
        cheeses[cheese] = cheeses[cheese] + 1
    else:
        cheeses[cheese] = 1

def generate_report():
    return json.dumps(cheeses, indent = 4)     

