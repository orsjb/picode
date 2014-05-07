package pi.network;

import java.io.IOException;

import core.Config;
import de.sciss.net.OSCServer;

public class StatusUpdateSender {
	
	OSCServer serv;
	
	public StatusUpdateSender() {
		//create server
		try {
			serv = OSCServer.newUsing(OSCServer.UDP, Config.statusSendPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//start thread to send status updates
		Thread t = new Thread() {
			public void run() {
				
				//TODO
				
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
	}
	
	
	
}
