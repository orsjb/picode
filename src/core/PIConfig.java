package core;

public class PIConfig implements EnvironmentConf {
	private String controllerHostName;

	@Override
	public String getControllerHostname() {
		if (controllerHostName != null) {
			return controllerHostName;
		}
		
		//Search for a controller
		return null; //placeholder
	}
	
	public int getMyId() {
		return 0;
	}

}
