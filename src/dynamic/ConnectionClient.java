package dynamic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

//NOTE - To run on PI

public class ConnectionClient {

	//DEBUG: entry point
	/*public static void main(String[] args) {
		try {
			ConnectionClient cc = new ConnectionClient();
			cc.beginSendAlive();
			//Keep this thread busy to avoid program termination
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	private static final String masterConfPath = "conf/master.conf";
	private static final int announceInterval = 500; //How often the PI announces itself while shaking hands with server
	private static final int aliveInterval = 3000; //How often the PI sends an alive message to the server

	public static final int sendPort = 2224;		//port to send on
	public static final int receivePort = 2225;		//port to listen on

	private final String masterIP;
	private final String localMAC;
	private int localId;
	private String localName; // The name of this particular PI. Assigned by master server.
	private OSCServer oscs;
	private Object confirmedLock;
	private boolean confirmed;
	private Thread aliveWorker;
	private Object aliveLock;
	private boolean sendingAlive;
	private boolean responded;
	private int problemsCount;

	public ConnectionClient() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(masterConfPath));
		masterIP = reader.readLine();
		reader.close();

		//Get MAC
		byte[] mac = getMAC();
		StringBuilder builder = new StringBuilder();

		for (byte a : mac)
			builder.append(String.format("%02x:", a));
		localMAC = builder.substring(0, builder.length() - 1);

		oscs = OSCServer.newUsing(OSCServer.UDP, receivePort);
		confirmedLock = new Object();
		aliveLock = new Object();
		confirmed = false;
		sendingAlive = false;
		oscs.start();

		oscs.addOSCListener(new OSCListener() {
			public void messageReceived(OSCMessage m, SocketAddress sender, long time) {
				synchronized (confirmedLock) {
					if (!confirmed
						&& m.getName().equals("/PI/hshake/respond")
						&& m.getArgCount() == 3
						&& m.getArg(0).equals(localMAC)) {
							confirmed = true;
							responded = true;
							localName = (String)m.getArg(1);
							Integer boxedId = (Integer)m.getArg(2);
							localId = boxedId;
							System.out.println("Respond message received. Connection confirmed. My name is " + localName + " and my ID is " + boxedId.toString());
					}
				}

				synchronized (aliveLock) {
					if (m.getName().equals("/PI/alive/acknowledge")
						&& m.getArgCount() == 1
						&& m.getArg(0).equals(localMAC)) {
							responded = true;
							System.out.println("Acknowledge message received.");
					}
				}
			}
		});

		//DEBUG:
		//oscs.dumpOSC( OSCChannel.kDumpText, System.out );

		int announceCount = 0;
		synchronized (confirmedLock) {
			while (!confirmed) {
				oscs.send(new OSCMessage("/PI/hshake/announce", new Object[] {localMAC}), new InetSocketAddress(masterIP, sendPort));
				System.out.println("sent announce message");
				/*if (++announceCount == 10) //Announced 10 times without response, restart
					rebootPI();*/
				try {
					confirmedLock.wait(announceInterval);
				} catch (InterruptedException e) {}
			}
		}

		aliveWorker = new Thread() {
			public void run() {
				synchronized (aliveLock) {
					problemsCount = 0;
					responded = true;
					while (sendingAlive) {
						if (responded) {
							responded = false;
							problemsCount = 0;
						} else {
							System.out.println("Server not responding...");
							/*if (++problemsCount == 4) //We have missed 4 acknowledgments in a row, restart
								rebootPI();
							}*/
						}

						try {
							oscs.send(new OSCMessage("/PI/alive/announce", new Object[] {localMAC, localId}), new InetSocketAddress(masterIP, sendPort));
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							aliveLock.wait(aliveInterval);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
	}

	private byte[] getMAC() {
		try {
			NetworkInterface netInterface;
			if (System.getProperty("os.name").startsWith("Mac OS")) {
				netInterface = NetworkInterface.getByName("en1");
			} else {
				netInterface = NetworkInterface.getByName("wlan0");
			}
			return netInterface.getHardwareAddress();
		} catch (Exception e) { //WiFi isn't up yet, wait
			System.out.println("still waiting for wifi...");
			try {
				Thread.sleep(2000);
				return getMAC();								//TODO this is crazy style
			} catch (InterruptedException e1) {

			}
		}

		return null;
	}

	private void rebootPI() {
		try {
			//http://stackoverflow.com/questions/1410741/want-to-invoke-a-linux-shell-command-from-java
			Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","sudo reboot"}).waitFor();
		} catch (Exception e) {}
	}

	public void beginSendAlive() {
		sendingAlive = true;
		aliveWorker.start();
	}

	public void endSendAlive() {
		synchronized (aliveLock) {
			sendingAlive = false;
		}
	}

	public String getName() {
		return localName;
	}

	public int getId() {
		return localId;
	}
}