package src;

import java.util.*;

public class Scout extends Process {
	ProcessId leader;
	ProcessId leaderResponderId;
	
	ProcessId[] acceptors;
	BallotNumber ballot_number;

	public Scout(Env env, ProcessId me, ProcessId leader, ProcessId leaderResponderId,
			ProcessId[] acceptors, BallotNumber ballot_number){
		this.env = env;
		this.me = me;
		this.acceptors = acceptors;
		this.leader = leader;
		this.leaderResponderId = leaderResponderId;
		this.ballot_number = ballot_number;
		env.addProc(me, this);
	}

	public void body(){
		P1aMessage m1 = new P1aMessage(me, ballot_number, leaderResponderId);
		Set<ProcessId> waitfor = new HashSet<ProcessId>();
		for (ProcessId a: acceptors) {
			sendMessage(a, m1);
			waitfor.add(a);
		}

		Set<PValue> pvalues = new HashSet<PValue>();
		while (2 * waitfor.size() >= acceptors.length) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof P1bMessage) {
				P1bMessage m = (P1bMessage) msg;

				//System.out.println(ballot_number.toString() + m.ballot_number.toString());
				
				
				int cmp = ballot_number.compareTo(m.ballot_number);
				if (cmp != 0) {
					sendMessage(leader, new PreemptedMessage(me, m.ballot_number, m.newLeader));
					return;
				}
				if (waitfor.contains(m.src)) {
					waitfor.remove(m.src);
					pvalues.addAll(m.accepted);
				}
			}
			else {
				System.err.println("Scout: unexpected msg");
			}
		}

		sendMessage(leader, new AdoptedMessage(me, ballot_number, pvalues));
	}
}
