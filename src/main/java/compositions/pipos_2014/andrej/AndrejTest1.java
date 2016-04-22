package compositions.pipos_2014.andrej;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import controller.network.SendToPI;
import core.PIPO;

public class AndrejTest1 implements PIPO {

	
	public static void main(String[] args) throws Exception {
		
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{
				
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
	public void action(DynamoPI d) {

		
		d.reset();
		
		WavePlayer wp = new WavePlayer(d.ac, 500, Buffer.SINE);
		Gain g = new Gain(d.ac, 1, 0.05f);
		g.addInput(wp);
		d.ac.out.addInput(g);
		
		
		
		
	}

}
