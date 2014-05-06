package test;

import sensors.MiniMU;
import sensors.MiniMU.MiniMUListener;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import dynamic.AudioSetup;

public class MiniMUTest {

	public static void main(String[] args) {
		//audio
		AudioContext ac = AudioSetup.getAudioContext(args);
		ac.start();
		//controllers
		final Glide freqCtrl = new Glide(ac, 500);
		final Glide gainCtrl = new Glide(ac, 0.1f);
		//set up signal chain
		WavePlayer wp = new WavePlayer(ac, freqCtrl, Buffer.SINE);
		Gain g = new Gain(ac, 1, gainCtrl);
		g.addInput(wp);
		ac.out.addInput(g);
		//get listening to data
		MiniMUListener myListener = new MiniMUListener() {
			public void accelData(double x, double y, double z) {
				freqCtrl.setValue((float)Math.abs(x) % 10000f + 600f);
				freqCtrl.setValue((float)Math.min(0.5, Math.abs(y) % 400. / 400.) + 0.1f);
				System.out.println("getting data: " + x + " " + y);
			}
		};
		MiniMU mm = new MiniMU(myListener);
		mm.start();
	}

}
