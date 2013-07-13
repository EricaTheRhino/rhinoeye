package rhino;

import java.awt.image.BufferedImage;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.util.function.Operation;
import org.openimaj.video.capture.VideoCaptureException;

import rhino.util.BrainInterface;
import rhino.util.RhinoPrefs;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class EyeQRApp extends EyeWatchingApp {

	public EyeQRApp() throws VideoCaptureException {
		super();
		this.isActive = true;
	}

	private final class QRCodeOperation implements Operation<MBFImage> {
		private BufferedImage bimg;
		private QRCodeReader reader;

		public QRCodeOperation() {
			this.reader = new com.google.zxing.qrcode.QRCodeReader();
		}

		@Override
		public void perform(MBFImage frame) {
			if (isActive) {
				logger.debug("Searching for QR image");
				this.bimg = ImageUtilities.createBufferedImageForDisplay(frame, this.bimg);
				final LuminanceSource source = new BufferedImageLuminanceSource(this.bimg);
				final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
				Result res;
				try {
					res = this.reader.decode(bitmap);
					if (res.getText() != null)
					{
						logger.debug("Got QR text: " + res.getText());

						BrainInterface.postEvent("interaction." + RhinoPrefs.getString("eye.name", "righteye") + ".scan",
								"qr", res.getText());
					}
				} catch (final NotFoundException e) {
				} catch (final ChecksumException e) {
				} catch (final FormatException e) {
				}
			}
		}
	}

	@Override
	public Operation<MBFImage> getOperation() {
		return new QRCodeOperation();
	}

}
