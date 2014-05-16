package controller.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import core.Config;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class PIConnection {
	
	public interface Listener {
		public void piAdded(LocalPIRepresentation pi);
		public void piRemoved(LocalPIRepresentation pi);
	}
	
	OSCServer oscServer;
	Map<String, LocalPIRepresentation> pis;
	Map<String, Integer> knownPIs;
	Listener listener;
	
	public PIConnection() {
		pis = new Hashtable<String, LocalPIRepresentation>();
		knownPIs = new Hashtable<String, Integer>();
		//read the known pis from file
		Scanner s = new Scanner(Config.knownPIsFile);
		while(s.hasNext()) {
			String[] line = s.nextLine().split("[ ]");
			knownPIs.put(line[0], Integer.parseInt(line[1]));
		}
		s.close();
		// create the OSC Server
		try {
			oscServer = OSCServer.newUsing(OSCServer.UDP, Config.statusFromPIPort);
			oscServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// set up to listen for basic messages
		oscServer.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress source, long timestamp) {
				incomingMessage(msg);
			}
		});
		// set up thread to watch for lost PIs
		new Thread() {
			public void run() {
				while(true) {
					checkPIAliveness();
					try {
						Thread.sleep(Config.aliveInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	private void incomingMessage(OSCMessage msg) {
		if(msg.getName().equals("/PI/alive")) {
			String piName = (String)msg.getArg(0);
			//see if we have this PI yet
			LocalPIRepresentation thisPI = pis.get(piName);
			if(thisPI == null) { //if not add it
				int id = knownPIs.get(piName);
				thisPI = new LocalPIRepresentation(piName, id);
				pis.put(piName, thisPI);
				//make sure this PI knows its ID
				sendToPI(piName, "/PI/set_id", id);
				//tell the listener
				if(listener != null) {
					listener.piAdded(thisPI);
				}
			}
			//keep up to date
			thisPI.lastTimeSeen = System.currentTimeMillis();	//Ultimately this should be "corrected time"
		}
	}
	
	public void sendToPI(String piName, String msgName, Object... args) {
		try {
			InetSocketAddress target = new InetSocketAddress(piName, Config.controlToPIPort);
			oscServer.send(new OSCMessage(msgName, args), target);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendToAllPIs(String msgName, Object... args) {
		for(String piName : pis.keySet()) {
			sendToPI(piName, msgName, args);
		}
	}
	
	public void sendToPIList(String[] list, String msgName, Object... args) {
		for(String piName : list) {
			sendToPI(piName, msgName, args);
		}
	}

	private void checkPIAliveness() {
		long timeNow = System.currentTimeMillis();
		List<String> pisToRemove = new ArrayList<String>();
		for(String piName : pis.keySet()) {
			LocalPIRepresentation thisPI = pis.get(piName);
			long timeSinceSeen = timeNow - thisPI.lastTimeSeen;
			if(timeSinceSeen > Config.aliveInterval * 5) {	//config this number?
				pisToRemove.add(piName);
			}
		}
		for(String piName : pisToRemove) {
			//tell the listener
			if(listener != null) {
				listener.piRemoved(pis.get(piName));
			}
			pis.remove(piName);
		}
	}
	
	
	//standard messages to PI
	
	public void piReboot() {
		sendToAllPIs("/PI/reboot");
	}
	
	public void piSync() {
		long timeNow = System.currentTimeMillis();
		long timeToSync = timeNow + 3000;
		sendToAllPIs("/PI/sync", timeToSync);
	}
	
	public void piGain(float dest, float timeMS) {
		sendToAllPIs("/PI/gain", dest, timeMS);
	}
	
	public void piReset() {
		sendToAllPIs("/PI/reset");
	}

	public void piResetSounding() {
		sendToAllPIs("/PI/reset_sounding");
	}

	public void clearSound() {
		sendToAllPIs("/PI/clearsound");
	}

	public void piFadeoutReset(float decay) {
		sendToAllPIs("/PI/fadeout_reset", decay);
	}

	public void piFadeoutClearsound(float decay) {
		sendToAllPIs("/PI/fadeout_clearsound", decay);
	}
	
}
