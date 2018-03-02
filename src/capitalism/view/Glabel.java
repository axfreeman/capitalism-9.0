package capitalism.view;

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
	private Label numberLabel=new Label();

	public Glabel() {
		Pane pane=new Pane();
		getNumberLabel().setPrefHeight(30);
		getNumberLabel().setAlignment(Pos.CENTER_RIGHT);
		getNumberLabel().setContentDisplay(ContentDisplay.RIGHT);
		getNumberLabel().setPrefHeight(30);
		getNumberLabel().setPrefWidth(60);
		getNumberLabel().setText("0.00");
		getChildren().addAll(description,pane,getNumberLabel());
		
		setHgrow(pane, Priority.ALWAYS);
		setPadding(new Insets(3, 3, 3, 3));
		final String cssBackground = "-fx-background-color: #DDDDDD;\n"
				+ "-fx-border-color: silver;\n"
				+ "-fx-border-width: 1;\n";
		setStyle(cssBackground);
		final String cssNumberLabel= "-fx-background-color: #F4F4F4;\n"
				+ "-fx-border-color: silver;\n"
				+ "-fx-border-width: 1;\n";
		getNumberLabel().setStyle(cssNumberLabel);
		getNumberLabel().setPadding(new Insets(2, 2, 2, 2));
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(0.5);
		dropShadow.setOffsetY(0.5);
		dropShadow.setColor(Color.CADETBLUE);
		getNumberLabel().setEffect(dropShadow);
	}

	public void setDescription(String description) {
		this.description.setText(description);
	}

	public Label getNumberLabel() {
		return numberLabel;
	}

	public void setNumberLabel(Label numberLabel) {
		this.numberLabel = numberLabel;
	}
}
