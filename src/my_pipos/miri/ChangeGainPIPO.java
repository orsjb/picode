package my_pipos.miri;

import dynamic.DynamoPI;
import dynamic.PIPO;
import dynamic.SendToPI;

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
