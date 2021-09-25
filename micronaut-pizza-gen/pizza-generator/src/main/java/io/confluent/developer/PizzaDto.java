package io.confluent.developer;

import io.micronaut.core.annotation.Introspected;

import java.util.ArrayList;

@Introspected
public class PizzaDto {
    String orderId;
    String sauce;
    String cheese;
    ArrayList<String> meats;
    ArrayList<String> veggies;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSauce() {
        return sauce;
    }

    public void setSauce(String sauce) {
        this.sauce = sauce;
    }

    public String getCheese() {
        return cheese;
    }

    public void setCheese(String cheese) {
        this.cheese = cheese;
    }

    public ArrayList<String> getMeats() {
        return meats;
    }

    public void setMeats(ArrayList<String> meats) {
        this.meats = meats;
    }

    public ArrayList<String> getVeggies() {
        return veggies;
    }

    public void setVeggies(ArrayList<String> veggies) {
        this.veggies = veggies;
    }

    @Override
    public String toString() {
        return "PizzaDto{" +
                "orderId='" + orderId + '\'' +
                ", sauce='" + sauce + '\'' +
                ", cheese='" + cheese + '\'' +
                ", meats=" + meats +
                ", veggies=" + veggies +
                '}';
    }
}
