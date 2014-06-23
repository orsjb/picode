package controller;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import controller.jfx_gui.GUIBuilder;
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
    	//test code...
//    	piConnection.createTestPI();
//    	piConnection.createTestPI();
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
		Group masterGroup = new Group();
		BorderPane border = new BorderPane();
		HBox hbox = new HBox();
		hbox.setMinHeight(100);
		border.setTop(hbox);
		VBox vbox = new VBox();
		vbox.setMinWidth(50);
		border.setLeft(vbox);
		border.setRight(new VBox());
		masterGroup.getChildren().add(border);
    	//list of PIs
		ListView<LocalPIRepresentation> list = new ListView<LocalPIRepresentation>();
    	list.setItems(piConnection.getPIs());
    	list.setCellFactory(new Callback<ListView<LocalPIRepresentation>, ListCell<LocalPIRepresentation>>() {
			@Override
			public ListCell<LocalPIRepresentation> call(ListView<LocalPIRepresentation> theView) {
				return new PIRepCell();
			}
		});
    	list.setMinWidth(1000);
    	list.setMaxWidth(1000);
    	list.setMinHeight(700);
       	border.setCenter(list);
       	GUIBuilder.createButtons((Pane)border.getTop(), piConnection);
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