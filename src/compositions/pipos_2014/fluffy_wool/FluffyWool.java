package compositions.pipos_2014.fluffy_wool;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.network.NetworkCommunication.Listener;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.SendToPI;
import core.PIPO;
import de.sciss.net.OSCMessage;

public class FluffyWool implements PIPO {

	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) throws Exception {
		
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{
				//names of all of the devices you want to send this code to:
				"pisound-009e959c5093.local", 
				"pisound-009e959c47ef.local", 
				"pisound-009e959c4dbc.local", 
				"pisound-009e959c3fb2.local",
				"pisound-009e959c50e2.local",
				"pisound-009e959c47e8.local",
				"pisound-009e959c510a.local",
				"pisound-009e959c502d.local",
				});
	}
	
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
					
					//play a simple sine wave
					WavePlayer wp = new WavePlayer(d.ac, 500f, Buffer.SINE);
					Gain g = new Gain(d.ac, 1, 0.1f);
					g.addInput(wp);
					masterGain.addInput(g);
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
