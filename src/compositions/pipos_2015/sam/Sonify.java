package compositions.pipos_2015.sam;




public class Sonify{
		
	float inValue = 0;
	float reducedValue = 0;
	float mappedValue = 0; 
	float lowInput, lowOutput, highInput, highOutput;
	float offsetIn, offsetOut;
	float rangeIn, rangeOut;
	float outValue = 0;	
	
	float pastFilterValue, previousDiffValue = 0; 

	
	public Sonify(float lowIn, float highIn, float lowOut, float highOut){
		
		// Scale values
		lowInput  		= lowIn;
		highInput		= highIn;
		lowOutput		= lowOut;
		highOutput 		= highOut;
		
	}
		
	
	//private class timeseries{
		
		
	
	//}
	
	private void scaleInput(){
		
		// range In
		float offsetIn 		= lowInput; 
		float rangeIn 		= highInput - lowInput; 
		
		// range Out
		float offsetOut 	= lowOutput; 
		float rangeOut 		= highOutput - lowOutput; 
		
		// output calculated
		outValue 		= (((inValue - offsetIn) / rangeIn)  * rangeOut) + offsetOut;
		
	}

	public void addValue(float inputVal){
		
		// 
		inValue = inputVal;
	
		update();
		
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
	
	
}