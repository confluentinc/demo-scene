import json
import uuid

class Pizza:
    def __init__(self):
        self.order_id = ''
        self.sauce = ''
        self.cheese = ''
        self.meats = ''
        self.veggies = ''

    def toJSON(self):
        return json.dumps(self, default=lambda o: o.__dict__,
                          sort_keys=False, indent=4)
    def __str__(self):
        return json.dumps(self.__dict__)



class PizzaOrder:
    def __init__(self, count):
        self.id = str(uuid.uuid4().int)
        self.count = count
        self.pizzas = []

    def add_pizza(self, pizza):
        self.pizzas.append(pizza)

    def get_pizzas(self):
        return self.pizzas

    def __str__(self):
        return json.dumps(self.__dict__)

    def toJSON(self):
        return json.dumps(self, default=lambda o: o.__dict__,
                          sort_keys=False, indent=4)
