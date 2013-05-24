package rhino;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.io.FileUtils;
import org.openimaj.knn.approximate.FloatNearestNeighboursKDTree;
import org.openimaj.time.Timer;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.IntFloatPair;
import org.openimaj.video.capture.VideoCapture;


/**
 * OpenIMAJ Hello world!
 *
 */
public class KNNSkinFollow {
	private final static String SKIN_NONSKIN = "/org/openimaj/Skin_NonSkin.txt";
	public static void main(String[] args) throws Exception {

		int w = 320;
		int h = 240;
		if(args.length == 2){
			w = Integer.parseInt(args[0]);
			h = Integer.parseInt(args[1]);
		}
		String[] lines = FileUtils.readlines(KNNSkinFollow.class.getResourceAsStream(SKIN_NONSKIN));
		FloatNearestNeighboursKDTree.Factory fKdTreeFactory = new FloatNearestNeighboursKDTree.Factory();
		ArrayList<IndependentPair<Integer, FloatFV>> points = new ArrayList<IndependentPair<Integer, FloatFV>>();
		float[][] data = new float[lines.length][];
		for (int j = 0; j < lines.length; j++) {
			String line = lines[j];
			String[] parts = line.split("\t");
			FloatFV colour = new FloatFV(3);
			// Colour held as bgr
			colour.values[0] = Integer.parseInt(parts[2])/255f;
			colour.values[1] = Integer.parseInt(parts[1])/255f;
			colour.values[2] = Integer.parseInt(parts[0])/255f;
			IndependentPair<Integer, FloatFV> classColour = IndependentPair.pair(Integer.parseInt(parts[3]), colour);
			points.add(classColour);
			data[j] = colour.values;
		}
		System.out.println("Creating KDTree");
		FloatNearestNeighboursKDTree skinNN = fKdTreeFactory.create(data);
		final VideoCapture e = new VideoCapture(w, h);
		System.out.println("Was given w,h: "+ e.getWidth() + "," + e.getHeight());
		FImage skin = new FImage(e.getWidth(),e.getHeight());
		for (;e.hasNextFrame();) {
			Timer t = Timer.timer();
			MBFImage frame = e.getNextFrame();
			DisplayUtilities.displayName(frame, "normal");
			float[] colours = new float[3];
			for (int y = 0; y < skin.height; y++) {
				for (int x = 0; x < skin.width; x++) {
					Float[] pixe = frame.getPixel(x, y);
					colours[0] = pixe[0];
					colours[1] = pixe[1];
					colours[2] = pixe[2];
					List<IntFloatPair> nn = skinNN.searchKNN(colours , 10);
					int skinCount = 0;
					for (IntFloatPair skinclass : nn) {
						if(points.get(skinclass.first).firstObject() == 1)
							skinCount++;
					}
					if(skinCount > nn.size()/2){
						skin.pixels[y][x] = 1;
					}

				}
			}
			DisplayUtilities.displayName(skin, "skin");
			skin.fill(0f);
			System.out.println(String.format("Took: %dms, %s faces detected", t.duration(),"some"));
		}
	}
}
