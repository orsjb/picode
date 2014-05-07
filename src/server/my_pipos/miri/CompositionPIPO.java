package server.my_pipos.miri;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class CompositionPIPO implements PIPO {

	Long pos;
	Envelope masterGain;

	@Override
	public void action(final DynamoPI d) {
		
		//set master volume
//		d.ac.out.getGainUGen().setValue(0.1f);
		d.ac.out.setGain(0.2f);
		

		final float[] freqs = {157.22132875f, 314.4426575f, 628.885315f, 1257.597778f, 1886.037354f};
		final float[] amps = {0.1f, 0.1f, 0.212482f, 0.061316f, 0.015238f};
		final float ampMult = 10f;
		final int[] group = {0, 0, 0, 0, 0, 1, 1, 1, 2, 2};
		final float[] ratios = {1f, 1f, 1.5f, 1.5f, 2f, 2f, 2f, 3f, 3f, 4f};

		float time = 0;

		final float sampleGain = 5f;
		final float pianoGain = 2.5f;

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

		///////////////
		//play area..//
		///////////////



		////// PART 1 /////
		//start with babble
		d.put("s", "babble");
		//clean up anything else
		pos = new Long(0);
		d.put("p", pos);
		rwet.setValue(0);
		looperStart.setValue(-1);
		//base rfreq set according to vocals
		rfreq.setValue(freqs[id % freqs.length]);
		resetArrays(rmfreqp, rmwetp, gp);
		//volume
		masterGain.clear();
		masterGain.addSegment(sampleGain, 1000f);

		////// PART 2 /////
		//bring in tone

		time += 0.5f * 60 * 1000;
//		time += 1000;

		timedEvent(d, time, new Bead() {
			public void messageReceived(Bead message) {
				masterGain.clear();
				masterGain.addSegment(0.7f, 30 * 1000);
//				masterGain.addSegment(0f, 30 * 1000);
				float freq = freqs[id % freqs.length] + d.rng.nextFloat() * 5;
				rfreq.setValue(freq);
				//schedule change tone...
				rfreq.addSegment(freq, 10000 + d.rng.nextFloat() * 60000);
				rfreq.addSegment(freq / 1.5f, 100);
				rwet.clear();
				rwet.addSegment(amps[id % amps.length] * ampMult, 30 * 1000);		//tone volume here
			}
		});

		////// PART 3 /////
		//back to babble

		time += 0.5f * 60 * 1000;	

		timedEvent(d, time, new Bead() {
			public void messageReceived(Bead message) {
				rfreq.clear();
				rfreq.addSegment(100, 50);
				rwet.clear();
				rwet.addSegment(0, 2000);
				masterGain.clear();
				masterGain.addSegment(0, 2000);
				masterGain.addSegment(0, 2000f, new Bead() {
					public void messageReceived(Bead message) {
						d.put("s", "babble");
						//clean up anything else
						pos = new Long(0);
						d.put("p", pos);
						looperStart.setValue(-1);
					}
				});
				masterGain.addSegment(sampleGain, 100f);
			}
		});		


		////// PART 4 /////
		//loops - stop first, then go to loop (for group 0 and 1 only)

		time += 1 * 60 * 1000;

		float timeRandomnessRange = 30000;

		if(group[id % group.length] != 2) {
			timedEvent(d, time + d.rng.nextFloat() * 
					timeRandomnessRange, new Bead() {
				public void messageReceived(Bead message) {
					masterGain.clear();
					masterGain.addSegment(0, 100f);
					masterGain.addSegment(0, 3000f, new Bead() {
						public void messageReceived(Bead b) {
							gainLoop(gp);
							looperStart.setValue(20000);
							looperFreq.setValue(
									1000f / (ratios[id % ratios.length] * 2000f));
						}
					});
					masterGain.addSegment(sampleGain, 4000f);
				}
			});
		}

		///// PART 5 //////
		//loops start to drift

		time += 50 * 1000;
//		time += 1000;

		if(group[id % group.length] == 0) {
			timedEvent(d, time, new Bead() {
				public void messageReceived(Bead b) {
					//drift loops
					float val = looperStart.getCurrentValue();
					looperStart.addSegment(val + 100000, 5000000);
				}
			});
		}

		////// PART 6 /////
		//bring in tones again
		
		time += 1 * 60 * 1000;
//		time += 1000;

		timedEvent(d, time, new Bead() {
			public void messageReceived(Bead message) {
				masterGain.clear();
				masterGain.addSegment(2f, 30 * 1000);
				rwet.clear();
				rwet.addSegment(amps[id % amps.length] * ampMult, 30 * 1000);		//tone volume here
				rfreq.setValue(freqs[id % freqs.length] + d.rng.nextFloat() * 10f);
			}
		});

		///// PART 7 //////

		//loop other elements (group 2)

		time += 1 * 60 * 1000;	
//		time += 1000;		

		if(group[id % group.length] == 2) {
			timedEvent(d, time, new Bead() {
				public void messageReceived(Bead message) {
					masterGain.clear();
					masterGain.addSegment(0, 100f);
					masterGain.addSegment(0, 1000f, new Bead() {
						public void messageReceived(Bead b) {
							looperStart.setValue(20000);
							looperStart.addSegment(20000 + 100000, 5000000);
							float freq = 1000f / (ratios[id % ratios.length] * 500f);
							looperFreq.setValue(freq);
						}
					});
					masterGain.addSegment(sampleGain * 0.5f, 100f);
				}
			});
		}

		///// PART 8 //////
		//back to babble, quantised

		time += 1 * 60 * 1000;
//		time += 1000;

		timedEvent(d, time, new Bead() {
			public void messageReceived(Bead message) {
	
				rfreq.clear();
				float value = rfreq.getCurrentValue();
				rfreq.addSegment(value + d.rng.nextFloat() * 10f, 5000f);
				//fade out the tones
				float fade = 10000f;
				masterGain.clear();
				masterGain.addSegment(sampleGain, fade);
				rwet.clear();
				rwet.addSegment(0, fade, new Bead() {
					public void messageReceived(Bead b) {
						d.put("s", "quant");
						//clean up anything else
						pos = new Long(0);
						d.put("p", pos);
						looperStart.setValue(-1);
						masterGain.clear();
						masterGain.addSegment(sampleGain, 10000);
					}
				});


			}
		});



		////// PART 9 /////
		//stop, all do loop

		time += 1 * 60 * 1000;	
//		time += 1000;	

		timedEvent(d, time, new Bead() {
			public void messageReceived(Bead message) {
				masterGain.clear();
				masterGain.addSegment(0, 100f);
				masterGain.addSegment(0, 6000f, new Bead() {
					public void messageReceived(Bead b) {
						looperStart.setValue(20000);
						looperStart.addSegment(20000 + 100000, 50000000);
						float freq = 1000f / (ratios[id % ratios.length] * 500f);
						looperFreq.setValue(freq);
						float newFreq = 1000f / 40f;
						looperFreq.addSegment(newFreq, 30000);
						looperFreq.addSegment(freq / 10f, 60000);
					}
				});
				masterGain.addSegment(sampleGain, 20000f);
			}
		});

		////// PART 10 ////
		//stop back to babble

		time += 2 * 60 * 1000;
//		time += 1000;

		timedEvent(d, time, new Bead() {
			public void messageReceived(Bead message) {
				masterGain.clear();
				masterGain.addSegment(0, 15000f);
				masterGain.addSegment(0, 5000f, new Bead() {
					public void messageReceived(Bead b) {
						d.put("s", "babble");
						//clean up anything else
						pos = new Long(0);
						d.put("p", pos);
						rwet.setValue(0);
						looperStart.setValue(-1);
						masterGain.addSegment(sampleGain, 100);
					}
				});
			}
		});

		////// PART 11 ////
		//stop, tones --> looped tones fading in on some groups, different loop lengths

		time += 3 * 60 * 1000;
//		time += 1000;

		if(group[id % group.length] == 0) {
			timedEvent(d, time * d.rng.nextFloat() * 20000f, new Bead() {
				public void messageReceived(Bead message) {
					masterGain.clear();
					masterGain.addSegment(1, 100f);
					masterGain.addSegment(1, 6000f, new Bead() {
						public void messageReceived(Bead b) {
							rfreq.clear();
							rfreq.setValue(freqs[id % freqs.length] + d.rng.nextFloat() * 10f);
							melArray2(rmfreqp);
//							wetArray1(rmwetp);
							looperStart.clear();
							looperStart.setValue(20000 + 20000 * d.rng.nextFloat());
							float freq = 1000f / (ratios[(id + 2) % ratios.length] * 500f);
							looperFreq.clear();
							looperFreq.setValue(freq);
							float newFreq = 1000f / (ratios[(id + 2) % ratios.length] * 5000f);
							looperFreq.addSegment(newFreq, 150000);
							rwet.clear();
							rwet.addSegment(amps[id % amps.length] * ampMult, 10000f + d.rng.nextFloat() * 10000f);
							masterGain.addSegment(0, 40000f);
						}
					});



				}
			});
		}

		////// PART 12 /////
		//same for everyone else

		time += 0.5f * 60 * 1000;

		if(group[id % group.length] != 0) {
			timedEvent(d, time + d.rng.nextFloat() * 20000f, new Bead() {
				public void messageReceived(Bead message) {
					masterGain.clear();
					masterGain.addSegment(1, 100f);
					masterGain.addSegment(1, 6000f, new Bead() {
						public void messageReceived(Bead b) {
							rfreq.clear();
							rfreq.setValue(freqs[id % freqs.length] + d.rng.nextFloat() * 10f);
							melArray1(rmfreqp);
							looperStart.clear();
							looperStart.setValue(20000 + 20000 * d.rng.nextFloat());
							float freq = 1000f / (ratios[(id + 2) % ratios.length] * 500f);
							looperFreq.clear();
							looperFreq.setValue(freq);
							float newFreq = 1000f / (ratios[(id + 2) % ratios.length] * 2000f);
							looperFreq.addSegment(newFreq, 200000f);
							rwet.clear();
							rwet.addSegment(amps[id % amps.length] * ampMult, 10000f + d.rng.nextFloat() * 10000f);
						}
					});
				}
			});
		}

		////// PART 13 /////
		//mix in pianos looping

		time += 3.5f * 60 * 1000;

		timedEvent(d, time + d.rng.nextFloat() * timeRandomnessRange, new Bead() {
			public void messageReceived(Bead message) {
				d.put("s", "pno");
				masterGain.clear();
				masterGain.addSegment(pianoGain, 100000);
				rwet.clear();
				rwet.addSegment(0, 200000);
			}
		});
		
		////// PART 13b //////
		// all pianos looping, ring mod, slow fade

		time += 3 * 60 * 1000;

		timedEvent(d, time + d.rng.nextFloat() * timeRandomnessRange, new Bead() {
			public void messageReceived(Bead message) {
				float newFreq = 1000f / (ratios[(id + 2) % ratios.length] * 200f);
				looperFreq.addSegment(newFreq, 150000);
			}
		});

		////// PART 14 //////
		// all pianos looping, ring mod, slow fade

		time += 2 * 60 * 1000;

		timedEvent(d, time + d.rng.nextFloat() * timeRandomnessRange, new Bead() {
			public void messageReceived(Bead message) {
				masterGain.clear();
				resetArrays(rmfreqp, rmwetp, gp);
				rwet.clear();
				rwet.addSegment(0.5f, 30000f * d.rng.nextFloat());
				masterGain.addSegment(0, 50000 + d.rng.nextFloat() * 20000f);
			}
		});

		///// REPEAT ////////
		time += 1 * 60 * 1000;




	}

	private void testBleep(DynamoPI d) {
		Envelope e = new Envelope(d.ac, 1);
		Gain g = new Gain(d.ac, 1, e);
		WavePlayer wp = new WavePlayer(d.ac, 500, Buffer.SINE);
		g.addInput(wp);
		e.addSegment(0, 1000, new KillTrigger(g));
		d.sound(g);
	}

	private void resetArrays(Buffer rmfreqp, Buffer rmwetp, Buffer gp) {
		for(int i = 0; i < gp.buf.length; i++) {
			rmfreqp.buf[i] = 1;
			rmwetp.buf[i] = 0;
			gp.buf[i] = 1;
		}
	}

	private void melArray1(Buffer b) {
		int bufferSize = b.buf.length;
		for(int i = 0; i < bufferSize; i++) {
			int mult = 1 + ((i * 4) / bufferSize);
			b.buf[i] = (float)mult * 0.5f;
		}
	}
	
	private void melArray2(Buffer b) {
		int bufferSize = b.buf.length;
		for(int i = 0; i < bufferSize; i++) {
			int mult = 1 + ((i * 3) / bufferSize);
			b.buf[i] = (float)mult * 0.5f;
		}
	}
	
	private void gainLoop(Buffer b) {
		int bufferSize = b.buf.length;
		for(int i = 0; i < bufferSize; i++) {
			float mult = (float)i / bufferSize;
			b.buf[i] = mult;
		}
	}

//	private void wetArray1(Buffer b) {
//		int bufferSize = b.buf.length;
//		for(int i = 0; i < bufferSize; i++) {
//			int mult = i < bufferSize;
//			b.buf[i] = mult;
//		}
//	}

	private void timedEvent(DynamoPI d, float delayTime, Bead event) {
		//		d.ac.out.addDependent(new DelayTrigger(d.ac, delayTime, event));
		masterGain.addDependent(new DelayTrigger(d.ac, delayTime, event));
	}


	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
