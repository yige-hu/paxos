package src;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ClientRequester extends Process {

	Object syncObj;
	ProcessId[] replicas;
	ProcessId client;
	List<ProcessId> responded_replicas;
	Condition roc_syncObj;
	Lock lock;
	
	ClientRequester(Env env, ProcessId me, ProcessId client, ProcessId[] replicas, Object syncObj, 
			List<ProcessId> responded_replicas, Lock lock, Condition roc_syncObj) {
		this.env = env;
		this.me = me;
		this.client = client;
		this.replicas = replicas;
		this.syncObj = syncObj;
		this.responded_replicas = responded_replicas;
		this.lock = lock;
		this.roc_syncObj = roc_syncObj;
		
		env.addProc(me, this);
	}
	
	void request(Command command){
		for (ProcessId ldr: replicas) {
			env.sendMessage(ldr,
				new RequestMessage(me, command));
		}
	}
	
	void requestROC(Command command){
		//for (;;) {
			for (ProcessId ldr : responded_replicas) {
				env.sendMessage(ldr, new ROCRequestMessage(me, command));
			}
			lock.lock();
			try {
				if (roc_syncObj.await(500, TimeUnit.MICROSECONDS)) {
					System.out.println("await");
					return;
				}
				//System.out.println("await time-out");
			} catch (InterruptedException e) { 
			} finally {
				lock.unlock();
			}

		//}
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
