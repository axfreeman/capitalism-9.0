package rd.dev.simulation.custom;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * this class creates a control to display text, an optional graphic, and a number.
 */

public class Glabel extends HBox {

	@FXML Label description;// this text displays a description of the number and an optional graphic
	@FXML Label numberLabel; // this text displays the number itself


	public Glabel() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Glabel_layout.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
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
