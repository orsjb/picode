package controller.jfx_gui;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import controller.network.PIConnection;

public abstract class GUIBuilder {

	public static void createButtons(Pane pane, final PIConnection piConnection) {
    	
		
		//master buttons
    	
		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piReboot();
				}
			});
	    	b.setText("Reboot");
	    	pane.getChildren().add(b);
		}
		

		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piShutdown();
				}
			});
	    	b.setText("Shutdown");
	    	pane.getChildren().add(b);
		}
    	

		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piSync();
				}
			});
	    	b.setText("Sync");
	    	pane.getChildren().add(b);
		}

		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piReset();
				}
			});
	    	b.setText("Reset");
	    	pane.getChildren().add(b);
		}

		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piResetSounding();
				}
			});
	    	b.setText("Reset Sounding");
	    	pane.getChildren().add(b);
		}

		{
			Button b = new Button();
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					piConnection.piClearSound();
				}
			});
	    	b.setText("Clear Sound");
	    	pane.getChildren().add(b);
		}
		
		//text sender
		final TextField codeField = new TextField();
		codeField.setMinSize(500, 50);
		pane.getChildren().add(codeField);
		
		for(int i = 1; i <= 4; i++) {
			Button b = new Button();
			final int index = i;
	    	b.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					String codeText = codeField.getText();
					//need to parse the code text
					String[] elements = codeText.split("[ ]");
					String msg = elements[0];
					Object[] args = new Object[elements.length - 1];
					for(int i = 0; i < args.length; i++) {
						String s = elements[i + 1];
						try {
							args[i] = Integer.parseInt(s);
						} catch(Exception ex) {
							try {
								args[i] = Double.parseDouble(s);
							} catch(Exception exx) {
								args[i] = s;
							}
						}
					}
					piConnection.sendToPIGroup(index, msg, args);
				}
			});
	    	b.setText("Send " + i);
	    	pane.getChildren().add(b);
		}
		
	
		
    	
	}
	
}
