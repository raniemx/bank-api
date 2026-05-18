# Secure Retail Banking API

A robust, production-ready RESTful Banking API built using **Java**, **Spring Boot**, and an embedded **SQLite** database. This project implements full CRUD functionality for managing bank accounts, handling secure financial transactions with ACID compliance, and generating ledger statements.

---

## 🚀 Features & Business Logic

- **Account Management (CRUD):**
    - Securely create bank accounts with auto-generated 4-digit account numbers.
    - Close accounts with strict validation (Requires a verified PIN and an exact balance of `$0.00`).
- **Financial Transactions:**
    - Handle real-time Deposits and Withdrawals.
    - Automated input validation (blocks negative amounts and over-drafting).
- **Data Protection & Consistency:**
    - **Encapsulation:** Uses private data fields with standard DTO mappings.
    - **Transaction Security:** Implements database rollbacks (`conn.rollback()`) to ensure ledger entries never fail mid-way.
- **Digital Statement Generation:** Returns a complete, chronologically sorted transaction ledger for specific accounts.

---

## 🛠️ Tech Stack & Architecture

- **Backend Framework:** Java 17 / Spring Boot 3.x
- **Database Layer:** Embedded SQLite via Native JDBC (`PreparedStatement`)
- **API Architecture:** RESTful Endpoints returning JSON payloads

---

## 🚦 API Endpoints & Testing Examples

You can interact with this API locally using PowerShell or cURL.

### 1. Create a New Account
* **HTTP Method:** POST
* **URL:** `/accounts`
* **Sample PowerShell Command:**
  `Invoke-RestMethod -Uri "http://localhost:8080/accounts" -Method Post -ContentType "application/json" -Body '{"holderName": "John Doe", "initialDeposit": 500.00, "pin": "1234"}'`

### 2. Check Account Balance
* **HTTP Method:** GET
* **URL:** `/accounts/{accountNumber}/balance?pin={pin}`
* **Sample PowerShell Command:**
  `Invoke-RestMethod -Uri "http://localhost:8080/accounts/1012/balance?pin=1234" -Method Get`

### 3. Account Closure (Delete)
* **HTTP Method:** DELETE
* **URL:** `/accounts/close`
* **Sample PowerShell Command:**
  `Invoke-RestMethod -Uri "http://localhost:8080/accounts/close" -Method Delete -ContentType "application/json" -Body '{"accountNumber": 1012, "pin": "1234"}'`

---

## 📈 Key Technical Takeaways From This Project
Building this application provided deep hands-on experience in:
1. **Spring Boot Serialization:** Understanding how Spring uses reflection and getters/setters to map raw incoming JSON strings directly to secure Java objects.
2. **Database Transaction Management:** Manually controlling connection auto-commits to handle relational updates across multiple tables safely.
3. **Version Control:** Standard Git workflows including repository tracking, staging, atomic commits, and remote cloud deployment to GitHub.