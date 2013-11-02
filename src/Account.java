package src;

public class Account {
	
	BankClient client;
	int accountNum;
	long balance;
	
	Account(BankClient client, int accountNum) {
		this.client = client;
		this.accountNum = accountNum;
		this.balance = 0;
	}
	
	Account(BankClient client, int accountNum, long balance) {
		this.client = client;
		this.accountNum = accountNum;
		this.balance = balance;
	}
	
	public String toString(){
		return "accountNum=" + accountNum + " balance=" + balance;
	}

}
