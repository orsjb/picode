package my_pipos.old_miri;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import dynamic.DynamoPI;
import dynamic.PIPO;
import dynamic.SendToPI;

public class SimpleTestPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		Sample s = SampleManager.sample("audio/PiBallRawBabble1.aif");
		
		d.reset();
		
		SamplePlayer sp = new SamplePlayer(d.ac, s);
		sp.getRateUGen().setValue(0.5f);
		d.sound(sp);
		
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"pi1"});
	}
}
