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

import capitalism.view.command.CreateProjectCommand;
import capitalism.view.custom.ImageButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class EditorControlBar extends HBox {
	private static ImageButton createProjectButton = new ImageButton("littlePlus.png", null, new CreateProjectCommand(),
			"Create a new project", "Create a new project (under development)");
	private Button btnNew = new Button("New Record");

	EventHandler<ActionEvent> btnNewHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			Editor.setCommodityData(EditableCommodity.editableCommodities(1, 1));
			Editor.setIndustryData(EditableIndustry.editableIndustries(1, 1));
			Editor.setSocialClassData(EditableSocialClass.editableSocialClasses(1, 1));
			for (EditableIndustry industry : Editor.getIndustryData()) {
				for (EditableCommodity commodity : Editor.getCommodityData()) {
//					industry.addProductiveStock(commodity.getName());
					if (commodity.getFunction().equals("Productive Inputs"))
						industry.addProductiveStock(commodity.getName());
				}
			}
			Editor.addIndustryStockColumns();
		}
	};

	EditorControlBar() {
		btnNew.setOnAction(btnNewHandler);
		setPrefWidth(400);
		getChildren().add(btnNew);
		getChildren().add(createProjectButton);
	}

}
