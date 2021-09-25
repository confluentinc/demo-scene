package io.confluent.developer;

import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Random;

@Singleton
public class SauceService {

    private HashMap<Integer, String> sauces;
    private Random rand = new Random();

    public String chooseSauce(){
        int idx = rand.nextInt(6);
        return sauces.get(Integer.valueOf(idx));
    }

    public SauceService(){
        sauces = new HashMap<Integer, String>();
        sauces.put(0, "None");
        sauces.put(1, "Light");
        sauces.put(2, "Normal");
        sauces.put(3, "Extra");
        sauces.put(4, "Alfredo");
        sauces.put(5, "BBQ");
    }
}
