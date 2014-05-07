package server.my_pipos.icmc2013;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class RegBleepPIPO implements PIPO {
	
	public String name = "regBleep";

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}
	
	@Override
	public void action(final DynamoPI d) {
		kill(d);
		create(d);
//		kill(d);
	}
	
	public void create(final DynamoPI d) {
		Bead b = new Bead() {
			int nextInterval = 100 * d.myIndex() + 5;
			public void messageReceived(Bead m) {
				if(d.clock.getCount() % nextInterval == 0) {
					int[] pitches = {62, 60, 67, 74, 81, 88, 95, 102, 109, 106};
					int pitch = pitches[d.myIndex() % pitches.length];
//					Noise n = new Noise(d.ac);
					WavePlayer wp = new WavePlayer(d.ac, Pitch.mtof(pitch - 24), d.rng.nextBoolean() ? Buffer.SINE : Buffer.SINE);
					Envelope genv = new Envelope(d.ac, 0);
					final Gain g = new Gain(d.ac, 1, genv);
					g.addInput(wp);
					genv.addSegment(d.rng.nextFloat() * 1.5f + 0.5f, d.rng.nextFloat() * d.rng.nextFloat() * d.rng.nextFloat() * 5000);
					genv.addSegment(0, 3000, new KillTrigger(g));
					d.sound(g);
				}
				
			}
		};
		d.pattern(b);
		d.put(name, b);
	}
	
	public void kill(final DynamoPI d) {
		Bead b = (Bead)d.get(name);
		if(b != null) {
			b.kill();
			d.share.remove(name);
		}
	}
	
}
