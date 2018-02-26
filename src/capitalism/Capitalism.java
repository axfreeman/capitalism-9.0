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

package capitalism;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import capitalism.utils.DBHandler;
import capitalism.utils.Reporter;
import capitalism.view.ViewManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Capitalism extends Application {

	private static final Logger logger = LogManager.getLogger(Capitalism.class);
	public static DBHandler dataHandler = new DBHandler();			// handles the initial database transactions such as creation and initialisation

	public static ViewManager viewManager = null; 					// controls the display
	/**
	 * the base for all files that are created in, or copied into, the user's file system
	 */
	public static String userBasePath = System.getProperty("user.home").replace('\\', '/') + "/Documents/Capsim/";

	// graphic elements that need to be accessed by several different managers

	private static Stage primaryStage;
	private static BorderPane rootLayout = new BorderPane();

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
	 * Then ShowMainWindow creates a window to be located inside the root. It loads the FXML information and starts 
	 * a ViewManager (aka 'Controller') to manage the display.
	 * 
	 * It also builds the dynamic tables, but does not display them.
	 */

	@Override public void start(Stage primaryStage) {
		// set up the logger files and log the fact that we have started

		Reporter.initialiseLoggerFiles();
		logger.debug("CAPITALISM STARTUP: Entered the start procedure");

		// initiaise all the persistent entities

		dataHandler.initialiseDataBaseAndStart();
		
		// Initialise the values and prices of the stocks and the commodities, and the global totals.
		// This is needed because the information in the user-supplied data files is incomplete - it provides only the bare minimum.
		// It has to be done before viewManager starts up because it needs to access fully-initialised data.
		// Initialization is performed for all projects, so we can subsequently switch from one to the other.

		Simulation.startup();

		// set up the display (also creates viewManager, see comments)

		Capitalism.primaryStage = primaryStage;
		Capitalism.primaryStage.setTitle("Capitalism");
		initRootLayout();
		showMainWindow();
	}

	/**
	 * Initialize the root layout.
	 */

	public void initRootLayout() {
			rootLayout.setPrefHeight(800);
			rootLayout.setPrefWidth(1300);
			Scene scene = new Scene(rootLayout);
			String css=getClass().getResource("/SimulationTheme.css").toExternalForm();
			scene.getStylesheets().add(css);
			primaryStage.setScene(scene);
			primaryStage.setY(0);
			primaryStage.show();
	}

	/**
	 * Show the main window inside the root layout.
	 */

	public void showMainWindow() {
		logger.debug("Entered showMainWindow");
		AnchorPane simulationOverview = new AnchorPane();
		simulationOverview.setPrefHeight(800);
		simulationOverview.setPrefWidth(1300);
		rootLayout.setCenter(simulationOverview);			// Set the overview stage into the center of the root layout.
		viewManager=new ViewManager(simulationOverview);
		viewManager.setUp(this);
		}

	/**
	 * @return the main stage.
	 */

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public DBHandler getDBHandler() {
		return dataHandler;
	}
}
