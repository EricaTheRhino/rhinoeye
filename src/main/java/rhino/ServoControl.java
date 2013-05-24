package rhino;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import org.apache.log4j.Logger;

import rhino.util.RhinoPrefs;

public class ServoControl implements PreferenceChangeListener{
	private static ServoControl servoInstance;
	Logger logging = Logger.getLogger(ServoControl.class);
//	int SERVO_MIN=40;
//	int SERVO_MAX=220;
//	long SERVO_WAIT_TIME=50;
	final String devLocation = "/dev/servoblaster";
	int currentPWM;
	int PWMdelta=5;
	PrintWriter servoOut;
	String servoID = "0";
	boolean servoActive=true;
	private long lastServoCommand = 0;
	private float pwmProp;
	private ServoControl(){
		if(!new File(devLocation).exists()){
			servoActive=false;
		}
		try {
			servoOut = new PrintWriter(new FileOutputStream(devLocation));
		} catch (FileNotFoundException e) {

		}
		reinit();
		RhinoPrefs.getPrefs().addPreferenceChangeListener(this);
	}
	
	public int SERVO_MIN(){return RhinoPrefs.getInt("eye.servo.min",40);}
	public int SERVO_MAX(){return RhinoPrefs.getInt("eye.servo.max",220);}
	public long SERVO_WAIT_TIME(){return RhinoPrefs.getLong("eye.servo.wait",50l);}
	
	public synchronized static ServoControl getInstance(){
		if(servoInstance == null) servoInstance = new ServoControl();
		return servoInstance;
	}
	public void servoSet(float pwmProp){
		if(pwmProp>1f)pwmProp = 1f;
		if(pwmProp<0f)pwmProp=0f;
		servoSet((int)(SERVO_MIN() + DELTA() * pwmProp));
	}
	public void servoSet(float pwmProp,long interval){
		if(pwmProp>1f)pwmProp = 1f;
		if(pwmProp<0f)pwmProp=0f;
		logging.info("Current prop is: " + this.pwmProp);
		logging.info("Target prop is: " + pwmProp);
		long steps = interval/SERVO_WAIT_TIME();
		logging.info("N steps: " + steps);
		int incr = (int) (( (SERVO_MIN() + pwmProp * DELTA())- this.currentPWM )/steps);
		logging.info("Increment: " + incr);
		// Min move is 1
		if(incr == 0)
			if(pwmProp > this.pwmProp) 
				incr = -1;
			else if (pwmProp < this.pwmProp)
				incr = 1;
			else
			{
				incr=0;
				steps=0;
			}
		
		for (int i = 0; i < steps; i++) {
			this.servoSet(this.currentPWM + incr);
		}
		
	}
	public void servoSet(int newPWM) {
		logging.info("Attemping to set PWM to: " + newPWM);
		newPWM = Math.max(Math.min(newPWM, SERVO_MAX()),SERVO_MIN());
		currentPWM = newPWM;
		this.pwmProp = getDirection();
		if(servoActive){			
			if(System.currentTimeMillis() - lastServoCommand <= SERVO_WAIT_TIME()) return;
			String servoMessage = String.format("%s=%s",servoID ,newPWM);
			logging.debug("Sending servo message: " + servoMessage);
			servoOut.println(servoMessage);
			servoOut.flush();
			this.lastServoCommand  = System.currentTimeMillis();
		}
		try {
			logging.info("Sleeping: " +SERVO_WAIT_TIME());
			Thread.sleep(SERVO_WAIT_TIME());
		} catch (InterruptedException e) {
		}
	}
	public void servoDelta(int delta) {
		servoSet(currentPWM + delta);
	}
	public void servoDelta(float delta) {
		servoSet(currentPWM + (int)(delta * DELTA()));
	}
	public void reinit() {
		servoSet(SERVO_MIN() + (DELTA()/2));
	}

	private int DELTA() {
		return SERVO_MAX() - SERVO_MIN();
	}

	public float getDirection() {
		return (this.currentPWM-SERVO_MIN()) / (float)DELTA();
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent evt) {
		if(evt.getKey().startsWith("eye.servo"))
			this.servoSet(this.pwmProp);
	}
}
