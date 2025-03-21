package bankingApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ============================
// Custom Exceptions
// ============================
class NegativeDepositException extends Exception {
    public NegativeDepositException(String message) {
        super(message);
    }
}

class OverdrawException extends Exception {
    public OverdrawException(String message) {
        super(message);
    }
}

class InvalidAccountOperationException extends Exception {
    public InvalidAccountOperationException(String message) {
        super(message);
    }
}

// ============================
// Observer Pattern - Observer and Logger
// ============================
interface Observer {
    void update(String message);
}

class TransactionLogger implements Observer {
   
    public void update(String message) {
        System.out.println("[Log]: " + message);
    }
}

// ============================
// BankAccount Class
// ============================
class BankAccount {
    protected String accountNumber;
    protected double balance;
    protected boolean isActive;
    private List<Observer> observers = new ArrayList<>();

    public BankAccount(String accountNumber, double initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.isActive = true;
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    private void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public void deposit(double amount) throws NegativeDepositException, InvalidAccountOperationException {
        if (!isActive) throw new InvalidAccountOperationException("Cannot deposit to a closed account.");
        if (amount < 0) throw new NegativeDepositException("Deposit amount cannot be negative.");

        balance += amount;
        notifyObservers("Deposited $" + amount);
    }

    public void withdraw(double amount) throws OverdrawException, InvalidAccountOperationException {
        if (!isActive) throw new InvalidAccountOperationException("Cannot withdraw from a closed account.");
        if (amount > balance) throw new OverdrawException("Insufficient funds.");

        balance -= amount;
        notifyObservers("Withdrew $" + amount);
    }

    public double getBalance() {
        return balance;
    }

    public void closeAccount() {
        isActive = false;
        notifyObservers("Account closed.");
    }
}

// ============================
// Decorator Pattern - SecureBankAccount
// ============================
class SecureBankAccount extends BankAccount {
    private BankAccount baseAccount;
    private String pin;

    public SecureBankAccount(BankAccount baseAccount, String pin) {
        super(baseAccount.accountNumber, baseAccount.balance);
        this.baseAccount = baseAccount;
        this.pin = pin;
    }

    private boolean validatePin(String inputPin) {
        return pin.equals(inputPin);
    }

    public void secureDeposit(double amount, String inputPin) throws NegativeDepositException, InvalidAccountOperationException {
        if (!validatePin(inputPin)) {
            System.out.println("Invalid PIN. Transaction denied.");
            return;
        }
        baseAccount.deposit(amount);
    }

    public void secureWithdraw(double amount, String inputPin) throws OverdrawException, InvalidAccountOperationException {
        if (!validatePin(inputPin)) {
            System.out.println("Invalid PIN. Transaction denied.");
            return;
        }
        if (amount > 500) {
            System.out.println("Withdrawal limit exceeded. Max: $500");
            return;
        }
        baseAccount.withdraw(amount);
    }

    public double getBalance() {
        return baseAccount.getBalance();
    }
}

// ============================
// Main Program
// ============================
public class BankAccountTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Create Bank Account
            System.out.print("Enter initial balance: ");
            double initialBalance = scanner.nextDouble();
            scanner.nextLine(); // Consume newline

            BankAccount account = new BankAccount("123456", initialBalance);
            System.out.println("Bank Account Created: #123456");

            // Attach Logger
            account.addObserver(new TransactionLogger());

            // Set PIN and Secure Account
            System.out.print("Set your account PIN: ");
            String pin = scanner.nextLine();
            SecureBankAccount secureAccount = new SecureBankAccount(account, pin);

            // Makes Deposit
            System.out.print("Enter deposit amount: ");
            double depositAmount = scanner.nextDouble();
            scanner.nextLine();
            System.out.print("Enter PIN: ");
            String depositPin = scanner.nextLine();
            secureAccount.secureDeposit(depositAmount, depositPin);

            // Makes Withdrawal
            System.out.print("Enter withdrawal amount: ");
            double withdrawAmount = scanner.nextDouble();
            scanner.nextLine();
            System.out.print("Enter PIN: ");
            String withdrawPin = scanner.nextLine();
            secureAccount.secureWithdraw(withdrawAmount, withdrawPin);

            // Final Balance
            System.out.println("Final Balance: $" + secureAccount.getBalance());

        } catch (NegativeDepositException | OverdrawException | InvalidAccountOperationException e) {
            System.out.println("Transaction Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
