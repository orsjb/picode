package synch;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SenderTest {

	public static void main(String[] args) throws IOException {
		MulticastSocket s = new MulticastSocket();
		byte buf[] = new byte[10];
		for (int i=0; i<buf.length; i++) buf[i] = (byte)i;
		// Create a DatagramPacket 
		DatagramPacket pack = new DatagramPacket(buf, buf.length,
							 InetAddress.getByName("225.2.2.5"), 2225);
		// Do a send. Note that send takes a byte for the ttl and not an int.
		
		
//		int ttl = s.getTimeToLive(); 
//		s.setTimeToLive(newttl); 
		s.send(pack); 
//		s.setTimeToLive(ttl);
		
		// And when we have finished sending data close the socket
		s.close();
	}

}
