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
import java.io.File;
import capitalism.model.OneProject;
import capitalism.utils.Dialogues;
import capitalism.utils.XMLStuff;
import capitalism.view.command.AddRowCommand;
import capitalism.view.custom.ImageButton;
import capitalism.view.custom.RadioButtonPair;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * The EditorControlBar contains the main editor controls, in a bar above the tables. There will
 * only ever be one of these so most variables and methods are static
 */

public  class EditorControlBar extends HBox {
	private final static Logger logger = LogManager.getLogger("EditorControlBar");

	private Button btnSave = new Button("Save Project");
	private Button btnNew = new Button("Create New Project");

	/**
	 * This button allows the user to add a new row by invoking {@link AddRowCommand#execute(ImageButton)}
	 */
	private static ImageButton addNewRow = new ImageButton("littlePlus.png", "littlePlus.png", new AddRowCommand(), "Add a new row to this table", "");
	private static Pane spacer = new Pane();
	private static RadioButtonPair stocksPair = new RadioButtonPair(
			"Required Stocks",
			"Actual Stocks",
			"Show the production or consumption stocks are required, given industrial output and the size of the social classes",
			"Show the actual production and consumption stocks owned by the industries and social classes");
	EventHandler<ActionEvent> btnSaveHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			File file = Dialogues.saveFileChooser("Where should this project be saved?");
			if (file == null)
				return;
			logger.debug("Saving new project to {}", file.getAbsolutePath());
			OneProject oneProject = Editor.wrappedOneProject();
			XMLStuff.exportToXML(oneProject, file);
		}
	};

	EventHandler<ActionEvent> btnNewHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			Editor.createSkeletonProject();
		}
	};

	EditorControlBar() {
		setMaxWidth(Double.MAX_VALUE);
		spacer.setPrefHeight(50);
		spacer.setPrefWidth(50);
		setHgrow(spacer, Priority.ALWAYS);
		btnNew.setOnAction(btnNewHandler);
		btnSave.setOnAction(btnSaveHandler);
		setPrefWidth(400);
		getChildren().add(btnNew);
		getChildren().add(btnSave);
		getChildren().add(addNewRow);
		getChildren().add(spacer);
		getChildren().add(stocksPair);
	}
	
	public static boolean displayActuals() {
		return stocksPair.result();
	}

}

