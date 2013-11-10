package src;

import java.util.ArrayList;
import java.util.List;

public class Client extends Process {

	Env env;
	ProcessId me;
	ProcessId[] replicas;
	Integer num;
	private int req_id;
	int waitForResponse = 0;
	Object syncObj = new Object();
	ProcessId requester;
	
	public Client(Env env, ProcessId me, ProcessId[] replicas, int num) {
		this.env = env;
		this.me = me;
		this.replicas = replicas;
		this.num = num;
		
		this.req_id = 0;
		this.requester = new ProcessId(me + "requester");
		env.addProc(me, this);
	}

	void request(String op){
		env.sendMessage(requester, new ClientRequestMessage(me, new Command(me, req_id, num.toString() + " " + op)));
		req_id ++;
	}

	@Override
	void body() {
		ClientRequester clientRequester = new ClientRequester(env, requester, me, replicas, syncObj);
		
		System.out.println("Here I am: " + me);
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof ClientMessage) {
				ClientMessage m = (ClientMessage) msg;
				request(m.op);				
			} else if (msg instanceof RespondMessage) {
				RespondMessage m = (RespondMessage) msg;
				if (waitForResponse == m.command.req_id) {
					System.out.println("Get response: " + m.command + m.result);
					synchronized(syncObj) { syncObj.notify(); }
					waitForResponse ++;
				}
			}
			else {
				System.err.println("Replica: unknown msg type");
			}
		}
		
	}

}
