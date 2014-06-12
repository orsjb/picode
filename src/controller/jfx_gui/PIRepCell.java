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
import javafx.scene.text.Text;

public class PIRepCell extends ListCell<LocalPIRepresentation> {
	
	int count = 0;
	
	public PIRepCell() {
		setMinHeight(80);
	}
	
	@Override
    public void updateItem(final LocalPIRepresentation item, boolean empty) {
//        super.updateItem(item, empty);
		
		//Issue here is that gui needs to be attached to "item", then readded
		
//		System.out.println("updateItem() [" + this + "] " + count++ + " | (item = " + item + ")");
        
        if (item != null) {
        	
        	if(item.getGui() == null) {
        	
	        	//set up main panel
	        	
	        	HBox hbox = new HBox();
	        	//elements
	        	Text name = new Text(item.hostname);
	        	hbox.getChildren().add(name);
	        	//
	        	Button b = new Button("Reset");
	        	b.setOnAction(new EventHandler<ActionEvent>() {
	        	    @Override public void handle(ActionEvent e) {
	        	    	item.send("/PI/reset");
	        	    }
	        	});
	        	hbox.getChildren().add(b);
	        	//TODO 
	        	//
	        	for(int i = 0; i < 4; i++) {
	        		final int index = i;
		        	CheckBox c = new CheckBox();
		        	c.selectedProperty().addListener(new ChangeListener<Boolean>() {
		                public void changed(ObservableValue<? extends Boolean> ov,
		                        Boolean oldval, Boolean newval) {
		                            item.groups[index] = newval;
		                    }
		                });
		        	hbox.getChildren().add(c);
	        	}
	        	
	        	//
	        	Slider s = new Slider(0, 1, 0.5);
	        	s.setOrientation(Orientation.HORIZONTAL);
	        	s.valueProperty().addListener(new ChangeListener<Number>() {
	
					@Override
					public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
						item.send("/PI/gain", newval.floatValue(), 50f);
					}
					
				});
	        	hbox.getChildren().add(s);
	        	
	        	//a status string
	        	Text statusText = new Text("status unknown");
	        	hbox.getChildren().add(statusText);
	        	
	        	item.setGui(hbox);

        	}
        	setGraphic(item.getGui());
        	
        }

    }
	
	
	
}
