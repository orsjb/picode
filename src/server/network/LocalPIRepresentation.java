package server.network;



public class LocalPIRepresentation {

	long lastTimeSeen;
	
	public LocalPIRepresentation(String hostname) {
		this.hostname = hostname;
	}

	String hostname;
	public String getHostname() {
		return hostname;
	}

	
}
