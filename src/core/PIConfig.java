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

	@Override
	public String getMyHostName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getMyId() {
		return 0;
	}

}
