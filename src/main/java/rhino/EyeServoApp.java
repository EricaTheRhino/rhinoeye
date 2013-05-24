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


public class EyeServoApp extends Application {
	
	public static class EyeDirection extends AppTypedResource<EyeServoApp>{
		public EyeDirection() {
		}
		@Put
		@Post
		public void direction(Representation r) {
			Form form = new Form(r);
			String direction = form.getFirstValue("dir");
			String time = form.getFirstValue("int");
			if(time==null){				
				this.app.servo.servoSet(Float.parseFloat(direction));
			}
			else{
				this.app.servo.servoSet(Float.parseFloat(direction),Long.parseLong(time));
			}
		}
		
		@Get("json")
		public Representation direction(){
			Map<Object,Object> mapRet = new HashMap<Object, Object>();
			mapRet.put("dir", this.app.servo.getDirection());
			JsonRepresentation ret = new JsonRepresentation(mapRet);
			return ret;
		}
	}
	
	private ServoControl servo;
	public EyeServoApp() {
		this.servo = ServoControl.getInstance();
	}
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/direction", EyeDirection.class);
		return router;
	}
}
