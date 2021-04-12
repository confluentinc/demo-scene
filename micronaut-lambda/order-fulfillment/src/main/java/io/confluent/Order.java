package io.confluent;

public class Order {
    private String orderId;
    private String customerNumber;
    private String item;
    private Double amount;

    public String getCustomerNumber(){
        return customerNumber;
    }
    public void setCustomerNumber(String customerNumber){
        this.customerNumber = customerNumber;
    }
    public String getItem(){
        return item;
    }
    public void setItem(String item){
        this.item = item;
    }
    public Double getAmount(){
        return amount;
    }
    public void setAmount(Double amount){
        this.amount = amount;
    }
    public String getOrderId(){
        return orderId;
    }
    public void setOrderId(String orderId){
        this.orderId = orderId;
    }

    public Order(){

    }

    public String toString(){
        return String.format("OrderId: %s, Customer: %s, Item: %s, Amount: %.2f",
                orderId, customerNumber, item, amount);
    }
}
