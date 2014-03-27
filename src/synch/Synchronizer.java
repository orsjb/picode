package synch;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class Synchronizer implements OSCListener {

	/*
	 * A tool for each Raspberry PI to work out its current synch with respect to all other PIs.
	 * We keep this independent of the audio system because the audio system start-time needs to be synched.
	 */
	
	String uid; //how to uniquely identify this machine
	String uidOfLeader;
	String myIP;
	String broadcastAddr;
	OSCServer sender; //the OSC element that sends blips
	boolean broadcasting = false;
	int port = 3323;
	long delayTimeMS = 1000;
	long timeOfLastTick = -1;
	Map<String, Long> currentTickTimes = new Hashtable<String, Long>();
	
	public Synchronizer() {
		try {
			getMyMACAddressAndIPAddress();
			//next start broadcasting once every second, just broadcast your own name
			//also listen
			sender = OSCServer.newUsing(OSCServer.UDP, port);
			sender.addOSCListener(this);
			sender.start();
			startBroadcast();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getMyMACAddressAndIPAddress() throws SocketException {
		//first do all of this to find the MAC address which we will use as default UID
		ArrayList<String> macs = new ArrayList<String>();
		ArrayList<String> ips = new ArrayList<String>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	    while (interfaces.hasMoreElements()) {
	      NetworkInterface nif = interfaces.nextElement();
	      if(!nif.isUp()) continue;
	      byte[] lBytes = nif.getHardwareAddress();
	      StringBuffer lStringBuffer = new StringBuffer();
	      if (lBytes != null) {
	        for (byte b : lBytes) {
	          lStringBuffer.append(String.format("%1$02X", new Byte(b)));
	        }
	      }
	      if(lStringBuffer.length() > 0) {
		      System.out.print("Interface: " + nif.getDisplayName());
		      System.out.println(" (MAC=" + lStringBuffer + ")");
	    	  macs.add(lStringBuffer.toString());
		      Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
		      for (InetAddress inetAddress : Collections.list(inetAddresses)) {
		    	  if(inetAddress instanceof Inet4Address) {
		    		  ips.add(inetAddress.getHostAddress());
		    		  System.out.println(" -- InetAddress: " + inetAddress.getHostAddress());
		    	  }
		    	  
		      }
	      }
	    }
		//our assumption is that the relevant hardware address (wifi) is the first in list
		//but we'd prefer to do better than this
		//but it doesn't really matter because this is just to identify the unit
		uid = macs.get(0);
		uidOfLeader = uid;
		myIP = ips.get(0);
		String[] ipParts = myIP.split("[.]");
		broadcastAddr = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + "." + "255";
		System.out.println("My IP address: " + myIP);
		System.out.println("My broadcast address: " + broadcastAddr);
	}
	
	public void startBroadcast() {
		if(broadcasting) return;
		broadcasting = true;
		new Thread() {
			public void run() {
				while(broadcasting) {
					try {
						sender.send(new OSCMessage(uid), new InetSocketAddress(broadcastAddr, port));
						timeOfLastTick = System.currentTimeMillis();
						
						//TODO plan next delay time
						
						Thread.sleep(delayTimeMS);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public void stopBroadcast() {
		broadcasting = false;
	}

	@Override
	public void messageReceived(OSCMessage msg, SocketAddress arg1, long arg2) {
		String sourceUID = msg.getName();
		if(sourceUID.equals(uid)) {
			System.out.println("Received selfie.");
			return;
		}
		System.out.println("Ext message received: " +  sourceUID);
		//store the time of the incoming
		currentTickTimes.put(sourceUID, System.currentTimeMillis());
		//see if the sender can be classed as leader 
		//(if their uid is lexographically earlier, that.compareTo(this)<0 )
		if(sourceUID.compareTo(uidOfLeader) < 0) {
			uidOfLeader = sourceUID;					//TODO what if leader stops sending?
		}
		
		
	}
	
	
	public static void main(String[] args) {
		new Synchronizer();
	}
	
	
	
}
