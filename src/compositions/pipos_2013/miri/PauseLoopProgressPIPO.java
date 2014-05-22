package compositions.pipos_2013.miri;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.ugens.Envelope;

public class PauseLoopProgressPIPO implements PIPO {

	@Override
	public void action(final DynamoPI d) {
		
		final Envelope loopStart = (Envelope)d.get("loopStart");
		loopStart.clear();
	
	
		
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
