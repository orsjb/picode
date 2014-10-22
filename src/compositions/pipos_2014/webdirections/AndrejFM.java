package compositions.pipos_2014.webdirections;

import java.util.List;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.TapIn;
import net.beadsproject.beads.ugens.TapOut;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.network.NetworkCommunication.Listener;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.SendToPI;
import core.PIPO;
import de.sciss.net.OSCMessage;

public class AndrejFM extends MiniMUListener implements PIPO {

	
	final float[][] MASTER_PRESET = {{0.7f,  50},{0.2f, 100},{0.6f, 200},{0.0f, 300}, //Gain
			{600, 100},{250, 300},{800, 100},{0.0f,  50}, //CF
			{50,  200},{50, 50},{50,   50},{0.0f, 300}, //MF
			{80f,  50},{80f, 400},{20f,  50},{0.0f, 300}}; //MD
	
	final float[][] SECOND_PRESET = {{0.7f,  50},{0.2f, 100},{0.6f, 200},{0.0f, 300}, //Gain
			{600, 100},{250, 300},{800, 100},{0.0f,  50}, //CF
			{50,  200},{50, 50},{50,   50},{0.0f, 300}, //MF
			{80f,  50},{80f, 400},{20f,  50},{0.0f, 300}}; //MD

	final float[][] THIRD_PRESET = {{0.7f,  50},{0.2f, 100},{0.6f, 200},{0.0f, 300}, //Gain
			{600, 100},{250, 300},{800, 100},{0.0f,  50}, //CF
			{50,  200},{50, 50},{50,   50},{0.0f, 300}, //MF
			{80f,  50},{80f, 400},{20f,  50},{0.0f, 300}}; //MD

	
	private static final long serialVersionUID = 1L;

	Envelope gainEnvelope;
	Envelope carrierFreqEnvelope; // connect to to amount added to function
	Envelope modulatorFreqEnvelope; // connect to modulator freqModulator frequency
	Envelope modDepthEnvelope;
	
	List<Long> events;		//a history of recent event times, used to match other PIs
	
	DynamoPI d;
	
	boolean[] activeAgents = new boolean[20];

	float[][] mutate(float[][] original) {
		float[][] newArray = new float[16][2];

		//TODO

		return newArray;
	}

