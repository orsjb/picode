package core.config;

public class JSONInteger implements JSONMapping<Integer> {
	private Integer defaultValue = null;
	private boolean isRequired = true;
	private String key;
	
	public JSONInteger(String key, boolean isRequired, Integer defaultValue) {
		super();
		this.defaultValue = defaultValue;
		this.isRequired = isRequired;
		this.key = key;
	}
	
	public JSONInteger(String key) {
		super();
		this.key = key;
	}

	@Override
	public String key() {
		return key;
	}
	
	@Override
	public Integer defaultValue() {
		return defaultValue;
	}
	
	@Override
	public boolean isRequired() {
		return isRequired;
	}
}
