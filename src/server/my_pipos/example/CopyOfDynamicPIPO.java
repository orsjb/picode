package server.my_pipos.example;

import java.net.SocketAddress;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;

public class CopyOfDynamicPIPO implements PIPO {

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"pi1"});
	}
	
	@Override
	public void action(final DynamoPI d) {

		d.reset();
		
		//simple set of random tones
		
//		int N = 2;
//		final Gain g = new Gain(d.ac, 1, 1f/N);
//		for(int i = 0; i < N; i++) {
//			WavePlayer wp = new WavePlayer(d.ac, 400 + (float)Math.random() * 500f, Buffer.SINE);
//			g.addInput(wp);
//		}
//		d.sound(g);
		

		
//		OSCListener l = new OSCListener() {
//			
//			@Override
//			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
//				g.setGain((Float)msg.getArg(0));
//			}
//		};
//		d.oscServer.addOSCListener(l);
		
//		d.sound(new Noise(d.ac));
	}
	
}
