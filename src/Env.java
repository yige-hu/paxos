package src;

import java.util.*;

public class Env {
	Map<ProcessId, Process> procs = new HashMap<ProcessId, Process>();
	//public final static int nAcceptors = 3, nReplicas = 2, nLeaders = 2, nRequests = 10;
	public final static int nAcceptors = 3, nReplicas = 2, nLeaders = 2, nClients = 4;
	
	Client clients[] = new Client[nClients];

	synchronized void sendMessage(ProcessId dst, PaxosMessage msg){
		Process p = procs.get(dst);
		if (p != null) {
			p.deliver(msg);
		}
	}

	synchronized void addProc(ProcessId pid, Process proc){
		procs.put(pid, proc);
		proc.start();
	}

	synchronized void removeProc(ProcessId pid){
		procs.remove(pid);
	}

	void run(String[] args){
		ProcessId[] acceptors = new ProcessId[nAcceptors];
		ProcessId[] replicas = new ProcessId[nReplicas];
		ProcessId[] leaders = new ProcessId[nLeaders];

		for (int i = 0; i < nAcceptors; i++) {
			acceptors[i] = new ProcessId("acceptor:" + i);
			Acceptor acc = new Acceptor(this, acceptors[i]);
		}
		for (int i = 0; i < nReplicas; i++) {
			replicas[i] = new ProcessId("replica:" + i);
			Replica repl = new Replica(this, replicas[i], leaders);
		}
		for (int i = 0; i < nLeaders; i++) {
			leaders[i] = new ProcessId("leader:" + i);
			Leader leader = new Leader(this, leaders[i], acceptors, replicas);
		}

//		for (int i = 1; i < nRequests; i++) {
//			ProcessId pid = new ProcessId("client:" + i);
//			for (int r = 0; r < nReplicas; r++) {
//				sendMessage(replicas[r],
//					new RequestMessage(pid, new Command(pid, 0, "operation " + i)));
//			}
//		}
		
		for (int i = 0; i < nClients; i++) {
			clients[i] = new Client(this, new ProcessId("client:" + i), replicas);
		}
		
		testSuit1();
		
	}

	private void testSuit1() {
		clients[0].request("addBankClient 1");
		clients[1].request("addBankClient 2 Jim");
		clients[0].request("addBankClient 3 Lily");
		clients[2].request("addBankClient 4");
		
		clients[1].request("createAccount 1 1 100");
		clients[2].request("createAccount 1 2");
		clients[3].request("createAccount 2 1 200");
		clients[1].request("createAccount 3 1 50");
		
		clients[1].request("deposit 1 1 150");
		clients[2].request("deposit 1 2 300");
		clients[0].request("deposit 3 1 225");
		
		clients[3].request("withdraw 1 1 30");
		clients[3].request("withdraw 2 1 45");
		
		clients[1].request("transfer 1 1 2 30");
		clients[2].request("transfer 2 1 1 42 3");
	}

	public static void main(String[] args){
		new Env().run(args);
	}
}
