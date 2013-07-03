package rhino;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.routing.Router;


public class EyeLightApp extends LockableApplication {
	public static class EyeBlink extends AppTypedResource<EyeLightApp> {
		@Post
		@Put
		public void blink(Representation rep) {
			this.app.lock();
			Form form = new Form(rep);
			this.app.eyecontrol.blink(
				Long.parseLong(form.getFirstValue("time"))
			);
			this.app.unlock();
		}
		
	}

	public static class EyeLevel extends AppTypedResource<EyeLightApp> {
		@Put
		@Post
		public void level(Representation rep) {
			this.app.lock();
			Form form = new Form(rep);
			this.app.eyecontrol.setBrightness(
				Float.parseFloat(form.getFirstValue("level"))
			);
			this.app.unlock();
		}
		
		@Get("json")
		public Representation level(){
			Map<Object,Object> retMap = new HashMap<Object, Object>();
			retMap.put("level", this.app.eyecontrol.currentBright);
			JsonRepresentation rep = new JsonRepresentation(retMap );
			return rep;
		}
	}
	EyeLightControl eyecontrol;

	public EyeLightApp() {
		this.eyecontrol = EyeLightControl.getInstance();
		this.eyecontrol.setBrightness(0);
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/blink", EyeBlink.class);
		router.attach("/level", EyeLevel.class);
		return router;
	}

}