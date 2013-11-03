package src;

import java.util.*;

public class Leader extends Process {
	ProcessId[] acceptors;
	ProcessId[] replicas;
	BallotNumber ballot_number;
	boolean active = false;
	Map<Integer, Command> proposals = new HashMap<Integer, Command>();
	
	long time_out = 10;
	float increase_factor = (float) 1.1;
	int decrease_factor = 1;
	int monitorNum = 0;

	public Leader(Env env, ProcessId me, ProcessId[] acceptors,
										ProcessId[] replicas){
		this.env = env;
		this.me = me;
		ballot_number = new BallotNumber(0, me);
		this.acceptors = acceptors;
		this.replicas = replicas;
		env.addProc(me, this);
	}

	public void body(){
		System.out.println("Here I am: " + me);

		new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
			me, acceptors, ballot_number);
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof ProposeMessage) {
				ProposeMessage m = (ProposeMessage) msg;
				if (!proposals.containsKey(m.slot_number)) {
					proposals.put(m.slot_number, m.command);
					if (active) {
						new Commander(env,
							new ProcessId("commander:" + me + ":" + ballot_number + ":" + m.slot_number),
							me, acceptors, replicas, ballot_number, m.slot_number, m.command);
					}
				}
			}
















			else if (msg instanceof AdoptedMessage) {
				AdoptedMessage m = (AdoptedMessage) msg;

				if (ballot_number.equals(m.ballot_number)) {
					Map<Integer, BallotNumber> max = new HashMap<Integer, BallotNumber>();
					for (PValue pv : m.accepted) {
						BallotNumber bn = max.get(pv.slot_number);
						if (bn == null || bn.compareTo(pv.ballot_number) < 0) {
							max.put(pv.slot_number, pv.ballot_number);
							proposals.put(pv.slot_number, pv.command);
						}
					}

					for (int sn : proposals.keySet()) {
						new Commander(env,
							new ProcessId("commander:" + me + ":" + ballot_number + ":" + sn),
							me, acceptors, replicas, ballot_number, sn, proposals.get(sn));
					}
					active = true;
					
					// adjust time-out for the next ballot
					if (time_out > decrease_factor) {
						time_out -= decrease_factor;
					}
				}
			}

			else if (msg instanceof PreemptedMessage) {
				PreemptedMessage m = (PreemptedMessage) msg;
				if (ballot_number.compareTo(m.ballot_number) < 0) {
					
					active = false;
					
					// failure detector:
					for (;;) {
						System.out.println("Monitor");
						
						// TODO
						monitorNum ++;
						long start_time = System.currentTimeMillis();
						Monitor monitor = new Monitor(env, new ProcessId("monitor:" + monitorNum + me), m.newLeader);
						try {
							monitor.join(time_out);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						long duration = System.currentTimeMillis() - start_time;
						
						if (duration >= time_out) {				
							break;
						}
						
						try {
							sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					// adjust time-out for the next ballot
					time_out *= increase_factor;
					
					ballot_number = new BallotNumber(m.ballot_number.round + 1, me);
					new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
						me, acceptors, ballot_number);
				}
			}
			
			// failure detector
			else if (msg instanceof PingRequestMessage) {
				PingRequestMessage m = (PingRequestMessage) msg;
				sendMessage(msg.src, new PingRespondMessage(me));				
			}

			else {
				System.err.println("Leader: unknown msg type");
			}
		}
	}
}
