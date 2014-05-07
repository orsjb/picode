package pi.test;

import pi.sensors.MiniMU;
import pi.sensors.MiniMU.MiniMUListener;
import core.AudioSetup;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;

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
				gainCtrl.setValue((float)Math.abs(y) % 400f / 1600f + 0.1f);
				System.out.println("getting data: " + x + " " + y);
			}
		};
		MiniMU mm = new MiniMU(myListener);
		mm.start();
	}

}