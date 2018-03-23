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
import capitalism.view.command.AddRowCommand;
import capitalism.view.command.CreateCommand;
import capitalism.view.command.SaveEditorWindowCommand;
import capitalism.view.command.SimCommand;
import capitalism.view.custom.ImageButton;
import capitalism.view.custom.RadioButtonPair;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * The EditorControlBar contains the main editor controls, in a bar above the tables. There will
 * only ever be one of these so most variables and methods are static
 */

public class EditorControlBar extends HBox {
	@SuppressWarnings("unused")
	private final static Logger logger = LogManager.getLogger("EditorControlBar");

	private static Pane spacer = new Pane();
	private ImageButton simButton = new ImageButton(
			"start.png",
			"start.png",
			new SimCommand(),
			"Open the main Window and start the simulation",
			"Open the main window and start the simulation");
	private ImageButton createButton = new ImageButton(
			"create.png",
			"create.png",
			new CreateCommand(),
			"Create a new project",
			"Create a new project");
	private static ImageButton addNewRow = new ImageButton(
			"littlePlus.png",
			"littlePlus.png",
			new AddRowCommand(),
			"",
			"Add a new row to this table");
	private static RadioButtonPair stocksPair = new RadioButtonPair(
			"Required Stocks",
			"Actual Stocks",
			"Show the production or consumption stocks are required, given industrial output and the size of the social classes",
			"Show the actual production and consumption stocks owned by the industries and social classes");
	private static ImageButton saveButton = new ImageButton(
			"save2.png",
			"save2.png",
			new SaveEditorWindowCommand(),
			"Save the contents of the editor window to a file on your computer",
			"Save the contents of the editor window to a file on your computer");
	EventHandler<ActionEvent> btnSaveHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
		}
	};

	EditorControlBar() {
		setMaxWidth(Double.MAX_VALUE);
		spacer.setPrefHeight(50);
		spacer.setPrefWidth(50);
		setHgrow(spacer, Priority.ALWAYS);
		setPrefWidth(400);
		getChildren().addAll(createButton, saveButton, simButton, addNewRow, spacer, stocksPair);
	}

	public static boolean displayActuals() {
		return stocksPair.result();
	}
}
