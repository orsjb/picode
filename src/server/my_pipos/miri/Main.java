package server.my_pipos.miri;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		//My composition
		//setup
		DickensPIPO.main(args);
		//wait for setup
		Thread.sleep(10000);
		
		//start with babble
		BabblePIPO.main(args);
		
		
		
		
		
	}

}
