package dynamic;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.IOAudioFormat;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;

public abstract class AudioSetup {

	public static AudioContext getAudioContext() {
		int bufSize = 8192;
		return new AudioContext(new JavaSoundAudioIO(bufSize), bufSize, new IOAudioFormat(22000, 16, 0, 1));
	}
}
