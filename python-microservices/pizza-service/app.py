from flask import Flask
import pizza_service

app = Flask(__name__)


@app.route('/order/<count>', methods=['POST'])
def order_pizzas(count):
    order_num = pizza_service.order_pizzas(int(count))
    return '{"order_id":"' + order_num + '"}'


@app.route('/order/<order_id>', methods=['GET'])
def get_order(order_id):
    return pizza_service.get_order(order_id)


if __name__ == '__main__':
    app.run()
