package io.confluent;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import java.util.*;


@Introspected
public class OrderRequestHandler extends MicronautRequestHandler<Object, Order> {

    @Override
    public Order execute(Object input) {
        Order order = new Order();
        order.setCustomerNumber(extractField("customerNumber", input));
        order.setItem(extractField("item", input));
        order.setAmount(Double.parseDouble(extractField("amount", input)));
        order.setOrderId(UUID.randomUUID().toString());
        return order;
    }

    
    private String extractField(String field, Object input){
        String inputString = input.toString();
        String tmp;
        int idx = inputString.indexOf(field);
        int len = field.length() + 1;
        int end;
        if (idx > 0){
            tmp = inputString.substring(idx + len, inputString.length());
            end = calcEnd(tmp);
            return tmp.substring(0, end);
        } else {
            return "Name not found";
        }
    }
    private int calcEnd(String tmp){
        int a = tmp.indexOf("}");
        int b = tmp.indexOf(",");
        if (a< 0 || b < 0){
            return Math.max(a, b);
        } else {
            return Math.min(a, b);
        }
    }

}
