package com.bank;

public class DepositRequest {
    double amount;

    //Blank constructor is required by spring  to build the object
    public DepositRequest() {}

    //A getter is required by Spring to read the data\
    public double getAmount() {
        return amount;
    }

    //A setter is required by Spring to inject the 150.50
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
