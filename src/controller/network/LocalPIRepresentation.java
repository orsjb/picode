package controller.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.UnresolvedAddressException;

import javafx.scene.Node;
import core.Config;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;



public class LocalPIRepresentation {

	public long lastTimeSeen;
	public final String hostname;
	public final int id;
	private InetSocketAddress addr = null;
	private final OSCServer server;
	public final boolean[] groups;
	
	Node gui = null;
	
	public LocalPIRepresentation(String hostname, int id, OSCServer server) {
		this.hostname = hostname;
		this.id = id;
		this.server = server;
		groups = new boolean[4];
	}

	public synchronized void send(String msgName, Object... args) {
		if(hostname.startsWith("Virtual Test PI")) {
			return;
		}
		OSCMessage msg = new OSCMessage(msgName, args);
		if(addr == null) {
			addr = new InetSocketAddress(hostname, Config.controlToPIPort);
		}
		try {
			server.send(msg, addr);
		} catch (UnresolvedAddressException e) {
			System.out.println("Unable to send to PI: " + hostname);
			//e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public Node getGui() {
		return gui;
	}

	public void setGui(Node gui) {
		this.gui = gui;
	}
	
}
