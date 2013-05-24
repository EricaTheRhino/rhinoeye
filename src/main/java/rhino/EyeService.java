package rhino;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.restlet.Component;
import org.restlet.data.Protocol;

import rhino.util.RhinoPrefs;


public class EyeService extends Component{
	static {
		if(System.getProperty("os.name").toLowerCase().contains("mac")){
			
			ConsoleAppender console = new ConsoleAppender(); // create appender
			// configure the appender
			String PATTERN = "%d [%p|%c|%C{1}] %m%n";
			console.setLayout(new PatternLayout(PATTERN));
			console.setThreshold(Level.INFO);
			console.activateOptions();
			// add appender to any Logger (here is root)
			Logger.getRootLogger().addAppender(console);
		}
	}
	public EyeService() throws Exception {
		loadProps();
		this.getContext().getLogger().setLevel(java.util.logging.Level.OFF);
		getServers().add(Protocol.HTTP,Integer.parseInt(System.getProperty("eye.service.port")));
		getDefaultHost().attach("/lights", new EyeLightApp());
		getDefaultHost().attach("/servo", new EyeServoApp());
		
		getDefaultHost().attach("/image", new EyeImageApp());
		getDefaultHost().attach("/qr", new EyeQRApp());
		getDefaultHost().attach("/face", new EyeFaceApp());
		getDefaultHost().attach("/prefs", new RhinoPrefs());
	}
	
	public void loadProps() throws IOException{
		Properties props = new Properties();
		props.load(EyeLightControl.class.getResourceAsStream("eye.properties"));
		for (Object key : props.keySet()) {
			if (!System.getProperties().containsKey(key)) {
				System.getProperties().setProperty((String) key,
						(String) props.get(key));
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		new EyeService().start();
	}
}
