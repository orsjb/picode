
package compositions.pipos_2015.sam;

import controller.network.SendToPI;
import core.PIPO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU;

//import pi.sonify.Sonify;
import java.util.Arrays;


public class DataShake implements PIPO {

    double lowVal = 69.0f;
    double hiVal = 81.0f;
    //Sonify sonifyer;
    // set up sonification

    // set up data
    double[] dataVector = {1.4,1.4,1.3,1.5,1.4,1.7,1.4,1.5,1.4,1.5,1.5,1.6,1.4,1.1,1.2,1.5,1.3,1.4,1.7,1.5,1.7,1.5,1,1.7,1.9,1.6,1.6,1.5,1.4,1.6,1.6,1.5,1.5,1.4,1.5,1.2,1.3,1.4,1.3,1.5,1.3,1.3,1.3,1.6,1.9,1.4,1.6,1.4,1.5,1.4,4.7,4.5,4.9,4,4.6,4.5,4.7,3.3,4.6,3.9,3.5,4.2,4,4.7,3.6,4.4,4.5,4.1,4.5,3.9,4.8,4,4.9,4.7,4.3,4.4,4.8,5,4.5,3.5,3.8,3.7,3.9,5.1,4.5,4.5,4.7,4.4,4.1,4,4.4,4.6,4,3.3,4.2,4.2,4.2,4.3,3,4.1,6,5.1,5.9,5.6,5.8,6.6,4.5,6.3,5.8,6.1,5.1,5.3,5.5,5,5.1,5.3,5.5,6.7,6.9,5,5.7,4.9,6.7,4.9,5.7,6,4.8,4.9,5.6,5.8,6.1,6.4,5.6,5.1,5.6,6.1,5.6,5.5,4.8,5.4,5.6,5.1,5.1,5.9,5.7,5.2,5,5.2,5.4,5.1};
    double oldInValue = 0;

    public DataShake() {
       // sonifyer = new Sonify(1.0f, 7.0f, (float) lowVal, (float) hiVal);
       // sonifyer.printSonificationAlgorithm();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("x");

        String fullClassName = Thread.currentThread().getStackTrace()[1].getClassName().replace(".", "/");
        SendToPI.send(fullClassName, new String[]{
                "pisound-009e959c5093.local",
//                "pisound-009e959c47ef.local",
//                "pisound-009e959c4dbc.local",
//                "pisound-009e959c3fb2.local",
//                "pisound-009e959c50e2.local",
//                "pisound-009e959c47e8.local",
//                "pisound-009e959c510a.local",
//                "pisound-009e959c502d.local"
        });
    }


    public void create(final DynamoPI d) {
        // double lowVal = 69.0f;
        // double hiVal = 81.0f;
        // Sonify sonifyer = new Sonify(1.0f, 7.0f, lowVal, hiVal);
        // set up sonification
        // sonifyer.printSonificationAlgorithm();
        // set up data
        // double[] dataVector = {1.4,1.4,1.3,1.5,1.4,1.7,1.4,1.5,1.4,1.5,1.5,1.6,1.4,1.1,1.2,1.5,1.3,1.4,1.7,1.5,1.7,1.5,1,1.7,1.9,1.6,1.6,1.5,1.4,1.6,1.6,1.5,1.5,1.4,1.5,1.2,1.3,1.4,1.3,1.5,1.3,1.3,1.3,1.6,1.9,1.4,1.6,1.4,1.5,1.4,4.7,4.5,4.9,4,4.6,4.5,4.7,3.3,4.6,3.9,3.5,4.2,4,4.7,3.6,4.4,4.5,4.1,4.5,3.9,4.8,4,4.9,4.7,4.3,4.4,4.8,5,4.5,3.5,3.8,3.7,3.9,5.1,4.5,4.5,4.7,4.4,4.1,4,4.4,4.6,4,3.3,4.2,4.2,4.2,4.3,3,4.1,6,5.1,5.9,5.6,5.8,6.6,4.5,6.3,5.8,6.1,5.1,5.3,5.5,5,5.1,5.3,5.5,6.7,6.9,5,5.7,4.9,6.7,4.9,5.7,6,4.8,4.9,5.6,5.8,6.1,6.4,5.6,5.1,5.6,6.1,5.6,5.5,4.8,5.4,5.6,5.1,5.1,5.9,5.7,5.2,5,5.2,5.4,5.1};

    }

