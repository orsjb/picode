package compositions.pipos_2013;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import controller.network.SendToPI;

public class OldHostnameMethod {

	private static final String clientListPath = "temp/clients.temp";
	
	public static void send(String fullClassName, Object hostname) throws Exception {
		send(fullClassName, new Object[] {hostname});
	}

	public static void send(String fullClassName, Object[] hostnames) throws Exception {
		String[] ips = resolveHostnamesToIPs(hostnames);
		String simpleClassName = new File(fullClassName).getName();
		String packagePath = new File(fullClassName).getParent();
		SendToPI.send(packagePath, simpleClassName, ips);
	}
	
	private static String[] resolveHostnamesToIPs(Object[] hostnames) { //NOTE: This method will accept either IDs or names, or both
		ArrayList<String> ips = new ArrayList<String>();
		HashMap<Integer, String> idsMap = new HashMap<Integer, String>();
		HashMap<String, String> namesMap = new HashMap<String, String>();
		Scanner scanner;
		
		try {
			scanner = new Scanner(new File(clientListPath)); //TODO: ensure we are not reading the file at the same time node.js is modifying it
			while (scanner.hasNext()) {
				int id = Integer.parseInt(scanner.next());
				String name = scanner.next();
				String ip = scanner.next();
				idsMap.put(id, ip);
				namesMap.put(name, ip);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (Object o : hostnames) {
			if (o instanceof Integer) {
				if (!idsMap.containsKey(o))
					throw new InvalidHostnameException(o.toString() + " is not a valid hostname.");
				ips.add(idsMap.get(o));
			} else if (o instanceof String) {
				if (!namesMap.containsKey(o))
					throw new InvalidHostnameException(o + " is not a valid hostname.");
				ips.add(namesMap.get(o));
			}
		}
		
		return ips.toArray(new String[0]);
	}

	public static class InvalidHostnameException extends RuntimeException {
		public InvalidHostnameException() {
			super();
		}
		
		public InvalidHostnameException(String message) {
			super(message);
		}
	}
}
