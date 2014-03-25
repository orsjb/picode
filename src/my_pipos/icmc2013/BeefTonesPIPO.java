package my_pipos.icmc2013;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import dynamic.DynamoPI;
import dynamic.PIPO;
import dynamic.SendToPI;

public class BeefTonesPIPO implements PIPO {
	
	public String name = "beef";

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
		Envelope modFreq = new Envelope(d.ac, 20f);	
		Envelope modAmount = new Envelope(d.ac, 100f);
		Envelope baseFreq = new Envelope(d.ac, 500 + d.rng.nextFloat() * 10);
		WavePlayer mod = new WavePlayer(d.ac, modFreq, Buffer.SINE);
		Function freqmod = new Function(baseFreq, mod, modAmount) {
			@Override
			public float calculate() {
				return x[0] + (x[1] * x[2]); 
			}
		};
		WavePlayer carrier = new WavePlayer(d.ac, freqmod, Buffer.SINE);
		Envelope genv = new Envelope(d.ac, 0);
		Gain g = new Gain(d.ac, 1, genv);
		g.addInput(carrier);
		d.sound(g);
		d.put(name, g);
		d.put(name + "1", genv);
		
		//action
		baseFreq.addSegment(500, 200);
		baseFreq.addSegment(600, 3000);
		genv.addSegment(5, 15000);
		modAmount.addSegment(100f, 100000);
		modAmount.addSegment(0, 5000);
		modFreq.addSegment(20f, 20000);
		modFreq.addSegment(5f, 50000);
	}
	
	public void kill(final DynamoPI d) {
		final Bead g = (Bead)d.get(name);
		final Envelope genv = (Envelope)d.get(name + "1");
		if(genv != null && g != null) {
			genv.addSegment(0, 5000, new KillTrigger(g));
			d.share.remove(name);
			d.share.remove(name + "1");
		}
	}
	
}
