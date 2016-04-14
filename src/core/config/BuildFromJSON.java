package core.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public interface BuildFromJSON<T> {
	ArrayList<JSONMapping<?>> getMappings();
	T build();
	
	default JSONObject readJSONFile(String fileName) {
		JSONObject json = null;
		try {
			FileReader reader = new FileReader(fileName);
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(reader);
			
		} catch (FileNotFoundException e) {
			System.err.println("Unable to open file:" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error reading file:" + fileName);
			e.printStackTrace();
		} catch (ParseException e) {
			System.err.println("Error parsing file:" + fileName);
			e.printStackTrace();
		}
		return json;
	}
}
