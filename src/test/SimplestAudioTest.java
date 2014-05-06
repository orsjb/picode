package test;

import core.AudioSetup;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Noise;

public class SimplestAudioTest {
	public static void main(String[] args) {
		AudioContext ac = AudioSetup.getAudioContext(args);
		Noise n = new Noise(ac);
		ac.out.setGain(0.5f);
		ac.out.addInput(n);
		ac.start();
	}
}
