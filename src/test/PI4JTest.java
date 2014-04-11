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
		
		
		/*
		 * writeGyrReg(L3G_CTRL_REG1, 0b00001111); // Normal power mode, all axes enabled
		 * writeGyrReg(L3G_CTRL_REG4, 0b00110000); // Continuous update, 2000 dps full scale
		 */
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
		int numElements = 3;									//
		int bytesPerElement = 2;								//assuming short?
		int numBytes = numElements * bytesPerElement;			//
		bytes = new byte[numBytes];								//
		DataInputStream gyroIn;
		while (true) {
			int r = device.read(bytes, 0, bytes.length);
			System.out.println("Num bytes read: " + r);
			
			gyroIn = new DataInputStream(new ByteArrayInputStream(bytes));
			for(int i = 0; i < numElements; i++) {
				
				byte a = gyroIn.readByte();
				byte b = gyroIn.readByte();
				
				float f = (short)(b << 8 | a);
				
				System.out.print(f + " ");
			
			}
			System.out.println();
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		
		/*
		 * The C code...
		 * 
		 
        uint8_t block[6];
        selectDevice(file,GYR_ADDRESS);
 
        readBlock(0x80 | L3G_OUT_X_L, sizeof(block), block);
 
        *g = (int16_t)(block[1] << 8 | block[0]);
        *(g+1) = (int16_t)(block[3] << 8 | block[2]);
        *(g+2) = (int16_t)(block[5] << 8 | block[4]); }
        
         *
		 */
		
	}

}
