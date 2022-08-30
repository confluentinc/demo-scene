from flask import Flask
from threading import Thread
import json
import pizza_service

app = Flask(__name__)


@app.route('/order/<count>', methods=['POST'])
def order_pizzas(count):
    order_id = pizza_service.order_pizzas(int(count))
    return json.dumps({"order_id": order_id})


@app.route('/order/<order_id>', methods=['GET'])
def get_order(order_id):
    return pizza_service.get_order(order_id)


if __name__ == '__main__':
    app.run()

@app.before_first_request
def launch_consumer():
    t = Thread(target=pizza_service.load_orders)
    t.start()