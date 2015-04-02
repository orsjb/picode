package compositions.pipos_2015.sam;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.PauseTrigger;
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
import core.Config;

public class Ubicomp2015DataCollection implements PIPO {

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

	Glide xAccFactor, yAccFactor, zAccFactor, xGyrFactor, yGyrFactor, zGyrFactor, xMagFactor, yMagFactor, zMagFactor, freqVal, gainVal;
	float accelAnyDirection, gyroChangeAngle, changeInXAcc, changeInXGyr, spinningX, shakenness;
	float pastFilterValue, previousDiffValue = 0; 
	float loFreq, hiFreq, loGain, hiGain, loVal, hiVal;
	boolean doMovingAverage, doDifference, recording;
	String inputToMap;// choices = useX, useY, useZ, combineXYZ
	// two values used within the two stages of the sonification,
	// reduced is the reduction applied to the input - sometimes nothing
	// sometimes a difference etc. 
	// after the mapping we get mappedValue
	// TODO write a class for this.
	float reducedValue, mappedValue = 0; 

//	// this is an array of names for each type of sonification.
//	String[] sonifications = {"instantaneousAccToRawPitch", "XToRawPitch", "YToRawPitch", "ZToRawPitch",
//			"diffInstantaneousAccToRawPitch", "diffInstantaneousXToRawPitch", "diffInstantaneousYToRawPitch", "diffInstantaneousZToRawPitch",
//			"lopassAccToRawPitch", "lopassXToRawPitch", "lopassYToRawPitch", "lopassZToRawPitch"};

	
	String[] sonifications = {"chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping","chooseSonificationMapping"}; 
			
			
			
	public void chooseSonification(String sonification){


		switch (sonification){
		//instantaneous

		case "silence":
			loFreq = 0;  hiFreq = 0;  loGain=0;  hiGain=0;  loVal = 100; hiVal = 300;
			doMovingAverage = false;  doDifference = false ; 
			inputToMap = "combineXYZpythagoras";  
			break;
		
		case "chooseSonificationMapping":
			
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1;  loVal = 100; hiVal = 300;
			doMovingAverage = false;  doDifference = false ; 
			inputToMap = "useX";  
			break;

		}
	}	


	float mapDifference(float input){
		float output = input - previousDiffValue;
		previousDiffValue = input; 
		return output;
	}

	float mapMovingAverage(float input){
		float output = (float) (input * 0.5 + pastFilterValue * 0.95);
		pastFilterValue = output; 
		return output;
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
	
		
	
	float mapScale(float input, float lowInput, float highInput, float lowOutput, float highOutput){
		float offsetIn = lowInput ; 
		float rangeIn = highInput - lowInput; 
		float offsetOut = lowOutput ; 
		float rangeOut = highOutput - lowOutput; 
		float output = (((input - offsetIn) / rangeIn)  * rangeOut) + offsetOut;
		return output;
	}


	float reduceVectorless(double x, double y, double z){
		// first hypotenuse
		float firstHtns = (float) Math.sqrt(Math.pow(x,2f) + Math.pow(y,2f)); 
		float secondHtns = (float) Math.sqrt(Math.pow(firstHtns,2f) + Math.pow(z,2f));
		// second hypotenuse
		return secondHtns;
	}

	public void printSonification(String sonification, final DynamoPI d){

		String outString = "Sonification " + sonification ; 
		System.out.println(outString);
		d.communication.broadcastOSC("/PI/outputValue", new Object[] {outString});
	}

	public int[] setupRandomChoice(int[] order){
		int[] outputOrder = order;
		for (int i =0; i < (order.length * 50); i++ ){
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
		chooseSonification(sonifications[0]);

		// initialisation of the sonification choices
		int[] sonChoice = new int[20]; //
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
			int numSonifications = 2;
			public void messageReceived(Bead msg) {
			
					if(d.clock.isBeat() && d.clock.getBeatCount() % 64 == 0) {
						//switch to new sonification
						if (sonNum < numSonifications){ 
							chooseSonification(sonifications[sonNum]);
							printSonification(sonifications[sonNum],  d);
							sonNum = sonNum + 1;
						} else {
							
							// If sonNum == numSonifications then we have come to the 
							// end of the trials and should stop. 
							msg.kill(); // kill the bead
							
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
						chooseSonification("silence");
						printSonification("silence",  d);
					}
				
			}
		};
		
		
		
		d.clock.addMessageListener(systematicSonificationOrdering);
		
		g.addInput(wp);
		d.ac.out.addInput(g);
	}

	


	private void setupMu(final DynamoPI d) {
		//mu
		//set up Mu responder
		xAccFactor = new Glide(d.ac, 0, 1);
		yAccFactor = new Glide(d.ac, 0, 1);
		zAccFactor = new Glide(d.ac, 0, 1);
		xGyrFactor = new Glide(d.ac, 0, 1);
		yGyrFactor = new Glide(d.ac, 0, 1);
		zGyrFactor = new Glide(d.ac, 0, 1);
		xMagFactor = new Glide(d.ac, 0, 1);
		yMagFactor = new Glide(d.ac, 0, 1);
		zMagFactor = new Glide(d.ac, 0, 1);
		freqVal = new Glide(d.ac, 440, 100); 
		gainVal = new Glide(d.ac, 0.1f, 100); 

		d.communication.addListener(new NetworkCommunication.Listener() {
			@Override
			public void msg(OSCMessage msg) {
				if(msg.getName().equals("/PI/sonification")) {
					// set sonification
					String strVal  = ((String)msg.getArg(0)).toString();
					chooseSonification(strVal);
				}
			}
		});



		d.mu.addListener(new MiniMUListener() {

			@Override
			public void imuData(double xA, double yA, double zA,double xG, double yG, double zG,double xC, double yC, double zC) {

				switch (inputToMap) {
				case "combineXYZpythagoras":
					reducedValue = reduceVectorless(xA, yA, zA);
					break;
				case "useX":
					reducedValue = (float) xA;
					break;
				case "useY":
					reducedValue = (float) yA;
					break;
				case "useZ":
					reducedValue = (float) zA;
					break;
				}

				if (doDifference){    // Should we do the differencing?
					reducedValue = mapDifference(reducedValue);
				}


				if (doMovingAverage){ // Should we do the moving average?
					reducedValue = mapMovingAverage(reducedValue);
				}

				// we have to do some kind of mapping - this is linear
				mappedValue = mapScale(reducedValue, loVal, hiVal, loFreq, hiFreq);

				// debug 
				String outString =  xA + " " + yA + " " + zA + " " + xG + " " + yG + " " + zG + " "  + xC + " " + yC + " " + zC + " " + reducedValue + " " + mappedValue ; 

				d.communication.broadcastOSC("/PI/outputValue", new Object[] {outString});

				freqVal.setValue(mappedValue); // set the value 


			}
		});
	}

	private float scaleMU(float x) {
		//TODO	 - output between -1 and 1 (using tanh?)
		return (float)Math.tanh(x / 250);
	}

}
