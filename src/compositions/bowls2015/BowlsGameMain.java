package compositions.bowls2015;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import controller.network.SendToPI;
import core.PIPO;
import core.Synchronizer.BroadcastListener;

public class BowlsGameMain implements PIPO {
	
	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		System.out.println("The path will be run: " + fullClassName);
		SendToPI.send(fullClassName, new String[]{
				"localhost"
				});
	}
	
	private static final long serialVersionUID = 1L;

	@Override
	public void action(final DynamoPI d) {
		
		//Let the game begin
		
		final Envelope freq = new Envelope(d.ac, 500f);
		WavePlayer wp = new WavePlayer(d.ac, freq, Buffer.SINE);
		Gain g = new Gain(d.ac, 1, 0.1f);
		g.addInput(wp);
		d.sound(g);
		
		
		//use d.synch.broadcast("message"); to broadcast things
	
		//use d.synch.addBroadcastListener(new BroadcastListener()); to listen to things
		
		d.synch.addBroadcastListener(new BroadcastListener() {
			@Override
			public void messageReceived(String s) {
				freq.setValue((float)(Math.random() * 10000f));
			}
		});
		
		new Thread() {
			public void run() {
				while(true) {
					d.synch.broadcast("x");
					try {
						Thread.sleep((int)(Math.random() * 4000 + 1000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
	}
}
