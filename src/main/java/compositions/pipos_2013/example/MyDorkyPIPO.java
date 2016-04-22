package compositions.pipos_2013.example;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Compressor;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;

public class MyDorkyPIPO implements PIPO {

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"pi1"});
	}
	
	@Override
	public void action(final DynamoPI d) {

//		d.reset();
		
		d.ac.out.setGain(5f);
		
		//simple set of random tones
		
//		SampleManager.sample("amen", "audio/billy_joe.aif");
//		SampleManager.sample("pno", "audio/piano_improv.aif");
		
//		SamplePlayer sp = new SamplePlayer(d.ac, SampleManager.sample("amen"));
		
//		for(int i = 0; i < 1; i++) {
//			Envelope e = new Envelope(d.ac, 500 + (float)Math.random() * 100);
//			e.addSegment(1000 + (float)Math.random() * 100, 1000);
//			
//			WavePlayer wp = new WavePlayer(d.ac, e, Buffer.SINE);
//	//		d.put("mysound", sp);
//			Gain g = new Gain(d.ac, 1, 1f);
//			g.addInput(wp);
//			
//			d.sound(g);
//		}
		
		
		d.reset();
		
		int id = d.myIndex();
		

		
		Sample s = SampleManager.sample("quant", "audio/PiBallRawBabble4.aif");	
		
		Gain g = new Gain(d.ac, 1, 3f);
		
		g.addInput(new SamplePlayer(d.ac, s));
		
		Compressor c = new Compressor(d.ac);
		c.addInput(g);
		
//		d.sound(c);
		
		
		
		
		
		
	}
	
}
