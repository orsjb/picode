package compositions.breaks;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;

public class PIPOLoadData implements PIPO {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[] {"localhost"});
	}
	
	@Override
	public void action(final DynamoPI d) {
		//this array is referenced later by the PIs, who know their #id
		double[] distribution = {0.5, 1, 1.5, 2, 3, 2.5, 1.25};
		d.put("dist", distribution);
	}
	
}
