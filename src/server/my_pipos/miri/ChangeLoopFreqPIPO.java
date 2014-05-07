package server.my_pipos.miri;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.DelayTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;

public class ChangeLoopFreqPIPO implements PIPO {

	@Override
	public void action(final DynamoPI d) {
		
		float[] loopRatios = {8, 3, 4f, 1f, 1.5f, 2, 1, 1.5f, 3, 3, 4, 4, 4, 4};
		float baseLoopLen = 10000f;
		float drift = 50f;
		
		//get elements
		final Envelope looperFreq = (Envelope)d.get("loopFreq");
		
		//choose loop length
		final float loopLength = baseLoopLen * loopRatios[d.myIndex()];

		looperFreq.clear();
		looperFreq.addSegment(1000f / loopLength, drift);
		
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
