package controller;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import controller.jfx_gui.PIRepCell;
import controller.network.LocalPIRepresentation;
import controller.network.PIConnection;
import core.Synchronizer;

/**
 * MasterServer keeps contact with all PIs. Can control them etc.
 * Connects over OSC. This is kept entirely separate from the network synch tool, which only runs on the PIs.
 * 
 * @author ollie
 */

public class ControllerMain extends Application {
	

	PIConnection piConnection;
	Synchronizer synchronizer;

    @Override 
    public void start(Stage stage) {
    	piConnection = new PIConnection();
    	setupGUI(stage);
    	
    	
//    	piConnection.setListener(new PIConnection.Listener() {
//			public void piRemoved(LocalPIRepresentation pi) {
//				thePIs.remove(pi);
//			}
//			public void piAdded(LocalPIRepresentation pi) {
//				System.out.println("Adding PI to list: " + pi.hostname + "(" + System.currentTimeMillis() + ")");
//				thePIs.add(pi);
//				System.out.println("Added PI to list: " + pi.hostname + "(" + System.currentTimeMillis() + ")");
//			}
//		});
    	
    	
    	
    	synchronizer = Synchronizer.get();
    	//get normal desktop application behaviour - closing the stage terminates the app
    	stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	      @Override
	      public void handle (final WindowEvent event) {
	          System.exit(0);
	        }
	    });
    }
    
	private void setupGUI(Stage stage) {
    	ListView<LocalPIRepresentation> list = new ListView<LocalPIRepresentation>();
//    	thePIs = FXCollections.observableArrayList();
//    	list.setItems(thePIs);
    	
    	list.setItems(piConnection.getPIs());
    	
    	list.setCellFactory(new Callback<ListView<LocalPIRepresentation>, ListCell<LocalPIRepresentation>>() {
			@Override
			public ListCell<LocalPIRepresentation> call(ListView<LocalPIRepresentation> theView) {
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


    public static void main(String[] args) {
        Application.launch(args);
    }
}