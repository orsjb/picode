package core;

public class ControllerConfig implements EnvironmentConf {

	@Override
	public String getControllerHostname() {
		return getMyHostName(); // I am the controller
	}

}
