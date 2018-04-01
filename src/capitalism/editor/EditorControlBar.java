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

import capitalism.editor.command.AddRowCommand;
import capitalism.editor.command.HelpCommand;
import capitalism.editor.command.SaveEditorWindowCommand;
import capitalism.editor.command.SimCommand;
import capitalism.model.Project;
import capitalism.view.command.CreateCommand;
import capitalism.view.custom.ImageButton;
import capitalism.view.custom.RadioButtonPair;
import javafx.collections.ObservableList;
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
	@SuppressWarnings("unused") private final static Logger logger = LogManager.getLogger("EditorControlBar");

	private static Pane spacer = new Pane();
	private static EditorProjectCombo projectCombo = null;
	private static ObservableList<Project> projects = Project.observableProjects();
	private Project currentProject;
	private String currentProjectDescription;

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
			"Required Stocks ",
			"Actual Stocks ",
			"Show the production or consumption stocks are required, given industrial output and the size of the social classes",
			"Show the actual production and consumption stocks owned by the industries and social classes");
	private static ImageButton saveButton = new ImageButton(
			"download.png",
			"download.png",
			new SaveEditorWindowCommand(),
			"Save the contents of the editor window to a file on your computer",
			"Save the contents of the editor window to a file on your computer");
	private static ImageButton helpButton = new ImageButton(
			"unhelp.png",
			"help.png",
			new HelpCommand(),
			"Display the help window",
			"Close the help window"
			);
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
		currentProject = Project.get(1);
		currentProjectDescription = currentProject.getDescription();
		projectCombo = new EditorProjectCombo(projects, currentProjectDescription);
		// use spacer if need be to place one or more controls on the right of the bar
		getChildren().addAll(projectCombo, stocksPair, spacer, createButton, saveButton, simButton, addNewRow,helpButton);
	}

	public static boolean displayActuals() {
		return stocksPair.result();
	}
}
