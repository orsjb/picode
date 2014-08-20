package controller.network;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import core.Config;


public class SendToPI {

	public static void send(String fullClassName, String[] hostnames) throws Exception {
		String simpleClassName = new File(fullClassName).getName();
		String packagePath = new File(fullClassName).getParent();
//		sendOLD(packagePath, simpleClassName, hostnames);
		send(packagePath, simpleClassName, hostnames);
	}
	
	public static void send(String packagePath, String className, String[] hostnames) throws Exception {
		File packageDir = new File("bin/" + packagePath);
		File[] contents = packageDir.listFiles();
		ArrayList<byte[]> allFilesAsBytes = new ArrayList<byte[]>();
		for(File f : contents) {
			String fname = f.getName();
			if(fname.startsWith(className + "$") && fname.endsWith(".class")) {
				allFilesAsBytes.add(getClassFileAsByteArray(packagePath + "/" + fname));
			}
		}
		allFilesAsBytes.add(getClassFileAsByteArray(packagePath + "/" + className + ".class"));
		//now we have all the files as byte arrays
		//time to send
		for(String hostname : hostnames) {
        	try {
				//send all of the files to this hostname
				for(byte[] bytes : allFilesAsBytes) {
					Socket s = new Socket(hostname, Config.codeToPIPort);
					s.getOutputStream().write(bytes);
					s.close();
				}
        	} catch(Exception e) {
        		System.out.println("SendToPI: unable to send to " + hostname);
        	}
        } 
	}
	
	public static void sendOLD(String packagePath, String className, String[] hostnames) throws Exception {
		File packageDir = new File("bin/" + packagePath);
		File[] contents = packageDir.listFiles();
		for(File f : contents) {
			String fname = f.getName();
			if(fname.startsWith(className + "$") && fname.endsWith(".class")) {
				SendToPI.sendClassFileOLD(packagePath + "/" + fname, hostnames);
			}
		}
		SendToPI.sendClassFileOLD(packagePath + "/" + className + ".class", hostnames);
	}
	
	private static void sendClassFileOLD(String fullClassFileName, String[] hostnames) throws Exception {
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
        	try {
				Socket s = new Socket(hostname, Config.codeToPIPort);
				s.getOutputStream().write(bytes);
				s.close();
        	} catch(UnknownHostException e) {
        		System.out.println("SendToPI: Was not able to send file " + fullClassFileName + " to " + hostname);
        	}
        }        
	}

	public static byte[] getClassFileAsByteArray(String fullClassFileName) throws Exception {
		FileInputStream fis = new FileInputStream(new File("bin/" + fullClassFileName));
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int data = fis.read();
        while(data != -1){
            buffer.write(data);
            data = fis.read();
        }
        fis.close();
        byte[] bytes = buffer.toByteArray();
        buffer.close();
        return bytes;
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

