package pi;

import core.PIConfig;

public class ManagerAgent {

	public static void main(String[] args) {
		System.out.println("PI Manager Agent Started");
		PIConfig env = new PIConfig();
		System.out.println("Listening for Controller...");
		System.out.println("Found controller on host: " + env.getControllerHostname());
		
		System.out.println("Requesting configuration settings from controller...");

		System.out.println("PI Manager Agent Exiting");
	}

}
