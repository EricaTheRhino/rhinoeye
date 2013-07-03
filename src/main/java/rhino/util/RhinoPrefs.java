package rhino.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.routing.Router;

import rhino.AppTypedResource;

public class RhinoPrefs extends Application {

	private static final String PREFS_BASE_NODE = "/org/openimaj/rhino";

	public static Preferences getPrefs() {
		final Preferences base = Preferences.userRoot().node(PREFS_BASE_NODE);
		return base;
	}

	public static int getInt(String key, int i) {
		final Preferences prefs = getPrefs();
		final int ret = prefs.getInt(
				key,
				Integer.parseInt(System.getProperty(key, "" + i))
				);
		if (ret != prefs.getInt(key, Integer.MIN_VALUE)) {
			prefs.put(key, "" + ret);
			try {
				prefs.sync();
			} catch (final BackingStoreException e) {
			}
		}
		return ret;
	}

	public static long getLong(String key, long l) {
		final Preferences prefs = getPrefs();
		final int ret = prefs.getInt(
				key,
				Integer.parseInt(System.getProperty(key, "" + l))
				);
		if (ret != prefs.getLong(key, Long.MIN_VALUE)) {
			prefs.put(key, "" + ret);
			try {
				prefs.sync();
			} catch (final BackingStoreException e) {
			}
		}
		return ret;
	}

	public static String getString(String key, String l) {
		final Preferences prefs = getPrefs();
		final String ret = prefs.get(
				key,
				System.getProperty(key, "" + l)
				);
		if (ret != prefs.get(key, "")) {
			prefs.put(key, "" + ret);
			try {
				prefs.sync();
			} catch (final BackingStoreException e) {
			}
		}
		return ret;
	}

	
	public static class RhinoPrefInner extends AppTypedResource<RhinoPrefs> {
		@Put
		@Post
		public void set(Representation rep) {
			final Object k = this.getRequest().getAttributes().get("key");
			final Preferences prefs = getPrefs();
			if (k != null) {
				final Form f = new Form(rep);
				prefs.put(k.toString(), f.getFirstValue("v"));
			} else {
				final Form f = new Form(rep);
				final Map<String, String> keyvalues = f.getValuesMap();

				for (final Entry<String, String> parameter : keyvalues.entrySet()) {
					prefs.put(parameter.getKey(), parameter.getValue());
				}
			}
			try {
				prefs.sync();
			} catch (final BackingStoreException e) {
			}
		}

		@Override
		@Get("json")
		public Representation get() {

			final Object k = this.getRequest().getAttributes().get("key");

			JsonRepresentation rep = null;
			final Preferences prefs = getPrefs();
			if (k != null)
			{
				final HashMap<Object, Object> keyval = new HashMap<Object, Object>();
				keyval.put(k, prefs.get(k.toString(), null));
				rep = new JsonRepresentation(keyval);
			}
			else {
				final Map<Object, Object> mapprefs = asMap(prefs);
				rep = new JsonRepresentation(mapprefs);
			}
			System.out.println(this.getRequest());
			return rep;
		}

		private Map<Object, Object> asMap(Preferences prefs) {
			final Map<Object, Object> ret = new HashMap<Object, Object>();
			try {
				for (final String key : prefs.keys()) {
					ret.put(key, prefs.get(key, null));
				}
			} catch (final BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ret;
		}
	}

	public RhinoPrefs() {
	}

	@Override
	public Restlet createInboundRoot() {
		final Router router = new Router(getContext());
		router.attach("/{key}", RhinoPrefInner.class);
		router.attach("/", RhinoPrefInner.class);
		return router;
	}

}
