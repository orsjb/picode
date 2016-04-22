package compositions.pipos_2013.miri;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;

public class PianoPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		
		SampleManager.sample("pno", "audio/piano_improv.aif");
		
		//choose sample
		d.put("s", "pno");

		//set time to zero
		d.put("p", new Long(0));
		
		//turn off loop
		Envelope looperStart = (Envelope)d.get("loopStart");
		looperStart.setValue(-1); 

		//turn off effect
		Envelope rwet = (Envelope)d.get("rwet");
		rwet.setValue(0);

		Envelope masterGain = (Envelope)d.get("masterGain");

		masterGain.clear();
		masterGain.addSegment(2f, 200f);
		
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
