package compositions.pipos_2013.breaks;

import controller.network.SendToPI;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;
import pi.dynamic.DynamoPI;
import pi.network.ControllerConnection;
import core.PIPO;
import de.sciss.net.OSCMessage;

public class PIPOPlayBreak implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"localhost"});
	}
	
	@Override
	public void action(final DynamoPI d) {
		
		
		final GranularSamplePlayer gsp = new GranularSamplePlayer(d.ac, SampleManager.sample("amen"));
		gsp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
		gsp.getRateUGen().setValue(0);
		final Gain g = new Gain(d.ac, 1, new Envelope(d.ac, 1));
		g.addInput(gsp);
		d.sound(g);
		
		ControllerConnection.Listener listener = new ControllerConnection.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("rate")) {
					float arg = (Float)msg.getArg(0);
					gsp.getRateUGen().setValue(arg);
				} else if(msg.getName().equals("pitch")) {
					float arg = (Float)msg.getArg(0);
					gsp.getPitchUGen().setValue(arg);
				} else if(msg.getName().equals("gint")) {
					float arg = (Float)msg.getArg(0);
					gsp.getGrainIntervalUGen().setValue(arg);
				} else if(msg.getName().equals("gain")) {
					float arg = (Float)msg.getArg(0);
					Envelope e = (Envelope)g.getGainUGen();
					if(!e.isLocked()) e.setValue(arg);
				}
			}
		};
		d.controller.addListener(listener);
		
		//store
		d.put("amenctrl", listener);
		d.put("amen", gsp);
		d.put("amengain", g);
		
		
	}
	
}
