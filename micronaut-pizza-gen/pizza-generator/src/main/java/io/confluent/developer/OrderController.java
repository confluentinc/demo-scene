package io.confluent.developer;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import io.confluent.developer.avro.Pizza;
import java.util.ArrayList;

@Controller("/order")
public class OrderController {
    @Inject
    private RandomPizzaProducer pizzaProducer;

    @Inject
    private PizzaService pizzaService;

    @Post
    public HttpResponse<String> orderPizzas(@Body PizzaOrder order) {
        Integer count = order.getCount();
        for (Integer i = 0; i < count; i++){
            Pizza newPizza = new Pizza();
            newPizza.setOrderId(order.getOrderId());
            newPizza.setMeats(new ArrayList<String>());
            newPizza.setVeggies(new ArrayList<String>());
            pizzaProducer.buildPizza(order.getOrderId(), newPizza);
        }
        pizzaService.addNewOrder(order);
        return HttpResponse.ok(order.getOrderId());
    }

    @Get("/{id}")
    public HttpResponse<PizzaOrder> getPizzaOrder(String id){
        PizzaOrder po = pizzaService.fetchOrder(id);
        System.out.println("PizzaOrder is " + po.toString());
        return HttpResponse.ok().body(po);
    }

}
