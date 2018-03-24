/*
 *  Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
 *
 *   Capsim is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
 */
package capitalism.editor;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * This class creates and handles a box to be placed in an editor pane.
 */
public class EditorBox extends VBox {

	/**
	 * Create a box containing a table and a dialogueBox.
	 * Create a textArea to display the 'userGuide' information
	 * 
	 * @param table
	 *            the editable table
	 * @param dialogueBox
	 *            a dialogue box in which the user enters additional information
	 * @param userGuide
	 *            a text explaining what the user can do in this tab
	 */

	EditorBox(TableView<?> table, EditorDialogueBox dialogueBox, String userGuide) {
		setPrefWidth(Double.MAX_VALUE);
		HBox dialogueContainer = new HBox();

		// leave enough room for everything we might want to put in the dialogue box.
		dialogueContainer.setPrefHeight(10000); // weird: if we set to MAX_VALUE, it squeezes everything else out
		//dialogueContainer.setMaxHeight(Double.MAX_VALUE);
		// textArea.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(this, Priority.ALWAYS);
		Label textArea = new Label();
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setWrapText(true);
		HBox.setHgrow(textArea, Priority.ALWAYS);
		textArea.setFont(new Font(14));
		textArea.setText(userGuide);
		textArea.setTextAlignment(TextAlignment.CENTER);
		final String cssBackground = "-fx-background-color: LIGHTSTEELBLUE;\n"
				+ "-fx-border-color: silver;\n"
				+ "-fx-border-width: 3;\n";
		// setStyle(cssBackground);
		final String cssTextArea = "-fx-background-color: #F4F4F4;\n"
				+ "-fx-border-color: silver;\n"
				+ "-fx-border-width: 1;\n";
		textArea.setStyle(cssBackground);
		textArea.setPadding(new Insets(5, 5, 5, 5));
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(0.5);
		dropShadow.setOffsetY(0.5);
		dropShadow.setColor(Color.CADETBLUE);
		textArea.setEffect(dropShadow);

		dialogueContainer.getChildren().addAll(dialogueBox, textArea);
		getChildren().addAll(table, dialogueContainer);
	}
}
