package my_pipos.icmc2013;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;

public class SetupPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		//settings
		d.pl.setSteal(true);
		d.pl.setMaxInputs(10);
		//load audio
		int id = d.myIndex();
		int[] map10to7 = {1,3,5,6,7,8,9};
		SampleManager.sample("babble", "audio/piano_improv.aif");
//		SampleManager.sample("mouse", "audio/mouse.mp3");
		
//		d.ac.out.setGain(0.1f);
		
	}


	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
