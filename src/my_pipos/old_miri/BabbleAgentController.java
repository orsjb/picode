package my_pipos.old_miri;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import core.PIPO;

public class BabbleAgentController implements PIPO {

	private static final long serialVersionUID = 1L;

	@Override
	public void action(DynamoPI d) {

		BabbleAgentPIPO x = (BabbleAgentPIPO)d.get("x");
		if(x == null) return;
		
		
		
//		//live code zone... (doesn't work).
//		
//		x.looping = true;
//		x.loopStartMS = 50000;
//		x.setLoopLenMS(6000);
//		
//		int len = x.ringmodFreqMultPattern.buf.length;
////		for(int i = 0; i < len; i++) {
////			x.ringmodFreqMultPattern.buf[i] = 1;
////		}
//		
//		x.masterRingModFreq.addSegment(5000, 5000);
//		
		
	}
	

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"pi1"});
	}

}
