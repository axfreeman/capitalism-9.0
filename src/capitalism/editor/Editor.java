/*
 * Copyright (C) Alan Freeman 2017-2019
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.editor.model.EditableCommodity;
import capitalism.editor.model.EditableIndustry;
import capitalism.editor.model.EditableSocialClass;
import capitalism.editor.model.EditableCommodity.*;
import capitalism.editor.model.EditableIndustry.*;
import capitalism.editor.model.EditableSocialClass.*;
import capitalism.model.Commodity;
import capitalism.reporting.Dialogues;
import capitalism.view.custom.NumericField;
import capitalism.view.custom.RadioButtonPair;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.VBox;

/*
 * with acknowledgements to http://java-buddy.blogspot.ca/2012/04/javafx-2-editable-tableview.html
 */

public class Editor extends VBox {
	private static final Logger logger = LogManager.getLogger("Editor");

	private static EditorControlBar ecb = new EditorControlBar();

	private static TableView<EditableCommodity> commodityTable = new TableView<EditableCommodity>();
	private static TableView<EditableIndustry> industryTable = new TableView<EditableIndustry>();
	private static TableView<EditableSocialClass> socialClassTable = new TableView<EditableSocialClass>();
	private static TableColumn<EditableIndustry, String> industryInputsSuperColumn = new TableColumn<EditableIndustry, String>("Inputs");
	private static TableColumn<EditableSocialClass, String> socialClassConsumptionSuperColumn = new TableColumn<EditableSocialClass, String>("Consumer Goods");

	private static TabPane tabPane = null;
	private static Tab commodityTab = new Tab("Commodities");
	private static Tab industryTab = new Tab("Industries");
	private static Tab socialClassTab = new Tab("Classes");

	private static VBox commodityBox = null;
	private static VBox industryBox = null;
	private static VBox socialClassBox = null;

	private static TextField commodityField;
	private static TextField industryField;
	private static TextField industryCommodityField;
	private static NumericField industryOutputField;
	private static TextField socialClassField;
	private static NumericField socialClassSizeField;

	private static RadioButtonPair commodityFunction;
	private static RadioButtonPair commodityOrigin;

	private static EditorDialogueBox industryDialogueBox;
	private static EditorDialogueBox socialClassDialogueBox;
	private static EditorDialogueBox commodityDialogueBox;

	// the next three are a complete botch to get things going while we figure
	// out how to get inherited disable working
	public static ArrayList<Node> industryDisableList = new ArrayList<Node>();
	public static ArrayList<Node> commodityDisableList = new ArrayList<Node>();
	public static ArrayList<Node> socialClassDisableList = new ArrayList<Node>();

	public enum TAB_SELECTION {
		COMMODITY, INDUSTRY, SOCIALCLASS, UNKNOWN;
	}

	/**
	 * Tells which tab is selected.
	 * 
	 * @return an enum representing which tab has been selected (COMMODITY, INDUSTRY, SOCIALCLASS)
	 */
	public static TAB_SELECTION selectedTab() {
		if (commodityTab.isSelected())
			return TAB_SELECTION.COMMODITY;
		if (industryTab.isSelected())
			return TAB_SELECTION.INDUSTRY;
		if (socialClassTab.isSelected())
			return TAB_SELECTION.SOCIALCLASS;
		return TAB_SELECTION.UNKNOWN;
	}

	/**
	 * Construct the singleton editor window. This will be displayed modally (using {@code showandwait()}) by {@link EditorManager}
	 */
	public Editor() {
		buildDialogues();
		// box for the commodity table
		commodityBox = new EditorBox(commodityTable, commodityDialogueBox,Explanations.commodityText);
		industryBox = new EditorBox(industryTable, industryDialogueBox,Explanations.industryText);
		socialClassBox = new EditorBox(socialClassTable, socialClassDialogueBox,Explanations.socialClassText);
		
		// the tabbed pane
		tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		tabPane.getTabs().addAll(commodityTab, industryTab, socialClassTab);

		commodityTab.setContent(commodityBox);
		industryTab.setContent(industryBox);
		socialClassTab.setContent(socialClassBox);

		getChildren().addAll(ecb, tabPane);

		// TODO get this working using disable inheritance.
		Collections.addAll(industryDisableList,commodityBox,socialClassBox,industryTable);
		Collections.addAll(socialClassDisableList,commodityBox,industryBox,socialClassTable);
		Collections.addAll(commodityDisableList,industryBox,socialClassBox,commodityTable);

		industryDialogueBox.hideDialogue();
		socialClassDialogueBox.hideDialogue();
		commodityDialogueBox.hideDialogue();
	}

