import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class BankAccount {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String bankName = readLine(scanner, "Enter bank name: ");
		Bank bank = new Bank(bankName);

		System.out.println("Enter savings account details");
		String savingsNo = readLine(scanner, "Account number: ");
		String savingsHolder = readLine(scanner, "Holder name: ");
		double savingsOpening = readDouble(scanner, "Opening balance: ");
		SavingsAccount savings = new SavingsAccount(savingsNo, savingsHolder, savingsOpening);

		System.out.println("Enter current account details");
		String currentNo = readLine(scanner, "Account number: ");
		String currentHolder = readLine(scanner, "Holder name: ");
		double currentOpening = readDouble(scanner, "Opening balance: ");
		double overdraftLimit = readDouble(scanner, "Overdraft limit: ");
		CurrentAccount current = new CurrentAccount(currentNo, currentHolder, currentOpening, overdraftLimit);

		System.out.println("Enter fixed deposit account details");
		String fixedNo = readLine(scanner, "Account number: ");
		String fixedHolder = readLine(scanner, "Holder name: ");
		double fixedOpening = readDouble(scanner, "Opening balance: ");
		FixedDepositAccount fixed = new FixedDepositAccount(fixedNo, fixedHolder, fixedOpening);

		bank.addAccount(savings);
		bank.addAccount(current);
		bank.addAccount(fixed);
		System.out.println();

		System.out.println("--- Savings Account Transactions ---");
		double savingsDeposit = readDouble(scanner, "Deposit amount: ");
		printDeposit(savings, savingsDeposit);
		double savingsWithdraw = readDouble(scanner, "Withdraw amount: ");
		printWithdraw(savings, savingsWithdraw);
		double savingsWithdrawFail = readDouble(scanner, "Withdraw amount (should fail): ");
		printWithdrawFailure(savings, savingsWithdrawFail, "Savings");
		double interest = savings.applyInterest();
		System.out.println("Interest applied: Rs." + format2(interest) + " at 4.5%");
		System.out.println();

		System.out.println("--- Current Account Transactions ---");
		double currentDeposit = readDouble(scanner, "Deposit amount: ");
		printDeposit(current, currentDeposit);
		double currentWithdraw = readDouble(scanner, "Withdraw amount: ");
		printWithdraw(current, currentWithdraw);
		double currentWithdrawFail = readDouble(scanner, "Withdraw amount (should fail): ");
		printWithdrawFailure(current, currentWithdrawFail, "Current");
		System.out.println();

		printAllAccounts(bank);
		System.out.println();

		printBankSummary(bank);
		System.out.println();

		printStatement(savings);
		scanner.close();
	}

	private static String readLine(Scanner scanner, String prompt) {
		System.out.print(prompt);
		String value = scanner.nextLine().trim();
		while (value.isEmpty()) {
			System.out.print(prompt);
			value = scanner.nextLine().trim();
		}
		return value;
	}

	private static double readDouble(Scanner scanner, String prompt) {
		System.out.print(prompt);
		while (!scanner.hasNextDouble()) {
			scanner.next();
			System.out.print(prompt);
		}
		double value = scanner.nextDouble();
		scanner.nextLine();
		return value;
	}

	static String format1(double value) {
		return String.format(Locale.US, "%.1f", value);
	}

	static String format2(double value) {
		return String.format(Locale.US, "%.2f", value);
	}

	private static void printDeposit(Account account, double amount) {
		account.deposit(amount);
		System.out.println("OK: Deposited Rs." + format1(amount) + " | New Balance: Rs." + format1(account.getBalance()));
	}

	private static void printWithdraw(Account account, double amount) {
		if (account.withdraw(amount)) {
			System.out.println("OK: Withdrawn Rs." + format1(amount) + " | New Balance: Rs." + format1(account.getBalance()));
		}
	}

	private static void printWithdrawFailure(Account account, double amount, String typeLabel) {
		if (!account.withdraw(amount)) {
			System.out.println("WARN: " + account.getLastFailureMessage());
			System.out.println("ERR: Withdrawal of Rs." + format1(amount) + " not allowed for " + typeLabel);
		}
	}

	private static void printAllAccounts(Bank bank) {
		System.out.println("===== " + bank.getName() + " - ALL ACCOUNTS =====");
		String header = "| "
				+ padRight("Acc No", 8) + " | "
				+ padRight("Holder Name", 18) + " | "
				+ padRight("Type", 13) + " | "
				+ padRight("Balance", 12) + " |";
		System.out.println(header);
		System.out.println(repeat('-', header.length()));

		for (Account account : bank.getAccounts()) {
			String row = "| "
					+ padRight(account.getAccountNo(), 8) + " | "
					+ padRight(account.getHolderName(), 18) + " | "
					+ padRight(account.getType(), 13) + " | "
					+ padRight("Rs." + format2(account.getBalance()), 12) + " |";
			System.out.println(row);
		}

		System.out.println(repeat('=', header.length()));
	}

	private static void printBankSummary(Bank bank) {
		System.out.println("===== BANK SUMMARY =====");
		System.out.println("Bank Name      : " + bank.getName());
		System.out.println("Total Accounts : " + bank.getAccounts().size());
		System.out.println("Total Deposits : Rs." + format2(bank.getTotalDeposits()));
		System.out.println("========================");
	}

	private static void printStatement(Account account) {
		System.out.println("===== ACCOUNT STATEMENT =====");
		System.out.println("Account No : " + account.getAccountNo());
		System.out.println("Holder     : " + account.getHolderName());
		System.out.println("Type       : " + account.getType());
		System.out.println("Balance    : Rs." + format2(account.getBalance()));
		System.out.println();
		System.out.println("Transaction History:");

		int index = 1;
		for (String entry : account.getHistory()) {
			System.out.println(index + ". " + entry);
			index++;
		}

		System.out.println("=============================");
	}

	private static String padRight(String value, int width) {
		return String.format(Locale.US, "%-" + width + "s", value);
	}

	private static String repeat(char ch, int count) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			builder.append(ch);
		}
		return builder.toString();
	}
}

