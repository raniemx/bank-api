package com.bank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
public class BankController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Welcome to the Bank API! The server is up and running successfully.";
    }

    @GetMapping("/accounts/{accountNumber}/balance")
    public String getBalance(@PathVariable int accountNumber,
                             @RequestParam String pin) {
        try{
            Double balance = AccountService.getBalanceIfValid(accountNumber, pin);

            if(balance == null) {
                return "Error: Invalid account number or incorrect PIN.";
            }

            return "Account " + accountNumber + " Balance: $" + String.format("%.2f", balance);
        } catch (SQLException e) {
            return "Server Error: Unable to communicate with the database.";
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/accounts")
    public String createAccount(@org.springframework.web.bind.annotation.RequestBody AccountRequest request) {
        try {
            //We call your existing database logic to create and account!
            int generatedAccountNumber = (int) (Math.random() * 9000) + 1000;

            boolean isSaved = AccountService.saveAccount(generatedAccountNumber, request.holderName, request.initialDeposit, request.pin);
            if (isSaved) {
                return "Success! Account created. Your assigned Account Number is: " + generatedAccountNumber;
            } else {
                return "Error: Could not save the account details.";
            }
        } catch(SQLException e) {
            return "Server Error: Code crashed while inserting into the database.";
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/accounts/{accountNumber}/deposit")
    public String deposit(@PathVariable int accountNumber,
                          @RequestParam String pin,
                          @org.springframework.web.bind.annotation.RequestBody DepositRequest request) {
        try {
            //1. Check if the account exists and the PIN is valid first
            Double currentBalance = AccountService.getBalanceIfValid(accountNumber, pin);

            if(currentBalance == null) {
                return "ErrorL Invalid account number or incorrect PIN.";
            }

            Double newBalance = AccountService.depositAmount(accountNumber, currentBalance, request.getAmount());

            if(newBalance == null) {
                return "Error: Deposit amount must be greater than $0.00";
            }

            return "Deposit successful! New Balance for Account " + accountNumber + "is : $" + String.format("%.2f", newBalance);
        } catch(SQLException e) {
            return "Server Error: Transaction failed during database processing.";
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/accounts/{accountNumber}/withdraw")
    public String withdraw(@PathVariable int accountNumber,
                           @RequestParam String pin,
                           @org.springframework.web.bind.annotation.RequestBody WithdrawalRequest request) {
        try {
            Double currentBalance = AccountService.getBalanceIfValid(accountNumber, pin);

            if(currentBalance == null) {
                return "Incorrect Account Number or PIN.";
            }

            Double newBalance = AccountService.withdrawAmount(accountNumber, currentBalance, request.getAmount());

            if(newBalance == null) {
                return "Error: Insufficient funds or invalid withdrawal amount";
            }

            return "Withdrawal successful! New Balance for Account " + accountNumber + " is: $" + String.format("%.2f", newBalance);
        } catch (Exception e) {
            return "Server Error: Transaction failed during database processing.";
        }
    }

    @GetMapping("/accounts/{accountNumber}/statement")
    public Object getStatement(@PathVariable int accountNumber, @RequestParam String pin) {
        try{
            java.util.List<TransactionResponse> history = AccountService.getTransactionsForWeb(accountNumber, pin);

            if(history == null) {
                return "Error: Invalid account number or incorrect PIN.";
            }

            if(history.isEmpty()) {
                return "No transaction records found on this account.";
            }

            return history;
        } catch(SQLException e) {
            return "Server Error: Unable to fetch transaction history.";
        }
    }

    @DeleteMapping("/accounts/close")
    public ResponseEntity<String> closeAccount(@RequestBody CloseAccountRequest request) {
        try {
            boolean success = AccountService.closeAccount(request.getAccountNumber(), request.getPin());

            if(success) {
                return ResponseEntity.ok("Account successfully closed. Thank you for banking with us.");
            } else {
                return ResponseEntity.badRequest().body("Could not close account due to a database issue");
            }
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch(Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occured: " + e.getMessage());
        }
    }
}

class CloseAccountRequest {
    private int accountNumber;
    private String pin;

    //getters and setters
    public int getAccountNumber() { return accountNumber;}
    public void setAccountNumber(int accountNumber) {this.accountNumber = accountNumber;}
    public String getPin() {return pin;}
    public void setPin(String pin) {this.pin = pin;}
}
