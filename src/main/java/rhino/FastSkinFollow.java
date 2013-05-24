package rhino;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.time.Timer;
import org.openimaj.video.capture.VideoCapture;

/**
 * OpenIMAJ Hello world!
 *
 */
public class FastSkinFollow {

	private final static String SKIN_NONSKIN = "/org/openimaj/image/processing/face/detection/skin.png";

	public static void main(String[] args) throws Exception {

		int width = 160;
		int height = 120;
		if (args.length == 2) {
			width = Integer.parseInt(args[0]);
			height = Integer.parseInt(args[1]);
		}
		int MIN_PIXELS = (width / 15 * height / 15);

		VideoCapture cap = new VideoCapture(width, height);
		FImage skin = new FImage(cap.getWidth(), cap.getHeight());
		Pixel prior = new Pixel(cap.getWidth() / 2, cap.getHeight() / 2);
		while (cap.hasNextFrame()) {
			Timer timer = Timer.timer();
			int total = 1;
			Pixel average = prior.copy();
			MBFImage frame = cap.getNextFrame();
			float[][][] colours = getColours(frame);
			for (int y = 0; y < colours[0].length; y++) {
				for (int x = 0; x < colours[0][0].length; x++) {
					float r = colours[0][y][x] * 255f;
					float g = colours[1][y][x] * 255f;
					float b = colours[2][y][x] * 255f;
					float v = Math.max(Math.max(r, g), b);
					float s = v == 0 ? 0 : 255 * (v - Math.min(Math.min(r, g), b)) / v;
					float h = 0f;
					if (0 != s) {
						if (v == r) {
							h = 30f * (g - b) / s;
						} else if (v == g) {
							h = 60 + ((b - r) / s);
						} else {
							h = 120 + ((r - g) / s);
						}
						if (h < 0) {
							h += 360;
						}
					}
					float value = 0;

					if (v >= 15 && v <= 250) {
						if (h >= 3 && h <= 33) {
							value = 1f;
						}
					}

					skin.pixels[y][x] = value;
				}
			}
			DisplayUtilities.displayName(skin,"skin");
			System.out.println(String.format("Found %d blobs in %dms", total, timer.duration()));
		}
	}

	private static float[][][] getColours(MBFImage frame) {
		float[][][] ret = new float[3][][];
		ret[0] = frame.getBand(0).pixels;
		ret[1] = frame.getBand(1).pixels;
		ret[2] = frame.getBand(2).pixels;
		return ret;
	}
}
