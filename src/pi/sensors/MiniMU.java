package pi.sensors;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class MiniMU {
	
	public static abstract class MiniMUListener {
		public void accelData(double x, double y, double z) {}
		public void gyroData(double x, double y, double z) {}
		public void magData(double x, double y, double z) {}
		public void imuData(double x, double y, double z,double x2, double y2, double z2,double x3, double y3, double z3) {}
		public void tempData(double t) {}
	}

	final static byte MAG_ADDRESS = 0x1e;
	final static byte ACC_ADDRESS = 0x19;
	final static byte GYR_ADDRESS = 0x6b;

	final static int MAG_DATA_ADDR = 0xa8;
	final static int GYRO_DATA_ADDR = 0xa8;
	final static int ACC_DATA_ADDR = 0xa8;

	I2CBus bus;
	I2CDevice gyrodevice, acceldevice, magdevice;
	
	public Set<MiniMUListener> listeners = new HashSet<MiniMUListener>();
	
	public MiniMU(MiniMUListener listener) {
		this();
		addListener(listener);
	}

	public MiniMU() {
		try {
			System.out.println("Starting sensors reading:");
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
			System.out.println("Connected to bus OK!");

			// GYRO
			gyrodevice = bus.getDevice(GYR_ADDRESS);
			gyrodevice.write(0x20, (byte) 0b00001111);
			gyrodevice.write(0x23, (byte) 0b00110000);

			// ACCEL
			acceldevice = bus.getDevice(ACC_ADDRESS);
			acceldevice.write(0x20, (byte) 0b01000111);
			acceldevice.write(0x23, (byte) 0b00101000);

			
//	        //// LSM303DLHC Magnetometer (from the c code)
//
//	        // DO = 011 (7.5 Hz ODR)
//	        writeMagReg(LSM303_CRA_REG_M, 0b00001100);
//			    #define LSM303_CRA_REG_M         0x00 // LSM303DLH, LSM303DLM, LSM303DLHC
//	        // GN = 001 (+/- 1.3 gauss full scale)
//	        writeMagReg(LSM303_CRB_REG_M, 0b00100000);
//              #define LSM303_CRB_REG_M         0x01 // LSM303DLH, LSM303DLM, LSM303DLHC

			//	        // MD = 00 (continuous-conversion mode)
//	        writeMagReg(LSM303_MR_REG_M, 0b00000000);
//    			#define LSM303_MR_REG_M  0x02 // LSM303DLH, LSM303DLM, LSM303DLHC

			// COMPASS enable
			magdevice = bus.getDevice(MAG_ADDRESS);
			magdevice.write(0x00, (byte) 0b00001100);// DO = 011 (7.5 Hz ODR)
			magdevice.write(0x01, (byte) 0b00100000);// GN = 001 (+/- 1.3 gauss full scale)
			magdevice.write(0x02, (byte) 0b00000000);// MD = 00 (continuous-conversion mode)
			
			
		} catch(IOException e) {
			System.out.println("Warning: unable to communicate with the MiniMU, we're not going to be getting any sensor data :-(");
			e.printStackTrace();
		}
	}
	
	public void addListener(MiniMUListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(MiniMUListener listener) {
		listeners.remove(listener);
	}
	
	public void clearListeners() {
		listeners.clear();
	}
	
	public void start() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						float[] gyroData = readSensorsGyro();
						float[] accelData = readSensorsAccel();
						float[] magData = readSensorsMag();

						//pass data on to listeners
						for(MiniMUListener listener : listeners) {
							if (accelData.length > 0 ){ // test for empty array. 
								listener.accelData(accelData[0], accelData[1], accelData[2]);
								listener.gyroData(  gyroData[0],  gyroData[1],  gyroData[2]);
								listener.magData(    magData[0],   magData[1],   magData[2]);
							
								listener.imuData(  accelData[0], accelData[1], accelData[2], 
													gyroData[0],  gyroData[1],  gyroData[2], 
													 magData[0],   magData[1],   magData[2]);
							}
						}
					} catch (IOException e) {
//						System.out.println("MiniMU not receiving data.");
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		new Thread(task).start();
	}

	private float[] readSensorsGyro() throws IOException {
		int numElements = 3; //
		float[] result = new float[numElements];
		int bytesPerElement = 2; // assuming short?
		int numBytes = numElements * bytesPerElement; //
		byte[] bytes = new byte[numBytes]; //
		DataInputStream accelIn;
		gyrodevice.read(0xa8, bytes, 0, bytes.length);
		accelIn = new DataInputStream(new ByteArrayInputStream(bytes));
		for (int i = 0; i < numElements; i++) {
			byte a = accelIn.readByte(); //least sig
			byte b = accelIn.readByte(); //most sig
			boolean[] abits = getBits(a);
			boolean[] bbits = getBits(b);
			boolean[] shortybits = new boolean[16];
			for(int j = 0; j < 8; j++) {
				shortybits[j] = bbits[j];
			}
			for (int j = 0; j < 8; j++) {
				shortybits[j + 8] = abits[j];
			}
			int theInt = bits2Int(shortybits);
			result[i] = theInt;
		}
		return result;
	}
	
	private float[] readSensorsAccel() throws IOException {
		int numElements = 3; //
		float[] result = new float[numElements];
		int bytesPerElement = 2; // assuming short?
		int numBytes = numElements * bytesPerElement; //
		byte[] bytes = new byte[numBytes]; //
		DataInputStream accelIn;
		acceldevice.read(0xa8, bytes, 0, bytes.length);
		accelIn = new DataInputStream(new ByteArrayInputStream(bytes));
		for (int i = 0; i < numElements; i++) {
			byte a = accelIn.readByte(); //least sig
			byte b = accelIn.readByte(); //most sig
			boolean[] abits = getBits(a);
			boolean[] bbits = getBits(b);
			boolean[] shortybits = new boolean[16];
			for(int j = 0; j < 8; j++) {
				shortybits[j] = bbits[j];
			}
			for(int j = 0; j < 8; j++) {
				shortybits[j + 8] = abits[j];
			}
			int theInt = bits2Int(shortybits);
			result[i] = theInt;
		}
		return result;
	}
	
	private float[] readSensorsMag() throws IOException {
		int numElements = 3; //
		float[] result = new float[numElements];
		int bytesPerElement = 2; // assuming short?
		int numBytes = numElements * bytesPerElement; //
		byte[] bytes = new byte[numBytes]; //
		DataInputStream magIn;
		magdevice.read(0x83, bytes, 0, bytes.length);
		magIn = new DataInputStream(new ByteArrayInputStream(bytes));
		for (int i = 0; i < numElements; i++) {
			byte a = magIn.readByte(); //least sig
			byte b = magIn.readByte(); //most sig
			boolean[] abits = getBits(a);
			boolean[] bbits = getBits(b);
			boolean[] shortybits = new boolean[16];
			// The mag sensor is BIG ENDIAN on the lsm303dlhc 
			// so lets flip b and a compared to Acc
			for(int j = 0; j < 8; j++) {
				shortybits[j] = abits[j];
			}
			for(int j = 0; j < 8; j++) {
				shortybits[j + 8] = bbits[j];
			}
			int theInt = bits2Int(shortybits);
			result[i] = theInt;
		}
		return result;
	}
	
	public static boolean[] getBits(byte inByte) {
		boolean[] bits = new boolean[8];
		for (int j = 0; j < 8; j++) {
			// Shift each bit by 1 starting at zero shift
			byte tmp = (byte) (inByte >> j);
			// Check byte with mask 00000001 for LSB
			bits[7-j] = (tmp & 0x01) == 1;
		}
		return bits;
	}
	
	public static String bits2String(boolean[] bbits) {
		StringBuffer b = new StringBuffer();
		for(boolean v : bbits) {
			b.append(v?1:0);
		}
		return b.toString();
	}
	
	public static int bits2Int(boolean[] bbits) {
		int result = 0;
		int length = bbits.length - 1;
		if (bbits[0]) { // if the most significant bit is true
			for(int i = 0; i < length; i++) { //
				result -= bbits[length - i] ? 0 : Math.pow(2, i) ; // use the negative complement version 
			}
		} else {
			for(int i = 0; i < length; i++) {
				result += bbits[length - i]? Math.pow(2, i) : 0; // use the positive version
			}
		}
		return result;
	}
	
	public static String byte2Str(byte inByte) {
		boolean[] bbits = getBits(inByte);
		return bits2String(bbits);
	}

}
