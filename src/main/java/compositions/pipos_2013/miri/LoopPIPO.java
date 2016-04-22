package compositions.pipos_2013.miri;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.WavePlayer;

public class LoopPIPO implements PIPO {

	@Override
	public void action(final DynamoPI d) {
		
		float[] loopRatios = {1, 1.5f, 2, 1.5f, 1.5f, 2, 2, 3, 3, 3, 4, 4, 4, 4};
		float baseLoopLen = 2000f;
		
		//get elements
		final WavePlayer looper = (WavePlayer)d.get("looper");
		final Envelope looperFreq = (Envelope)d.get("loopFreq");
		final Envelope loopStart = (Envelope)d.get("loopStart");
		
		//choose loop length
		final float loopLength = baseLoopLen * loopRatios[d.myIndex()];
		
		float randomRange  = 0f;
		float randomOffset = 0f;
		
		//turn off osc and set loop at some random future time
		//loop start is chosen as current time
		
		DelayTrigger dt = new DelayTrigger(d.ac, d.rng.nextFloat() * randomRange + randomOffset, new Bead() {
			public void messageReceived(Bead message) {
				//get current pos
				Long pos = (Long)d.get("p");
				//start the loop
				loopStart.clear();
				float start = (float)d.ac.samplesToMs(pos);
				loopStart.setValue(start);
				loopStart.addSegment(start + 10000, 500000);
				looper.setPhase(0);
				looperFreq.clear();
				looperFreq.addSegment(1000f / loopLength, 200);
			}
		});
		d.ac.out.addDependent(dt);
	
		
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
