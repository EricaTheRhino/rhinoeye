package rhino;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.util.stream.Stream;
import org.openimaj.video.capture.VideoCaptureException;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.routing.Router;

import rhino.camera.VideoStreamFactory;

public class EyeImageApp extends Application  {
	static Logger logger = Logger.getLogger(EyeImageApp.class);
	public static class CurrentImage extends AppTypedResource<EyeImageApp>{
		@Get
		public Representation image() throws IOException{
			logger.debug("Asking for next image from stream");
			MBFImage image = this.app.videoStream.next();
			logger.debug("Rendering image...");
			ByteArrayOutputStream imageOutput = new ByteArrayOutputStream();
			ImageUtilities.write(image, "jpeg", imageOutput);
			
			InputRepresentation ret = new InputRepresentation(
					new ByteArrayInputStream(imageOutput.toByteArray()),
					MediaType.IMAGE_JPEG
			);
			return ret;
		}
	}
	private Stream<MBFImage> videoStream;

	public EyeImageApp() throws VideoCaptureException {
		this.videoStream = VideoStreamFactory.getInstance().createStream();
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/current", CurrentImage.class);
		return router;
	}
}
