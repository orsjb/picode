package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Scanner;

public abstract class Device {

	public static final String myHostname;						//the hostname for this PI (wifi)
	public static final String myMAC;							//the wlan MAC for this PI (wifi)

	static {
		String tmpHostname = null;
		String tmpMAC = null;
		try {
			NetworkInterface netInterface;
			if (System.getProperty("os.name").startsWith("Mac OS")) {
				netInterface = NetworkInterface.getByName("en1");
			} else {
				netInterface = NetworkInterface.getByName("wlan0");
			}
			if(netInterface != null) {
				byte[] mac = netInterface.getHardwareAddress();
				StringBuilder builder = new StringBuilder();
				for (byte a : mac) {
					builder.append(String.format("%02x", a));
				}
				tmpMAC = builder.substring(0, builder.length() - 1);
			} 
			//first attempt at hostname is to query the /etc/hostname file which should have
			//renamed itself (on the PI) before this Java code runs
			try {
				Scanner s = new Scanner(new File("/etc/hostname"));
				tmpHostname = s.next() + ".local";
				s.close();
			} catch(Exception e) {/*Swallow this exception*/}
			//if we don't have the mac derive the MAC from the hostname
			if(tmpMAC == null && tmpHostname != null) {
				tmpMAC = tmpHostname.substring(8, 20);
			} 
			//if we don't have the hostname get by traditional means
			if(tmpHostname == null) {
				tmpHostname = InetAddress.getLocalHost().getHostName();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		myHostname = tmpHostname;
		myMAC = tmpMAC;
	}
	
	public static void main(String[] args) {
		System.out.println(myHostname + " " + myMAC);
		
	}
	
}
