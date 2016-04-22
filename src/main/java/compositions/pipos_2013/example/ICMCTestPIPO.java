package compositions.pipos_2013.example;

import compositions.pipos_2013.miri.Recipients;
import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class ICMCTestPIPO implements PIPO {

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

	
	@Override
	public void action(final DynamoPI d) {

//		d.reset();
		
		d.ac.out.setGain(1f);
		
		//simple set of random tones
		
		for(int i = 0; i < 1; i++) {
			Envelope e = new Envelope(d.ac, 500 + (float)Math.random() * 100);
			e.addSegment(1000 + (float)Math.random() * 100, 1000);
			
			WavePlayer wp = new WavePlayer(d.ac, e, Buffer.SINE);
	//		d.put("mysound", sp);
			Gain g = new Gain(d.ac, 1, 1f);
			g.addInput(wp);
			
			d.sound(g);
		}
		
		
	}
	
}