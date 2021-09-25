package io.confluent.developer;

import jakarta.inject.Singleton;

import java.util.*;

@Singleton
public class VeggieService {
    
    private HashMap<Integer, String> veggies;
    private Random rand = new Random();

    public ArrayList<String> chooseVeggies(){
        ArrayList<String> result = new ArrayList<String>();
        int itemCount = rand.nextInt(5);
        if (itemCount == 0){
            result.add("None");
        } else {
            Set<Integer> uniques = new HashSet<Integer>();
            while (uniques.size() < itemCount +1){
                uniques.add(Integer.valueOf(rand.nextInt(7)));
            }
            for (Integer idx : uniques){
                result.add(veggies.get(idx));
            }
        }
        return result;
    }

    public VeggieService(){
        veggies = new HashMap<Integer, String>();
        veggies.put(0, "Bell Peppers");
        veggies.put(1, "Onions");
        veggies.put(2, "Black Olives");
        veggies.put(3, "Green Olives");
        veggies.put(4, "Tomatoes");
        veggies.put(5, "Mushrooms");
        veggies.put(6, "Pineapple");
    }
    
}
