package core;


public abstract class Config {

	//hosts and ports for network messages
	public final static String controllerHostname  	= "boing.local"; 
	public final static String multicastSynchAddr   = "225.2.2.5";
	public final static int statusFromPIPort 		= 2223;
	public final static int clockSynchPort			= 2224;
	public final static int codeToPIPort			= 2225;
	public final static int controlToPIPort			= 2226;
	
	//places
	public final static String workingDir = "/home/pi/git/pisound";
	public final static String audioDir = workingDir + "/audio";
	
	
	
	
}
