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

import pi.dynamic.DynamoPI;
import core.AudioSetup;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.IOAudioFormat;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;

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
	
	AudioContext ac;
	
	String myMAC; //how to uniquely identify this machine
	String myIP;
	MulticastSocket broadcastSocket;
	String multicastGroup = "225.2.2.5";
	int multicastPort = 2225;
	long timeCorrection = 0;			//add this to current time to get the REAL current time
	long stableTimeCorrection = 0;
	long startTimeAbs;
	long lastTick;
	int stabilityCount = 0;
	boolean doLaunch = false;
	boolean launched = false;

	boolean on = true;
	boolean verbose = false;
	boolean veryverbose = false;
	boolean timedebug = false;
	
	Map<Long, Map<String, long[]>> log;		//first referenced by message send time, then by respodent's name, with the time the respondent replied and the current time
	
	public Synchronizer(AudioContext _ac) {
		
		//basics
		log = new Hashtable<Long, Map<String, long[]>>();
		
		//audio
		ac = _ac;
		
		startTimeAbs = System.currentTimeMillis();
		
		try {
			//basic init => find out my mac address and IP address
			getMyMACAddressAndIPAddress();
			//start listening
			setupListener();
			//setup sender
			broadcastSocket = new MulticastSocket();
			//start sending
			startSending();
			//display clock
			displayClock();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public long stableTimeNow() {
		return System.currentTimeMillis() + stableTimeCorrection;
	}
	
	public long correctedTimeNow() {
		return stableTimeNow() + timeCorrection;
	}
	
	public void displayClock() {
		Thread t = new Thread() {
			public void run() {
				while(on) {
					long timeNow = correctedTimeNow();
					long tick = timeNow / 10000;
					if(tick != lastTick && timeNow % 10000 < 4) {
						//display
						Date d = new Date(timeNow);
						System.out.println("The time is: " + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds() + " (short correction = " + timeCorrection + "ms, long correction = " + stableTimeCorrection + "ms)");
						//launch after 30s
						long correctedStartTime = startTimeAbs + timeCorrection + stableTimeCorrection;
						
						//this is temp - replace this with a remote message to start audio
						if(timeNow - correctedStartTime > 30000 && !launched) {
							doLaunch = true;
							launched = true;
						}
						
						if(doLaunch) {
							launch();
							doLaunch = false;
						}
						lastTick = tick;
					}
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}
	
	private void launch() {
		try {
			new DynamoPI(ac);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
					broadcast("s " + myMAC + " " + stableTimeNow() + " " + myMAC + " " + stableTimeNow());	
					//the last two components are just to ensure that send and return messages are same length to avoid network delays
					try {
						Thread.sleep(500 + (int)(100 * Math.random()));	//randomise send time to break network send patterns
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//now that all of the responses have come back...
					calculateTimeCorrection();
					try {
						Thread.sleep(500 + (int)(100 * Math.random()));	//randomise send time to break network send patterns
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
	
	private void calculateTimeCorrection() {
		
		for(Long sendTime : log.keySet()) {
			Map<String, long[]> responses = log.get(sendTime);
			//find the leader
			String theLeader = myMAC;
			
			if(timedebug) System.out.println("At send time = " + sendTime);
			
			for(String mac : responses.keySet()) {
				
				if(timedebug) System.out.println("          Response from: " + mac + " return sent: " + responses.get(mac)[0] + ", received: " + responses.get(mac)[1]);
				
				if(theLeader.compareTo(mac) < 0) {
					theLeader = mac;
				}
			}	
			
			if(timedebug) System.out.println("Leader is " + theLeader);
			
			if(theLeader != myMAC) {
				//if you are not the leader then make a time adjustment
				long[] times = responses.get(theLeader);
				long leaderResponseTime = times[0];
				long receiveTime = times[1];
				long roundTripTime = receiveTime - sendTime;
				long messageTime = roundTripTime / 2;
				long receiveTimeAccordingToLeader = leaderResponseTime + messageTime;
				timeCorrection = receiveTimeAccordingToLeader - receiveTime;
				if(timedebug) System.out.println("time correction: " + timeCorrection + ", message time: " + messageTime + ", response sent: " + leaderResponseTime + ", response received: " + receiveTime);
			}
		}		
		//finally, clear the log (for now - we might make the log last longer later)
		log.clear();
		//stability count
		if(stabilityCount++ == 20) {
			stabilityCount = 0;
			stableTimeCorrection += timeCorrection;
			timeCorrection = 0;
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
		myMAC = macs.get(0);
		myIP = ips.get(0);
		System.out.println("My IP address: " + myIP);
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
				broadcast("r " + parts[1] + " " + parts[2] + " " + myMAC + " " + stableTimeNow());
			}
		} else if(parts[0].equals("r")) {
			//a response message
			//respond only if you WERE the sender
			if(parts[1].equals(myMAC)) {
				//find out how long the return trip was
				long timeOriginallySent = Long.parseLong(parts[2]);
				String otherMAC = parts[3];
				long timeReturnSent = Long.parseLong(parts[4]);
				long currentTime = stableTimeNow();
				log(timeOriginallySent, otherMAC, timeReturnSent, currentTime);
				if(verbose) {
					long returnTripTime = currentTime - timeOriginallySent;
					long timeAheadOfOther = (currentTime - (returnTripTime / 2)) - timeReturnSent;	//+ve if this unit is ahead of other unit
					System.out.println("Return trip from " + myMAC + " to " + parts[3] + " took " + returnTripTime + "ms");
					System.out.println("This machine (" + myMAC + ") is " + (timeAheadOfOther > 0 ? "ahead of" : "behind") + " " + otherMAC + " by " + Math.abs(timeAheadOfOther) + "ms");
				}
			}
		}
	}



	private void log(long timeOriginallySent, String otherMAC, long timeReturnSent, long currentTime) {
		if(!log.containsKey(timeOriginallySent)) {
			log.put(timeOriginallySent, new Hashtable<String, long[]>());
		}
		log.get(timeOriginallySent).put(otherMAC, new long[] {timeReturnSent, currentTime});
	}

	private void broadcast(String s) {
		byte buf[] = null;
		try {
			buf = s.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		if(veryverbose) System.out.println("Sending message: " + s + " (length in bytes = " + buf.length + ")");
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
		new Synchronizer(AudioSetup.getAudioContext(args));
	}
	
}
