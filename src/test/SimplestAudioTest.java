package test;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Noise;
import dynamic.AudioSetup;

public class SimplestAudioTest {
	public static void main(String[] args) {
		AudioContext ac = AudioSetup.getAudioContext();
		Noise n = new Noise(ac);
		ac.out.getGainUGen().setValue(0.5f);
		ac.out.addInput(n);
		ac.start();
	}
}
