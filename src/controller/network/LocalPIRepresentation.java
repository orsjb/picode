package controller.network;



public class LocalPIRepresentation {

	long lastTimeSeen;
	String hostname;
	int id;
	
	public LocalPIRepresentation(String hostname) {
		this.hostname = hostname;
	}

	public String getHostname() {
		return hostname;
	}

	
}
