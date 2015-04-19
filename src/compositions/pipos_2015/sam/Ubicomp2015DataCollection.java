package compositions.pipos_2015.sam;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.network.NetworkCommunication;
import pi.sensors.MiniMU.MiniMUListener;
import controller.network.*;
import core.PIPO;
import de.sciss.net.OSCMessage;
import compositions.pipos_2015.sam.Sonify;


public class Ubicomp2015DataCollection implements PIPO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {

		String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
		SendToPI.send(fullClassName, new String[]{

				//			"pisound-009e959c5093.local", 
				"pisound-009e959c47ef.local",
				//			"pisound-009e959c4dbc.local", 
				//			"pisound-009e959c3fb2.local",
				//			"pisound-009e959c50e2.local",
				//			"pisound-009e959c47e8.local",
				//			"pisound-009e959c510a.local",
				//			"pisound-009e959c502d.local"
		});
	}

	Glide freqVal, gainVal;
	float loGain, hiGain;
	boolean doMovingAverage, doDifference = false; 
	boolean recording;
	String inputToMap; 
	Sonify sonifyer;
	float inputRange; 
	float rangeCentre;

	
	public static String[][] initiliaseCombinations(){
		String[] paramRange  = {"LowRng","MedRng","HiRng"};
		String[] paramCentre = {"LowCtr","MedCtr","HiCtr"};
		String[] paramType   = {"UseX","UseY","UseZ"};

		String[][] paramCombinations =  new String [paramRange.length * paramCentre.length * paramType.length][3];
		
		int row = 0;
		for (int i = 0; i <  paramRange.length; i++){
			for (int j = 0; j <  paramCentre.length; j++){
				for (int k = 0; k <  paramType.length; k++){
					System.out.println(row + " " + i + " " + j + " " + k);
					paramCombinations[row][0] = paramRange[i];
					paramCombinations[row][1] = paramCentre[j];
					paramCombinations[row][2] = paramType[k];
					row++;
				}	
			}
		}
		return paramCombinations;
	}

	String[][] paramCombinations = initiliaseCombinations();
	

	public void chooseSonificationCombination(String reservedSonification){
		switch (reservedSonification){
			case "silence":
		
				sonifyer = new Sonify(0, 0, 0, 0);
				loGain=0;  
				hiGain=0;  
				break;
		}
	}
	
	
	public void chooseSonificationCombination(String[] paramCombination){
		
		switch (paramCombination[0]){

			case "LowRng":
				inputRange = 1;
				break;
			case "MedRng":
				inputRange = 4;
				break;
			case "HiRng":
				inputRange = 12;
				break;
		}
		
		switch (paramCombination[1]){
			
			case "LowCtr":
				rangeCentre = 57;
				break;
			case "MedCtr":
				rangeCentre = 69;
				break;
			case "HiCtr":
				rangeCentre = 81;
				break;
		}
				
		switch (paramCombination[2]){

			case "UseX":
				inputToMap = "useX";  
				break;
			
			case "UseY":
				inputToMap = "useY";  
				break;
			
			case "UseZ":
				inputToMap = "useZ";  
				break;
		}
			
		sonifyer = new Sonify(-3000, 3000, rangeCentre-inputRange, rangeCentre+inputRange);

	}	


	
	float mtof(float input){
	// convert midi note number to a frequency
	
		float output = (float) (Math.pow(2, (input-69)/12) * 440);
		return output; 	
		
	}
	
	
	float ftom(float input){
	// convert frequency val to a midi note number 
	
		float output = (float) (69 + (12 *  (Math.log(input/440)/Math.log(2))));				
		return output; 
		
	}

	
	float reduceVectorless(double x, double y, double z){
	// reduce to di
		
		// first hypotenuse
		float firstHtns = (float) Math.sqrt(Math.pow(x,2f) + Math.pow(y,2f)); 
		float secondHtns = (float) Math.sqrt(Math.pow(firstHtns,2f) + Math.pow(z,2f));
		
		// second hypotenuse
		return secondHtns;
	
	}


	public void printSonification(String paramCombination, final DynamoPI d){
		switch(paramCombination){
		
		case "Silence":
		
			String outString = "Sonification Silence"; 
			System.out.println(outString);
			d.communication.broadcastOSC("/PI/outputValue", new Object[] {outString});
			break;
		
		}
		
	}
	
	public void printSonification(String[] paramCombination, final DynamoPI d){
		String outString = "Sonification " + paramCombination[0] + " " + paramCombination[1] + " " + paramCombination[2]; 
		System.out.println(outString);
		d.communication.broadcastOSC("/PI/outputValue", new Object[] {outString});
	}

	public int[] setupRandomChoice(int[] order){
		
		int[] outputOrder = order;
		
		for (int i = 0; i < (order.length * 50); i++ ){
			// swap randomly chosen index with another randomly chosen index. 
			int swap1 = (int) Math.floor(Math.random() * order.length);
			int swap2 = (int) Math.floor(Math.random() * order.length);
			int temp  = outputOrder[swap1];   
			outputOrder[swap1] = outputOrder[swap2];
			outputOrder[swap2] = temp;
		}
		
		for (int i = 0; i < order.length ; i++ ){
			System.out.println("setupRandomChoice ---- " + i +  " ---- " + outputOrder[i]);
		}
		
		return outputOrder;
	}

	

	@Override
	public void action(final DynamoPI d) {
		
		d.reset();
		setupMu(d);
		chooseSonificationCombination(paramCombinations[0]);

		// initialisation of the sonification choices
		int[] sonChoice = new int[27]; //
		for (int i = 0; i < sonChoice.length; i++ ){
			sonChoice[i] = i; 
		}
		//randomisation of the sonification choices. 
		sonChoice = setupRandomChoice(sonChoice);

		Function func = new Function(freqVal) {
			@Override
			public float calculate() {
				return x[0]; 
			}
		};

		Function func2 = new Function(gainVal) {
			@Override
			public float calculate() {
				return x[0]; 
			}
		};

		WavePlayer wp = new WavePlayer(d.ac, func, Buffer.SINE);
		Gain g = new Gain(d.ac, 1, func2);
		
		d.communication.addListener(new NetworkCommunication.Listener() {
	
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/PI/recording/on")) {
					recording = true;
					
					d.communication.broadcastOSC("/PI/recording", new Object[] {"Message Received"});
					
				} else if(msg.getName().equals("/PI/recording/off")) {
					recording = false;
				}
			}
		});
			
		Bead systematicSonificationOrdering = new Bead() {
			int sonNum = 0;
			int numSonifications = 27;
			public void messageReceived(Bead msg) {
				if(d.clock.isBeat() && d.clock.getBeatCount() % 64 == 0 && sonNum == 0) {
					String outString = "startFiles";
					d.communication.broadcastOSC("/PI/file", new Object[] {outString});
				}
				
				if(d.clock.isBeat() && d.clock.getBeatCount() % 64 == 0) {
					//switch to new sonification
					if (sonNum < numSonifications){ 
						chooseSonificationCombination(paramCombinations[sonNum]);
						printSonification(paramCombinations[sonNum],  d);
						sonNum = sonNum + 1;
					} else {
						// If sonNum == numSonifications then we have come to the 
						// end of the trials and should stop. 
						msg.kill(); // kill the bead
						String outString = "stopFiles";
						d.communication.broadcastOSC("/PI/file", new Object[] {outString});
						recording = false;
					}				
				}

				if(d.clock.isBeat() && d.clock.getBeatCount() % 64 == 1) {
					// start recording
					String outString = "open";
					d.communication.broadcastOSC("/PI/file", new Object[] {outString});
				}

				if(d.clock.isBeat() && d.clock.getBeatCount() % 64 == 56) {
					// stop recording
					String outString = "close";
					d.communication.broadcastOSC("/PI/file", new Object[] {outString});
				}

				if(d.clock.isBeat() && d.clock.getBeatCount() % 64 == 57) {					
					// switch to silence
					chooseSonificationCombination("silence");
					printSonification("silence",  d);
				}
			}
		};
		
		d.clock.addMessageListener(systematicSonificationOrdering);	
		g.addInput(wp);
		d.ac.out.addInput(g);
	}

	


	private void setupMu(final DynamoPI d) {

		freqVal = new Glide(d.ac, 440, 10); 
		gainVal = new Glide(d.ac, 0.1f, 10); 

		d.communication.addListener(new NetworkCommunication.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/PI/sonification")) {
					// set sonification
					String strVal  = ((String)msg.getArg(0)).toString();
					chooseSonificationCombination(strVal);
				}
			}
		});


		d.mu.addListener(new MiniMUListener() {

			@Override
			public void imuData(double xA, double yA, double zA, double xG, double yG, double zG,double xC, double yC, double zC) {
				float mappedValue = 0;
				float reducedValue = 0;
				
				switch (inputToMap) {
					case "combineXYZpythagoras":
						reducedValue = reduceVectorless(xA, yA, zA);
						sonifyer.addValue(reducedValue);
						break;
					case "useX":
						reducedValue = (float) xA;
						sonifyer.addValue(reducedValue);
						break;
					case "useY":
						reducedValue = (float) yA;
						sonifyer.addValue(reducedValue);
						break;
					case "useZ":
						reducedValue = (float) zA;
						sonifyer.addValue(reducedValue);
						break;
				}
				
				// we have to do some kind of mapping - this is linear
				mappedValue = sonifyer.getOutputMTOF();
				String outString =  xA + " " + yA + " " + zA + " " + xG + " " + yG + " " + zG + " "  + xC + " " + yC + " " + zC + " " + reducedValue + " " + mappedValue ; ;
				d.communication.broadcastOSC("/PI/outputValue", new Object[] {outString});
				freqVal.setValue(mappedValue); // set the value 
			}
		});
	}


}
