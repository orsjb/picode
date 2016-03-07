
package compositions.pipos_2015.sam;

import controller.network.SendToPI;
import core.PIPO;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.sensors.MiniMU;




public class DataShake implements PIPO {

    float lowVal = 69.0f;
    float hiVal = 81.0f;
    Sonify sonifyer = new Sonify(1.0f, 7.0f, lowVal, hiVal);
    // set up sonification

    // set up data
    double[] dataVector = {1.4,1.4,1.3,1.5,1.4,1.7,1.4,1.5,1.4,1.5,1.5,1.6,1.4,1.1,1.2,1.5,1.3,1.4,1.7,1.5,1.7,1.5,1,1.7,1.9,1.6,1.6,1.5,1.4,1.6,1.6,1.5,1.5,1.4,1.5,1.2,1.3,1.4,1.3,1.5,1.3,1.3,1.3,1.6,1.9,1.4,1.6,1.4,1.5,1.4,4.7,4.5,4.9,4,4.6,4.5,4.7,3.3,4.6,3.9,3.5,4.2,4,4.7,3.6,4.4,4.5,4.1,4.5,3.9,4.8,4,4.9,4.7,4.3,4.4,4.8,5,4.5,3.5,3.8,3.7,3.9,5.1,4.5,4.5,4.7,4.4,4.1,4,4.4,4.6,4,3.3,4.2,4.2,4.2,4.3,3,4.1,6,5.1,5.9,5.6,5.8,6.6,4.5,6.3,5.8,6.1,5.1,5.3,5.5,5,5.1,5.3,5.5,6.7,6.9,5,5.7,4.9,6.7,4.9,5.7,6,4.8,4.9,5.6,5.8,6.1,6.4,5.6,5.1,5.6,6.1,5.6,5.5,4.8,5.4,5.6,5.1,5.1,5.9,5.7,5.2,5,5.2,5.4,5.1};


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
        float lowVal = 69.0f;
        float hiVal = 81.0f;
        Sonify sonifyer = new Sonify(1.0f, 7.0f, lowVal, hiVal);
        // set up sonification
        sonifyer.printSonificationAlgorithm();
        // set up data
        double[] dataVector = {1.4,1.4,1.3,1.5,1.4,1.7,1.4,1.5,1.4,1.5,1.5,1.6,1.4,1.1,1.2,1.5,1.3,1.4,1.7,1.5,1.7,1.5,1,1.7,1.9,1.6,1.6,1.5,1.4,1.6,1.6,1.5,1.5,1.4,1.5,1.2,1.3,1.4,1.3,1.5,1.3,1.3,1.3,1.6,1.9,1.4,1.6,1.4,1.5,1.4,4.7,4.5,4.9,4,4.6,4.5,4.7,3.3,4.6,3.9,3.5,4.2,4,4.7,3.6,4.4,4.5,4.1,4.5,3.9,4.8,4,4.9,4.7,4.3,4.4,4.8,5,4.5,3.5,3.8,3.7,3.9,5.1,4.5,4.5,4.7,4.4,4.1,4,4.4,4.6,4,3.3,4.2,4.2,4.2,4.3,3,4.1,6,5.1,5.9,5.6,5.8,6.6,4.5,6.3,5.8,6.1,5.1,5.3,5.5,5,5.1,5.3,5.5,6.7,6.9,5,5.7,4.9,6.7,4.9,5.7,6,4.8,4.9,5.6,5.8,6.1,6.4,5.6,5.1,5.6,6.1,5.6,5.5,4.8,5.4,5.6,5.1,5.1,5.9,5.7,5.2,5,5.2,5.4,5.1};

    }

    @Override
    public void action(final DynamoPI d) {
        System.out.println("Dynamo Pi");
        sonifyer.printSonificationAlgorithm();

        d.reset();
        int id = d.myIndex();

        final Glide g = new Glide(d.ac);
        WavePlayer wp = new WavePlayer(d.ac, g, Buffer.SINE);
        d.pl.addInput(wp);

        MiniMU.MiniMUListener myListener = new MiniMU.MiniMUListener() {
            @Override
            public void accelData(double x, double y, double z) {


            }
            //
            public void gyroData(double x, double y, double z){
                System.out.println("Gyro x Val: " + x + "Gyro y Val: " + y +"Gyro z Val: " + z);
                // subtract offset and add range then divide by range to get 0-1
                double offset = -4000;
                double range = 8000;
                //double indexIntoData = (x - offset) / range;
                //son.indexToValue(indexIntoData);
                //double freq = son.getOutputMTOF();

               // WavePlayer wp = new WavePlayer(d.ac, (float) freq, Buffer.SINE);
                //Gain g2 = new Gain(d.ac, 1, new Envelope(d.ac, 0));
                //g2.addInput(wp);
                //d.ac.out.addInput(g2);
              //  ((Envelope)g2.getGainEnvelope()).addSegment(0.1f, (int) (Math.random() * 200));
                //((Envelope)g2.getGainEnvelope()).addSegment(0,  (int) (Math.random() * 7000), new KillTrigger(g2));


                //g2.setValue((float) freq);

            }
            //
            public void freefallEvent() {}
            //
        };
        d.mu.addListener(myListener);

//		d.sound(c);

    }

}
