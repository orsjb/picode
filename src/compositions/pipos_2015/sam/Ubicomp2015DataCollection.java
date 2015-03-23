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
//			"pisound-009e959c47e8.local"
//			"pisound-009e959c510a.local",
//			"pisound-009e959c502d.local",
			
		});
	}
	
	Glide xAccFactor, yAccFactor, zAccFactor, xGyrFactor, yGyrFactor, zGyrFactor, xMagFactor, yMagFactor, zMagFactor, freqVal, gainVal;
	float accelAnyDirection, gyroChangeAngle, changeInXAcc, changeInXGyr, spinningX, shakenness;
	float pastFilterValue, previousDiffValue = 0; 
	float loFreq, hiFreq, loGain, hiGain, loVal, hiVal;
	boolean doMovingAverage, doDifference;
	String inputToMap;// choices = useX, useY, useZ, combineXYZ
	
	// two values used within the two stages of the sonification,
	// reduced is the reduction applied to the input - sometimes nothing
	// sometimes a difference etc. 
	// after the mapping we get mappedValue
	// TODO write a class for this.
	float reducedValue, mappedValue = 0; 
	
	// this is an array of names for each type of sonification.
	String[] sonifications = {"instantaneousAccToRawPitch", "XToRawPitch", "YToRawPitch", "ZToRawPitch",
							  "diffInstantaneousAccToRawPitch", "diffInstantaneousXToRawPitch", "diffInstantaneousYToRawPitch", "diffInstantaneousZToRawPitch",
			                  "lopassAccToRawPitch", "lopassXToRawPitch", "lopassYToRawPitch", "lopassZToRawPitch"};



	public void chooseSonification(String sonification){
		switch (sonification){
		//instantaneous
		case "instantaneousAccToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=0;  loVal = 100; hiVal = 300;
			doMovingAverage = false;  doDifference = false ; 
			inputToMap = "combineXYZpythagoras";  
			break;

		case "XToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1;  loVal = 100; hiVal = 300;
			doMovingAverage = false;  doDifference = false ; 
			inputToMap = "useX";  
			break;

		case "YToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1;  loVal = 100; hiVal = 300;
			doMovingAverage = false;  doDifference = false ; 
			inputToMap = "useY";
			break;

		case "ZToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1; loVal = 100; hiVal = 300;
			doMovingAverage = false;  doDifference = false ; 
			inputToMap = "useZ";  
			break;

// diff
		case "diffInstantaneousAccToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=0;  loVal = -100; hiVal = 100;
			doMovingAverage = false;  doDifference = true ; 
			inputToMap = "combineXYZpythagoras";  
			break;

		case "diffInstantaneousXToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1; loVal = -100; hiVal = 100;
			doMovingAverage = false;  doDifference = true ; 
			inputToMap = "useX";  
			break;

		case "diffInstantaneousYToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1;  loVal = -100; hiVal = 100;
			doMovingAverage = false;  doDifference = true ; 
			inputToMap = "useY";
			break;

		case "diffInstantaneousZToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1;  loVal = -100; hiVal = 100;
			doMovingAverage = false;  doDifference = true ; 
			inputToMap = "useZ";  
			break;

			// lopass
		case "lopassAccToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=0;  loVal = 0; hiVal = 3000;
			doMovingAverage = true;  doDifference = false ; 
			inputToMap = "combineXYZpythagoras";  
			break;

		case "lopassXToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1; loVal = 100; hiVal = 300;
			doMovingAverage = true;  doDifference = false ; 
			inputToMap = "useX";  
			break;

		case "lopassYToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1;  loVal = 100; hiVal = 300;
			doMovingAverage = true;  doDifference = false ; 
			inputToMap = "useY";
			break;

		case "lopassZToRawPitch":
			loFreq = 200;  hiFreq = 500;  loGain=0;  hiGain=1;  loVal = 100; hiVal = 300;
			doMovingAverage = true;  doDifference = false ; 
			inputToMap = "useZ";  
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
	
	
	@Override
	public void action(final DynamoPI d) {
		d.reset();
		setupMu(d);
		chooseSonification(sonifications[0]);
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
		
		d.clock.addMessageListener(new Bead() {
			int sonNum = 0;
			public void messageReceived(Bead msg) {
				if(d.clock.isBeat() && d.clock.getBeatCount() % 32 == 0) {
					if (sonNum < 12){ 
					chooseSonification(sonifications[sonNum]);
					System.out.println("SonificationNumber " + sonNum);
					String outString = "SonificationNumber " + sonNum + " " + sonifications[sonNum]; 
					// System.out.println(outString);
					d.communication.broadcastOSC("/PI/outputValue", new Object[] {outString});
			
					sonNum = sonNum + 1;
					
					} else {
						sonNum = 0; 
						chooseSonification(sonifications[sonNum]);
						System.out.println("SonificationNumber " + sonNum);
						String outString = "SonificationNumber " + sonNum + " " + sonifications[sonNum]; 
						// System.out.println(outString);
						d.communication.broadcastOSC("/PI/outputValue", new Object[] {outString});
					}				
				}
			}
		});
		
		
		
		
		
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
			public void accelData(double xA, double yA, double zA) {
				

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
				String outString =  xA + " " + yA + " " + zA + " " + reducedValue + " " + mappedValue ; 
				
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
