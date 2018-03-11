/*
 *  capitalism.view.editoreman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in thEditorManagerf this project
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

import capitalism.utils.Reporter;
import capitalism.view.command.CreateProjectCommand;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.ImageButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class EditorManager {
	private final static Logger logger = LogManager.getLogger("ViewManager");

	private Stage editorStage = null;
	private Scene editorScene;
	private Editor editor = new Editor();

	public EditorManager() {
		Reporter.report(logger, 0, "Editor Window Opened");
		editorStage = new Stage();
		editorStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				DisplayControlsBox.setEditorOpen(false);
			}
		});
		editorScene = new Scene(editor, 1000, 500);
		editorStage.setScene(editorScene);
		editorStage.showAndWait();
	}

	public static class EditorControlBar extends HBox {
		private static ImageButton createProjectButton = new ImageButton("littlePlus.png", null, new CreateProjectCommand(),
				"Create a new project", "Create a new project (under development)");
		private Button btnLoad = new Button("Load Existing Project");
		private Button btnSave = new Button("Save Project");
		private Button btnNew = new Button("Create New Project");

		EventHandler<ActionEvent> btnLoadHandler = new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent t) {

				// First find out which commodities we have
				Editor.setCommodityData(EditableCommodity.editableCommodities());
				
				// Next, find out which industries we have
				Editor.setIndustryData(EditableIndustry.editableIndustries());
				
				// Now add the productive stocks that these industries own
				for (EditableIndustry industry : Editor.getIndustryData()) {
					for (EditableCommodity commodity : Editor.getCommodityData()) {
						if (commodity.getFunction().equals("Productive Inputs"))
							industry.addProductiveStock(commodity.getName());
					}
				}
				// and create columns for the productive stocks
				Editor.addIndustryStockColumns();

				// Populate the EditableStocks from the simulation. 
				// The money and sales stocks were created by the EditableIndustry constructor
				// We just added the productive stocks
				EditableIndustry.loadAllStocksFromSimulation();
				
				Editor.setSocialClassData(EditableSocialClass.editableSocialClasses());
				// Now add the consumption stocks that these industries classes own
				for (EditableSocialClass socialClass: Editor.getSocialClassData()) {
					for (EditableCommodity commodity : Editor.getCommodityData()) {
						if (commodity.getFunction().equals("Consumer Goods"))
							socialClass.addConsumptionStock(commodity.getName());
					}
				}
				EditableSocialClass.loadAllStocksFromSimulation();
			}
		};

		EventHandler<ActionEvent> btnSaveHandler = new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent t) {
				// TODO stub
			}
		};

		EventHandler<ActionEvent> btnNewHandler = new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent t) {
				// TODO stub
			}
		};
		
		EditorControlBar() {
			btnLoad.setOnAction(btnLoadHandler);
			btnNew.setOnAction(btnNewHandler);
			btnSave.setOnAction(btnSaveHandler);
			setPrefWidth(400);
			getChildren().add(btnLoad);
			getChildren().add(btnNew);
			getChildren().add(btnSave);
			getChildren().add(createProjectButton);
		}

	}
}
