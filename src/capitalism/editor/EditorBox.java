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

import capitalism.Capitalism;
import capitalism.help.Browser;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * This class creates and handles a box to be placed in an editor pane.
 */
public class EditorBox extends VBox {
	private static final Logger logger = LogManager.getLogger("EditorBox");

	WebView browser;
	WebEngine webEngine;


	/**
	 * Create a box containing a table,a dialogueBox and a help browser.
	 * The browser is given an initial URL in the local file system.
	 * 
	 * @param table
	 *            the editable table
	 * @param dialogueBox
	 *            a dialogue box in which the user enters additional information
	 * @param helpFileName
	 *            the name of the helpFile, relative to its containing directory
	 */
	EditorBox(TableView<?> table, EditorDialogueBox dialogueBox, String helpFileName) {
		setPrefWidth(Double.MAX_VALUE);
		HBox dialogueContainer = new HBox();
		String helpUrlString = Capitalism.getUserBasePath() + "help/" + helpFileName;
		logger.debug("Accessing help file at {}", helpUrlString);
		
		// leave enough room for everything we might want to put in the dialogue box.
		dialogueContainer.setPrefHeight(10000); // weird: if we set to MAX_VALUE, it squeezes everything else out
		VBox.setVgrow(this, Priority.ALWAYS);
		browser = new WebView();
		webEngine= browser.getEngine();
		VBox browser2=new VBox();
		browser.setPrefHeight(200);
		browser.setMaxHeight(450);
		browser.setPrefWidth(800);
		browser.setStyle("-fx-background-color:RED");
		browser2.setPrefHeight(200);
		browser2.setMaxHeight(450);
		browser2.setPrefWidth(800);
		browser2.setStyle("-fx-background-color:RED");
		
		webEngine.load("file:/" + helpUrlString);
		dialogueContainer.getChildren().addAll(dialogueBox, browser);
		getChildren().addAll(table, dialogueContainer);
	}

	/**
	 * Hide the help box
	 */
	public void hideHelp() {
		browser.setVisible(false);
	}

	/**
	 * Show the help box
	 */
	public void showHelp() {
		browser.setVisible(true);
	}
}
