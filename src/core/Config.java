package core;

public abstract class Config {

	//ports for network messages
	public final static int statusSendPort 		= 2223;
	public final static int clockSynchPort		= 2224;
	public final static int codeSendPort		= 2225;
	public final static int controlSendPort		= 2226;
	
	//places
	public final static String workingDir = "/home/pi/git/pisound";
	public final static String audioDir = workingDir + "/audio"; 
	
	
}
