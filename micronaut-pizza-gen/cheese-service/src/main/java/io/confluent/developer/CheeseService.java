package io.confluent.developer;

import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Random;

@Singleton
public class CheeseService {

    private HashMap<Integer, String> cheeses;
    private Random rand = new Random();

    public String chooseCheese(){
        int idx = rand.nextInt(5);
        return cheeses.get(Integer.valueOf(idx));
    }

    public CheeseService(){
        cheeses = new HashMap<Integer, String>();
        cheeses.put(0, "None");
        cheeses.put(1, "Normal");
        cheeses.put(2, "Extra");
        cheeses.put(3, "Three-Cheese");
        cheeses.put(4, "Goat");
    }

}
