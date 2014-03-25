package my_pipos.old_miri;

import my_pipos.miri.Recipients;
import dynamic.DynamoPI;
import dynamic.PIPO;
import dynamic.SendToPI;

public class ClearPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		d.reset();
	}


	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
