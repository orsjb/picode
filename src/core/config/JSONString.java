package core.config;

public class JSONString implements JSONMapping<String> {
	private String defaultValue = null;
	private boolean isRequired = true;
	private String key;
	
	public JSONString(String key, boolean isRequired, String defaultValue) {
		super();
		this.defaultValue = defaultValue;
		this.isRequired = isRequired;
		this.key = key;
	}
	
	public JSONString(String key) {
		super();
		this.key = key;
	}

	@Override
	public String key() {
		return key;
	}
	
	@Override
	public String defaultValue() {
		return defaultValue;
	}
	
	@Override
	public boolean isRequired() {
		return isRequired;
	}

}
