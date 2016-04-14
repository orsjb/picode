package core.config;

import org.json.simple.JSONObject;

public interface JSONMapping<T> {
			public String	key();
	default public boolean	isRequired()	{ return true; }
	default public T		defaultValue()	{ return null; }
	
	@SuppressWarnings("unchecked")
	default public T value(JSONObject json) {
		if (json.containsKey(key())) {
			try {
				return (T) json.get(key());
			}
			catch (ClassCastException e) {
				System.err.println( "key: " + key() + " is not of expected type!");
				return (T) null;
			}
		}
		else if (isRequired()) {
			System.err.println("Missing required key: " + key());
			return (T) null;
		}
		else {
			return (T) null;
		}
	}
}
