package pi;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Hashtable;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.IOAudioFormat;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.PolyLimit;
import net.beadsproject.beads.ugens.SamplePlayer;
import de.sciss.net.OSCChannel;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;


public class Main {

	static String home = "/home/pi";
	static String audioDir = "/audio";
	
	static AudioContext ac;
	static Clock clock;
	static Envelope clockInterval;
	static PolyLimit pl;
	
	public class Sound {
		GranularSamplePlayer sp;
		Glide rate, grainSize, grainInterval;
		Envelope gain;
		Gain g;
		
		public Sound(String sample) {
			sp = new GranularSamplePlayer(ac, SampleManager.sample(sample));
			rate = new Glide(ac, 1);
			gain = new Envelope(ac, 1);
			grainSize = new Glide(ac, 30);
			grainInterval = new Glide(ac, 20);
			sp.setGrainInterval(grainInterval);
			sp.setGrainSize(grainSize);
			sp.setRate(rate);
			g = new Gain(ac, 1, gain);
			g.addInput(sp);
			pl.addInput(g);
		}
	}
	
	static Hashtable<String, Sound> sounds;
	
	/*
	 * Most elementary sampler
	 */
	
	public static void main(String[] args) throws IOException {
		//setup
		if(System.getProperty("os.name").startsWith("Mac")) {
			home = ".";
		}
		sounds = new Hashtable<String, Sound>();
		SampleManager.group("audio", home + audioDir);
		//audio
		ac = new AudioContext(new JavaSoundAudioIO(8192), 8192, new IOAudioFormat(22000, 16, 0, 1));
		clockInterval = new Envelope(ac, 500);
		clock = new Clock(ac, clockInterval);
		pl = new PolyLimit(ac, 1, 3);
		pl.setSteal(true);
		ac.out.addInput(pl);
		ac.out.addDependent(clock);
		clock.addMessageListener(new Bead() {
			public void messageReceived(Bead msg) {
				if(clock.isBeat()) {
					SamplePlayer sp = new SamplePlayer(ac, SampleManager.randomFromGroup("audio"));
					pl.addInput(sp);
				}
			}
		}); 
		ac.start();
		
		//OSCServer
		OSCServer serv = OSCServer.newUsing(OSCChannel.UDP, 5555);
		serv.addOSCListener(new OSCListener() {
			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
				if       (msg.getName() == "+") {
					oneHit(msg);
				} else if(msg.getName() == ">") {
					startSound(msg);
				} else if(msg.getName() == "v") {
					modSound(msg);
				} else if(msg.getName() == "=") {
					startSeq(msg);
				} else if(msg.getName() == "-") {
					delete(msg);
				} else if(msg.getName() == "0") {
					deleteAll(msg);
				} else if(msg.getName() == "_") {
					synch(msg);
				}
			}

			
		});
		serv.start();
	}

	public static void synch(OSCMessage msg) {
		//args: start-tempo [new-tempo interval ....] 
		// TODO Auto-generated method stub
		
	}

	public static void deleteAll(OSCMessage msg) {
		// TODO Auto-generated method stub
	}

	public static void delete(OSCMessage msg) {
		//args: name
		// TODO Auto-generated method stub
		
	}

	public static void startSeq(OSCMessage msg) {
		//args: 
		// TODO Auto-generated method stub
		
	}

	public static void modSound(OSCMessage msg) {
		//args: name [modname modval...]
		// TODO Auto-generated method stub
		
	}

	public static void startSound(OSCMessage msg) {
		//args: name, rate, gain, grainsize, grain interval, [loop start, loop end]
		// TODO Auto-generated method stub
		
	}

	private static void oneHit(OSCMessage msg) {
		//args: name, rate, gain, grainsize, grain interval, [sustain decay]
		// TODO Auto-generated method stub
		
	}
}
