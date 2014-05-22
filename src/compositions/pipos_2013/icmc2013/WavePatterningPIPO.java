package compositions.pipos_2013.icmc2013;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class WavePatterningPIPO implements PIPO {

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
//		SendToPI.send(fullClassName, new String[]{"pi7"});
//		SendToPI.send(fullClassName, new String[] {"pi2", "pi3", "pi1"});
		SendToPI.send(fullClassName, Recipients.list);
	}
	
	@Override
	public void action(final DynamoPI d) {
		kill(d);
		create(d);
		etc(d);
		kill(d);
	}
	
	public void etc(final DynamoPI d) {
		//other stuff 
		Bead pattern = new Bead() {
			int nextInterval = 1;
			public void messageReceived(Bead message) {
				if(d.clock.getCount() % nextInterval == 0) {
					nextInterval = d.rng.nextInt(100) * d.rng.nextInt(5) + 1;
					Envelope genv = (Envelope)d.get("genv");
					Envelope fenv = (Envelope)d.get("fenv");
					genv.addSegment(0.7f + 0.3f * d.rng.nextFloat(), 10);
					int midi = 64 + d.rng.nextInt(5);
					midi = Pitch.forceToScale(midi, Pitch.dorian);	
					fenv.addSegment(Pitch.mtof(midi) + 5 * d.rng.nextFloat(),  d.rng.nextFloat() * 5000);
				}
			}
		};
		d.pattern(pattern);
		d.put("risingpattern", pattern);	
	}
	
	public void create(final DynamoPI d) {
		//create sound
		Envelope fenv = new Envelope(d.ac, 500);
		Envelope modFreq = new Envelope(d.ac, 200f);	
		modFreq.addSegment(100f, 30500);
		Envelope modAmount = new Envelope(d.ac, 0f);
		modAmount.addSegment(200f, 1500);
		WavePlayer mod = new WavePlayer(d.ac, modFreq, Buffer.SINE);
		Function freqmod = new Function(fenv, mod, modAmount) {
			@Override
			public float calculate() {
				return x[0] + (x[1] * x[2]); 
			}
		};
		WavePlayer wp = new WavePlayer(d.ac, freqmod, Buffer.SINE);
		Envelope genv = new Envelope(d.ac, 0);
		Gain g = new Gain(d.ac, 1, genv);
		g.addInput(wp);
		d.sound(g);
		d.put("rising", g);
		d.put("genv", genv);
		d.put("fenv", fenv);
		
	}
	
	public void kill(final DynamoPI d) {
		//kill sound
		final Gain g = (Gain)d.get("rising");
		Envelope genv = (Envelope)d.get("genv");
		if(genv != null) genv.addSegment(0, 5000, new KillTrigger(g));
		d.share.remove("rising");
		d.share.remove("genv");
		d.share.remove("fenv");
		Bead pattern = (Bead)d.get("risingpattern");
		if(pattern != null) pattern.kill();
		d.share.remove("risingpattern");
	}
	
	
}
