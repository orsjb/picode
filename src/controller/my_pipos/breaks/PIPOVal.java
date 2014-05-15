package controller.my_pipos.breaks;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;

public class PIPOVal implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"localhost"});
	}
	
	@Override
	public void action(final DynamoPI d) {
		
		d.put("state", "break");
		
//		GranularSamplePlayer sp = (GranularSamplePlayer)d.get("amen");
//		sp.getRateUGen().setValue(-0.000000001f);
//		sp.getGrainIntervalUGen().setValue(300);
		
//		WavePlayer wp = new WavePlayer(d.ac, 0.03f, Buffer.SINE);
//		Function f = new Function(wp) {
//			
//			@Override
//			public float calculate() {
//				// TODO Auto-generated method stub
//				return x[0] * 50f + 60f;
//			}
//		};
//		sp.setGrainInterval(f);
		
//		sp.getGrainSizeUGen().setValue(200);
//		sp.getRandomnessUGen().setValue(0.1f);
		
//		sp.getRateUGen().setValue(0.5f);
		
		
//		Gain g = (Gain)d.get("amengain");
//		g.getGainUGen().setValue(0.5f);
		
	}
	
}
