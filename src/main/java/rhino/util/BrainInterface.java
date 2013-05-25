package rhino.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class BrainInterface {
	static Logger logger = Logger.getLogger(BrainInterface.class);

	public static void postEvent(String name, String... params) {
		try {
			final HttpClient httpclient = new DefaultHttpClient();
			final HttpPost httppost = new HttpPost("http://brain/events/");

			httppost.addHeader("content-type", "application/x-www-form-urlencoded");
			httppost.setEntity(encode(name, params));

			final HttpResponse res = httpclient.execute(httppost);

			logger.debug(IOUtils.toString(res.getEntity().getContent()));
		} catch (final Exception e) {
			logger.debug(e);
		}
	}

	static StringEntity encode(String name, String... params) throws UnsupportedEncodingException {
		final String jsonString = "{\"event\":\"" + name + "\", \"params\": {" + encodeParams(params) + "}}";

		return new StringEntity(jsonString);
	}

	private static String encodeParams(String[] params) {
		String result = "";

		for (int i = 0; i < params.length; i += 2) {
			result += "\"" + params[i] + "\":\"" + params[i + 1] + "\"";
			if (i < params.length - 2)
				result += ",";
		}

		return result;
	}
}
