package controller.my_pipos.old_miri;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;

public class Demonstration {

	public static void main(String[] args) {
		
		AudioContext ac = new AudioContext();
//		GranularSamplePlayer sp = new GranularSamplePlayer(ac, SampleManager.sample("/Users/ollie/Desktop/200dickensvol1_14_dickens_64kb.mp3"));
		
		SamplePlayer sp = new SamplePlayer(ac, SampleManager.sample("/Users/ollie/Desktop/200dickensvol1_14_dickens_64kb.mp3"));

		double len = sp.getSample().getLength();
//		sp.getGrainSizeUGen().setValue(50);
//		sp.getGrainIntervalUGen().setValue(20);
//		sp.getRandomnessUGen().setValue(0.01f);
//		
		sp.setPosition(sp.getSample().getLength() * 0.5f);
//		sp.getRateUGen().setValue(0.1f);
		
		Envelope e = new Envelope(ac, 0.1f);
		
		WavePlayer wp = new WavePlayer(ac, e, Buffer.SINE);
		
		Function ringMod = new Function(sp, wp)
		{
			public float calculate()
			{
				return x[0];  // * x[1];
			}
		};
		
		
		
		e.addSegment(0.1f, 5000);
		e.addSegment(1000, 100);
		e.addSegment(1000, 2000);

		e.addSegment(0.1f, 100);
		
		SamplePlayer sp2 = new SamplePlayer(ac, SampleManager.sample("/Users/ollie/Desktop/200dickensvol1_14_dickens_64kb.mp3"));
		sp2.setPosition(len * 0.2f);
		
		sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
		sp2.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
		
		sp.getLoopStartUGen().setValue((float)len * 0.5f);
		sp2.getLoopStartUGen().setValue((float)len * 0.2f);
		

		sp.getLoopEndUGen().setValue((float)len * 0.5f + 2000f);
		sp2.getLoopEndUGen().setValue((float)len * 0.2f + 3000f);
		
		ac.out.addInput(sp2);
		ac.out.addInput(ringMod);
		ac.start();
		
	}
	
}
