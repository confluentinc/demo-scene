package io.confluent.developer;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import io.confluent.developer.avro.Pizza;

@Singleton
public class PizzaService {
    HashMap<String, PizzaOrder> pizzaWarmer = new HashMap<String, PizzaOrder>();
    @Inject
    private PizzaMapper pizzaMapper;

    public void addToOrder(String orderId, Pizza completedPizza){
        PizzaOrder po = pizzaWarmer.get(orderId);
        if (po != null) {
            po.addPizza(pizzaMapper.toDto(completedPizza));
        } else {
            System.out.println("Bummer! They lost my pizza order!");
        }
    }
    public void addNewOrder(PizzaOrder newOrder){
        PizzaOrder po = pizzaWarmer.get(newOrder.getOrderId());
        if (po == null){
            pizzaWarmer.put(newOrder.getOrderId(), newOrder);
        }
    }
    public PizzaOrder fetchOrder(String orderId){
        return pizzaWarmer.get(orderId);
    }
}
