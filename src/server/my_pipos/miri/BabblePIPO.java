package server.my_pipos.miri;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Glide;

public class BabblePIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		
		//choose sample
//		d.put("s", "synch1");
//		d.put("s", "quant");
		d.put("s", "babble");
		
		

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
		masterGain.addSegment(5f, 20000f);
		
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
