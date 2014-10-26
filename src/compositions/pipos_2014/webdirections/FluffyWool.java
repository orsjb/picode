package compositions.pipos_2014.webdirections;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.network.NetworkCommunication.Listener;
import pi.sensors.MiniMU.MiniMUListener;
import core.PIPO;
import core.Synchronizer.BroadcastListener;
import de.sciss.net.OSCMessage;

public class FluffyWool implements PIPO {

	private static final long serialVersionUID = 1L;
	Envelope gainEnvelope;
	Envelope carrierFreqEnvelope; // connect to to amount added to function
	Envelope modulatorFreqEnvelope; // connect to modulator freqModulator frequency
	Envelope modDepthEnvelope;
	Gain masterGain;
	Envelope masterGainCtrl;
	Envelope delayTime;
	Envelope birdGain;
	Glide birdRate;
	TapIn delayIn;
	GranularSamplePlayer fluffSp;
	
	DynamoPI d;
	
	int[] blues = {0, 3, 5, 6, 7, 10, 12, 17};
	
	int currentPitch = 0;
	int pitchOff = 0;
	
	int beatInterval = 1;
	
	@Override
	public void action(final DynamoPI d) {
		this.d = d;
		d.reset();
		
		masterGainCtrl = new Envelope(d.ac, 1);
		masterGain = new Gain(d.ac, 1, masterGainCtrl);
		d.sound(masterGain);

		SampleManager.sample("fluff", "audio/Fluffy/fluff.wav");
		SampleManager.sample("nightingale", "audio/Fluffy/nightingale.wav");
		SampleManager.sample("kookaburra", "audio/Fluffy/kookaburra.wav");
		
		//set up the delay
		delayIn = new TapIn(d.ac, 5000);
		delayTime = new Envelope(d.ac, 400f);
		TapOut delayOut = new TapOut(d.ac, delayIn, delayTime);
		Gain delayGain = new Gain(d.ac, 1, 0.3f);
		delayGain.addInput(delayOut);
		delayIn.addInput(delayGain); // feedback
		masterGain.addInput(delayGain); //connect delay output to audio context
		
		setupFM();
		setupMetal();
		setupBirds();
		setupFluffChorus();
		
		//beat
		d.pattern(new Bead() {
			public void messageReceived(Bead msg) {
				if(d.clock.isBeat() && d.clock.getBeatCount() % beatInterval == 0) {
					//TODO
				}
			}
		});
		
		//make a DIAD respond to incoming messages (from server)
		d.communication.addListener(new Listener() {
			@Override
			public void msg(OSCMessage msg) {
				System.out.println("Received messsage: " + msg.getName());
				if(msg.getName().equals("/sinewave")) {
					
				} else if(msg.getName().equals("/launchpad/id")) {
					int id = (Integer)msg.getArg(0);
					playnote(id);
				} else if(msg.getName().equals("/launchpad/tempo")) {
					int id = (Integer)msg.getArg(0);
					//TODO
					
				} else if(msg.getName().equals("/launchpad/pitch")) {
					int id = (Integer)msg.getArg(0);
					pitchOff = blues[id];
				}
			}
		});
		
		
		//make a DIAD respond to incoming messages (from others)
		d.synch.addBroadcastListener(new BroadcastListener() {
			@Override
			public void messageReceived(String s) {
				// TODO
			}
		});
		
		
		//make the sound respond to the sensors
		d.mu.addListener(new MiniMUListener() {

			double lastVal;
			
			@Override
			public void accelData(double x, double y, double z) {
				//TODO
			}
			
			@Override
			public void gyroData(double x, double y, double z) {
				double val = Math.sqrt(x*x + y*y + z*z);
				double rate = Math.abs(val - lastVal);
				lastVal = val;
				rate /= 100.;
				rate -= 1;
				if(rate < 0) rate = 0;
				rate /= 10.;
				birdRate.setValue((float)rate);
				System.out.println("gyro " + rate);
				if(rate == 0) {
					birdGain.clear();
					birdGain.addSegment(0, 300);
				} else {
					birdGain.clear();
					birdGain.addSegment(1, 200);
				}
			}

			@Override
			public void magData(double x, double y, double z) {
//				System.out.println("Mag data: " + x + " " + y + " "  + z);
			}
			
		});

	}
	
	void playnote(int id) {
		if(id == 0) {
			gainEnvelope.clear();
			gainEnvelope.addSegment(0, 5000);
		} else if(id < 8) {
			gainEnvelope.clear();
			gainEnvelope.addSegment(0.05f, 2000);
			int pitch = blues[id-1] + 60;
			float freq = Pitch.mtof(pitch);
			carrierFreqEnvelope.addSegment(freq, 500 * d.rng.nextFloat());
		} else {
			gainEnvelope.clear();
			gainEnvelope.addSegment(0.05f, 2000);
			int pitch = blues[id-8] + 84;
			float freq = Pitch.mtof(pitch);
			carrierFreqEnvelope.addSegment(freq, 500 * d.rng.nextFloat());
		}
	}
	
	void setupFM() {
		//set up FM synth
		modulatorFreqEnvelope = new Envelope(d.ac, 500f);
		WavePlayer freqModulator = new WavePlayer(d.ac, modulatorFreqEnvelope, Buffer.SINE);
		modDepthEnvelope = new Envelope(d.ac, 0f);
		carrierFreqEnvelope = new Envelope(d.ac, 0.0f);
		Function modulationFunction = new Function(freqModulator, carrierFreqEnvelope, modDepthEnvelope) {
			public float calculate() {
				return (x[0] * x[2]) + x[1]; //figure out how to plug carrierEnvelope into this
			}
		};
		WavePlayer carrier = new WavePlayer(d.ac, modulationFunction, Buffer.SAW);
		gainEnvelope = new Envelope(d.ac, 0.0f);
		Gain carrierGain = new Gain(d.ac, 1, gainEnvelope);
		carrierGain.addInput(carrier);
		//plug in
		delayIn.addInput(carrierGain); // connect synth gain to delay
		masterGain.addInput(carrierGain);		
	}
	
	void setupBirds() {
		GranularSamplePlayer sp = new GranularSamplePlayer(d.ac, SampleManager.sample("kookaburra"));
//		GranularSamplePlayer sp = new GranularSamplePlayer(d.ac, SampleManager.sample("nightingale"));
		sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
		sp.getGrainSizeUGen().setValue(40);
		sp.getGrainIntervalUGen().setValue(35);
		sp.getRandomnessUGen().setValue(0.1f);
		birdGain = new Envelope(d.ac, 1);
		Gain g = new Gain(d.ac, 1, birdGain);
		g.addInput(sp);
		masterGain.addInput(g);
		birdRate = new Glide(d.ac, 0, 500);
		sp.setRate(birdRate);
		//plug in
		delayIn.addInput(g); // connect synth gain to delay
		masterGain.addInput(g);	//and main out
	}
	

	private void setupFluffChorus() {
		fluffSp = new GranularSamplePlayer(d.ac, SampleManager.sample("fluff"));
		delayIn.addInput(fluffSp); // connect synth gain to delay
		masterGain.addInput(fluffSp);
		//add to delay
	}

	private void setupMetal() {
		// TODO
	}

}
