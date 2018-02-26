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

package capitalism.view;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.Capitalism;
import capitalism.Simulation;
import capitalism.model.Global;
import capitalism.model.Project;
import capitalism.model.TimeStamp;
import capitalism.utils.Dialogues;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionButtonsBox;
import capitalism.view.custom.ActionStates;
import capitalism.view.custom.DisplayControls;
import capitalism.view.custom.TimeStampView;
import capitalism.view.custom.TimeStampViewItem;
import capitalism.view.custom.TrackingControls;
import capitalism.view.tables.SwitchableGraphicsGrid;
import capitalism.view.tables.TabbedTableViewer;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;

public class ViewManager {
	final static Logger logger = LogManager.getLogger("ViewManager");

	// general display parameters

	public static Rectangle2D screenBounds = Screen.getPrimary().getBounds();
	public static String largeNumbersFormatString = "%1$,.0f";	// Formats the display of large floating point numbers
	public static String smallNumbersFormatString = "%1$.2f";   // Formats the display of small floating point numbers
	public static ContentDisplay graphicsState = ContentDisplay.TEXT_ONLY; // Whether to display graphics, text, or both

	public static String deltaSymbol = "± ";

	private static ActionButtonsBox actionButtonsBox;

	// this is the right-hand Vbox, the container within which we put the button bar, the grid pane, and the tabbed table viewer
	VBox simulationResultsPane = new VBox();;
	HBox manePane= new HBox();
	VBox bigEverything= new VBox();
	AnchorPane anchorPane = new AnchorPane();
	
	// this is the root of the timeStamp treeView
	private TimeStampView tree;

	// these are the things that go in the simulationResultsPane

	private SwitchableGraphicsGrid switchableGrid;
	private static TabbedTableViewer tabbedTableViewer;

	// Display Controls
	private DisplayControls displayControls= new DisplayControls();
	private TrackingControls trackingControls =new TrackingControls();


	public ViewManager(AnchorPane anchorPane) {
		logger.debug("ViewManagerController Constructor was called");
		logger.debug(" Screen right is " + Double.toString(screenBounds.getMaxX()));
		logger.debug(" Screen top is " + Double.toString(screenBounds.getMaxY()));
		logger.debug(" Screen left is " + Double.toString(screenBounds.getMinX()));
		logger.debug(" Screen bottom is " + Double.toString(screenBounds.getMinY()));
		this.anchorPane=anchorPane;
		tabbedTableViewer = new TabbedTableViewer();
	}

	/**
	 * Called from capitalism as part of the startup process.

	 * @param capitalism
	 *            the main application that created this viewManager.
	 * 
	 */
	public void setUp(Capitalism capitalism) {
		logger.debug("Setting up the View Manager");

		// set up the tabbed table viewer, the combo box and the button bars
		addCustomControls();
		
		// the timestamp table is set up here, and also when the project changes
		refreshTimeStampTable();

		// the display is refreshed every time it changes, starting here
		refreshDisplay();
	}

	/**
	 * adds a tooltip to a CheckBox. Overloads {@link setTip}
	 * 
	 * @param box
	 *            the table
	 * @param text
	 *            the tooltip
	 */
	@SuppressWarnings("unused") private void setTip(CheckBox box, String text) {
		Tooltip tip = new Tooltip();
		tip.setText(text);
		tip.setFont(new Font(15));
		box.setTooltip(tip);
	}

	/**
	 * adds a tooltip to a RadioButton. Overloads {@link setTip}
	 * 
	 * @param button
	 *            the table
	 * @param text
	 *            the tooltip
	 */
	public static void setTip(RadioButton button, String text) {
		Tooltip tip = new Tooltip();
		tip.setText(text);
		tip.setFont(new Font(15));
		button.setTooltip(tip);
	}

	/**
	 * Complete re-initialisation of the whole database from user data.
	 * also reconstructs the main window and hence the entire display
	 */

	public void restart() {
		Reporter.report(logger, 1, "RESTART OF ENTIRE SIMULATION REQUESTED");
		Capitalism.dataHandler.restart();// fetch all the data
		Simulation.startup();// pre-process all the data
		actionButtonsBox.setActionStateFromLabel("Accumulate");
		refreshTimeStampTable();
		refreshDisplay();
		// capitalism.showMainWindow();// Set up the display (NOTE: this calls setMainApp and hence all the display initialization methods)
	}

	/**
	 * Ask the user where to store data and then dump the database into it as a set of CSV files
	 */
	public void dataDump() {
		File saveDirectory = Dialogues.directoryChooser("Location to save data");
		Capitalism.dataHandler.saveDataBase(saveDirectory);
		// TODO think systematically about what can go wrong, and trap it
		// think of all the possible problems and catch them.
		// a programme error may mean that the location is not a directory
		// the user may not have permission to write there
		// one or more of the files may already exist
	}

