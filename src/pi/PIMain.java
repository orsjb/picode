package pi;

import pi.dynamic.DynamoPI;
import core.AudioSetup;

/**
 * Entry point for PI code.
 * 
 * @param args
 */
public class PIMain { 
	
	public static void main(String[] args) throws Exception {
		new DynamoPI(AudioSetup.getAudioContext(args));
	}
}
