package io.confluent;

public class NewOrder {
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
    public NewOrder(){

    }

    public String toString(){
        String result = String.format("Customer: %s, Item: %s, Amount: %.2f", customerNumber, item, amount);
        return result;
    }
}
