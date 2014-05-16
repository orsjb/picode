package controller.network;



public class LocalPIRepresentation {

	long lastTimeSeen;
	public final String hostname;
	public final int id;
	
	public LocalPIRepresentation(String hostname, int id) {
		this.hostname = hostname;
		this.id = id;
	}

	
}
