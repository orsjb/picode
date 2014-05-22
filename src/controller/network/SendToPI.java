package controller.network;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import core.Config;


public class SendToPI {

	public static void send(String fullClassName, String[] hostnames) throws Exception {
		String simpleClassName = new File(fullClassName).getName();
		String packagePath = new File(fullClassName).getParent();
		send(packagePath, simpleClassName, hostnames);
	}
	
	public static void send(String packagePath, String className, String[] hostnames) throws Exception {
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
			Socket s = new Socket(hostname, Config.codeToPIPort);
			s.getOutputStream().write(bytes);
			s.close();
        }
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
}

