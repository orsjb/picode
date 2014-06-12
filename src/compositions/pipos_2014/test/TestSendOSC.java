package compositions.pipos_2014.test;

import controller.network.PIConnection;

public class TestSendOSC {

	public static void main(String[] args) {
		PIConnection pic = new PIConnection();
		pic.sendToAllPIs("/PI/noise/on");
	}

}
