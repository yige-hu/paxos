package src;

public class MonitorResponder extends Process {

	MonitorResponder(Env env, ProcessId me) {
		this.env = env;
		this.me = me;
		env.addProc(me, this);
	}
	
	@Override
	void body() {
		System.out.println("Here I am: " + me);
		
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof PingRequestMessage) {
				PingRequestMessage m = (PingRequestMessage) msg;
				sendMessage(msg.src, new PingRespondMessage(me));
			}
		}
		
	}

}
