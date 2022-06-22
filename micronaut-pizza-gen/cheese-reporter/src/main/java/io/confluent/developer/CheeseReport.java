package io.confluent.developer;

import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class CheeseReport {
    Map<String, Integer> cheeses = new HashMap<String, Integer>();

    public void addCheeseCount(String cheese){
        if(cheeses.containsKey(cheese)){
            cheeses.put(cheese, cheeses.get(cheese) + 1);
        } else {
            cheeses.put(cheese, 1);
        }
    }

    public Map getReport(){
        return cheeses;
    }
}
