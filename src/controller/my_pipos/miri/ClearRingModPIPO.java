package controller.my_pipos.miri;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.ugens.Envelope;

public class ClearRingModPIPO implements PIPO {

	@Override
	public void action(final DynamoPI d) {
		
		//get elements
		final Envelope rwet = (Envelope)d.get("rwet");
	
		rwet.clear();
		rwet.addSegment(0, 20000f * d.rng.nextFloat() + 5000f);
		
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
