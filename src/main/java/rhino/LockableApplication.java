package rhino;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.restlet.Application;

public abstract class LockableApplication extends Application{ 
	Lock lock = new ReentrantLock();
	void lock() {
		if(!lock.tryLock()){
			throw new RuntimeException("Problem getting lock");
		}
	}
	
	void unlock() {
		lock.unlock();
	}
}
