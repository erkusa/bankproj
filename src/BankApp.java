import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class User {
    private String userId;
    private String name;
    private String password;
    private List<Account> accounts;

    public User(String userId, String name, String password) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.accounts = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public void addAccount(Account account) {
        accounts.add(account);
        DatabaseConnection.executeUpdate("INSERT INTO accounts (account_number, user_id, currency) VALUES (?, ?, ?)",
                account.getAccountNumber(), userId, account.getCurrency());
    }

    public List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM accounts WHERE user_id = ?")) {
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                accounts.add(new Account(rs.getString("account_number"), rs.getString("currency"), rs.getDouble("balance")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }
}

class Account {
    private String accountNumber;
    private double balance;
    private String currency;

    public Account(String accountNumber, String currency) {
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.balance = 0.0;
    }

    public Account(String accountNumber, String currency, double balance) {
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            DatabaseConnection.executeUpdate("UPDATE accounts SET balance = ? WHERE account_number = ?", balance, accountNumber);
            System.out.println("Deposit successful. New balance: " + balance + " " + currency);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            DatabaseConnection.executeUpdate("UPDATE accounts SET balance = ? WHERE account_number = ?", balance, accountNumber);
            System.out.println("Withdrawal successful. New balance: " + balance + " " + currency);
        } else {
            throw new InsufficientFundsException("Insufficient funds or invalid amount.");
        }
    }
}


class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

class BankService {
    private Map<String, User> users;
    private Scanner scanner;

    public BankService() {
        users = new HashMap<>();
        scanner = new Scanner(System.in);
        loadUsersFromDatabase();
    }

    private void loadUsersFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String userId = rs.getString("user_id");
                String name = rs.getString("name");
                String password = rs.getString("password");
                users.put(userId, new User(userId, name, password));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void transfer() {
        System.out.print("Enter your user ID: ");
        String userId = scanner.next();
        User user = users.get(userId);

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.print("Enter your password: ");
        String password = scanner.next();

        if (!user.authenticate(password)) {
            System.out.println("Incorrect password. Transfer canceled.");
            return;
        }

        System.out.print("Enter your account number: ");
        String fromAccount = scanner.next();
        System.out.print("Enter recipient's account number: ");
        String toAccount = scanner.next();
        System.out.print("Enter transfer amount: ");
        double amount = scanner.nextDouble();

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            String selectBalanceQuery = "SELECT balance FROM accounts WHERE account_number = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectBalanceQuery)) {
                selectStmt.setString(1, fromAccount);
                ResultSet rs = selectStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Sender account not found.");
                    return;
                }

                double senderBalance = rs.getDouble("balance");

                if (senderBalance < amount) {
                    System.out.println("Insufficient funds.");
                    return;
                }
            }

            String updateSenderQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            try (PreparedStatement updateSenderStmt = connection.prepareStatement(updateSenderQuery)) {
                updateSenderStmt.setDouble(1, amount);
                updateSenderStmt.setString(2, fromAccount);
                updateSenderStmt.executeUpdate();
            }

            String updateReceiverQuery = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            try (PreparedStatement updateReceiverStmt = connection.prepareStatement(updateReceiverQuery)) {
                updateReceiverStmt.setDouble(1, amount);
                updateReceiverStmt.setString(2, toAccount);
                int rowsAffected = updateReceiverStmt.executeUpdate();

                if (rowsAffected == 0) {
                    System.out.println("Recipient account not found.");
                    connection.rollback();
                    return;
                }
            }

            connection.commit();
            System.out.println("Transfer successful. Transferred " + amount + " from " + fromAccount + " to " + toAccount);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void withdraw() {
        System.out.print("Enter user ID: ");
        String userId = scanner.next();
        User user = users.get(userId);

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.next();

        if (!user.authenticate(password)) {
            System.out.println("Incorrect password. Withdrawal canceled.");
            return;
        }

        System.out.print("Enter account number: ");
        String accountNumber = scanner.next();
        System.out.print("Enter withdrawal amount: ");
        double amount = scanner.nextDouble();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = connection.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?");
             PreparedStatement updateStmt = connection.prepareStatement("UPDATE accounts SET balance = ? WHERE account_number = ?")) {

            selectStmt.setString(1, accountNumber);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");

                if (amount > 0 && amount <= currentBalance) {
                    double newBalance = currentBalance - amount;
                    updateStmt.setDouble(1, newBalance);
                    updateStmt.setString(2, accountNumber);
                    updateStmt.executeUpdate();
                    System.out.println("Withdrawal successful. New balance: " + newBalance);
                } else {
                    System.out.println("Insufficient funds or invalid amount.");
                }
            } else {
                System.out.println("Account not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createUser() {
        System.out.print("Enter user ID: ");
        String userId = scanner.next();
        System.out.print("Enter name: ");
        String name = scanner.next();
        System.out.print("Enter password: ");
        String password = scanner.next();

        if (users.containsKey(userId)) {
            System.out.println("User with this ID already exists.");
        } else {
            users.put(userId, new User(userId, name, password));
            DatabaseConnection.executeUpdate("INSERT INTO users (user_id, name, password) VALUES (?, ?, ?)",
                    userId, name, password);
            System.out.println("User created successfully.");
        }
    }

    public void createAccount() {
        System.out.print("Enter user ID: ");
        String userId = scanner.next();
        User user = users.get(userId);

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.print("Enter account number: ");
        String accountNumber = scanner.next();
        System.out.print("Enter currency (USD/EUR/KZT): ");
        String currency = scanner.next().toUpperCase();

        Account account = new Account(accountNumber, currency);
        user.addAccount(account);
        System.out.println("Account created successfully.");
    }

    public void deposit() {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.next();

        System.out.print("Enter deposit amount: ");
        double amount = scanner.nextDouble();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT balance FROM accounts WHERE account_number = ?")) {
            statement.setString(1, accountNumber);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                double newBalance = currentBalance + amount;
                DatabaseConnection.executeUpdate("UPDATE accounts SET balance = ? WHERE account_number = ?", newBalance, accountNumber);
                System.out.println("Deposit successful. New balance: " + newBalance);
            } else {
                System.out.println("Account not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class BankApp {
    public static void main(String[] args) {
        BankService bankService = new BankService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nChoose an action:");
            System.out.println("1. Create user");
            System.out.println("2. Create account");
            System.out.println("3. Deposit");
            System.out.println("4. Withdraw");
            System.out.println("5. Transfer money");
            System.out.println("6. Exit");

            System.out.print("Enter your choice: ");
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    bankService.createUser();
                    break;
                case 2:
                    bankService.createAccount();
                    break;
                case 3:
                    bankService.deposit();
                    break;
                case 4:
                    bankService.withdraw();
                    break;
                case 5:
                    bankService.transfer();
                    break;
                case 6:
                    System.out.println("Exiting application.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}