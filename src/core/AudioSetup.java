package core;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.IOAudioFormat;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;

public abstract class AudioSetup { 

	public static AudioContext getAudioContext(String[] args) {			//args are bufSize (8192), sample rate (22050), input channels (0), output channels (1)
		try {
			int bufSize = Integer.parseInt(args[0]);
			int sampleRate = Integer.parseInt(args[1]);
			int inchans = Integer.parseInt(args[2]);
			int outchans = Integer.parseInt(args[3]);
			System.out.println("Creating AudioContext with args: bufSize=" + bufSize + ", sampleRate=" + sampleRate + ", ins=" + inchans + ", outs=" + outchans);
			return new AudioContext(new JavaSoundAudioIO(bufSize), bufSize, new IOAudioFormat(sampleRate, 16, inchans, outchans));
		} catch(Exception e) {
			System.out.println("Warning, correct args have not been supplied to AudioSetup.getAudioContext()");
			return getAudioContext();
		}
	}
	
	public static AudioContext getAudioContext() {			
		return getAudioContext(new String[] {"8192", "22050", "0", "1"});
	}
}
