package my_pipos.example;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import dynamic.DynamoPI;
import dynamic.PIPO;

public class DynamicPIPO implements PIPO {
	
	@Override
	public void action(final DynamoPI d) {
		d.pattern(new Bead() {
			public void messageReceived(Bead message) {
				if(d.clock.isBeat()) {
					Sample s = SampleManager.randomFromGroup("snd");
					SamplePlayer sp = new SamplePlayer(d.ac, s);
					d.sound(sp);
				}
			}
		});
	}
	
	
	
}
