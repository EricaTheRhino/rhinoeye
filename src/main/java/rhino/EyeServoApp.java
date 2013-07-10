package rhino;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.routing.Router;

public class EyeServoApp extends LockableApplication {

	public static class EyeDirection extends AppTypedResource<EyeServoApp> {
		public EyeDirection() {
		}

		@Put
		@Post
		public void direction(Representation r) {
			this.app.lock();
			final Form form = new Form(r);
			final String direction = form.getFirstValue("dir");
			final String time = form.getFirstValue("int");
			if (time == null) {
				this.app.servo.servoSet(Float.parseFloat(direction));
			}
			else {
				this.app.servo.servoSet(Float.parseFloat(direction), Long.parseLong(time));
			}
			this.app.unlock();
		}

		@Get("json")
		public Representation direction() {
			final Map<Object, Object> mapRet = new HashMap<Object, Object>();
			mapRet.put("dir", this.app.servo.getDirection());
			final JsonRepresentation ret = new JsonRepresentation(mapRet);
			return ret;
		}
	}

	private ServoControl servo;

	public EyeServoApp() {
		this.servo = ServoControl.getInstance();

		this.servo.servoSet(0, 1000);
		this.servo.servoSet(1, 1000);
		this.servo.servoSet(0.5f, 1000);
	}

	@Override
	public Restlet createInboundRoot() {
		final Router router = new Router(getContext());
		router.attach("/direction", EyeDirection.class);
		return router;
	}
}
