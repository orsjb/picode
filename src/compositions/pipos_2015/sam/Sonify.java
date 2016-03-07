package compositions.pipos_2015.sam;


public class Sonify{
		
	double inValue;
	double lowInput, lowOutput, highInput, highOutput;
	double offsetIn, offsetOut;
	double rangeIn, rangeOut;
    double outValue;
    public double outValueFreq;
    double[] data;
    int dataIndex;
    double pastFilterValue, previousDiffValue = 0;

	
	public Sonify(double lowIn, double highIn, double lowOut, double highOut){
		
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


    public Sonify(double[] dataIn, double lowOut, double highOut){
        // Contructor if you have the entire data vector

        double lowIn = dataIn[0];
        double highIn = dataIn[0];

        // calculateValues
        for (int i = 0; i<dataIn.length; i++) {
            lowIn = Math.min(dataIn[i], lowIn);
            highIn = Math.max(dataIn[i], highIn);
        }
        data = dataIn;

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


    private void scaleInput(){
		
		// output calculated
		outValue = (((inValue - offsetIn) / rangeIn)  * rangeOut) + offsetOut;

	}



	public void addValue(double inputVal){
		inValue = inputVal;
        scaleInput();
        update();
        System.out.println(inValue + " " + offsetIn + " " + outValue);

    }

    public void indexToValue(double inputIndexAsDecimal) {

        if (inputIndexAsDecimal >= 1 |inputIndexAsDecimal < 0){
            System.err.println("inputIndexAsDecimal Error - either 1 or greater or less than 0");
        }
        // get length of data to find index

        dataIndex  = (int) (data.length * inputIndexAsDecimal);
        inValue = data[dataIndex]; // set inValue so update can be called
        scaleInput();
        update();

    }

        public void printSonificationAlgorithm() {

        // 5 values spaced across the range
        double[] vals = new double[5];
        for (int i = 0; i < 5; i++){
            System.out.println(offsetIn + " is the offset, and " + rangeIn + " is the range times " + ((double) i) / 5f);
            vals[i] = offsetIn + rangeIn * ((double) i) / 5f;
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

		//

	}
	
	public double getOutput(){
		
		return outValue;
	
	}
	
	public double getOutputMTOF(){

        outValueFreq = mtof(outValue);
        System.out.println("outValue "+ outValue + " is " + outValueFreq);
		return outValueFreq;
	}


	double mapDifference(double input){
		double output = input - previousDiffValue;
		previousDiffValue = input; 
		return output;
	}


	double mapMovingAverage(double input){
		double output = input * 0.05 + pastFilterValue * 0.95;
		pastFilterValue = output; 
		return output;
	}


	double mtof(double input){
	// convert midi note number to a frequency

		double output = Math.pow(2, (input-69)/12) * 440;
        return output;
		
	}
	
	double ftom(double input){
	// convert frequency val to a midi note number 
	
		double output =  69 + (12 *  (Math.log(input/440)/Math.log(2)));
		return output;
	}


	
}