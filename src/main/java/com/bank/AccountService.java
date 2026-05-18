package com.bank;

import org.springframework.web.bind.annotation.GetMapping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountService {

    public static boolean transactionRecords(int accountNumber, String inputPin) throws SQLException {
        String accountSql = "SELECT pin FROM accounts WHERE account_number = ?";
        String transactionSql = "SELECT type, amount, timestamp FROM transactions WHERE account_number = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement accStmt = conn.prepareStatement(accountSql)) {
            accStmt.setInt(1, accountNumber);

            try (ResultSet rs = accStmt.executeQuery()) {
                if (rs.next()) {
                    String dbPin = rs.getString("pin");
                    if (dbPin.equalsIgnoreCase(inputPin)) {
                        try (PreparedStatement transactionStmt = conn.prepareStatement(transactionSql)) {
                            transactionStmt.setInt(1, accountNumber);
                            try (ResultSet transRs = transactionStmt.executeQuery()) {
                                System.out.println("\n=============== MINI STATEMENT ===============");
                                System.out.printf("%-20s | %-10s | $%-10s\n", "TIMESTAMP", "TYPE", "AMOUNT");

                                boolean hasTransactions = false;

                                while (transRs.next()) {
                                    hasTransactions = true;
                                    String type = transRs.getString("type");
                                    Double amount = transRs.getDouble("amount");
                                    String timestamp = transRs.getString("timestamp");

                                    System.out.printf("%-20s | %-10s | $%-10.2f\n", timestamp, type, amount);
                                }

                                if (!hasTransactions) {
                                    System.out.println("No transaction records for this account.");
                                }
                                System.out.println("==============================================");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static java.util.List<TransactionResponse> getTransactionsForWeb(int accountNumber, String inputPin) throws SQLException {
        //1. Create an empty List to hold our transaction objects.
        java.util.List<TransactionResponse> history = new java.util.ArrayList<>();

        String accountSql = "SELECT pin from accounts WHERE account_number = ?";
        String transactionSql = "SELECT type, amount, timestamp FROM transactions WHERE account_number = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement accStmt = conn.prepareStatement(accountSql)) {
            accStmt.setInt(1, accountNumber);

            try (ResultSet rs = accStmt.executeQuery()) {
                if (rs.next()) {
                    String dbPin = rs.getString("pin");
                    if (dbPin.equalsIgnoreCase(inputPin)) {
                        try (PreparedStatement transStmt = conn.prepareStatement(transactionSql)) {
                            transStmt.setInt(1, accountNumber);
                            try (ResultSet transRs = transStmt.executeQuery()) {
                                while (transRs.next()) {
                                    String type = transRs.getString("type");
                                    double amount = transRs.getDouble("amount");
                                    String timestamp = transRs.getString("timestamp");

                                    TransactionResponse record = new TransactionResponse(timestamp, type, amount);
                                    history.add(record);
                                }
                            }
                        }
                        return history;
                    }
                }
            }
        }
        return null;
    }

    public static Double getBalanceIfValid(int accountNumber, String inputPin) throws SQLException {
        String sql = "SELECT pin, balance FROM accounts WHERE account_number = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbPin = rs.getString("pin");
                    if (dbPin.equalsIgnoreCase(inputPin)) {
                        return rs.getDouble("balance");
                    }
                }
            }
        }
        return null;
    }

    public static boolean saveAccount(int accountNumber, String name, double initialBalance, String pin) throws SQLException {
        String sql = "INSERT INTO accounts(account_number, holder_name, balance, pin) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNumber);
            pstmt.setString(2, name);
            pstmt.setDouble(3, initialBalance);
            pstmt.setString(4, pin);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public static Double depositAmount(int accountNumber, double currentBalance, double amount) throws SQLException {
        if (amount <= 0) return null;

        double newBalance = currentBalance + amount;
        String updateSQL = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        String logSQL = "INSERT INTO transactions (account_number, type, amount) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();

            conn.setAutoCommit(false);

            try (PreparedStatement updatestmt = conn.prepareStatement(updateSQL)) {
                updatestmt.setDouble(1, newBalance);
                updatestmt.setInt(2, accountNumber);
                updatestmt.executeUpdate();
            }

            try (PreparedStatement logstmt = conn.prepareStatement(logSQL)) {
                logstmt.setInt(1, accountNumber);
                logstmt.setString(2, "DEPOSIT");
                logstmt.setDouble(3, amount);
                logstmt.executeUpdate();
            }

            conn.commit();
            return newBalance;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.out.println("Transaction failed! Rolling back changes");
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public static Double withdrawAmount(int accountNumber, double currentBalance, double amount) throws SQLException {
        if (amount <= 0 || amount > currentBalance) return null;

        double newBalance = currentBalance - amount;
        String updateSQL = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        String logSQL = "INSERT INTO transactions (account_number, type, amount) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();

            conn.setAutoCommit(false);

            try (PreparedStatement updatestmt = conn.prepareStatement(updateSQL)) {
                updatestmt.setDouble(1, newBalance);
                updatestmt.setInt(2, accountNumber);
                updatestmt.executeUpdate();
            }

            try (PreparedStatement logstmt = conn.prepareStatement(logSQL)) {
                logstmt.setInt(1, accountNumber);
                logstmt.setString(2, "WITHDRAWAL");
                logstmt.setDouble(3, amount);
                logstmt.executeUpdate();
            }

            conn.commit();
            return newBalance;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.out.println("Transaction failed! Rolling back changes...");
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public static boolean closeAccount(int accountNumber, String pin) {
        //1. fetch the acount to see if it exists.
        String checkSQL = "SELECT pin, balance FROM accounts WHERE account_number = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, accountNumber);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Account not found.");
                }

                String dbPin = rs.getString("pin");
                double dbBalance = rs.getDouble("balance");

                //2. Security Check: Verify PIN
                if (!dbPin.equals(pin)) {
                    throw new IllegalArgumentException("Invalid PIN.");
                }

                if (dbBalance != 0.0) {
                    throw new IllegalArgumentException("Cannot close account. Balance must be $0.00. Please withdraw remaining funds first.");
                }
            }

            String deleteSql = "DELETE FROM accounts WHERE account_number = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, accountNumber);
                int rowsAffected = deleteStmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.out.println("Database Error: Cannot check for accounts.");
            return false;
        }
    }
}
