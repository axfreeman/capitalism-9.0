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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.javafx.application.LauncherImpl;

import capitalism.controller.Simulation;
import capitalism.editor.EditorManager;
import capitalism.utils.DBHandler;
import capitalism.utils.Dialogues;
import capitalism.utils.Reporter;
import capitalism.view.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Capitalism extends Application {
	private static final Logger logger = LogManager.getLogger(Capitalism.class);
	private static DBHandler dataHandler = new DBHandler();		// handles the initial database transactions such as creation and initialisation
	private static String userBasePath = System.getProperty("user.home").replace('\\', '/') + "/Documents/Capsim/";
	
	/**
	 * The main class extends the javafx class 'Application' and therefore inherits {@code launch()} which is where the action begins.
	 * There ain't no such thing as a free launch.
	 * The order is:
	 * 1. launch calls {@link #main(String[])} which calls the preloader to display the splash screen 
	 *    and instruct it to start {@link Capitalism#main(String[])} when it is ready
	 * 2. super#init() gets called unless it is overeridden - which, in this case, it is.
	 *    call {@link #init()} instead, since this overrides super.init()
	 * 3. call {@link #start} where we currently do most of the heavy lifting
	 * 4. wait until finished - either because of Platform#exit or because last window is closed 
	 *    (provided Platform.implicitExit attribute is true)
	 * 5. call {@link #stop()} to turn out the lights (also does nothing unless overridden)
	 * see https://docs.oracle.com/javase/8/javafx/api/javafx/application/Application.html
	 * 
	 * @param args
	 *            the arguments supplied by the external caller. Ignored in this application
	 */
	public static void main(String[] args) {
		logger.debug("Startup: Entered Capitalism#main");
		Reporter.setStartTime();// start the benchmarking clock

		// launch the preloader which displays the splash screen
		LauncherImpl.launchApplication(Capitalism.class, SplashScreenPreLoader.class, args);
	}

	/**
	 * The {@link Capitalism#init()} method doesn't actually do anything because we do all the heavy lifting in 
	 * {@link Capitalism#start(Stage)}; we keep it so if later we want to do some heavy lifting here, the 
	 * possibility exists.
	 */
	@Override public void init() throws Exception {
		logger.debug("Entered init, thread: " + Thread.currentThread().getName());
	}

	/**
	 * The Capitalism constructor is called by ApplicationPreloader
	 */
	public Capitalism() {
		System.out.println("Capitalism constructor called, thread: " + Thread.currentThread().getName());
	}

	/**
	 * The preloader splash screen is already up and probably showing by now.
	 * First build the main window and the log window, but do not populate them and do not show them.
	 * Then, initialise the database, which has to be done before the other windows can be populated.
	 * Then start the simulation itself, which preprocesses the data and also must complete before populating any windows
	 * Then populate the windows main window and the log window
	 * Then build and populate the editor window in one go
	 * TODO rationalise the editor window process to bring it in line with the others
	 */
	@Override public void start(Stage primaryStage) {
		ViewManager.buildMainWindow(primaryStage);
		Reporter.createLogWindow();
		
		SplashScreenPreLoader.setProgress("Loading data");
		logger.debug("Starting database after {} milliseconds",Reporter.timeSinceStart());
		// Create the database and read in the user-defined persistent entities
		if (!DBHandler.initialiseDataBaseAndStart()) {
			Dialogues.alert(logger, "Data error on startup. Sorry, could not continue");
			return;
		}

		SplashScreenPreLoader.setProgress("Initialising Simulation");
		logger.debug("Starting simulation after {} milliseconds",Reporter.timeSinceStart());

		if (!Simulation.startup()) {
			// TODO we may wish to take some other action if there is a database flaw
		}
		SplashScreenPreLoader.setProgress("Initialising the display");
		logger.debug("Starting view manager after {} milliseconds",Reporter.timeSinceStart());

		ViewManager.startUp();
		ViewManager.getPrimaryStage().centerOnScreen();
        ViewManager.getPrimaryStage().show();
        EditorManager.buildEditorWindow();
		SplashScreenPreLoader.setProgress("Ready");
		logger.debug("Finished initialising after {} milliseconds",Reporter.timeSinceStart());
        EditorManager.showEditorWindow();
	}

	/**
	 * @return the data handler.
	 */

	public static DBHandler getDBHandler() {
		return dataHandler;
	}

	/**
	 * Returns the base for all files that are created in, or copied into, the user's file system.
	 * TODO Currently a fixed location, but could be made configurable
	 * 
	 * @return the userBasePath
	 */
	public static String getUserBasePath() {
		return userBasePath;
	}
}
