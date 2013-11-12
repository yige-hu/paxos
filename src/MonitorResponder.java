package src;

public class MonitorResponder extends Process {
	
	ProcessId leader;

	MonitorResponder(Env env, ProcessId me, ProcessId leader) {
		this.env = env;
		this.me = me;
		this.leader = leader;
		env.addProc(me, this);
	}
	
	@Override
	void body() {
		System.out.println("Here I am: " + me);
		
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof PingRequestMessage) {
				
				PingRequestMessage m = (PingRequestMessage) msg;
				
				if (env.TEST_NETWORK_PARTITION) {
					//System.out.println("net partition: " + me + m.leader);
					if (leader.toString().equals("leader:0") || leader.toString().equals("leader:1"))
						if (m.leader.toString().equals("leader:2") || m.leader.toString().equals("leader:3") || m.leader.toString().equals("leader:4")) {
							System.out.println("net partition: " + leader + m.leader);
							continue;
						}
					if (leader.toString().equals("leader:2") || leader.toString().equals("leader:3") || leader.toString().equals("leader:4"))
						if (m.leader.toString().equals("leader:0") || m.leader.toString().equals("leader:1")) {
							System.out.println("net partition: " + leader + m.leader);
							continue;
						}
				}
				
				sendMessage(msg.src, new PingRespondMessage(me));
			}
		}
		
	}

}
