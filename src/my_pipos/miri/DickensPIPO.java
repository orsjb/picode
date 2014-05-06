package my_pipos.miri;

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

public class DickensPIPO implements PIPO {

	private static final long serialVersionUID = 1L;
	
	 WavePlayer osc; 	//the ring modulator
//	 long posSamp = 0;					//
	 Envelope loopStartMS;					//loop start point (value less than zero means "don't loop")
	 
	 Envelope masterGain;				//a master gain control
	 Envelope masterRingModFreq;		//a master ringmod freq control
	 Envelope masterRingModWet;			//a master ringmod freq control
	
	 WavePlayer looper;					//a phasor used for looping
	 Envelope looperFreq;					//frequency of the phasor
	//The following will be controlled by the above looper
	 Buffer ringmodFreqMultPattern;		//a Buffer that will control the ringmod freq
	 Buffer ringmodWetPattern;			//a Buffer that will control the ringmod wet level
	 Buffer gainPattern;				//a Buffer that will control the gain
	 
	 
	 float[] map10to7 = {1,3,5,6,7,8,9};
	
	@Override
	public void action(final DynamoPI d) {
		//reset
		d.reset();
		//get ID
		int id = d.myIndex();
		System.out.println("My index = " + id);
		//load sounds
		SampleManager.sample("babble", "audio/PiBallRawBabble" + map10to7[id % 7] + ".aif");
		SampleManager.sample("quant", "audio/PiBallRawBabble" + id + "Qnt.aif");						
		SampleManager.sample("synch1", "audio/PiBallRawBabble1.aif");
//		SampleManager.sample("synch2", "audio/PiBallRawBabble2.aif");
//		SampleManager.sample("synch3", "audio/PiBallRawBabble3.aif");
		SampleManager.sample("pno", "audio/piano_improv.aif");
		//the buffers
		int bufferSize = 8192 * 2;
		ringmodFreqMultPattern = new Buffer(bufferSize);
		ringmodWetPattern = new Buffer(bufferSize);
		gainPattern = new Buffer(bufferSize);
		Buffer phasor = new Buffer(bufferSize);
		for(int i = 0; i < bufferSize; i++) {
			ringmodFreqMultPattern.buf[i] = 1;
			ringmodWetPattern.buf[i] = 0;
			gainPattern.buf[i] = 1;
			phasor.buf[i] = (float)i/(float)bufferSize;
		}
		//basic UGens
		loopStartMS = new Envelope(d.ac, -1);
		masterRingModFreq = new Envelope(d.ac, 1000);
		masterRingModWet = new Envelope(d.ac, 0);
		looperFreq = new Envelope(d.ac, 0.01f);
		looper = new WavePlayer(d.ac, looperFreq, phasor);
		masterGain = new Envelope(d.ac, 1f);	
		//set up the ringmod oscillator
		Function ringModFreqFunction = new Function(looper, masterRingModFreq) {
			@Override
			public float calculate() {
				return ringmodFreqMultPattern.getValueFraction(x[0]) * x[1];
			}
		};
		osc = new WavePlayer(d.ac, ringModFreqFunction, Buffer.SINE);	
		//store things we want to edit
		// - ugens
		d.put("loopFreq", looperFreq);
		d.put("loopStart", loopStartMS);
		d.put("masterGain", masterGain);
		d.put("rfreq", masterRingModFreq);
		d.put("rwet", masterRingModWet);
		// - buffers
		d.put("rfreqLoop", ringmodFreqMultPattern);
		d.put("rwetLoop", ringmodWetPattern);
		d.put("gLoop", gainPattern);
		d.put("looper", looper);
		
		//set up the main audio function
		UGen audio = new UGen(d.ac, 1) {
			
			double currentPos;
			float[] smp = new float[1];
			float wet, rm, g, modSig;
			long posSamp = 0;
			
			@Override
			public void calculateBuffer() {
				String sampName = (String)d.get("s");
				Sample currentSample = null;
				if(sampName != null) {
					currentSample = SampleManager.sample(sampName);
				}
				Long posSampL = (Long)d.get("p");
				if(posSampL != null) {
					posSamp = posSampL;
					d.share.remove("p");
				}
				//force udpate
				loopStartMS.update();
				osc.update();
				masterGain.update();
				masterRingModWet.update();
				looper.update();
				for(int i = 0; i < bufferSize; i++) {
					double loopS = loopStartMS.getValue(0, i);
					double lpos = looper.getValueDouble(0, i);
					if(currentSample != null) {
						if(loopS >= 0) {
							double loopLenMS = 1000. / looperFreq.getValue(0, i);
							currentPos = lpos * loopLenMS + loopS;					//this is where we are in the sample
						} else {
							currentPos = d.ac.samplesToMs(posSamp++);					//non looping version
						}
						currentSample.getFrameLinear(currentPos, smp);			//grab the sample data
					} else {
						smp[0] = 0;
					}
//					wet = ringmodWetPattern.getValueFraction((float)lpos);		//current wet value
					wet = masterRingModWet.getValue(0, i);
					modSig = wet * 0.2f * osc.getValue(0, i);
					rm = 1f - (wet * 0.5f * (osc.getValue(0, i) + 1));			//get ring mod multiplier, including wet value
					g = gainPattern.getValueFraction((float)lpos);				//current gain value
					bufOut[0][i] = masterGain.getValue(0, i) * g * (smp[0] * rm) + modSig;	
				}
			}
		};
		//play sound
		d.sound(audio);
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
