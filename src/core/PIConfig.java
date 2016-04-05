package core;

import java.net.UnknownHostException;

public class PIConfig implements EnvironmentConf, ControllerDiscoverer {
	private String controllerHostName;

	@Override
	public String getControllerHostname() {
		if (controllerHostName != null) {
			return controllerHostName;
		}
		
		//Block and search for a controller, we need a logging framework too now :/
		try {
			controllerHostName = listenForController(getMulticastSynchAddr(), getControllerDiscoveryPort());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return controllerHostName;
	}
	
	public int getMyId() {
		return 0;
	}

}
