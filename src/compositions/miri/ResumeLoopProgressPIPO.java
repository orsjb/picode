package compositions.miri;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.ugens.Envelope;

public class ResumeLoopProgressPIPO implements PIPO {

	@Override
	public void action(final DynamoPI d) {
		
		final Envelope loopStart = (Envelope)d.get("loopStart");
		loopStart.clear();
		float val = loopStart.getValue();
		loopStart.addSegment(val + 100000, 1000000);
	
	
		
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
