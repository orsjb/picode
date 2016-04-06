package pi;

import pi.dynamic.DynamoPI;
import core.AudioSetup;
import core.PIConfig;

/**
 * Entry point for PI code.
 * 
 * @param args
 */
public class PIMain { 
	
	public static void main(String[] args) throws Exception {
		DynamoPI pi = new DynamoPI(AudioSetup.getAudioContext(args), new PIConfig());
		if(args.length > 5) {
			boolean autostart = Boolean.parseBoolean(args[5]);
			if(autostart) {
				System.out.println("Detected autostart. Starting audio right away.");
				pi.startAudio();
			}
		}
	}
}
