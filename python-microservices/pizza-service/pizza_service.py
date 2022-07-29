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

def order_pizzas(count):
    order = PizzaOrder(count)
    pizza_warmer[order.id] = order
    for i in range(count):
        new_pizza = Pizza()
        new_pizza.order_id = order.id
        pizza_producer.produce('pizza', key=order.id, value=new_pizza.toJSON())
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
    pizza_consumer.subscribe(['pizza-with-veggies'])
    while True:
        event = pizza_consumer.poll(1.0)
        if event is None:
            pass
        elif event.error():
            print(f'Bummer - {event.error()}')
        else:
            pizza = json.loads(event.value())
            add_pizza(pizza['order_id'], pizza)

def add_pizza(order_id, pizza):
    if order_id in pizza_warmer.keys():
        order = pizza_warmer[order_id]
        order.add_pizza(pizza)
