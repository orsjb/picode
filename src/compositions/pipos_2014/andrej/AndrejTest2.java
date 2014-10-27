package compositions.pipos_2014.andrej;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.SendToPI;
import core.PIPO;

public class AndrejTest2 implements PIPO {

	
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
	
	Glide xFactor, yFactor, zFactor;
	
	@Override
	public void action(DynamoPI d) {

		
		d.reset();
		setupMu(d);
		
		
		
		
		Function func = new Function(xFactor) {
			public float calculate() {
				return (x[0] + 2) * 500f; 
			}
		};
		
		WavePlayer wp = new WavePlayer(d.ac, func, Buffer.SINE);
		Gain g = new Gain(d.ac, 1, 0.02f);
		g.addInput(wp);
		d.ac.out.addInput(g);
		
		
		
		
	}
	
	private void setupMu(DynamoPI d) {
		//mu
		//set up Mu responder
		xFactor = new Glide(d.ac, 0, 100);
		yFactor = new Glide(d.ac, 0, 100);
		zFactor = new Glide(d.ac, 0, 100);
		d.mu.addListener(new MiniMUListener() {
			@Override
			public void accelData(double x, double y, double z) {
//				System.out.println(x + " " + y + " " + z);
				float scaledX = scaleMU((float)x);
				xFactor.setValue(scaledX);
				float scaledY = scaleMU((float)y);
				yFactor.setValue(scaledY);
				float scaledZ = scaleMU((float)z);
				zFactor.setValue(scaledZ);
//				System.out.println(scaledX + " " + scaledY + " " + scaledZ);
			}
		});
	}
	
	private float scaleMU(float x) {
		//TODO	 - output between -1 and 1 (using tanh?)
		return (float)Math.tanh(x / 250);
	}

}
