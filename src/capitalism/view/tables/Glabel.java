package capitalism.view.tables;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

/**
 * this class creates a control to display text, an optional graphic, and a number.
 */

public class Glabel extends HBox {

	// this displays a description of the number and an optional graphic
	Label description=new Label();

	// this displays the number itself
	Label numberLabel=new Label();

	public Glabel() {

		Pane pane=new Pane();

		numberLabel.setPrefHeight(30);
		numberLabel.setAlignment(Pos.CENTER_RIGHT);
		numberLabel.setContentDisplay(ContentDisplay.RIGHT);
		numberLabel.setPrefHeight(30);
		numberLabel.setPrefWidth(60);
		numberLabel.setText("0.00");
		getChildren().addAll(description,pane,numberLabel);
		
		setHgrow(pane, Priority.ALWAYS);
		setPadding(new Insets(3, 3, 3, 3));
		final String cssBackground = "-fx-background-color: antiquewhite;\n"
				+ "-fx-border-color: burlywood;\n"
				+ "-fx-border-width: 1;\n";
		setStyle(cssBackground);
		final String cssNumberLabel= "-fx-background-color: lightcyan;\n"
				+ "-fx-border-color: silver;\n"
				+ "-fx-border-width: 1;\n";
		numberLabel.setStyle(cssNumberLabel);
		numberLabel.setPadding(new Insets(2, 2, 2, 2));
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(1.0);
		dropShadow.setOffsetY(1.0);
		dropShadow.setColor(Color.CADETBLUE);
		numberLabel.setEffect(dropShadow);
	}

	protected void setDescription(String description) {
		this.description.setText(description);
	}
}
