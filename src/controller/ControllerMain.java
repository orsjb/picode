package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
import controller.network.SendToPI;
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
	String currentPIPO = "";
	

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
    	//set the colors
    	for(int i = 0; i < 8; i++) {
    		launchpad.setButtonColour(0, 0, i, LaunchPad.HIGH_AMBER);
    		launchpad.setButtonColour(0, i+1, 8, LaunchPad.HIGH_AMBER);
    		for(int j = 0; j < 8; j++) {
    			if(i == 0 && j % 2 == 0) {
    				launchpad.setButtonColour(0, i+1, j, LaunchPad.MEDIUM_RED);
    			} else {
    				launchpad.setButtonColour(0, i+1, j, LaunchPad.HIGH_GREEN);
    			}
    		}
    	}
    	launchpad.show();
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
       	//populate combobox with list of compositions
       	List<String> compositionFileNames = new ArrayList<String>();
       	Queue<File> dirs = new LinkedList<File>();
       	dirs.add(new File("./bin/compositions"));
       	while (!dirs.isEmpty()) {
       	  for (File f : dirs.poll().listFiles()) {
       	    if (f.isDirectory()) {
       	      dirs.add(f);
       	    } else if (f.isFile()) {
       	    	String path = f.getPath();
       	    	path = path.substring(6, path.length() - 6);
       	    	if(!path.contains("$")) {
           	    	System.out.println(path);
           	    	compositionFileNames.add(path);
       	    	}
       	    }
       	  }
       	}
       	ComboBox<String> menu = new ComboBox<String>();
       	for(final String compositionFileName : compositionFileNames) {
	       	menu.getItems().add(compositionFileName);
       	}

       	menu.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
       		@Override
       		public void changed(ObservableValue<? extends String> arg0, String arg1, final String arg2) {
       			if(arg2 != null) {
       				currentPIPO = arg2;
       			}
       		}
       	});

       	HBox hbox = new HBox();
       	topBox.getChildren().add(hbox);
       	hbox.getChildren().add(menu);
       	Button sendCode = new Button(">>");
       	sendCode.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
   				try {
   					SendToPI.send(currentPIPO, piConnection.getPIHostnames());
   				} catch (Exception ex) {
   					ex.printStackTrace();
   				}
			}
		});
       	hbox.getChildren().add(sendCode);
       	//set up the scene
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
//		System.out.println("Launchpad: " + parent + " " + launchPadID + " " + row + " " + col + " " + push);
		piConnection.sendToAllPIs("/launchpad", new Object[] {row, col, push?1:0});
	}
	
	
	
}