package test;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class PI4JTest2 {
	
	public static class MyInputListener implements GpioPinListenerDigital {
	    @Override
	    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
	        // display pin state on console
	        System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
	                + event.getState());
	    }
	}

	public static void main(String[] args) {
		final GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalInput myInPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03); 
		myInPin.addListener(new MyInputListener());
		
		while(true) {}
	}
}
