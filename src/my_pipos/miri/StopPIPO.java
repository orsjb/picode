package my_pipos.miri;

import net.beadsproject.beads.ugens.Envelope;
import dynamic.DynamoPI;
import dynamic.PIPO;
import dynamic.SendToPI;

public class StopPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		d.share.remove("s");
		final Envelope rwet = (Envelope)d.get("rwet");
		rwet.setValue(0);
		((Envelope)d.get("masterGain")).clearDependents();
	}


	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
