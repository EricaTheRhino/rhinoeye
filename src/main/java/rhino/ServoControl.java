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
	int PWMdelta=5;
	PrintWriter servoOut;
	String servoID = "0";
	boolean servoActive=false;
	private long lastServoCommand = 0;
	private float pwmProp;
	private int controlToken;
	private ServoControl(){
		initServoOut();
		reinit();
		RhinoPrefs.getPrefs().addPreferenceChangeListener(this);
	}
	
	private void initServoOut() {
		// If the file does not exist deactivate the servo
		if(!new File(devLocation).exists()){
			servoActive = false;
			servoOut = null;
		}
		else{			
			// If the servo is not currently active, try to activate it
			if(!servoActive){			
				try {
					servoOut = new PrintWriter(new FileOutputStream(devLocation));
					servoActive = true;
				} catch (FileNotFoundException e) {
					servoActive=false;
				}
			}
		}
	}

	public int SERVO_MIN(){return RhinoPrefs.getInt("eye.servo.min",40);}
	public int SERVO_MAX(){return RhinoPrefs.getInt("eye.servo.max",220);}
	public long SERVO_WAIT_TIME(){return RhinoPrefs.getLong("eye.servo.wait",50l);}
	
	public synchronized static ServoControl getInstance(){
		if(servoInstance == null) servoInstance = new ServoControl();
		return servoInstance;
	}
	
	public synchronized int requestControlToken(){
		return ++this.controlToken;
	}
	public synchronized boolean confirmControl(int token){
		return this.controlToken == token;
	}
	public void servoSet(float pwmProp){
		if(pwmProp>1f)pwmProp = 1f;
		if(pwmProp<0f)pwmProp=0f;
		this.pwmProp = pwmProp;
		servoSet(requestControlToken(),(int)(SERVO_MIN() + DELTA() * pwmProp));
	}
	private void servoSet(int controlToken,float pwmProp){
		if(pwmProp>1f)pwmProp = 1f;
		if(pwmProp<0f)pwmProp=0f;
		this.pwmProp = pwmProp;
		servoSet(controlToken,(int)(SERVO_MIN() + DELTA() * pwmProp));
	}
	public void servoSet(float pwmProp,long interval){
		int token = this.requestControlToken();
		if(pwmProp>1f)pwmProp=1f;
		if(pwmProp<0f)pwmProp=0f;
		logging.info("Current prop is: " + this.pwmProp);
		logging.info("Target prop is: " + pwmProp);
		float targetDelta = pwmProp - this.pwmProp;
		long steps = interval/SERVO_WAIT_TIME();
		logging.info("N steps: " + steps);
		float incr = targetDelta / steps;
		logging.info("Increment: " + incr);
		if(incr == 0) return;
		for (int i = 0; i < steps; i++) {
			this.servoSet(token,this.pwmProp + incr);
		}
		this.pwmProp = pwmProp;
		this.servoSet(token,this.pwmProp);
		
	}
	private void servoSet(int controlToken, int newPWM) {
		if(!confirmControl(controlToken))return;
		initServoOut();
		logging.info("Attemping to set PWM to: " + newPWM);
		newPWM = Math.max(Math.min(newPWM, SERVO_MAX()),SERVO_MIN());
		if(servoActive){			
			if(System.currentTimeMillis() - lastServoCommand <= SERVO_WAIT_TIME()) return;
			String servoMessage = String.format("%s=%s",servoID ,newPWM);
			logging.debug("Sending servo message: " + servoMessage);
			servoOut.println(servoMessage);
			servoOut.flush();
			this.lastServoCommand  = System.currentTimeMillis();
		}
		else{
			logging.error("NO SERVO ACTIVE");
			logging.error("setting fake pwmPROP to: " + this.pwmProp + " with PWM: " + newPWM);
		}
		try {
			
			logging.info("Sleeping: " +SERVO_WAIT_TIME());
			Thread.sleep(SERVO_WAIT_TIME());
		} catch (InterruptedException e) {
		}
	}
	
	public void servoDelta(float delta) {
		this.pwmProp += delta;
		int newPWM = (int) (SERVO_MIN() + DELTA() * this.pwmProp);
		this.servoSet(requestControlToken(),newPWM);
	}
	public void reinit() {
		this.pwmProp = 0.5f;
		servoSet(SERVO_MIN() + (DELTA()/2));
	}

	private int DELTA() {
		return SERVO_MAX() - SERVO_MIN();
	}

	public float getDirection() {
		return this.pwmProp;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent evt) {
		if(evt.getKey().startsWith("eye.servo"))
			this.servoSet(this.pwmProp);
	}
}
