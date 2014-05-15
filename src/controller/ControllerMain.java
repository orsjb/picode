package controller;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import controller.jfx_gui.PIRepCell;
import controller.network.LocalPIRepresentation;
import controller.network.PIConnection;

/**
 * MasterServer keeps contact with all PIs. Can control them etc.
 * Connects over OSC. This is kept entirely separate from the network synch tool, which only runs on the PIs.
 * 
 * @author ollie
 */

public class ControllerMain extends Application {
	
	ObservableList<LocalPIRepresentation> thePIs;
	PIConnection piConnection;

    @Override 
    public void start(Stage stage) {
    	setupGUI(stage);
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


    public static void main(String[] args) {
        Application.launch(args);
    }
}