
package compositions.pipos_2013.example;

import controller.network.SendToPI;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU;
import core.PIPO;

public class BleepPatterns implements PIPO {

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"pi9"});
	}
	
	@Override
	public void action(final DynamoPI d) {

		
		d.reset();
		
		int id = d.myIndex();
		
		
		final Glide g = new Glide(d.ac);

		WavePlayer wp = new WavePlayer(d.ac, g, Buffer.SINE);
		d.pl.addInput(wp);
		
		
		MiniMU.MiniMUListener myListener = new MiniMU.MiniMUListener() {

			@Override
			public void accelData(double x, double y, double z) {
				g.setValue((float)x);
			}
			
			public void freefallEvent() {
				//
			}
			
		};
		d.mu.addListener(myListener);
		
		
		
		
//		d.sound(c);
		
		
		
		
		
		
	}
	
}
