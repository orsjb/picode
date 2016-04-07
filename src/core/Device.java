package core;

import java.io.File;
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
				//if you can't get the wlan then get the ethernet mac address:
				if(netInterface == null) {
					netInterface = NetworkInterface.getByName("en0");
				}
			} else {
				netInterface = NetworkInterface.getByName("wlan0");
				if (netInterface == null) {
					netInterface = NetworkInterface.getByName("eth0");
				}
			}
			
			if(netInterface != null) {
				byte[] mac = netInterface.getHardwareAddress();
				StringBuilder builder = new StringBuilder();
				for (byte a : mac) {
					builder.append(String.format("%02x", a));
				}
				tmpMAC = builder.substring(0, builder.length());
			}
			//first attempt at hostname is to query the /etc/hostname file which should have
			//renamed itself (on the PI) before this Java code runs
			try {
				Scanner s = new Scanner(new File("/etc/hostname"));
				String line = s.next();
				if (line != null && !line.isEmpty() && !line.endsWith("-")) {
					tmpHostname = line;
				}
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
			//If everything still isn't working lets try via our interface for an IP address
			if (tmpHostname == null) {
				tmpHostname = netInterface.getInetAddresses().nextElement().getHostAddress();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//ensure we have a local suffix
		// Windows won't care either way but *nix systems need it
		if (!tmpHostname.contains(".")) {
			tmpHostname += ".local";	//we'll assume a .local extension is required if no extension exists
		}
		
		myHostname = tmpHostname;
		myMAC = tmpMAC;
		//report
		System.out.println("My hostname is: " + myHostname);
		System.out.println("My wlan MAC address is: " + myMAC);
	}
	
	public static void main(String[] args) {
		//static code above will run
		@SuppressWarnings("unused")
		String x = Device.myHostname;
	}
	
}
