package controller.jfx_gui;

import controller.network.LocalPIRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PIRepCell extends ListCell<LocalPIRepresentation> {
	
	int count = 0;
	
	public PIRepCell() {
		setMinHeight(80);
	}
	
	@Override
    public void updateItem(final LocalPIRepresentation item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
		//gui needs to be attached to "item", can't rely on PIRepCell to bind to item
        if (item != null) {
        	if(item.getGui() == null) {
        		//set up main panel
	        	HBox hbox = new HBox();
	        	hbox.setSpacing(5);
	        	//elements
	        	VBox txtvbox = new VBox();
	        	hbox.getChildren().add(txtvbox);
	        	//name of the PI
	        	Text name = new Text(item.hostname);
	        	name.setUnderline(true);
	        	txtvbox.getChildren().add(name);
	        	//a status string
	        	Text statusText = new Text("status unknown");
	        	txtvbox.getChildren().add(statusText);
	        	//reset button
	        	Button b = new Button("Reset");
	        	b.setOnAction(new EventHandler<ActionEvent>() {
	        	    @Override public void handle(ActionEvent e) {
	        	    	item.send("/PI/reset");
	        	    }
	        	});
	        	txtvbox.getChildren().add(b);
	        	//group allocations
	        	VBox groupsVbox = new VBox();
	        	hbox.getChildren().add(groupsVbox);
	        	groupsVbox.getChildren().add(new Text("G#"));
	        	for(int i = 0; i < 4; i++) {
	        		final int index = i;
		        	CheckBox c = new CheckBox();
		        	c.selectedProperty().addListener(new ChangeListener<Boolean>() {
		                public void changed(ObservableValue<? extends Boolean> ov,
		                        Boolean oldval, Boolean newval) {
		                            item.groups[index] = newval;
		                    }
		                });
		        	groupsVbox.getChildren().add(c);
	        	}
	        	
	        	//
	        	Slider s = new Slider(0, 2, 1);
	        	s.setOrientation(Orientation.VERTICAL);
	        	s.valueProperty().addListener(new ChangeListener<Number>() {
	
					@Override
					public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
						item.send("/PI/gain", newval.floatValue(), 50f);
					}
					
				});
	        	hbox.getChildren().add(s);
	        	
	        	item.setGui(hbox);

        	}
        	setGraphic(item.getGui());
        	
        }

    }
	
	
	
}
