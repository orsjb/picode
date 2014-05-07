package server.my_pipos.icmc2013;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.WavePlayer;

public class RegPIPO implements PIPO {

	public String name = "regNoise";
	
	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
//		SendToPI.send(fullClassName, new String[] {"pi2", "pi3", "pi1"});
	}
	
	@Override
	public void action(final DynamoPI d) {
		kill(d);
//		create(d);
	}
	
	public void create(final DynamoPI d) {
		Bead b = new Bead() {
			int nextInterval = 10;
			public void messageReceived(Bead m) {
				
				if(d.clock.isBeat() && d.clock.getBeatCount() % nextInterval == 0) {
					Envelope genv = new Envelope(d.ac, 0);
					final Gain g = new Gain(d.ac, 1, genv);
					if(d.rng.nextFloat() < 0.5f) {
						UGen snd = new Noise(d.ac);
						g.addInput(snd);
						genv.addSegment(d.rng.nextFloat() * 1 + 0.5f, 500);
					} else {
						UGen snd = new WavePlayer(d.ac, Pitch.mtof(60), Buffer.SINE);
						g.addInput(snd);
						snd = new WavePlayer(d.ac, Pitch.mtof(60) + 4 * d.rng.nextFloat(), Buffer.SINE);
						g.addInput(snd);
						genv.addSegment(d.rng.nextFloat() * 3 + 0.5f, 1000);
					}
					d.sound(g);
					genv.addSegment(0, 5000, new KillTrigger(g));
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
