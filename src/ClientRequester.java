package src;

import java.util.List;
import java.util.Map;

public class ClientRequester extends Process {

	Object syncObj;
	ProcessId[] replicas;
	ProcessId client;
	List<ProcessId> responded_replicas;

	
	ClientRequester(Env env, ProcessId me, ProcessId client, ProcessId[] replicas, Object syncObj, 
			List<ProcessId> responded_replicas) {
		this.env = env;
		this.me = me;
		this.client = client;
		this.replicas = replicas;
		this.syncObj = syncObj;
		this.responded_replicas = responded_replicas;
		
		env.addProc(me, this);
	}
	
	void request(Command command){
		for (ProcessId ldr: replicas) {
			env.sendMessage(ldr,
				new RequestMessage(me, command));
		}
	}
	
	void requestROC(Command command){
		// TODO
		for (ProcessId ldr: responded_replicas) {
			env.sendMessage(ldr,
				new ROCRequestMessage(me, command));
		}
	}
	
	@Override
	void body() {
		System.out.println("Here I am: " + me);
		for (;;) {
			PaxosMessage msg = getNextMessage();
			
			if (msg instanceof ClientRequestMessage) {
				ClientRequestMessage m = (ClientRequestMessage) msg;
				request(m.command);
				try { synchronized(syncObj) { syncObj.wait(); } } catch(InterruptedException ie) { }
				
			} else if (msg instanceof ROCClientRequestMessage) {
				ROCClientRequestMessage m = (ROCClientRequestMessage) msg;
				requestROC(m.command);
				try { synchronized(syncObj) { syncObj.wait(); } } catch(InterruptedException ie) { }
			}
		}
		
	}

}
