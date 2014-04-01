package synch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;

public class Synchronizer {

	/*
	 * A tool for each Raspberry PI to work out its current synch with respect to all other PIs.
	 * We keep this independent of the audio system because the audio system start-time needs to be synched.
	 * 
	 * Each synchronizer sends regular pulses every second with the syntax:
	 * s <MAC1> <timeMS>
	 * 
	 * An s means send. Upon receiving an s, each synchronizer also responds with
	 * r <MAC1> <timeMS> <MAC2> <timeMS>
	 */
	
	String myMAC; //how to uniquely identify this machine
	String myIP;
	MulticastSocket broadcastSocket;
	String multicastGroup = "225.2.2.5";
	int multicastPort = 2225;
	long timeCorrection;			//add this to current time to get the REAL current time

	boolean on = true;
	boolean verbose = true;
	boolean veryverbose = false;
	
	
	public Synchronizer() {
		try {
			//basic init => find out my mac address and IP address
			getMyMACAddressAndIPAddress();
			//start listening
			setupListener();
			//setup sender
			broadcastSocket = new MulticastSocket();
			//start sending
			startSending();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public long timeNow() {
		return System.currentTimeMillis() + timeCorrection;
	}
	
	private void setupListener() throws IOException {
		final MulticastSocket s = new MulticastSocket(multicastPort);
		s.joinGroup(InetAddress.getByName(multicastGroup));
		//start a listener thread
		Thread t = new Thread() {
			public void run() {
				while(on) {
					try {
						byte[] buf = new byte[512];
						DatagramPacket pack = new DatagramPacket(buf, buf.length);
						s.receive(pack);
						String response = new String(buf, "US-ASCII");
						if(veryverbose) System.out.println("Received data: " + response + " (total string length=" + pack.getLength() + ")");
						messageReceived(response);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				s.close();
			}
		};
		t.start();
	}
	
	private void startSending() {
		Thread t = new Thread() {
			public void run() {
				while(on) {
					broadcast("s " + myMAC + " " + timeNow());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}
	
	public void close() {
		on = false;
		broadcastSocket.close();
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
	    	  if(verbose) System.out.print("Interface: " + nif.getDisplayName());
	    	  if(verbose) System.out.println(" (MAC=" + lStringBuffer + ")");
	    	  macs.add(lStringBuffer.toString());
		      Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
		      for (InetAddress inetAddress : Collections.list(inetAddresses)) {
		    	  if(inetAddress instanceof Inet4Address) {
		    		  ips.add(inetAddress.getHostAddress());
		    		  if(verbose) System.out.println(" -- InetAddress: " + inetAddress.getHostAddress());
		    	  }
		    	  
		      }
	      }
	    }
		//our assumption is that the relevant hardware address (wifi) is the first in list
		//but we'd prefer to do better than this
		//but it doesn't really matter because this is just to identify the unit
		myMAC = macs.get(0);
		myIP = ips.get(0);
		if(verbose) System.out.println("My IP address: " + myIP);
	}
	
	public void messageReceived(String msg) {
		String[] parts = msg.split("[ ]");
		for(int i = 0; i < parts.length; i++) {
			parts[i] = parts[i].trim();
		}
		if(parts[0].equals("s")) {
			//an original send message
			//respond if you were not the sender
			if(!parts[1].equals(myMAC)) {
				broadcast("r " + parts[1] + " " + parts[2] + " " + myMAC + " " + timeNow());
			}
		} else if(parts[0].equals("r")) {
			//a response message
			//respond only if you WERE the sender
			if(parts[1].equals(myMAC)) {
				//find out how long the return trip was
				long timeOriginallySent = Long.parseLong(parts[2]);
				String otherMAC = parts[3];
				long timeReturnSent = Long.parseLong(parts[4]);
				long currentTime = timeNow();
				long returnTripTime = currentTime - timeOriginallySent;
				long timeAheadOfOther = (currentTime - (returnTripTime / 2)) - timeReturnSent;	//+ve if this unit is ahead of other unit
				
				//correct your time to be that of the other's time
				timeCorrection = -timeAheadOfOther;

				
				
				if(verbose) System.out.println("Return trip from " + myMAC + " to " + parts[3] + " took " + returnTripTime + "ms");
				if(verbose) System.out.println("This machine (" + myMAC + ") is " + (timeAheadOfOther > 0 ? "ahead of" : "behind") + " " + otherMAC + " by " + Math.abs(timeAheadOfOther) + "ms");
				
			}
		}
	}

	private void broadcast(String s) {
		byte buf[] = null;
		try {
			buf = s.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		if(verbose) System.out.println("Sending message: " + s + " (length in bytes = " + buf.length + ")");
		// Create a DatagramPacket 
		DatagramPacket pack = null;
		try {
			pack = new DatagramPacket(buf, buf.length, InetAddress.getByName(multicastGroup), multicastPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
		try {
			broadcastSocket.send(pack);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	public static void main(String[] args) {
		new Synchronizer();
	}
	
}
