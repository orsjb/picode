package compositions.pipos_2013.icmc2013;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;

public class ChangeGainPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		float g = 0.7f;
//		float g = 1f;
		d.ac.out.setGain(g);
	}


	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
