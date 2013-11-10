package src;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientRequester extends Process {

	Object syncObj;
	ProcessId[] replicas;
	ProcessId client;
	List<ProcessId> responded_replicas;
	CountDownLatch latch;
	CountDownLatch latch_finish;
	
	ClientRequester(Env env, ProcessId me, ProcessId client, ProcessId[] replicas, Object syncObj, 
			List<ProcessId> responded_replicas, CountDownLatch latch, CountDownLatch latch_finish) {
		this.env = env;
		this.me = me;
		this.client = client;
		this.replicas = replicas;
		this.syncObj = syncObj;
		this.responded_replicas = responded_replicas;
		this.latch = latch;
		this.latch_finish = latch_finish;
		
		env.addProc(me, this);
	}
	
	void request(Command command){
		for (ProcessId ldr: replicas) {
			env.sendMessage(ldr,
				new RequestMessage(me, command));
		}
	}
	
	void requestROC(Command command){
		while (true) {
			for (ProcessId ldr : responded_replicas) {
				env.sendMessage(ldr, new ROCRequestMessage(me, command));
			}
			
			try {
				if (latch.await(500, TimeUnit.MICROSECONDS)) {
//					System.out.println("await_finished");
					latch_finish.countDown();
					return;				
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
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
