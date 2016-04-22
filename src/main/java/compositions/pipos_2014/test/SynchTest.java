package compositions.pipos_2014.test;

import net.beadsproject.beads.core.Bead;
import pi.dynamic.DynamoPI;
import pi.network.NetworkCommunication.Listener;
import controller.network.SendToPI;
import core.PIPO;
import de.sciss.net.OSCMessage;

public class SynchTest implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{ 
				"pisound-009e959c50e2.local",
				"pisound-009e959c502d.local",
				});
	}
	
	@Override
	public void action(final DynamoPI d) {
		d.reset();
		
//		d.communication.addListener(new Listener() {
//			
//			@Override
//			public void msg(OSCMessage msg) {
//
//				if(msg.getName().equals("sblp")) {
//					
//					d.synch.doAtNextStep(new Runnable() {
//						
//						@Override
//						public void run() {
//							d.testBleep();
//						}
//					}, 5000);
//					
//				}
//			}
//		});
		
		
		step(d);
		
		
	}
	
	public void step(final DynamoPI d) {
		d.synch.doAtNextStep(new Runnable() {

			@Override
			public void run() {
				d.testBleep();
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				step(d);
			}
		}, 500);
	}

}
