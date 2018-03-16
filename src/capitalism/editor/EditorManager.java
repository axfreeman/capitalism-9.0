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

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.model.Commodity.FUNCTION;
import capitalism.model.Commodity.ORIGIN;
import capitalism.model.OneProject;
import capitalism.utils.Dialogues;
import capitalism.utils.Reporter;
import capitalism.utils.XMLStuff;
import capitalism.view.ViewManager;
import capitalism.view.custom.DisplayControlsBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class EditorManager {
	private final static Logger logger = LogManager.getLogger("EditorManager");

	private Stage editorStage = null;
	private Scene editorScene;
	private Editor editor = new Editor();

	private static boolean displayActuals= false;

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
		load();
		editorStage.showAndWait();
	}

	public static void load() {
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
		for (EditableSocialClass socialClass : Editor.getSocialClassData()) {
			for (EditableCommodity commodity : Editor.getCommodityData()) {
				if (commodity.getFunction().equals("Consumer Goods"))
					socialClass.addConsumptionStock(commodity.getName());
			}
		}

		// and create the columns for consumption stocks
		Editor.addSocialClassStockColumns();
		EditableSocialClass.loadAllStocksFromSimulation();

	}

	/**
	 * The EditorControlBar contains the main editor controls, in a bar above the tables
	 */
	
	public static class EditorControlBar extends HBox {
		private Button btnSave = new Button("Save Project");
		private Button btnNew = new Button("Create New Project");

		private static Pane spacer = new Pane();

		private RadioButton showDesiredStocksButton = new RadioButton("Required Stocks");
		private RadioButton showActualStocksButton = new RadioButton("Actual Stocks");
		private ToggleGroup stocksToggle = new ToggleGroup();

		EventHandler<ActionEvent> btnSaveHandler = new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent t) {
				File file=Dialogues.saveFileChooser("Where should this project be saved?");
				if (file == null) return;
				logger.debug("Saving new project to {}", file.getAbsolutePath());
				OneProject oneProject= Editor.wrappedOneProject();
				XMLStuff.exportToXML(oneProject,file);
			}
		};

		EventHandler<ActionEvent> btnNewHandler = new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent t) {
				createSkeletonProject();
			}
		};

		EditorControlBar() {
			setMaxWidth(Double.MAX_VALUE);

			spacer.setPrefHeight(50);
			spacer.setPrefWidth(50);
			setHgrow(spacer, Priority.ALWAYS);
			ViewManager.setTip(showDesiredStocksButton,
					"Show the production or consumption stocks are required, given industrial output and the size of the social classes");
			ViewManager.setTip(showActualStocksButton, "Show the actual production and consumption stocks owned by the industries and social classes");
			showDesiredStocksButton.setToggleGroup(stocksToggle);
			showActualStocksButton.setToggleGroup(stocksToggle);
			showDesiredStocksButton.setUserData("DESIRED");
			showActualStocksButton.setUserData("ACTUAL");
			showDesiredStocksButton.setSelected(true);
			stocksToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				@Override public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
					if (stocksToggle.getSelectedToggle() != null) {
						String selected = stocksToggle.getSelectedToggle().getUserData().toString();
						switch (selected) {
						case "DESIRED":
							displayActuals=false;
							break;
						case "ACTUAL":
							displayActuals=true;
							break;
						default:
							logger.error("Unknown radio button {} selected ", selected);
							break;
						}
						Editor.refresh();
					}
				}
			});
			btnNew.setOnAction(btnNewHandler);
			btnSave.setOnAction(btnSaveHandler);
			setPrefWidth(400);

			showDesiredStocksButton.setPrefHeight(30);
			showActualStocksButton.setPrefHeight(30);
			showDesiredStocksButton.setPadding(new Insets(2, 0, 0, 0));
			showActualStocksButton.setPadding(new Insets(2, 0, 0, 0));

			getChildren().add(btnNew);
			getChildren().add(btnSave);
			getChildren().add(spacer);
			getChildren().add(showDesiredStocksButton);
			getChildren().add(showActualStocksButton);
		}
	}

	/**
	 * Create a skeleton project, which contains the minimum necessary for a viable project
	 */
	public static void createSkeletonProject() {
		Reporter.report(logger, 0, "CREATING A PROJECT");
		// Clear the decks
		Editor.clearDecks();

		// Create the commodities money, necessities, labour power, and means of production
		Reporter.report(logger, 1, "Creating the basic commodities");
		EditableCommodity moneyCommodity=EditableCommodity.makeCommodity("Money",ORIGIN.MONEY,FUNCTION.MONEY);
		EditableCommodity necessityCommodity=EditableCommodity.makeCommodity("Necessities",ORIGIN.INDUSTRIALLY_PRODUCED,FUNCTION.CONSUMER_GOOD);
		EditableCommodity meandOfProductionCommodity=EditableCommodity.makeCommodity("Means of Production",ORIGIN.INDUSTRIALLY_PRODUCED,FUNCTION.PRODUCTIVE_INPUT);
		EditableCommodity labourPowerCommodity=EditableCommodity.makeCommodity("Labour Power",ORIGIN.SOCIALLY_PRODUCED,FUNCTION.PRODUCTIVE_INPUT);
		Editor.getCommodityData().addAll(moneyCommodity,necessityCommodity,meandOfProductionCommodity,labourPowerCommodity);
		
		// Create the social classes Capitalists, Workers and their stocks
		Reporter.report(logger, 1, "Creating the minimum social Classes");
		EditableSocialClass capitalists=EditableSocialClass.makeSocialClass("Capitalists",0);
		EditableSocialClass workers=EditableSocialClass.makeSocialClass("Workers",1);
		Editor.getSocialClassData().addAll(workers,capitalists);
		
		// Create the two industries means of production and consumption and their stocks
		Reporter.report(logger, 1, "Creating the minimum industries");
		EditableIndustry dI = EditableIndustry.makeIndustry("Department I","Means of production",0);
		EditableIndustry dII=EditableIndustry.makeIndustry("Departmment II","Necessities", 0);
		Editor.getIndustryData().addAll(dI,dII);
		// repopulate the display
		Editor.rebuildTables();
	}
	
	public static boolean displayActuals() {
		return displayActuals;
	}
}
