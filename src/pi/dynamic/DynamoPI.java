package pi.dynamic;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Random;

import pi.ConnectionClient;
import core.AudioSetup;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.IOAudioFormat;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.PolyLimit;
import net.beadsproject.beads.ugens.Static;
import net.beadsproject.beads.ugens.WavePlayer;
import de.sciss.net.OSCServer;

public class DynamoPI {
	
	/*
	 * NOTE on access.
	 * 
	 * Currently this is v lazy. Ultimately make these private and wrap as many up
	 * in appropriate wrapper methods. DynamoPI should always be in control of its own state.
	 * Objects that do things to DynamoPI might wander off and get confused. Need to be able to
	 * do reset and access any elements that have been added.
	 * (Unfortunately OSCServer is a bit lame as it doesn't allow access to its listeners).
	 */

	 public final String home = "/home/pi";
	 public final String audioDir = "/audio"; 
	
	 public final AudioContext ac;
	 public final Clock clock;
	 public final Envelope clockInterval;
	 public final PolyLimit pl;
	 
	 public final Hashtable<String, Object> share;
	 public OSCServer oscServer;
	 public final Random rng;
	 
	 public ConnectionClient connectionClient;
	 
	 int nextID = 0;
	
	public static void main(String[] args) throws IOException {

		new DynamoPI(AudioSetup.getAudioContext(args));
	}
	
	public DynamoPI(AudioContext _ac) throws IOException {
		
		System.out.println("Launching DynamoPI!");
		ac = _ac;
		
		//share
		share = new Hashtable<String, Object>();
		//rng
		rng = new Random();
		
		
		clockInterval = new Envelope(ac, 500);
		clock = new Clock(ac, clockInterval);
		pl = new PolyLimit(ac, 1, 3);
		pl.setSteal(true);
		ac.out.addInput(pl);
		ac.out.addDependent(clock);
		ac.start();
		
		//TEMP: test code
		
		clock.addMessageListener(new Bead() {
			public void messageReceived(Bead message) {
				if(clock.getCount() % 64 == 0) {
					WavePlayer wp = new WavePlayer(ac, 500 + rng.nextInt(100), Buffer.SINE);
					Envelope e = new Envelope(ac, 0.1f);
					Gain g = new Gain(ac, 1, e);
					g.addInput(wp);
					ac.out.addInput(g);
					e.addSegment(0, 200, new KillTrigger(g));
				}
			}
		});
		

		
//		//Block until handshake with server is complete
//		System.out.println("Waiting for response from server...");
//		connectionClient = new ConnectionClient();
//		//Now begin sending alive messages in a separate thread
//		connectionClient.beginSendAlive();
//		//socket server (listens to incoming classes)
//		DynamoClassLoader loader = new DynamoClassLoader(ClassLoader.getSystemClassLoader());
//		ServerSocket server = new ServerSocket(1234);
//		//OSC server
//		oscServer = OSCServer.newUsing(OSCServer.UDP, 5555);
//		oscServer.start();
//		//start socket server listening loop
//		while(true) {
//			//must reopen socket each time
//			Socket s = server.accept();
//			Class<? extends PIPO> pipoClass = null;
//			try {
//				InputStream input = s.getInputStream();
//				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//		        int data = input.read();
//		        while(data != -1){
//		            buffer.write(data);
//		            data = input.read();
//		        }
//		        byte[] classData = buffer.toByteArray();
//				Class<?> c = loader.createNewClass(classData);
//				Class<?>[] interfaces = c.getInterfaces();
//				boolean isPIPO = false;
//				for(Class<?> cc : interfaces) {
//					if(cc.equals(PIPO.class)) {
//						isPIPO = true;
//						break;
//					}
//				}
//				if(isPIPO) {
//					pipoClass = (Class<? extends PIPO>)c;
//					System.out.println("new PIPO >> " + pipoClass.getName());
//					//this means we're done with the sequence, time to recreate the classloader to avoid duplicate errors
//					loader = new DynamoClassLoader(ClassLoader.getSystemClassLoader());
//				} 
//			} catch(Exception e) {/* snub it? */
//				System.out.println("Exception Caught trying to read Object from Socket");
//				e.printStackTrace();
//			}
//			if(pipoClass != null) {
//				PIPO pipo = null;
//				try {
//					pipo = pipoClass.newInstance();
//					pipo.action(this);
//					respond(s.getInetAddress());
//				} catch (Exception e) {
//					e.printStackTrace();	//catching all exceptions means that we avert an exception heading up
//											//to audio processes.
//				}
//			}
//			s.close();
//		}
		
	}
	
	private void respond(InetAddress masterAddress) {
//		try {
//			Socket s = new Socket(masterAddress, 4321);
			//TODO send back whatever status info you want here
			//(list of what is connected to "clock", "pl", stored in "share", name of last sent object, etc.)
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public void put(String s, Object o) {
		share.put(s, o);
	}
	
	public Object get(String s) {
		return share.get(s);
	}
	
	public int getInt(String s) {
		return (Integer)share.get(s);
	}

	public float getFloat(String s) {
		return (Float)share.get(s);
	}

	public String getString(String s) {
		return (String)share.get(s);
	}

	public UGen getUGen(String s) {
		return (UGen)share.get(s);
	}

	public Bead getBead(String s) {
		return (Bead)share.get(s);
	}
	
	public String pattern(Bead pattern) {
		clock.addMessageListener(pattern);
		String name = "pattern" + nextID++;
		put(name, pattern);
		System.out.println(name);
		return name;
	}
	
	public String sound(UGen snd) {
		pl.addInput(snd);
		String name = "snd" + nextID++;
		put(name, snd);
		System.out.println(name);
		return name;
	}
	
	public void reset() {
		ac.out.clearDependents();
		ac.out.addDependent(clock);
		ac.out.clearInputConnections();
		ac.out.addInput(pl);
		clock.clearMessageListeners();
		clock.clearInputConnections();
		clock.clearDependents();
		share.clear();
		pl.clearInputConnections();
		pl.clearDependents();
		oscServer.dispose();
		try {
			oscServer = OSCServer.newUsing(OSCServer.UDP, 5555);
			oscServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//This is like reset() except that any sounds currently playing are kept.
	public void resetLeaveSounding() {
		ac.out.clearDependents();
		ac.out.addDependent(clock);
		clock.clearMessageListeners();
		clock.clearInputConnections();
		clock.clearDependents();
		share.clear();
		pl.clearDependents();
		oscServer.dispose();
		try {
			oscServer = OSCServer.newUsing(OSCServer.UDP, 5555);
			oscServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void fadeOutClearSound(float fadeTime) {
		Envelope e = new Envelope(ac, ac.out.getValue());
		e.addSegment(0, fadeTime, new Bead() {
			public void messageReceived(Bead message) {
				pl.clearInputConnections();
				ac.out.clearInputConnections();
				ac.out.addInput(pl);
				ac.out.setGain(new Static(ac, 1));
			}
		});
		ac.out.setGain(e);
	}
		
	public int myIndex() {
		return connectionClient.getId();
	}

}