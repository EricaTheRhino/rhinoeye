package rhino;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.time.Timer;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.processing.motion.GridMotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimatorAlgorithm;
import org.openimaj.video.translator.MBFImageToFImageVideoTranslator;

/**
 * OpenIMAJ Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws Exception {

		int w = 320;
		int h = 240;
		if(args.length == 2){
			w = Integer.parseInt(args[0]);
			h = Integer.parseInt(args[1]);
		}
		final VideoCapture cap = new VideoCapture(w, h);
		MotionEstimator e = new GridMotionEstimator(new MBFImageToFImageVideoTranslator(cap),
				new MotionEstimatorAlgorithm.TEMPLATE_MATCH(),
				30, 30,true);
		System.out.println("Was given w,h: "+ e.getWidth() + "," + e.getHeight());
		for (Iterator<FImage> iterator = e.iterator(); iterator.hasNext();) {
			Timer t = Timer.timer();
			FImage frame = iterator.next();
			Point2dImpl meanMotion = new Point2dImpl(0,0);
			Map<Point2d, Point2d> analysis = e.getMotionVectors();
			for (Entry<Point2d, Point2d> line : analysis.entrySet()) {
				Point2d to = line.getKey().copy();
				to.translate(line.getValue());
				frame.drawLine(line.getKey(), to, 1f);
				meanMotion.x += line.getValue().getX();
			}
			DisplayUtilities.displayName(frame, "frame");
			meanMotion.x /= analysis.size();
			meanMotion.y /= analysis.size();
			System.out.println("took: " + t.duration() + "ms, mean motion x: " + meanMotion.x);
			// ImageUtilities.write(frame, new File("test" + i + ".png"));

			try {
				Thread.sleep(500);
			} catch (final InterruptedException er) {
				er.printStackTrace();
			}
		}
	}
}
