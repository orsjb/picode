package compositions.pipos_2014.webdirections;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.BufferFactory;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.network.NetworkCommunication.Listener;
import pi.sensors.MiniMU.MiniMUListener;
import core.PIPO;
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
	Envelope filtFreq;
	Envelope rhythmFreq;
	
	DynamoPI d;
	
	int[] blues = {0, 3, 5, 6, 7, 10, 12, 17};
	
	int currentPitch;
	int pitchOff;
	
	@Override
	public void action(final DynamoPI d) {
		this.d = d;
		d.reset();
		
		masterGainCtrl = new Envelope(d.ac, 0);
		masterGain = new Gain(d.ac, 1, masterGainCtrl);
		d.sound(masterGain);

		SampleManager.sample("fluffy", "audio/fluff.wav");
		
		setupFM();
		setupFluffy();
		
		//make a DIAD respond to incoming messages
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
					rhythmFreq.clear();
					rhythmFreq.addSegment((id * 2) + 0.5f, 5000);
					
				}
			}
		});
		
		//do some funny pitch things
		
		
		//make the sound respond to the sensors
		d.mu.addListener(new MiniMUListener() {

			@Override
			public void accelData(double x, double y, double z) {
				
				
				
				if(z < 0) { 	//if the DIAD is upside-down
					masterGainCtrl.clear();
					masterGainCtrl.addSegment(0, 5000f); //fade out
				} else {
					masterGainCtrl.clear();
					masterGainCtrl.addSegment(1, 5000f); //fade in
				}
			}

			@Override
			public void gyroData(double x, double y, double z) {
				//TODO
				double val = Math.sqrt(x*x + y*y + z*z);
				
			}

			@Override
			public void magData(double x, double y, double z) {
				System.out.println("Mag data: " + x + " " + y + " "  + z);
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
				
				Buffer buf = new Buffer(512);
				for(int i = 0; i < 256; i++) {
					buf.buf[i] = 1;
				}
				rhythmFreq = new Envelope(d.ac, 1);
				WavePlayer rhythm = new WavePlayer(d.ac, rhythmFreq, buf);
				Gain rhythmGain = new Gain(d.ac, 1, rhythm);
				rhythmGain.addInput(carrierGain);
				
				
				TapIn delayIn = new TapIn(d.ac, 5000);
				delayIn.addInput(rhythmGain); // connect synth gain to delay
				delayTime = new Envelope(d.ac, 400f);
				TapOut delayOut = new TapOut(d.ac, delayIn, delayTime);
				Gain delayGain = new Gain(d.ac, 1, 0.6f);
				delayGain.addInput(delayOut);
				delayIn.addInput(delayGain); // feedback

				
				filtFreq = new Envelope(d.ac, 500f);
				BiquadFilter bq = new BiquadFilter(d.ac, BiquadFilter.Type.NOTCH, filtFreq, 1);
				bq.addInput(rhythmGain);
				bq.addInput(delayGain); //connect delay output to audio context
				masterGain.addInput(bq);
				
	}
	
	void setupFluffy() {
		GranularSamplePlayer sp = new GranularSamplePlayer(d.ac, SampleManager.sample("fluffy"));
		Gain g = new Gain(d.ac, 1, sp);
		
	}

}
