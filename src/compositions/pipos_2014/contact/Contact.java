package compositions.pipos_2014.contact;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.events.PauseTrigger;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.Mult;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.network.ControllerConnection;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.SendToPI;
import core.Config;
import core.PIPO;
import de.sciss.net.OSCMessage;

public class Contact implements PIPO {

	private static final long serialVersionUID = 1L;
	public static final boolean verbose = false;
	public static final int[] scalePitches = {0, 3, 5, 6, 7, 10};	//blues scale
	
	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{
//				"pisound-009e959c5093.local", 
//				"pisound-009e959c510a.local", 
				"pisound-009e959c47ef.local", 
//				"pisound-009e959c502d.local",
//				"pisound-009e959c50e2.local",
				});
	}

	@Override
	public void action(DynamoPI d) {
		
		d.reset();
		
		
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
		setupApreggiatedPatterns(d);
		
		
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
		g.pause(true);
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
					gainCtrl.clear();
					gainCtrl.addSegment(0.1f, 5000);
				} else if(msg.getName().equals("/PI/noise/off")) {
					gainCtrl.clear();
					gainCtrl.addSegment(0, 10000, new PauseTrigger(g));
				}
			}
		});
	}
	
	private int intervalRange = 100;
	/////////////////////////////////////////////////////////////////
	private void setupSoloInstrument(final DynamoPI d) {
		//create sound
		final Envelope fenv = new Envelope(d.ac, 500);
		final Envelope modFreq = new Envelope(d.ac, 700f);	
		modFreq.addSegment(100f, 30500);
		final Envelope modAmount = new Envelope(d.ac, 800f);
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
					nextInterval = d.rng.nextInt(intervalRange) * d.rng.nextInt(5) + 1;
					genv.addSegment(0.1f + 0.3f * d.rng.nextFloat(), 100);
					int midi = 64 + d.rng.nextInt(5);
					midi = Pitch.forceToScale(midi, Pitch.dorian);	
					fenv.addSegment(Pitch.mtof(midi) + 1 * d.rng.nextFloat(),  d.rng.nextFloat() * 500);
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
					genv.lock(false);
					genv.clear();
					genv.addSegment(0.1f, 5000);
				} else if(msg.getName().equals("/PI/solo/off")) {
					genv.clear();
					genv.addSegment(0, 10000, new Bead() {
						public void messageReceived(Bead message) {
							g.pause(true);
							pattern.pause(true);
						}
					});
					genv.lock(true);
				} else if(msg.getName().equals("/PI/solo/modFreq")) {
					modFreq.clear();
					modFreq.addSegment(((Number)msg.getArg(0)).floatValue(), 600);
				} else if(msg.getName().equals("/PI/solo/modAmount")) {
					modAmount.clear();
					modAmount.addSegment(((Number)msg.getArg(0)).floatValue(), 600);
				} else if(msg.getName().equals("/PI/solo/interval")) {
					intervalRange = ((Number)msg.getArg(0)).intValue();
				}
			}
		});
	}
	
	//////////////////////////////////////////////////////////////////////
	private void setupChords(final DynamoPI d) {
		Sample chord = SampleManager.sample(Config.audioDir + "/" + "chords/chord1.wav");
		final GranularSamplePlayer gsp = new GranularSamplePlayer(d.ac, chord);
		gsp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
		gsp.getLoopStartUGen().setValue(500);
		gsp.getLoopEndUGen().setValue((float)chord.getLength() - 500);
		//controls
		final Glide rndGlide = new Glide(d.ac, 0, 500);
		final Glide gsizeGlide = new Glide(d.ac, 50, 500);
		final Glide gintervalGlide = new Glide(d.ac, 100, 500);
		gsp.setRandomness(rndGlide);
		gsp.setGrainSize(gsizeGlide);
		gsp.setGrainInterval(gintervalGlide);
		//Choose my chord note based on myID
		int id = d.myIndex();
		int pitch = scalePitches[id % scalePitches.length];
		gsp.getPitchUGen().setValue(Pitch.mtof(pitch + 60) / Pitch.mtof(60));
		//gain control
		final Envelope genv = new Envelope(d.ac, 0);
		final Gain g = new Gain(d.ac, 1, genv);
		g.addInput(gsp);
		d.ac.out.addInput(g);
		g.pause(true);
		//set up controller
		d.controller.addListener(new ControllerConnection.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/PI/chord/on")) {
					g.pause(false);
					genv.clear();
					genv.addSegment(2, 3000);
				} else if(msg.getName().equals("/PI/chord/off")) {
					genv.clear();
					genv.addSegment(0, 7000, new PauseTrigger(g));
				} else if(msg.getName().equals("/PI/chord/rnd")) {
					rndGlide.setValue(((Number)msg.getArg(0)).floatValue());
				} else if(msg.getName().equals("/PI/chord/gsize")) {
					gsizeGlide.setValue(((Number)msg.getArg(0)).floatValue());
				} else if(msg.getName().equals("/PI/chord/ginterval")) {
					gintervalGlide.setValue(((Number)msg.getArg(0)).floatValue());
				}
			}
		});
	}
	
	//////////////////////////////////////////////////////////////////////
	boolean improvMadnessOn = false;
	//////////////////////////////////////////////////////////////////////
	private void setupImprovMadness(final DynamoPI d) {
//		SampleManager.group("improv", Config.audioDir + "/" + "improv");
		final Sample squeal = SampleManager.sample(Config.audioDir + "/bjsqueal/bjsqueal.wav");
		final Envelope genv = new Envelope(d.ac, 0);
		final Gain g = new Gain(d.ac, 1, genv);
		d.ac.out.addInput(g);							//fixed audio added
		g.pause(true);
		
		//create a sound
		final GranularSamplePlayer sp = new GranularSamplePlayer(d.ac, squeal);
		//audio rate controllers
		float initFreq = 1f;
		
		sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
		
		final Envelope freqEnv = new Envelope(d.ac, initFreq);
		final Envelope rateEnv = new Envelope(d.ac, 0.001f);
		final Envelope grainIntervalEnv = new Envelope(d.ac, 40);
		final Envelope grainSizeRatio = new Envelope(d.ac, 3);
		final Envelope randomEnv = new Envelope(d.ac, 0.1f);
		sp.setPitch(freqEnv);
		sp.setRate(rateEnv);
		
		Mult grainSizeEnv = new Mult(d.ac, 1, grainSizeRatio);
		grainSizeEnv.addInput(grainIntervalEnv);
		
		sp.setGrainSize(grainSizeEnv);
		sp.setGrainInterval(grainIntervalEnv);
		sp.setRandomness(randomEnv);
		
		//gain envelope
		g.addInput(sp);
		//sound action
		
		//set up OSC responder
		d.controller.addListener(new ControllerConnection.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/PI/madness/on")) {
					g.pause(false);
					improvMadnessOn = true;
					genv.clear();
					genv.addSegment(0.5f, 1000);
				} else if(msg.getName().equals("/PI/madness/off")) {
					genv.clear();
					genv.addSegment(0, 10000, new Bead() {
						public void messageReceived(Bead b) {
							g.pause(true);
							improvMadnessOn = false;
						}
					});
				} else if(msg.getName().equals("/PI/madness/rate")) {
					rateEnv.clear();
					rateEnv.addSegment(((Number)msg.getArg(0)).floatValue(), ((Number)msg.getArg(1)).floatValue());
				} else if(msg.getName().equals("/PI/madness/interval")) {
					grainIntervalEnv.clear();
					grainIntervalEnv.addSegment(((Number)msg.getArg(0)).floatValue(), ((Number)msg.getArg(1)).floatValue());
				} else if(msg.getName().equals("/PI/madness/ratio")) {
					grainSizeRatio.clear();
					grainSizeRatio.addSegment(((Number)msg.getArg(0)).floatValue(), ((Number)msg.getArg(1)).floatValue());
				} else if(msg.getName().equals("/PI/madness/rnd")) {
					randomEnv.clear();
					randomEnv.addSegment(((Number)msg.getArg(0)).floatValue(), ((Number)msg.getArg(1)).floatValue());
				} else if(msg.getName().equals("/PI/madness/pitch")) {
					freqEnv.clear();
					freqEnv.addSegment(((Number)msg.getArg(0)).floatValue(), ((Number)msg.getArg(1)).floatValue());
				}
			}
		});
		//set up MiniMU responder
		MiniMUListener myListener = new MiniMUListener() {
			double accum = 0, prevX = 0, prevY = 0, prevZ = 0;
			double thresh = 10;
			public void accelData(double x, double y, double z) {
				if(!improvMadnessOn) {
					return;
				}
				double xdiff = x - prevX, ydiff = y - prevY, zdiff = z - prevZ;
				accum += xdiff+ydiff+zdiff;
				if(accum > thresh) {
					
					//TODO - something with the position
					
					accum = 0;
				}
			}
		};
		d.mu.addListener(myListener);
		//also run a clock
		Bead pattern = new Bead() {
			public void messageReceived(Bead b) {
				if(improvMadnessOn) {
					if(d.clock.getCount() % 100 == 0) {
						//TODO
					}
				}
			}
		};
		d.pattern(pattern);
	}


	/////////////////////////////////////////////////////////////
	private boolean playArpeggios = false;
	private int intervalLeap = 20;
	/////////////////////////////////////////////////////////////
	public void setupApreggiatedPatterns(final DynamoPI d) {
		//create the pattern
		Bead b = new Bead() {
			int nextInterval = intervalLeap * d.myIndex() + 5;
			int nextPitch = 0;
			public void messageReceived(Bead m) {
				if(!playArpeggios) {
					return;
				}
				if(d.clock.getCount() % nextInterval == 0) {
					int[] pitches = {62, 60, 67, 74, 81, 88, 95, 102, 109, 106};
					
					//int pitch = pitches[d.myIndex() % pitches.length];
					
					int pitch = pitches[((d.myIndex() / 2) * nextPitch++) % pitches.length];
					
//					Noise n = new Noise(d.ac);
					WavePlayer wp = new WavePlayer(d.ac, Pitch.mtof(pitch - 24), d.rng.nextFloat() < 0.9f ? Buffer.SINE : Buffer.SINE);
					float gainMax = d.rng.nextFloat() * 0.5f + 0.5f;
					Envelope genv = null;
					if(d.rng.nextFloat() < 0.3f) {
						genv = new Envelope(d.ac, 0);
						genv.addSegment(gainMax, 10);
					} else {
						genv = new Envelope(d.ac, 0);
					}
					final Gain g = new Gain(d.ac, 1, genv);
					g.addInput(wp);
					genv.addSegment(gainMax, d.rng.nextFloat() * d.rng.nextFloat() * d.rng.nextFloat() * 5000);
					genv.addSegment(0, 3000, new KillTrigger(g));
					d.sound(g);
				}
			}
		};
		//add it
		d.pattern(b);
		d.controller.addListener(new ControllerConnection.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/PI/arpeggio/on")) {
					playArpeggios = true;
				} else if(msg.getName().equals("/PI/arpeggio/off")) {
					playArpeggios = false;
				} else if(msg.getName().equals("/PI/arpeggio/interval")) {
					intervalLeap = ((Number)msg.getArg(0)).intValue();
				}
			}
		});
	}

}
