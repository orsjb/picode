package compositions.pipos_2014.contact;

import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.Noise;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU.MiniMUListener;
import core.PIPO;

public class Contact implements PIPO {

	private static final long serialVersionUID = 1L;
	
	public static final boolean verbose = false;

	@Override
	public void action(DynamoPI d) {
		
		//set responsive behaviours
		////////////////////////////////////////
		
		//filtered white noise going to sparkle...
		
		
		//solo instrument...
		
		
		//chord instruments - scatter, free improv bleeping... (use a couple of samples + granulation)
		
		
		//scatter with misc samples + bleeps
		
		
		//smooth noise gentle glitch
		
		
		//arpeggiated patterns
		
		
		
	}
	
	public void filteredNoiseAction(final DynamoPI d) {
		//controllers
		final Glide freqCtrl = new Glide(d.ac, 500);
		final Glide gainCtrl = new Glide(d.ac, 0.1f);
		//set up signal chain
		Noise n = new Noise(d.ac);
		BiquadFilter bf = new BiquadFilter(d.ac, 1);
		bf.addInput(n);
		bf.setFrequency(freqCtrl);
		bf.setQ(0.9f);
		Gain g = new Gain(d.ac, 1, gainCtrl);
		g.addInput(bf);
		d.ac.out.addInput(g);
		//get listening to data
		MiniMUListener myListener = new MiniMUListener() {
			public void accelData(double x, double y, double z) {
				String AccString = String.format("MiniMu Acc X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
				System.out.println(AccString);
				freqCtrl.setValue(((float)Math.abs(x) * 15f) % 5000f + 100f);
				gainCtrl.setValue(((float)Math.abs(y) * 30f) % 400f / 3200f + 0.01f);
			}
			public void gyroData(double x, double y, double z) {
				String GyrString = String.format("MiniMu Gyr X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
				System.out.println(GyrString);
			}
		};
		d.mu.addListener(myListener);
	}

}
