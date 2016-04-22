package compositions.pipos_2013.miri;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;

public class ChangeGainPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		d.ac.out.setGain(1f);
	}


	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
