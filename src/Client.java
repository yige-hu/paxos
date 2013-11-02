package src;

import java.util.ArrayList;
import java.util.List;

public class Client {

	Env env;
	ProcessId me;
	ProcessId[] replicas;
	private int req_id;
	
	public Client(Env env, ProcessId me, ProcessId[] replicas) {
		this.env = env;
		this.me = me;
		this.replicas = replicas;
		this.req_id = 0;
	}

	void request(String op){
		
		for (ProcessId ldr: replicas) {
			env.sendMessage(ldr,
				new RequestMessage(me, new Command(me, req_id ++, op)));
		}
	}
	
	void respond(String op){
		System.out.println(me + ": get respond, operation=" + op.toString());
	}

}
