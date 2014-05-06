package my_pipos.old_miri;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;

public class BabbleAgentPIPO implements PIPO {

	private static final long serialVersionUID = 1L;
	
	 Sample currentSample;
	
	 WavePlayer osc; 	//the ring modulator
	
	 long posSamp = 0;
	 Glide loopStartMS, loopLenMS;
	 

	 Envelope masterGain;				//a master gain control
	 Envelope masterRingModFreq;		//a master ringmod freq control
	
	 WavePlayer looper;				//a phasor used for looping
	//The following will be controlled by the above looper
	 Buffer ringmodFreqMultPattern;	//a Buffer that will control the ringmod freq
	 Buffer ringmodWetPattern;		//a Buffer that will control the ringmod wet level
	 Buffer gainPattern;				//a Buffer that will control the gain
	
	@Override
	public void action(final DynamoPI d) {
		//reset
		d.reset();
		//get ID
		int id = d.myIndex();
		//load sounds
		SampleManager.sample("babble", "audio/PiBallRawBabble" + (id + 1) + ".aif");
//		babbleQuantSample = SampleManager.sample("audio/babbleQuant.aif");						//TODO other sounds
//		synchSample1 = SampleManager.sample("audio/synch1.aif");
//		synchSample2 = SampleManager.sample("audio/synch2.aif");
//		synchSample3 = SampleManager.sample("audio/synch3.aif");
		//the buffers
		int bufferSize = 512;
		ringmodFreqMultPattern = new Buffer(bufferSize);
		ringmodWetPattern = new Buffer(bufferSize);
		gainPattern = new Buffer(bufferSize);
		Buffer phasor = new Buffer(bufferSize);
		for(int i = 0; i < bufferSize; i++) {
			ringmodFreqMultPattern.buf[i] = 1;
			ringmodWetPattern.buf[i] = 0;
			gainPattern.buf[i] = 1;
			phasor.buf[i] = i/(float)bufferSize;
		}
		//basic UGens
		loopStartMS = new Glide(d.ac, -1);
		masterRingModFreq = new Envelope(d.ac, 1000);
		looper = new WavePlayer(d.ac, 0.01f, phasor);
		//set up the ringmod oscillator
		Function ringModFreqFunction = new Function(looper, masterRingModFreq) {
			@Override
			public float calculate() {
				return ringmodFreqMultPattern.getValueFraction(x[0]) * x[1];
			}
			
		};
		osc = new WavePlayer(d.ac, ringModFreqFunction, Buffer.SINE);
		masterGain = new Envelope(d.ac, 0.7f);		
		//set up the main audio function
		UGen audio = new UGen(d.ac, 1) {
			
			double currentPos;
			float[] smp = new float[1];
			float wet, rm, g;
			
			@Override
			public void calculateBuffer() {
				for(int i = 0; i < bufferSize; i++) {
					if(currentSample == null) {
						return;
					}
					double loopS = loopStartMS.getValueDouble(0, i);
					double lpos = looper.getValueDouble(0, i);
					if(loopS >= 0) {
						double loopLenMS = 1000. / looper.getFrequencyUGen().getValue(0, i);
						currentPos = lpos * loopLenMS + loopS;	//this is where we are in the sample
					} else {
						posSamp++;
						currentPos = d.ac.samplesToMs(posSamp);			//non looping version
					}
					currentSample.getFrameNoInterp(currentPos, smp);		//grab the sample data
					wet = ringmodWetPattern.getValueFraction((float)lpos);		//current wet value
					rm = 1f - (wet * 0.5f * (osc.getValue(0, 1) + 1));				//get ring mod multiplier, including wet value
					g = gainPattern.getValueFraction((float)lpos);				//current gain value
					bufOut[0][i] = masterGain.getValue(0, i) * g * (smp[0] * rm);	
				}
			}
		};
		//play sound
		d.sound(audio);
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"pi1"});
	}

}
