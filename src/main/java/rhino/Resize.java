package rhino;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.time.Timer;
import org.openimaj.video.capture.VideoCapture;

/**
 * OpenIMAJ Hello world!
 *
 */
public class Resize{
	public static void main(String[] args) throws Exception {

		int w = 320;
		int h = 240;
		if(args.length == 2){
			w = Integer.parseInt(args[0]);
			h = Integer.parseInt(args[1]);
		}
		final VideoCapture e = new VideoCapture(w, h);
		System.out.println("Was given w,h: "+ e.getWidth() + "," + e.getHeight());
		for (;e.hasNextFrame();) {
			Timer t = Timer.timer();
			MBFImage frame = e.getNextFrame();
			ResizeProcessor.halfSize(frame);
			System.out.println(String.format("Took: %dms",t.duration()));
		}
	}
}
