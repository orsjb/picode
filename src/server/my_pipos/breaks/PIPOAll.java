package server.my_pipos.breaks;

import java.net.SocketAddress;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;
import pi.dynamic.DynamoPI;
import pi.network.ControllerConnection;
import server.network.SendToPI;
import core.PIPO;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;

public class PIPOAll implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"localhost"});
	}
	
	String state = "init";
	
	@Override
	public void action(final DynamoPI d) {
		//set up
		//demand that everything is reset (does not apply to audio files)
		d.resetLeaveSounding();
		d.put("state", "");
		//load samples
		SampleManager.sample("amen", "audio/amen-175.aif");
		SampleManager.sample("pianoA", "audio/piano.ff.A2.aiff");
		//this array is referenced later by the PIs, who should know their #id (not implemented yet)
		double[] distribution = {0.5, 1, 1.5, 2, 3, 2.5, 1.25};
		d.put("dist", distribution);
		//create the osc listener
		ControllerConnection.Listener listener = new ControllerConnection.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				//TODO
			}
		};
		//create the pattern
		Bead pattern = new Bead() {
			public void messageReceived(Bead message) {
				//poll state to see what to do next
				String nextState = (String)d.get("state");
				if(!nextState.equals(state)) {
					state = nextState;
					if(state.startsWith("break")) {
						//play break
						final GranularSamplePlayer gsp = new GranularSamplePlayer(d.ac, SampleManager.sample("amen"));
						gsp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
						gsp.getRateUGen().setValue(0);
						final Gain g = new Gain(d.ac, 1, new Envelope(d.ac, 1));
						g.addInput(gsp);
						d.sound(g);
						d.put("amen", gsp);
						d.put("amengain", g);
						//
					} else if(state.startsWith("piano")) {
						
						//stop break
						Gain g = (Gain)d.get("amengain");
						((Envelope)g.getGainUGen()).addSegment(0, 1000, new KillTrigger(g));
						//play piano
						SamplePlayer sp = new SamplePlayer(d.ac, SampleManager.sample("pianoA"));
						float rate = (float)Math.pow(2, d.rng.nextInt(3)) / (d.rng.nextInt(4) + 1);
						d.sound(sp);
						
					} else if(state.equals("pianoPattern")) {
						//TODO
					} else if(state.equals("stop")) {
						d.fadeOutClearSound(1000);
					}
					
				}
			}
		};
		//set up
		d.controller.addListener(listener);
		d.pattern(pattern);
		//store 
		d.put("oscctrl", listener);
		d.put("pattern", pattern);	
	}
	
}
