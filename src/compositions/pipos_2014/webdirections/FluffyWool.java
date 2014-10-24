package compositions.pipos_2014.webdirections;

import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import pi.dynamic.DynamoPI;
import pi.network.NetworkCommunication.Listener;
import pi.sensors.MiniMU.MiniMUListener;
import core.PIPO;
import de.sciss.net.OSCMessage;

public class FluffyWool implements PIPO {

	private static final long serialVersionUID = 1L;

	
	@Override
	public void action(final DynamoPI d) {
		
		d.reset();
		final Envelope masterGainCtrl = new Envelope(d.ac, 0);
		final Gain masterGain = new Gain(d.ac, 1, masterGainCtrl);
		d.sound(masterGain);
		
		//make a DIAD respond to incoming messages
		d.communication.addListener(new Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/sinewave")) {
					
				}
			}
		});
		
		//make the sound respond to the sensors
		d.mu.addListener(new MiniMUListener() {

			@Override
			public void accelData(double x, double y, double z) {
				if(z < 0) { 	//if the DIAD is upside-down
					masterGainCtrl.clear();
					masterGainCtrl.addSegment(0, 5000f); //fade out
				} else {
					masterGainCtrl.clear();
					masterGainCtrl.addSegment(1, 5000f); //fade in
				}
			}
			
		});

		
		
		
	}

}
