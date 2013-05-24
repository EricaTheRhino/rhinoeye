package rhino;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.connectedcomponent.ConnectedComponentLabeler;
import org.openimaj.image.model.pixel.HistogramPixelModel;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.time.Timer;
import org.openimaj.video.capture.VideoCapture;



/**
 * OpenIMAJ Hello world!
 *
 */
public class SkinFollow {

	private final static String SKIN_NONSKIN = "/org/openimaj/image/processing/face/detection/skin.png";
	private static ServoControl cont;
	public static void main(String[] args) throws Exception {
		cont = ServoControl.getInstance();
		int w = 160;
		int h = 120;
		if(args.length == 2){
			w = Integer.parseInt(args[0]);
			h = Integer.parseInt(args[1]);
		}
		int MIN_PIXELS = (w/15 * h/15);

		HistogramPixelModel skinModel = new HistogramPixelModel(16, 6);
		final MBFImage rgb = ImageUtilities.readMBF(SkinFollow.class.getResourceAsStream(SKIN_NONSKIN));
		skinModel.learnModel(Transforms.RGB_TO_HS(rgb));

		VideoCapture cap = new VideoCapture(w, h);
		ConnectedComponentLabeler lab = new ConnectedComponentLabeler(ConnectedComponent.ConnectMode.CONNECT_4);
		Pixel prior = new Pixel(w/2,h/2);
		while(cap.hasNextFrame()){
			Timer timer = Timer.timer();
			int total = 1;
			Pixel average = prior.copy();
			MBFImage frame = cap.getNextFrame();
			FImage skin = skinModel.predict(Transforms.RGB_TO_HS(frame));
			skin.threshold(0.3f);
			lab.analyseImage(skin);
			for (ConnectedComponent cmp: lab.getComponents()) {
				if(cmp.pixels.size() <MIN_PIXELS) continue;
				Pixel calculateCentroidPixel = cmp.calculateCentroidPixel();
				average.x += calculateCentroidPixel.x;
				average.y += calculateCentroidPixel.y;
				total++;
			}
			average.x/=total;
			average.y/=total;

			if(prior.x - average.x < 0 ){
				cont.servoDelta(- cont.PWMdelta);
			}
			else{
				cont.servoDelta(+ cont.PWMdelta);
			}
			if(total!=1)
				System.out.println(String.format("Found %d blobs in %dms",total,timer.duration()));
		}
	}
}