	/**
	 * Build the dialogue boxes
	 */
	public static void buildDialogues() {
		// commodityBox associated dialogue box
		List<Node> commodityList = new ArrayList<Node>();
		commodityField = new TextField();
		commodityField.setPromptText("Name of the new commodity");
		commodityFunction = new RadioButtonPair("Production", "Consumption", "Used as an input to production", "Consumed by a social class");
		commodityOrigin = new RadioButtonPair("Industrial", "Social", "Produced by an industry", "Produced by a class");
		Collections.addAll(commodityList, commodityField, commodityFunction, commodityOrigin);
		commodityDialogueBox = new EditorDialogueBox(commodityList, "Enter the name of the new commodity", commoditySaveHandler, commodityDisableList);

		// industryBox associated dialogue box
		List<Node> industryList = new ArrayList<Node>();
		industryField = new TextField();
		industryField.setPromptText("Name of the new industry");
		industryCommodityField = new TextField();
		industryCommodityField.setPromptText("Name of the commodity that this industry produces");
		industryOutputField = new NumericField();
		industryOutputField.setPromptText("Suggested output");
		Collections.addAll(industryList, industryField, industryCommodityField, industryOutputField);
		industryDialogueBox = new EditorDialogueBox(industryList, "Enter the name of the new industry", industrySaveHandler, industryDisableList);

		// socialClassBox associated dialogue box
		// get the name of the social class and its participation ratio
		List<Node> socialClassList = new ArrayList<Node>();
		socialClassField = new TextField();
		socialClassField.setPromptText("Name of the new social Class");
		socialClassSizeField = new NumericField();
		socialClassSizeField.setPromptText("Initial size of this class");
		Collections.addAll(socialClassList, socialClassField, socialClassSizeField);
		socialClassDialogueBox = new EditorDialogueBox(socialClassList, "Enter the name of the new social class", socialClassSaveHandler,
				socialClassDisableList);
	}

