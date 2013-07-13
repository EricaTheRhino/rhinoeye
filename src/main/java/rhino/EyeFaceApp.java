package rhino;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.function.Operation;
import org.openimaj.video.capture.VideoCaptureException;

import rhino.util.BrainInterface;
import rhino.util.RhinoPrefs;

public class EyeFaceApp extends EyeWatchingApp {

	public EyeFaceApp() throws VideoCaptureException {
		super();
		this.isActive = true;
	}

	private final class FaceOperation implements Operation<MBFImage> {
		private HaarCascadeDetector det;
		private ResizeProcessor resize;
		private int width;
		private int height;
		private long lastDet;

		public FaceOperation() {
			this.width = Integer.parseInt(System.getProperty("eye.face.width", "160"));
			this.height = Integer.parseInt(System.getProperty("eye.face.height", "120"));
			final int search = Integer.parseInt(System.getProperty("eye.face.search", "20"));
			this.resize = new ResizeProcessor(width, height);
			this.det = new HaarCascadeDetector(search);
			
		}

		@Override
		public void perform(MBFImage frame) {
			if (isActive) {
				if(System.currentTimeMillis() - this.lastDet < 5000)return;
				final float xRatio = frame.getWidth() / (float) width;
				final float yRatio = frame.getHeight() / (float) height;
				FImage grey = null;
				if (xRatio == 1 && yRatio == 1)
					grey = frame.flatten();
				else
					grey = frame.process(resize).flatten();
				final List<DetectedFace> rects = det.detectFaces(grey);
				final Map<Object, Object> ret = new HashMap<Object, Object>();
				final ArrayList<Map<Object, Object>> faces = new ArrayList<Map<Object, Object>>();
				ret.put("faces", faces);
				for (final DetectedFace detectedFace : rects) {
					final Map<Object, Object> face = new HashMap<Object, Object>();
					final Rectangle rec = detectedFace.getBounds();
					face.put("x", rec.x * xRatio);
					face.put("y", rec.y * yRatio);
					face.put("width", rec.width * xRatio);
					face.put("height", rec.height * yRatio);
					BrainInterface.postEvent("interaction." + RhinoPrefs.getString("eye.name", "righteye") + ".face","face", rec.toString());
					logger.info("Face found!");
				}
				this.lastDet = System.currentTimeMillis();
			}
		}
	}

	@Override
	public Operation<MBFImage> getOperation() {
		return new FaceOperation();
	}

}
