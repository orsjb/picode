package pi;

import java.util.Scanner;

import pi.dynamic.DynamoPI;
import core.AudioSetup;

/**
 * Entry point for PI code.
 * 
 * @param args
 */
public class PIMain { 

	public static final String myHostname;								//the hostname for this PI
	public static final String myMAC;									//the wlan MAC for this PI
	
	static {
		//get the hostname
		Scanner s = new Scanner("/etc/hostname");
		myHostname = s.next();
		s.close();
		//derive the MAC from the hostname
		myMAC = myHostname.substring(8, 20);
	}
	
	public static void main(String[] args) throws Exception {
		new DynamoPI(AudioSetup.getAudioContext(args));
	}
}
