package sensors;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class MiniMU {
	
	public static abstract class MiniMUListener {
		public void accelData(double x, double y, double z) {}
		public void gyroData(double x, double y, double z) {}
		public void magData(double x, double y, double z) {}
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
	
	MiniMUListener listener;
	
	public MiniMU(MiniMUListener listener) {
		this();
		setListener(listener);
	}

	public MiniMU() {
		try {
			System.out.println("Starting sensors reading:");
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
			System.out.println("Connected to bus OK!");
			// GYRO
			gyrodevice = bus.getDevice(GYR_ADDRESS);
//			gyrodevice.write(0x20, (byte) 0b00001111);
//			gyrodevice.write(0x23, (byte) 0b00110000);
			// ACCEL
			acceldevice = bus.getDevice(ACC_ADDRESS);
			acceldevice.write(0x20, (byte) 0b01010111);
			acceldevice.write(0x23, (byte) 0b00101000);
			// MAG
//			magdevice = bus.getDevice(MAG_ADDRESS);
//			gyrodevice.write(0x20, (byte) 0b00001111);
//			gyrodevice.write(0x23, (byte) 0b00110000);
			// TEMP
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setListener(MiniMUListener listener) {
		this.listener = listener;
	}

	public void start() {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
//						float[] gyroData = readingSensorsGyro();
						float[] accelData = readingSensorsAccel();
//						System.out.println(gyroData[0] + "\t" + gyroData[1] + "\t" + gyroData[2] + "\t" + accelData[0] + "\t" + accelData[1] + "\t" + accelData[2] + "\t");
//						System.out.println(accelData[0] + "\t" + accelData[1] + "\t" + accelData[2] + "\t");
//						double M_PI = 3.14159265358979323846;
//						double RAD_TO_DEG = 57.29578;
//						double accXangle = (float) (Math.atan2(accelData[1],accelData[2])+M_PI)*RAD_TO_DEG;
//						double accYangle = (float) (Math.atan2(accelData[2],accelData[0])+M_PI)*RAD_TO_DEG;
						//pass data on to listeners
						if(listener != null) {
							listener.accelData(accelData[0], accelData[1], accelData[2]);
//							listener.gyroData(x, y, z);
//							listener.magData(x, y, z);
//							listener.tempData(t);
						}
						Thread.sleep(10);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		new Thread(task).start();
	}

	private float[] readingSensorsGyro() throws IOException {
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
//			System.out.print(bits2String(abits) + ":" + bits2String(bbits) + "   ");
			boolean[] shortybits = new boolean[16];
			for(int j = 0; j < 8; j++) {
				shortybits[j] = bbits[j];
			}
			for(int j = 0; j < 8; j++) {
				shortybits[j + 8] = abits[j];
			}
			int theInt = bits2Int(shortybits);
			result[i] = theInt / 5000f;
		}
		return result;
	}
	
	private float[] readingSensorsAccel() throws IOException {
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
//			System.out.print(bits2String(abits) + ":" + bits2String(bbits));
			boolean[] shortybits = new boolean[12];
			for(int j = 0; j < 8; j++) {
				shortybits[j] = bbits[j];
			}
			for(int j = 0; j < 4; j++) {
				shortybits[j + 8] = abits[j];
			}
			int theInt = bits2Int(shortybits);
			result[i] = theInt;
			System.out.print(bits2String(shortybits) + " (" + theInt + ")     ");
		}
		System.out.println();
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
		for(int i = 0; i < length; i++) {
//			result += Math.pow(2, (bbits[length - i]?1:0));
			result += bbits[length - i]? Math.pow(2, i) : 0;
		}
		if(bbits[0]) result = -result;
		return result;
	}
	
	public static String byte2Str(byte inByte) {
		boolean[] bbits = getBits(inByte);
		return bits2String(bbits);
	}

}