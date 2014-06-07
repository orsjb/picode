package compositions.pipos_2014.contact;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.PauseTrigger;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.network.ControllerConnection;
import pi.sensors.MiniMU.MiniMUListener;
import core.Config;
import core.PIPO;
import de.sciss.net.OSCMessage;

public class Contact implements PIPO {

	private static final long serialVersionUID = 1L;
	
	public static final boolean verbose = false;

	@Override
	public void action(DynamoPI d) {
		
		//set responsive behaviours
		////////////////////////////////////////
		
		//filtered white noise going to sparkle...
		setupFilteredNoise(d);
		
		//solo instrument...
		setupSoloInstrument(d);
		
		//chord instruments - scatter, free improv bleeping... (use a couple of samples + granulation)
		setupChords(d);
		
		//scatter with misc samples + bleeps
		setupImprovMadness(d);
		
		
		//smooth noise gentle glitch
		
		
		//arpeggiated patterns
		
		
		
	}

	//////////////////////////////////////////////////////////////
	private void setupFilteredNoise(final DynamoPI d) {
		//controllers
		final Glide freqCtrl = new Glide(d.ac, 500);
		final Envelope gainCtrl = new Envelope(d.ac, 0);
		//set up signal chain
		Noise n = new Noise(d.ac);
		BiquadFilter bf = new BiquadFilter(d.ac, 1);
		bf.addInput(n);
		bf.setFrequency(freqCtrl);
		bf.setQ(0.9f);
		final Gain g = new Gain(d.ac, 1, gainCtrl);
		g.addInput(bf);
		d.ac.out.addInput(g);		//add the sound to ac.out since we don't want it killed by PolyLimit.
		//get listening to data
		MiniMUListener myListener = new MiniMUListener() {
			public void accelData(double x, double y, double z) {
				String AccString = String.format("MiniMu Acc X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
				System.out.println(AccString);
				freqCtrl.setValue(((float)Math.abs(x) * 15f) % 5000f + 100f);
			}
			public void gyroData(double x, double y, double z) {
				String GyrString = String.format("MiniMu Gyr X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
				System.out.println(GyrString);
			}
		};
		d.mu.addListener(myListener);
		//set this whole thing to fade in and out on messages
		d.controller.addListener(new ControllerConnection.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/PI/noise/on")) {
					g.pause(false);
					gainCtrl.addSegment(0.1f, 5000);
				} else if(msg.getName().equals("/PI/noise/off")) {
					gainCtrl.addSegment(0, 10000, new PauseTrigger(g));
				}
			}
		});
	}
	
	/////////////////////////////////////////////////////////////////
	private void setupSoloInstrument(final DynamoPI d) {
		//create sound
		Envelope fenv = new Envelope(d.ac, 500);
		Envelope modFreq = new Envelope(d.ac, 200f);	
		modFreq.addSegment(100f, 30500);
		Envelope modAmount = new Envelope(d.ac, 0f);
		modAmount.addSegment(200f, 1500);
		WavePlayer mod = new WavePlayer(d.ac, modFreq, Buffer.SINE);
		Function freqmod = new Function(fenv, mod, modAmount) {
			@Override
			public float calculate() {
				return x[0] + (x[1] * x[2]); 
			}
		};
		WavePlayer wp = new WavePlayer(d.ac, freqmod, Buffer.SINE);
		final Envelope genv = new Envelope(d.ac, 0);
		final Gain g = new Gain(d.ac, 1, genv);
		g.addInput(wp);
		d.ac.out.addInput(g);	
		g.pause(true);
		//the pattern
		final Bead pattern = new Bead() {
			int nextInterval = 1;
			public void messageReceived(Bead message) {
				if(d.clock.getCount() % nextInterval == 0) {
					nextInterval = d.rng.nextInt(100) * d.rng.nextInt(5) + 1;
					Envelope genv = (Envelope)d.get("genv");
					Envelope fenv = (Envelope)d.get("fenv");
					genv.addSegment(0.7f + 0.3f * d.rng.nextFloat(), 10);
					int midi = 64 + d.rng.nextInt(5);
					midi = Pitch.forceToScale(midi, Pitch.dorian);	
					fenv.addSegment(Pitch.mtof(midi) + 5 * d.rng.nextFloat(),  d.rng.nextFloat() * 5000);
				}
			}
		};
		pattern.pause(true);
		d.pattern(pattern);
		//set up the OSC listeners
		d.controller.addListener(new ControllerConnection.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/PI/solo/on")) {
					pattern.pause(false);
					g.pause(false);
					genv.addSegment(0.1f, 5000);
				} else if(msg.getName().equals("/PI/solo/off")) {
					genv.addSegment(0, 10000, new Bead() {
						public void messageReceived(Bead message) {
							g.pause(true);
							pattern.pause(true);
						}
					});
				}
			}
		});
	}
	
	//////////////////////////////////////////////////////////////////////
	private void setupChords(final DynamoPI d) {
		Sample chord = SampleManager.sample(Config.audioDir + "/" + "chords/chord1");
		GranularSamplePlayer gsp = new GranularSamplePlayer(d.ac, chord);
		
	}
	
	//////////////////////////////////////////////////////////////////////
	boolean improvMadnessOn = false;
	//////////////////////////////////////////////////////////////////////
	private void setupImprovMadness(final DynamoPI d) {
		
		SampleManager.group("improv", Config.audioDir + "/" + "improv");
		
		
		final Envelope genv = new Envelope(d.ac, 0);
		final Gain g = new Gain(d.ac, 1, genv);
		d.ac.out.addInput(g);							//fixed audio added
		g.pause(true);
		//set up OSC responder
		d.controller.addListener(new ControllerConnection.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/PI/madness/on")) {
					g.pause(false);
					improvMadnessOn = true;
					genv.addSegment(1, 1000);
				} else if(msg.getName().equals("/PI/madness/off")) {
					genv.addSegment(0, 10000, new Bead() {
						public void messageReceived(Bead b) {
							g.pause(true);
							improvMadnessOn = false;
						}
					});
				}
			}
		});
		//set up MiniMU responder
		MiniMUListener myListener = new MiniMUListener() {
			double accum = 0, prevX = 0, prevY = 0, prevZ = 0;
			double thresh = 10;
			public void accelData(double x, double y, double z) {
				double xdiff = x - prevX, ydiff = y - prevY, zdiff = z - prevZ;
				accum += xdiff+ydiff+zdiff;
				if(accum > thresh) {
					improvSoundEvent();
					accum = 0;
				}
				
			}
			private void improvSoundEvent() {
				
				GranularSamplePlayer sp = new GranularSamplePlayer(d.ac, SampleManager.randomFromGroup("improv"));
				Envelope env = new Envelope(d.ac, 1);
				

				Envelope freqEnv = new Envelope(d.ac, 1);
				Envelope rateEnv = new Envelope(d.ac, 1);
				Envelope grainSizeEnv = new Envelope(d.ac, 30);
				Envelope grainRateEnv = new Envelope(d.ac, 20);
				
				
				//TODO
				
				Gain g = new Gain(d.ac, 1, env);
				g.addInput(sp);
				
				
				d.sound(g);
			}
		};
		d.mu.addListener(myListener);
	}

}
