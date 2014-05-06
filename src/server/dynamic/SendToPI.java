package server.dynamic;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class SendToPI {
	private static final String clientListPath = "temp/clients.temp";
	
	public static void send(String fullClassName, Object hostname) throws Exception {
		send(fullClassName, new Object[] {hostname});
	}

	public static void send(String fullClassName, Object[] hostnames) throws Exception {
		String[] ips = resolveHostnamesToIPs(hostnames);
		String simpleClassName = new File(fullClassName).getName();
		String packagePath = new File(fullClassName).getParent();
		sendClassFileWithEnclosedClasses(packagePath, simpleClassName, ips);
	}
	
	private static void sendClassFileWithEnclosedClasses(String packagePath, String className, String[] hostnames) throws Exception {
		File packageDir = new File("bin/" + packagePath);
		File[] contents = packageDir.listFiles();
		for(File f : contents) {
			String fname = f.getName();
			if(fname.startsWith(className + "$") && fname.endsWith(".class")) {
				SendToPI.sendClassFile(packagePath + "/" + fname, hostnames);
			}
		}
		SendToPI.sendClassFile(packagePath + "/" + className + ".class", hostnames);
	}
	
	private static void sendClassFile(String fullClassFileName, String[] hostnames) throws Exception {
		FileInputStream fis = new FileInputStream(new File("bin/" + fullClassFileName));
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int data = fis.read();
        while(data != -1){
            buffer.write(data);
            data = fis.read();
        }
        fis.close();
        byte[] bytes = buffer.toByteArray();
        for(String hostname : hostnames) {
			Socket s = new Socket(hostname, 1234);
			s.getOutputStream().write(bytes);
			s.close();
        }
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
	
	public static byte[] objectToByteArray(Object object) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(object);
		  bytes = bos.toByteArray();
		  out.close();
		  bos.close();
		} catch(Exception e) {
			e.printStackTrace();
		} 
		return bytes;
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

