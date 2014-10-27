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
import pi.sensors.MiniMU.MiniMUListener;
import core.PIPO;
import core.Synchronizer.BroadcastListener;

public class QuorumSensingAndrej extends MiniMUListener implements PIPO {


	
	private static final long serialVersionUID = 1L;

	Envelope gainEnvelope;
	Envelope carrierFreqEnvelope; // connect to to amount added to function
	Envelope modulatorFreqEnvelope; // connect to modulator freqModulator frequency
	Envelope modDepthEnvelope;
	
	final static int ID1 = 6;
	final static int ID2 = 2;
	final static int ID3 = 3;
	final static int ID4 = 4;
	
	List<Long> events;		//a history of recent event times, used to match other PIs
	
	DynamoPI d;
	
	boolean[] activeAgents = new boolean[20];

	
	
	
	public float[][] masterSound() {
		float n1 = d.rng.nextFloat() * 1000f + 2000f;
	    float n2 = d.rng.nextFloat() * 1000f + 2000f;
	    float n3 = d.rng.nextFloat() * 1000f + 2000f;
	    float n4 = d.rng.nextFloat() * 1000f + 2000f;
		float[][] f = {{0.8f,  50},{0.8f, 300},{0.5f, 200},{0.0f, 1250}, //Gain 1800
	            {n1, 100},{n2, 1000},{n3, 200},{n4,  400}, //CF
	            {n1/16,  200},{n2/16,   50},{n3/16,   50},{n4/16, 300}, //MF
	            {100,  50},{850, 500},{1500,  50},{1500, 1200}}; //MD
		return f;
	}
	
	public float[][] secondSound() {
	float n1 = d.rng.nextFloat() * 1500f + 2000;
    float n2 = d.rng.nextFloat() * 1500f + 2000;
    float n3 = d.rng.nextFloat() * 1500f + 2000;
    float n4 = d.rng.nextFloat() * 1500f + 2000;
    float n5 = d.rng.nextFloat() * 1500f + 2000;
    float n6 = d.rng.nextFloat() * 1500f + 2000;
    float n7 = d.rng.nextFloat() * 1500f + 2000;
	float[][] f2 = {{0.8f,  30},{0.8f, 620},{0.8f, 620},{0.0f, 30}, //Gain 1300
            {n1, 100},{n2, 100},{n3, 500},{n4,  100},{n5, 300},{n6, 100},{n7, 100}, //CF
            {n1/18,  500},{n2/18, 300},{n3/18, 200},{n4/18, 300}, //MF
            {100,  100},{1100, 500},{500,  100},{800, 200},{300,100},{900,100},{1100,100}}; //MD
	return f2;
	}

	public float[][] thirdSound() {
		float n1 = d.rng.nextFloat() * 2600f + 2000f;
	    float n2 = d.rng.nextFloat() * 2600f + 2000f;
		float[][] f3 = {{0.8f,  375},{0.5f, 375},{0.8f, 375},{0.0f, 375}, //Gain 1500
                {n1, 500},{n1, 500},{n2, 10},{n2,  490}, //CF
                {n1/19,  200},{n2/19, 200},{n1/19,  1000},{n2/19, 100}, //MF
                {100,  250},{100, 250},{1400,  500},{1400, 500}}; //MD
		return f3;
	}
	
	//etc....


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
		
		d.synch.addBroadcastListener(new BroadcastListener() {
			@Override
			public void messageReceived(String msg) {
				String[] msgParts = msg.split("[ ]");
				if(msgParts[0].equals("/shaken")) {
					int id = Integer.parseInt(msgParts[1]);
					activeAgents[id] = true;
					System.out.println("Received /shaken message from " + id);
					if(id != d.myIndex()) {
						respond();
					}
				} else if(msgParts[0].equals("/inactive")) {
					int id = Integer.parseInt(msgParts[1]);
					activeAgents[id] = false;
					System.out.println("Received /inactive message from " + id);
				}
			}
		});
	}
	
	void setupBeat() {
		//set up how to trigger the sound
		Bead b = new Bead() {
			public void messageReceived(Bead m) {
				if(d.clock.isBeat() && d.clock.getBeatCount() % 4 == 0) {
					// trigger(MASTER_PRESET);
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
	boolean active = false;
	
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
			active = true;
			count = 0;
		}
		count++;
		if(count > 1000 && active == true) {
			active = false;
			inactive();
		}
		prevX = mx; prevY = my; prevZ = mz;
	}
	
	public void shake() {
		System.out.println("Sending /shaken message.");
		act();
		d.synch.broadcast("/shaken " + d.myIndex() + " ");
	}
	
	public void inactive() {
		System.out.println("Sending /inactive message.");
		d.synch.broadcast("/inactive " + d.myIndex() + " ");
	}

	private double scaleMU(double x) {
		return Math.tanh(x / 2500.);
	}
	
	public int numActiveAgents() {
		int count = 0;
		for(int i = 0; i < activeAgents.length; i++) {
			if(activeAgents[i]) count++;
		}
		return count;
	}
	
	void respond() {
		//TODO
		if(activeAgents[d.myIndex()]) {
			//this means that I am active
		}
		if(numActiveAgents() >= 2) {
			//this means there are 2 active agents
		}
		
		if(activeAgents[ID1]) {
			//this refers to a specific unit. Do you want to know if it is active?
			trigger(thirdSound());
			System.out.println("GOT IT!!");
		}
		
		if(activeAgents[ID2]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID3]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID1] && activeAgents[ID2]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID1] && activeAgents[ID3]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID1] && activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID2] && activeAgents[ID1]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID2] && activeAgents[ID3]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID2] && activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID3] && activeAgents[ID1]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID3] && activeAgents[ID2]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID3] && activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID4] && activeAgents[ID1]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID4] && activeAgents[ID2]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID4] && activeAgents[ID3]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID1] && activeAgents[ID2] && activeAgents[ID3]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID1] && activeAgents[ID2] && activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID1] && activeAgents[ID3] && activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID2] && activeAgents[ID3] && activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[ID1] && activeAgents[ID2] && activeAgents[ID3] && activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[3]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[3]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
		if(activeAgents[3]) {
			//this refers to a specific unit. Do you want to know if it is active?
		}
		
	}
	
	void act() {
		//TODO
		trigger(masterSound());
	}
	
}




