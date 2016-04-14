package core.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PrototypeReadConfig {

	public static void main(String[] args) {
		String configFile = "config/test-pi-config.json";
		
		try {
			FileReader reader = new FileReader(configFile);
			
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(reader);
			
			boolean useIP = (boolean) json.get("useIP");
			String sharedKey = (String) json.get("sharedKey");
			System.out.println("Use IP: " + useIP);
			System.out.println("Shared Key: " + sharedKey);
			
			json.forEach( (k, v) -> System.out.println(k + ": " + v) );
			
			System.out.println(json);
		} catch (FileNotFoundException e) {
			System.err.println("Unable to open file:" + configFile);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error reading file:" + configFile);
			e.printStackTrace();
		} catch (ParseException e) {
			System.err.println("Error parsing file:" + configFile);
			e.printStackTrace();
		}
	}

}
