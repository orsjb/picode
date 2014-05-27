package compositions.pipos_2014.test;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.SendToPI;
import core.PIPO;

public class MUListenerTest2 implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{
//				"pisound-009e959c5093.local", 
//				"pisound-009e959c510a.local", 
				"pisound-009e959c502d.local"
				});
	}
	
	@Override
	public void action(final DynamoPI d) {
		
		d.reset();
//		d.startAudio();
//		
		//controllers
//		final Glide freqCtrl = new Glide(d.ac, 500);
//		final Glide gainCtrl = new Glide(d.ac, 0.1f);
//		//set up signal chain
//		WavePlayer wp = new WavePlayer(d.ac, freqCtrl, Buffer.SQUARE);
//		Gain g = new Gain(d.ac, 1, gainCtrl);
//		g.addInput(wp);
//		d.sound(g);
//		//get listening to data
//		MiniMUListener myListener = new MiniMUListener() {
//			
//			public void accelData(double x, double y, double z) {
//				String AccString = String.format("MiniMu Acc X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
//
//				System.out.println(AccString);
//				freqCtrl.setValue(((float)Math.abs(x) * 1f) % 5000f + 100f);
//				gainCtrl.setValue(((float)Math.abs(y) * 10f) % 400f / 1600f + 0.1f);
//			}
//			
//			public void gyroData(double x, double y, double z) {
//				String GyrString = String.format("MiniMu Gyr X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
//				System.out.println(GyrString);
//			}
//			
//		};
//		d.mu.addListener(myListener);
	}

}
