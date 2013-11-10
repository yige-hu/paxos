package src;

public class ClientRequester extends Process {

	Object syncObj;
	ProcessId[] replicas;
	ProcessId client;

	
	ClientRequester(Env env, ProcessId me, ProcessId client, ProcessId[] replicas, Object syncObj) {
		this.env = env;
		this.me = me;
		this.client = client;
		this.replicas = replicas;
		this.syncObj = syncObj;
		
		env.addProc(me, this);
	}
	
	void request(Command command){
		for (ProcessId ldr: replicas) {
			env.sendMessage(ldr,
				new RequestMessage(me, command));
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
			}
		}
		
	}

}
