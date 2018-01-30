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

package rd.dev.simulation.view;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import rd.dev.simulation.Capitalism;
import rd.dev.simulation.Simulation;
import rd.dev.simulation.custom.ActionButtonsBox;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.custom.ProjectCombo;
import rd.dev.simulation.custom.SwitchableGraphicsGrid;
import rd.dev.simulation.custom.TabbedTableViewer;
import rd.dev.simulation.custom.TimeStampView;
import rd.dev.simulation.custom.TimeStampViewItem;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.datamanagement.ObservableListProvider;
import rd.dev.simulation.datamanagement.SelectionsProvider;
import rd.dev.simulation.model.Stock;
import rd.dev.simulation.model.Global;
import rd.dev.simulation.model.Project;
import rd.dev.simulation.model.TimeStamp;
import rd.dev.simulation.utils.Dialogues;
import rd.dev.simulation.utils.Reporter;

public class ViewManager {
	final Logger logger = LogManager.getLogger("ViewManager");

	// Shorthand for the two most-used managers used for data services

	private Simulation sm;
	private ObservableListProvider ol;

	// general display parameters

	public static Rectangle2D screenBounds = Screen.getPrimary().getBounds();
	public static String largeNumbersFormatString = "%1$,.0f";	// Formats the display of large floating point numbers
	public static String smallNumbersFormatString = "%1$.2f";   // Formats the display of small floating point numbers
	public static ContentDisplay graphicsState = ContentDisplay.TEXT_ONLY; // Whether to display graphics, text, or both

	// This enum and the following variables determine whether values, and prices, are displayed in units of money or time
	// They control the 'hints' colouring of the columns and the symbols (eg '$', '#') placed before price and value magnitudes in the tables display

	public enum DisplayAsExpression {
		MONEY, TIME;
	}

	public static DisplayAsExpression valuesExpressionDisplay = DisplayAsExpression.MONEY;
	public static DisplayAsExpression pricesExpressionDisplay = DisplayAsExpression.MONEY;
	public static boolean displayHints = false;

	public static String moneyExpressionSymbol = "$";
	public static String quantityExpressionSymbol = "#";

	public static String pricesExpressionSymbol = moneyExpressionSymbol;
	public static String valuesExpressionSymbol = moneyExpressionSymbol;

	private static ActionButtonsBox actionButtonsBox;

	// this is the right-hand Vbox, the container within which we put the button bar, the grid pane, and the tabbed table viewer
	@FXML VBox simulationResultsPane;

	// this is the root of the timeStamp treeView
	private TimeStampView tree;

	// this is the projectCombo box

	private static ProjectCombo projectCombo = null;

	// these are the things that go in the simulationResultsPane

	private SwitchableGraphicsGrid switchableGrid;
	private TabbedTableViewer tabbedTableViewer = new TabbedTableViewer();

	// this is the container for the action buttons

	@FXML VBox controlsVBox;

	// The display selection variables: tells the user which project and timestamp is being looked at

	@FXML private Label projectCursorLabel;
	@FXML private Label timeStampCursorLabel;

	// Display Controls

	@FXML private HBox displayControlsBox;

	@FXML private ButtonBar leftButtonBar;
	@FXML private ButtonBar rightButtonBar;

	@FXML private Button colourHintButton;
	@FXML private Button logButton;
	@FXML private Button dataDumpButton;
	@FXML private Button restartButton;
	@FXML private Button toggleDecimalButton;
	@FXML private Button toggleValuesExpressionButton;
	@FXML private Button togglePricesExpressionButton;
	@FXML private Button graphicsToggle;
	@FXML private Button loadButton;

	@FXML private RadioButton showValuesButton;
	@FXML private RadioButton showQuantitiesButton;
	@FXML private RadioButton showPricesButton;
	ToggleGroup magnitudeToggle = new ToggleGroup();

	// Radio Buttons

	@FXML private RadioButton startSelectorButton;
	@FXML private RadioButton endSelectorButton;
	@FXML private RadioButton customSelectorButton;
	@FXML private RadioButton previousSelectorButton;
	ToggleGroup comparatorToggle = new ToggleGroup();

	/**
	 * ViewManager constructor.
	 * Called by the {@code loader} class before the {@code initialize} method by the controller, which knows about it
	 * because it is referenced in {@code SimulationOverview.fxml}.
	 */

