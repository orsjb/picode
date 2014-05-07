package server.my_pipos.example;

import pi.dynamic.DynamoPI;
import server.dynamic.SendToPI;
import server.my_pipos.miri.Recipients;
import core.PIPO;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class TemplatePIPO implements PIPO {

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

	
	@Override
	public void action(final DynamoPI d) {
		
		
	}
	
	
	
}
