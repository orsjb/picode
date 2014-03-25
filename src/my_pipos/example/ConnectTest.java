package my_pipos.example;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Socket s = new Socket("10.0.1.5", 1234);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
