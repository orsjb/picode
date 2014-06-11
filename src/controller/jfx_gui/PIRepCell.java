package controller.jfx_gui;

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
import controller.network.LocalPIRepresentation;

public class PIRepCell extends ListCell<LocalPIRepresentation> {
	
	public PIRepCell() {
		setMinHeight(80);
	}
	
	@Override
    public void updateItem(final LocalPIRepresentation item, boolean empty) {
//        super.updateItem(item, empty);
		System.out.println("updateItem() " + Math.random());
              
        if (item != null) {
        	//set up main panel
        	HBox hbox = new HBox();
            setGraphic(hbox);
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
        	Slider s = new Slider(1, 0, 1);
        	s.setOrientation(Orientation.HORIZONTAL);
        	s.valueProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
					item.send("/PI/gain", (Float)newval, 100);
				}
				
			});
        	hbox.getChildren().add(s);
        	
        	//a status string
        	Text statusText = new Text("status unknown");
        	hbox.getChildren().add(statusText);
        	
        }
    }
	
	
	
}
