package src;

import java.util.ArrayList;
import java.util.List;

public class Client extends Process {

	Env env;
	ProcessId me;
	ProcessId[] replicas;
	Integer num;
	Object syncObj;
	private int req_id;
	boolean waitForResponse = false;
	
	public Client(Env env, ProcessId me, ProcessId[] replicas, int num, Object syncObj) {
		this.env = env;
		this.me = me;
		this.replicas = replicas;
		this.num = num;
		this.syncObj = syncObj;
		this.req_id = 0;
		
		env.addProc(me, this);
	}

	void request(String op){
		for (ProcessId ldr: replicas) {
			env.sendMessage(ldr,
				new RequestMessage(me, new Command(me, req_id, num.toString() + " " + op)));
		}
		req_id ++;
	}

	@Override
	void body() {
		System.out.println("Here I am: " + me);
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof ClientMessage) {
				ClientMessage m = (ClientMessage) msg;
				request(m.op);
				waitForResponse = true;
				
			} else if (msg instanceof RespondMessage) {
				if (waitForResponse) {
					RespondMessage m = (RespondMessage) msg;
					System.out.println("Get response: " + m.command + m.result);
					synchronized(syncObj) { syncObj.notify(); }
					waitForResponse = false;
				}
			}
			else {
				System.err.println("Replica: unknown msg type");
			}
		}
		
	}

}
