package rhino;

import java.io.IOException;
import java.util.Arrays;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import org.apache.log4j.Logger;
import org.restlet.resource.Get;

import rhino.util.RhinoPrefs;

public class EyeLightControl implements PreferenceChangeListener{
	Logger logging = Logger.getLogger(EyeLightControl.class);
	
	static String I2C_COMMAND = "i2cset";

	public static EyeLightControl instance;
	public float currentBright;

	private EyeLightControl() {
		setBrightness(1f);
		RhinoPrefs.getPrefs().addPreferenceChangeListener(this);
	}
	private int EYE_MIN(){return RhinoPrefs.getInt("eye.light.min",0xff);}
	private int EYE_MAX(){return RhinoPrefs.getInt("eye.light.max",0xe6);}
	private int EYE_WAIT_TIME(){return RhinoPrefs.getInt("eye.light.wait",50) ;}
	private int DIFF(){return EYE_MIN() - EYE_MAX();}
	
	public static EyeLightControl getInstance(){
		if(instance==null){
			instance = new EyeLightControl();
		}
		return instance;
	}
	public void setBrightness(float bright) {
		if (bright > 1.0f)
			bright = 1f;
		if (bright < 0f)
			bright = 0f;
		this.currentBright = bright;
		logging.info("Setting bright: " + bright);
		try {
			i2cset(bright);
		} catch (Exception e) {
			return;
		}
	}

	public void open(long over) {
		float change = 1.f - this.currentBright;

		int steps = (int) (over / EYE_WAIT_TIME());
		float delta = change / steps;
		logging.debug("Overall change = " + change);
		logging.debug("Delta = " + delta);
		logging.debug("Nsteps = " + steps);
		while (this.currentBright < 1.0f) {
			this.currentBright += delta;
			setBrightness(this.currentBright);
			try {
				Thread.sleep(EYE_WAIT_TIME());
			} catch (InterruptedException e) {

			}
		}
	}

	public void close(long over) {
		float change = this.currentBright;
		int steps = (int) (over / EYE_WAIT_TIME());
		float delta = change / steps;
		logging.debug("Overall change = " + change);
		logging.debug("Delta = " + delta);
		logging.debug("Nsteps = " + steps);
		while (this.currentBright > 0f) {
			this.currentBright -= delta;
			setBrightness(this.currentBright);
			try {
				Thread.sleep(EYE_WAIT_TIME());
			} catch (InterruptedException e) {

			}
		}
	}

	public void blink(long over) {
		this.setBrightness(EYE_MAX());
		this.close(over / 2);
		this.open(over / 2);
	}

	private void i2cset(float bright) throws IOException, InterruptedException {
		int diffBright = (int) (DIFF() * bright);
		int newBright = EYE_MIN() - diffBright;
		String[] brightCommand = new String[] { I2C_COMMAND, "-y", "1", "0x2c",
				"0x00", "0x" + Integer.toHexString(newBright) };
		logging.info("Running command:\n" + Arrays.toString(brightCommand));
		Process proc = Runtime.getRuntime().exec(brightCommand);
		proc.waitFor();
	}

	@Get
	public void handleDef() {

	}

	

	public static void main(String[] args) throws Exception {
		EyeLightControl cont = new EyeLightControl();
		
		cont.blink(1000);
	}
	@Override
	public void preferenceChange(PreferenceChangeEvent evt) {
		if(evt.getKey().startsWith("eye.light"))
			setBrightness(currentBright);
	}
}
