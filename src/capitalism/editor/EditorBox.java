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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.utils.WebStuff;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * This class creates and handles a box to be placed in an editor pane.
 */
public class EditorBox extends VBox {
	private static final Logger logger = LogManager.getLogger("EditorBox");
	private Label textArea = null;
	final WebView browser = new WebView();
	final WebEngine webEngine = browser.getEngine();
	private ScrollPane scrollPane;

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
	EditorBox(TableView<?> table, EditorDialogueBox dialogueBox, String helpFile, String userGuide) {
		setPrefWidth(Double.MAX_VALUE);
		HBox dialogueContainer = new HBox();

		// leave enough room for everything we might want to put in the dialogue box.
		dialogueContainer.setPrefHeight(10000); // weird: if we set to MAX_VALUE, it squeezes everything else out
		// dialogueContainer.setMaxHeight(Double.MAX_VALUE);
		// textArea.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(this, Priority.ALWAYS);
		textArea = new Label();
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setWrapText(true);
		HBox.setHgrow(textArea, Priority.ALWAYS);
		textArea.setFont(new Font(14));
		textArea.setText(userGuide);
		textArea.setTextAlignment(TextAlignment.CENTER);
		final String cssBackground = "-fx-background-color: LIGHTSTEELBLUE;\n"
				+ "-fx-border-color: silver;\n"
				+ "-fx-border-width: 3;\n"
				+ "-fx-border-radius: 10 10 10 10;\n" +
				"  -fx-background-radius: 10 10 10 10;";

		// setStyle(cssBackground);
		// final String cssTextArea = "-fx-background-color: #F4F4F4;\n"
		// + "-fx-border-color: silver;\n"
		// + "-fx-border-width: 1;\n";

		textArea.setStyle(cssBackground);
		textArea.setPadding(new Insets(5, 5, 5, 5));
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(0.5);
		dropShadow.setOffsetY(0.5);
		dropShadow.setColor(Color.CADETBLUE);
		textArea.setEffect(dropShadow);

		textArea.setTranslateX(-10);
		textArea.setTranslateY(10);

		webEngine.loadContent("<p>welcome</p><p>Help facility under development</p>");
		scrollPane = new ScrollPane();
		scrollPane.setContent(browser);
		scrollPane.setEffect(dropShadow);
		String content = WebStuff.getFile(helpFile);
		webEngine.loadContent(content);
		logger.debug("\nWEBCONTENT");
		System.out.print(textArea.getText());
		scrollPane.setMaxHeight(400);
		scrollPane.setMaxWidth(Double.MAX_VALUE);
		scrollPane.setTranslateX(-15);
		scrollPane.setTranslateY(10);
		// User can scroll by panning, only
		// and even that isn't implemented yet.
		// basically we used a scrollPane because we couldn't get the VBox to size properly
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		dialogueContainer.getChildren().addAll(dialogueBox, scrollPane /* ,textArea */);
		getChildren().addAll(table, dialogueContainer);
	}

	/**
	 * Hide the help box
	 */
	public void hideHelp() {
		scrollPane.setVisible(false);
	}

	/**
	 * Show the help box
	 */
	public void showHelp() {
		scrollPane.setVisible(true);
	}
}
