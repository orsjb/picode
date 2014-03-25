package my_pipos.miri;

import java.net.SocketAddress;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.WavePlayer;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import dynamic.DynamoPI;
import dynamic.PIPO;
import dynamic.SendToPI;

public class InstallationPIPO implements PIPO {

	Long pos;
	Envelope masterGain;
	
	final float[] formantFreqs = {1.875f, 8.5185f, 2.9f};
	
	@Override
	public void action(final DynamoPI d) {
		//set master volume
//		d.ac.out.getGainUGen().setValue(0.1f);
		d.ac.out.setGain(1.5f);
		
//		d.reset();

//		final float[] freqs = {157.22132875f, 196.52625f, 314.4426575f, 628.885315f, 1257.597778f, 1886.037354f};
//		final float[] amps = {0.1f, 0.1f, 0.212482f, 0.061316f, 0.015238f};
		
		final float[] freqs = {165f, 220f, 275f, 330f, 385f, 440f, 495f};
		
		final float[] amps = {0.1f};
		
		
		
		
		final float ampMult = 10f;
		final float[] ratios = {1f, 1f, 1.5f, 2f, 2f, 3f, 3f, 4f, 5f, 5f};

		
		final float sampleGain = 5f;
		
		//id
		final int id = d.myIndex();

		//pos (works in no loop mode)
		pos = new Long(0);
		d.put("p", pos);

		//get ugens
		final Envelope looperFreq = (Envelope)d.get("loopFreq");
		final Envelope looperStart = (Envelope)d.get("loopStart");
		masterGain = (Envelope)d.get("masterGain");
		final Envelope rfreq = (Envelope)d.get("rfreq");
		final Envelope rwet = (Envelope)d.get("rwet");
		final WavePlayer looper = (WavePlayer)d.get("looper");

		//get buffers
		final Buffer rmfreqp = (Buffer)d.get("rfreqLoop");
		final Buffer rmwetp = (Buffer)d.get("rwetLoop");
		final Buffer gp = (Buffer)d.get("gLoop");

		////// PART 1 /////
		//start with babble
		d.put("s", "babble");
		masterGain.setValue(0);
//		d.put("s", null);
		//clean up anything else
		pos = new Long(0);
		d.put("p", pos);
		rwet.setValue(0);
		looperStart.setValue(-1);
		//base rfreq set according to vocals
		rfreq.setValue(freqs[id % freqs.length]);
		resetArrays(rmfreqp, rmwetp, gp);
		//volume
//		masterGain.clear();
//		masterGain.addSegment(sampleGain, 1000f);
		
		d.oscServer.addOSCListener(new OSCListener() {
			public void messageReceived(OSCMessage m, SocketAddress sender, long t) {
				System.out.println("OSC Message");
				if (m.getName().equals("/PI/sensor/density") && m.getArgCount() == 1) {
					switch ((Integer)m.getArg(0)) {
						case 0:
							//babble
							System.out.println("Density: 0");
							masterGain.clear();
							masterGain.addSegment(0, 1500f);
							masterGain.addSegment(0, 2000f, new Bead() {
								public void messageReceived(Bead b) {
									resetArrays(rmfreqp, rmwetp, gp);
									d.put("s", "babble");
									//clean up anything else
									pos = new Long(0);
									d.put("p", pos);
									rwet.setValue(0);
									looperStart.setValue(-1);
									masterGain.addSegment(sampleGain, 100);
								}
							});
							break;
						case 1:
							//Tone

							d.put("s", "babble");
							
							looperStart.setValue(-1);
							resetArrays(rmfreqp, rmwetp, gp);
							masterGain.clear();
							masterGain.addSegment(0.7f, 30 * 1000);
//							masterGain.addSegment(0f, 30 * 1000);
							float freq = freqs[id % freqs.length] + d.rng.nextFloat() * 5;
							rfreq.setValue(freq);
							//schedule change tone...
							rfreq.addSegment(freq, 20000 + d.rng.nextFloat() * 60000);
							rfreq.addSegment(freq / 1.5f, 100);
							rwet.clear();
							rwet.addSegment(2f * amps[id % amps.length] * ampMult, 30 * 1000);
							System.out.println("Density: 1");
							break;
						case 2:
							//loops
							masterGain.clear();
							masterGain.addSegment(0, 100f);
							masterGain.addSegment(0, 1000f, new Bead() {
								public void messageReceived(Bead b) {
									

									d.put("s", "babble");
									
									resetArrays(rmfreqp, rmwetp, gp);
									rwet.clear();
									rwet.setValue(0);
									
									
									
									looperStart.setValue(20000);
									looperStart.addSegment(20000 + 100000, 2000000);
									float freq = 1000f / (ratios[id % ratios.length] * 1000f);
									looperFreq.setValue(freq);
									
									
//									float newFreq = 1000f / 40f;
//									looperFreq.addSegment(newFreq, 30000);
//									looperFreq.addSegment(freq / 10f, 60000);
									
									
									
									
								}
							});
							masterGain.addSegment(sampleGain, 2000f);
							break;
						case 3:
							//Looped tones
							masterGain.clear();
							masterGain.addSegment(1, 100f);
							masterGain.addSegment(sampleGain, 1000f, new Bead() {
								public void messageReceived(Bead b) {
									

									d.put("s", "babble");
									
									float scale = 1.8f;
									
									rfreq.clear();
									rfreq.setValue(freqs[id / 2]);// + d.rng.nextFloat() * 2f);
									if(id % 2 == 0) {
										melArray2(rmfreqp);
//										scale = 0.3f;
									} else {
										resetArrays(rmfreqp, rmwetp, gp);
//										scale = 2f;
									}
//									wetArray1(rmwetp);
									
									
									looperStart.clear();
									looperStart.setValue(20000 + 20000 * d.rng.nextFloat());
									
									
									float freq = 1000f / (ratios[(id + 2) % ratios.length] * 500f);
									looperFreq.clear();
									looperFreq.setValue(freq);
									float newFreq = 1000f / ((ratios[(id + 2) % ratios.length] * 5000f + d.rng.nextFloat() * 100f));
									looperFreq.addSegment(newFreq, 30000);
									
									
									rwet.clear();
									rwet.addSegment(scale * amps[id % amps.length] * ampMult, 10000f + d.rng.nextFloat() * 10000f);
									masterGain.addSegment(0, 4000f);
								
								
								
								}
							});
							
							break;
					}
				}
			}
		});
	}

	private void resetArrays(Buffer rmfreqp, Buffer rmwetp, Buffer gp) {
		for(int i = 0; i < gp.buf.length; i++) {
			rmfreqp.buf[i] = 1;
			rmwetp.buf[i] = 0;
			gp.buf[i] = 1;
		}
	}
	
	private void melArray2(Buffer b) {
		int bufferSize = b.buf.length;
		for(int i = 0; i < bufferSize; i++) {
			int mult = 1 + ((i * 3) / bufferSize);
			b.buf[i] = (float)mult * 0.5f;
			
//			int index = (i * 3) / bufferSize;
//			b.buf[i] = formantFreqs[index];
			
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
