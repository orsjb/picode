package test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class PI4JTest {

	I2CBus bus;
	I2CDevice device;
	byte[] bytes;

	public static void main(String[] args) throws IOException {
		PI4JTest pit = new PI4JTest();
		pit.startReading();
	}

	public PI4JTest() throws IOException {
		System.out.println("Starting sensors reading:");
		bus = I2CFactory.getInstance(I2CBus.BUS_1);
		System.out.println("Connected to bus OK!");
		// get device itself - gyro?
		device = bus.getDevice(0x6b);
		System.out.println("Connected to device OK!");
	}

	public void startReading() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					readingSensors();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(task).start();
	}

	private void readingSensors() throws IOException {
		int numElements = 3;
		int bytesPerElement = 2;
		bytes = new byte[numElements * bytesPerElement];		//assuming floats!!
		DataInputStream gyroIn;
		while (true) {
			int r = device.read(bytes, 0, bytes.length);
			System.out.println("Num bytes read: " + r);
			
			gyroIn = new DataInputStream(new ByteArrayInputStream(bytes));
			for(int i = 0; i < numElements; i++) {
				
				short s = gyroIn.readShort();
				System.out.print(s + " ");
			
			}
			System.out.println();
			try {
				Thread.sleep(700);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

}
