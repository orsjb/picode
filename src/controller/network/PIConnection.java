package controller.network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javafx.application.Platform;
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
	int newID = -1;
	
	public PIConnection() {
		pis = new Hashtable<String, LocalPIRepresentation>();
		knownPIs = new Hashtable<String, Integer>();
		//read the known pis from file
		try {
			Scanner s = new Scanner(new File(Config.knownPIsFile));
			while(s.hasNext()) {
				String[] line = s.nextLine().split("[ ]");
				knownPIs.put(line[0], Integer.parseInt(line[1]));
			}
			s.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
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
			System.out.println("PI Alive Message: " + piName);
			//see if we have this PI yet
			LocalPIRepresentation thisPI = pis.get(piName);
			if(thisPI == null) { //if not add it
				int id = 0;
				if(knownPIs.containsKey(piName)) {
					id = knownPIs.get(piName);					
				} else {
					id = newID--;
				}
				thisPI = new LocalPIRepresentation(piName, id);
				pis.put(piName, thisPI);
				//tell the listener
				if(listener != null) {
					listener.piAdded(thisPI);
				}
				//make sure this PI knows its ID
				//since there is a lag in assigning an InetSocketAddress, and since this is the first
				//message sent to the PI, it should be done in a separate thread.
				final LocalPIRepresentation piID = thisPI;
				new Thread() {
					public void run() {
						sendToPI(piID, "/PI/set_id", piID.id);					
					}
				}.start();
			}
			//keep up to date
			thisPI.lastTimeSeen = System.currentTimeMillis();	//Ultimately this should be "corrected time"
		}
	}
	
	public void sendToPI(LocalPIRepresentation pi, String msgName, Object... args) {
		pi.send(oscServer, new OSCMessage(msgName, args));
	}
	
	public void sendToAllPIs(String msgName, Object... args) {
		for(LocalPIRepresentation pi : pis.values()) {
			sendToPI(pi, msgName, args);
		}
	}
	
	public void sendToPIList(String[] list, String msgName, Object... args) {
		for(String piName : list) {
			sendToPI(pis.get(piName), msgName, args);
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
		for(final String piName : pisToRemove) {
			//tell the listener
			if(listener != null) {
				Platform.runLater(new Runnable() {
			        @Override
			        public void run() {
						listener.piRemoved(pis.get(piName));
						System.out.println("Removed PI from list: " + piName);
			        }
			   });
			}
			pis.remove(piName);
		}
	}
	
	
	//standard messages to PI
	
	public void piReboot() {
		sendToAllPIs("/PI/reboot");
	}

	public void piShutdown() {
		sendToAllPIs("/PI/shutdown");
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
