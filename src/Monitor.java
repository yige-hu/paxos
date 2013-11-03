package src;

public class Monitor extends Process {
	
	ProcessId newLeader;
	
	Monitor(Env env, ProcessId me, ProcessId newLeader) {
		this.env = env;
		this.me = me;
		this.newLeader = newLeader;
		env.addProc(me, this);
	}

	@Override
	void body() {
		sendMessage(newLeader, new PingRequestMessage(me));
		
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof PingRespondMessage) {
				return;
			}
		}
	}

}
