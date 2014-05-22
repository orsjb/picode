package compositions.pipos_2013.breaks;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class PIPOPlayDronePattern implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"localhost"});
	}
	
	@Override
	public void action(final DynamoPI d) {
		
		float baseFreq = 500;
		d.put("baseFreq", baseFreq);
		
		Bead pattern = new Bead() {
			boolean isplaying = false;
			public void messageReceived(Bead message) {
				//play a long droney tone swell and end
				if(!isplaying) {
					float baseFreq = (Float)d.get("baseFreq");
					float freq = baseFreq * (float)Math.pow(2, d.rng.nextInt(4));
					WavePlayer wp = new WavePlayer(d.ac, freq, Buffer.SQUARE);
					Envelope e = new Envelope(d.ac, 0);
					Gain g = new Gain(d.ac, 1, e);
					g.addInput(wp);
					d.sound(g);
					isplaying = true;
				}
			}
		};
		
		d.pattern(pattern);
		d.put("bleep", pattern);
		
	}
	
}
