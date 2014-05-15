package controller.my_pipos.icmc2013;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;

public class NoisesPIPO implements PIPO {

	public String name = "noise_fades";
	
	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}
	
	@Override
	public void action(final DynamoPI d) {
		kill(d);
		create(d);
		kill(d);
	}
	
	public void create(final DynamoPI d) {
		Bead b = new Bead() {
			int nextInterval = d.rng.nextInt(100) + 1;
			public void messageReceived(Bead m) {
				if(d.clock.getBeatCount() % nextInterval == 0) {
					nextInterval = d.rng.nextInt(500) + 1;
					int id = d.myIndex();
					Noise n = new Noise(d.ac);
					Envelope genv = new Envelope(d.ac, 0);
					final Gain g = new Gain(d.ac, 1, genv);
					g.addInput(n);
					d.sound(g);
					genv.addSegment(d.rng.nextFloat() * 1f, d.rng.nextFloat() * 15000 + 100);
					genv.addSegment(0, 10, new KillTrigger(g));
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
