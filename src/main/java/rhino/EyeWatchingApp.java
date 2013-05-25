package rhino;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.image.MBFImage;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.Stream;
import org.openimaj.video.capture.VideoCaptureException;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.routing.Router;

import rhino.camera.VideoStreamFactory;

public abstract class EyeWatchingApp extends Application {
	public transient boolean isActive = false;
	Logger logger = Logger.getLogger(EyeWatchingApp.class);

	public static class QRState extends AppTypedResource<EyeWatchingApp> {

		@Put
		@Post
		public void state(Representation rep) {
			final Form form = new Form(rep);
			final int run = Integer.parseInt(form.getFirstValue("run"));
			if (run == 0) {
				this.app.isActive = false;
			}
			else {
				this.app.isActive = true;
			}
		}

		@Get("json")
		public Representation state() throws IOException {
			final Map<Object, Object> state = new HashMap<Object, Object>();
			state.put("run", this.app.isActive);
			final JsonRepresentation jsonRep = new JsonRepresentation(state);
			return jsonRep;
		}
	}

	private Stream<MBFImage> videoStream;

	public EyeWatchingApp() throws VideoCaptureException {
		this.videoStream = VideoStreamFactory.getInstance().createStream();
		new Thread(new Runnable() {

			@Override
			public void run() {
				videoStream.forEach(getOperation());
			}

		}).start();
	}

	public abstract Operation<MBFImage> getOperation();

	@Override
	public Restlet createInboundRoot() {
		final Router router = new Router(getContext());
		router.attach("/state", QRState.class);
		return router;
	}
}
