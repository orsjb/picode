package controller.network;

import java.io.IOException;
import java.net.InetSocketAddress;

import core.Config;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;



public class LocalPIRepresentation {

	long lastTimeSeen;
	public final String hostname;
	public final int id;
	private InetSocketAddress addr;
	private final OSCServer server;
	public final boolean[] groups;
	
	public LocalPIRepresentation(String hostname, int id, OSCServer server) {
		this.hostname = hostname;
		this.id = id;
		this.server = server;
		groups = new boolean[4];
	}

	public synchronized void send(String msgName, Object... args) {
		OSCMessage msg = new OSCMessage(msgName, args);
		if(addr == null) {
			addr = new InetSocketAddress(hostname, Config.controlToPIPort);
		}
		try {
			server.send(msg, addr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