	float[][] crossover(float[][] source1, float[][] source2) {
		float[][] newArray = new float[16][2];

		//TODO

		return newArray;
	}


	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{
//				"pisound-009e959c5093.local", 
//				"pisound-009e959c47ef.local", 
//				"pisound-009e959c4dbc.local", 
//				"pisound-009e959c3fb2.local",
//				"pisound-009e959c50e2.local",
				"pisound-009e959c47e8.local",
//				"pisound-009e959c510a.local",
//				"pisound-009e959c502d.local",
		});
	}

	@Override
	public void action(final DynamoPI d) {
		this.d = d;
		d.reset();
		
		d.ac.out.getGainUGen().setValue(0.5f);
		
		modulatorFreqEnvelope = new Envelope(d.ac, 0.0f);
		WavePlayer freqModulator = new WavePlayer(d.ac, modulatorFreqEnvelope, Buffer.SINE);
		modDepthEnvelope = new Envelope(d.ac, 0.0f);
		carrierFreqEnvelope = new Envelope(d.ac, 0.0f);
		Function modulationFunction = new Function(freqModulator, carrierFreqEnvelope, modDepthEnvelope) {
			public float calculate() {
				return (x[0] * x[2]) + x[1]; //figure out how to plug carrierEnvelope into this
			}
		};
		WavePlayer carrier = new WavePlayer(d.ac, modulationFunction, Buffer.SINE);
		gainEnvelope = new Envelope(d.ac, 0.0f);
		Gain carrierGain = new Gain(d.ac, 1, gainEnvelope);

		TapIn delayIn = new TapIn(d.ac, 5000);
		delayIn.addInput(carrierGain); // connect synth gain to delay
		TapOut delayOut = new TapOut(d.ac, delayIn, 200.0f);
		Gain delayGain = new Gain(d.ac, 1, 0.50f);
		delayGain.addInput(delayOut);
		delayIn.addInput(delayGain); // feedback

		carrierGain.addInput(carrier);
		d.ac.out.addInput(carrierGain);
		d.ac.out.addInput(delayGain); //connect delay output to audio context


		//set up sensor response
		d.mu.addListener(this);
		
		//listen for other's messages
		
		d.communication.addListener(new Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/shaken")) {
					int id = (Integer)msg.getArg(0);
					activeAgents[id] = true;
					

					//TODO WHAT HAPPENS HERE?
					
					
				} else if(msg.getName().equals("/inactive")) {
					int id = (Integer)msg.getArg(0);
					activeAgents[id] = false;
				}
			}
		});
	}
	
	void setupBeat() {
		//set up how to trigger the sound
		Bead b = new Bead() {
			public void messageReceived(Bead m) {
				if(d.clock.isBeat() && d.clock.getBeatCount() % 4 == 0) {
					trigger(MASTER_PRESET);
				}
			}
		};
		//add it
		d.pattern(b);
	}

	void trigger(float[][] cellID) {
		//Gain
		gainEnvelope.clear();
		gainEnvelope.addSegment(cellID[0][0], cellID[0][1]); // over 50 ms rise to 0.8
		gainEnvelope.addSegment(cellID[1][0], cellID[1][1]);
		gainEnvelope.addSegment(cellID[2][0], cellID[2][1]);
		gainEnvelope.addSegment(cellID[3][0], cellID[3][1]); // over 300ms fall to 0.0
		// Carrier Frequency
		carrierFreqEnvelope.clear();
		carrierFreqEnvelope.addSegment(cellID[4][0], cellID[4][1]);
		carrierFreqEnvelope.addSegment(cellID[5][0], cellID[5][1]);
		carrierFreqEnvelope.addSegment(cellID[6][0], cellID[6][1]);
		carrierFreqEnvelope.addSegment(cellID[7][0], cellID[7][1]);
		//Modulator Frequency
		modulatorFreqEnvelope.clear();
		modulatorFreqEnvelope.addSegment(cellID[8][0], cellID[8][1]);
		modulatorFreqEnvelope.addSegment(cellID[9][0], cellID[9][1]);
		modulatorFreqEnvelope.addSegment(cellID[10][0], cellID[10][1]);
		modulatorFreqEnvelope.addSegment(cellID[11][0], cellID[11][1]);
		//Modulation Depth
		modDepthEnvelope.clear();
		modDepthEnvelope.addSegment(cellID[12][0], cellID[12][1]);
		modDepthEnvelope.addSegment(cellID[13][0], cellID[13][1]);
		modDepthEnvelope.addSegment(cellID[14][0], cellID[14][1]);
		modDepthEnvelope.addSegment(cellID[15][0], cellID[15][1]);
	}

	double prevX = 0, prevY = 0, prevZ = 0;
	
	@Override
	public void accelData(double x, double y, double z) {
		
	}
	
	int count;
	
	@Override
	public void gyroData(double x, double y, double z) {
		double mx = scaleMU(x);
		double my = scaleMU(y);
		double mz = scaleMU(z);
		double dx = mx - prevX;
		double dy = my - prevY;
		double dz = mz - prevZ;
		double v = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(v > 0.5) {
			shake();
			count = 0;
		}
		count++;
		if(count > 2000) {			//TODO check if this is a good time out
			inactive();
		}
		prevX = mx; prevY = my; prevZ = mz;
	}
	
	public void shake() {
		d.communication.sendEveryone("/shaken", new Object[] {d.myIndex()});
		
		
		//TODO WHAT HAPPENS HERE?
		
		trigger(MASTER_PRESET);
		
		
	}
	
	public void inactive() {
		d.communication.sendEveryone("/inactive", new Object[] {d.myIndex()});
	}

	private double scaleMU(double x) {
		return Math.tanh(x / 2500.);
	}
	

	
	
	
	


}




