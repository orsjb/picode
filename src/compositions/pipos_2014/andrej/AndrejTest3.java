package compositions.pipos_2014.andrej;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.PauseTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.network.NetworkCommunication;
import controller.network.SendToPI;
import core.PIPO;
import de.sciss.net.OSCMessage;

public class AndrejTest3 implements PIPO {

	
	public static void main(String[] args) throws Exception {
		
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{
				
//			"pisound-009e959c5093.local", 
			"pisound-009e959c47ef.local", 
//			"pisound-009e959c4dbc.local", 
//			"pisound-009e959c3fb2.local",
//			"pisound-009e959c50e2.local",
//			"pisound-009e959c47e8.local",
//			"pisound-009e959c510a.local",
//			"pisound-009e959c502d.local",
			
			});
	}
	
	
	@Override
	public void action(DynamoPI d) {

		
		d.reset();

		final Envelope e = new Envelope(d.ac, 0);
		
		WavePlayer wp = new WavePlayer(d.ac, 500, Buffer.SINE);
		Gain g = new Gain(d.ac, 1, e);
		g.addInput(wp);
		d.ac.out.addInput(g);
		
		d.communication.addListener(new NetworkCommunication.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				try {
					if(msg.getName().equals("/PI/andrej/on")) {
						e.addSegment(0.02f, 4000);
					} else if(msg.getName().equals("/PI/andrej/off")) {
						e.addSegment(0f, 10000);
					} 
				} catch(Exception e) {
					//do nothing
				}
			}
		});
		
		
		
		
	}
	

}
