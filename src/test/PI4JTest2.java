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
		System.out.println("Got GPIO instance.");
		GpioPinDigitalInput myInPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03); 
		System.out.println("Got GPIO pin 3.");
		myInPin.addListener(new MyInputListener());
		System.out.println("Set up listener for GPIO pin 3.");
		
		while(true) {}
	}
}
