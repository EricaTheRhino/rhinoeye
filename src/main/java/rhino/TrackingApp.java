package rhino;

import org.apache.log4j.Logger;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.video.capture.VideoCaptureException;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Get;
import org.restlet.routing.Router;

import rhino.FaceFinder.FaceListener;

public class TrackingApp extends Application {
	static Logger logging = Logger.getLogger(TrackingApp.class);
	FaceFinder faceFinder;
	ServoControl cont;
	public static class TrackFace extends AppTypedResource<TrackingApp>{
		@Get
		public String represent() {
			String active = (String) this.getRequest().getAttributes().get("active");
			if(active.equals("0")){
				this.app.faceFinder.removeListener("faceTrack");
			}
			else{
				this.app.addFaceTracker();
			}
			return "";
		}
	}
	public TrackingApp() {
		try {
			faceFinder = new FaceFinder();
			cont = ServoControl.getInstance();
		} catch (VideoCaptureException e) {
		}
	}
	
	public void addFaceTracker() {
		faceFinder.addFaceListener("faceTrack",new FaceListener(){
			Rectangle screenDims = faceFinder.getScreenDims();
			@Override
			public void faceFound(DetectedFace face) {
				float midx = screenDims.width/2;
				Point2d detMid = face.getBounds().calculateCentroid();
				float move = ((midx - detMid.getX())/midx)/2f;
				logging.debug("Face detected! Moving servo: " + move);
				cont.servoDelta(move);
			}
		});
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/face/{active}", TrackFace.class);
		return router;
	}
}
