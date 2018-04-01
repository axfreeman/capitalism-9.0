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
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * This class creates and handles a box to be placed in an editor pane.
 */
public class EditorBox extends VBox {
	private static final Logger logger = LogManager.getLogger("EditorBox");

	final Browser browser = new Browser();

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
	EditorBox(TableView<?> table, EditorDialogueBox dialogueBox, String helpFileName, String userGuide) {
		setPrefWidth(Double.MAX_VALUE);
		HBox dialogueContainer = new HBox();
		String helpUrlString=Capitalism.getUserBasePath()+"help/"+helpFileName;
		logger.debug("Accessing help file at {}",helpUrlString);
		
		// leave enough room for everything we might want to put in the dialogue box.
		dialogueContainer.setPrefHeight(10000); // weird: if we set to MAX_VALUE, it squeezes everything else out
		VBox.setVgrow(this, Priority.ALWAYS);
		DropShadow dropShadow = new DropShadow();
		dropShadow.setOffsetX(3);
		dropShadow.setOffsetY(2);
		dropShadow.setColor(Color.GRAY);

		String roundCornerCss = 
				 "	    -fx-background-radius: 18 18 18 18;"
				+ "	    -fx-border-radius: 18 18 18 18;";

		browser.setPrefHeight(200);
		browser.setMaxHeight(450);
		browser.setPrefWidth(800);
		browser.load("file:/"+helpUrlString);
		browser.setStyle(roundCornerCss);
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
