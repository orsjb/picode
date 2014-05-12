package pi;

import java.io.IOException;

import pi.dynamic.DynamoPI;
import pi.network.StatusUpdateSender;
import pi.synch.Synchronizer;
import core.AudioSetup;

/**
 * Entry point for PI code.
 * 
 * - Starts Synchronizer, which runs continually in its own thread and keeps things in sync. It does 
 * this by (1) attempting to start all audio systems in time, (2) keeping track of the frame count and clock
 * so that new agents can find synch.
 * 
 * - Starts listening to sensors.
 * 
 * - Creates connection to controller. Listens to incoming commands and sends status back.
 * 
 * @param args
 */
public class PIMain {

	/** Handles synchronization. */
	Synchronizer synchronizer;
	
	/** Sends status updates to the controller. */
	StatusUpdateSender statusUpdateSender;
	
	/** Runs audio and listens for control. */
	DynamoPI dynamo;
	
	public static void main(String[] args) throws Exception {
		new PIMain(args);
	}
	
	public PIMain(String[] args) throws Exception {	
		
		
		dynamo = new DynamoPI(AudioSetup.getAudioContext(args));
		synchronizer = new Synchronizer();
		statusUpdateSender = new StatusUpdateSender();
		
		
	}

}
