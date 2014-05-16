package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.NetworkInterface;
import java.util.Scanner;

public abstract class Device {

	public static final String myHostname;						//the hostname for this PI
	public static final String myMAC;							//the wlan MAC for this PI
	
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
			//e.printStackTrace();
		}
		myHostname = tmp;
		if(myHostname != null) {
			myMAC = myHostname.substring(8, 20);
		} else {
			myMAC = null;
		}
	}
	
	
	//OLD............
	
	
	//some hardware-related fields and methods

	//macWlan stores the wlan MAC address as a String, lowercase with no colons separating
	public static final String macWlan;
	static {
		String result = "Unknown";
		try {
			NetworkInterface netInterface;
			if (System.getProperty("os.name").startsWith("Mac OS")) {
				netInterface = NetworkInterface.getByName("en1");
			} else {
				netInterface = NetworkInterface.getByName("wlan0");
			}
			byte[] mac = netInterface.getHardwareAddress();
			StringBuilder builder = new StringBuilder();
			for (byte a : mac) {
				builder.append(String.format("%02x", a));
			}
			result = builder.substring(0, builder.length() - 1);
		} catch (Exception e) { //WiFi isn't up yet 
			e.printStackTrace();
		}
		macWlan = result;
	}

	//returns the name in the form pisound-<MAC>.local
	public static String getDeviceName() {
		return "pisound-" + macWlan + ".local";
	}

	
	public static void main(String[] args) {
		System.out.println(macWlan);
		
	}
	
}
