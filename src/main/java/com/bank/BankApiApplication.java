package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.sql.*;

@SpringBootApplication // tells springboot to configure a web server
public class BankApiApplication {
    public static void main(String[] args) {
        //this launches the built-in server engine (Tomcat)

        // TEMPORARY DATABASE INSPECTOR
        System.out.println("=== LOOKING UP CURRENT ACCOUNTS ===");
        try(Connection conn = DatabaseManager.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT account_number, balance, pin FROM accounts")) {
                while(rs.next()) {
                    System.out.println("Acc #: " + rs.getInt("account_number") + " | Balance: $" + rs.getDouble("balance") + " | PIN: " + rs.getString("pin"));
                }
        } catch(SQLException e) {
            System.out.println("Could not read database automatically: " + e.getMessage());
        }
        System.out.println("===================================\n");

        SpringApplication.run(BankApiApplication.class, args);
    }
}
