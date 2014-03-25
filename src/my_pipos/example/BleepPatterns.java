
package my_pipos.example;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import dynamic.DynamoPI;
import dynamic.PIPO;
import dynamic.SendToPI;

public class BleepPatterns implements PIPO {

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"pi9"});
	}
	
	@Override
	public void action(final DynamoPI d) {

		
		d.reset();
		
		int id = d.myIndex();
		

		Bead pattern = new Bead() {
			public void messageReceived(Bead message) {
				
				if(d.clock.isBeat()) {
				
				
					Noise n = new Noise(d.ac);
					Envelope e = new Envelope(d.ac, 1);
					Gain g = new Gain(d.ac, 1, e);
					//
					
				}
			}
		};
		
		d.pattern(pattern);
		
		
		
		
//		d.sound(c);
		
		
		
		
		
		
	}
	
}
