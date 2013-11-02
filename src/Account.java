package src;

public class Account {
	
	Client client;
	int accountNum;
	long balance;
	
	Account(Client client, int accountNum) {
		this.client = client;
		this.accountNum = accountNum;
		this.balance = 0;
	}
	
	Account(Client client, int accountNum, long balance) {
		this.client = client;
		this.accountNum = accountNum;
		this.balance = balance;
	}

}
