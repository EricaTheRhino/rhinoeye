package rhino;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.time.Timer;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.openimaj.video.translator.MBFImageToFImageVideoTranslator;

/**
 * OpenIMAJ Hello world!
 *
 */
public class FaceFinder implements Runnable {
	public static interface FaceListener{
		public void faceFound(DetectedFace face);
	}
	Map<String,FaceListener> finders = new HashMap<String,FaceListener>();
	Logger logging = Logger.getLogger(FaceFinder.class);
	private HaarCascadeDetector det;
	private MBFImageToFImageVideoTranslator videoCapture;
	
	public FaceFinder() throws VideoCaptureException {
		int w = Integer.parseInt(System.getProperty("eye.track.face.width", "160"));
		int h = Integer.parseInt(System.getProperty("eye.track.face.height", "120"));
		videoCapture = new MBFImageToFImageVideoTranslator(new VideoCapture(w, h));
		logging.debug("Was given w,h: "+ videoCapture.getWidth() + "," + videoCapture.getHeight());
		this.det = new HaarCascadeDetector(w/6);
		
	}
	
	public void addFaceListener(String id, FaceListener listener){
		synchronized (finders) {			
			this.finders.put(id, listener);
			if(finders.size() == 1){
				new Thread(this).start();
			}
		}
	}
	
	public void removeListener(String id){
		synchronized (finders) {			
			this.finders.remove(id);
		}
	}
	int nimg = 0;
	private DetectedFace detectFace() {
		Timer t = Timer.timer();
		FImage frame = videoCapture.getNextFrame();
		List<DetectedFace> faces = det.detectFaces(frame);
		logging.debug(String.format("Took: %dms, %d faces detected", t.duration(),faces.size()));
		if(faces.size() == 0 ){
			return null;
		}
		else{
			logging.debug("Face detected!");
			if(Logger.getRootLogger().getLevel().equals(Level.DEBUG)){
				new File("imgout").mkdirs();
				try {
					ImageUtilities.write(frame, new File(String.format("imgout/img_%d_faceat_%s.png",nimg,faces.get(0).getBounds())));
					nimg++;
					nimg = nimg % 10 == 0 ? 0 : nimg;
				} catch (IOException e) {
				}
			}
			return faces.get(0);
		}
	}
	
	public static void main(String[] args) throws Exception {
		FaceFinder follow = new FaceFinder();
		FaceListener listener = new FaceListener() {
			
			@Override
			public void faceFound(DetectedFace face) {
				Rectangle bounds = face.getBounds();
				System.out.println("Face detected: " + bounds);
			}
		};
		follow.addFaceListener("main", listener);
		Thread.sleep(2000);
		follow.removeListener("main");
		Thread.sleep(2000);
		follow.addFaceListener("main", listener);
		Thread.sleep(2000);
		follow.removeListener("main");
		
	}

	@Override
	public void run() {			
		while(true){
			synchronized (this.finders) {
				if(this.finders.size()==0){
					break;
				}
				DetectedFace detected = this.detectFace();
				if(detected!=null){					
					for (FaceListener listener: finders.values()) {
						listener.faceFound(detected);
					}
				}
			}
		}
	}

	public Rectangle getScreenDims() {
		return new Rectangle(0,0,this.videoCapture.getWidth(),this.videoCapture.getHeight());
	}
}
