package core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ControllerAdvertiser {
	ControllerConfig env;
	private Thread advertismentService;

	public ControllerAdvertiser(ControllerConfig env) throws UnknownHostException {
		super();
		this.env = env;
		
		InetAddress addr = InetAddress.getByName(env.getMulticastSynchAddr());
		//set up an indefinite thread to advertise the controller
		advertismentService = new Thread() {
			public void run() {
				try (DatagramSocket serverSocket = new DatagramSocket() ) {
					String msg = "controllerHostname: " + env.getMyHostName();
					DatagramPacket msgPacket = new DatagramPacket(
						msg.getBytes(),
						msg.getBytes().length, addr, 
						env.getControllerDiscoveryPort()
					);
					while(true) {
						serverSocket.send(msgPacket);
						try {
							Thread.sleep(env.getAliveInterval());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						serverSocket.send(msgPacket);
					}
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
 				
			}
		};
	}
	
	public void start() {
		advertismentService.start();
	}
	
	public void interrupt() {
		advertismentService.interrupt();
	}
	
	public boolean isAlive() {
		return advertismentService.isAlive();
	}
}
