package my_pipos.breaks;

import java.net.SocketAddress;

import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import dynamic.DynamoPI;
import dynamic.PIPO;
import dynamic.SendToPI;

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
		
		OSCListener listener = new OSCListener() {
			@Override
			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
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
		d.oscServer.addOSCListener(listener);
		
		//store
		d.put("amenctrl", listener);
		d.put("amen", gsp);
		d.put("amengain", g);
		
		
	}
	
}