	public ViewManager() {
		logger.debug("ViewManagerController Constructor was called");
		logger.debug(" Screen right is " + Double.toString(screenBounds.getMaxX()));
		logger.debug(" Screen top is " + Double.toString(screenBounds.getMaxY()));
		logger.debug(" Screen left is " + Double.toString(screenBounds.getMinX()));
		logger.debug(" Screen bottom is " + Double.toString(screenBounds.getMinY()));
	}

	// Initializes the controller class. This method is automatically called after the fxml file has been loaded.
	// the launch procedure also automatically calls 'setUp'
	// Since the work is done in 'setUp', here we do as little as possible.

	@FXML private void initialize() {
		logger.debug("Entered initialize");
	}

	/**
	 * Called from capitalism as part of the startup process.
	 * Most early initialization is done here but note that everything in the .fxml files are set up by reflection in 'Initialize' after the fxml file is
	 * loaded. This method sets up the project tables and initializes the project cursor, sets up the timeStamp table and initializes the timeStamp cursor,
	 * and populates all the tables. Note that the {@code Capitalism} class initializes some variables after loading (and hence after calling initialize) but
	 * before calling this method, which gets the variables from {@code Capitalism} here.
	 * 
	 * @param capitalism
	 *            the main application that created this viewManager.
	 * 
	 */
	public void setUp(Capitalism capitalism) {
		logger.debug("Entered setMainApp");
		sm = capitalism.getSimulation();
		ol = Capitalism.olProvider;
		initializeButtonBar();
		initializeRadioButtons();
		setTooltips();

		// NOTE: by now, we have to be sure sm has been initialized, or we will hand a null pointer to the actionButtonsBox
		initializeActionButtons();

		// the timestamp table is set up here, and also when the project changes
		refreshTimeStampTable();

		// set up the globals grid
		initializeGlobalsGrid();

		// set up the tabbed table viewer
		initializeTabbedTableViewer();

		// the display is refreshed every time it changes, starting here
		// TODO use the observables properly so changes are picked up automatically
		refreshDisplay();
	}

	private void setTooltips() {
		setTip(colourHintButton, "Display colour hints to indicate quantity, value and price columns");
		setTip(logButton, "Open a window displaying a step-by-step report on the simulation");
		setTip(graphicsToggle, "Display icons instead of text labels for some of the columns, creating more space to view the results");
		setTip(dataDumpButton, "Save the current project including its history to a directory that you choose");
		setTip(restartButton, "Restart the current project from scratch");
		setTip(showQuantitiesButton, "Switch to display of quantities in the overview table");
		setTip(showPricesButton, "Switch to display of prices in the overview table");
		setTip(showValuesButton, "Switch to display of values in the overview table");
		setTip(toggleDecimalButton, "Display magnitudes to two decimal points more");
		setTip(toggleValuesExpressionButton, "Display Values as time instead of money");
		setTip(togglePricesExpressionButton, "Display Prices as time instead of money");
		setTip(restartButton, "Restart the current project from scratch");
	}

	/**
	 * adds a tooltip to a Button. Overloads {@link setTip}
	 * 
	 * @param button
	 *            the table
	 * @param text
	 *            the tooltip
	 */
	private void setTip(Button button, String text) {
		Tooltip tip = new Tooltip();
		tip.setText(text);
		tip.setFont(new Font(15));
		button.setTooltip(tip);
	}

	/**
	 * adds a tooltip to a RadioButton. Overloads {@link setTip}
	 * 
	 * @param button
	 *            the table
	 * @param text
	 *            the tooltip
	 */
	private void setTip(RadioButton button, String text) {
		Tooltip tip = new Tooltip();
		tip.setText(text);
		tip.setFont(new Font(15));
		button.setTooltip(tip);
	}

