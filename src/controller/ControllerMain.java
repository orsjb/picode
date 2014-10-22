package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import controller.jfx_gui.GUIBuilder;
import controller.jfx_gui.PIRepCell;
import controller.launchpad.LaunchPad;
import controller.launchpad.LaunchPadBehaviour;
import controller.network.LocalPIRepresentation;
import controller.network.PIConnection;
import core.Synchronizer;

/**
 * MasterServer keeps contact with all PIs. Can control them etc.
 * Connects over OSC. This is kept entirely separate from the network synch tool, which only runs on the PIs.
 * 
 * @author ollie
 */

public class ControllerMain extends Application implements LaunchPadBehaviour {

	PIConnection piConnection;
	Synchronizer synchronizer;
	LaunchPad launchpad;
	

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
    	//set up the LaunchPad
    	launchpad = new LaunchPad(new String[] {"Launchpad"}, this);
    }
    
	private void setupGUI(Stage stage) {
		Group masterGroup = new Group();
		BorderPane border = new BorderPane();
		VBox topBox = new VBox();
		topBox.setMinHeight(100);
		border.setTop(topBox);
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
       	GUIBuilder.createButtons(topBox, piConnection);
       	
       	
       	//list of compositions
       	List<String> compositionFileNames = new ArrayList<String>();
       	Queue<File> dirs = new LinkedList<File>();
       	dirs.add(new File("./bin/compositions"));
       	while (!dirs.isEmpty()) {
       	  for (File f : dirs.poll().listFiles()) {
       	    if (f.isDirectory()) {
       	      dirs.add(f);
       	    } else if (f.isFile()) {
       	    	compositionFileNames.add(f.getAbsolutePath());
       	    }
       	  }
       	}
       	ContextMenu menu = new ContextMenu();
       	//TODO - complete this, sending the files
       	
       	//
       	
       	
        Scene scene = new Scene(masterGroup); 
        stage.setTitle("--PI Controller--"); 
        stage.setScene(scene); 
        stage.sizeToScene(); 
        stage.show(); 
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

	@Override
	public void buttonAction(LaunchPad parent, int launchPadID, int row, int col, boolean push) {
		if(push) {
			if(row == 0) {
				//top row - tempo stuff
				int tempoIndex = col;
				piConnection.sendToAllPIs("/launchpad/tempo", new Object[] {tempoIndex});
			} else if(col == 8) {
				//right column - pitch stuff
				int pitchIndex = row - 1;
				piConnection.sendToAllPIs("/launchpad/pitch", new Object[] {pitchIndex});
			} else {
				//main grid
				int group = col / 2;
				int id = row - 1;
				if(group % 2 == 1) {
					id += 8;
				}
				//send ID to group
				piConnection.sendToPIGroup(group, "/launchpad/id", new Object[] {id});
			}
		}
	}
	
	
	
}