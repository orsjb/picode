package my_pipos.breaks;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import de.sciss.net.OSCListener;

public class PIPOKillBreak implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"localhost"});
	}
	
	@Override
	public void action(final DynamoPI d) {

		//store
		OSCListener listener = (OSCListener)d.get("amenctrl");
		GranularSamplePlayer gsp = (GranularSamplePlayer)d.get("amen");
		Gain g = (Gain)d.get("amengain");
		
		d.oscServer.removeOSCListener(listener);
		
		float pitch = gsp.getPitchUGen().getValue();
		Envelope e = new Envelope(d.ac, pitch);
		gsp.setPitch(e);
		e.addSegment(0, 4000 * d.rng.nextFloat(), new KillTrigger(g));
		
	}
	
}