	private void initializeRadioButtons() {
		startSelectorButton.setToggleGroup(comparatorToggle);
		endSelectorButton.setToggleGroup(comparatorToggle);
		customSelectorButton.setToggleGroup(comparatorToggle);
		previousSelectorButton.setToggleGroup(comparatorToggle);
		startSelectorButton.setUserData("START");
		endSelectorButton.setUserData("END");
		customSelectorButton.setUserData("CUSTOM");
		previousSelectorButton.setUserData("PREVIOUS");
		previousSelectorButton.setSelected(true);
		comparatorToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
				if (comparatorToggle.getSelectedToggle() != null) {
					String selected = comparatorToggle.getSelectedToggle().getUserData().toString();
					logger.debug(comparatorToggle.getSelectedToggle().getUserData().toString());
					switch (selected) {
					case "END":
						Simulation.setTimeStampComparatorCursor(Simulation.timeStampIDCurrent);
						break;
					case "START":
						Simulation.setTimeStampComparatorCursor(1);
						break;
					case "PREVIOUS":
						int cursor = Simulation.timeStampDisplayCursor;
						;
						cursor = cursor < 2 ? 1 : cursor - 1;
						Simulation.setTimeStampComparatorCursor(cursor);
						break;
					case "CUSTOM":
						Simulation.setTimeStampComparatorCursor(Simulation.timeStampDisplayCursor);
						break;
					default:
						logger.error("Unknown radio button {} selected ", selected);
						break;
					}
					refreshDisplay();
				}
			}
		});

		showValuesButton.setToggleGroup(magnitudeToggle);
		showPricesButton.setToggleGroup(magnitudeToggle);
		showQuantitiesButton.setToggleGroup(magnitudeToggle);
		showValuesButton.setUserData("VALUES");
		showPricesButton.setUserData("PRICES");
		showQuantitiesButton.setUserData("QUANTITIES");
		showPricesButton.setSelected(true);
		magnitudeToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
				if (magnitudeToggle.getSelectedToggle() != null) {
					String selected = magnitudeToggle.getSelectedToggle().getUserData().toString();
					logger.debug(magnitudeToggle.getSelectedToggle().getUserData().toString());
					switch (selected) {
					case "VALUES":
						tabbedTableViewer.setDisplayAttribute(Stock.ValueExpression.VALUE);
						break;
					case "PRICES":
						tabbedTableViewer.setDisplayAttribute(Stock.ValueExpression.PRICE);
						break;
					case "QUANTITIES":
						tabbedTableViewer.setDisplayAttribute(Stock.ValueExpression.QUANTITY);
						break;
					default:
						logger.error("Unknown radio button {} selected ", selected);
						break;
					}
					refreshDisplay();
				}
			}
		});
	}

	/**
	 * Complete re-initialisation of the whole database from user data.
	 * also reconstructs the main window and hence the entire display
	 */

	public void restart() {
		Reporter.report(logger, 1, "RESTART OF ENTIRE SIMULATION REQUESTED");
		Capitalism.dataHandler.restart();// fetch all the data
		sm.startup();// pre-process all the data
		actionButtonsBox.setActionStateFromLabel("Accumulate");
		refreshTimeStampTable();
		refreshDisplay();
		// capitalism.showMainWindow();// Set up the display (NOTE: this calls setMainApp and hence all the display initialization methods)
	}

	// initializes all the buttons in the bar at the top of the window.
	// These buttons control various aspects of the visualisation, but except for the restart button, do not affect the sm.

	private void initializeButtonBar() {

		initializeProjectCombo();
		colourHintButton.setOnAction((event) -> {
			toggleHints();
		});
		logButton.setOnAction((event) -> {
			Reporter.logWindow.showLoggerWindow();
		});
		restartButton.setOnAction((event) -> {
			restart();
		});
		dataDumpButton.setOnAction((event) -> {
			Reporter.report(logger, 1, "User requested dump of entire database to CSV files");
			dataDump();
		});
		loadButton.setOnAction((event) -> {
			Reporter.report(logger, 1, "User requested loading the database from a new location");
			dataLoad();
		});
		toggleDecimalButton.setOnAction((event) -> {
			toggleDecimals();
		});

		toggleValuesExpressionButton.setOnAction((event) -> {
			toggleValueExpression();
		});

		togglePricesExpressionButton.setOnAction((event) -> {
			togglePriceExpression();
		});

		graphicsToggle.setOnAction((event) -> {
			tabbedTableViewer.switchHeaderDisplay();
		});
		// initialize so start state is text only

		graphicsState = ContentDisplay.GRAPHIC_ONLY;
		tabbedTableViewer.switchHeaderDisplay();

		// start off displaying prices

		tabbedTableViewer.setDisplayAttribute(Stock.ValueExpression.PRICE);
	}

	/**
	 * toggle on and off colour hints for the columns
	 * 
	 */
	private void toggleHints() {
		if (displayHints) {
			setTip(colourHintButton, "Display colour hints to indicate quantity, value and price columns");
			colourHintButton.setText("Hints");
			displayHints = false;
			setTip(colourHintButton, "");
		} else {
			displayHints = true;
			setTip(colourHintButton, "Turn off colour hints for columns");
			colourHintButton.setText("No Hints");
		}
		refreshDisplay();
	}

	/**
	 * set up the comboBox for project selection
	 * TODO this ought to rebuild when projects are switched but haven't yet worked this out.
	 * Hence I just rebuild it.
	 * Note that this method must therefore be called explicitly when the project is switched.
	 */
	private void initializeProjectCombo() {
		ObservableList<Project> projects = ol.observableProjects();
		Project currentProject = Capitalism.selectionsProvider.projectSingle(Simulation.projectCurrent);
		String currentProjectDescription = currentProject.getDescription();
		projectCombo = new ProjectCombo(projects, currentProjectDescription);
		displayControlsBox.getChildren().add(0, projectCombo);
	}

	/**
	 * Ask the user where to store data and then dump the database into it as a set of CSV files
	 */
	private void dataDump() {
		File saveDirectory = Dialogues.directoryChooser("Location to save data");
		Capitalism.dataHandler.saveDataBase(saveDirectory);
		// TODO what can go wrong?
		// think of all the possible problems and catch them.
		// a programme error may mean that the location is not a directory
		// the user may not have permission to write there
		// one or more of the files may already exist
	}

	/**
	 * Ask the user where to load data from and then restore the database from it
	 */
	private void dataLoad() {
		File saveDirectory = Dialogues.directoryChooser("Location of the new data");
		try {
			Capitalism.dataHandler.loadDatabase(saveDirectory.getCanonicalPath());
		} catch (IOException e) {
			// TODO handle this better
			e.printStackTrace();
		}
		// TODO what can go wrong?
		// the files may not be there
		// the user may not have saved the current state of the directory

	}

	private void initializeTabbedTableViewer() {
		simulationResultsPane.getChildren().add(tabbedTableViewer);
		tabbedTableViewer.createDynamicCircuitsTable();

	}

	/**
	 * set up the globals grid to display Glabels that can be used display text, graphics and the relevant numeric value from the current Globals record
	 * set the values of the labels within which these numeric values will be displayed. See also ({@link populateGlobalsGrid}
	 * A bit of a botch; the labels should be bound to the Global entity directly.
	 * TODO fix this.
	 */

	private void initializeGlobalsGrid() {
		switchableGrid = new SwitchableGraphicsGrid();
		simulationResultsPane.getChildren().add(switchableGrid);
	}

	/**
	 * populate the number fields in the globals grid from the current value of the Global persistent entity.
	 * See also {@link initializeGlobalsGrid}
	 */
	private void populateGlobalsGrid() {
		Global global = DataManager.getGlobal(Simulation.timeStampIDCurrent);
		switchableGrid.populate(smallNumbersFormatString, global);
	}

	/**
	 * create an ActionButtonsBox control, tell it that we are managing it, and tell it to build the buttons
	 */
	private void initializeActionButtons() {
		// create the box once only, even if we restart
		if (actionButtonsBox == null) {
			actionButtonsBox = new ActionButtonsBox();
			actionButtonsBox.setViewManager(this);
		}
		controlsVBox.getChildren().add(actionButtonsBox);
	}

	private void toggleDecimals() {
		if (largeNumbersFormatString.equals("%1$,.0f")) {
			largeNumbersFormatString = "%1$,.2f";
			smallNumbersFormatString = "%1$.4f";
			setTip(toggleDecimalButton, "Display all large magnitudes to zero decimal places and all small magnitudes to two decimal places");
			toggleDecimalButton.setText("<<..");
		} else {
			largeNumbersFormatString = "%1$,.0f";
			smallNumbersFormatString = "%1$.2f";
			setTip(toggleDecimalButton, "Display all large magnitudes to two decimal places and all small magnitudes to four decimal places");
			toggleDecimalButton.setText("..>>");
		}
		refreshDisplay();
	}

	/**
	 * extract the per-project currency and quantity symbols from the globals record.
	 * NOTE these should perhaps be in the project record not the globals record.
	 * But if a simulation involves a currency reform, it could be in the right place after all.
	 */
	public void setExpressionSymbols() {
		Global global = DataManager.getGlobal(1);
		moneyExpressionSymbol = global.getCurrencySymbol();
		quantityExpressionSymbol = global.getQuantitySymbol();
		
		//sneaky way to force the display options onto the new project
		
		togglePriceExpression();
		togglePriceExpression();
		toggleValueExpression();
		toggleValueExpression();
		
	}

	private void togglePriceExpression() {
		if (pricesExpressionDisplay == DisplayAsExpression.MONEY) {
			pricesExpressionDisplay = DisplayAsExpression.TIME;
			pricesExpressionSymbol = quantityExpressionSymbol;
			togglePricesExpressionButton.setText("$Prices");
			setTip(togglePricesExpressionButton, "Display Prices as money instead of time");
		} else {
			pricesExpressionDisplay = DisplayAsExpression.MONEY;
			pricesExpressionSymbol = moneyExpressionSymbol;
			togglePricesExpressionButton.setText("#Prices");
			setTip(togglePricesExpressionButton, "Display Prices as time instead of money");
		}
		;
		refreshDisplay();
	}

	private void toggleValueExpression() {
		if (valuesExpressionDisplay == DisplayAsExpression.MONEY) {
			valuesExpressionDisplay = DisplayAsExpression.TIME;
			valuesExpressionSymbol = quantityExpressionSymbol;
			toggleValuesExpressionButton.setText("$Values");
			setTip(toggleValuesExpressionButton, "Display Prices as money instead of time");
		} else {
			valuesExpressionDisplay = DisplayAsExpression.MONEY;
			valuesExpressionSymbol = moneyExpressionSymbol;
			toggleValuesExpressionButton.setText("#Values");
			setTip(toggleValuesExpressionButton, "Display Prices as time instead of money");
		}
		;
		refreshDisplay();
	}

	/**
	 * (for display purposes). Allows the user to view a value magnitude in either its intrinsic expression (time) or its extrinsic expression (money)
	 * depending on the setting of expressionDisplay
	 * 
	 * @param intrinsicValueExpression
	 *            the magnitude of value, expressed intrinsically (usually, time but developers may add other value categories with different dimensions)
	 * @param valuesExpressionDisplay
	 *            selects whether to display as an intrinsic or an extrinsic magnitude
	 * @return the requested expression of the value magnitude - unchanged if expressionDisplay is TIME but divided by MELT if expressionDisplay is MONEY
	 */

	public static double valueExpression(double intrinsicValueExpression, DisplayAsExpression valuesExpressionDisplay) {
		if (valuesExpressionDisplay == DisplayAsExpression.MONEY) {
			return intrinsicValueExpression;
		} else {
			Global global = DataManager.getGlobal(Simulation.timeStampIDCurrent);
			double melt = global.getMelt();
			return intrinsicValueExpression / melt;
		}
	}

	/**
	 * reset the timeStampDisplayCursor after the user has made a selection
	 * 
	 * @param selectedTimeStamp
	 *            the timeStamp that the user selected
	 */

	public void viewTimeStamp(TimeStamp selectedTimeStamp) {
		int selectedTimeStampID = selectedTimeStamp.getTimeStampID();
		if (selectedTimeStampID > Simulation.timeStampIDCurrent)
			return; // this doesn't yet exist
		if (selectedTimeStampID == -1)
			return;// this is a placesholder whose sub-actions have not yet occurred;
		logger.debug("User opted to view the timeStamp {} with comparator {} ", selectedTimeStampID, selectedTimeStamp.getComparatorTimeStampID());
		Simulation.timeStampDisplayCursor = selectedTimeStampID;
		// Simulation.setTimeStampComparatorCursor(selectedTimeStamp.getComparatorTimeStampID());
		refreshDisplay();
	}

	/**
	 * responds when the user switches project
	 * 
	 * @param newValue
	 *            the selected Project
	 */
	public void switchProject(Project newValue) {
		logger.debug("entered switchProject");
		if (newValue.getProjectID() != Simulation.projectCurrent) {
			logger.debug("Requested switch to project with ID {} and description {} ", newValue.getProjectID(), newValue.getDescription());
			DataManager.switchProjects(newValue.getProjectID(), actionButtonsBox);
			setExpressionSymbols();
			refreshTimeStampTable();
			refreshDisplay();
		}
	}

	/**
	 * builds the main treeView which displays the results of the simulation
	 */

	public void refreshTimeStampTable() {
		int periods = Simulation.periodCurrent;
		if (tree != null) {// we've already created one tree, so now we have to delete it and start over
			controlsVBox.getChildren().remove(tree);
		}
		tree = new TimeStampView();
		tree.setShowRoot(false);

		// public TimeStamp(int timeStampID, int projectFK, int period, String superState, int COMPARATORTIMESTAMPID, String description)
		// the root, which is not displayed, contains an observed timeStamp with ID = -1, which is not persisted (hence the ID causes no conflicts)
		TimeStampViewItem treeRoot = new TimeStampViewItem(new TimeStamp(-1, 1, 0, "", -1, "Start"));
		tree.setRoot(treeRoot);

		// create a node for each current period, containing an observed timeStamp with:
		// ID=1 at the start of the simulation
		// ID=the last timeStamp in the previous period for each subsequent period
		// TODO the second of these

		for (int period = 1; period < periods + 1; period++) {
			TimeStampViewItem periodRoot = new TimeStampViewItem(
					new TimeStamp(1, Simulation.projectCurrent, period, "", -1, String.format("Period %d", period)));
			treeRoot.getChildren().add(periodRoot);
			if (period < periods) {
				logger.debug(" The view of period {} will be closed up because it is not the current period", period);
				periodRoot.setExpanded(false);
			}
		}

		// create the tree

		for (TreeItem<TimeStamp> periodItem : treeRoot.getChildren()) {
			int thisPeriod = periodItem.getValue().getPeriod();

			// for each period create a node for all actionStates, using the actionState table.
			// this means that all superstates are displayed in the tree, even if we haven't reached them yet in the simulation
			// if the actionState is a superState make it a child of the period

			for (ActionStates a : ActionStates.values()) {
				if (a.superAction == null) { // it's not a baby

					TimeStampViewItem superStateRoot = new TimeStampViewItem(
							new TimeStamp(-1, Simulation.projectCurrent, periodItem.getValue().getPeriod(), a.getText(), -1, a.getText()));
					periodItem.getChildren().add(superStateRoot);
					logger.debug("Adding the superstate action called {} in period {}", a.getText(), thisPeriod);

					// add all the children.
					// these are taken from the timeStamp table, not the actionState table. Thus, we only add the states that have been reached in the
					// simulation

					for (TimeStamp childStamp : SelectionsProvider.timeStampsBySuperState(Simulation.projectCurrent, thisPeriod, a.text)) {
						logger.debug("Processing the timestamp called {}", childStamp.getDescription());
						TimeStampViewItem childState = new TimeStampViewItem(childStamp);

						// set the ID for the superstate of which this is a part, so that if the user opts to view it,
						// the tabbedTables will display the data from the last executed component action (child)

						superStateRoot.getChildren().add(childState);
						superStateRoot.getValue().setTimeStampID(childStamp.getTimeStampID());
					}
				}
			}
		}
		controlsVBox.getChildren().add(1, tree);
	}

	public int getLastPeriod(int project) {
		int p = 0;
		for (TimeStamp t : SelectionsProvider.timeStampsAll(project)) {
			if (t.getPeriod() > p)
				p = t.getPeriod();
		}
		return p;
	}

	/**
	 * Repopulate each {@code ViewTable} with its observable list. Called when either the underlying data changes because the sm has moved forward, or
	 * the user selects a new project or timeStamp for study..
	 * 
	 * TODO it should not, I think, be necessary to do this each time we advance a step in the simulation. The whole point
	 * of binding the tables to the entities is to that when the entities change, so do the controls in which they display.
	 * But initially, the main aim is to get the infernal thing working...
	 */
	public void refreshDisplay() {
		int currentProject = Simulation.projectCurrent;
		projectCursorLabel.setText("Project " + currentProject);
		timeStampCursorLabel.setText("Time " + Simulation.timeStampDisplayCursor);

		logger.debug(String.format("Refresh Display with project %d, timestamp %d and comparator %d",
				currentProject, Simulation.timeStampDisplayCursor, Simulation.getTimeStampComparatorCursor()));

		tabbedTableViewer.populateUseValuesViewTable();
		tabbedTableViewer.populateProductiveStocksViewTable();
		tabbedTableViewer.populateMoneyStocksViewTable();
		tabbedTableViewer.populateSalesStocksViewTable();
		tabbedTableViewer.populateConsumptionStocksViewTable();
		tabbedTableViewer.populateCircuitsViewTable();
		tabbedTableViewer.populateSocialClassesViewTable();
		populateGlobalsGrid();
		tabbedTableViewer.createDynamicCircuitsTable();
	}
}