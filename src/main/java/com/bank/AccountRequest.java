package com.bank;

public class AccountRequest {
    public String holderName;
    public String pin;
    public double initialDeposit;

    public AccountRequest() {}

    public String getHolderName() {return holderName;}
    public void setHolderName(String holderName) {this.holderName = holderName;}

    public String getPin() { return pin;}
    public void setPin(String pin) { this.pin = pin;}

    public double getInitialDeposit() { return initialDeposit;}
    public void setInitialDeposit(double initialDeposit) {this.initialDeposit = initialDeposit;}
}
