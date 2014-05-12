package core;

import java.net.NetworkInterface;

public class Util {

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
	
	//reboots teh PI
	public static void rebootPI() {
		try {
			Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","sudo reboot"}).waitFor();
		} catch (Exception e) {}
	}
	
}
