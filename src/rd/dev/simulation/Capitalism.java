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

package rd.dev.simulation;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import rd.dev.simulation.command.PreTrade;
import rd.dev.simulation.custom.ActionStates;
import rd.dev.simulation.datamanagement.DataManager;
import rd.dev.simulation.datamanagement.ObservableListProvider;
import rd.dev.simulation.datamanagement.SelectionsProvider;
import rd.dev.simulation.utils.DBHandler;
import rd.dev.simulation.utils.Reporter;
import rd.dev.simulation.view.ViewManager;
import rd.dev.simulation.command.Demand;

public class Capitalism extends Application {

	private static final Logger logger = LogManager.getLogger(Capitalism.class);
	public static DBHandler dataHandler = new DBHandler();			// handles the initial database transactions such as creation and initialisation
	public static DataManager dataManager = new DataManager();		// handles most persistent entities
	public static ObservableListProvider olProvider = new ObservableListProvider();// interface between persistent entities and the display
	public static Simulation simulation = new Simulation();			// controls the action
	public static SelectionsProvider selectionsProvider = null;
	public static ViewManager viewManager = null; 					// controls the display
	/**
	 * the base for all files that are created in, or copied into, the user's file system
	 */
	public static String userBasePath = System.getProperty("user.home").replace('\\', '/') + "/Documents/Capsim/";

	// graphic elements that need to be accessed by several different managers

	public static Stage primaryStage;
	private BorderPane rootLayout;

	public static void main(String[] args) {
		logger.debug("Entered Capitalism constructor");

		/*
		 * 'Launch' is where the action begins.
		 * However, there ain't no such thing as a free launch.
		 * The Order is:
		 * 1. calls super.init()(does nothing unless overeridden)
		 * 2. calls 'start' (see below)
		 * 3. waits until finished - either because of Platform.exit() or because last window is closed (provided Platform.implicitExit attribute is true)
		 * 4. calls stop() to turn out the lights (does nothing unless overridden)
		 * see https://docs.oracle.com/javase/8/javafx/api/javafx/application/Application.html
		 */

		launch();
	}

