package test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class PI4JTest {

	final static byte MAG_ADDRESS = 0x1e;
	final static byte ACC_ADDRESS = 0x19;
	final static byte GYR_ADDRESS = 0x6b;

	final static int MAG_DATA_ADDR = 0xa8;
	final static int GYRO_DATA_ADDR = 0xa8;
	final static int ACC_DATA_ADDR = 0xa8;


	I2CBus bus;
	I2CDevice gyrodevice, acceldevice, magdevice;
	
	

	public static void main(String[] args) throws IOException {
		
//		System.out.println(String.format("%02X", 0x80 | 0x28));
		
		
		PI4JTest pit = new PI4JTest();
		pit.startReading();
	}

	public PI4JTest() throws IOException {
		System.out.println("Starting sensors reading:");
		bus = I2CFactory.getInstance(I2CBus.BUS_1);
		System.out.println("Connected to bus OK!");
		// get devices!
//		System.out.println("Connected to devices OK!");

		/*
		 * writeGyrReg(L3G_CTRL_REG1, 0b00001111); // Normal power mode, all
		 * axes enabled writeGyrReg(L3G_CTRL_REG4, 0b00110000); // Continuous
		 * update, 2000 dps full scale
		 */

		// GYRO
		gyrodevice = bus.getDevice(GYR_ADDRESS);
		gyrodevice.write(0x20, (byte) 0b00001111);
		gyrodevice.write(0x23, (byte) 0b00110000);

		// ACCEL
		// Normal power mode, all axes enabled -- 0x20
		// Continuous update, 2000 dps full scale -- 0x23
		acceldevice = bus.getDevice(ACC_ADDRESS);
		acceldevice.write(0x20, (byte) 0b01010111);
		acceldevice.write(0x23, (byte) 0b00101000);

		// MAG
//		magdevice = bus.getDevice(MAG_ADDRESS);
//		gyrodevice.write(0x20, (byte) 0b00001111);
//		gyrodevice.write(0x23, (byte) 0b00110000);

		// TEMP?????

	}

	public void startReading() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
//						readingSensorsGyro();
						readingSensorsAccel();
						Thread.sleep(10);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		new Thread(task).start();
	}

	private void readingSensorsGyro() throws IOException {
		System.out.println("GYRO");
		int numElements = 3; //
		int bytesPerElement = 2; // assuming short?
		int numBytes = numElements * bytesPerElement; //
		byte[] bytes = new byte[numBytes]; //
		DataInputStream gyroIn;
		int r = gyrodevice.read(GYRO_DATA_ADDR, bytes, 0, bytes.length);
//			System.out.println("Num bytes read: " + r);
		gyroIn = new DataInputStream(new ByteArrayInputStream(bytes));
		for (int i = 0; i < numElements; i++) {
			byte a = gyroIn.readByte();
			byte b = gyroIn.readByte();
			float f = (b << 8 | a);
			System.out.print(a + ":" + b + "  " + "(" + f + "), ");
		}
		System.out.println();
	}
	
	private void readingSensorsAccel() throws IOException {
		System.out.println("ACCEL");
		int numElements = 3; //
		int bytesPerElement = 2; // assuming short?
		int numBytes = numElements * bytesPerElement; //
		byte[] bytes = new byte[numBytes]; //
		
		
		
		DataInputStream accelIn;
		int r = acceldevice.read(0xa8, bytes, 0, bytes.length);
		

		//read the bytes backwards
//		byte[] backwardBytes = new byte[numBytes];
//		for(int i = 0; i < numBytes; i++) {
//			backwardBytes[numBytes - i - 1] = bytes[i];
//		}
		
//			System.out.println("Num bytes read: " + r);
//		accelIn = new DataInputStream(new ByteArrayInputStream(backwardBytes));
		accelIn = new DataInputStream(new ByteArrayInputStream(bytes));
		for (int i = 0; i < numElements; i++) {
		
			byte a = accelIn.readByte();	//least sig
			byte b = accelIn.readByte(); //most sig
			
//			short s = accelIn.readShort();
			
//			String aString = String.format("%02X", a);
//			String bString = String.format("%02X", b);
			
//			String aString = Integer.toBinaryString((int)a);
//			String bString = Integer.toBinaryString((int)b);
			
//			int x = (b | a << 8)>>4;
			
//			System.out.print(aString + ":" + bString + "  ");
//			System.out.print(a + ":" + b + "(" + x + ") -- ");
			System.out.print(byte2Str(a) + ":" + byte2Str(b) + " ");
//			System.out.print(s + "  ");
		}
		System.out.println();
	}
	

	/*
	 * The C code...
	 * 
	 * 
	 * uint8_t block[6]; selectDevice(file,GYR_ADDRESS);
	 * 
	 * readBlock(0x80 | L3G_OUT_X_L, sizeof(block), block);
	 * 
	 * g = (int16_t)(block[1] << 8 | block[0]);(g+1) = (int16_t)(block[3] <<
	 * 8 | block[2]);(g+2) = (int16_t)(block[5] << 8 | block[4]); }
	 */
	
	
	
	private static boolean[] getBits(byte inByte) {
		boolean[] bits = new boolean[8];
		for (int j = 0; j < 8; j++) {
			// Shift each bit by 1 starting at zero shift
			byte tmp = (byte) (inByte >> j);
			// Check byte with mask 00000001 for LSB
			bits[7-j] = (tmp & 0x01) == 1;
		}
		return bits;
	}
	

	
	private static String byte2Str(byte inByte) {
		boolean[] bbits = getBits(inByte);
		StringBuffer b = new StringBuffer();
		for(boolean v : bbits) {
			b.append(v?1:0);
		}
		return b.toString();
	}

}
