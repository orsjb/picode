package compositions.pipos_2013.breaks;

import controller.network.SendToPI;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import pi.dynamic.DynamoPI;
import pi.network.ControllerConnection;
import core.PIPO;

public class PIPOKillBreak implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"localhost"});
	}
	
	@Override
	public void action(final DynamoPI d) {

		//store
		ControllerConnection.Listener listener = (ControllerConnection.Listener)d.get("amenctrl");
		GranularSamplePlayer gsp = (GranularSamplePlayer)d.get("amen");
		Gain g = (Gain)d.get("amengain");
		
		d.controller.removeListener(listener);
		
		float pitch = gsp.getPitchUGen().getValue();
		Envelope e = new Envelope(d.ac, pitch);
		gsp.setPitch(e);
		e.addSegment(0, 4000 * d.rng.nextFloat(), new KillTrigger(g));
		
	}
	
}