	/**
	 * create the static columns for the Commodity table
	 */
	private static void makeCommodityTable() {
		commodityTable.getColumns().clear();
		commodityTable.setEditable(true);
		commodityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn(EC_ATTRIBUTE.NAME));
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn(EC_ATTRIBUTE.FUNCTION));
		commodityTable.getColumns().add(EditableCommodity.makeStringColumn(EC_ATTRIBUTE.ORIGIN));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn(EC_ATTRIBUTE.UNIT_VALUE));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn(EC_ATTRIBUTE.UNIT_PRICE));
		commodityTable.getColumns().add(EditableCommodity.makeDoubleColumn(EC_ATTRIBUTE.TURNOVER));
		commodityTable.setItems(EditorLoader.commodityData);
	}

	/**
	 * Create the static columns for the Industry table
	 */
	private static void makeIndustryTable() {
		industryTable.getColumns().clear();
		industryTable.setEditable(true);
		industryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		industryTable.getColumns().add(EditableIndustry.makeStringColumn(EI_ATTRIBUTE.NAME));
		industryTable.getColumns().add(EditableIndustry.makeStringColumn(EI_ATTRIBUTE.COMMODITY_NAME));

		industryInputsSuperColumn = new TableColumn<EditableIndustry, String>("Inputs");
		industryInputsSuperColumn.setMaxWidth(Double.MAX_VALUE);
		industryTable.getColumns().add(industryInputsSuperColumn);

		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn(EI_ATTRIBUTE.OUTPUT));
		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn(EI_ATTRIBUTE.SALES));
		industryTable.getColumns().add(EditableIndustry.makeDoubleColumn(EI_ATTRIBUTE.MONEY));
		industryTable.setItems(EditorLoader.industryData);
	}

	/**
	 * Create the static columns for the Social Class table
	 */
	private static void makeSocialClassTable() {
		socialClassTable.getColumns().clear();
		socialClassTable.setEditable(true);
		socialClassTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		socialClassTable.getColumns().add(EditableSocialClass.makeStringColumn(ESC_ATTRIBUTE.NAME));

		socialClassConsumptionSuperColumn = new TableColumn<EditableSocialClass, String>("Consumer Goods");
		socialClassConsumptionSuperColumn.setMaxWidth(Double.MAX_VALUE);
		socialClassTable.getColumns().add(socialClassConsumptionSuperColumn);

		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.SIZE));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.PR));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.REVENUE));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.SALES));
		socialClassTable.getColumns().add(EditableSocialClass.makeDoubleColumn(ESC_ATTRIBUTE.MONEY));
		socialClassTable.setItems(EditorLoader.socialClassData);
	}

	/**
	 * Add one column for each productive stock in the industry Table.
	 * public because it is invoked post-hoc by the Editor Manager
	 * TODO do something more elegant
	 */
	public static void addIndustryStockColumns() {
		for (EditableCommodity commodity : EditorLoader.commodityData) {
			logger.debug("Adding columns for industries: trying {}", commodity.getName());
			if (commodity.getFunction().equals(Commodity.FUNCTION.PRODUCTIVE_INPUT.text())) {
				logger.debug("Adding {}", commodity.getName());
				industryInputsSuperColumn.getColumns().add(EditableIndustry.makeStockColumn(commodity.getName()));
			}
		}
	}

	/**
	 * Add one column for each consumption stock in the Social Class table.
	 */
	private static void addSocialClassStockColumns() {
		for (EditableCommodity commodity : EditorLoader.commodityData) {
			logger.debug("Adding columns for consumption goods: trying {}", commodity.getName());
			if (commodity.getFunction().equals(Commodity.FUNCTION.CONSUMER_GOOD.text())) {
				logger.debug("Adding {}", commodity.getName());
				socialClassConsumptionSuperColumn.getColumns().add(EditableSocialClass.makeStockColumn(commodity.getName()));
			}
		}
	}

	/**
	 * Remake the tables. This has to be done whenever the stock columns change, because the
	 * commodities they represent have new names. There may be a better way but this is
	 * the simplest and I don't think it's expensive because there are no database operations involved.
	 */
	public static void makeAllTables() {
		commodityTable.getColumns().clear();
		industryTable.getColumns().clear();
		socialClassTable.getColumns().clear();
		makeCommodityTable();
		makeIndustryTable();
		makeSocialClassTable();
		
		// Create columns for the productive stocks
		addIndustryStockColumns();
		
		// Create the columns for consumption stocks
		addSocialClassStockColumns();
		setTableHeights();
	}

	/**
	 * little helper method
	 */
	public static void heightSniffer() {
		logger.debug("SNIFFING THE DIMENSIONS OF THE CONTROLS");
		double actualHeight = commodityBox.getHeight();
		double prefHeight=commodityBox.getPrefHeight();
		double minHeight=commodityBox.getMinHeight();
		double maxHeight=commodityBox.getMaxHeight();
		logger.debug("Heights: actual {}, preferred {}, min {}, max{}",actualHeight,prefHeight,minHeight,maxHeight);
	}
	
	
	/**
	 * Set the table heights to accomodate the number of rows
	 * TODO retrieve the heights dynamically
	 */
	public static void setTableHeights() {
		heightSniffer();
		setOneTableHeight(commodityTable, 1);
		setOneTableHeight(industryTable, 2);
		setOneTableHeight(socialClassTable, 2);
	}
	
	public static void  setOneTableHeight(TableView<?> table,int increment) {
		int rowCount = table.getItems().size() + increment;
		double prefHeight = Math.min(rowCount * 25, 500) + 3;
		table.setFixedCellSize(25);
		logger.debug("Table has {} rows, 500 is allocated for the pane including the table, whose height will be set to {}", rowCount, prefHeight);
		table.setMinHeight(USE_PREF_SIZE);
		table.setPrefHeight(prefHeight);
	}

	/**
	 * Refresh the tables by requesting a reload of their data. This is called when there is
	 * a change to an item of data within a table (for example as a result of an edit)
	 * but the structure of the table has not changed.
	 * TODO check empirically if this is really needed because these are observables
	 * so in principle refresh should be automatic
	 */
	public static void refresh() {
		industryTable.refresh();
		socialClassTable.refresh();
		commodityTable.refresh();
	}

	/**
	 * Open the commodity dialogue box so the user can add a commodity. The user response is handled by
	 * {@link Editor#commoditySaveHandler}
	 */
	public static void addCommodity() {
		commodityDialogueBox.showDialogue();
	}

	/**
	 * Create a new commodity.
	 * Get the name of the commodity, its origin and its function from the {@link Editor#commodityDialogueBox}.
	 * Validate that no other commodity has the same name. If productive, iterate adding stocks of it to all industries.
	 * If consumption, iterate adding stocks to all social classes.
	 * Create at least one industry that makes this commodity and report on this fact.
	 */
	private static EventHandler<ActionEvent> commoditySaveHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			// Check that there is a name for the commodity
			String commodityText = commodityField.getText();
			logger.debug("User entered a commodity called [{}]", commodityText);
			if (((commodityText == null)) || (commodityText.equals(""))) {
				commodityDialogueBox.warn("Commodity name cannot be blank");
				return;
			}
			if (isCommodity(commodityText)) {
				commodityDialogueBox.warn("A commodity with this name already exists");
				return;
			}
			Commodity.ORIGIN origin = commodityOrigin.result() ? Commodity.ORIGIN.INDUSTRIALLY_PRODUCED : Commodity.ORIGIN.SOCIALLY_PRODUCED;
			Commodity.FUNCTION function = commodityFunction.result() ? Commodity.FUNCTION.PRODUCTIVE_INPUT : Commodity.FUNCTION.CONSUMER_GOOD;
			logger.debug("Origin is {} and function is {}", origin, function);
			EditableCommodity newCommodity = EditableCommodity.makeCommodity(commodityText, origin, function);
			EditorLoader.commodityData.add(newCommodity);
			// Now we have to add the relevant stock items to the data model
			// case 1: it is an industrially-produced input. So we need an input stock
			// case 2: it is a socially-produced input. Same as the above
			// case 3: it is an industrially-produced consumption good: So we need a consumption stock
			// case 4: it is a socially-produced consumption good. No reason why not but we haven't got there yet.
			if (function == Commodity.FUNCTION.PRODUCTIVE_INPUT) {
				for (EditableIndustry ind : EditorLoader.industryData) {

					ind.addProductiveStock(commodityText);
				}
			} else {
				if (function == Commodity.FUNCTION.CONSUMER_GOOD) {
					if (origin == Commodity.ORIGIN.INDUSTRIALLY_PRODUCED) {
						for (EditableSocialClass sc : EditorLoader.socialClassData) {
							sc.addConsumptionStock(commodityText);
						}
					} else {
						Dialogues.alert(logger, "Sorry, we haven't implemented this yet. We should. Perhaps you would like to?");
					}
				}
			}
			makeAllTables(); // because the extra columnns need to be constructed
			commodityDialogueBox.hideDialogue();
		}
	};

	/**
	 * Open the industry dialogue box so the user can add an industry. The user response is handled by
	 * {@link Editor#industrySaveHandler}
	 */
	public static void addIndustry() {
		industryDialogueBox.showDialogue();
	}

	private static EventHandler<ActionEvent> industrySaveHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			String industryText = industryField.getText();
			String industryCommodityText = industryCommodityField.getText();
			// Check that there is a name for the industry

			if (industryText == null || industryText.equals("")) {
				industryDialogueBox.warn("Industry name cannot be blank");
				return;
			}
			if (isIndustry(industryText)) {
				industryDialogueBox.warn("An industry with this name already exists");
			}

			if (industryText == null || industryCommodityText.equals("")) {
				industryDialogueBox.warn("Commodity name cannot be blank");
				return;
			}
			if (!isCommodity(industryCommodityText)) {
				industryDialogueBox.warn("Commodity does not exist");
				return;
			}
			double industryOutput;
			try {
				industryOutput = Double.parseDouble(industryOutputField.getText());
			} catch (Exception e) {
				Dialogues.info("Number Required", "You should enter a number for the output of this industry, even if it is zero");
				return;
			}

			logger.debug("User entered an industry called [{}]", industryText);
			EditableIndustry newIndustry = EditableIndustry.makeIndustry(industryText, industryCommodityText, industryOutput);
			EditorLoader.industryData.add(newIndustry);
			// Now we have to add the relevant stock items to the data model
			for (EditableCommodity c : EditorLoader.commodityData) {
				// Is it an intergalactic thunderbanana?
				if (c.getFunction().equals(Commodity.FUNCTION.PRODUCTIVE_INPUT.text())) {
					// Yes, it most certainly is an intergalactic thunderbanana?
					newIndustry.addProductiveStock(c.getName());
				}
			}
			makeAllTables(); // because the extra columnns need to be constructed
			industryDialogueBox.hideDialogue();
		}
	};

	/**
	 * Open the socialClass dialogue box so the user can add a socialClass. The user response is handled by
	 * {link Editor#socialClassSaveHandler}
	 */
	public static void addSocialClass() {
		socialClassDialogueBox.showDialogue();
	}

	/**
	 * Using the input from {@link Editor#socialClassDialogueBox}}, constructs a new
	 * social class in the {@link Editor#makeSocialClassTable()}.
	 */
	private static EventHandler<ActionEvent> socialClassSaveHandler = new EventHandler<ActionEvent>() {
		@Override public void handle(ActionEvent t) {
			// Check that there is a name for the socialClass
			if (socialClassField.getText().equals("")) {
				socialClassDialogueBox.warn("The social class name cannot be blank");
				return;
			}
			if (isSocialClass(socialClassField.getText())) {
				socialClassDialogueBox.warn("A social class with this name already exists");
			}
			double socialClassSize;
			try {
				socialClassSize=Double.parseDouble(socialClassSizeField.getText());
			}catch (Exception e) {
				Dialogues.info("Number Required", "You should enter a number for the size of this class, even if it is zero");
				return;
			}
			EditableSocialClass newsocialClass = EditableSocialClass.makeSocialClass(
					socialClassField.getText(),socialClassSize, 0);
			EditorLoader.socialClassData.add(newsocialClass);
			socialClassTable.refresh();
			socialClassDialogueBox.hideDialogue();
			makeAllTables();
		}
	};

	/**
	 * Disable or enable the controls, tabs and tables- used when a dialogueBox is open, to give the effect
	 * of a modal dialogue without the pother and complexity of a new modal window.
	 * TODO work out how to do this using inheritance, selectively
	 * TODO at present just for the industry pane
	 * 
	 * @param disabled
	 *            true if the controls are to be disabled, false if they should be enabled
	 */
	public static void setControlsDisabled(boolean disabled) {
		ecb.setDisable(disabled);
	}

	/**
	 * Checks whether a commodity already exists
	 * 
	 * @param commodityName
	 *            the name of the commodity to check
	 * @return true if commodityName is in the current list of commodities, false otherwise
	 */
	public static boolean isCommodity(String commodityName) {
		for (EditableCommodity e : EditorLoader.commodityData) {
			if (e.getName().equals(commodityName))
				return true;
		}
		return false;
	}

	/**
	 * Checks whether an industry already exists
	 * 
	 * @param industryName
	 *            the name of the industry to check
	 * @return true if industryName is in the current list of industries, false otherwise
	 */
	public static boolean isIndustry(String industryName) {
		for (EditableIndustry i : EditorLoader.industryData) {
			if (i.getName().equals(industryName))
				return true;
		}
		return false;
	}

	/**
	 * Check whether a social Class already exists
	 * 
	 * @param socialClassName
	 *            the name of the social class to check
	 * @return true if socialClass is in the current list of socialClasses, false otherwise
	 */
	public static boolean isSocialClass(String socialClassName) {
		for (EditableSocialClass s : EditorLoader.socialClassData) {
			if (s.getName().equals(socialClassName))
				return true;
		}
		return false;
	}

	/**
	 * @return the commodityData as a printable string
	 */
	public String commodityDataAsString() {
		StringBuilder sb = new StringBuilder();
		for (EditableCommodity e : EditorLoader.commodityData) {
			sb.append(e.toString());
		}
		return sb.toString();
	}
}