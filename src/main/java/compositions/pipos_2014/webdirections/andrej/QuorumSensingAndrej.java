package compositions.pipos_2014.webdirections.andrej;

import java.util.List;

import controller.network.SendToPI;
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

	public static void main(String[] args) throws Exception {
		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		System.out.println("The path will be run: " + fullClassName);
		SendToPI.send(fullClassName, new String[]{
				"localhost" 
				});
	}
	
	private static final long serialVersionUID = 1L;

	Envelope gainEnvelope;
	Envelope carrierFreqEnvelope; // connect to to amount added to function
	Envelope modulatorFreqEnvelope; // connect to modulator freqModulator frequency
	Envelope modDepthEnvelope;
	
	final static int ID1 = 3;
	final static int ID2 = 8;
	final static int ID3 = 6;
	final static int ID4 = 4;
	
	List<Long> events;		//a history of recent event times, used to match other PIs
	
	DynamoPI d;
	
	boolean[] activeAgents = new boolean[20];

	
	//#####################SIGNAL SOUNDS######################
	
	public float[][] signal1() {
		float n1 = d.rng.nextFloat() * 1000f + 500f;
	    float n2 = d.rng.nextFloat() * 1000f + 500f;
	    float n3 = d.rng.nextFloat() * 1000f + 500f;
	    float n4 = d.rng.nextFloat() * 1000f + 500f;
		float[][] f = {{0.8f,  50},{0.8f, 300},{0.5f, 200},{0.0f, 1250}, //Gain 1800
	            {n1, 100},{n2, 1000},{n3, 200},{n4,  400}, //CF
	            {n1/6,  200},{n2/6,   50},{n3/6,   50},{n4/6, 300}, //MF
	            {10,  50},{85, 500},{150,  50},{150, 1200}}; //MD
		return f;
	}
	
	public float[][] signal2() {
	float n1 = d.rng.nextFloat() * 1500f + 500;
    float n2 = d.rng.nextFloat() * 1500f + 500;
    float n3 = d.rng.nextFloat() * 1500f + 500;
    float n4 = d.rng.nextFloat() * 1500f + 500;
    float n5 = d.rng.nextFloat() * 1500f + 500;
    float n6 = d.rng.nextFloat() * 1500f + 500;
    float n7 = d.rng.nextFloat() * 1500f + 500;
	float[][] f = {{0.8f,  30},{0.8f, 620},{0.8f, 620},{0.0f, 30}, //Gain 1300
            {n1, 100},{n2, 100},{n3, 500},{n4,  100},{n5, 300},{n6, 100},{n7, 100}, //CF
            {n1/8,  500},{n2/8, 300},{n3/8, 200},{n4/8, 300}, //MF
            {10,  100},{110, 500},{50,  100},{80, 200},{30,100},{90,100},{110,100}}; //MD
	return f;
	}

	public float[][] signal3() {
		float n1 = d.rng.nextFloat() * 2600f + 500f;
	    float n2 = d.rng.nextFloat() * 2600f + 500f;
		float[][] f = {{0.8f,  375},{0.5f, 375},{0.8f, 375},{0.0f, 375}, //Gain 1500
                {n1, 500},{n1, 500},{n2, 10},{n2,  490}, //CF
                {n1/9,  200},{n2/9, 200},{n1/9,  1000},{n2/9, 100}, //MF
                {10,  250},{10, 250},{140,  500},{140, 500}}; //MD
		return f;
	}
	
	public float[][] signal4() {
		float n1 = d.rng.nextFloat() * 1600f + 500f;
	    float n2 = d.rng.nextFloat() * 1600f + 500f;
		float[][] f = {{0.6f,  10},{0.8f, 1090},{0.3f, 700},{0.0f, 700}, //Gain 2500
                {n1, 300},{n2, 900},{n1, 600},{n2,  700}, //CF
                {n1/2,  1700},{n2/2, 10},{n1/2, 490},{n2/2, 300}, //MF
                {220,  1000},{220, 500},{220,  500},{220, 500}}; //MD
		return f;
	}
	
	//#########################END OF SIGNAL SOUNDS################################
	
	
	//#########################ACTION SOUNDS################################
	
	public float[][] action1() {
		float n1 = d.rng.nextFloat() * 1500f + 500f;
		float[][] f = {{0.9f,  1000},{0.9f, 50},{0.2f, 40},{0.0f, 500}, //Gain
                {n1, 10},{200, 20},{800, 10},{400,  400}, //CF
                {500,  200},{50,   50},{50,   50},{70, 30}, //MF
                {500,  50},{200, 30},{600,  50},{2000, 70}}; //MD
		return f;
	}
	
	public float[][] action2() {
		float n1 = d.rng.nextFloat() * 400f + 200f;
		float[][] f = {{0.9f,  10},{0.9f, 200},{0.2f, 100},{0.0f, 200}, //Gain
                {n1, 10},{400, 120},{n1, 300},{600,  100}, //CF
                {10,  90},{100,   100},{50,   500},{120, 30}, //MF
                {1000,  50},{1200, 300},{1500,  400},{1000, 70}}; //MD
		return f;
	}
	
	public float[][] action3() {
		float n1 = d.rng.nextFloat() * 1500f + 500f;
		float[][] f = {{0.9f,  100},{0.9f, 300},{0.2f, 100},{0.0f, 400}, //Gain
                {n1, 10},{1800, 120},{700, 30},{n1,  20}, //CF
                {1000,  20},{800,   100},{940,   50},{600, 30}, //MF
                {200,  50},{700, 300},{200,  200},{200, 70}}; //MD
		return f;
	}
	
	public float[][] action4() {
		float m1 = d.rng.nextFloat() * 1500f + 500f;
		float[][] f = {{0.4f,  200},{0.9f, 300},{0.6f, 100},{0.0f, 200}, //Gain
                {200, 300},{500, 120},{300, 300},{600,  200}, //CF
                {10,  20},{200,   200},{1000,   300},{500, 400}, //MF
                {m1,  400},{2000, 30},{200,  50},{m1, 400}}; //MD
		return f;
	}
	
	public float[][] action5() {
		float n1 = d.rng.nextFloat() * 1500f + 500f;
		float[][] f = {{0.9f,  10},{0.8f, 1000},{0.8f, 20},{0.0f, 600}, //Gain
                {n1, 10},{200, 200},{200, 400},{200,  400}, //CF
                {10,  200},{50,   500},{50,   50},{70, 300}, //MF
                {500,  100},{200, 120},{600,  500},{2000, 70}}; //MD
		return f;
	}
	
	public float[][] action6() {
		float[][] f = {{0.8f,  10},{0.8f, 500},{0.4f, 40},{0.0f, 500}, //Gain
                {200, 10},{250, 500},{0, 50},{300,  400}, //CF
                {500,  200},{50,   50},{100,   500},{70, 30}, //MF
                {100,  200},{500, 200},{2000,  200},{5000, 200}}; //MD
		return f;
	}
	
	public float[][] action7() {
		float n1 = d.rng.nextFloat() * 1500 + 500f;
		float[][] f = {{0.9f,  1000},{0.9f, 1000},{0.2f, 100},{0.0f, 500}, //Gain
                {n1, 10},{200, 20},{800, 10},{400,  400}, //CF
                {100,  200},{50,  200},{50,   1000},{70, 30}, //MF
                {1000,  200},{700, 200},{200,  200},{50, 200}}; //MD
		return f;
	}
	
	public float[][] action8() {
		float n1 = d.rng.nextFloat() * 1500f + 500f;
		float[][] f = {{0.9f,  90},{0.5f, 400},{0.8f, 100},{0.0f, 800}, //Gain
                {n1, 300},{n1, 100},{n1, 10},{n1,  400}, //CF
                {120,  200},{200,   50},{500,   50},{1000, 30}, //MF
                {4000,  90},{800, 800},{600,  50},{100, 70}}; //MD
		return f;
	}
	
	public float[][] action9() {
		float n1 = d.rng.nextFloat() * 1800f + 200f;
		float[][] f = {{0.9f,  200},{0.9f, 1000},{0.2f, 345},{0.0f, 500}, //Gain
                {n1, 500},{n1, 500},{n1, 500},{n1,  500}, //CF
                {123,  200},{10,   30},{534,   1000},{200, 600}, //MF
                {50,  500},{900, 800},{2000,  700},{2000, 800}}; //MD
		return f;
	}
	
	public float[][] action10() {
		float n1 = d.rng.nextFloat() * 400f + 200f;
		float[][] f = {{0.9f,  50},{0.9f, 1000},{0.5f, 200},{0.0f, 120}, //Gain
                {n1, 500},{n1, 500},{n1, 500},{n1, 500}, //CF
                {50,  1000},{3,   200},{70,  320},{421, 100}, //MF
                {100,  500},{1320, 500},{500,  500},{100, 500}}; //MD
		return f;
	}
	
	public float[][] action11() {
		float n1 = d.rng.nextFloat() * 1500f + 500f;
		float[][] f = {{0.9f,  10},{0.9f, 500},{0.2f, 400},{0.0f, 500}, //Gain
                {n1, 10},{n1, 500},{n1, 1000},{n1,  400}, //CF
                {10003,  200},{2005,   500},{2050,   1000},{10000, 30}, //MF
                {50000,  50},{31030, 800},{20000,  500},{50000, 500}};
		return f;
	}
	
	public float[][] action12() {
		float n1 = d.rng.nextFloat() * 1500f + 500f;
		float[][] f = {{0.9f,  10},{0.9f, 50},{0.2f, 40},{0.0f, 500}, //Gain
                {n1, 10},{200, 20},{800, 10},{400,  400}, //CF
                {20000,  200},{31390,   50},{41879,   50},{28749, 30}, //MF
                {12400,  50},{26700, 30},{46700,  50},{22300, 70}}; //MD
		return f;
	}
	
	//#########################END OF ACTION SOUNDS################################
	
	//#########################HARMONY SOUNDS#######################################
	
	public float[][] harmony1() {
		float[][] f = {{0.8f,  2000},{0.8f, 1000},{0.6f, 990},{0.0f, 10}, //Gain
                {2000, 1000},{2010, 1000},{2010, 1000},{2000,  1000}, //CF
                {1000,  1000},{1005,  1000},{1005,   1000},{1000, 1000}, //MF
                {2000,  1000},{5000, 1000},{2500,  1500},{3000, 500}}; //MD
		return f;
	}
	
	public float[][] harmony2() {
		float[][] f = {{0.8f,  2000},{0.8f, 1000},{0.6f, 990},{0.0f, 10}, //Gain
                {1000, 1000},{1010, 1000},{1010, 1000},{1000,  1000}, //CF
                {500,  1000},{505,  1000},{505,   1000},{500, 1000}, //MF
                {2000,  1000},{5000, 1000},{2500,  1500},{3000, 500}}; //MD
		return f;
	}
	
	public float[][] harmony3() {
		float[][] f = {{0.8f,  2000},{0.8f, 1000},{0.6f, 990},{0.0f, 10}, //Gain
                {1500, 1000},{1510, 1000},{1510, 1000},{1500,  1000}, //CF
                {750,  1000},{755,  1000},{755,   1000},{750, 1000}, //MF
                {2000,  1000},{5000, 1000},{2500,  1500},{3000, 500}}; //MD
		return f;
	}
	
	public float[][] harmony4() {
		float[][] f = {{0.8f,  2000},{0.8f, 1000},{0.6f, 990},{0.0f, 10}, //Gain
                {1200, 1000},{1210, 1000},{1210, 1000},{1200,  1000}, //CF
                {600,  1000},{605,  1000},{605,   1000},{600, 1000}, //MF
                {2000,  1000},{5000, 1000},{2500,  1500},{3000, 500}}; //MD
		return f;
	}
	
	//#########################END OF HARMONY SOUNDS################################


	@Override
	public void action(final DynamoPI d) {
		this.d = d;
		d.reset();
		
		d.ac.out.getGainUGen().setValue(0.1f);
		
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

		TapIn delayIn = new TapIn(d.ac, 2000);
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
	
	void signal2Trigger(float[][] cellID) {
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
		carrierFreqEnvelope.addSegment(cellID[8][0], cellID[8][1]);
		carrierFreqEnvelope.addSegment(cellID[9][0], cellID[9][1]);
		carrierFreqEnvelope.addSegment(cellID[10][0], cellID[10][1]);
		//Modulator Frequency
		modulatorFreqEnvelope.clear();
		modulatorFreqEnvelope.addSegment(cellID[11][0], cellID[11][1]);
		modulatorFreqEnvelope.addSegment(cellID[12][0], cellID[12][1]);
		modulatorFreqEnvelope.addSegment(cellID[13][0], cellID[13][1]);
		modulatorFreqEnvelope.addSegment(cellID[14][0], cellID[14][1]);
		//Modulation Depth
		modDepthEnvelope.clear();
		modDepthEnvelope.addSegment(cellID[15][0], cellID[15][1]);
		modDepthEnvelope.addSegment(cellID[16][0], cellID[16][1]);
		modDepthEnvelope.addSegment(cellID[17][0], cellID[17][1]);
		modDepthEnvelope.addSegment(cellID[18][0], cellID[18][1]);
		modDepthEnvelope.addSegment(cellID[19][0], cellID[19][1]);
		modDepthEnvelope.addSegment(cellID[20][0], cellID[20][1]);
		modDepthEnvelope.addSegment(cellID[21][0], cellID[21][1]);
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
			active = true;
			activeAgents[d.myIndex()] = true;
			shake();
			count = 0;
		}
		count++;
		if(count > 1000 && active == true) {
			active = false;
			activeAgents[d.myIndex()] = false;
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
				trigger(action1());
			//d.setStatus("jjj");
		}
		
		if(activeAgents[ID2]) {
			//d.setStatus("x: "+activeAgents[ID2]);
			//this refers to a specific unit. Do you want to know if it is active?
				trigger(action7());
		}
		
		if(activeAgents[ID3]) {
			//this refers to a specific unit. Do you want to know if it is active?
				trigger(action10());
		}
		
		if(activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
				trigger(action11());
		}
	}
	
	
	void act() {
		
		//d.setStatus("x: "+activeAgents[ID1]);
		
		
		//TODO
		
		if(activeAgents[ID1]) {
			//this refers to a specific unit. Do you want to know if it is active?
				
			trigger(signal1());
			System.out.println("GOT IT!!");
		}
		
		 if(activeAgents[ID2]) {
			//this refers to a specific unit. Do you want to know if it is active?

			signal2Trigger(signal2());
			System.out.println("GOT IT!!");
		}
		
		if(activeAgents[ID3]) {
			//this refers to a specific unit. Do you want to know if it is active?
			
			trigger(signal3());
			System.out.println("GOT IT!!");
		}
		
		if(activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
			trigger(signal4());
			System.out.println("GOT IT!!");
		}
		

		
		else if(activeAgents[ID1] && activeAgents[ID2] && activeAgents[ID3] && activeAgents[ID4]) {
			//this refers to a specific unit. Do you want to know if it is active?
			if (numActiveAgents() == 4) {
				if(activeAgents[ID1]) {
					if(activeAgents[d.myIndex()]) {
					trigger(harmony1());
					}
					}
				if(activeAgents[ID2]) {
					if(activeAgents[d.myIndex()]) {
					trigger(harmony2());
					}
					}
				if(activeAgents[ID3]) {
					if(activeAgents[d.myIndex()]) {
					trigger(harmony3());
					}
					}
				if(activeAgents[ID4]) {
					if(activeAgents[d.myIndex()]) {
					trigger(harmony4());
					}
					}
				
			}
		}
	}
	
}




