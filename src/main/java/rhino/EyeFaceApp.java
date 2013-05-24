package rhino;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.function.Operation;
import org.openimaj.video.capture.VideoCaptureException;
import org.restlet.Restlet;
import org.restlet.resource.ClientResource;

import rhino.util.RestletUtil;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class EyeFaceApp extends EyeWatchingApp {
	
	public EyeFaceApp() throws VideoCaptureException {
		super();
	}
	private final class FaceOperation implements Operation<MBFImage> {
		private HaarCascadeDetector det;
		private ResizeProcessor resize;
		private int width;
		private int height;
		public FaceOperation() {
			this.width = Integer.parseInt(System.getProperty("eye.face.width", "160"));
			this.height = Integer.parseInt(System.getProperty("eye.face.height", "120"));
			int search = Integer.parseInt(System.getProperty("eye.face.search", "20"));
			this.resize = new ResizeProcessor(width, height);
			this.det = new HaarCascadeDetector(search);
		}
		@Override
		public void perform(MBFImage frame) {
			if(isActive){
				float xRatio = frame.getWidth() / (float)width;
				float yRatio = frame.getHeight() / (float)height;
				FImage grey = null;
				if(xRatio == 1 && yRatio == 1)
					grey = frame.flatten();
				else
					grey = frame.process(resize).flatten();
				List<DetectedFace> rects = det.detectFaces(grey);
				Map<Object,Object> ret = new HashMap<Object, Object>();
				ArrayList<Map<Object, Object>> faces = new ArrayList<Map<Object,Object>>();
				ret.put("faces", faces);
				for (DetectedFace detectedFace : rects) {
					Map<Object,Object> face = new HashMap<Object,Object>();
					Rectangle rec = detectedFace.getBounds();
					face.put("x", rec.x * xRatio);
					face.put("y", rec.y * yRatio);
					face.put("width", rec.width * xRatio);
					face.put("height", rec.height * yRatio);
					logger.debug("Detected face: " +face);
				}
			}
		}
	}
	@Override
	public Operation<MBFImage> getOperation() {
		return new FaceOperation();
	}

}
