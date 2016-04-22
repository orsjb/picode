package compositions.pipos_2013.rhythm_studies;

import compositions.pipos_2013.miri.Recipients;
import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.WavePlayer;

public class PatternPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		
		float[] freqs = {628.885315f, 1257.597778f, 1886.037354f, 3142.596924f};
		float[] amps = {0.212482f, 0.061316f, 0.015238f, 0.004406f};
		
		int myId = d.myIndex();
		
		final WavePlayer wp = new WavePlayer(d.ac, freqs[myId % freqs.length], Buffer.SINE);
			
		
		
		
		
	}

	
	
	
	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
