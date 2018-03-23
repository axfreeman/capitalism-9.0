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
import capitalism.view.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Capitalism extends Application {
	private static final Logger logger = LogManager.getLogger(Capitalism.class);
	private static DBHandler dataHandler = new DBHandler();		// handles the initial database transactions such as creation and initialisation
	private static String userBasePath = System.getProperty("user.home").replace('\\', '/') + "/Documents/Capsim/";
	
	/**
	 * The main class extends the javafx class 'Application' and therefore inherits {@code launch()} which is where the action begins
	 * However, there ain't no such thing as a free launch.
	 * The Order is:
	 * 1. call {@link #main(String[])} which calls the preloader to display the splash screen 
	 *    and instruct it to start {@link Capitalism#main(String[])} when it is ready
	 * 2. call super#init() unless overeridden - which, in this case, it is.
	 *    call {@link #init()} instead, since this overrides super.init()
	 * 3. call {@link #start} (see below)
	 * 4. wait until finished - either because of Platform#exit or because last window is closed 
	 *    (provided Platform.implicitExit attribute is true)
	 * 5. call {@link #stop()} to turn out the lights (also does nothing unless overridden)
	 * see https://docs.oracle.com/javase/8/javafx/api/javafx/application/Application.html
	 * 
	 * @param args
	 *            the arguments supplied by the external caller. Ignored in this application
	 */

	public static void main(String[] args) {
		System.out.println("Capitalism Rosy Dawn");
		// launch the preloader which displays the splash screen
		LauncherImpl.launchApplication(Capitalism.class, ApplicationPreloader.class, args);
	}

	@Override public void init() throws Exception {
		System.out.println("entered init, thread: " + Thread.currentThread().getName());
		logger.debug("DEBUG STARTUP");
	}

	// Constructor is called after BEFORE_LOAD.
	public Capitalism() {
		System.out.println("Capitalism constructor called, thread: " + Thread.currentThread().getName());
	}

	/**
	 * A slightly complex procedure but necessary as far as I can see.
	 * {@code primaryStage} is supplied by the JavaFX launcher. We simply pass it to the viewManager statically,
	 * because there is no need to instantiate multiplie copies of the primary stage (or it wouldn't be primary, would it?)
	 * Next, create the viewManager. There's only a single instance of it.
	 * The constructor doesn't do much, but it does initialise a root layout which is a container for most of the
	 * display, and it creates the LogWindow (again, a single instance) which is used for reporting. Thus, it must
	 * be done before the next step, initialising the values and prices of the stocks and the commodities, and the global totals.
	 * This, performed by the Simulation class static startup method, is needed because the information in the user-supplied
	 * data files is incomplete - it provides only the bare minimum. It is done before viewManager constructs most of its
	 * custom controls (the display tables, buttons, etc) because to do this, viewManager needs the fully-initialised data.
	 * Initialization is performed for all projects, so we can subsequently switch from one to the other.
	 * Finally we ask the ViewManager to populate the display with the various custom controls that it manages.
	 */

	@Override public void start(Stage primaryStage) {
		ViewManager.buildMainView(primaryStage);
		
		// Create the database and read in the user-defined persistent entities
		
		if (!DBHandler.initialiseDataBaseAndStart()) {
			logger.error("Data error on startup. Sorry, could not continue");
			return;
		}
		if (!Simulation.startup()) {
			// TODO we may wish to take some other action if there is a database flaw
		}
		ViewManager.startUp();
		ViewManager.getPrimaryStage().centerOnScreen();
        ViewManager.getPrimaryStage().show();
        EditorManager.buildEditorWindow();
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
