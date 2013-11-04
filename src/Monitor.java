package src;

public class Monitor extends Process {
	
	ProcessId newLeaderResponder;
	
	Monitor(Env env, ProcessId me, ProcessId newLeaderResponder) {
		this.env = env;
		this.me = me;
		this.newLeaderResponder = newLeaderResponder;
		env.addProc(me, this);
	}

	@Override
	void body() {
		sendMessage(newLeaderResponder, new PingRequestMessage(me));
		
		PaxosMessage msg = getNextMessage();

		if (msg instanceof PingRespondMessage) {
			return;
		}
	}

}
