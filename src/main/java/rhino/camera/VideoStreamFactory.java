package rhino.camera;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openimaj.image.MBFImage;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.stream.BlockingDroppingBufferedStream;
import org.openimaj.util.stream.Stream;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

public class VideoStreamFactory {
	Logger logger = Logger.getLogger(VideoStreamFactory.class);
	private VideoCapture capture;
	private ArrayList<ArrayBlockingDroppingQueue<MBFImage>> listeners;
	private MBFImage cache;
	private static VideoStreamFactory instance;

	private VideoStreamFactory() throws VideoCaptureException {
		int width = Integer.parseInt(System.getProperty("eye.camera.width", "640"));
		int height = Integer.parseInt(System.getProperty("eye.camera.height", "480"));;
		capture = new VideoCapture(width, height);
		cache = new MBFImage(capture.getWidth(),capture.getHeight(),3);
		logger.debug(String.format("Video init: %dx%d",width,height));
		this.listeners = new ArrayList<ArrayBlockingDroppingQueue<MBFImage>>();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(true){
					logger.debug(String.format("Getting next frame"));
					cache.internalCopy(capture.getNextFrame());
					logger.debug(String.format("Offering frame to listeners"));
					synchronized (listeners) {
						for (ArrayBlockingDroppingQueue<MBFImage> queue : listeners) {
							queue.offer(cache);
						}
					}
				}
			}
		}).start();
	}
	
	public Stream<MBFImage> createStream() {
		synchronized (listeners) {			
			ArrayBlockingDroppingQueue<MBFImage> queue = new ArrayBlockingDroppingQueue<MBFImage>(1);
			listeners.add(queue);
			return new BlockingDroppingBufferedStream<MBFImage>(queue) {
				
			};
		}
	}
	
	public static synchronized VideoStreamFactory getInstance() throws VideoCaptureException{
		if(instance == null){
			instance=new VideoStreamFactory();
		}
		return instance;
	}
}
