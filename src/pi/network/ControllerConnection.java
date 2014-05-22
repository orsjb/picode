package pi.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

import core.Config;
import core.Device;
import core.Synchronizer;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class ControllerConnection {

	public static interface Listener {
		public void msg(OSCMessage msg);
	}

	int myID;										 			//ID assigned by the controller
	private OSCServer oscServer;					 			//The one and only OSC server
	private InetSocketAddress controller;			 			//The network details of the controller
	private Set<Listener> listeners = new HashSet<Listener>(); 	//Listeners to incoming OSC messages
	
	public ControllerConnection() throws IOException {
		//init the OSCServer
		try {
			oscServer = OSCServer.newUsing(OSCServer.UDP, Config.controlToPIPort);
			oscServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//add a single master listener that forwards listening to delegates
		oscServer.addOSCListener(new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress src, long time) {
				//include default listener behaviour that listens for the ID assigned to this PI
				if(msg.getName().equals("/PI/set_id")) {
					myID = (Integer)msg.getArg(0);
				} else {
					//all other messages get forwarded to delegate listeners
					for(Listener l : listeners) {
						l.msg(msg);
					}
				}
			}
		});
		//set up the controller address
		controller = new InetSocketAddress(Config.controllerHostname, Config.statusFromPIPort);
		//set up an indefinite thread to ping the controller
		new Thread() {
			public void run() {
				while(true) {
					sendToController("/PI/alive", new Object[] {Device.myHostname, Synchronizer.time()});
					try {
						Thread.sleep(Config.aliveInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
 				
			}
		}.start();
	}

	public void sendToController(String msg, Object[] args) {
		try {
			oscServer.send(new OSCMessage(msg, args), controller);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addListener(Listener l) {
		listeners.add(l);
	}
	
	public void removeListener(Listener l) {
		listeners.remove(l);
	}
	
	public void clearListeners() {
		listeners.clear();
	}
	
	public int getID() {
		return myID;
	}

}