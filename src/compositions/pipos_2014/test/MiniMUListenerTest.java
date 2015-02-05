package compositions.pipos_2014.test;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.Static;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.SendToPI;
import core.PIPO;

public class MiniMUListenerTest implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{
//				"pisound-009e959c5093.local", 
//				"pisound-009e959c510a.local", 
//				"pisound-009e959c502d.local",
//				"pisound-009e959c4dbc.local",
//				"pisound-009e959c50e2.local",
				"pisound-009e959c47e8.local"
				});
	}
	
	@Override
	public void action(final DynamoPI d) {
		
		d.reset();
		d.startAudio();
	
		//controllers
		final Glide freqCtrl = new Glide(d.ac, 500);
		final Glide rateCtrl = new Glide(d.ac, 0.1f);
		//set up signal chain
		d.clock.addMessageListener(new Bead() {
			public void messageReceived(Bead msg) {
				if(d.clock.isBeat()) {
					final float baseF = Pitch.mtof(Pitch.forceToScale(d.rng.nextInt(40) + 20, Pitch.pentatonic));
					Function f = new Function(freqCtrl) {
						@Override
						public float calculate() {
							return  baseF + x[0];
						}
					};
					
					WavePlayer wp = new WavePlayer(d.ac, f, Buffer.SQUARE);
					Envelope e = new Envelope(d.ac, 0.f);
					Gain g = new Gain(d.ac, 1, e);
					e.addSegment(0.05f, 1);
					e.addSegment(0, 200, new KillTrigger(g));
					
					g.addInput(wp);
					d.sound(g);
				}
			}
		});
		
//		d.clock.setIntervalEnvelope(rateCtrl);
		d.clock.setIntervalEnvelope(new Static(d.ac, 500));
		
		
		//get listening to data
		MiniMUListener myListener = new MiniMUListener() {
			
			public void accelData(double x, double y, double z) {
				String AccString = String.format("MiniMu Acc X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
				
				
				System.out.println(AccString);
				freqCtrl.setValue(((float)Math.abs(x) * 1f));
				rateCtrl.setValue(4000f * (((float)Math.abs(y) * 3f) % 400f / 1600f + 0.01f));
//				rateCtrl.setValue(2000f * (((float)Math.abs(y) * 1f) + 0.01f));
				d.communication.broadcastOSC("/MinimuListener", new Object[] {AccString});
			}
			
			public void gyroData(double x, double y, double z) {
				String GyrString = String.format("MiniMu Gyr X/Y/Z = %05.2f %05.2f %05.2f", x,y,z);
				System.out.println(GyrString);
			}
			
		};
		d.mu.addListener(myListener);
		
		
		
	}

}
