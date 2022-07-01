import json

from pizza import Pizza, PizzaOrder
from configparser import ConfigParser
from confluent_kafka import Producer, Consumer

config_parser = ConfigParser(interpolation=None)
config_file = open('config.properties', 'r')
config_parser.read_file(config_file)
producer_config = dict(config_parser['kafka_client'])
consumer_config = dict(config_parser['kafka_client'])
consumer_config.update(config_parser['consumer'])
pizza_producer = Producer(producer_config)

pizza_warmer = {}
pizza_topic = 'pizza'
completed_pizza_topic = 'pizza-with-veggies'


def order_pizzas(count):
    order = PizzaOrder(count)
    pizza_warmer[order.id] = order
    for i in range(count):
        new_pizza = Pizza()
        new_pizza.order_id = order.id
        pizza_producer.produce(pizza_topic, key=order.id, value=new_pizza.toJSON())
    pizza_producer.flush()
    return order.id

def get_order(order_id):
    order = pizza_warmer[order_id]
    if order == None:
        return "Order not found, perhaps it's not ready yet."
    else:
        return order.toJSON()


def load_orders():
    pizza_consumer = Consumer(consumer_config)
    pizza_consumer.subscribe([completed_pizza_topic])
    while True:
        evt = pizza_consumer.poll(1.0)
        if evt is None:
            pass
        elif evt.error():
            print(f'Bummer - {evt.error()}')
        else:
            pizza = json.loads(evt.value())
            add_pizza(pizza['order_id'], pizza)

def add_pizza(order_id, pizza):
    if order_id in pizza_warmer.keys():
        order = pizza_warmer[order_id]
        order.add_pizza(pizza)
