package rhino;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.saliency.AchantaSaliency;
import org.openimaj.image.saliency.SaliencyMapGenerator;
import org.openimaj.time.Timer;
import org.openimaj.video.capture.VideoCapture;

/**
 * OpenIMAJ Hello world!
 *
 */
public class Saliency{
	public static void main(String[] args) throws Exception {

		int w = 320;
		int h = 240;
		if(args.length == 2){
			w = Integer.parseInt(args[0]);
			h = Integer.parseInt(args[1]);
		}
		final VideoCapture e = new VideoCapture(w, h);
		System.out.println("Was given w,h: "+ e.getWidth() + "," + e.getHeight());
		SaliencyMapGenerator<MBFImage> gen = new AchantaSaliency();
		for (;e.hasNextFrame();) {
			Timer t = Timer.timer();
			MBFImage frame = e.getNextFrame();
			gen.analyseImage(frame);
			FImage smap = gen.getSaliencyMap();
//			DisplayUtilities.displayName(frame, "img");
//			DisplayUtilities.displayName(smap, "saliency");
			FValuePixel maxSaliency = smap.maxPixel();
			System.out.println(String.format("Took: %dms, %d,%d most salient point with saliency: %2.2f", t.duration(),maxSaliency.x,maxSaliency.y,maxSaliency.value));
		}
	}
}
