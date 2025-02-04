import java.util.*;


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
    }

    public List<Account> getAccounts() {
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
            System.out.println("Deposit successful. New balance: " + balance + " " + currency);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
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
        System.out.print("Enter user ID: ");
        String userId = scanner.next();
        User user = users.get(userId);

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.print("Enter account number: ");
        String accountNumber = scanner.next();
        Account account = findAccount(user, accountNumber);

        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Enter deposit amount: ");
        double amount = scanner.nextDouble();
        account.deposit(amount);
    }

    public void withdraw() {
        System.out.print("Enter user ID: ");
        String userId = scanner.next();
        User user = users.get(userId);

        if (user == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.print("Enter account number: ");
        String accountNumber = scanner.next();
        Account account = findAccount(user, accountNumber);

        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Enter withdrawal amount: ");
        double amount = scanner.nextDouble();

        try {
            account.withdraw(amount);
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
        }
    }

    private Account findAccount(User user, String accountNumber) {
        for (Account account : user.getAccounts()) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
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
            System.out.println("5. Exit");

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
                    System.out.println("Exiting application.");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
