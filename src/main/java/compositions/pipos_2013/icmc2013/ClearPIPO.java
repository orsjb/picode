package compositions.pipos_2013.icmc2013;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;

public class ClearPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		d.reset();
	}

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
//		SendToPI.send(fullClassName, new String[] {"pi1", "pi2"});
//		SendToPI.send(fullClassName, new String[] {"pi3", "pi4", "pi5", "pi7", "pi8", "pi9"});
	}

}
