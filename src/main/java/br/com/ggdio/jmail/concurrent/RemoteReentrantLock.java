package br.com.ggdio.jmail.concurrent;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Remote reentrant lock
 * 
 * @author Guilherme Dio
 *
 */
public class RemoteReentrantLock extends ReentrantLock {

	private static final long serialVersionUID = -5080316389960203759L;
	
	private final Locking locking;
	private final String namespace;
	
	public RemoteReentrantLock(Locking locking, String namespace) {
		super();
		this.locking = locking;
		this.namespace = "remotelock:" + namespace;
	}

	@Override
	public boolean tryLock() {
		if(locking.lock(namespace)) {
			return super.tryLock();
		}
		
		return false;
	}
	
	@Override
	public void unlock() {
		locking.unlock(namespace);
		super.unlock();
	}

}