package src;

public class Monitor extends Process {
	
	ProcessId leader;
	ProcessId newLeaderResponder;
	
	Monitor(Env env, ProcessId me, ProcessId leader, ProcessId newLeaderResponder) {
		this.env = env;
		this.me = me;
		this.leader = leader;
		this.newLeaderResponder = newLeaderResponder;
		env.addProc(me, this);
	}

	@Override
	void body() {
		sendMessage(newLeaderResponder, new PingRequestMessage(me, leader));
		
		PaxosMessage msg = getNextMessage();

		if (msg instanceof PingRespondMessage) {
			return;
		}
	}

}
