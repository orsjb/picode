package core;

public class ControllerConfig implements EnvironmentConf {

	@Override
	public String getControllerHostname() {
		return getMyHostName(); // I am the controller
	}

	@Override
	public String getMyHostName() {
		// TODO Auto-generated method stub
		return null;
	}

}
