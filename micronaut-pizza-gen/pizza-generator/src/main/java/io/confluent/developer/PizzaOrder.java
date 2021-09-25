package io.confluent.developer;

import java.util.ArrayList;
import java.util.UUID;
import io.micronaut.core.annotation.Introspected;
import com.fasterxml.jackson.annotation.*;

@Introspected
public class PizzaOrder {
    private Integer count;
    private String orderId;
    private ArrayList<PizzaDto> pizzas;

    public Integer getCount(){
        return this.count;
    }
    public void setCount(Integer val){
        this.count = val;
    }
    public String getOrderId(){
        return this.orderId;
    }

    public void addPizza(PizzaDto completedPizza){
        this.pizzas.add(completedPizza);
    }
    public ArrayList<PizzaDto> getPizzas(){
        return this.pizzas;
    }
    public PizzaOrder(){
        this.orderId = UUID.randomUUID().toString();
        this.pizzas = new ArrayList<PizzaDto>();
    }
    public PizzaOrder(Integer numPizzas){
        this.count = numPizzas;
        this.orderId = UUID.randomUUID().toString();
        this.pizzas = new ArrayList<PizzaDto>();
    }

    @Override
    public String toString() {
        return "PizzaOrder{" +
                "count=" + count +
                ", orderId='" + orderId + '\'' +
                ", pizzas=" + pizzas +
                '}';
    }
}
