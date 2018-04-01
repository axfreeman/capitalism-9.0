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

import capitalism.model.Project;
import capitalism.reporting.Dialogues;
import capitalism.view.custom.ProjectCell;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class EditorProjectCombo extends ComboBox<Project> {
	private static final Logger logger = LogManager.getLogger(EditorProjectCombo.class);

	public EditorProjectCombo(ObservableList<Project> projects, String prompt) {
		super(projects);
		setPromptText(prompt);
		setWidth(200);
		setMinWidth(200);
		
		Callback<ListView<Project>, ListCell<Project>> callBack = new Callback<ListView<Project>, ListCell<Project>>() {
			@Override public ListCell<Project> call(ListView<Project> param) {
				return new ProjectCell();
			}
		};
		setCellFactory(callBack);

		valueProperty().addListener(new ChangeListener<Project>() {
			@Override public void changed(ObservableValue<? extends Project> observable, Project oldValue, Project newValue) {
				if (oldValue != null) {
					logger.debug("Editor chose project {}",oldValue.getDescription());
				}
				if (newValue != null) {
					logger.debug("Editor choice of project {}", newValue.getDescription());
					setPromptText(newValue.getDescription());
					Dialogues.info("Loading Project", String.format("Project %d",newValue.getProjectID()));
					EditorLoader.loadFromSimulation(newValue.getProjectID());
				}
			}
		});
	}

	public EditorProjectCombo(ObservableList<Project> items) {
		super(items);
	}
}
