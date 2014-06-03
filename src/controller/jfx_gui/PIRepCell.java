package controller.jfx_gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    public void updateItem(LocalPIRepresentation item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
        	//set up main panel
        	HBox hbox = new HBox();
            setGraphic(hbox);
        	//elements
        	Text name = new Text(item.hostname);
        	hbox.getChildren().add(name);
        	//
        	Button b = new Button("X");
        	hbox.getChildren().add(b);
        	//
        	CheckBox c = new CheckBox("Include in Send");
        	hbox.getChildren().add(c);
        	//
        	Slider s = new Slider(1, 0, 1);
        	s.setOrientation(Orientation.HORIZONTAL);
        	s.valueProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					// TODO Auto-generated method stub
					
				}
			});
        	hbox.getChildren().add(s);
        }
    }
	
}
