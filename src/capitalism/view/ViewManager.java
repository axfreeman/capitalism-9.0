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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.Capitalism;
import capitalism.controller.Simulation;
import capitalism.model.Project;
import capitalism.model.TimeStamp;
import capitalism.utils.Reporter;
import capitalism.view.custom.ActionButtonsBox;
import capitalism.view.custom.ActionStates;
import capitalism.view.custom.DisplayControlsBox;
import capitalism.view.custom.TimeStampView;
import capitalism.view.custom.TimeStampViewItem;
import capitalism.view.custom.TrackingControlsBox;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * This class controls the display. It constructs a container window called rootLayout which has no functionality
 * and can probably be dispensed with. In this container it places another container called anchorPane which helps
 * provide for the resizing of the window by the user. Within that, it lays out the components so that they appear
 * in the correct places and are as responsive as possible to user interactions.
 * 
 * Most of this is done by means of custom controls which can be found in the package {@code capitalism.view.custom}.
 * 
 * As a general rule, the graphic components are static as are the methods that manage them or provide access to
 * them. Since there will only always be one instance of the display at any time, I can see no cost to this and
 * it allows for a clean, functional programming style where most if not all references are to the ViewManager class
 * and not to the single instance of it.
 * 
 * The same principles are applied to the custom controls.
 * 
 * These principles are NOT however applied to the data model, where multiple instances of every record are created
 * all the time. For this same reason they don't apply to the display classes in {@code capitalism.view.tables} which
 * manage the layout of the tables. Multiple instances of most of these exist because the code is designed to be
 * as re-usable as possible.
 *
 */

public class ViewManager {
	final static Logger logger = LogManager.getLogger("ViewManager");

	// general display constants that are fixed throughout

	final public static Rectangle2D screenBounds = Screen.getPrimary().getBounds();
	final public static String deltaSymbol = "± ";

	// display parameters that can change as the simulation proceeds

	private static String largeFormat = "%1$,.0f";	// Formats the display of large floating point numbers
	private static String smallFormat = "%1$.2f";   // Formats the display of small floating point numbers

	// graphic elements and custom controls
	private static Stage primaryStage;
	private static BorderPane rootLayout = new BorderPane();
	private static ActionButtonsBox actionButtonsBox;
	private static VBox simulationResultsPane;
	private static HBox manePane;
	private static VBox bigEverything;
	private static AnchorPane anchorPane;
	private static TimeStampView timeStampViewer;
	private static SwitchableGraphicsGrid switchableGrid;
	private static TabbedTableViewer tabbedTableViewer;
	private static DisplayControlsBox displayControlsBox;
	private static TrackingControlsBox trackingControlsBox;

	public ViewManager() {
		logger.debug("Creating ViewManager");
		logger.debug(" Screen right is " + Double.toString(screenBounds.getMaxX()));
		logger.debug(" Screen top is " + Double.toString(screenBounds.getMaxY()));
		logger.debug(" Screen left is " + Double.toString(screenBounds.getMinX()));
		logger.debug(" Screen bottom is " + Double.toString(screenBounds.getMinY()));

		// construct the root window, a simple container with almost no functionality
		// TODO do we really need it?

		rootLayout.setPrefHeight(800);
		rootLayout.setPrefWidth(1300);

		// display the root layout. Later (in startup) it will hold an anchorPane where most of the business is conducted.

		Scene scene = new Scene(rootLayout);
		String css = getClass().getResource("/SimulationTheme.css").toExternalForm();
		scene.getStylesheets().add(css);
		primaryStage.setScene(scene);
		primaryStage.setY(0);
		primaryStage.show();
		primaryStage.setTitle("Capitalism");

		// now, and only now, create the logger window. We had to wait until the application launched but
		// we have to do it now because otherwise, we would crash the logging reports that occur during Simulation.startup()

		Reporter.createLogWindow();
	}

	/**
	 * Called from capitalism as part of the startup process. Constructs all the detail, mainly by invoking
	 * the helper method {@code addCustomControls}, then populates the displays.
	 */
	public static void startUp() {

		// set up all the custom controls which are the meat of the simulation. This includes the
		// tabbed table viewer, the combo box, the button bars and the timeStampView

		addCustomControls();

		// the timestamp table is populated here, and also when the project changes
		refreshTimeStampView();

		// the display is refreshed every time it changes, starting here
		refreshDisplay();
	}

