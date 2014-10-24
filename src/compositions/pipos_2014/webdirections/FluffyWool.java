package compositions.pipos_2014.webdirections;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
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
	
	DynamoPI d;
	
	int[] blues = {0, 3, 5, 6, 7, 10, 12, 17};
	
	@Override
	public void action(final DynamoPI d) {
		this.d = d;
		d.reset();
		masterGainCtrl = new Envelope(d.ac, 0);
		masterGain = new Gain(d.ac, 1, masterGainCtrl);
		d.sound(masterGain);

		setupFM();
		
		
		
		
		//make a DIAD respond to incoming messages
		d.communication.addListener(new Listener() {
			@Override
			public void msg(OSCMessage msg) {
				System.out.println("Received messsage: " + msg.getName());
				if(msg.getName().equals("/sinewave")) {
					
				} else if(msg.getName().equals("/launchpad/id")) {
					int id = (Integer)msg.getArg(0);
					playnote(id);
				}
			}
		});
		
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
			
		});

		
		
		
	}
	
	void playnote(int id) {
		if(id == 0) {
			gainEnvelope.clear();
			gainEnvelope.addSegment(0, 5000);
		} else if(id < 8) {
			gainEnvelope.clear();
			gainEnvelope.addSegment(0.1f, 2000);
			int pitch = blues[id-1] + 72;
			float freq = Pitch.mtof(pitch);
			carrierFreqEnvelope.addSegment(freq, 500 * d.rng.nextFloat());
		} else {
			gainEnvelope.clear();
			gainEnvelope.addSegment(0.1f, 2000);
			int pitch = blues[id-8] + 12;
			float freq = Pitch.mtof(pitch) / 32f;
			modulatorFreqEnvelope.addSegment(freq, 500);
		}
		
		
	}
	
	void setupFM() {
		//set up FM synth
				modulatorFreqEnvelope = new Envelope(d.ac, 0.0f);
				WavePlayer freqModulator = new WavePlayer(d.ac, modulatorFreqEnvelope, Buffer.SINE);
				modDepthEnvelope = new Envelope(d.ac, 50f);
				carrierFreqEnvelope = new Envelope(d.ac, 0.0f);
				Function modulationFunction = new Function(freqModulator, carrierFreqEnvelope, modDepthEnvelope) {
					public float calculate() {
						return (x[0] * x[2]) + x[1]; //figure out how to plug carrierEnvelope into this
					}
				};
				WavePlayer carrier = new WavePlayer(d.ac, modulationFunction, Buffer.SINE);
				gainEnvelope = new Envelope(d.ac, 0.0f);
				Gain carrierGain = new Gain(d.ac, 1, gainEnvelope);
				TapIn delayIn = new TapIn(d.ac, 5000);
				delayIn.addInput(carrierGain); // connect synth gain to delay
				TapOut delayOut = new TapOut(d.ac, delayIn, 200.0f);
				Gain delayGain = new Gain(d.ac, 1, 0.50f);
				delayGain.addInput(delayOut);
				delayIn.addInput(delayGain); // feedback

				carrierGain.addInput(carrier);
				masterGain.addInput(carrierGain);
				masterGain.addInput(delayGain); //connect delay output to audio context
	}

}
