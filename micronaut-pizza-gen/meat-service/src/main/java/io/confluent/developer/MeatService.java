package io.confluent.developer;

import jakarta.inject.Singleton;

import java.util.*;

@Singleton
public class MeatService {

    private HashMap<Integer, String> meats;
    private Random rand = new Random();

    public ArrayList<String> chooseMeats(){
        ArrayList<String> result = new ArrayList<String>();
        int itemCount = rand.nextInt(4);
        if (itemCount == 0){
            result.add("None");
        } else {
            Set<Integer> uniques = new HashSet<Integer>();
            while (uniques.size() < itemCount +1){
                uniques.add(Integer.valueOf(rand.nextInt(7)));
            }
            for (Integer idx : uniques) {
                result.add(meats.get(idx));
            }
        }
        return result;
    }

    public MeatService(){
        meats = new HashMap<Integer, String>();
        meats.put(0, "Pepperoni");
        meats.put(1, "Sausage");
        meats.put(2, "Ham");
        meats.put(3, "Bacon");
        meats.put(4, "Beef");
        meats.put(5, "Salami");
        meats.put(6, "Anchovies");
    }

}
