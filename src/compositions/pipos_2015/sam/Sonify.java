package compositions.pipos_2015.sam;


public class Sonify{
		
	float inValue;
	float lowInput, lowOutput, highInput, highOutput;
	float offsetIn, offsetOut;
	float rangeIn, rangeOut;
    float outValue;
    public float outValueFreq;

    float pastFilterValue, previousDiffValue = 0;

	
	public Sonify(float lowIn, float highIn, float lowOut, float highOut){
		
		// Scale values
		lowInput  		= lowIn;
		highInput		= highIn;
		lowOutput		= lowOut;
		highOutput 		= highOut;

        // range In
        offsetIn 		= lowInput;
        rangeIn 		= highInput - lowInput;
        // range Out
        offsetOut 	= lowOutput;
        rangeOut 	= highOutput - lowOutput;
        System.out.println("This is a Sonify with lowIn " +  lowIn + " highIn  " + highIn + " lowOut  " + lowOut +  " highOut " +  highOut);
	}
		
	
	//private class timeseries{
	//}
	
	private void scaleInput(){
		
		// output calculated
		outValue = (((inValue - offsetIn) / rangeIn)  * rangeOut) + offsetOut;


		
	}

	public void addValue(float inputVal){
		inValue = inputVal;
        update();
        System.out.println(inValue + " " + offsetIn + " " + outValue);

    }

    public void printSonificationAlgorithm() {


        // 5 values spaced across the range
        float[] vals = new float[5];
        for (int i = 0; i < 5; i++){
            System.out.println(offsetIn + " is the offset, and " + rangeIn + " is the range times " + ((float) i) / 5f);
            vals[i] = offsetIn + rangeIn * ((float) i) / 5f;
        }
        System.out.println(vals[3]);
        // State values
        System.out.println("Sonification Algorithm Loaded");
        System.out.println(": Scale values from "  +  lowInput + " to " + highInput );
        System.out.println(": to "  +  lowOutput + " to " + highOutput);
        System.out.println(":");
        System.out.println(":");
        for (int i = 0; i < 5; i ++) {
            this.addValue(vals[i]);
            System.out.println(": for " + vals[i] + " the output value is " + outValue + " and the freq value is " + this.getOutputMTOF());
        }


    }
	
	
	
	private void update(){


		scaleInput();

		// deposit in ring buffer
		
		// calculate mean of ring buffer
		
		// calculate median 
		
		// calculate differenced value to 4th order 
		
		// running Max and Min
		
		// time since last peak
		
		// time since 
			
	}
	
	public float getOutput(){
		
		return outValue;
	
	}
	
	public float getOutputMTOF(){


        outValueFreq = mtof(outValue);
        System.out.println("outValue "+ outValue + " is " + outValueFreq);
		return outValueFreq;
	}
	
	

	float mapDifference(float input){
		float output = input - previousDiffValue;
		previousDiffValue = input; 
		return output;
	}



	float mapMovingAverage(float input){
		float output = (float) (input * 0.05 + pastFilterValue * 0.95);
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
	
	
}