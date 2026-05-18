package com.bank;

public class TransactionResponse {
    private String timestamp;
    private String type;
    private double amount;

    public TransactionResponse(String timestamp, String type, double amount) {
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
    }
    public String getTimestamp() {return timestamp;}
    public String getType() {return type;}
    public Double getAmount() {return amount;}


}
