package src;

import java.net.URL;
import java.util.*;

public class Replica extends Process {
	ProcessId[] leaders;
	int slot_num = 1;
	Map<Integer /* slot number */, Command> proposals = new HashMap<Integer, Command>();
	Map<Integer /* slot number */, Command> decisions = new HashMap<Integer, Command>();
	
	Map<Integer, BankClient> bankClients = new HashMap<Integer, BankClient>();
	private DataInfo dataInfo;

	public Replica(Env env, ProcessId me, ProcessId[] leaders){
		this.env = env;
		this.me = me;
		this.leaders = leaders;
		this.dataInfo = new DataInfo(me + ".dat");
		this.dataInfo.clearDataInfo();
		env.addProc(me, this);
	}

	void propose(Command c){
		if (!decisions.containsValue(c)) {
			for (int s = 1;; s++) {
				if (!proposals.containsKey(s) && !decisions.containsKey(s)) {
					proposals.put(s, c);
					for (ProcessId ldr: leaders) {
						sendMessage(ldr, new ProposeMessage(me, s, c));
					}
					break;
				}
			}
		}
	}

	void perform(Command c){
		for (int s = 1; s < slot_num; s++) {
			if (c.equals(decisions.get(s))) {
				slot_num++;
				return;
			}
		}
		
		String op = c.op.toString();
		try {
			StringTokenizer t = new StringTokenizer(op, " ");
			String type = t.nextToken();
			
			if (type.equals("addBankClient")) {
				
				int clientID = Integer.parseInt(t.nextToken());
				
				if (t.hasMoreTokens()) {
					String name = t.nextToken();
					addBankClient(clientID, name);
				} else {
					addBankClient(clientID);
				}
				
			} else if (type.equals("createAccount")) {
				
				int clientID = Integer.parseInt(t.nextToken());
				int accountNum = Integer.parseInt(t.nextToken());
				
				if (t.hasMoreTokens()) {
					long balance = Long.parseLong(t.nextToken());
					createAccount(clientID, accountNum, balance);
				} else {
					createAccount(clientID, accountNum);
				}
				
			} else if (type.equals("deposit")) {
				
				int clientID = Integer.parseInt(t.nextToken());
				int accountNum = Integer.parseInt(t.nextToken());
				long amount = Long.parseLong(t.nextToken());
				
				deposit(clientID, accountNum, amount);
				
			} else if (type.equals("withdraw")) {
				
				int clientID = Integer.parseInt(t.nextToken());
				int accountNum = Integer.parseInt(t.nextToken());
				long amount = Long.parseLong(t.nextToken());
				
				withdraw(clientID, accountNum, amount);
				
			} else if (type.equals("transfer")) {
				
				int fromClient = Integer.parseInt(t.nextToken());
				int fromAcc = Integer.parseInt(t.nextToken());
				int toAcc = Integer.parseInt(t.nextToken());
				long amount = Long.parseLong(t.nextToken());
				
				if (t.hasMoreTokens()) {
					int toClient = Integer.parseInt(t.nextToken());
					transfer(fromClient, fromAcc, toAcc, amount, toClient);
				} else {
					transfer(fromClient, fromAcc, toAcc, amount);
				}
			}
		} catch (Exception e) {
			System.out.println("Invalid command: " + op + ", " + e.toString());
		}
		
		dataInfo.writeDataInfo("" + me + ": perform " + c);
		for (BankClient client : bankClients.values()) {
			dataInfo.writeDataInfo(client.toString());
			
			for (Account account : client.accounts.values()) {
				dataInfo.writeDataInfo("\t" + account.toString());
			}
		}
		
		System.out.println("" + me + ": perform " + c);
		slot_num++;
	}














	public void body(){
		System.out.println("Here I am: " + me);
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof RequestMessage) {
				RequestMessage m = (RequestMessage) msg;
				propose(m.command);
			}

			else if (msg instanceof DecisionMessage) {
				DecisionMessage m = (DecisionMessage) msg;
				decisions.put(m.slot_number, m.command);
				for (;;) {
					Command c = decisions.get(slot_num);
					if (c == null) {
						break;
					}
					Command c2 = proposals.get(slot_num);
					if (c2 != null && !c2.equals(c)) {
						propose(c2);
					}
					perform(c);
				}
			}
			else {
				System.err.println("Replica: unknown msg type");
			}
		}
	}
	
	
	
	
	public void addBankClient(int clientID) {
		BankClient c = new BankClient(this, clientID);
		bankClients.put(clientID, c);
	}
	
	public void addBankClient(int clientID, String name) {
		BankClient c = new BankClient(this, clientID, name);
		bankClients.put(clientID, c);
	}
	
	public void createAccount(int clientID, int accountNum) {
		BankClient c = bankClients.get(clientID);
		c.createAccount(accountNum);
	}
	
	public void createAccount(int clientID, int accountNum, long balance) {
		BankClient c = bankClients.get(clientID);
		c.createAccount(accountNum, balance);
	}
	
	public boolean deposit(int clientID, int accountNum, long amount) {
		BankClient c = bankClients.get(clientID);
		return c.deposit(accountNum, amount);
	}
	
	public boolean withdraw(int clientID, int accountNum, long amount) {
		BankClient c = bankClients.get(clientID);
		return c.withdraw(accountNum, amount);
	}
	
	public boolean transfer(int clientID, int fromAcc, int toAcc, long amount) {
		BankClient c = bankClients.get(clientID);
		return c.transfer(fromAcc, toAcc, amount);
	}
	
	public boolean transfer(int fromClient, int fromAcc, int toAcc, long amount, int toClient) {
		BankClient c = bankClients.get(fromClient);
		return c.transfer(fromAcc, toClient, toAcc, amount);
	}
}
