package compositions.pipos_2015.sam;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.*;
import core.PIPO;

public class Ubicomp2015DataCollection implements PIPO {
	
	public static void main(String[] args) throws Exception {
		
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{
				
//			"pisound-009e959c5093.local", 
//			"pisound-009e959c47ef.local", 
//			"pisound-009e959c4dbc.local", 
//			"pisound-009e959c3fb2.local",
//			"pisound-009e959c50e2.local",
			"pisound-009e959c47e8.local"
//			"pisound-009e959c510a.local",
//			"pisound-009e959c502d.local",
			
		});
	}
	
	Glide xAccFactor, yAccFactor, zAccFactor, xGyrFactor, yGyrFactor, zGyrFactor, xMagFactor, yMagFactor, zMagFactor;
	
	@Override
	public void action(DynamoPI d) {
		
		d.reset();
		setupMu(d);
		Function func = new Function(xGyrFactor) {
			public float calculate() {
				return (x[0] + 2) * 500f; 
			}
		};
		WavePlayer wp = new WavePlayer(d.ac, func, Buffer.SINE);
		Gain g = new Gain(d.ac, 1, 0.02f);
		g.addInput(wp);
		d.ac.out.addInput(g);
	}
	
	private void setupMu(final DynamoPI d) {
		//mu
		//set up Mu responder
		xAccFactor = new Glide(d.ac, 0, 1);
		yAccFactor = new Glide(d.ac, 0, 1);
		zAccFactor = new Glide(d.ac, 0, 1);
		xGyrFactor = new Glide(d.ac, 0, 1);
		yGyrFactor = new Glide(d.ac, 0, 1);
		zGyrFactor = new Glide(d.ac, 0, 1);
		xMagFactor = new Glide(d.ac, 0, 1);
		yMagFactor = new Glide(d.ac, 0, 1);
		zMagFactor = new Glide(d.ac, 0, 1);
		

		d.mu.addListener(new MiniMUListener() {
			
			@Override
			public void accelData(double xA, double yA, double zA) {
				float scaledX = scaleMU((float)xA);
				xAccFactor.setValue(scaledX);
				float scaledY = scaleMU((float)yA);
				yAccFactor.setValue(scaledY);
				float scaledZ = scaleMU((float)zA);
				zAccFactor.setValue(scaledZ);
				String AccString = scaledX + " " + scaledY + " " + scaledZ;
				d.communication.broadcastOSC("/accelbaby", new Object[] {AccString});
			}
			
			@Override
			public void gyroData(double xG, double yG, double zG){
				float scaledX = scaleMU((float)xG);
				xGyrFactor.setValue(scaledX);
				float scaledY = scaleMU((float)yG);
				yGyrFactor.setValue(scaledY);
				float scaledZ = scaleMU((float)zG);
				zGyrFactor.setValue(scaledZ);
				String GyrString = scaledX + " " + scaledY + " " + scaledZ;
				d.communication.broadcastOSC("/gyrobaby", new Object[] {GyrString});
			}

			public void magData(double xM, double yM, double zM){
				float scaledX = scaleMU((float)xM);
				xMagFactor.setValue(scaledX);
				float scaledY = scaleMU((float)yM);
				yMagFactor.setValue(scaledY);
				float scaledZ = scaleMU((float)zM);
				zMagFactor.setValue(scaledZ);
				String MagString = scaledX + " " + scaledY + " " + scaledZ;
				d.communication.broadcastOSC("/PI/mag", new Object[] {MagString});
			}
			
		});
		


	}
	
	private float scaleMU(float x) {
		//TODO	 - output between -1 and 1 (using tanh?)
		return (float)Math.tanh(x / 250);
	}

}