    @Override
    public void action(final DynamoPI d) {
        System.out.println("action- Dynamo Pi");

        double[] axisVector = new double[250];
        double[] dataJitterVector = new double[dataVector.length];


        d.reset();
        int id = d.myIndex();

        final Glide g = new Glide(d.ac);
        WavePlayer wp = new WavePlayer(d.ac, g, Buffer.SINE);
        d.pl.addInput(wp);
        double jitterAmount = 0.1;
        for (int i = 0; i < dataVector.length; i++) {
            double jitterValue = Math.random() * jitterAmount * 2 - jitterAmount;
            System.out.print(jitterValue + " ");
            dataJitterVector[i] = dataVector[i] + jitterValue;
            System.out.println(dataJitterVector[i]);

        }
        if (false) {
            Arrays.sort(dataVector);
        }


        //double[] dataInput = (double[]) dataInputObj;
        double lowIn = dataVector[0];
        double highIn = dataVector[0];

        // calculate Values for data
        for (int i = 0; i < dataVector.length; i++) {
            lowIn = Math.min(dataVector[i], lowIn);
            highIn = Math.max(dataVector[i], highIn);
        }
        double offsetIn = lowIn;
        double rangeIn = highIn - lowIn;


        // if we want to have data along an x-axis matching the axis itself we need to make
        // a vector with 1000 bins - use lowIn and highIn called axisVector
        for (int ax = 0; ax < axisVector.length-1; ax++){
            double lowAx  = ax * (rangeIn/250) + offsetIn; // set bin low to bin number * range/1000 + low
            double highAx = (ax+1) * (rangeIn/250) + offsetIn;
            for (int da = 0; da < dataJitterVector.length; da++) {
                if (dataJitterVector[da] > lowAx && dataJitterVector[da] < highAx) {
                    axisVector[ax] = dataJitterVector[da];
                    break;
                } else {
                    axisVector[ax] = -99;
                }
            }
           // System.out.println("ax: " + ax + " axisVector: " + axisVector[ax] + " lowAx: " + lowAx + " highAx: " + highAx);
        }



        MiniMU.MiniMUListener myListener = new MiniMU.MiniMUListener() {

            @Override
            public void accelData(double x, double y, double z) {
               // System.out.println("Acc X Val: \t" + x + " Acc Y Val: \t" + y + " Acc Z Val: \t" + z);

                // subtract offset and add range then divide by range to get 0-1


                double offsetAcc = -4000;
                double rangeAcc = 8000;
                double indexIntoData = (x - offsetAcc) / rangeAcc;


                boolean doSonification = true;

                boolean axisOrDataAsSearchSpace = true;
                double inValue = 0;
                if (axisOrDataAsSearchSpace) {
                    // this will just find the data in the sorted or unsorted Vector.
                    int indexToValue = (int) (dataVector.length * indexIntoData);
                    // checking
                    if (indexToValue >= dataVector.length){
                        indexToValue = dataVector.length-1;
                    } else if (indexToValue < 0) {
                        indexToValue = 0;
                    }
                    inValue = dataVector[indexToValue];// map to a position in the dataVector

                } else {
                    // get the position in the interaction axis
                    int indexToValue = (int) (axisVector.length * indexIntoData);
                    // checking
                    if (indexToValue >= axisVector.length){
                        indexToValue = axisVector.length-1;
                    } else if (indexToValue < 0) {
                        indexToValue = 0;
                    }
                    inValue = axisVector[indexToValue]; // map to a position in the axisVector



                }

                double lowVal = 58.0f;
                double hiVal = 82.0f;
                double rangeOut = hiVal - lowVal;
                double offsetOut = lowVal;
                double midiValue = (((inValue - offsetIn) / rangeIn) * rangeOut) + offsetOut;
                double freq = mtof(midiValue);
                //System.out.println(midiValue + " " + freq);

                if (inValue != oldInValue && inValue != -99){

                    WavePlayer wp = new WavePlayer(d.ac, (float) freq, Buffer.SINE);

                    Gain g = new Gain(d.ac, 1, new Envelope(d.ac, 0));
                    //System.out.println(g.getEnvelopes().size());
                    g.addInput(wp);
                    d.ac.out.addInput(g);
                    g.setValue((float) freq);
                    ((Envelope) g.getGainEnvelope()).addSegment(0.9f, (int) (5));
                    KillTrigger kt = new KillTrigger(g);
                    kt.setKillListener(new Bead() {
                        public void messageReceived(Bead mess) {
                            System.out.println("Killed ");
                        }
                    });
                    ((Envelope) g.getGainEnvelope()).addSegment(0, (int) (40), kt);
                    oldInValue = inValue;
                }
            }
        };
        d.mu.addListener(myListener);
    }


    double mtof(double input){

        // convert midi note number to a frequency
        double output = (double) (Math.pow(2, (input-69)/12) * 440);
        return output;

    }

    double ftom(double input){

        // convert frequency value to a midi note number
        double output = (double) (69 + (12 *  (Math.log(input/440)/Math.log(2))));
        return output;

    }

}
