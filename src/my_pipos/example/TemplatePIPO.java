package my_pipos.example;

import my_pipos.miri.Recipients;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import dynamic.DynamoPI;
import dynamic.PIPO;
import dynamic.SendToPI;

public class TemplatePIPO implements PIPO {

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

	
	@Override
	public void action(final DynamoPI d) {
		
		
	}
	
	
	
}
