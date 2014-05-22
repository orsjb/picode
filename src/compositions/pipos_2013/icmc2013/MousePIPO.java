package compositions.pipos_2013.icmc2013;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;

public class MousePIPO implements PIPO {
	
	public String name = "mouse";

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
//		SendToPI.send(fullClassName, "pi9");
	}
	
	@Override
	public void action(final DynamoPI d) {
		create(d);
//		kill(d);
	}
	
	public void create(final DynamoPI d) {	
		
		if(d.get(name) != null) return;
		
//		Sample s = SampleManager.sample("babble");	
		Sample s = SampleManager.sample("mouse");	
//		Sample s = SampleManager.sample("babble_all");	
		
		SamplePlayer sp = new SamplePlayer(d.ac, s);
		
		sp.getRateUGen().setValue(1 + d.rng.nextFloat() * 0.03f);
		
		double len = s.getLength();
		float offset = 20000;
		
		sp.getLoopStartUGen().setValue((float)len / 2 + offset);
		
		sp.getRateUGen().setValue((float)Math.pow(1, d.rng.nextInt(3) - 1));
		
		
		Envelope end = new Envelope(d.ac, (float)len / 2 + 400 * (d.myIndex() + 1) + offset);
		end.addSegment((float)len / 2 + 8000 * (d.myIndex() + 1) + offset, 30);
		end.addSegment((float)len / 2 + 20 + offset, 200000 * d.rng.nextFloat() + 10000);
		end.addSegment((float)len / 2 + 17 + offset, 10000 + d.myIndex() * 30);
		end.addSegment((float)len / 2 + 40 + offset, 10000);
		end.addSegment((float)len, 1000000);
		sp.setLoopEnd(end);
		
		
//		sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
		sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
		sp.setPosition(len / 2 + offset - 10);
		
		Envelope genv = new Envelope(d.ac, 0);
		genv.addSegment(10, 1000);
		
		Gain g = new Gain(d.ac, 1, genv);
		g.addInput(sp);
		d.sound(g);
		d.put(name, g);
		d.put(name + 1, genv);
		d.put(name + 2, sp);
	}
	
	public void kill(final DynamoPI d) {
		Envelope genv = (Envelope)d.get(name + 1);
		if(genv != null) {
			Bead g = (Bead)d.get(name);
			genv.addSegment(0, 10000, new KillTrigger(g));
			d.share.remove(name);
			d.share.remove(name + 1);
			d.share.remove(name + 2);
		}
	}

	
}
