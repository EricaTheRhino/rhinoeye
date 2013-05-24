package rhino;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.time.Timer;
import org.openimaj.video.capture.VideoCapture;

/**
 * OpenIMAJ Hello world!
 *
 */
public class FrameDelta{
	public static void main(String[] args) throws Exception {

		int w = 320;
		int h = 240;
		if(args.length == 2){
			w = Integer.parseInt(args[0]);
			h = Integer.parseInt(args[1]);
		}
		VideoCapture e = new VideoCapture(w,h);
		System.out.println("Was given w,h: "+ e.getWidth() + "," + e.getHeight());
		// Hold a greyscale previous frame
		FImage prevGrey = e.getNextFrame().flatten();

		Pixel prior = new Pixel(e.getWidth()/2,e.getHeight()/2);
		for (;e.hasNextFrame();) {
			int total = 1;
			Pixel middle = new Pixel(prior.x,prior.y);
			Timer t = Timer.timer();
			// Get access to the current frame's pixels as arrays
			float[][][] rgbFrame = getRGB(e.getNextFrame());
			for (int y = 0; y < prevGrey.height; y++) {
				for (int x = 0; x < prevGrey.width; x++) {
					// Calculate new grey value and the delta to the old
					float frameGrey = (rgbFrame[0][y][x] + rgbFrame[1][y][x] + rgbFrame[2][y][x])/3f;
					float diff = Math.abs(frameGrey - prevGrey.pixels[y][x]);
					// Swap the previous pixel value
					prevGrey.pixels[y][x] = frameGrey;
					if( diff > 0.2f){
						middle.x += x;
						middle.y += y;
						total++;
					}
				}

			}
			middle.x /= total;
			middle.y /= total;
			if(middle.x == prior.x && middle.y == prior.y) continue;

			System.out.println(String.format("Took: %dms, Detected biggest center: %s",t.duration(),middle));
		}
	}

	private static float[][][] getRGB(MBFImage frame) {
		float[][][] rgb = new float[3][][];
		rgb[0] = frame.bands.get(0).pixels;
		rgb[1] = frame.bands.get(1).pixels;
		rgb[2] = frame.bands.get(2).pixels;
		return rgb;
	}
}
