# 🏦 AcnBankingSystem

**AcnBankingSystem** is a robust, enterprise-grade banking API built with **Spring Boot**. It provides a secure and scalable foundation for managing financial accounts, processing real-time transactions, and ensuring data integrity through strict ACID compliance.

---

## ✨ Features

* **Secure Authentication:** Multi-level security using **Spring Security** and **JWT** (JSON Web Tokens).
* **Account Management:** Create and manage multiple account types (Savings, Current) with automated account number generation.
* **Real-time Transactions:** Seamless fund transfers, deposits, and withdrawals with transactional rollback support.
* **Transaction Auditing:** Comprehensive history logging for every movement of funds, including timestamps and reference IDs.
* **Input Validation:** Robust server-side validation using Hibernate Validator to ensure financial data accuracy.
* **Error Handling:** Global exception handling for clean, consistent API error responses.

---

## 🛠️ Tech Stack

* **Framework:** Spring Boot 3.x
* **Language:** Java 21
* **Security:** Spring Security & JWT
* **Persistence:** Spring Data JPA (Hibernate)
* **Database:** PostgreSQL / MySQL
* **Build Tool:** Maven
* **Documentation:** Swagger UI / OpenAPI 3

---

## 🚀 Getting Started

### Prerequisites

* **JDK 21** or higher
* **Maven 3.8+**
* **Database:** PostgreSQL (or your preferred SQL database)

### Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/yourusername/AcnBankingSystem.git](https://github.com/yourusername/AcnBankingSystem.git)
    cd AcnBankingSystem
    ```

2.  **Database Configuration:**
    Update `src/main/resources/application.properties` with your credentials:
    ```properties
    spring.application.name=AcnBankingSystem
    spring.datasource.url=jdbc:postgresql://localhost:5432/acn_banking_db
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    
    # Hibernate strategy
    spring.jpa.hibernate.ddl-auto=update
    ```

3.  **Build and Run:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

The application will start on `http://localhost:8080`.

---

## 📖 API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register a new user |
| `POST` | `/api/v1/auth/login` | Authenticate and get JWT token |
| `GET` | `/api/v1/accounts` | List all accounts for the user |
| `POST` | `/api/v1/accounts` | Open a new bank account |
| `POST` | `/api/v1/transactions/transfer` | Transfer funds between accounts |
| `GET` | `/api/v1/transactions/history` | View transaction logs |

> **Note:** Access the interactive documentation at `http://localhost:8080/swagger-ui.html` once the app is running.

---

## 🧪 Testing

To run the unit and integration test suite:

```bash
mvn test
