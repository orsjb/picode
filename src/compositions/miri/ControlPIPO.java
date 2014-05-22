package compositions.miri;

import controller.network.SendToPI;
import pi.dynamic.DynamoPI;
import core.PIPO;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;

public class ControlPIPO implements PIPO {

	@Override
	public void action(DynamoPI d) {
		
		d.ac.out.setGain(0.9f);
		
		//sample
//		d.put("s", "babble");
//		d.share.remove("s");
		
		//id
//		int id = d.myIndex() + 1;
		
		//pos (works in no loop mode)
//		Long pos = (Long)d.get("p");
		
		//get ugens
//		Envelope looperFreq = (Envelope)d.get("loopFreq");
//		Envelope looperStart = (Envelope)d.get("loopStart");
//		Envelope masterGain = (Envelope)d.get("masterGain");
//		Envelope rfreq = (Envelope)d.get("rfreq");
//		Envelope rwet = (Envelope)d.get("rwet");
//		WavePlayer looper = (WavePlayer)d.get("looper");

		///////////////
		//play area..//
		///////////////
//		masterGain.clear();
//		masterGain.addSegment(2f, 10000f);
		

//		d.put("p", new Long(0));
		
//		looperStart.setValue((1 * 60 + 2) * 1000);
		
//		looperStart.setValue((float)d.ac.samplesToMs(pos));
//		looper.setPhase(0);

//		looperStart.setValue(-1);
//		looperFreq.setValue(1000f / 102f);
		
//		rfreq.setValue(4000);
//		rwet.setValue(2f);
//		rwet.setValue(1);
		
		

		//get buffers
//		Buffer rmfreqp = (Buffer)d.get("rfreqLoop");
//		Buffer rmwetp = (Buffer)d.get("rwetLoop");
//		Buffer gp = (Buffer)d.get("gLoop");
//		
//		int bufferSize = 8192 * 2;
//		for(int i = 0; i < bufferSize; i++) {
//			int mult = 1+i*4/bufferSize;
//			rmfreqp.buf[i] = mult * 2;
//		
//			
//			rmwetp.buf[i] = i > 400? 0 : 1;
//			gp.buf[i] = i > 500? 0 : 1;
//			
//		}
		
		
	}


	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, Recipients.list);
	}

}
