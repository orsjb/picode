package server.my_pipos.miri;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;

public class RingModPIPO implements PIPO {

	@Override
	public void action(final DynamoPI d) {
		
		float[] freqMults = {1, 1.5f, 2, 1, 1.5f, 2f, 1f, 1.5f, 3f, 3f, 4f, 5f};
		float baseFreq = 1000f;
		
		float delayRange = 1000f;
		
		//get elements
		final Envelope rfreq = (Envelope)d.get("rfreq");
		final Envelope rwet = (Envelope)d.get("rwet");
	
		float newFreq = baseFreq * freqMults[d.myIndex()];
		rfreq.clear();
		float oldFreq = rfreq.getValue();
		rfreq.addSegment(oldFreq, delayRange * d.rng.nextFloat());
		rfreq.addSegment(newFreq, 100);
		
		rwet.clear();
		float wetVal = rwet.getValue();
		rwet.addSegment(wetVal, delayRange * d.rng.nextFloat() + delayRange, new Bead() {
			public void messageReceived(Bead message) {
				rwet.addSegment(2f, 20000f * d.rng.nextFloat() + 5000f);
			}
		});
		
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
