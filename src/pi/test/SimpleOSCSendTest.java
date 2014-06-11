package pi.test;

import java.io.IOException;
import java.net.InetSocketAddress;

import core.Config;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class SimpleOSCSendTest {

	public static void main(String[] args) throws IOException {
		OSCServer s = OSCServer.newUsing(OSCServer.UDP);
		InetSocketAddress addr = new InetSocketAddress("pisound-009e959c510a.local", Config.controlToPIPort);
		
		s.send(new OSCMessage("/PI/"), null);
	}

}
