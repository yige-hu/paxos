package src;

import java.util.HashMap;
import java.util.Map;

public class Client {
	
	Env env;
	int clientID;
	String name;
	Map<Integer, Account> accounts = new HashMap<Integer, Account>();
	
	Client(Env env, int clientID) {
		this.env = env;
		this.clientID = clientID;
	}
	
	Client(Env env, int clientID, String name) {
		this.env = env;
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
	
	public boolean transfer(int fromAcc, int toClientID,  int toAcc, long amount) {
		Account acc1 = accounts.get(fromAcc);
		Account acc2 = env.clients.get(toClientID).accounts.get(toAcc);
		if (acc1 == null || acc2 == null) { return false; }
		if (acc1.balance < amount) { return false; }
		acc1.balance -= amount;
		acc2.balance += amount;
		return true;
	}

}
