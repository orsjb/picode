package server;

import java.io.IOException;
import java.net.SocketAddress;

import server.jfx_gui.PIRepCell;
import server.network.LocalPIRepresentation;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

/**
 * MasterServer keeps contact with all PIs. Can control them etc.
 * Connects over OSC. This is kept entirely separate from the network synch tool, which only runs on the PIs.
 * 
 * @author ollie
 */

public class ControllerMain extends Application implements OSCListener {
	
	OSCServer serv = null;
	ObservableList<LocalPIRepresentation> thePIs;

    @Override 
    public void start(Stage stage) {
    	setupGUI(stage);
    	setupServer();
    }
    
    private void setupServer() {
    	try {
    	serv = OSCServer.newUsing(OSCServer.UDP, 6666);
    	serv.start();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    	serv.addOSCListener(this);
    }
    
	private void setupGUI(Stage stage) {
    	ListView<LocalPIRepresentation> list = new ListView<LocalPIRepresentation>();
    	thePIs = FXCollections.observableArrayList();
    	list.setItems(thePIs);
    	list.setCellFactory(new Callback<ListView<LocalPIRepresentation>, ListCell<LocalPIRepresentation>>() {
			@Override
			public ListCell<LocalPIRepresentation> call(ListView<LocalPIRepresentation> arg0) {
				return new PIRepCell();
			}
		});
    	list.setPrefWidth(1000);
    	list.setPrefHeight(700);
    	Group masterGroup = new Group();
    	masterGroup.getChildren().add(list);
        Scene scene = new Scene(masterGroup); 
        stage.setTitle("--PI Controller--"); 
        stage.setScene(scene); 
        stage.sizeToScene(); 
        stage.show(); 
    }

	@Override
	public void messageReceived(OSCMessage msg, SocketAddress addr, long timestamp) {
		//incoming.
		switch(msg.getName()) {
		
		//1) regular pings from PI - regular status updates.
		
		
		//2) information about what can be controlled
		
		
		
		}
		
		
	}

    public static void main(String[] args) {
        Application.launch(args);
    }
}