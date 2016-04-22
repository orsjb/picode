package compositions.pipos_2014.test;

import controller.network.PIConnection;
import core.ControllerConfig;

public class TestSendOSC {

	public static void main(String[] args) {
		PIConnection pic = new PIConnection(new ControllerConfig());
		pic.sendToAllPIs("/PI/noise/on");
	}

}
