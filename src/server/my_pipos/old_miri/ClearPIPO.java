package server.my_pipos.old_miri;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import server.my_pipos.miri.Recipients;
import core.PIPO;

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