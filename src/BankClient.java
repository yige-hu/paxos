package src;

import java.util.HashMap;
import java.util.Map;

public class BankClient {
	
	Replica replica;
	int clientID;
	String name;
	Map<Integer, Account> accounts = new HashMap<Integer, Account>();
	
	BankClient(Replica replica, int clientID) {
		this.replica = replica;
		this.clientID = clientID;
	}
	
	BankClient(Replica replica, int clientID, String name) {
		this.replica = replica;
		this.clientID = clientID;
		this.name = name;
	}
	
	public void createAccount(int accountNum) {
		Account acc = new Account(this, accountNum);
		accounts.put(accountNum, acc);
	}
	
	public void createAccount(int accountNum, long balance) {
		Account acc = new Account(this, accountNum, balance);
		accounts.put(accountNum, acc);
	}
	
	public boolean deposit(int accountNum, long amount) {
		Account acc = accounts.get(accountNum);
		if (acc == null) { return false; }
		acc.balance += amount;
		return true;
	}
	
	public boolean withdraw(int accountNum, long amount) {
		Account acc = accounts.get(accountNum);
		if (acc == null) { return false; }
		if (acc.balance < amount) { return false; }
		acc.balance -= amount;
		return true;
	}
	
	public boolean transfer(int fromAcc, int toAcc, long amount) {
		Account acc1 = accounts.get(fromAcc);
		Account acc2 = accounts.get(toAcc);
		if (acc1 == null || acc2 == null) { return false; }
		if (acc1.balance < amount) { return false; }
		acc1.balance -= amount;
		acc2.balance += amount;
		return true;
	}
	
	public boolean transfer(int fromAcc, int toClient,  int toAcc, long amount) {
		Account acc1 = accounts.get(fromAcc);
		Account acc2 = replica.bankClients.get(toClient).accounts.get(toAcc);
		if (acc1 == null || acc2 == null) { return false; }
		if (acc1.balance < amount) { return false; }
		acc1.balance -= amount;
		acc2.balance += amount;
		return true;
	}
	
	public long inquiry(int accountNum) {
		Account acc = accounts.get(accountNum);
		return acc.balance;
	}
	
	public String toString(){
		return "clientID=" + clientID + " name=" + name;
	}

}
