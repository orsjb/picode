package core.test;

import java.util.ArrayList;

import org.json.simple.JSONObject;

import core.config.BuildFromJSON;
import core.config.JSONMapping;
import core.config.JSONString;

public class PrototypeBuildClassFromJSON {
	
	public static testConfig getConfig(String fileName) {
		BuildFromJSON<testConfig> configMapper = new BuildFromJSON<testConfig>() {
			ArrayList<JSONMapping<?>> mappings = getMappings();
			JSONObject json = readJSONFile(fileName);

			@Override
			public ArrayList<JSONMapping<?>> getMappings() {
				ArrayList<JSONMapping<?>> jsonKeyMappings = new ArrayList<JSONMapping<?>>();
				//Type mappings:
				jsonKeyMappings.add(new JSONString("sharedKey"));
				return jsonKeyMappings;
			}

			@Override
			public testConfig build() {
				return new testConfig(
					(String) mappings.get(0).value(json)
				);
			}
			
		};
		return configMapper.build();
	}
	
	class testConfig {
		String sharedKey;

		public testConfig(String sharedKey) {
			super();
			this.sharedKey = sharedKey;
		}
		
	}
	
	public static void main(String[] args) {
		testConfig config = getConfig("config/test-pi-config.json");
		System.out.println(config);
	}
}