	/**
	 * debug method, sometimes used to check what's going on with the runnable jar
	 */
	public void printClassPath() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader) cl).getURLs();
		for (URL url : urls) {
			System.out.println(url.getFile());
		}
	}

	/**
	 * Start is where the visualization begins. The primary stage and scene are built by InitRootLayout.
	 * This creates a bare window in which the main application will be placed. It also displays this bare window.
	 * Then ShowMainWindow creates a window to be located inside the root. It loads the FXML information and starts a ViewManager (aka 'Controller') to manage
	 * the display.
	 * <p>
	 * It also builds the dynamic tables, but does not display them.
	 */

	@Override public void start(Stage primaryStage) {
		// set up the logger files and log the fact that we have started

		Reporter.initialiseLoggerFiles();
		logger.debug("CAPITALISM STARTUP: Entered the start procedure");

		// initiaise all the persistent entities

		dataHandler.initialiseDataBaseAndStart();
		dataManager.startup();
		selectionsProvider = new SelectionsProvider();
		initializeActionStates();
		
		// initialise the values and prices of the stocks and the usevalues, and the global totals.
		// this is needed because the information in the user-supplied data files is incomplete - it provides only the bare minimum
		// this is done for all projects, so we can subsequently switch from one to the other.
		// TODO if there are many projects, we may want to do this lazily

		simulation.startup();

		// set up the display (also creates viewManager, see comments)

		Capitalism.primaryStage = primaryStage;
		Capitalism.primaryStage.setTitle("Capitalism");
		initRootLayout();
		showMainWindow();
	}

	/**
	 * Initialise the {@link ActionStates ActionStates } enum class. This class is central to the operation of the simulation.
	 * It defines the possible actions of the simulation, which are of two types: (1) sub-actions such as {@link Demand Demand} which carry out
	 * primitive actions;(2) super-actions such as {@link PreTrade PreTrade} which conducts a series of primitive actions. The super-Actions correspond to normal
	 * economic phases or 'aspects' of the reproduction of an economy namely the purchase of commodities, the production of commodities, and the distribution of
	 * revenues. The primitives don't have a distinct economic meaning but can help understand the logical components of a complete economic activity. This is
	 * given visual representation at several places in the simulation, notably in the logfile and in the actionButtons, which have a tree structure so that the
	 * user may decide either to study the detail of a phase or simply the it overall effects.
	 */
	public void initializeActionStates() {
		
		ActionStates.M_C_PreTrade.setSuccessor(ActionStates.C_P_Produce);
		ActionStates.C_P_Produce.setSuccessor(ActionStates.C_M_Distribute);
		ActionStates.C_M_Distribute.setSuccessor(ActionStates.M_C_PreTrade);

		ActionStates.M_C_Demand.setSuccessor(ActionStates.M_C_Constrain);
		ActionStates.M_C_Constrain.setSuccessor(ActionStates.M_C_Trade);
		ActionStates.M_C_Trade.setSuccessor(ActionStates.C_P_Produce);
		ActionStates.C_P_IndustriesProduce.setSuccessor(ActionStates.C_P_ClassesReproduce);
		ActionStates.C_P_ClassesReproduce.setSuccessor(ActionStates.C_P_ImmediateConsequences);
		ActionStates.C_P_ImmediateConsequences.setSuccessor(ActionStates.C_M_Distribute);
		ActionStates.C_M_Revenue.setSuccessor(ActionStates.C_M_Accumulate);
		ActionStates.C_M_Accumulate.setSuccessor(ActionStates.M_C_PreTrade);
		
		ActionStates.M_C_PreTrade.setPermissibleSubAction(ActionStates.M_C_Demand);
		ActionStates.C_P_Produce.setPermissibleSubAction(ActionStates.C_P_IndustriesProduce);
		ActionStates.C_M_Distribute.setPermissibleSubAction(ActionStates.C_M_Revenue);

		ActionStates.M_C_Demand.setParent(ActionStates.M_C_PreTrade);
		ActionStates.M_C_Constrain.setParent(ActionStates.M_C_PreTrade);
		ActionStates.M_C_Trade.setParent(ActionStates.M_C_PreTrade);
		ActionStates.C_P_IndustriesProduce.setParent(ActionStates.C_P_Produce);
		ActionStates.C_P_ClassesReproduce.setParent(ActionStates.C_P_Produce);
		ActionStates.C_P_ImmediateConsequences.setParent(ActionStates.C_P_Produce);
		ActionStates.C_M_Revenue.setParent(ActionStates.C_M_Distribute);
		ActionStates.C_M_Accumulate.setParent(ActionStates.C_M_Distribute);
	
	}

	/**
	 * Initialize the root layout.
	 */

	public void initRootLayout() {
		logger.debug("Entered InitRootLayout to build the root window");
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Capitalism.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();
			Scene scene = new Scene(rootLayout);
			String css=getClass().getResource("/SimulationTheme.css").toExternalForm();
			scene.getStylesheets().add(css);
			primaryStage.setScene(scene);
//			primaryStage.setX(Screen.getPrimary().getBounds().getMaxX() * 0.3);
			primaryStage.setY(0);
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Show the main window inside the root layout.
	 */

	public void showMainWindow() {
		logger.debug("Entered showMainWindow");
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Capitalism.class.getResource("view/SimulationOverview.fxml"));
			AnchorPane simulationOverview = (AnchorPane) loader.load();
			rootLayout.setCenter(simulationOverview);			// Set the overview stage into the center of the root layout.

			// THIS IS THE POINT AT WHICH THE VIEWMANAGER IS FIRED UP.
			// the loaders getController method is in effect a factory, which uses the information in the @FXML declarations, and simulationOverview.fxml, to
			// initialise all the display controls

			viewManager = loader.getController();
			logger.debug("ViewManager has been contructed");
			viewManager.setUp(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the main stage.
	 */

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public ViewManager getViewManager() {
		return viewManager;
	}

	public Simulation getSimulation() {
		return simulation;
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	public ObservableListProvider olFactory() {
		return olProvider;
	}

	public DBHandler getDBHandler() {
		return dataHandler;
	}
}
