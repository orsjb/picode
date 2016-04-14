package core.config;

public class JSONDouble implements JSONMapping<Double> {
	private Double defaultValue = null;
	private boolean isRequired = true;
	private String key;
	
	public JSONDouble(String key, boolean isRequired, Double defaultValue) {
		super();
		this.defaultValue = defaultValue;
		this.isRequired = isRequired;
		this.key = key;
	}
	
	public JSONDouble(String key) {
		super();
		this.key = key;
	}

	@Override
	public String key() {
		return key;
	}
	
	@Override
	public Double defaultValue() {
		return defaultValue;
	}
	
	@Override
	public boolean isRequired() {
		return isRequired;
	}
}
