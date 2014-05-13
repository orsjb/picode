package pi.dynamic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Random;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.PolyLimit;
import net.beadsproject.beads.ugens.Static;
import pi.network.ControllerConnection;
import pi.sensors.MiniMU;
import pi.synch.Synchronizer;
import core.AudioSetup;
import core.Config;
import core.PIPO;

public class DynamoPI {

	// audio stuffs
	public final AudioContext ac;
	public final Clock clock;
	public final Envelope clockInterval;
	public final PolyLimit pl;
	boolean audioOn = false;

	// sensor stuffs
	public final MiniMU mu;

	// shared data
	public final Hashtable<String, Object> share = new Hashtable<String, Object>();
	int nextElementID = 0;

	// random number generator
	public final Random rng = new Random();

	// network stuff
	public ControllerConnection controller;
	public Synchronizer synch;

	public static void main(String[] args) throws IOException {
		new DynamoPI(AudioSetup.getAudioContext(args));
	}

	public DynamoPI(AudioContext _ac) throws IOException {
		ac = _ac;
		// default audio setup (note we don't start the audio context yet)
		clockInterval = new Envelope(ac, 500);
		clock = new Clock(ac, clockInterval);
		pl = new PolyLimit(ac, 1, 4);
		pl.setSteal(true);
		ac.out.addInput(pl);
		ac.out.addDependent(clock);
		System.out.println("DynamoPI audio setup complete.");
		// sensor setup
		mu = new MiniMU();
		mu.start();
		// start the connection
		controller = new ControllerConnection();
		synch = new Synchronizer();
		// start listening for code
		startListeningForCode();
	}

	private void startListeningForCode() {
		new Thread() {
			public void run() {
				try {
					// socket server (listens to incoming classes)
					DynamoClassLoader loader = new DynamoClassLoader(ClassLoader.getSystemClassLoader());
					ServerSocket server = new ServerSocket(Config.codeToPIPort);
					// start socket server listening loop
					while (true) {
						// must reopen socket each time
						Socket s = server.accept();
						Class<? extends PIPO> pipoClass = null;
						try {
							InputStream input = s.getInputStream();
							ByteArrayOutputStream buffer = new ByteArrayOutputStream();
							int data = input.read();
							while (data != -1) {
								buffer.write(data);
								data = input.read();
							}
							byte[] classData = buffer.toByteArray();
							Class<?> c = loader.createNewClass(classData);
							Class<?>[] interfaces = c.getInterfaces();
							boolean isPIPO = false;
							for (Class<?> cc : interfaces) {
								if (cc.equals(PIPO.class)) {
									isPIPO = true;
									break;
								}
							}
							if (isPIPO) {
								pipoClass = (Class<? extends PIPO>) c;
								System.out.println("new PIPO >> " + pipoClass.getName());
								// this means we're done with the sequence, time
								// to
								// recreate
								// the classloader to avoid duplicate errors
								loader = new DynamoClassLoader(ClassLoader.getSystemClassLoader());
							}
						} catch (Exception e) {/* snub it? */
							System.out.println("Exception Caught trying to read Object from Socket");
							e.printStackTrace();
						}
						if (pipoClass != null) {
							PIPO pipo = null;
							try {
								pipo = pipoClass.newInstance();
								pipo.action(DynamoPI.this);
							} catch (Exception e) {
								e.printStackTrace(); // catching all exceptions
													 // means that we avert an exception
													 // heading up to audio processes.
							}
						}
						s.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	public void put(String s, Object o) {
		share.put(s, o);
	}

	public Object get(String s) {
		return share.get(s);
	}

	public int getInt(String s) {
		return (Integer) share.get(s);
	}

	public float getFloat(String s) {
		return (Float) share.get(s);
	}

	public String getString(String s) {
		return (String) share.get(s);
	}

	public UGen getUGen(String s) {
		return (UGen) share.get(s);
	}

	public Bead getBead(String s) {
		return (Bead) share.get(s);
	}

	public String pattern(Bead pattern) {
		clock.addMessageListener(pattern);
		String name = "pattern" + nextElementID++;
		put(name, pattern);
		System.out.println(name);
		return name;
	}

	public String sound(UGen snd) {
		pl.addInput(snd);
		String name = "snd" + nextElementID++;
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
		mu.clearListeners();
		controller.clearListeners();
	}

	// This is like reset() except that any sounds currently playing are kept.
	public void resetLeaveSounding() {
		ac.out.clearDependents();
		ac.out.addDependent(clock);
		clock.clearMessageListeners();
		clock.clearInputConnections();
		clock.clearDependents();
		share.clear();
		pl.clearDependents();
		mu.clearListeners();
		controller.clearListeners();
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
		return controller.getID();
	}

}