interface Transactional {
	void deposit(double amount);

	boolean withdraw(double amount);
}

abstract class Account implements Transactional {
	private final String accountNo;
	private final String holderName;
	private final String type;
	protected double balance;
	private final List<String> history;
	private String lastFailureMessage;

	protected Account(String accountNo, String holderName, String type, double openingBalance) {
		this.accountNo = accountNo;
		this.holderName = holderName;
		this.type = type;
		this.balance = openingBalance;
		this.history = new ArrayList<>();
		this.history.add("Account opened with balance: Rs." + BankAccount.format1(openingBalance));
	}

	@Override
	public void deposit(double amount) {
		balance += amount;
		history.add("CREDIT | Rs." + BankAccount.format2(amount) + " | Balance: Rs." + BankAccount.format2(balance));
	}

	@Override
	public boolean withdraw(double amount) {
		String failureReason = validateWithdrawal(amount);
		if (failureReason != null) {
			lastFailureMessage = failureReason;
			return false;
		}
		balance -= amount;
		history.add("DEBIT  | Rs." + BankAccount.format2(amount) + " | Balance: Rs." + BankAccount.format2(balance));
		return true;
	}

	protected String validateWithdrawal(double amount) {
		return null;
	}

	protected void addHistory(String entry) {
		history.add(entry);
	}

	public String getAccountNo() {
		return accountNo;
	}

	public String getHolderName() {
		return holderName;
	}

	public String getType() {
		return type;
	}

	public double getBalance() {
		return balance;
	}

	public List<String> getHistory() {
		return Collections.unmodifiableList(history);
	}

	public String getLastFailureMessage() {
		return lastFailureMessage;
	}
}

class SavingsAccount extends Account {
	private static final double MIN_BALANCE = 1000.0;
	private static final double INTEREST_RATE = 0.045;

	public SavingsAccount(String accountNo, String holderName, double openingBalance) {
		super(accountNo, holderName, "Savings", openingBalance);
	}

	@Override
	protected String validateWithdrawal(double amount) {
		if (balance - amount < MIN_BALANCE) {
			return "Cannot withdraw: Balance must stay above Rs." + BankAccount.format1(MIN_BALANCE);
		}
		return null;
	}

	public double applyInterest() {
		double interest = balance * INTEREST_RATE;
		balance += interest;
		addHistory("CREDIT | Rs." + BankAccount.format2(interest) + " | Balance: Rs." + BankAccount.format2(balance));
		return interest;
	}
}

class CurrentAccount extends Account {
	private final double overdraftLimit;

	public CurrentAccount(String accountNo, String holderName, double openingBalance, double overdraftLimit) {
		super(accountNo, holderName, "Current", openingBalance);
		this.overdraftLimit = overdraftLimit;
	}

	@Override
	protected String validateWithdrawal(double amount) {
		if (balance < 0 || balance - amount < -overdraftLimit) {
			return "Exceeds overdraft limit of Rs." + BankAccount.format1(overdraftLimit);
		}
		return null;
	}
}

class FixedDepositAccount extends Account {
	public FixedDepositAccount(String accountNo, String holderName, double openingBalance) {
		super(accountNo, holderName, "Fixed Deposit", openingBalance);
	}

	@Override
	protected String validateWithdrawal(double amount) {
		return "Fixed Deposit withdrawals are locked";
	}
}

class Bank {
	private final String name;
	private final List<Account> accounts;

	public Bank(String name) {
		this.name = name;
		this.accounts = new ArrayList<>();
	}

	public void addAccount(Account account) {
		accounts.add(account);
		System.out.println("OK: Account added: " + account.getAccountNo() + " for " + account.getHolderName());
	}

	public String getName() {
		return name;
	}

	public List<Account> getAccounts() {
		return Collections.unmodifiableList(accounts);
	}

	public double getTotalDeposits() {
		double total = 0.0;
		for (Account account : accounts) {
			total += account.getBalance();
		}
		return total;
	}
}

