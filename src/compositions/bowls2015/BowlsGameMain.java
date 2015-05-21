package compositions.bowls2015;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.SendToPI;
import core.EZShell;
import core.PIPO;
import core.Synchronizer.BroadcastListener;

/**
 * 
 * TODO:
 * 
 *  > how does the Pi get its ID?? perhaps last 4 digits of hostname?
 *  > check different states, broadcast states, work out who is being played
 *  > 
 * 
 * 
 * @author ollie
 *
 */


public class BowlsGameMain implements PIPO {
	
	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		System.out.println("The path will be run: " + fullClassName);
		SendToPI.send(fullClassName, new String[]{
			"localhost"
			});
	}
	public enum MovementState {
		UNKNOWN, STILL, ROLLING, SLIGHT, FREEFALL
	}
	
	private static final long serialVersionUID = 1L;

	int[] blues = {0, 3, 5, 6, 7, 10, 12, 15, 17, 18, 19, 22, 24, 27, 29, 30, 31, 34, 36, 39, 41, 42, 43, 46, 48};
	int[] mel = {0, 0, 0, 1, 2, 3, 0, 0, 0, 1, 2, 3, 0, 3, 3, 1, 2, 4, 4, 4, 3, 2, 1, 12, 13, 14, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11, 11, 11, 12, 9, 7, 5, 3, 2, 0, 0};
	int[] offsets = {0, 5, 7, 12, 17, 19, 24, 29};
	
	String myID;
	MovementState state;

	@Override
	public void action(final DynamoPI d) {
		String hostname = EZShell.call("hostname");
		if(hostname != null) {
			myID = hostname.substring(hostname.length() - 5, hostname.length() - 1);
		} else {
			myID = "" + d.rng.nextInt(10) + "" + d.rng.nextInt(10) + "" + d.rng.nextInt(10) + "" + d.rng.nextInt(10);
		}
		state = MovementState.UNKNOWN;
		
		//Let the game begin
		
		//test audio
		final Envelope freq = new Envelope(d.ac, 500f);
		WavePlayer wp = new WavePlayer(d.ac, freq, Buffer.SINE);
		Gain g = new Gain(d.ac, 1, 0.1f);
		g.addInput(wp);
		d.sound(g);
		
		d.pattern(new Bead() {
			public void messageReceived(Bead msg) {
				//TODO ?? Do something with a pattern??
			}
		});
		
		d.mu.addListener(new MiniMUListener() {

			@Override
			public void accelData(double x, double y, double z) {
				// TODO Auto-generated method stub
				super.accelData(x, y, z);
			}

			@Override
			public void gyroData(double x, double y, double z) {
				// TODO Auto-generated method stub
				super.gyroData(x, y, z);
			}

			@Override
			public void magData(double x, double y, double z) {
				// TODO Auto-generated method stub
				super.magData(x, y, z);
			}
			
		});
		
		//use d.synch.broadcast("message"); to broadcast things
		
		//use d.synch.doAtTime(Runnable) to make something happen at a synchronised time
		
		//use d.synch.addBroadcastListener(new BroadcastListener()); to listen to things
		d.synch.addBroadcastListener(new BroadcastListener() {
			@Override
			public void messageReceived(String s) {
				//TODO
			}
		});
		

		
	}
}
