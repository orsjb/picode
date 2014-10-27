package compositions.pipos_2014.webdirections;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.events.PauseTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.Mult;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.Static;
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
	Envelope birdGainEnv;
	Gain birdGain;
	Glide birdRate;
	TapIn delayIn;
	GranularSamplePlayer fluffSp;
	GranularSamplePlayer birdSample;
	
	DynamoPI d;
	
	int[] blues = {0, 3, 5, 6, 7, 10, 12, 15, 17, 18, 19, 22, 24, 27, 29, 30, 31, 34, 36, 39, 41, 42, 43, 46, 48};
	int[] mel = {0, 0, 0, 1, 2, 3, 0, 0, 0, 1, 2, 3, 0, 3, 3, 1, 2, 4, 4, 4, 3, 2, 1, 12, 13, 14, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11, 11, 11, 12, 9, 7, 5, 3, 2, 0, 0};
	int[] offsets = {0, 5, 7, 12, 17, 19, 24, 29};
	
	String birdState = "off";
	
	int currentNote = -1;
	int currentStep = 0;
	int pitchOff = 0;
	
	int beatInterval = 0;
	
	@Override
	public void action(final DynamoPI d) {
		this.d = d;
		d.reset();
		
		masterGainCtrl = new Envelope(d.ac, 0.5f);
		masterGain = new Gain(d.ac, 1, masterGainCtrl);
		d.sound(masterGain);

		SampleManager.sample("fluff", "audio/Fluffy/fluff.wav");
		SampleManager.sample("nightingale", "audio/Fluffy/nightingale.wav");
		SampleManager.sample("kookaburra", "audio/Fluffy/kookaburra.wav");
		SampleManager.sample("powerchord", "audio/Fluffy/powerchord.wav");
		
		//set up the delay
		delayIn = new TapIn(d.ac, 5000);
		delayTime = new Envelope(d.ac, 150f);
		TapOut delayOut = new TapOut(d.ac, delayIn, delayTime);
		Gain delayGain = new Gain(d.ac, 1, 0.4f);
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
				if(beatInterval == 0) return;
				if(d.clock.getCount() % (8 - beatInterval) == 0) {
					currentStep++;
					playnote();
				}
			}
		});
		d.clock.setTicksPerBeat(8);
		d.clock.getIntervalEnvelope().setValue(300);
		
		//make a DIAD respond to incoming messages (from server)
		d.communication.addListener(new Listener() {
			@Override
			public void msg(OSCMessage msg) {
				System.out.println("Received messsage: " + msg.getName());
				if(msg.getName().equals("/bird")) {
					birdState = "bird";
					birdSample.setSample(SampleManager.sample("nightingale"));
				} else if(msg.getName().equals("/kook")) {
					birdState = "kook";
					birdSample.setSample(SampleManager.sample("kookaburra"));
					
				} else if(msg.getName().equals("/nobird")) {
					birdState = "off";
					birdGainEnv.clear();
					birdGainEnv.addSegment(1, 200, new PauseTrigger(birdGain));
				} else if(msg.getName().equals("/launchpad/id")) {
					
					
					
					int id = (Integer)msg.getArg(0);
					currentNote = id - 1;
					playnote();
				} else if(msg.getName().equals("/launchpad/tempo")) {
					int id = (Integer)msg.getArg(0);
					//play metal sounds
					playmetal(id);
				} else if(msg.getName().equals("/launchpad/pitch")) {
					int id = (Integer)msg.getArg(0);
					pitchOff = offsets[id];
					playnote();
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
			}
			
			@Override
			public void gyroData(double x, double y, double z) {
				double val = Math.sqrt(x*x + y*y + z*z);
				double rate = Math.abs(val - lastVal);
				rate /= 100.;
				lastVal = val;
				//change the birds playback rate
				if(!birdState.equals("off")) {
					double birdsRateVal = rate - 1;
					if(birdsRateVal < 0) birdsRateVal = 0;
					birdsRateVal /= 10.;
					birdRate.setValue((float)birdsRateVal);
					if(rate == 0) {
						birdGainEnv.clear();
						birdGainEnv.addSegment(0, 300, new PauseTrigger(birdGain));
					} else {
						birdGain.pause(false);
						birdGainEnv.clear();
						birdGainEnv.addSegment(1, 200);
					}
				}
				//always change the beat interval
				double tmp = (rate) / 10.;
				beatInterval = (int)tmp;
				if(beatInterval < 0) beatInterval = 0;
				if(beatInterval > 7) beatInterval = 7;
				System.out.println("Rate: " + rate + "... Beat Interval: " + beatInterval);
			}

			@Override
			public void magData(double x, double y, double z) {
			}
			
		});
	}
	
	void launchpadMsg(int row, int col, boolean push) {
		if(push) {
			if(row == 0) {
				//top row - tempo stuff
				int tempoIndex = col;
				
				//TODO
				
			} else if(col == 8) {
				//right column - pitch stuff
				int pitchIndex = row - 1;
				
				//TODO
				
			} else {
				//main grid
				int group = col / 2;
				int id = row - 1;
				if(col % 2 == 1) {
					id += 8;
				}
				//send ID to group

				//TODO
				
			}
		}
	}
	
	void playmetal(int id) {
		
		SamplePlayer sp = new SamplePlayer(d.ac, SampleManager.sample("powerchord"));
		sp.getPitchUGen().setValue( Pitch.mtof(blues[id] + 60) / Pitch.mtof(60) );
		masterGain.addInput(sp);
		delayIn.addInput(sp);
		
	}
	
	void playvoice(int id) {
		
		SamplePlayer sp = new SamplePlayer(d.ac, SampleManager.sample("fluff"));
		
		float rate = Pitch.mtof(blues[id] + 60) / Pitch.mtof(60);
		Envelope pitchEnv = new Envelope(d.ac, rate);
		pitchEnv.addSegment(rate, 2000);
		pitchEnv.addSegment(0, 2000, new KillTrigger(sp));
		
		delayIn.addInput(sp);
		masterGain.addInput(sp);
		
	}
	
	void playnote() {
		if(currentNote == -1) {
			gainEnvelope.clear();
			gainEnvelope.addSegment(0, 5000);
		} else {
			gainEnvelope.clear();
			gainEnvelope.addSegment(0.05f, 2000);
			int index = currentNote + mel[currentStep % mel.length];
			int pitch = blues[index % blues.length];
			pitch += 60 + pitchOff;
			float freq = Pitch.mtof(pitch);
			float glide = 0;
			if(d.rng.nextFloat() < 0.3f) glide = 400 * d.rng.nextFloat() * d.rng.nextFloat() * d.rng.nextFloat(); 
			carrierFreqEnvelope.addSegment(freq, glide);
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
		//custom buffer, 2 parts sine to 1 part saw
		Buffer buf = new Buffer(2048);
		for(int i = 0; i < buf.buf.length; i++) {
			buf.buf[i] = 0.7f * Buffer.SINE.getValueFraction((float)i / buf.buf.length) + 0.5f * Buffer.SINE.getValueFraction((float)i / buf.buf.length);
		}
		WavePlayer carrier1 = new WavePlayer(d.ac, modulationFunction, buf);
		gainEnvelope = new Envelope(d.ac, 0.0f);
		Gain carrierGain1 = new Gain(d.ac, 1, gainEnvelope);
		carrierGain1.addInput(carrier1);
		//plug in
		delayIn.addInput(carrierGain1); // connect synth gain to delay
		masterGain.addInput(carrierGain1);		
	}
	
	void setupBirds() {
		birdSample = new GranularSamplePlayer(d.ac, SampleManager.sample("kookaburra"));
//		GranularSamplePlayer sp = new GranularSamplePlayer(d.ac, SampleManager.sample("nightingale"));
		birdSample.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
		birdSample.getGrainSizeUGen().setValue(40);
		birdSample.getGrainIntervalUGen().setValue(35);
		birdSample.getRandomnessUGen().setValue(0.1f);
		birdGainEnv = new Envelope(d.ac, 0);
		birdGain = new Gain(d.ac, 1, birdGainEnv);
		birdGain.addInput(birdSample);
		birdRate = new Glide(d.ac, 0, 500);
		birdSample.setRate(birdRate);
		//plug in
		delayIn.addInput(birdGain); // connect synth gain to delay
		masterGain.addInput(birdGain);	//and main out
	}
	

	private void setupFluffChorus() {
		fluffSp = new GranularSamplePlayer(d.ac, SampleManager.sample("fluff"));
//		delayIn.addInput(fluffSp); // connect synth gain to delay
//		masterGain.addInput(fluffSp);
		//add to delay
	}

	private void setupMetal() {
		// TODO
	}

}