package rhino;

import java.util.concurrent.locks.ReentrantLock;

import org.restlet.Application;
import org.restlet.resource.ServerResource;

public class AppTypedResource<T extends Application> extends ServerResource {
	protected T app;
	private static ReentrantLock lock = new ReentrantLock();
	@SuppressWarnings("unchecked")
	public AppTypedResource() {
		this.app = (T) this.getApplication();
	}
	
}