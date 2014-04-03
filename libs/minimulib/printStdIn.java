import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugen.Glide;
import net.beadsproject.beads.ugen.WavePlayer;


public class printStdIn{
	public static void main (String args[]){


		AudioContext ac = new AudioContext();
		ac.start();

		Glide g = new Glide(ac, 500);
		WavePlayer wp = new WavePlayer(ac, g, Buffer.SINE);

		ac.out.addInput(wp);
		ac.out.getGainUGen().setValue(0.1f);

		try{
			BufferedReader br = new BufferedReader(new InputStreamReader (System.in));
			String input;
			while ((input=br.readLine())!=null){
				System.out.println("Java: " + input);
				float val = Float.parseFloat(input.split("[ ]")[0]);
				g.setValue(val *  2000f);
			}
		}catch(IOException io){
		io.printStackTrace();
		}
	}
}
