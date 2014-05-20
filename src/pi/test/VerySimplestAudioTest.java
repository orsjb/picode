package pi.test;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.IOAudioFormat;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.ugens.Noise;

public class VerySimplestAudioTest {
	public static void main(String[] args) {
		
		System.out.println("Mixer info: ");
		JavaSoundAudioIO.printMixerInfo();
		System.out.println("------------");

		AudioContext ac = new AudioContext(new JavaSoundAudioIO(1024), 1024, new IOAudioFormat(44100, 16, 0, 1));
		Noise n = new Noise(ac);
		ac.out.setGain(0.5f);
		ac.out.addInput(n);
		ac.start();
		
	}
}
