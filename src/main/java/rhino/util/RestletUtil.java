package rhino.util;

import java.util.HashMap;
import java.util.Map;

import org.restlet.ext.json.JsonRepresentation;

public class RestletUtil {
	public static JsonRepresentation json(Object ... objs){
		Map<Object,Object> ret = new HashMap<Object,Object>();
		int until = (objs.length/2)*2;
		for (int i = 0; i < until; i+=2) {
			ret.put(objs[i], objs[i+1]);
		}
		return new JsonRepresentation(ret);
	}
}