	/**
	 * This is where most of the work is done. All custom controls are laid out inside anchorPane, which
	 * is the container for the application.
	 */
	private static void addCustomControls() {
		anchorPane = new AnchorPane();
		anchorPane.setPrefHeight(800);
		anchorPane.setPrefWidth(1300);
		simulationResultsPane = new VBox();
		manePane = new HBox();
		bigEverything = new VBox();
		anchorPane.getChildren().add(bigEverything);
		AnchorPane.setBottomAnchor(bigEverything, 0.0);
		AnchorPane.setTopAnchor(bigEverything, 0.0);
		AnchorPane.setLeftAnchor(bigEverything, 0.0);
		AnchorPane.setRightAnchor(bigEverything, 0.0);
		rootLayout.setCenter(anchorPane);			// Set the overview stage into the center of the root layout.
		bigEverything.getChildren().add(manePane);
		manePane.setMaxHeight(Double.MAX_VALUE);
		manePane.setMaxWidth(Double.MAX_VALUE);
		VBox.setVgrow(manePane, Priority.ALWAYS);
		tabbedTableViewer = new TabbedTableViewer();
		displayControlsBox = new DisplayControlsBox();
		trackingControlsBox = new TrackingControlsBox();
		manePane.getChildren().addAll(trackingControlsBox, simulationResultsPane);
		switchableGrid = new SwitchableGraphicsGrid();
		simulationResultsPane.setMaxHeight(Double.MAX_VALUE);
		simulationResultsPane.setMaxWidth(Double.MAX_VALUE);
		simulationResultsPane.setPrefHeight(700);
		simulationResultsPane.setPrefWidth(836);
		simulationResultsPane.getChildren().add(displayControlsBox);
		simulationResultsPane.getChildren().add(switchableGrid);
		simulationResultsPane.getChildren().add(tabbedTableViewer);
		HBox.setHgrow(simulationResultsPane, Priority.ALWAYS);

		// Create an ActionButtonsBox control, tell it that we are managing it, and tell it to build the buttons
		// Create the box once only, even if we restart
		if (actionButtonsBox == null) {
			actionButtonsBox = new ActionButtonsBox();
		}
		trackingControlsBox.getChildren().add(actionButtonsBox);
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

	public static void restart() {
		Reporter.report(logger, 1, "RESTART OF ENTIRE SIMULATION REQUESTED");
		Capitalism.dataHandler.restart();// fetch all the data
		Simulation.startup();// pre-process all the data
		actionButtonsBox.setActionStateFromLabel("Accumulate");
		refreshTimeStampView();
		refreshDisplay();
		// capitalism.showMainWindow();// Set up the display (NOTE: this calls setMainApp and hence all the display initialization methods)
	}

	
	/**
	 * populate the number fields in the summary grid from the value of the TimeStamp persistent entity
	 * defined by the displayCursor
	 * 
	 */
	private static void populateSummaryGrid() {
		TimeStamp timeStamp = TimeStamp.get(Simulation.timeStampDisplayCursor);
		switchableGrid.populate(smallFormat, timeStamp);
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

	public static double valueExpression(double intrinsicValueExpression, DisplayControlsBox.DISPLAY_AS_EXPRESSION valuesExpressionDisplay) {
		if (valuesExpressionDisplay == DisplayControlsBox.DISPLAY_AS_EXPRESSION.MONEY) {
			return intrinsicValueExpression;
		} else {
			double melt = Simulation.currentTimeStamp.getMelt();
			return intrinsicValueExpression / melt;
		}
	}

	/**
	 * reset the timeStampDisplayCursor after the user has made a selection
	 * 
	 * @param selectedTimeStamp
	 *            the timeStamp that the user selected
	 */

	public static void viewTimeStamp(TimeStamp selectedTimeStamp) {
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
			DisplayControlsBox.setExpressionSymbols();

			// the timeStamp has been reset in DataManager. Our responsibility is to display the consequences of the change
			refreshTimeStampView();

			// rebuild all the tables, because the number of columnns might have changed
			tabbedTableViewer.buildTables();

			// repopulate the tables we just built. Probably superfluous
			refreshDisplay();
		}
	}

	/**
	 * builds the main treeView which displays the results of the simulation
	 */

	public static void refreshTimeStampView() {
		logger.debug("Refreshing the timeStamp treeview for project {} at period {} and timeStamp {}",
				Simulation.getProjectCurrent(), Simulation.getPeriodCurrent(), Simulation.timeStampIDCurrent);
		int periods = Simulation.getPeriodCurrent();
		if (timeStampViewer != null) {// we've already created one tree, so now we have to delete it and start over
			trackingControlsBox.getChildren().remove(timeStampViewer);
		}
		timeStampViewer = new TimeStampView();
		timeStampViewer.setShowRoot(false);

		// the root, which is not displayed, contains an observed timeStamp with ID = -1, which is not persisted (hence the ID causes no conflicts)

		TimeStampViewItem treeRoot = new TimeStampViewItem(new TimeStamp(-1, 1, 0, "", -1, "Start"));
		timeStampViewer.setRoot(treeRoot);

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
							new TimeStamp(-1, Simulation.projectCurrent, periodItem.getValue().getPeriod(), a.text(), -1, a.text()));
					periodItem.getChildren().add(superStateRoot);
					logger.debug("Adding the superstate action called {} in period {}", a.text(), thisPeriod);

					// add all the children.
					// these are taken from the timeStamp table, not the actionState table. Thus, we only add the states that have been reached in the
					// simulation

					// diagnostics -switch off unless there are problems with the treeview
					for (TimeStamp t : TimeStamp.allInProject()) {
						logger.debug("TimeStamp described as {} has timeStampID {}, project {}, period {} and superState {}",
								t.getDescription(), t.getTimeStampID(), t.getProjectFK(), t.getPeriod(), t.getSuperState());
					}

					for (TimeStamp childStamp : TimeStamp.superStateChildren(thisPeriod, a.text())) {
						logger.debug("Processing the timestamp called {} in period {}", childStamp.getDescription(), thisPeriod);
						TimeStampViewItem childState = new TimeStampViewItem(childStamp);

						// set the ID for the superstate of which this is a part, so that if the user opts to view it,
						// the tabbedTables will display the data from the last executed component action (child)

						superStateRoot.getChildren().add(childState);
						superStateRoot.getValue().setTimeStampID(childStamp.getTimeStampID());
					}
				}
			}
		}
		trackingControlsBox.getChildren().add(1, timeStampViewer);
	}

	/**
	 * Repopulate each {@code ViewTable} with its observable list. Called when either the underlying data changes because the sm has moved forward, or
	 * the user selects a new project or timeStamp for study.
	 * 
	 * TODO the relation between this and {@link TabbedTableViewer#refreshTables()} is a bit murky. In theory the latter
	 * should be self-sufficient but I could not quite get this to work, because of a glitch in javafx.
	 */
	public static void refreshDisplay() {
		int currentProject = Simulation.projectCurrent;
		TrackingControlsBox.getProjectCursorLabel().setText("Project " + currentProject);
		TrackingControlsBox.getTimeStampCursorLabel().setText("Time " + Simulation.timeStampDisplayCursor);

		logger.debug(String.format("Refresh Display with project %d, timestamp %d and comparator %d",
				currentProject, Simulation.timeStampDisplayCursor, Simulation.getTimeStampComparatorCursor()));

		// as well as repopulating the data model with rePopulateTabbedTables,
		// we have to force a refresh of the display because if the data has not changed, it won't refresh
		// see https://stackoverflow.com/questions/11065140/javafx-2-1-tableview-refresh-items

		tabbedTableViewer.repopulateTabbedTables();
		TabbedTableViewer.refreshTables();
		populateSummaryGrid();
	}

	/**
	 * @return the tabbedTableViewer
	 */
	public static TabbedTableViewer getTabbedTableViewer() {
		return tabbedTableViewer;
	}

	/**
	 * @return the primaryStage
	 */
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * @param primaryStage
	 *            the primaryStage to set
	 */
	public static void setPrimaryStage(Stage primaryStage) {
		ViewManager.primaryStage = primaryStage;
	}

	/**
	 * @return the largeFormat
	 */
	public static String getLargeFormat() {
		return largeFormat;
	}

	/**
	 * @param largeFormat
	 *            the largeFormat to set
	 */
	public static void setLargeFormat(String largeFormat) {
		ViewManager.largeFormat = largeFormat;
	}

	/**
	 * @return the smallFormat
	 */
	public static String getSmallFormat() {
		return smallFormat;
	}

	/**
	 * @param smallFormat
	 *            the smallFormat to set
	 */
	public static void setSmallFormat(String smallFormat) {
		ViewManager.smallFormat = smallFormat;
	}
}