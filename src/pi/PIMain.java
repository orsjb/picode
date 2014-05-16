package pi;

import java.io.File;
import java.io.FileNotFoundException;
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
		String tmp = null;
		try {
			Scanner s = new Scanner(new File("/etc/hostname"));
			tmp = s.next() + ".local";
			System.out.println("My hostname is: " + tmp);
			s.close();
			//derive the MAC from the hostname
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		myHostname = tmp;
		myMAC = myHostname.substring(8, 20);
	}
	
	public static void main(String[] args) throws Exception {
		new DynamoPI(AudioSetup.getAudioContext(args));
	}
}
