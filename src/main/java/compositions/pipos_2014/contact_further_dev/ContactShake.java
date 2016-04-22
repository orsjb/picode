package compositions.pipos_2014.contact_further_dev;

import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.Mult;
import net.beadsproject.beads.ugens.PolyLimit;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.SendToPI;
import core.Config;
import core.PIPO;
import core.Synchronizer;

public class ContactShake implements PIPO {

	private static final long serialVersionUID = 1L;
	public static final boolean verbose = false;
	public static final int[] scalePitches = {0, 3, 5, 6, 7, 10};	//blues scale
	
	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		System.out.println("The path will be run: " + fullClassName);
		SendToPI.send(fullClassName, new String[]{
				"pisound-009e959c47ef.local", 
				"pisound-009e959c4dbc.local", 
				});
	}
	
	Glide xFactor, yFactor, zFactor;

	@Override
	public void action(DynamoPI d) {
		d.reset();
		d.ac.out.getGainUGen().setValue(1.3f);
		//settings
//		d.pl.setSteal(true);
//		d.pl.setMaxInputs(5);
		//set up Mu responder
		xFactor = new Glide(d.ac, 0, 100);
		yFactor = new Glide(d.ac, 0, 100);
		zFactor = new Glide(d.ac, 0, 100);
		//mu
		d.mu.addListener(new MiniMUListener() {
			@Override
			public void accelData(double x, double y, double z) {
				float scaledX = scaleMU((float)x);
				xFactor.setValue(scaledX);
				float scaledY = scaleMU((float)y);
				yFactor.setValue(scaledY);
				float scaledZ = scaleMU((float)z);
				zFactor.setValue(scaledZ);
			}
		});
		//set responsive behaviours
		////////////////////////////////////////
		//sound elements
		setupChords(d);
		setupApreggiatedPatterns(d);
	}
	
	private float scaleMU(float x) {
		return (float)Math.tanh(x / 250);
	}
	
	//////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////
	private void setupChords(final DynamoPI d) {
		Sample chord = SampleManager.sample(Config.audioDir + "/" + "chords/chord1.wav");
		final GranularSamplePlayer gsp = new GranularSamplePlayer(d.ac, chord);
		gsp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
		gsp.getLoopStartUGen().setValue(500);
		gsp.getLoopEndUGen().setValue((float)chord.getLength() - 500);
		//controls
//		final Glide rndGlide = new Glide(d.ac, 0, 5000);
		final Function rndGlide = new Function(yFactor) {
			@Override
			public float calculate() {
				return Math.abs(x[0]);
			}
		};
		final Glide gsizeGlide = new Glide(d.ac, 30, 5000);
		final Glide gintervalGlide = new Glide(d.ac, 30, 5000);
		final Glide grateGlideMult = new Glide(d.ac, 1, 5000);
		final UGen grateGlide = new Mult(d.ac, xFactor, grateGlideMult);
		gsp.setRandomness(rndGlide);
		gsp.setGrainSize(gsizeGlide);
		gsp.setGrainInterval(gintervalGlide);
		gsp.setRate(grateGlide);
		gsp.setPitch(new Function(grateGlide) {
			@Override
			public float calculate() {
				return x[0] * 2;
			}
		});
		//Choose my chord note based on myID
		int id = getID(d);
		int pitch = scalePitches[id % scalePitches.length];
		gsp.getPitchUGen().setValue(Pitch.mtof(pitch + 60) / Pitch.mtof(60));
		//gain control
		final Envelope genv = new Envelope(d.ac, 0);
		final Gain g = new Gain(d.ac, 1, genv);
		g.addInput(gsp);
		d.ac.out.addInput(g);
//		g.pause(true);
		//set up controller
		d.synch.addBroadcastListener(new Synchronizer.BroadcastListener() {
			@Override
			public void messageReceived(String msg) {
				try {
					if(msg.equals("/PI/chord/on")) {
//						g.pause(false);
						genv.clear();
						genv.addSegment(1, 1000);
						genv.addSegment(0, 5000);//, new PauseTrigger(g));
					}
				} catch(Exception e) {
					//do nothing
				}
			}
		});
	}
	
	/////////////////////////////////////////////////////////////
	public void setupApreggiatedPatterns(final DynamoPI d) {
		final Sample guitar = SampleManager.sample(Config.audioDir + "/" + "guit.wav");
		final PolyLimit pla = new PolyLimit(d.ac, 1, 3);
		pla.setSteal(true);
		d.ac.out.addInput(pla);		
		//minimu input
		MiniMUListener myListener = new MiniMUListener() {
			double prevX = 0, prevY = 0, prevZ = 0, thresh = 90; 
			int timeout = 0, count = 0;
			int nextPitch = 0;
			public void accelData(double x, double y, double z) {
				double xdiff = x - prevX, ydiff = y - prevY, zdiff = z - prevZ;
				double axd = Math.abs(xdiff), ayd = Math.abs(ydiff), azd = Math.abs(zdiff);
				//single combined event
				double accum = Math.abs(xdiff) + Math.abs(ydiff) + Math.abs(zdiff);

				//6 different directions TODO oooooo
				if(axd > ayd && axd > azd) {
					if(axd < 0) {
						//option 1
					} else {
						//option 2
					}
				} else if(ayd > azd) {
					if(ayd < 0) {
						//option 3
					} else {
						//option 4
					}
				} else {
					if(azd < 0) {
						//option 5
					} else {
						//option 6
					}
				}
				
				
//				System.out.println(accum);
				if(accum > thresh) {
//					System.out.println("Improv Madness EVENT!");
					if(count > timeout) {
						//TODO - madness sound miniMu response
						playPluckSound(d, nextPitch++, guitar, pla);
						count = 0;
						d.synch.broadcast("/PI/chord/on");
					}
				}
				count++;
				prevX = x;
				prevY = y; 
				prevZ = z;
			}
		};
		d.mu.addListener(myListener);
	}
	
	
	private void playPluckSound(final DynamoPI d, int pitch, Sample guitar, UGen output) {
		boolean sn = d.rng.nextFloat() < 0.9f;
		int[] pitches = {62, 60, 67, 74, 81, 88, 95};
		//int pitch = pitches[d.myIndex() % pitches.length];
		int note = pitches[((getID(d) / 2 + 1) * pitch) % pitches.length];
//					Noise n = new Noise(d.ac);
		if(!sn) note -= 12;
		
		//sine
		final float ptch = Pitch.mtof(note);  
		Function pitchMod = new Function(yFactor) {
			@Override
			public float calculate() {
				return ptch + x[0] * 100;	
			}
		};
		if(d.rng.nextBoolean()) {
			//sine
			WavePlayer wp = new WavePlayer(d.ac, pitchMod, sn ? Buffer.SINE : Buffer.SINE);
			float gainMax = sn ? d.rng.nextFloat() * 0.05f + 0.03f : d.rng.nextFloat() * 0.03f + 0.02f;
			Envelope genv = null;
			genv = new Envelope(d.ac, 0);
			genv.addSegment(gainMax, 100);
			final Gain g = new Gain(d.ac, 1, genv);
			g.addInput(wp);
			genv.addSegment(gainMax, d.rng.nextFloat() * d.rng.nextFloat() * d.rng.nextFloat() * 200);
			genv.addSegment(0, 2000, new KillTrigger(g));
			output.addInput(g);
		} else {
			//guit...
			Mult gpitchMod = new Mult(d.ac, 1, 1/440f);
			gpitchMod.addInput(pitchMod);
			SamplePlayer sp = new SamplePlayer(d.ac, guitar);
			sp.setRate(gpitchMod);
			Envelope esp = new Envelope(d.ac, 0.08f);
			Gain gn = new Gain(d.ac, 1, esp);
//			sp.setEndListener(new KillTrigger(gn));
			esp.addSegment(0, 500f, new KillTrigger(gn));
			gn.addInput(sp);
			output.addInput(gn);
		}
		
	}
	
	
	
	
	private int getID(DynamoPI d) {
		int i = d.myIndex();
		if(i < 0) i = 0;
		return i;
	}

}