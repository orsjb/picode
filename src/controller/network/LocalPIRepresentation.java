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
	
	public LocalPIRepresentation(String hostname, int id) {
		this.hostname = hostname;
		this.id = id;
	}

	public synchronized void send(OSCServer serv, OSCMessage msg) {
		if(addr == null) {
			addr = new InetSocketAddress(hostname, Config.controlToPIPort);
		}
		try {
			serv.send(msg, addr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