	/**
	 * Ask the user where to load data from and then restore the database from it
	 */
	public void dataLoad() {
		File saveDirectory = Dialogues.directoryChooser("Location of the new data");
		try {
			Capitalism.dataHandler.loadDatabase(saveDirectory.getCanonicalPath());
		} catch (IOException e) {
			// TODO handle this better
			e.printStackTrace();
		}
		// TODO think systematically about what can go wrong, and trap it
		// the files may not be there
		// the user may not have saved the current state of the directory

	}
	/**
	 * set up the globals grid to display Glabels that can be used display text, graphics and the relevant numeric value from the current Globals record
	 * set the values of the labels within which these numeric values will be displayed. See also ({@link populateGlobalsGrid}
	 * add the tabbedTableViewer and the displayControlsBar
	 */
	private void addCustomControls() {
		anchorPane.getChildren().add(bigEverything);
		AnchorPane.setBottomAnchor(bigEverything, 0.0);
		AnchorPane.setTopAnchor(bigEverything, 0.0);
		AnchorPane.setLeftAnchor(bigEverything, 0.0);
		AnchorPane.setRightAnchor(bigEverything, 0.0);
		bigEverything.getChildren().add(manePane);
		displayControls.registerViewManager(this);
		manePane.setMaxHeight(Double.MAX_VALUE);
		manePane.setMaxWidth(Double.MAX_VALUE);
		VBox.setVgrow(manePane, Priority.ALWAYS);
		manePane.getChildren().addAll(trackingControls,simulationResultsPane);
		switchableGrid = new SwitchableGraphicsGrid();
		simulationResultsPane.setMaxHeight(Double.MAX_VALUE);
		simulationResultsPane.setMaxWidth(Double.MAX_VALUE);
		simulationResultsPane.setPrefHeight(700);
		simulationResultsPane.setPrefWidth(836);
		simulationResultsPane.getChildren().add(displayControls);
		simulationResultsPane.getChildren().add(switchableGrid);
		simulationResultsPane.getChildren().add(tabbedTableViewer);
		HBox.setHgrow(simulationResultsPane, Priority.ALWAYS);

		// Create an ActionButtonsBox control, tell it that we are managing it, and tell it to build the buttons
		// Create the box once only, even if we restart
		if (actionButtonsBox == null) {
			actionButtonsBox = new ActionButtonsBox();
			actionButtonsBox.setViewManager(this);
		}
		trackingControls.getChildren().add(actionButtonsBox);	}

	/**
	 * populate the number fields in the globals grid from the value of the Global persistent entity
	 * at the timeStamp given by the displayCurrsor
	 * 
	 */
	private void populateGlobalsGrid() {
		Global global = Global.getGlobal(Simulation.timeStampDisplayCursor);
		switchableGrid.populate(smallNumbersFormatString, global);
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

	public static double valueExpression(double intrinsicValueExpression, DisplayControls.DISPLAY_AS_EXPRESSION valuesExpressionDisplay) {
		if (valuesExpressionDisplay == DisplayControls.DISPLAY_AS_EXPRESSION.MONEY) {
			return intrinsicValueExpression;
		} else {
			Global global = Global.getGlobal();
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
			Simulation.switchProjects(newValue.getProjectID(), actionButtonsBox);

			// user has the option to choose the monetary unit and its visual expression
			DisplayControls.setExpressionSymbols();

			// the timeStamp has been reset in DataManager. Our responsibility is to display the consequences of the change
			refreshTimeStampTable();

			// rebuild all the tables, because the number of columnns might have changed
			tabbedTableViewer.buildTables();

			// repopulate the tables we just built. Probably superfluous
			refreshDisplay();
		}
	}

	/**
	 * builds the main treeView which displays the results of the simulation
	 */

	public void refreshTimeStampTable() {
		int periods = Simulation.periodCurrent;
		if (tree != null) {// we've already created one tree, so now we have to delete it and start over
			trackingControls.getChildren().remove(tree);
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

					for (TimeStamp childStamp : TimeStamp.timeStampsBySuperState(thisPeriod, a.text)) {
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
		trackingControls.getChildren().add(1, tree);
	}

	public static int getLastPeriod(int project) {
		int p = 0;
		for (TimeStamp t : TimeStamp.timeStampsAll()) {
			if (t.getPeriod() > p)
				p = t.getPeriod();
		}
		return p;
	}

	/**
	 * Repopulate each {@code ViewTable} with its observable list. Called when either the underlying data changes because the sm has moved forward, or
	 * the user selects a new project or timeStamp for study..
	 */
	public void refreshDisplay() {
		int currentProject = Simulation.projectCurrent;
		TrackingControls.getProjectCursorLabel().setText("Project " + currentProject);
		TrackingControls.getTimeStampCursorLabel().setText("Time " + Simulation.timeStampDisplayCursor);

		logger.debug(String.format("Refresh Display with project %d, timestamp %d and comparator %d",
				currentProject, Simulation.timeStampDisplayCursor, Simulation.getTimeStampComparatorCursor()));

		// as well as repopulating the data model with rePopulateTabbedTables,
		// we have to force a refresh of the display because if the data has not changed, it won't refresh
		// see https://stackoverflow.com/questions/11065140/javafx-2-1-tableview-refresh-items
		tabbedTableViewer.repopulateTabbedTables();
		TabbedTableViewer.refreshTables();
		populateGlobalsGrid();
	}

	/**
	 * @return the tabbedTableViewer
	 */
	public static TabbedTableViewer getTabbedTableViewer() {
		return tabbedTableViewer;
	}

}